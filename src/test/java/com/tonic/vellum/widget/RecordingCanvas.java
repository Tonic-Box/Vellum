package com.tonic.vellum.widget;

import com.tonic.vellum.Canvas;
import com.tonic.vellum.geom.Rect;
import com.tonic.vellum.style.Style;

/** A minimal in-memory {@link Canvas} for unit-testing widget rendering. */
final class RecordingCanvas implements Canvas {

    private final int width;
    private final int height;
    private final char[][] chars;
    private final Style[][] styles;

    RecordingCanvas(int width, int height) {
        this.width = width;
        this.height = height;
        this.chars = new char[height][width];
        this.styles = new Style[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                chars[y][x] = ' ';
                styles[y][x] = Style.NORMAL;
            }
        }
    }

    char charAt(int x, int y) {
        return chars[y][x];
    }

    Style styleAt(int x, int y) {
        return styles[y][x];
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public Rect bounds() {
        return new Rect(0, 0, width, height);
    }

    @Override
    public void put(int x, int y, char c) {
        put(x, y, c, Style.NORMAL);
    }

    @Override
    public void put(int x, int y, char c, Style style) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return;
        }
        chars[y][x] = c;
        styles[y][x] = style;
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
        throw new UnsupportedOperationException("clip not needed for these tests");
    }
}
