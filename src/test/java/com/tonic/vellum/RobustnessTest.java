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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RobustnessTest {

    private static Thread run(App app) {
        Thread t = new Thread(app::run, "vellum-ui-test");
        t.setDaemon(true);
        t.start();
        return t;
    }

    @Test
    void renderExceptionIsIsolatedAndLoopKeepsRunning() throws Exception {
        AtomicInteger errors = new AtomicInteger();
        Section boom = new Section() {
            @Override
            protected void render(Canvas canvas) {
                throw new RuntimeException("render failure");
            }
        };
        FakeTerminal term = new FakeTerminal(10, 3);
        App app = App.builder()
                .root(boom)
                .focusOrder(boom)
                .onQuitKey('q')
                .onError(t -> errors.incrementAndGet())
                .useTerminal(term)
                .build();

        Thread ui = run(app);
        term.send(KeyEvent.character('q'));
        ui.join(2000);

        assertFalse(ui.isAlive(), "loop survived the render exception and quit cleanly");
        assertTrue(errors.get() >= 1, "render exception was routed to the error handler");
        assertTrue(term.restored, "terminal restored");
    }

    @Test
    void taskExceptionDoesNotStopLaterTasks() throws Exception {
        AtomicInteger errors = new AtomicInteger();
        AtomicBoolean laterRan = new AtomicBoolean();
        Section root = new Section() {
            @Override
            protected void render(Canvas canvas) { }
        };
        FakeTerminal term = new FakeTerminal(10, 3);
        App app = App.builder()
                .root(root)
                .focusOrder(root)
                .onError(t -> errors.incrementAndGet())
                .useTerminal(term)
                .build();

        Thread ui = run(app);
        app.post(() -> { throw new RuntimeException("task failure"); });
        app.post(() -> laterRan.set(true));
        app.post(app::quit);
        ui.join(2000);

        assertFalse(ui.isAlive());
        assertEquals(1, errors.get());
        assertTrue(laterRan.get(), "task after the failing one still ran");
    }

    @Test
    void burstOfKeysIsNotDropped() throws Exception {
        List<Character> seen = new ArrayList<>();
        CountDownLatch mounted = new CountDownLatch(1);
        Section sink = new Section() {
            @Override
            protected void render(Canvas canvas) { }

            @Override
            protected void onMount() {
                mounted.countDown();
            }

            @Override
            protected KeyResult onKey(KeyEvent key) {
                if (key.is(Key.CHAR)) {
                    seen.add(key.ch());
                    return KeyResult.CONSUMED;
                }
                return KeyResult.UNHANDLED;
            }
        };
        FakeTerminal term = new FakeTerminal(10, 3);
        App app = App.builder().root(sink).focusOrder(sink).useTerminal(term).build();

        Thread ui = run(app);
        assertTrue(mounted.await(2, TimeUnit.SECONDS));
        for (char c : "abcdef".toCharArray()) {
            term.send(KeyEvent.character(c));
        }

        CountDownLatch drained = new CountDownLatch(1);
        // a sentinel post after the keys; keys queued before are read first
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (term.pendingKeyCount() > 0 && System.nanoTime() < deadline) {
            Thread.sleep(5);
        }
        app.post(drained::countDown);
        assertTrue(drained.await(2, TimeUnit.SECONDS));

        app.post(app::quit);
        ui.join(2000);
        assertEquals(6, seen.size(), "every key in the burst was delivered");
    }
}
