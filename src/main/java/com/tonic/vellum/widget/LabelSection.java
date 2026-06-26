package com.tonic.vellum.widget;

import com.tonic.vellum.Canvas;
import com.tonic.vellum.CharWidth;
import com.tonic.vellum.Section;
import com.tonic.vellum.style.Style;

/** A single line of static or settable text, with optional horizontal alignment. */
public final class LabelSection extends Section {

    private String text;
    private Alignment alignment = Alignment.LEFT;
    private Style style = Style.NORMAL;

    public LabelSection(String text) {
        this.text = text == null ? "" : text;
    }

    public LabelSection setText(String text) {
        this.text = text == null ? "" : text;
        requestRedraw();
        return this;
    }

    public LabelSection alignment(Alignment alignment) {
        this.alignment = alignment;
        requestRedraw();
        return this;
    }

    public LabelSection style(Style style) {
        this.style = style;
        requestRedraw();
        return this;
    }

    public String text() {
        return text;
    }

    @Override
    protected void render(Canvas canvas) {
        int w = canvas.width();
        int h = canvas.height();
        if (h == 0 || w == 0) {
            return;
        }
        int y = h / 2;
        int textWidth = CharWidth.width(text);
        int x;
        switch (alignment) {
            case CENTER: x = Math.max(0, (w - textWidth) / 2); break;
            case RIGHT:  x = Math.max(0, w - textWidth); break;
            default:     x = 0;
        }
        canvas.put(x, y, text, style);
    }
}
