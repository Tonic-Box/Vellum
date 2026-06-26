package com.tonic.vellum;

import com.tonic.vellum.geom.Rect;
import com.tonic.vellum.style.Style;

/**
 * A width-by-height grid of styled cells, stored as flat parallel arrays
 * (no per-cell objects). Each cell holds a Unicode code point so supplementary
 * (astral) characters are representable. Out-of-range writes are ignored. Package-private:
 * the rendering kernel owns buffers; users only ever touch a {@link Canvas}.
 */
final class Buffer {

    /** Marks the second cell of a wide (double-width) glyph; the renderer skips it. */
    static final int WIDE_CONTINUATION = 0;

    private int width;
    private int height;
    private int[] codePoints;
    private Style[] styles;

    Buffer(int width, int height) {
        resize(width, height);
    }

    int width() { return width; }

    int height() { return height; }

    /** Reallocate to a new size and reset every cell to a blank space in the default style. */
    void resize(int width, int height) {
        this.width = Math.max(0, width);
        this.height = Math.max(0, height);
        int n = this.width * this.height;
        this.codePoints = new int[n];
        this.styles = new Style[n];
        clear();
    }

    /** Reset every cell to a blank space in the default style. */
    void clear() {
        for (int i = 0; i < codePoints.length; i++) {
            codePoints[i] = ' ';
            styles[i] = Style.NORMAL;
        }
    }

    /** Reset a sub-rectangle to blank spaces in the default style. */
    void clearRect(Rect area) {
        int x0 = Math.max(0, area.x());
        int y0 = Math.max(0, area.y());
        int x1 = Math.min(width, area.right());
        int y1 = Math.min(height, area.bottom());
        for (int y = y0; y < y1; y++) {
            int row = y * width;
            for (int x = x0; x < x1; x++) {
                codePoints[row + x] = ' ';
                styles[row + x] = Style.NORMAL;
            }
        }
    }

    /** Mark the whole buffer dirty for the next diff by writing sentinel styles. */
    void invalidate() {
        for (int i = 0; i < codePoints.length; i++) {
            codePoints[i] = ' ';
            styles[i] = null;
        }
    }

    /** Write a single cell; ignored when out of range. */
    void set(int x, int y, int codePoint, Style style) {
        if (x < 0 || x >= width || y < 0 || y >= height) return;
        int i = y * width + x;
        codePoints[i] = codePoint;
        styles[i] = style;
    }

    int codePointAt(int x, int y) {
        return codePoints[y * width + x];
    }

    Style styleAt(int x, int y) {
        return styles[y * width + x];
    }
}
