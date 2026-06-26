package com.tonic.vellum;

import com.tonic.vellum.geom.Rect;
import com.tonic.vellum.input.KeyEvent;
import com.tonic.vellum.layout.Constraint;
import com.tonic.vellum.layout.Slot;
import com.tonic.vellum.layout.Split;
import com.tonic.vellum.widget.LogSection;
import com.tonic.vellum.widget.MenuSection;
import com.tonic.vellum.widget.TabHost;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/** End-to-end checks of the spec's acceptance scenarios, driven through a fake terminal. */
class AcceptanceTest {

    private static Thread run(App app) {
        Thread t = new Thread(app::run, "vellum-ui-test");
        t.setDaemon(true);
        t.start();
        return t;
    }

    private static boolean awaitOutputContains(FakeTerminal term, String text, long timeoutMillis)
            throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
        while (System.nanoTime() < deadline) {
            synchronized (term.output) {
                if (term.output.toString().contains(text)) {
                    return true;
                }
            }
            Thread.sleep(10);
        }
        return false;
    }

    @Test
    void backgroundThreadFeedsLogViaPost() throws Exception {
        LogSection logs = new LogSection();
        MenuSection menu = new MenuSection("Logs");
        TabHost detail = new TabHost().add("Logs", logs);
        Section root = Split.horizontal(
                Slot.of(Constraint.fixed(10), menu),
                Slot.of(Constraint.fill(), detail));
        FakeTerminal term = new FakeTerminal(40, 12);
        App app = App.builder().root(root).focusOrder(menu, detail).onQuitKey('q').useTerminal(term).build();

        Thread ui = run(app);

        // feed 5 lines from a background thread, exactly as a real log stream would
        Thread feeder = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                final int id = i;
                app.post(() -> logs.append("line-" + id));
            }
        });
        feeder.start();
        feeder.join(2000);

        // wait until the UI thread has applied the appends
        CountDownLatch settled = new CountDownLatch(1);
        AtomicInteger count = new AtomicInteger();
        app.post(() -> {
            count.set(logs.lineCount());
            settled.countDown();
        });
        assertTrue(settled.await(2, TimeUnit.SECONDS));
        assertEquals(5, count.get());
        assertTrue(awaitOutputContains(term, "line-4", 2000),
                "latest log line should reach the screen");

        app.post(app::quit);
        ui.join(2000);
        assertFalse(ui.isAlive());
    }

    @Test
    void terminalResizeReflowsPanes() throws Exception {
        MenuSection menu = new MenuSection("A");
        LogSection body = new LogSection();
        Section root = Split.horizontal(
                Slot.of(Constraint.fixed(10), menu),
                Slot.of(Constraint.fill(), body));
        FakeTerminal term = new FakeTerminal(40, 12);
        App app = App.builder().root(root).focusOrder(menu, body).onQuitKey('q').useTerminal(term).build();

        Thread ui = run(app);

        AtomicReference<Rect> before = new AtomicReference<>();
        CountDownLatch first = new CountDownLatch(1);
        app.post(() -> {
            before.set(body.bounds());
            first.countDown();
        });
        assertTrue(first.await(2, TimeUnit.SECONDS));
        assertEquals(new Rect(10, 0, 30, 12), before.get());

        term.resizeTo(60, 20);

        AtomicReference<Rect> after = new AtomicReference<>();
        CountDownLatch second = new CountDownLatch(1);
        // poll on the UI thread until the resize has been applied
        Runnable[] check = new Runnable[1];
        check[0] = () -> {
            Rect b = body.bounds();
            if (b.width() == 50 && b.height() == 20) {
                after.set(b);
                second.countDown();
            } else {
                app.post(check[0]);
            }
        };
        app.post(check[0]);
        assertTrue(second.await(2, TimeUnit.SECONDS), "resize should reflow the body pane");
        assertEquals(new Rect(10, 0, 50, 20), after.get());

        app.post(app::quit);
        ui.join(2000);
        assertFalse(ui.isAlive());
    }
}
