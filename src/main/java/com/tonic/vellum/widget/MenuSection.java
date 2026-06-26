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
 * selected row gets the full-row focused or parked highlight. ENTER fires {@code onSelect}.
 */
public final class MenuSection extends AbstractListSection {

    private final List<String> items;

    /**
     * Creates a menu with the given items.
     *
     * @param items the menu item labels in order
     */
    public MenuSection(String... items) {
        this.items = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(items)));
    }

    /**
     * Returns the number of menu items.
     *
     * @return the item count
     */
    @Override
    protected int rowCount() {
        return items.size();
    }

    /**
     * Draws the item label for the given row.
     *
     * @param row the single-row canvas to draw into
     * @param index the item index
     * @param style the style the row has been filled with
     */
    @Override
    protected void renderRow(Canvas row, int index, Style style) {
        row.put(1, 0, items.get(index), style);
    }

    /**
     * Sets the handler called with the item index on ENTER.
     *
     * @param handler the activation handler
     * @return this MenuSection for chaining
     */
    @Override
    public MenuSection onSelect(IntConsumer handler) {
        super.onSelect(handler);
        return this;
    }
}
