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
 * A scrollable table with a fixed header and selectable rows; column widths are solved from
 * each column's Constraint, so the header and rows always align and tile the width.
 */
public final class Table extends AbstractListSection
{
    private static final class Column
    {
        final String title;
        final Constraint width;
        final Alignment align;

        Column(String title, Constraint width, Alignment align)
        {
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
    public Table column(String title, Constraint width)
    {
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
    public Table column(String title, Constraint width, Alignment align)
    {
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
    public Table addRow(String... cells)
    {
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
    public Table setRows(List<String[]> newRows)
    {
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
    public Table showHeader(boolean show)
    {
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
    public Table headerStyle(Style style)
    {
        this.headerStyle = style;
        requestRedraw();
        return this;
    }

    /**
     * @return the selected row's cells, or null when the table is empty
     */
    public String[] selectedRow()
    {
        int i = selectedIndex();
        return i >= 0 && i < rows.size() ? rows.get(i) : null;
    }

    @Override
    protected int rowCount()
    {
        return rows.size();
    }

    @Override
    protected int headerRows()
    {
        return showHeader && !columns.isEmpty() ? 1 : 0;
    }

    @Override
    protected void renderHeader(Canvas header)
    {
        Rect[] cells = columnRects(header.width());
        for (int i = 0; i < columns.size(); i++)
        {
            Text.putAligned(header.clip(cells[i]), 0, columns.get(i).title, columns.get(i).align, headerStyle);
        }
    }

    @Override
    protected void renderRow(Canvas row, int index, Style style)
    {
        String[] cells = rows.get(index);
        Rect[] rects = columnRects(row.width());
        for (int i = 0; i < columns.size(); i++)
        {
            String text = i < cells.length && cells[i] != null ? cells[i] : "";
            Text.putAligned(row.clip(rects[i]), 0, text, columns.get(i).align, style);
        }
    }

    private Rect[] columnRects(int width)
    {
        if (columnsDirty || width != cachedWidth)
        {
            List<Constraint> widths = new ArrayList<>(columns.size());
            for (Column column : columns)
            {
                widths.add(column.width);
            }
            cachedRects = LayoutSolver.solve(new Rect(0, 0, width, 1), widths, Axis.HORIZONTAL);
            cachedWidth = width;
            columnsDirty = false;
        }
        return cachedRects;
    }
}
