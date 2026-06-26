package com.tonic.vellum;

import com.tonic.vellum.geom.Rect;
import com.tonic.vellum.style.Style;

/**
 * A drawing surface clipped to a {@link Section}'s bounds. Coordinates are local:
 * {@code (0,0)} is the top-left of the section. Writes outside the section's width or height
 * are silently discarded. A Canvas only mutates cells in the framework's back buffer; it
 * does not emit escape codes.
 */
public interface Canvas {

    /**
     * Return the canvas width in columns.
     *
     * @return the width in columns
     */
    int width();

    /**
     * Return the canvas height in rows.
     *
     * @return the height in rows
     */
    int height();

    /**
     * Return the local bounds: always origin {@code (0,0)} with this canvas's width and height.
     *
     * @return the local bounds
     */
    Rect bounds();

    /**
     * Write a character with the default style.
     *
     * @param x the local column
     * @param y the local row
     * @param c the character to write
     */
    void put(int x, int y, char c);

    /**
     * Write a character with an explicit style.
     *
     * @param x the local column
     * @param y the local row
     * @param c the character to write
     * @param style the style to apply
     */
    void put(int x, int y, char c, Style style);

    /**
     * Write a Unicode code point (including supplementary/astral) with the default style.
     *
     * @param x the local column
     * @param y the local row
     * @param codePoint the Unicode code point to write
     */
    void putCodePoint(int x, int y, int codePoint);

    /**
     * Write a Unicode code point (including supplementary/astral) with an explicit style.
     *
     * @param x the local column
     * @param y the local row
     * @param codePoint the Unicode code point to write
     * @param style the style to apply
     */
    void putCodePoint(int x, int y, int codePoint, Style style);

    /**
     * Write a single line of text with the default style, truncated at the right edge.
     *
     * @param x the local column of the first character
     * @param y the local row
     * @param text the text to write
     */
    void put(int x, int y, String text);

    /**
     * Write a single line of text, truncated at the right edge.
     *
     * @param x the local column of the first character
     * @param y the local row
     * @param text the text to write
     * @param style the style to apply
     */
    void put(int x, int y, String text, Style style);

    /**
     * Fill a local rectangle with a character and style.
     *
     * @param area the local rectangle to fill
     * @param c the character to fill with
     * @param style the style to apply
     */
    void fill(Rect area, char c, Style style);

    /** Fill the entire canvas with spaces in the default style. */
    void clear();

    /**
     * Return a further-clipped sub-canvas whose origin is {@code sub}'s top-left.
     *
     * @param sub the local rectangle to clip to
     * @return the sub-canvas
     */
    Canvas clip(Rect sub);
}
