package com.tonic.vellum;

import com.tonic.vellum.input.Key;
import com.tonic.vellum.input.KeyEvent;
import com.tonic.vellum.widget.MenuSection;
import com.tonic.vellum.widget.TabHost;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class TabHostIntegrationTest {

    /** A content section that records visibility lifecycle and whether it is focused. */
    static class Tab extends Section {
        final AtomicInteger mounts = new AtomicInteger();
        final AtomicInteger unmounts = new AtomicInteger();

        @Override
        protected void render(Canvas canvas) {
            canvas.put(0, 0, 'X');
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

    private static void awaitDrained(FakeTerminal term) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (term.pendingKeyCount() > 0 && System.nanoTime() < deadline) {
            Thread.sleep(5);
        }
        Thread.sleep(30); // let the loop process the last consumed key
    }

    @Test
    void onlyActiveTabMountsAtStartup() throws Exception {
        Tab a = new Tab();
        Tab b = new Tab();
        Tab c = new Tab();
        TabHost tabs = new TabHost().add("A", a).add("B", b).add("C", c);
        FakeTerminal term = new FakeTerminal(30, 10);
        App app = App.builder().root(tabs).focusOrder(tabs).onQuitKey('q').useTerminal(term).build();

        Thread ui = run(app);
        term.send(KeyEvent.character('q'));
        ui.join(2000);

        assertEquals(1, a.mounts.get());
        assertEquals(0, b.mounts.get());
        assertEquals(0, c.mounts.get());
    }

    @Test
    void switchingTabsFiresExactlyOneUnmountAndOneMount() throws Exception {
        Tab a = new Tab();
        Tab b = new Tab();
        TabHost tabs = new TabHost().add("A", a).add("B", b);
        FakeTerminal term = new FakeTerminal(30, 10);
        App app = App.builder().root(tabs).focusOrder(tabs).onQuitKey('q').useTerminal(term).build();

        Thread ui = run(app);
        term.send(KeyEvent.special(Key.RIGHT)); // A -> B
        term.send(KeyEvent.character('q'));
        ui.join(2000);

        assertEquals(1, a.mounts.get());
        assertEquals(1, a.unmounts.get());
        assertEquals(1, b.mounts.get());
        assertEquals(0, b.unmounts.get());
        assertEquals(1, tabs.active());
    }

    @Test
    void activeContentIsOnFocusPathWhenTabHostFocused() throws Exception {
        Tab a = new Tab();
        TabHost tabs = new TabHost().add("A", a);
        FakeTerminal term = new FakeTerminal(30, 10);
        App app = App.builder().root(tabs).focusOrder(tabs).onQuitKey('q').useTerminal(term).build();

        AtomicBoolean contentFocused = new AtomicBoolean();
        AtomicBoolean hostFocused = new AtomicBoolean();
        CountDownLatch captured = new CountDownLatch(1);

        Thread ui = run(app);
        awaitDrained(term);
        app.post(() -> {
            contentFocused.set(a.isFocused());
            hostFocused.set(tabs.isFocused());
            captured.countDown();
        });
        assertTrue(captured.await(2, TimeUnit.SECONDS));
        app.post(app::quit);
        ui.join(2000);

        assertTrue(hostFocused.get(), "tab host should be focused");
        assertTrue(contentFocused.get(), "active content should be on the focus path");
    }

    @Test
    void menuSelectionDrivesTabSwitch() throws Exception {
        Tab logs = new Tab();
        Tab metrics = new Tab();
        TabHost tabs = new TabHost().add("Logs", logs).add("Metrics", metrics);
        MenuSection menu = new MenuSection("Logs", "Metrics").onSelect(tabs::select);
        com.tonic.vellum.layout.Split root = com.tonic.vellum.layout.Split.horizontal(
                com.tonic.vellum.layout.Slot.of(com.tonic.vellum.layout.Constraint.fixed(12), menu),
                com.tonic.vellum.layout.Slot.of(com.tonic.vellum.layout.Constraint.fill(), tabs));
        FakeTerminal term = new FakeTerminal(40, 10);
        App app = App.builder().root(root).focusOrder(menu, tabs).onQuitKey('q').useTerminal(term).build();

        Thread ui = run(app);
        term.send(KeyEvent.special(Key.DOWN));  // menu: Logs -> Metrics
        term.send(KeyEvent.special(Key.ENTER)); // fire onSelect -> tabs.select(1)
        term.send(KeyEvent.character('q'));
        ui.join(2000);

        assertEquals(1, tabs.active());
        assertEquals(1, metrics.mounts.get());
        assertEquals(1, logs.unmounts.get());
    }
}
