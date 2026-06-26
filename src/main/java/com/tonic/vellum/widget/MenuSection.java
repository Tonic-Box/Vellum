package com.tonic.vellum.widget;

import com.tonic.vellum.Canvas;
import com.tonic.vellum.KeyResult;
import com.tonic.vellum.Section;
import com.tonic.vellum.input.KeyEvent;
import com.tonic.vellum.style.Style;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * A vertical, arrow-navigable list with a selection. The selected row is drawn in
 * {@link Style#REVERSE} when focused and {@link Style#DIM_REVERSE} when parked - the cue
 * that makes multi-pane focus legible. {@code ENTER} fires the {@code onSelect} callback.
 */
public final class MenuSection extends Section {

    private final List<String> items;
    private int selected = 0;
    private Consumer<Integer> onSelect = i -> { };

    public MenuSection(String... items) {
        this.items = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(items)));
    }

    public MenuSection onSelect(Consumer<Integer> handler) {
        this.onSelect = handler;
        return this;
    }

    public int selectedIndex() {
        return selected;
    }

    @Override
    protected void render(Canvas canvas) {
        boolean focused = isFocused();
        for (int i = 0; i < items.size() && i < canvas.height(); i++) {
            Style style = Style.NORMAL;
            if (i == selected) {
                style = focused ? Style.REVERSE : Style.DIM_REVERSE;
            }
            canvas.put(1, i, items.get(i), style);
        }
    }

    @Override
    protected KeyResult onKey(KeyEvent key) {
        switch (key.code()) {
            case UP:
                selected = Math.max(0, selected - 1);
                requestRedraw();
                return KeyResult.CONSUMED;
            case DOWN:
                selected = Math.min(items.size() - 1, selected + 1);
                requestRedraw();
                return KeyResult.CONSUMED;
            case ENTER:
                onSelect.accept(selected);
                return KeyResult.CONSUMED;
            default:
                return KeyResult.UNHANDLED;
        }
    }
}
