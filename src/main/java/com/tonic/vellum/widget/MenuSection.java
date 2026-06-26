package com.tonic.vellum.widget;

import com.tonic.vellum.Canvas;
import com.tonic.vellum.style.Style;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * A vertical, arrow-navigable list of strings with a selection. Built on
 * {@link AbstractListSection}, so it scrolls when there are more items than rows; the
 * selected row gets the full-row focused/parked highlight. ENTER fires {@code onSelect}.
 */
public final class MenuSection extends AbstractListSection {

    private final List<String> items;

    public MenuSection(String... items) {
        this.items = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(items)));
    }

    @Override
    protected int rowCount() {
        return items.size();
    }

    @Override
    protected void renderRow(Canvas row, int index, Style style) {
        row.put(1, 0, items.get(index), style);
    }

    @Override
    public MenuSection onSelect(IntConsumer handler) {
        super.onSelect(handler);
        return this;
    }
}
