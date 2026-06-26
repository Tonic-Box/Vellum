package com.tonic.vellum.widget;

import com.tonic.vellum.Canvas;
import com.tonic.vellum.Section;
import com.tonic.vellum.geom.Rect;
import com.tonic.vellum.style.Style;

import java.util.Collections;
import java.util.List;

/**
 * Chrome owner: wraps a single child, draws a border and optional title, and insets the
 * child's bounds by the border thickness. The frame is drawn in {@link #focusedStyle()}
 * when the wrapped child is on the focus path, otherwise {@link #unfocusedStyle()} — so
 * focus is legible without the content section knowing anything about borders.
 */
public final class BorderSection extends Section {

    private static final char TOP_LEFT = '┌';
    private static final char TOP_RIGHT = '┐';
    private static final char BOTTOM_LEFT = '└';
    private static final char BOTTOM_RIGHT = '┘';
    private static final char HORIZONTAL = '─';
    private static final char VERTICAL = '│';

    private final Section child;
    private String title;
    private Style focusedStyle = Style.NORMAL;
    private Style unfocusedStyle = Style.DIM;

    public BorderSection(Section child) {
        this.child = child;
    }

    /** Wrap a section in a border. */
    public static BorderSection around(Section child) {
        return new BorderSection(child);
    }

    /** Wrap a section in a titled border. */
    public static BorderSection around(Section child, String title) {
        return new BorderSection(child).title(title);
    }

    public BorderSection title(String title) {
        this.title = title;
        return this;
    }

    public BorderSection focusedStyle(Style style) {
        this.focusedStyle = style;
        return this;
    }

    public BorderSection unfocusedStyle(Style style) {
        this.unfocusedStyle = style;
        return this;
    }

    public Style focusedStyle() {
        return focusedStyle;
    }

    public Style unfocusedStyle() {
        return unfocusedStyle;
    }

    @Override
    protected List<Section> children() {
        return Collections.singletonList(child);
    }

    @Override
    protected void onResize(Rect newBounds) {
        place(child, newBounds.inset(1));
    }

    @Override
    protected void render(Canvas canvas) {
        int w = canvas.width();
        int h = canvas.height();
        if (w < 2 || h < 2) {
            return;
        }
        Style style = child.isFocused() ? focusedStyle : unfocusedStyle;
        int last = w - 1;
        int bottom = h - 1;

        canvas.put(0, 0, TOP_LEFT, style);
        canvas.put(last, 0, TOP_RIGHT, style);
        canvas.put(0, bottom, BOTTOM_LEFT, style);
        canvas.put(last, bottom, BOTTOM_RIGHT, style);
        for (int x = 1; x < last; x++) {
            canvas.put(x, 0, HORIZONTAL, style);
            canvas.put(x, bottom, HORIZONTAL, style);
        }
        for (int y = 1; y < bottom; y++) {
            canvas.put(0, y, VERTICAL, style);
            canvas.put(last, y, VERTICAL, style);
        }
        if (title != null && !title.isEmpty() && w > 4) {
            // draw into the top edge between the corners; the sub-canvas truncates by display width
            canvas.clip(new Rect(1, 0, w - 2, 1)).put(0, 0, " " + title + " ", style);
        }
    }
}
