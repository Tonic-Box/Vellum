package com.tonic.vellum.widget;

import com.tonic.vellum.Canvas;
import com.tonic.vellum.style.Style;

/** A single line of static or settable text, with optional horizontal alignment. */
public final class LabelSection extends SingleRowSection {

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
    protected void renderRow(Canvas canvas, int y) {
        Text.putAligned(canvas, y, text, alignment, style);
    }
}
