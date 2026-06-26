package com.tonic.vellum.geom;

/**
 * Immutable rectangle in cell coordinates with pure carving helpers.
 *
 * <p>All helpers are non-mutating: {@code take*} returns a strip, {@code split*}
 * returns {@code {strip, remainder}}. There is no stateful cursor.
 */
public final class Rect {

    private final int x;
    private final int y;
    private final int width;
    private final int height;

    public Rect(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = Math.max(0, width);
        this.height = Math.max(0, height);
    }

    public int x() { return x; }
    public int y() { return y; }
    public int width() { return width; }
    public int height() { return height; }

    /** Exclusive right edge: {@code x + width}. */
    public int right() { return x + width; }

    /** Exclusive bottom edge: {@code y + height}. */
    public int bottom() { return y + height; }

    /** True when this rect covers zero cells. */
    public boolean isEmpty() { return width == 0 || height == 0; }

    /** True when the absolute point lies inside this rect. */
    public boolean contains(int px, int py) {
        return px >= x && px < right() && py >= y && py < bottom();
    }

    /** Shrink on all four sides by {@code amount}; dimensions clamp at zero. */
    public Rect inset(int amount) {
        return new Rect(x + amount, y + amount, width - 2 * amount, height - 2 * amount);
    }

    /** Top strip of the given height, clamped to this rect. */
    public Rect takeTop(int rows) {
        int h = clamp(rows, height);
        return new Rect(x, y, width, h);
    }

    /** Bottom strip of the given height, clamped to this rect. */
    public Rect takeBottom(int rows) {
        int h = clamp(rows, height);
        return new Rect(x, bottom() - h, width, h);
    }

    /** Left strip of the given width, clamped to this rect. */
    public Rect takeLeft(int cols) {
        int w = clamp(cols, width);
        return new Rect(x, y, w, height);
    }

    /** Right strip of the given width, clamped to this rect. */
    public Rect takeRight(int cols) {
        int w = clamp(cols, width);
        return new Rect(right() - w, y, w, height);
    }

    /** Split off a top strip: {@code {top, rest}}. */
    public Rect[] splitTop(int rows) {
        int h = clamp(rows, height);
        return new Rect[]{
                new Rect(x, y, width, h),
                new Rect(x, y + h, width, height - h)
        };
    }

    /** Split off a bottom strip: {@code {bottom, rest}}. */
    public Rect[] splitBottom(int rows) {
        int h = clamp(rows, height);
        return new Rect[]{
                new Rect(x, bottom() - h, width, h),
                new Rect(x, y, width, height - h)
        };
    }

    /** Split off a left strip: {@code {left, rest}}. */
    public Rect[] splitLeft(int cols) {
        int w = clamp(cols, width);
        return new Rect[]{
                new Rect(x, y, w, height),
                new Rect(x + w, y, width - w, height)
        };
    }

    /** Split off a right strip: {@code {right, rest}}. */
    public Rect[] splitRight(int cols) {
        int w = clamp(cols, width);
        return new Rect[]{
                new Rect(right() - w, y, w, height),
                new Rect(x, y, width - w, height)
        };
    }

    private static int clamp(int requested, int available) {
        if (requested < 0) return 0;
        return Math.min(requested, available);
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
