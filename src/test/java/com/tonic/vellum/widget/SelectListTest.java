package com.tonic.vellum.widget;

import com.tonic.vellum.input.Key;
import com.tonic.vellum.input.KeyEvent;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class SelectListTest {

    @Test
    void rendersWithCustomRendererAndTracksSelectedItem() {
        SelectList<Integer> list = new SelectList<>(Arrays.asList(10, 20, 30))
                .renderer(i -> "#" + i);
        RecordingCanvas c = new RecordingCanvas(6, 3);
        list.render(c);

        assertEquals('#', c.charAt(0, 0));
        assertEquals('1', c.charAt(1, 0));
        assertEquals('0', c.charAt(2, 0));
        assertEquals(Integer.valueOf(10), list.selectedItem());

        list.onKey(KeyEvent.special(Key.DOWN));
        assertEquals(Integer.valueOf(20), list.selectedItem());
    }

    @Test
    void enterFiresIndexAndItemCallbacks() {
        SelectList<String> list = new SelectList<>(Arrays.asList("a", "b"));
        AtomicReference<String> item = new AtomicReference<>();
        AtomicReference<Integer> index = new AtomicReference<>();
        list.onSelectItem(item::set).onSelect(index::set);

        list.onKey(KeyEvent.special(Key.DOWN));
        list.onKey(KeyEvent.special(Key.ENTER));
        assertEquals("b", item.get());
        assertEquals(Integer.valueOf(1), index.get());
    }

    @Test
    void setItemsClampsStaleSelection() {
        SelectList<String> list = new SelectList<>(Arrays.asList("a", "b", "c"));
        list.select(2);
        list.setItems(Collections.singletonList("x"));
        list.render(new RecordingCanvas(4, 2)); // render clamps the selection
        assertEquals("x", list.selectedItem());
    }
}
