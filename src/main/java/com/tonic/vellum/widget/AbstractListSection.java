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
 * focused/parked highlight. Subclasses supply the row count and how to draw a row.
 *
 * <p>Reused by {@link MenuSection}, {@link SelectList}, {@link RadioGroup}, {@link Table}
 * (body), and {@link TreeView}.
 */
public abstract class AbstractListSection extends Section {

    private final Viewport viewport = new Viewport();
    private int selected;
    private IntConsumer onSelect = i -> { };
    private IntConsumer onHighlight = i -> { };

    /** Number of selectable rows. */
    protected abstract int rowCount();

    /**
     * Draw one row. The canvas is a single row already filled with {@code style} (the
     * selection highlight for the selected row, otherwise the normal style).
     */
    protected abstract void renderRow(Canvas row, int index, Style style);

    /** Rows reserved at the top for a fixed header (default none). */
    protected int headerRows() {
        return 0;
    }

    /** Draw the header into its reserved area (default no-op). */
    protected void renderHeader(Canvas header) {
    }

    /** Invoked on ENTER. Default fires the {@code onSelect} callback; override for custom activation. */
    protected void onActivate(int index) {
        onSelect.accept(index);
    }

    public int selectedIndex() {
        return selected;
    }

    /** Move the selection (clamped); fires {@code onHighlight} when it changes. */
    public void select(int index) {
        int clamped = Maths.clamp(index, 0, Math.max(0, rowCount() - 1));
        if (clamped != selected) {
            selected = clamped;
            onHighlight.accept(selected);
            requestRedraw();
        }
    }

    /** Called with the row index on ENTER. */
    public AbstractListSection onSelect(IntConsumer handler) {
        this.onSelect = handler;
        return this;
    }

    /** Called with the row index whenever the selection moves. */
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
        int header = Math.min(headerRows(), h);
        if (header > 0) {
            renderHeader(canvas.clip(new Rect(0, 0, w, header)));
        }
        int bodyHeight = h - header;
        if (bodyHeight <= 0) {
            return;
        }
        int count = rowCount();
        viewport.ensureVisible(selected, count, bodyHeight);
        int top = viewport.top();
        boolean focused = isFocused();
        for (int i = 0; i < bodyHeight && top + i < count; i++) {
            int index = top + i;
            Style style = styleFor(index, focused);
            Canvas row = canvas.clip(new Rect(0, header + i, w, 1));
            row.fill(row.bounds(), ' ', style);
            renderRow(row, index, style);
        }
    }

    @Override
    protected KeyResult onKey(KeyEvent key) {
        switch (key.code()) {
            case UP:        select(selected - 1); return KeyResult.CONSUMED;
            case DOWN:      select(selected + 1); return KeyResult.CONSUMED;
            case PAGE_UP:   select(selected - pageStep()); return KeyResult.CONSUMED;
            case PAGE_DOWN: select(selected + pageStep()); return KeyResult.CONSUMED;
            case HOME:      select(0); return KeyResult.CONSUMED;
            case END:       select(rowCount() - 1); return KeyResult.CONSUMED;
            case ENTER:
                if (rowCount() > 0) {
                    onActivate(selected);
                }
                return KeyResult.CONSUMED;
            default:
                return KeyResult.UNHANDLED;
        }
    }

    private Style styleFor(int index, boolean focused) {
        if (index != selected) {
            return Style.NORMAL;
        }
        return focused ? Style.REVERSE : Style.DIM_REVERSE;
    }

    private int pageStep() {
        return Math.max(1, bounds().height() - headerRows());
    }
}
