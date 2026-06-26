package com.tonic.vellum.widget;

import com.tonic.vellum.KeyResult;
import com.tonic.vellum.input.Key;
import com.tonic.vellum.input.KeyEvent;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class FormControlsTest {

    @Test
    void buttonActivatesOnEnterAndSpace() {
        AtomicInteger clicks = new AtomicInteger();
        Button button = new Button("OK").onActivate(clicks::incrementAndGet);

        assertEquals(KeyResult.CONSUMED, button.onKey(KeyEvent.special(Key.ENTER)));
        button.onKey(KeyEvent.character(' '));
        assertEquals(2, clicks.get());
        assertEquals(KeyResult.UNHANDLED, button.onKey(KeyEvent.special(Key.LEFT)));
    }

    @Test
    void checkboxToggles() {
        AtomicReference<Boolean> last = new AtomicReference<>();
        Checkbox box = new Checkbox("Enable").onChange(last::set);
        assertFalse(box.isChecked());

        box.onKey(KeyEvent.character(' '));
        assertTrue(box.isChecked());
        assertTrue(last.get());

        box.onKey(KeyEvent.special(Key.ENTER));
        assertFalse(box.isChecked());
        assertFalse(last.get());
    }

    @Test
    void radioGroupChoosesUnderCursor() {
        AtomicInteger chosen = new AtomicInteger(-1);
        RadioGroup radio = new RadioGroup("a", "b", "c").onChange(chosen::set);
        assertEquals(0, radio.chosenIndex());

        radio.onKey(KeyEvent.special(Key.DOWN));
        radio.onKey(KeyEvent.special(Key.DOWN));
        radio.onKey(KeyEvent.special(Key.ENTER));
        assertEquals(2, radio.chosenIndex());
        assertEquals(2, chosen.get());

        radio.onKey(KeyEvent.special(Key.UP));
        radio.onKey(KeyEvent.character(' ')); // SPACE chooses too
        assertEquals(1, radio.chosenIndex());
    }

    @Test
    void radioGroupMarksTheChosenOption() {
        RadioGroup radio = new RadioGroup("a", "b");
        radio.choose(1);
        RecordingCanvas c = new RecordingCanvas(8, 2);
        radio.render(c);
        assertEquals('(', c.charAt(0, 1));
        assertEquals('o', c.charAt(1, 1)); // chosen marker on row 1
        assertEquals(' ', c.charAt(1, 0)); // row 0 not chosen
    }
}
