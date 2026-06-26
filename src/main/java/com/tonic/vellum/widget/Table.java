package com.tonic.vellum.widget;

import com.tonic.vellum.Canvas;
import com.tonic.vellum.geom.Rect;
import com.tonic.vellum.layout.Axis;
import com.tonic.vellum.layout.Constraint;
import com.tonic.vellum.layout.LayoutSolver;
import com.tonic.vellum.style.Style;

import java.util.ArrayList;
import java.util.List;

/**
 * A scrollable table with a fixed header and selectable rows. Column widths are computed
 * with the layout {@link LayoutSolver} from each column's {@link Constraint}, so the header
 * and rows always align and tile the width. Built on {@link AbstractListSection}.
 */
public final class Table extends AbstractListSection {

    private static final class Column {
        final String title;
        final Constraint width;
        final Alignment align;

        Column(String title, Constraint width, Alignment align) {
            this.title = title;
            this.width = width;
            this.align = align;
        }
    }

    private final List<Column> columns = new ArrayList<>();
    private final List<String[]> rows = new ArrayList<>();
    private boolean showHeader = true;
    private Style headerStyle = Style.BOLD;

    private Rect[] cachedRects;
    private int cachedWidth = -1;
    private boolean columnsDirty = true;

    /**
     * Adds a left-aligned column.
     *
     * @param title the column header title
     * @param width the column width constraint
     * @return this Table for chaining
     */
    public Table column(String title, Constraint width) {
        return column(title, width, Alignment.LEFT);
    }

    /**
     * Adds a column with the given alignment.
     *
     * @param title the column header title
     * @param width the column width constraint
     * @param align the cell and header alignment
     * @return this Table for chaining
     */
    public Table column(String title, Constraint width, Alignment align) {
        columns.add(new Column(title, width, align));
        columnsDirty = true;
        return this;
    }

    /**
     * Appends a row of cells.
     *
     * @param cells the cell values in column order
     * @return this Table for chaining
     */
    public Table addRow(String... cells) {
        rows.add(cells);
        requestRedraw();
        return this;
    }

    /**
     * Replaces all rows.
     *
     * @param newRows the new rows, each an array of cell values
     * @return this Table for chaining
     */
    public Table setRows(List<String[]> newRows) {
        rows.clear();
        rows.addAll(newRows);
        requestRedraw();
        return this;
    }

    /**
     * Sets whether the header row is shown.
     *
     * @param show {@code true} to show the header
     * @return this Table for chaining
     */
    public Table showHeader(boolean show) {
        this.showHeader = show;
        requestRedraw();
        return this;
    }

    /**
     * Sets the style used to draw the header row.
     *
     * @param style the header style
     * @return this Table for chaining
     */
    public Table headerStyle(Style style) {
        this.headerStyle = style;
        requestRedraw();
        return this;
    }

    /**
     * Returns the selected row's cells, or {@code null} when empty.
     *
     * @return the selected row's cell values, or {@code null}
     */
    public String[] selectedRow() {
        int i = selectedIndex();
        return i >= 0 && i < rows.size() ? rows.get(i) : null;
    }

    /**
     * Returns the number of rows.
     *
     * @return the row count
     */
    @Override
    protected int rowCount() {
        return rows.size();
    }

    /**
     * Returns the number of header rows (one when the header is shown and columns exist,
     * otherwise zero).
     *
     * @return the header row count
     */
    @Override
    protected int headerRows() {
        return showHeader && !columns.isEmpty() ? 1 : 0;
    }

    /**
     * Draws the column titles into the header area.
     *
     * @param header the canvas covering the reserved header area
     */
    @Override
    protected void renderHeader(Canvas header) {
        Rect[] cells = columnRects(header.width());
        for (int i = 0; i < columns.size(); i++) {
            Text.putAligned(header.clip(cells[i]), 0, columns.get(i).title, columns.get(i).align, headerStyle);
        }
    }

    /**
     * Draws the cells of the given row across the columns.
     *
     * @param row the single-row canvas to draw into
     * @param index the row index
     * @param style the style the row has been filled with
     */
    @Override
    protected void renderRow(Canvas row, int index, Style style) {
        String[] cells = rows.get(index);
        Rect[] rects = columnRects(row.width());
        for (int i = 0; i < columns.size(); i++) {
            String text = i < cells.length && cells[i] != null ? cells[i] : "";
            Text.putAligned(row.clip(rects[i]), 0, text, columns.get(i).align, style);
        }
    }

    /** Column rectangles for the given width, solved once per width (cached across rows). */
    private Rect[] columnRects(int width) {
        if (columnsDirty || width != cachedWidth) {
            List<Constraint> widths = new ArrayList<>(columns.size());
            for (Column column : columns) {
                widths.add(column.width);
            }
            cachedRects = LayoutSolver.solve(new Rect(0, 0, width, 1), widths, Axis.HORIZONTAL);
            cachedWidth = width;
            columnsDirty = false;
        }
        return cachedRects;
    }
}
