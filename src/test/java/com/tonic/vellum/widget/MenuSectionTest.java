package com.tonic.vellum.widget;

import com.tonic.vellum.KeyResult;
import com.tonic.vellum.input.Key;
import com.tonic.vellum.input.KeyEvent;
import com.tonic.vellum.style.Style;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class MenuSectionTest {

    @Test
    void selectedRowUsesParkedHighlightWhenUnfocused() {
        MenuSection menu = new MenuSection("Logs", "Metrics", "Config");
        RecordingCanvas c = new RecordingCanvas(20, 5);
        menu.render(c);

        // detached menu is not focused -> parked highlight on the selected (first) row
        assertEquals(Style.DIM_REVERSE, c.styleAt(1, 0));
        assertEquals(Style.NORMAL, c.styleAt(1, 1));
        assertEquals('L', c.charAt(1, 0));
    }

    @Test
    void arrowKeysMoveSelection() {
        MenuSection menu = new MenuSection("Logs", "Metrics", "Config");
        assertEquals(KeyResult.CONSUMED, menu.onKey(KeyEvent.special(Key.DOWN)));
        assertEquals(1, menu.selectedIndex());

        menu.onKey(KeyEvent.special(Key.DOWN));
        menu.onKey(KeyEvent.special(Key.DOWN)); // clamps at last index
        assertEquals(2, menu.selectedIndex());

        menu.onKey(KeyEvent.special(Key.UP));
        assertEquals(1, menu.selectedIndex());

        RecordingCanvas c = new RecordingCanvas(20, 5);
        menu.render(c);
        assertEquals(Style.DIM_REVERSE, c.styleAt(1, 1));
        assertEquals(Style.NORMAL, c.styleAt(1, 0));
    }

    @Test
    void enterFiresOnSelectWithCurrentIndex() {
        AtomicInteger chosen = new AtomicInteger(-1);
        MenuSection menu = new MenuSection("Logs", "Metrics").onSelect(chosen::set);
        menu.onKey(KeyEvent.special(Key.DOWN));
        assertEquals(KeyResult.CONSUMED, menu.onKey(KeyEvent.special(Key.ENTER)));
        assertEquals(1, chosen.get());
    }

    @Test
    void unrelatedKeysBubble() {
        MenuSection menu = new MenuSection("A", "B");
        assertEquals(KeyResult.UNHANDLED, menu.onKey(KeyEvent.special(Key.TAB)));
        assertEquals(KeyResult.UNHANDLED, menu.onKey(KeyEvent.special(Key.LEFT)));
    }
}
