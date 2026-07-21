package com.tonic.vellum.widget;

import com.tonic.vellum.Canvas;
import com.tonic.vellum.Section;
import com.tonic.vellum.style.Style;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Multi-line static or settable text, drawn top-down. Lines split on {@code \n}; optional word wrapping.
 */
public final class TextSection extends Section
{
    private List<String> lines;
    private Style style = Style.NORMAL;
    private boolean wrap;

    /**
     * Creates a text section. The text is split into lines on {@code \n}.
     *
     * @param text the initial text
     */
    public TextSection(String text)
    {
        this.lines = split(text);
    }

    /**
     * Sets the text. The text is split into lines on {@code \n}.
     *
     * @param text the new text
     * @return this TextSection for chaining
     */
    public TextSection setText(String text)
    {
        this.lines = split(text);
        requestRedraw();
        return this;
    }

    /**
     * Sets the text style.
     *
     * @param style the style
     * @return this TextSection for chaining
     */
    public TextSection style(Style style)
    {
        this.style = style;
        requestRedraw();
        return this;
    }

    /**
     * Enables width-aware word wrapping (default off).
     *
     * @param wrap {@code true} to enable wrapping
     * @return this TextSection for chaining
     */
    public TextSection wrap(boolean wrap)
    {
        this.wrap = wrap;
        requestRedraw();
        return this;
    }

    private static List<String> split(String text)
    {
        if (text == null || text.isEmpty())
        {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(text.split("\n", -1)));
    }

    @Override
    protected void render(Canvas canvas)
    {
        List<String> display = wrap ? TextWrap.wrapAll(lines, canvas.width()) : lines;
        for (int i = 0; i < display.size() && i < canvas.height(); i++)
        {
            canvas.put(0, i, display.get(i), style);
        }
    }
}
