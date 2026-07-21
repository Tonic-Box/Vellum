package com.tonic.vellum.widget;

import com.tonic.vellum.Canvas;
import com.tonic.vellum.style.Style;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;

/**
 * A scrollable, selectable list of typed items, each rendered to a display string; ENTER fires the selection handlers.
 */
public final class SelectList<T> extends AbstractListSection
{
    private final List<T> items = new ArrayList<>();
    private Function<T, String> renderer = String::valueOf;
    private Consumer<T> onSelectItem = item -> { };

    /**
     * Creates an empty list.
     */
    public SelectList()
    {
    }

    /**
     * Creates a list populated with the given items.
     *
     * @param items the initial items
     */
    public SelectList(List<T> items)
    {
        this.items.addAll(items);
    }

    /**
     * Replaces the items with the given list.
     *
     * @param newItems the new items
     * @return this SelectList for chaining
     */
    public SelectList<T> setItems(List<T> newItems)
    {
        items.clear();
        items.addAll(newItems);
        requestRedraw();
        return this;
    }

    /**
     * Sets the function that renders each item to a display string.
     *
     * @param renderer the item-to-string function
     * @return this SelectList for chaining
     */
    public SelectList<T> renderer(Function<T, String> renderer)
    {
        this.renderer = renderer;
        requestRedraw();
        return this;
    }

    /**
     * @return the selected item, or null when the list is empty
     */
    public T selectedItem()
    {
        int i = selectedIndex();
        return i >= 0 && i < items.size() ? items.get(i) : null;
    }

    /**
     * Sets the handler called with the item on ENTER.
     *
     * @param handler the item activation handler
     * @return this SelectList for chaining
     */
    public SelectList<T> onSelectItem(Consumer<T> handler)
    {
        this.onSelectItem = handler;
        return this;
    }

    @Override
    public SelectList<T> onSelect(IntConsumer handler)
    {
        super.onSelect(handler);
        return this;
    }

    @Override
    protected int rowCount()
    {
        return items.size();
    }

    @Override
    protected void renderRow(Canvas row, int index, Style style)
    {
        row.put(0, 0, renderer.apply(items.get(index)), style);
    }

    @Override
    protected void onActivate(int index)
    {
        super.onActivate(index);
        onSelectItem.accept(items.get(index));
    }
}
