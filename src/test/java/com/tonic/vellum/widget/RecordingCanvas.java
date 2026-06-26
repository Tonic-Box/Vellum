package com.tonic.vellum.widget;

import com.tonic.vellum.Canvas;
import com.tonic.vellum.geom.Rect;
import com.tonic.vellum.style.Style;

/** A minimal in-memory {@link Canvas} for unit-testing widget rendering; supports {@link #clip}. */
final class RecordingCanvas implements Canvas {

    private final char[][] chars;   // shared backing grid
    private final Style[][] styles;
    private final int ox;
    private final int oy;
    private final int w;
    private final int h;

    RecordingCanvas(int width, int height) {
        this.chars = new char[height][width];
        this.styles = new Style[height][width];
        this.ox = 0;
        this.oy = 0;
        this.w = width;
        this.h = height;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                chars[y][x] = ' ';
                styles[y][x] = Style.NORMAL;
            }
        }
    }

    private RecordingCanvas(char[][] chars, Style[][] styles, int ox, int oy, int w, int h) {
        this.chars = chars;
        this.styles = styles;
        this.ox = ox;
        this.oy = oy;
        this.w = w;
        this.h = h;
    }

    char charAt(int x, int y) {
        return chars[oy + y][ox + x];
    }

    Style styleAt(int x, int y) {
        return styles[oy + y][ox + x];
    }

    @Override
    public int width() {
        return w;
    }

    @Override
    public int height() {
        return h;
    }

    @Override
    public Rect bounds() {
        return new Rect(0, 0, w, h);
    }

    @Override
    public void put(int x, int y, char c) {
        put(x, y, c, Style.NORMAL);
    }

    @Override
    public void put(int x, int y, char c, Style style) {
        if (x < 0 || x >= w || y < 0 || y >= h) {
            return;
        }
        chars[oy + y][ox + x] = c;
        styles[oy + y][ox + x] = style;
    }

    @Override
    public void putCodePoint(int x, int y, int codePoint) {
        put(x, y, (char) codePoint, Style.NORMAL);
    }

    @Override
    public void putCodePoint(int x, int y, int codePoint, Style style) {
        put(x, y, (char) codePoint, style);
    }

    @Override
    public void put(int x, int y, String text) {
        put(x, y, text, Style.NORMAL);
    }

    @Override
    public void put(int x, int y, String text, Style style) {
        for (int i = 0; i < text.length(); i++) {
            put(x + i, y, text.charAt(i), style);
        }
    }

    @Override
    public void fill(Rect area, char c, Style style) {
        for (int y = area.y(); y < area.bottom(); y++) {
            for (int x = area.x(); x < area.right(); x++) {
                put(x, y, c, style);
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
        int cw = Math.max(0, Math.min(w, sub.right()) - sx);
        int ch = Math.max(0, Math.min(h, sub.bottom()) - sy);
        return new RecordingCanvas(chars, styles, ox + sx, oy + sy, cw, ch);
    }
}
