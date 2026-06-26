package com.tonic.vellum.widget;

import com.tonic.vellum.Canvas;
import com.tonic.vellum.input.Key;
import com.tonic.vellum.input.KeyEvent;
import com.tonic.vellum.style.Style;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class AbstractListSectionTest {

    /** A concrete list rendering each row's single-character label. */
    static final class CharList extends AbstractListSection {
        private final int count;

        CharList(int count) {
            this.count = count;
        }

        @Override
        protected int rowCount() {
            return count;
        }

        @Override
        protected void renderRow(Canvas row, int index, Style style) {
            row.put(0, 0, Character.forDigit(index, 10), style);
        }
    }

    @Test
    void scrollsToKeepSelectionVisible() {
        CharList list = new CharList(10);
        list.select(5);

        RecordingCanvas c = new RecordingCanvas(4, 3); // viewport of 3 rows
        list.render(c);

        // selection at index 5 with a 3-row viewport scrolls so rows 3,4,5 are visible
        assertEquals('3', c.charAt(0, 0));
        assertEquals('4', c.charAt(0, 1));
        assertEquals('5', c.charAt(0, 2));
    }

    @Test
    void selectedRowGetsFullRowHighlight() {
        CharList list = new CharList(3);
        list.select(1);
        RecordingCanvas c = new RecordingCanvas(4, 3);
        list.render(c);

        // unfocused -> parked highlight across the whole row
        assertEquals(Style.DIM_REVERSE, c.styleAt(0, 1));
        assertEquals(Style.DIM_REVERSE, c.styleAt(3, 1));
        assertEquals(Style.NORMAL, c.styleAt(0, 0));
    }

    @Test
    void keysMoveSelectionAndClamp() {
        CharList list = new CharList(5);
        list.onKey(KeyEvent.special(Key.DOWN));
        list.onKey(KeyEvent.special(Key.DOWN));
        assertEquals(2, list.selectedIndex());

        list.onKey(KeyEvent.special(Key.END));
        assertEquals(4, list.selectedIndex());
        list.onKey(KeyEvent.special(Key.DOWN)); // clamps at the last row
        assertEquals(4, list.selectedIndex());

        list.onKey(KeyEvent.special(Key.HOME));
        assertEquals(0, list.selectedIndex());
    }

    @Test
    void enterActivatesAndMovesFireHighlight() {
        CharList list = new CharList(3);
        AtomicInteger activated = new AtomicInteger(-1);
        AtomicInteger highlighted = new AtomicInteger(-1);
        list.onSelect(activated::set).onHighlight(highlighted::set);

        list.onKey(KeyEvent.special(Key.DOWN));
        assertEquals(1, highlighted.get());

        list.onKey(KeyEvent.special(Key.ENTER));
        assertEquals(1, activated.get());
    }

    @Test
    void emptyListIsSafe() {
        CharList list = new CharList(0);
        list.onKey(KeyEvent.special(Key.DOWN));
        list.onKey(KeyEvent.special(Key.ENTER));
        list.render(new RecordingCanvas(4, 3));
        assertEquals(0, list.selectedIndex());
    }
}
