package com.tonic.vellum.widget;

import com.tonic.vellum.Canvas;
import com.tonic.vellum.style.Style;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;

/**
 * A scrollable, selectable list of typed items. Each item is rendered to a string by the
 * {@code renderer} (default {@link String#valueOf}). ENTER fires {@code onSelect} (index)
 * and {@code onSelectItem} (the item).
 */
public final class SelectList<T> extends AbstractListSection {

    private final List<T> items = new ArrayList<>();
    private Function<T, String> renderer = String::valueOf;
    private Consumer<T> onSelectItem = item -> { };

    /**
     * Creates an empty list.
     */
    public SelectList() {
    }

    /**
     * Creates a list populated with the given items.
     *
     * @param items the initial items
     */
    public SelectList(List<T> items) {
        this.items.addAll(items);
    }

    /**
     * Replaces the items with the given list.
     *
     * @param newItems the new items
     * @return this SelectList for chaining
     */
    public SelectList<T> setItems(List<T> newItems) {
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
    public SelectList<T> renderer(Function<T, String> renderer) {
        this.renderer = renderer;
        requestRedraw();
        return this;
    }

    /**
     * Returns the selected item, or {@code null} when the list is empty.
     *
     * @return the selected item, or {@code null}
     */
    public T selectedItem() {
        int i = selectedIndex();
        return i >= 0 && i < items.size() ? items.get(i) : null;
    }

    /**
     * Sets the handler called with the item on ENTER.
     *
     * @param handler the item activation handler
     * @return this SelectList for chaining
     */
    public SelectList<T> onSelectItem(Consumer<T> handler) {
        this.onSelectItem = handler;
        return this;
    }

    /**
     * Sets the handler called with the item index on ENTER.
     *
     * @param handler the activation handler
     * @return this SelectList for chaining
     */
    @Override
    public SelectList<T> onSelect(IntConsumer handler) {
        super.onSelect(handler);
        return this;
    }

    /**
     * Returns the number of items.
     *
     * @return the item count
     */
    @Override
    protected int rowCount() {
        return items.size();
    }

    /**
     * Draws the rendered item for the given row.
     *
     * @param row the single-row canvas to draw into
     * @param index the item index
     * @param style the style the row has been filled with
     */
    @Override
    protected void renderRow(Canvas row, int index, Style style) {
        row.put(0, 0, renderer.apply(items.get(index)), style);
    }

    /**
     * Fires the index and item handlers for the activated row.
     *
     * @param index the index of the activated row
     */
    @Override
    protected void onActivate(int index) {
        super.onActivate(index);
        onSelectItem.accept(items.get(index));
    }
}
