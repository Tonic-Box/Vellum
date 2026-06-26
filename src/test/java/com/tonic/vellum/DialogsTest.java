package com.tonic.vellum;

import com.tonic.vellum.input.Key;
import com.tonic.vellum.input.KeyEvent;
import com.tonic.vellum.widget.Dialogs;
import com.tonic.vellum.widget.MenuSection;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;

class DialogsTest {

    private static Thread run(App app) {
        Thread t = new Thread(app::run, "vellum-ui-test");
        t.setDaemon(true);
        t.start();
        return t;
    }

    private static boolean await(BooleanSupplier cond, long ms) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(ms);
        while (System.nanoTime() < deadline) {
            if (cond.getAsBoolean()) {
                return true;
            }
            Thread.sleep(5);
        }
        return false;
    }

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

    private App app;
    private Thread ui;
    private FakeTerminal term;

    private void start() {
        MenuSection base = new MenuSection("base");
        term = new FakeTerminal(40, 12);
        app = App.builder().root(base).focusOrder(base).useTerminal(term).build();
        ui = run(app);
    }

    private void stop() throws InterruptedException {
        app.post(app::quit);
        ui.join(2000);
    }

    @Test
    void confirmYesRunsCallbackAndCloses() throws Exception {
        start();
        AtomicBoolean yes = new AtomicBoolean();
        OverlayHandle dialog = onUi(app, () -> Dialogs.confirm(app, "Sure?", () -> yes.set(true)));

        term.send(KeyEvent.special(Key.ENTER)); // Yes is focused first
        assertTrue(await(yes::get, 2000));
        assertFalse(onUi(app, dialog::isOpen));
        stop();
    }

    @Test
    void confirmTabToNoClosesWithoutCallback() throws Exception {
        start();
        AtomicBoolean yes = new AtomicBoolean();
        OverlayHandle dialog = onUi(app, () -> Dialogs.confirm(app, "Sure?", () -> yes.set(true)));

        term.send(KeyEvent.special(Key.TAB));   // Yes -> No
        term.send(KeyEvent.special(Key.ENTER)); // activate No
        assertTrue(await(() -> !runCheck(dialog), 2000));
        assertFalse(yes.get());
        stop();
    }

    @Test
    void escClosesWithoutCallback() throws Exception {
        start();
        AtomicBoolean yes = new AtomicBoolean();
        OverlayHandle dialog = onUi(app, () -> Dialogs.confirm(app, "Sure?", () -> yes.set(true)));

        term.send(KeyEvent.special(Key.ESCAPE));
        assertTrue(await(() -> !runCheck(dialog), 2000));
        assertFalse(yes.get());
        stop();
    }

    @Test
    void promptSubmitsTypedText() throws Exception {
        start();
        AtomicReference<String> submitted = new AtomicReference<>();
        OverlayHandle dialog = onUi(app, () -> Dialogs.prompt(app, "Name", submitted::set));

        term.send(KeyEvent.character('h'));
        term.send(KeyEvent.character('i'));
        term.send(KeyEvent.special(Key.ENTER)); // Enter in the field submits
        assertTrue(await(() -> "hi".equals(submitted.get()), 2000));
        assertFalse(onUi(app, dialog::isOpen));
        stop();
    }

    /** Reads isOpen on the UI thread; returns true if still open. */
    private boolean runCheck(OverlayHandle dialog) {
        try {
            return onUi(app, dialog::isOpen);
        } catch (InterruptedException e) {
            return true;
        }
    }
}
