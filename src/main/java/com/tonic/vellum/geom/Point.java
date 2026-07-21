package com.tonic.vellum.geom;

/**
 * An immutable point in cell coordinates.
 */
public final class Point
{
    private final int x;
    private final int y;

    /**
     * Creates a point.
     *
     * @param x x coordinate in cells
     * @param y y coordinate in cells
     */
    public Point(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    /**
     * @return the x coordinate in cells
     */
    public int x()
    {
        return x;
    }

    /**
     * @return the y coordinate in cells
     */
    public int y()
    {
        return y;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Point)) return false;
        Point p = (Point) o;
        return x == p.x && y == p.y;
    }

    @Override
    public int hashCode()
    {
        return 31 * x + y;
    }

    @Override
    public String toString()
    {
        return "Point[" + x + ", " + y + "]";
    }
}
