package com.tonic.vellum;

import com.tonic.vellum.input.Key;
import com.tonic.vellum.input.KeyEvent;
import com.tonic.vellum.layout.Constraint;
import com.tonic.vellum.layout.Slot;
import com.tonic.vellum.layout.Split;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class AppRunLoopTest {

    /** A focusable leaf that records the keys, focus events, and mount it receives. */
    static class Recorder extends Section {
        final List<KeyEvent> keys = new ArrayList<>();
        int focusGained;
        int focusLost;
        CountDownLatch mountedLatch;

        @Override
        protected void render(Canvas canvas) {
            canvas.put(0, 0, isFocused() ? '#' : '.');
        }

        @Override
        protected KeyResult onKey(KeyEvent key) {
            if (key.is(Key.TAB) || key.is(Key.SHIFT_TAB)) {
                return KeyResult.UNHANDLED; // let the focus manager move focus
            }
            keys.add(key);
            return KeyResult.CONSUMED;
        }

        @Override
        protected void onFocusGained() {
            focusGained++;
        }

        @Override
        protected void onFocusLost() {
            focusLost++;
        }

        @Override
        protected void onMount() {
            if (mountedLatch != null) {
                mountedLatch.countDown();
            }
        }
    }

    private static Thread runInBackground(App app) {
        Thread t = new Thread(app::run, "vellum-ui-test");
        t.setDaemon(true);
        t.start();
        return t;
    }

    @Test
    void tabAdvancesFocusAndFiresHooks() throws Exception {
        Recorder a = new Recorder();
        Recorder b = new Recorder();
        Section root = Split.horizontal(
                Slot.of(Constraint.fill(), a),
                Slot.of(Constraint.fill(), b));
        FakeTerminal term = new FakeTerminal(20, 5);
        App app = App.builder()
                .root(root)
                .focusOrder(a, b)
                .onQuitKey('q')
                .useTerminal(term)
                .build();

        Thread ui = runInBackground(app);
        term.send(KeyEvent.special(Key.TAB));      // a -> b
        term.send(KeyEvent.character('x'));         // delivered to b
        term.send(KeyEvent.character('q'));         // quit
        ui.join(2000);

        assertFalse(ui.isAlive(), "run loop should have exited");
        assertEquals(1, a.focusGained); // initial focus
        assertEquals(1, a.focusLost);   // lost on TAB
        assertEquals(1, b.focusGained); // gained on TAB
        assertEquals(1, b.keys.size());
        assertEquals('x', b.keys.get(0).ch());
        assertTrue(a.keys.isEmpty());
    }

    @Test
    void quitFullyRestoresTerminal() throws Exception {
        Recorder a = new Recorder();
        FakeTerminal term = new FakeTerminal(10, 3);
        App app = App.builder()
                .root(a)
                .focusOrder(a)
                .onQuitKey('q')
                .useTerminal(term)
                .build();

        Thread ui = runInBackground(app);
        term.send(KeyEvent.character('q'));
        ui.join(2000);

        assertFalse(ui.isAlive());
        assertTrue(term.restored, "terminal must be restored");
        assertFalse(term.rawMode, "raw mode must be off after restore");
        assertFalse(term.cursorHidden, "cursor must be shown after restore");
        assertNull(App.current(), "current app cleared after run");
    }

    @Test
    void offThreadRequestRedrawIsRejected() throws Exception {
        Recorder a = new Recorder();
        a.mountedLatch = new CountDownLatch(1);
        FakeTerminal term = new FakeTerminal(10, 3);
        App app = App.builder()
                .root(a)
                .focusOrder(a)
                .onQuitKey('q')
                .useTerminal(term)
                .build();

        Thread ui = runInBackground(app);
        assertTrue(a.mountedLatch.await(2, TimeUnit.SECONDS), "section should mount");

        assertThrows(IllegalStateException.class, a::requestRedraw);

        app.post(app::quit);
        ui.join(2000);
        assertFalse(ui.isAlive());
    }

    @Test
    void postRunsTaskOnUiThreadAndRepaints() throws Exception {
        Recorder a = new Recorder();
        a.mountedLatch = new CountDownLatch(1);
        FakeTerminal term = new FakeTerminal(10, 3);
        App app = App.builder()
                .root(a)
                .focusOrder(a)
                .onQuitKey('q')
                .useTerminal(term)
                .build();

        Thread ui = runInBackground(app);
        assertTrue(a.mountedLatch.await(2, TimeUnit.SECONDS));

        AtomicReference<Thread> taskThread = new AtomicReference<>();
        app.post(() -> {
            taskThread.set(Thread.currentThread());
            a.requestRedraw(); // must be legal on the UI thread
        });
        app.post(app::quit);
        ui.join(2000);

        assertFalse(ui.isAlive());
        assertEquals("vellum-ui-test", taskThread.get().getName());
        assertFalse(term.output.length() == 0, "initial paint should have produced output");
    }
}
