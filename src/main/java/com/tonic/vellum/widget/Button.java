package com.tonic.vellum.widget;

import com.tonic.vellum.Canvas;
import com.tonic.vellum.KeyResult;
import com.tonic.vellum.geom.Rect;
import com.tonic.vellum.input.KeyEvent;
import com.tonic.vellum.style.Style;

/**
 * A focusable button that runs its action on ENTER or SPACE and is drawn reversed while focused.
 */
public final class Button extends SingleRowSection
{
    private String label;
    private Runnable onActivate = () -> { };
    private Style style = Style.NORMAL;
    private Style focusedStyle = Style.REVERSE;

    /**
     * Creates a button with the given label.
     *
     * @param label the label text; treated as empty if {@code null}
     */
    public Button(String label)
    {
        this.label = label == null ? "" : label;
    }

    /**
     * Sets the button's label.
     *
     * @param label the label text; treated as empty if {@code null}
     * @return this Button for chaining
     */
    public Button setLabel(String label)
    {
        this.label = label == null ? "" : label;
        requestRedraw();
        return this;
    }

    /**
     * Sets the action run when the button is activated.
     *
     * @param handler the activation handler
     * @return this Button for chaining
     */
    public Button onActivate(Runnable handler)
    {
        this.onActivate = handler;
        return this;
    }

    /**
     * Sets the style used when the button is not focused.
     *
     * @param style the unfocused style
     * @return this Button for chaining
     */
    public Button style(Style style)
    {
        this.style = style;
        requestRedraw();
        return this;
    }

    /**
     * Sets the style used when the button is focused.
     *
     * @param style the focused style
     * @return this Button for chaining
     */
    public Button focusedStyle(Style style)
    {
        this.focusedStyle = style;
        requestRedraw();
        return this;
    }

    @Override
    protected void renderRow(Canvas canvas, int y)
    {
        Style s = isFocused() ? focusedStyle : style;
        canvas.fill(new Rect(0, y, canvas.width(), 1), ' ', s);
        Text.putAligned(canvas, y, "[ " + label + " ]", Alignment.CENTER, s);
    }

    @Override
    protected KeyResult onKey(KeyEvent key)
    {
        if (Keys.isActivation(key))
        {
            onActivate.run();
            return KeyResult.CONSUMED;
        }
        return KeyResult.UNHANDLED;
    }
}
