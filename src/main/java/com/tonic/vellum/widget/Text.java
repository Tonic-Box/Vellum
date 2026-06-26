package com.tonic.vellum.widget;

import com.tonic.vellum.Canvas;
import com.tonic.vellum.CharWidth;
import com.tonic.vellum.style.Style;

/** Display-width-aware text alignment helpers for widgets. */
final class Text {

    private Text() {}

    /** The starting column for {@code textWidth} display columns aligned within {@code width}. */
    static int alignedX(int width, int textWidth, Alignment align) {
        switch (align) {
            case CENTER: return Math.max(0, (width - textWidth) / 2);
            case RIGHT:  return Math.max(0, width - textWidth);
            default:     return 0;
        }
    }

    /** Draw {@code text} on row {@code y}, aligned by display width within the canvas. */
    static void putAligned(Canvas canvas, int y, String text, Alignment align, Style style) {
        canvas.put(alignedX(canvas.width(), CharWidth.width(text), align), y, text, style);
    }
}
