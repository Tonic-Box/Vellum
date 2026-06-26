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

    public SelectList() {
    }

    public SelectList(List<T> items) {
        this.items.addAll(items);
    }

    public SelectList<T> setItems(List<T> newItems) {
        items.clear();
        items.addAll(newItems);
        requestRedraw();
        return this;
    }

    public SelectList<T> renderer(Function<T, String> renderer) {
        this.renderer = renderer;
        requestRedraw();
        return this;
    }

    /** The selected item, or {@code null} when the list is empty. */
    public T selectedItem() {
        int i = selectedIndex();
        return i >= 0 && i < items.size() ? items.get(i) : null;
    }

    /** Called with the item on ENTER. */
    public SelectList<T> onSelectItem(Consumer<T> handler) {
        this.onSelectItem = handler;
        return this;
    }

    @Override
    public SelectList<T> onSelect(IntConsumer handler) {
        super.onSelect(handler);
        return this;
    }

    @Override
    protected int rowCount() {
        return items.size();
    }

    @Override
    protected void renderRow(Canvas row, int index, Style style) {
        row.put(0, 0, renderer.apply(items.get(index)), style);
    }

    @Override
    protected void onActivate(int index) {
        super.onActivate(index);
        onSelectItem.accept(items.get(index));
    }
}
