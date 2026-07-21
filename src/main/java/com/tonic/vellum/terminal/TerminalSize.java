package com.tonic.vellum.terminal;

/**
 * Immutable terminal dimensions in character cells.
 */
public final class TerminalSize
{
    private final int columns;
    private final int rows;

    /**
     * Creates a terminal size. Negative values are clamped to zero.
     *
     * @param columns width in cells
     * @param rows height in cells
     */
    public TerminalSize(int columns, int rows)
    {
        this.columns = Math.max(0, columns);
        this.rows = Math.max(0, rows);
    }

    /**
     * @return the width in cells
     */
    public int columns()
    {
        return columns;
    }

    /**
     * @return the height in cells
     */
    public int rows()
    {
        return rows;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof TerminalSize)) return false;
        TerminalSize t = (TerminalSize) o;
        return columns == t.columns && rows == t.rows;
    }

    @Override
    public int hashCode()
    {
        return 31 * columns + rows;
    }

    @Override
    public String toString()
    {
        return "TerminalSize[" + columns + "x" + rows + "]";
    }
}
