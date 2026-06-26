package com.tonic.vellum;

import com.tonic.vellum.input.Key;
import com.tonic.vellum.input.KeyEvent;
import com.tonic.vellum.widget.Form;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class FormFocusTest {

    /** A focusable field recording focus hooks and the keys it receives. */
    static final class Field extends Section {
        final AtomicInteger gained = new AtomicInteger();
        final AtomicInteger lost = new AtomicInteger();
        final AtomicInteger keys = new AtomicInteger();

        @Override
        protected void render(Canvas canvas) { }

        @Override
        protected KeyResult onKey(KeyEvent key) {
            if (key.is(Key.CHAR)) {
                keys.incrementAndGet();
                return KeyResult.CONSUMED;
            }
            return KeyResult.UNHANDLED;
        }

        @Override
        protected void onFocusGained() {
            gained.incrementAndGet();
        }

        @Override
        protected void onFocusLost() {
            lost.incrementAndGet();
        }
    }

    private static Thread run(App app) {
        Thread t = new Thread(app::run, "vellum-ui-test");
        t.setDaemon(true);
        t.start();
        return t;
    }

    private static boolean await(java.util.function.BooleanSupplier cond, long ms) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(ms);
        while (System.nanoTime() < deadline) {
            if (cond.getAsBoolean()) {
                return true;
            }
            Thread.sleep(5);
        }
        return false;
    }

    @Test
    void tabCyclesFieldsFiringHooksAndRoutingKeys() throws Exception {
        Field a = new Field();
        Field b = new Field();
        Form form = new Form().addField(a).addField(b);
        FakeTerminal term = new FakeTerminal(20, 6);
        App app = App.builder()
                .root(form)
                .focusOrder(form)
                .onQuit(e -> e.is(Key.ESCAPE))
                .useTerminal(term)
                .build();

        Thread ui = run(app);

        // first field focused at startup
        assertTrue(await(() -> a.gained.get() == 1, 2000));

        term.send(KeyEvent.special(Key.TAB)); // a -> b
        assertTrue(await(() -> b.gained.get() == 1, 2000));
        assertEquals(1, a.lost.get());

        term.send(KeyEvent.character('x')); // delivered to the focused field b
        assertTrue(await(() -> b.keys.get() == 1, 2000));
        assertEquals(0, a.keys.get());

        term.send(KeyEvent.special(Key.TAB)); // b -> wraps back to a
        assertTrue(await(() -> a.gained.get() == 2, 2000));
        assertEquals(1, b.lost.get());

        term.send(KeyEvent.special(Key.ESCAPE));
        ui.join(2000);
        assertFalse(ui.isAlive());
    }
}
