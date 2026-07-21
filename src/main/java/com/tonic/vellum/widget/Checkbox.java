package com.tonic.vellum.widget;

import com.tonic.vellum.Canvas;
import com.tonic.vellum.KeyResult;
import com.tonic.vellum.input.KeyEvent;
import com.tonic.vellum.style.Style;

import java.util.function.Consumer;

/**
 * A focusable on/off toggle, flipped by ENTER or SPACE and drawn reversed while focused.
 */
public final class Checkbox extends SingleRowSection
{
    private final String label;
    private boolean checked;
    private Consumer<Boolean> onChange = b -> { };

    /**
     * Creates an unchecked checkbox with the given label.
     *
     * @param label the label text; treated as empty if null
     */
    public Checkbox(String label)
    {
        this.label = label == null ? "" : label;
    }

    /**
     * Sets the checked state.
     *
     * @param checked the new state
     * @return this Checkbox for chaining
     */
    public Checkbox checked(boolean checked)
    {
        this.checked = checked;
        requestRedraw();
        return this;
    }

    /**
     * @return true if checked
     */
    public boolean isChecked()
    {
        return checked;
    }

    /**
     * Sets the handler invoked with the new state whenever the checkbox is toggled.
     *
     * @param handler the change handler
     * @return this Checkbox for chaining
     */
    public Checkbox onChange(Consumer<Boolean> handler)
    {
        this.onChange = handler;
        return this;
    }

    @Override
    protected void renderRow(Canvas canvas, int y)
    {
        Style style = isFocused() ? Style.REVERSE : Style.NORMAL;
        canvas.put(0, y, (checked ? "[x] " : "[ ] ") + label, style);
    }

    @Override
    protected KeyResult onKey(KeyEvent key)
    {
        if (Keys.isActivation(key))
        {
            checked = !checked;
            onChange.accept(checked);
            requestRedraw();
            return KeyResult.CONSUMED;
        }
        return KeyResult.UNHANDLED;
    }
}
