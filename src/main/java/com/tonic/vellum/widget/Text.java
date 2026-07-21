package com.tonic.vellum.widget;

import com.tonic.vellum.Canvas;
import com.tonic.vellum.CharWidth;
import com.tonic.vellum.style.Style;

final class Text
{
    private Text()
    {
    }

    static int alignedX(int width, int textWidth, Alignment align)
    {
        switch (align)
        {
            case CENTER: return Math.max(0, (width - textWidth) / 2);
            case RIGHT:  return Math.max(0, width - textWidth);
            default:     return 0;
        }
    }

    static void putAligned(Canvas canvas, int y, String text, Alignment align, Style style)
    {
        canvas.put(alignedX(canvas.width(), CharWidth.width(text), align), y, text, style);
    }
}
