package com.tonic.vellum;

import com.tonic.vellum.input.Key;
import com.tonic.vellum.input.KeyEvent;
import com.tonic.vellum.style.Style;
import com.tonic.vellum.widget.MenuSection;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;

class OverlayTest {

    static final class Panel extends Section {
        final AtomicInteger mounts = new AtomicInteger();
        final AtomicInteger unmounts = new AtomicInteger();
        final AtomicInteger keys = new AtomicInteger();

        @Override
        protected void render(Canvas canvas) {
            canvas.fill(canvas.bounds(), 'O', Style.NORMAL);
        }

        @Override
        protected KeyResult onKey(KeyEvent key) {
            keys.incrementAndGet();
            return KeyResult.CONSUMED;
        }

        @Override
        protected void onMount() {
            mounts.incrementAndGet();
        }

        @Override
        protected void onUnmount() {
            unmounts.incrementAndGet();
        }
    }

    private static Thread run(App app) {
        Thread t = new Thread(app::run, "vellum-ui-test");
        t.setDaemon(true);
        t.start();
        return t;
    }

    private static boolean await(BooleanSupplier cond, long timeoutMillis) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
        while (System.nanoTime() < deadline) {
            if (cond.getAsBoolean()) {
                return true;
            }
            Thread.sleep(5);
        }
        return false;
    }

    /** Read a value on the UI thread and return it. */
    private static <T> T onUi(App app, java.util.function.Supplier<T> supplier) throws InterruptedException {
        AtomicReference<T> result = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        app.post(() -> {
            result.set(supplier.get());
            done.countDown();
        });
        assertTrue(done.await(2, TimeUnit.SECONDS));
        return result.get();
    }

    @Test
    void overlayCapturesFocusModallyThenRestores() throws Exception {
        MenuSection menu = new MenuSection("a", "b", "c");
        Panel panel = new Panel();
        FakeTerminal term = new FakeTerminal(30, 10);
        App app = App.builder()
                .root(menu)
                .focusOrder(menu)
                .onQuit(e -> e.is(Key.ESCAPE))
                .useTerminal(term)
                .build();

        Thread ui = run(app);

        AtomicReference<OverlayHandle> handle = new AtomicReference<>();
        handle.set(onUi(app, () -> app.openOverlay(panel, Placement.centered(10, 5))));
        assertTrue(await(() -> panel.mounts.get() == 1, 2000), "overlay mounted");

        // a key the menu would consume goes to the overlay instead; the menu is untouched
        term.send(KeyEvent.special(Key.DOWN));
        assertTrue(await(() -> panel.keys.get() == 1, 2000), "overlay received the key");
        assertEquals(0, (int) onUi(app, menu::selectedIndex), "base menu did not receive the key");
        assertTrue(onUi(app, panel::isFocused), "overlay content is focused");
        assertFalse(onUi(app, menu::isFocused), "base is parked while overlay is open");

        // close: focus returns to the base
        app.post(() -> handle.get().close());
        assertTrue(await(() -> panel.unmounts.get() == 1, 2000), "overlay unmounted on close");

        term.send(KeyEvent.special(Key.DOWN));
        assertTrue(await(() -> {
            try {
                return onUi(app, menu::selectedIndex) == 1;
            } catch (InterruptedException e) {
                return false;
            }
        }, 2000), "base menu receives keys again after close");
        assertTrue(onUi(app, menu::isFocused), "base regained focus");

        term.send(KeyEvent.special(Key.ESCAPE));
        ui.join(2000);
        assertFalse(ui.isAlive());
        assertEquals(1, panel.mounts.get());
        assertEquals(1, panel.unmounts.get());
    }

    @Test
    void overlayRendersOnTopOfBase() throws Exception {
        MenuSection menu = new MenuSection("alpha");
        Panel panel = new Panel();
        FakeTerminal term = new FakeTerminal(20, 8);
        App app = App.builder()
                .root(menu)
                .focusOrder(menu)
                .onQuit(e -> e.is(Key.ESCAPE))
                .useTerminal(term)
                .build();

        Thread ui = run(app);
        onUi(app, () -> app.openOverlay(panel, Placement.centered(6, 3)));
        assertTrue(await(() -> {
            synchronized (term.output) {
                return term.output.toString().contains("OOOOOO");
            }
        }, 2000), "overlay cells were drawn");

        term.send(KeyEvent.special(Key.ESCAPE));
        ui.join(2000);
        assertFalse(ui.isAlive());
    }
}
