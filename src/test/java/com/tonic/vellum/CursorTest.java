package com.tonic.vellum;

import com.tonic.vellum.input.Key;
import com.tonic.vellum.input.KeyEvent;
import com.tonic.vellum.layout.Constraint;
import com.tonic.vellum.layout.Slot;
import com.tonic.vellum.layout.Split;
import com.tonic.vellum.widget.TextInput;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class CursorTest {

    static final class Inert extends Section {
        @Override
        protected void render(Canvas canvas) { }
    }

    private static Thread run(App app) {
        Thread t = new Thread(app::run, "vellum-ui-test");
        t.setDaemon(true);
        t.start();
        return t;
    }

    private static boolean awaitCursor(FakeTerminal term, int x, int y, long timeoutMillis)
            throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
        while (System.nanoTime() < deadline) {
            if (!term.cursorHidden && term.cursorX == x && term.cursorY == y) {
                return true;
            }
            Thread.sleep(5);
        }
        return false;
    }

    private static boolean awaitCursorHidden(FakeTerminal term, long timeoutMillis)
            throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
        while (System.nanoTime() < deadline) {
            if (term.cursorHidden) {
                return true;
            }
            Thread.sleep(5);
        }
        return false;
    }

    @Test
    void cursorTracksFocusedTextInputAndHidesWhenFocusLeaves() throws Exception {
        TextInput input = new TextInput();
        Inert other = new Inert();
        Section root = Split.horizontal(
                Slot.of(Constraint.fixed(10), input),
                Slot.of(Constraint.fill(), other));
        FakeTerminal term = new FakeTerminal(30, 5);
        App app = App.builder()
                .root(root)
                .focusOrder(input, other)
                .onQuit(e -> e.is(Key.ESCAPE))
                .useTerminal(term)
                .build();

        Thread ui = run(app);
        term.send(KeyEvent.character('a'));
        term.send(KeyEvent.character('b'));
        assertTrue(awaitCursor(term, 2, 0, 2000), "cursor follows the caret of the focused field");

        term.send(KeyEvent.special(Key.TAB)); // focus the inert pane
        assertTrue(awaitCursorHidden(term, 2000), "cursor hidden when a non-editable section is focused");

        term.send(KeyEvent.special(Key.ESCAPE));
        ui.join(2000);
        assertFalse(ui.isAlive());
    }
}
