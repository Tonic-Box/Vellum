package com.tonic.vellum;

import com.tonic.vellum.geom.Rect;
import com.tonic.vellum.style.Style;

/**
 * A drawing surface clipped to a section's bounds. Coordinates are local with (0,0) at the
 * section's top-left; out-of-range writes are silently discarded, and writes mutate the
 * back buffer without emitting escape codes.
 */
public interface Canvas
{
    /**
     * @return the canvas width in columns
     */
    int width();

    /**
     * @return the canvas height in rows
     */
    int height();

    /**
     * @return the local bounds: origin (0,0) with this canvas's width and height
     */
    Rect bounds();

    /**
     * Writes a character with the default style.
     *
     * @param x the local column
     * @param y the local row
     * @param c the character to write
     */
    void put(int x, int y, char c);

    /**
     * Writes a character with an explicit style.
     *
     * @param x the local column
     * @param y the local row
     * @param c the character to write
     * @param style the style to apply
     */
    void put(int x, int y, char c, Style style);

    /**
     * Writes a Unicode code point, including supplementary characters, with the default style.
     *
     * @param x the local column
     * @param y the local row
     * @param codePoint the Unicode code point to write
     */
    void putCodePoint(int x, int y, int codePoint);

    /**
     * Writes a Unicode code point, including supplementary characters, with an explicit style.
     *
     * @param x the local column
     * @param y the local row
     * @param codePoint the Unicode code point to write
     * @param style the style to apply
     */
    void putCodePoint(int x, int y, int codePoint, Style style);

    /**
     * Writes a single line of text with the default style, truncated at the right edge.
     *
     * @param x the local column of the first character
     * @param y the local row
     * @param text the text to write
     */
    void put(int x, int y, String text);

    /**
     * Writes a single line of text, truncated at the right edge.
     *
     * @param x the local column of the first character
     * @param y the local row
     * @param text the text to write
     * @param style the style to apply
     */
    void put(int x, int y, String text, Style style);

    /**
     * Fills a local rectangle with a character and style.
     *
     * @param area the local rectangle to fill
     * @param c the character to fill with
     * @param style the style to apply
     */
    void fill(Rect area, char c, Style style);

    /**
     * Fills the entire canvas with spaces in the default style.
     */
    void clear();

    /**
     * Returns a further-clipped sub-canvas whose origin is the given rectangle's top-left.
     *
     * @param sub the local rectangle to clip to
     * @return the sub-canvas
     */
    Canvas clip(Rect sub);
}
