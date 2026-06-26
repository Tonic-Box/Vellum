package com.tonic.vellum.geom;

import com.tonic.vellum.Maths;

/**
 * Immutable rectangle in cell coordinates. The {@code take*} methods return a strip and
 * the {@code split*} methods return {@code {strip, remainder}}; both leave this rect
 * unchanged.
 */
public final class Rect {

    private final int x;
    private final int y;
    private final int width;
    private final int height;

    /**
     * Creates a rectangle. Negative width or height is clamped to zero.
     *
     * @param x left edge in cells
     * @param y top edge in cells
     * @param width width in cells
     * @param height height in cells
     */
    public Rect(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = Math.max(0, width);
        this.height = Math.max(0, height);
    }

    /** @return the left edge in cells */
    public int x() { return x; }
    /** @return the top edge in cells */
    public int y() { return y; }
    /** @return the width in cells */
    public int width() { return width; }
    /** @return the height in cells */
    public int height() { return height; }

    /**
     * Returns the exclusive right edge.
     *
     * @return {@code x + width}
     */
    public int right() { return x + width; }

    /**
     * Returns the exclusive bottom edge.
     *
     * @return {@code y + height}
     */
    public int bottom() { return y + height; }

    /**
     * Reports whether this rect covers zero cells.
     *
     * @return true when the width or height is zero
     */
    public boolean isEmpty() { return width == 0 || height == 0; }

    /**
     * Reports whether the absolute point lies inside this rect.
     *
     * @param px point x in cells
     * @param py point y in cells
     * @return true when the point is within these bounds
     */
    public boolean contains(int px, int py) {
        return px >= x && px < right() && py >= y && py < bottom();
    }

    /**
     * Shrinks this rect on all four sides.
     *
     * @param amount cells to remove from each side; dimensions clamp at zero
     * @return a new rect inset by the amount
     */
    public Rect inset(int amount) {
        return new Rect(x + amount, y + amount, width - 2 * amount, height - 2 * amount);
    }

    /**
     * Returns the top strip of the given height, clamped to this rect.
     *
     * @param rows strip height in cells
     * @return the top strip
     */
    public Rect takeTop(int rows) {
        int h = clamp(rows, height);
        return new Rect(x, y, width, h);
    }

    /**
     * Returns the bottom strip of the given height, clamped to this rect.
     *
     * @param rows strip height in cells
     * @return the bottom strip
     */
    public Rect takeBottom(int rows) {
        int h = clamp(rows, height);
        return new Rect(x, bottom() - h, width, h);
    }

    /**
     * Returns the left strip of the given width, clamped to this rect.
     *
     * @param cols strip width in cells
     * @return the left strip
     */
    public Rect takeLeft(int cols) {
        int w = clamp(cols, width);
        return new Rect(x, y, w, height);
    }

    /**
     * Returns the right strip of the given width, clamped to this rect.
     *
     * @param cols strip width in cells
     * @return the right strip
     */
    public Rect takeRight(int cols) {
        int w = clamp(cols, width);
        return new Rect(right() - w, y, w, height);
    }

    /**
     * Splits off a top strip.
     *
     * @param rows strip height in cells
     * @return a two-element array {@code {top, rest}}
     */
    public Rect[] splitTop(int rows) {
        int h = clamp(rows, height);
        return new Rect[]{
                new Rect(x, y, width, h),
                new Rect(x, y + h, width, height - h)
        };
    }

    /**
     * Splits off a bottom strip.
     *
     * @param rows strip height in cells
     * @return a two-element array {@code {bottom, rest}}
     */
    public Rect[] splitBottom(int rows) {
        int h = clamp(rows, height);
        return new Rect[]{
                new Rect(x, bottom() - h, width, h),
                new Rect(x, y, width, height - h)
        };
    }

    /**
     * Splits off a left strip.
     *
     * @param cols strip width in cells
     * @return a two-element array {@code {left, rest}}
     */
    public Rect[] splitLeft(int cols) {
        int w = clamp(cols, width);
        return new Rect[]{
                new Rect(x, y, w, height),
                new Rect(x + w, y, width - w, height)
        };
    }

    /**
     * Splits off a right strip.
     *
     * @param cols strip width in cells
     * @return a two-element array {@code {right, rest}}
     */
    public Rect[] splitRight(int cols) {
        int w = clamp(cols, width);
        return new Rect[]{
                new Rect(right() - w, y, w, height),
                new Rect(x, y, width - w, height)
        };
    }

    private static int clamp(int requested, int available) {
        return Maths.clamp(requested, 0, available);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rect)) return false;
        Rect r = (Rect) o;
        return x == r.x && y == r.y && width == r.width && height == r.height;
    }

    @Override
    public int hashCode() {
        int h = x;
        h = 31 * h + y;
        h = 31 * h + width;
        h = 31 * h + height;
        return h;
    }

    @Override
    public String toString() {
        return "Rect[x=" + x + ", y=" + y + ", w=" + width + ", h=" + height + "]";
    }
}
