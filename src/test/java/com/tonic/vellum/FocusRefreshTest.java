package com.tonic.vellum;

import com.tonic.vellum.input.Key;
import com.tonic.vellum.input.KeyEvent;
import com.tonic.vellum.widget.TabHost;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;

class FocusRefreshTest {

    /** Tab content that records Up/Down keys and bubbles Left/Right (so the host switches tabs). */
    static final class Tab extends Section {
        final AtomicInteger keys = new AtomicInteger();

        @Override
        protected void render(Canvas canvas) { }

        @Override
        protected KeyResult onKey(KeyEvent key) {
            if (key.is(Key.UP) || key.is(Key.DOWN)) {
                keys.incrementAndGet();
                return KeyResult.CONSUMED;
            }
            return KeyResult.UNHANDLED;
        }
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

    @Test
    void switchingTabsRoutesKeysAndFocusToTheNewContent() throws Exception {
        Tab a = new Tab();
        Tab b = new Tab();
        TabHost detail = new TabHost().add("A", a).add("B", b);
        FakeTerminal term = new FakeTerminal(30, 10);
        App app = App.builder()
                .root(detail)
                .focusOrder(detail)
                .onQuit(e -> e.is(Key.ESCAPE))
                .useTerminal(term)
                .build();

        Thread ui = new Thread(app::run, "vellum-ui-test");
        ui.setDaemon(true);
        ui.start();

        term.send(KeyEvent.special(Key.RIGHT)); // host (focused) switches A -> B
        term.send(KeyEvent.special(Key.DOWN));  // must reach the new tab, B

        assertTrue(await(() -> b.keys.get() == 1, 2000), "key reached the newly active tab");
        assertEquals(0, a.keys.get(), "old tab no longer receives keys");

        // focus tracks the new content
        AtomicReference<Boolean> aFocused = new AtomicReference<>();
        AtomicReference<Boolean> bFocused = new AtomicReference<>();
        CountDownLatch captured = new CountDownLatch(1);
        app.post(() -> {
            aFocused.set(a.isFocused());
            bFocused.set(b.isFocused());
            captured.countDown();
        });
        assertTrue(captured.await(2, TimeUnit.SECONDS));
        assertTrue(bFocused.get(), "new content is focused");
        assertFalse(aFocused.get(), "old content is not focused");

        term.send(KeyEvent.special(Key.ESCAPE));
        ui.join(2000);
        assertFalse(ui.isAlive());
    }
}
