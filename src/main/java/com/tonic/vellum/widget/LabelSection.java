package com.tonic.vellum.widget;

import com.tonic.vellum.Canvas;
import com.tonic.vellum.style.Style;

/**
 * A single line of static or settable text, with optional horizontal alignment.
 */
public final class LabelSection extends SingleRowSection
{
    private String text;
    private Alignment alignment = Alignment.LEFT;
    private Style style = Style.NORMAL;

    /**
     * Creates a label with the given text. A {@code null} text is treated as empty.
     *
     * @param text the label text
     */
    public LabelSection(String text)
    {
        this.text = text == null ? "" : text;
    }

    /**
     * Sets the label text. A {@code null} text is treated as empty.
     *
     * @param text the new text
     * @return this LabelSection for chaining
     */
    public LabelSection setText(String text)
    {
        this.text = text == null ? "" : text;
        requestRedraw();
        return this;
    }

    /**
     * Sets the horizontal alignment.
     *
     * @param alignment the alignment
     * @return this LabelSection for chaining
     */
    public LabelSection alignment(Alignment alignment)
    {
        this.alignment = alignment;
        requestRedraw();
        return this;
    }

    /**
     * Sets the text style.
     *
     * @param style the style
     * @return this LabelSection for chaining
     */
    public LabelSection style(Style style)
    {
        this.style = style;
        requestRedraw();
        return this;
    }

    /**
     * @return the current text
     */
    public String text()
    {
        return text;
    }

    @Override
    protected void renderRow(Canvas canvas, int y)
    {
        Text.putAligned(canvas, y, text, alignment, style);
    }
}
