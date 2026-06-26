package com.tonic.vellum;

import com.tonic.vellum.geom.Rect;
import com.tonic.vellum.style.Style;

/**
 * {@link Canvas} backed by a {@link Buffer}. Local coordinates are translated to absolute
 * buffer coordinates by a fixed origin; writes outside this canvas's logical area or
 * outside the buffer are discarded. {@link #clip(Rect)} intersects with the current area
 * so nested canvases can never draw outside their parent.
 */
final class ClippedCanvas implements Canvas {

    private final Buffer buffer;
    private final int originX;
    private final int originY;
    private final int width;
    private final int height;

    /** Create a canvas whose local origin maps to the given absolute buffer position. */
    ClippedCanvas(Buffer buffer, Rect absolute) {
        this(buffer, absolute.x(), absolute.y(), absolute.width(), absolute.height());
    }

    private ClippedCanvas(Buffer buffer, int originX, int originY, int width, int height) {
        this.buffer = buffer;
        this.originX = originX;
        this.originY = originY;
        this.width = Math.max(0, width);
        this.height = Math.max(0, height);
    }

    @Override
    public int width() { return width; }

    @Override
    public int height() { return height; }

    @Override
    public Rect bounds() { return new Rect(0, 0, width, height); }

    @Override
    public void put(int x, int y, char c) {
        putCodePoint(x, y, c, Style.NORMAL);
    }

    @Override
    public void put(int x, int y, char c, Style style) {
        putCodePoint(x, y, c, style);
    }

    @Override
    public void putCodePoint(int x, int y, int codePoint) {
        putCodePoint(x, y, codePoint, Style.NORMAL);
    }

    @Override
    public void putCodePoint(int x, int y, int codePoint, Style style) {
        if (y < 0 || y >= height) return;
        int cw = CharWidth.of(codePoint);
        if (cw == 0) {
            return; // zero-width characters cannot occupy a cell on their own
        }
        if (x < 0 || x >= width) return;
        if (cw == 2) {
            if (x + 1 >= width) {
                buffer.set(originX + x, originY + y, ' ', style); // would straddle the edge
                return;
            }
            buffer.set(originX + x, originY + y, codePoint, style);
            buffer.set(originX + x + 1, originY + y, Buffer.WIDE_CONTINUATION, style);
        } else {
            buffer.set(originX + x, originY + y, codePoint, style);
        }
    }

    @Override
    public void put(int x, int y, String text) {
        put(x, y, text, Style.NORMAL);
    }

    @Override
    public void put(int x, int y, String text, Style style) {
        if (text == null) return;
        int col = x;
        int i = 0;
        while (i < text.length()) {
            int cp = text.codePointAt(i);
            i += Character.charCount(cp);
            int cw = CharWidth.of(cp);
            if (cw == 0) {
                continue; // skip zero-width / combining marks
            }
            if (col + cw > width) break; // truncate without splitting a wide glyph
            putCodePoint(col, y, cp, style);
            col += cw;
        }
    }

    @Override
    public void fill(Rect area, char c, Style style) {
        int x0 = Math.max(0, area.x());
        int y0 = Math.max(0, area.y());
        int x1 = Math.min(width, area.right());
        int y1 = Math.min(height, area.bottom());
        for (int y = y0; y < y1; y++) {
            for (int x = x0; x < x1; x++) {
                buffer.set(originX + x, originY + y, c, style);
            }
        }
    }

    @Override
    public void clear() {
        fill(bounds(), ' ', Style.NORMAL);
    }

    @Override
    public Canvas clip(Rect sub) {
        int sx = Math.max(0, sub.x());
        int sy = Math.max(0, sub.y());
        int sx1 = Math.min(width, sub.right());
        int sy1 = Math.min(height, sub.bottom());
        int w = Math.max(0, sx1 - sx);
        int h = Math.max(0, sy1 - sy);
        return new ClippedCanvas(buffer, originX + sx, originY + sy, w, h);
    }
}
