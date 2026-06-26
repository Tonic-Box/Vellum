package com.tonic.vellum.widget;

import com.tonic.vellum.Canvas;
import com.tonic.vellum.KeyResult;
import com.tonic.vellum.input.Key;
import com.tonic.vellum.input.KeyEvent;
import com.tonic.vellum.style.Style;

import java.util.function.Consumer;

/** A focusable on/off toggle. ENTER or SPACE flips it; reversed when focused. */
public final class Checkbox extends SingleRowSection {

    private final String label;
    private boolean checked;
    private Consumer<Boolean> onChange = b -> { };

    public Checkbox(String label) {
        this.label = label == null ? "" : label;
    }

    public Checkbox checked(boolean checked) {
        this.checked = checked;
        requestRedraw();
        return this;
    }

    public boolean isChecked() {
        return checked;
    }

    public Checkbox onChange(Consumer<Boolean> handler) {
        this.onChange = handler;
        return this;
    }

    @Override
    protected void renderRow(Canvas canvas, int y) {
        Style style = isFocused() ? Style.REVERSE : Style.NORMAL;
        canvas.put(0, y, (checked ? "[x] " : "[ ] ") + label, style);
    }

    @Override
    protected KeyResult onKey(KeyEvent key) {
        if (key.is(Key.ENTER) || (key.is(Key.CHAR) && key.ch() == ' ')) {
            checked = !checked;
            onChange.accept(checked);
            requestRedraw();
            return KeyResult.CONSUMED;
        }
        return KeyResult.UNHANDLED;
    }
}
