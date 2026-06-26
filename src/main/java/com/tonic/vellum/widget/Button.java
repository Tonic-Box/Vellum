package com.tonic.vellum.widget;

import com.tonic.vellum.Canvas;
import com.tonic.vellum.KeyResult;
import com.tonic.vellum.geom.Rect;
import com.tonic.vellum.input.Key;
import com.tonic.vellum.input.KeyEvent;
import com.tonic.vellum.style.Style;

/** A focusable, activatable button. ENTER or SPACE runs its action; reversed when focused. */
public final class Button extends SingleRowSection {

    private String label;
    private Runnable onActivate = () -> { };
    private Style style = Style.NORMAL;
    private Style focusedStyle = Style.REVERSE;

    public Button(String label) {
        this.label = label == null ? "" : label;
    }

    public Button setLabel(String label) {
        this.label = label == null ? "" : label;
        requestRedraw();
        return this;
    }

    public Button onActivate(Runnable handler) {
        this.onActivate = handler;
        return this;
    }

    public Button style(Style style) {
        this.style = style;
        requestRedraw();
        return this;
    }

    public Button focusedStyle(Style style) {
        this.focusedStyle = style;
        requestRedraw();
        return this;
    }

    @Override
    protected void renderRow(Canvas canvas, int y) {
        Style s = isFocused() ? focusedStyle : style;
        canvas.fill(new Rect(0, y, canvas.width(), 1), ' ', s);
        Text.putAligned(canvas, y, "[ " + label + " ]", Alignment.CENTER, s);
    }

    @Override
    protected KeyResult onKey(KeyEvent key) {
        if (key.is(Key.ENTER) || (key.is(Key.CHAR) && key.ch() == ' ')) {
            onActivate.run();
            return KeyResult.CONSUMED;
        }
        return KeyResult.UNHANDLED;
    }
}
