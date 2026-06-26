package com.tonic.vellum;

import com.tonic.vellum.focus.Navigation;
import com.tonic.vellum.geom.Point;
import com.tonic.vellum.geom.Rect;
import com.tonic.vellum.input.Key;
import com.tonic.vellum.input.KeyEvent;
import com.tonic.vellum.terminal.Terminal;
import com.tonic.vellum.terminal.TerminalSize;
import com.tonic.vellum.terminal.Terminals;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Owns the terminal, the single UI thread / event loop, layout, repaint, focus, and
 * resize. Background threads touch the UI only through {@link #post(Runnable)}, which
 * marshals work onto the UI thread - the framework's single concurrency primitive.
 */
public final class App {

    private static final long POLL_MILLIS = 16;

    private static volatile App current;

    private final Host host = new EngineHost();
    private final Stage stage;
    private final Section root;
    private final FocusManager focus;
    private final Section initialFocus;
    private final Predicate<KeyEvent> quitPredicate;
    private final Consumer<Throwable> errorHandler;
    private final ConcurrentLinkedQueue<Runnable> tasks = new ConcurrentLinkedQueue<>();

    private Terminal terminal;
    private Renderer renderer;
    private Buffer back;

    private ScheduledExecutorService scheduler;
    private Thread uiThread;
    private Throwable firstUnhandledError;
    private boolean cursorShown;
    private volatile boolean running;
    private volatile boolean resizePending;
    private boolean needsRepaint;

    private App(Builder b) {
        this.stage = new Stage(b.root);
        this.root = stage;
        this.initialFocus = b.initialFocus;
        this.quitPredicate = b.quitPredicate;
        this.errorHandler = b.errorHandler;
        this.terminal = b.terminal;
        this.focus = new FocusManager(new ArrayList<>(b.focusOrder), b.navigation, host);
    }

    public static Builder builder() {
        return new Builder();
    }

    /** The running instance, or {@code null} when no app is running. */
    public static App current() {
        return current;
    }

    /**
     * Install the terminal, run the event loop until {@link #quit()}, and restore the
     * terminal on exit - even if an exception propagates.
     */
    public void run() {
        uiThread = Thread.currentThread();
        current = this;
        if (terminal == null) {
            terminal = Terminals.system();
        }
        running = true;
        // Restore the terminal even if the process is killed (the finally below does not run on SIGTERM).
        Thread restoreHook = new Thread(terminal::restore, "vellum-restore");
        Runtime.getRuntime().addShutdownHook(restoreHook);
        try {
            terminal.setResizeListener(() -> resizePending = true);
            terminal.enterRawMode();
            terminal.enterAlternateScreen();
            terminal.hideCursor();

            TerminalSize size = terminal.size();
            allocate(size);
            root.resizeRoot(new Rect(0, 0, size.columns(), size.rows()));
            root.mountAsRoot(host);
            focus.start(initialFocus);
            repaint(true);

            loop();
        } finally {
            shutdownScheduler();
            try {
                root.unmountAsRoot();
            } catch (Throwable t) {
                handleError(t);
            }
            terminal.showCursor();
            terminal.flush();
            terminal.restore();
            removeShutdownHook(restoreHook);
            current = null;
            if (errorHandler == null && firstUnhandledError != null) {
                firstUnhandledError.printStackTrace();
            }
        }
    }

    private static void removeShutdownHook(Thread hook) {
        try {
            Runtime.getRuntime().removeShutdownHook(hook);
        } catch (IllegalStateException shuttingDown) {
            // the JVM is already shutting down; the hook will run and is idempotent
        }
    }

    private void loop() {
        while (running) {
            drainTasks();
            KeyEvent key = terminal.readKey(POLL_MILLIS);
            while (key != null && running) {
                handleKey(key);
                key = terminal.readKey(0); // drain any buffered burst input this frame
            }
            if (resizePending) {
                resizePending = false;
                applyResize();
            }
            if (needsRepaint) {
                needsRepaint = false;
                repaint(false);
            }
        }
    }

    /** Stop the loop and let {@link #run()} restore the terminal. */
    public void quit() {
        running = false;
    }

    /** Marshal a runnable onto the UI thread. Safe to call from any thread. */
    public void post(Runnable task) {
        tasks.add(task);
    }

    /**
     * Open a modal overlay: render {@code content} on top of the UI at {@code placement}
     * and route keys to it (Tab cycles {@code focusTargets}, or the content itself if none
     * are given). UI-thread-only. Close overlays in reverse order of opening.
     */
    public OverlayHandle openOverlay(Section content, Placement placement, Section... focusTargets) {
        stage.addOverlay(content, placement);
        List<Section> order = focusTargets.length > 0
                ? Arrays.asList(focusTargets)
                : Collections.singletonList(content);
        focus.pushScope(new ArrayList<>(order), null, order.get(0));
        return new OverlayHandleImpl(content);
    }

    /** Schedule a one-shot task on the UI thread after {@code delay}. */
    public Cancellable schedule(Duration delay, Runnable task) {
        ScheduledFuture<?> future = scheduler().schedule(
                () -> post(task), delay.toMillis(), TimeUnit.MILLISECONDS);
        return new FutureCancellable(future);
    }

    /** Schedule a repeating task on the UI thread. */
    public Cancellable scheduleAtFixedRate(Duration initialDelay, Duration period, Runnable task) {
        ScheduledFuture<?> future = scheduler().scheduleAtFixedRate(
                () -> post(task), initialDelay.toMillis(), period.toMillis(), TimeUnit.MILLISECONDS);
        return new FutureCancellable(future);
    }

    // ---- loop internals ----

    private void drainTasks() {
        Runnable task;
        while ((task = tasks.poll()) != null) {
            try {
                task.run();
            } catch (Throwable t) {
                handleError(t);
            }
        }
    }

    private void handleKey(KeyEvent key) {
        if (quitPredicate != null && quitPredicate.test(key)) {
            quit();
            return;
        }
        try {
            focus.dispatchKey(key);
        } catch (Throwable t) {
            handleError(t);
        }
    }

    /**
     * Route an exception thrown by user code (a task, key handler, or render) to the
     * configured handler, or remember the first one to print after the terminal is
     * restored. The loop keeps running so one broken section cannot take down the UI.
     */
    private void handleError(Throwable t) {
        if (errorHandler != null) {
            try {
                errorHandler.accept(t);
            } catch (Throwable ignored) {
                // a throwing error handler must not crash the loop
            }
        } else if (firstUnhandledError == null) {
            firstUnhandledError = t;
        }
    }

    private void applyResize() {
        TerminalSize size = terminal.size();
        allocate(size);
        root.resizeRoot(new Rect(0, 0, size.columns(), size.rows()));
        repaint(true);
    }

    private void allocate(TerminalSize size) {
        back = new Buffer(size.columns(), size.rows());
        if (renderer == null) {
            renderer = new Renderer(size.columns(), size.rows());
        } else {
            renderer.resize(size.columns(), size.rows());
        }
    }

    private void repaint(boolean full) {
        renderTree(root, full);
        String out = renderer.flush(back);
        if (!out.isEmpty()) {
            terminal.write(out);
        }
        updateCursor();
        terminal.flush();
    }

    /** Position the hardware cursor from the focused section's cursor hint, or hide it. */
    private void updateCursor() {
        Section focused = focus.focusedSection();
        Point local = focused != null ? safeCursor(focused) : null;
        if (local != null) {
            Rect b = focused.bounds();
            int cx = b.x() + local.x();
            int cy = b.y() + local.y();
            if (b.contains(cx, cy)) {
                terminal.moveCursor(cx, cy);
                if (!cursorShown) {
                    terminal.showCursor();
                    cursorShown = true;
                }
                return;
            }
        }
        if (cursorShown) {
            terminal.hideCursor();
            cursorShown = false;
        }
    }

    private Point safeCursor(Section focused) {
        try {
            return focused.cursor();
        } catch (Throwable t) {
            handleError(t);
            return null;
        }
    }

    private void renderTree(Section section, boolean full) {
        if (full || section.isDirty()) {
            try {
                section.renderInto(back);
            } catch (Throwable t) {
                handleError(t);
            }
        }
        for (Section child : section.children()) {
            renderTree(child, full);
        }
    }

    private ScheduledExecutorService scheduler() {
        if (scheduler == null) {
            scheduler = Executors.newSingleThreadScheduledExecutor(daemonFactory());
        }
        return scheduler;
    }

    private void shutdownScheduler() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    private static ThreadFactory daemonFactory() {
        return r -> {
            Thread t = new Thread(r, "vellum-scheduler");
            t.setDaemon(true);
            return t;
        };
    }

    /** The engine seam handed to sections and the focus manager; not part of the public API. */
    private final class EngineHost implements Host {
        @Override
        public void requestRepaint() {
            needsRepaint = true;
        }

        @Override
        public void assertUiThread() {
            if (Thread.currentThread() != uiThread) {
                throw new IllegalStateException(
                        "Section UI methods must run on the UI thread; use App.post(...) from other threads");
            }
        }

        @Override
        public boolean isOnFocusPath(Section section) {
            return focus.isOnFocusPath(section);
        }

        @Override
        public void refreshFocus() {
            focus.refresh();
        }
    }

    private final class OverlayHandleImpl implements OverlayHandle {
        private final Section content;
        private boolean open = true;

        OverlayHandleImpl(Section content) {
            this.content = content;
        }

        @Override
        public void close() {
            if (!open) {
                return;
            }
            open = false;
            stage.removeOverlay(content);
            focus.popScope();
        }

        @Override
        public boolean isOpen() {
            return open;
        }
    }

    private static final class FutureCancellable implements Cancellable {
        private final ScheduledFuture<?> future;

        FutureCancellable(ScheduledFuture<?> future) {
            this.future = future;
        }

        @Override
        public void cancel() {
            future.cancel(false);
        }

        @Override
        public boolean isCancelled() {
            return future.isCancelled();
        }
    }

    /** Fluent builder for an {@link App}. */
    public static final class Builder {
        private Section root;
        private final List<Section> focusOrder = new ArrayList<>();
        private Navigation navigation;
        private Section initialFocus;
        private Predicate<KeyEvent> quitPredicate;
        private Consumer<Throwable> errorHandler;
        private Terminal terminal;

        public Builder root(Section root) {
            this.root = root;
            return this;
        }

        public Builder focusOrder(Section... targets) {
            this.focusOrder.clear();
            this.focusOrder.addAll(Arrays.asList(targets));
            return this;
        }

        public Builder navigation(Navigation navigation) {
            this.navigation = navigation;
            return this;
        }

        public Builder initialFocus(Section target) {
            this.initialFocus = target;
            return this;
        }

        /** Quit when a key matches the predicate. */
        public Builder onQuit(Predicate<KeyEvent> predicate) {
            this.quitPredicate = predicate;
            return this;
        }

        /** Convenience: quit on a specific logical key. */
        public Builder onQuitKey(Key key) {
            return onQuit(e -> e.is(key));
        }

        /** Convenience: quit on a specific printable character. */
        public Builder onQuitKey(char ch) {
            return onQuit(e -> e.is(Key.CHAR) && e.ch() == ch);
        }

        /**
         * Handle exceptions thrown by a task, key handler, or section render, on the UI
         * thread. Without one, the loop keeps running and the first error is printed after
         * the terminal is restored.
         */
        public Builder onError(Consumer<Throwable> handler) {
            this.errorHandler = handler;
            return this;
        }

        /** Inject a terminal driver (used by tests). Package-private. */
        Builder useTerminal(Terminal terminal) {
            this.terminal = terminal;
            return this;
        }

        public App build() {
            if (root == null) {
                throw new IllegalStateException("App requires a root section");
            }
            return new App(this);
        }
    }
}
