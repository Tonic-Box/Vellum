package com.tonic.vellum.widget;

import com.tonic.vellum.Canvas;
import com.tonic.vellum.KeyResult;
import com.tonic.vellum.input.KeyEvent;
import com.tonic.vellum.style.Style;

import java.util.function.Consumer;

/**
 * A focusable on/off toggle. ENTER or SPACE flips its state, and it is drawn reversed while
 * focused.
 */
public final class Checkbox extends SingleRowSection {

    private final String label;
    private boolean checked;
    private Consumer<Boolean> onChange = b -> { };

    /**
     * Creates an unchecked checkbox with the given label.
     *
     * @param label the label text; treated as empty if {@code null}
     */
    public Checkbox(String label) {
        this.label = label == null ? "" : label;
    }

    /**
     * Sets the checked state.
     *
     * @param checked {@code true} to check, {@code false} to uncheck
     * @return this Checkbox for chaining
     */
    public Checkbox checked(boolean checked) {
        this.checked = checked;
        requestRedraw();
        return this;
    }

    /**
     * Returns whether the checkbox is checked.
     *
     * @return {@code true} if checked
     */
    public boolean isChecked() {
        return checked;
    }

    /**
     * Sets the handler invoked with the new state whenever the checkbox is toggled.
     *
     * @param handler consumer receiving the new checked state
     * @return this Checkbox for chaining
     */
    public Checkbox onChange(Consumer<Boolean> handler) {
        this.onChange = handler;
        return this;
    }

    /**
     * Renders the checkbox row.
     */
    @Override
    protected void renderRow(Canvas canvas, int y) {
        Style style = isFocused() ? Style.REVERSE : Style.NORMAL;
        canvas.put(0, y, (checked ? "[x] " : "[ ] ") + label, style);
    }

    /**
     * Toggles the checkbox on ENTER or SPACE.
     */
    @Override
    protected KeyResult onKey(KeyEvent key) {
        if (Keys.isActivation(key)) {
            checked = !checked;
            onChange.accept(checked);
            requestRedraw();
            return KeyResult.CONSUMED;
        }
        return KeyResult.UNHANDLED;
    }
}
