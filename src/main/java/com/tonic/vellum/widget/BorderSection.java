package com.tonic.vellum.widget;

import com.tonic.vellum.Canvas;
import com.tonic.vellum.Section;
import com.tonic.vellum.geom.Rect;
import com.tonic.vellum.style.Style;

import java.util.Collections;
import java.util.List;

/**
 * A border with an optional title around a single child, insetting the child's bounds by one
 * cell. The frame is drawn in the focused style while the child is on the focus path.
 */
public final class BorderSection extends Section
{
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
    public BorderSection(Section child)
    {
        this.child = child;
    }

    /**
     * Wraps a section in a border.
     *
     * @param child the wrapped child section
     * @return a new border around the child
     */
    public static BorderSection around(Section child)
    {
        return new BorderSection(child);
    }

    /**
     * Wraps a section in a titled border.
     *
     * @param child the wrapped child section
     * @param title the border title
     * @return a new titled border around the child
     */
    public static BorderSection around(Section child, String title)
    {
        return new BorderSection(child).title(title);
    }

    /**
     * Sets the border title drawn into the top edge.
     *
     * @param title the title text
     * @return this BorderSection for chaining
     */
    public BorderSection title(String title)
    {
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
    public BorderSection focusedStyle(Style style)
    {
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
    public BorderSection unfocusedStyle(Style style)
    {
        this.unfocusedStyle = style;
        requestRedraw();
        return this;
    }

    /**
     * @return the frame style used when the child is on the focus path
     */
    public Style focusedStyle()
    {
        return focusedStyle;
    }

    /**
     * @return the frame style used when the child is not on the focus path
     */
    public Style unfocusedStyle()
    {
        return unfocusedStyle;
    }

    @Override
    protected List<Section> children()
    {
        return Collections.singletonList(child);
    }

    @Override
    protected void onResize(Rect newBounds)
    {
        place(child, newBounds.inset(1));
    }

    @Override
    protected void render(Canvas canvas)
    {
        int w = canvas.width();
        int h = canvas.height();
        if (w < 2 || h < 2)
        {
            return;
        }
        Style style = child.isFocused() ? focusedStyle : unfocusedStyle;
        int last = w - 1;
        int bottom = h - 1;

        canvas.put(0, 0, TOP_LEFT, style);
        canvas.put(last, 0, TOP_RIGHT, style);
        canvas.put(0, bottom, BOTTOM_LEFT, style);
        canvas.put(last, bottom, BOTTOM_RIGHT, style);
        for (int x = 1; x < last; x++)
        {
            canvas.put(x, 0, HORIZONTAL, style);
            canvas.put(x, bottom, HORIZONTAL, style);
        }
        for (int y = 1; y < bottom; y++)
        {
            canvas.put(0, y, VERTICAL, style);
            canvas.put(last, y, VERTICAL, style);
        }
        if (title != null && !title.isEmpty() && w > 4)
        {
            canvas.clip(new Rect(1, 0, w - 2, 1)).put(0, 0, " " + title + " ", style);
        }
    }
}
