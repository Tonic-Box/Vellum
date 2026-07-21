package com.tonic.vellum.widget;

import com.tonic.vellum.Canvas;
import com.tonic.vellum.style.Style;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * A vertical, arrow-navigable, scrollable list of strings with a selection; ENTER fires {@code onSelect}.
 */
public final class MenuSection extends AbstractListSection
{
    private final List<String> items;

    /**
     * Creates a menu with the given items.
     *
     * @param items the menu item labels in order
     */
    public MenuSection(String... items)
    {
        this.items = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(items)));
    }

    @Override
    protected int rowCount()
    {
        return items.size();
    }

    @Override
    protected void renderRow(Canvas row, int index, Style style)
    {
        row.put(1, 0, items.get(index), style);
    }

    @Override
    public MenuSection onSelect(IntConsumer handler)
    {
        super.onSelect(handler);
        return this;
    }
}
