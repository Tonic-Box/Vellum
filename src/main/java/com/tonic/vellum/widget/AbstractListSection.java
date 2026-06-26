package com.tonic.vellum.widget;

import com.tonic.vellum.Canvas;
import com.tonic.vellum.KeyResult;
import com.tonic.vellum.Maths;
import com.tonic.vellum.Section;
import com.tonic.vellum.geom.Rect;
import com.tonic.vellum.input.KeyEvent;
import com.tonic.vellum.style.Style;

import java.util.function.IntConsumer;

/**
 * Base for vertical, scrollable, single-selection lists. Handles the cursor, scrolling to
 * keep the selection visible, key navigation, an optional fixed header, and the full-row
 * focused or parked highlight. Subclasses supply the row count and how to draw a row.
 *
 * <p>Reused by {@link MenuSection}, {@link SelectList}, {@link RadioGroup}, {@link Table}, and
 * {@link TreeView}.
 */
public abstract class AbstractListSection extends Section {

    private final Viewport viewport = new Viewport();
    private int selected;
    private IntConsumer onSelect = i -> { };
    private IntConsumer onHighlight = i -> { };

    /**
     * Returns the number of selectable rows.
     *
     * @return the row count
     */
    protected abstract int rowCount();

    /**
     * Draws one row. The canvas is a single row already filled with {@code style} (the
     * selection highlight for the selected row, otherwise the normal style).
     *
     * @param row the single-row canvas to draw into
     * @param index the index of the row being drawn
     * @param style the style the row has been filled with
     */
    protected abstract void renderRow(Canvas row, int index, Style style);

    /**
     * Returns the number of rows reserved at the top for a fixed header (default none).
     *
     * @return the header row count
     */
    protected int headerRows() {
        return 0;
    }

    /**
     * Draws the header into its reserved area (default no-op).
     *
     * @param header the canvas covering the reserved header area
     */
    protected void renderHeader(Canvas header) {
    }

    /**
     * Invoked on ENTER. The default fires the {@code onSelect} callback; override for custom
     * activation.
     *
     * @param index the index of the activated row
     */
    protected void onActivate(int index) {
        onSelect.accept(index);
    }

    /**
     * Returns the index of the currently selected row.
     *
     * @return the selected row index
     */
    public int selectedIndex() {
        return selected;
    }

    /**
     * Moves the selection to the given index (clamped to the valid range); fires
     * {@code onHighlight} when it changes.
     *
     * @param index the requested selection index
     */
    public void select(int index) {
        int clamped = Maths.clamp(index, 0, Math.max(0, rowCount() - 1));
        if (clamped != selected) {
            selected = clamped;
            onHighlight.accept(selected);
            requestRedraw();
        }
    }

    /**
     * Sets the handler called with the row index on ENTER.
     *
     * @param handler the activation handler
     * @return this AbstractListSection for chaining
     */
    public AbstractListSection onSelect(IntConsumer handler) {
        this.onSelect = handler;
        return this;
    }

    /**
     * Sets the handler called with the row index whenever the selection moves.
     *
     * @param handler the highlight handler
     * @return this AbstractListSection for chaining
     */
    public AbstractListSection onHighlight(IntConsumer handler) {
        this.onHighlight = handler;
        return this;
    }

    @Override
    protected void render(Canvas canvas) {
        int w = canvas.width();
        int h = canvas.height();
        if (w <= 0 || h <= 0) {
            return;
        }
        if (selected >= rowCount()) {
            selected = Math.max(0, rowCount() - 1); // data may have shrunk
        }
        int header = headerHeight(h);
        if (header > 0) {
            renderHeader(canvas.clip(new Rect(0, 0, w, header)));
        }
        int body = bodyHeight(h);
        if (body <= 0) {
            return;
        }
        int count = rowCount();
        viewport.ensureVisible(selected, count, body);
        int top = viewport.top();
        boolean focused = isFocused();
        for (int i = 0; i < body && top + i < count; i++) {
            int index = top + i;
            Style style = styleFor(index, focused);
            Canvas row = canvas.clip(new Rect(0, header + i, w, 1));
            row.fill(row.bounds(), ' ', style);
            renderRow(row, index, style);
        }
    }

    @Override
    protected KeyResult onKey(KeyEvent key) {
        if (Keys.isActivation(key)) {
            if (rowCount() > 0) {
                onActivate(selected);
            }
            return KeyResult.CONSUMED;
        }
        switch (key.code()) {
            case UP:        select(selected - 1); return KeyResult.CONSUMED;
            case DOWN:      select(selected + 1); return KeyResult.CONSUMED;
            case PAGE_UP:   select(selected - pageStep()); return KeyResult.CONSUMED;
            case PAGE_DOWN: select(selected + pageStep()); return KeyResult.CONSUMED;
            case HOME:      select(0); return KeyResult.CONSUMED;
            case END:       select(rowCount() - 1); return KeyResult.CONSUMED;
            default:        return KeyResult.UNHANDLED;
        }
    }

    private Style styleFor(int index, boolean focused) {
        if (index != selected) {
            return Style.NORMAL;
        }
        return focused ? Style.REVERSE : Style.DIM_REVERSE;
    }

    /** Rows occupied by the fixed header within {@code totalHeight} rows. */
    private int headerHeight(int totalHeight) {
        return Math.min(headerRows(), Math.max(0, totalHeight));
    }

    /** Rows available for the scrollable body below the header within {@code totalHeight} rows. */
    private int bodyHeight(int totalHeight) {
        return Math.max(0, totalHeight - headerHeight(totalHeight));
    }

    private int pageStep() {
        return Math.max(1, bodyHeight(bounds().height()));
    }
}
