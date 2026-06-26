package com.tonic.vellum.widget;

import com.tonic.vellum.Canvas;
import com.tonic.vellum.Section;
import com.tonic.vellum.geom.Rect;
import com.tonic.vellum.style.Style;

import java.util.Collections;
import java.util.List;

/**
 * Wraps a single child, draws a border and optional title, and insets the child's bounds by
 * the border thickness. The frame is drawn in {@link #focusedStyle()} when the wrapped child
 * is on the focus path, otherwise {@link #unfocusedStyle()}.
 */
public final class BorderSection extends Section {

    private static final char TOP_LEFT = '\u250C';
    private static final char TOP_RIGHT = '\u2510';
    private static final char BOTTOM_LEFT = '\u2514';
    private static final char BOTTOM_RIGHT = '\u2518';
    private static final char HORIZONTAL = '\u2500';
    private static final char VERTICAL = '\u2502';

    private final Section child;
    private String title;
    private Style focusedStyle = Style.NORMAL;
    private Style unfocusedStyle = Style.DIM;

    /**
     * Creates a border around the given child.
     *
     * @param child the wrapped child section
     */
    public BorderSection(Section child) {
        this.child = child;
    }

    /**
     * Wraps a section in a border.
     *
     * @param child the wrapped child section
     * @return a new border around the child
     */
    public static BorderSection around(Section child) {
        return new BorderSection(child);
    }

    /**
     * Wraps a section in a titled border.
     *
     * @param child the wrapped child section
     * @param title the border title
     * @return a new titled border around the child
     */
    public static BorderSection around(Section child, String title) {
        return new BorderSection(child).title(title);
    }

    /**
     * Sets the border title drawn into the top edge.
     *
     * @param title the title text
     * @return this BorderSection for chaining
     */
    public BorderSection title(String title) {
        this.title = title;
        requestRedraw();
        return this;
    }

    /**
     * Sets the frame style used when the child is on the focus path.
     *
     * @param style the focused frame style
     * @return this BorderSection for chaining
     */
    public BorderSection focusedStyle(Style style) {
        this.focusedStyle = style;
        requestRedraw();
        return this;
    }

    /**
     * Sets the frame style used when the child is not on the focus path.
     *
     * @param style the unfocused frame style
     * @return this BorderSection for chaining
     */
    public BorderSection unfocusedStyle(Style style) {
        this.unfocusedStyle = style;
        requestRedraw();
        return this;
    }

    /**
     * Returns the frame style used when the child is on the focus path.
     *
     * @return the focused frame style
     */
    public Style focusedStyle() {
        return focusedStyle;
    }

    /**
     * Returns the frame style used when the child is not on the focus path.
     *
     * @return the unfocused frame style
     */
    public Style unfocusedStyle() {
        return unfocusedStyle;
    }

    /**
     * Returns the wrapped child as the sole child section.
     *
     * @return a singleton list containing the child
     */
    @Override
    protected List<Section> children() {
        return Collections.singletonList(child);
    }

    /**
     * Insets the child's bounds by the border thickness.
     *
     * @param newBounds the new bounds of this section
     */
    @Override
    protected void onResize(Rect newBounds) {
        place(child, newBounds.inset(1));
    }

    /**
     * Draws the border frame and optional title.
     *
     * @param canvas the canvas to draw into
     */
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
