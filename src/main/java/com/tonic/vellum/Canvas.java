package com.tonic.vellum;

import com.tonic.vellum.geom.Rect;
import com.tonic.vellum.style.Style;

/**
 * A drawing surface clipped to a {@link Section}'s bounds. Coordinates are <em>local</em>:
 * {@code (0,0)} is the top-left of the section. Writes outside {@code [0,width) x [0,height)}
 * are silently discarded - this clipping guarantee is central to the framework.
 *
 * <p>The user never sees escape codes; a Canvas only mutates cells in the framework's
 * back buffer.
 */
public interface Canvas {

    int width();

    int height();

    /** Local bounds: always origin {@code (0,0)} with this canvas's width and height. */
    Rect bounds();

    /** Write a character with the default style. */
    void put(int x, int y, char c);

    /** Write a character with an explicit style. */
    void put(int x, int y, char c, Style style);

    /** Write a Unicode code point (including supplementary/astral) with the default style. */
    void putCodePoint(int x, int y, int codePoint);

    /** Write a Unicode code point (including supplementary/astral) with an explicit style. */
    void putCodePoint(int x, int y, int codePoint, Style style);

    /** Write a single line of text (default style), truncated at the right edge. */
    void put(int x, int y, String text);

    /** Write a single line of text, truncated at the right edge. */
    void put(int x, int y, String text, Style style);

    /** Fill a local rectangle with a character and style. */
    void fill(Rect area, char c, Style style);

    /** Fill the entire canvas with spaces in the default style. */
    void clear();

    /** Return a further-clipped sub-canvas whose origin is {@code sub}'s top-left. */
    Canvas clip(Rect sub);
}
