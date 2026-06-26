package com.tonic.vellum.widget;

import com.tonic.vellum.Canvas;
import com.tonic.vellum.Section;
import com.tonic.vellum.geom.Rect;
import com.tonic.vellum.style.Style;

/** A single-row text bar, reverse-video by default, for hints and status. */
public final class StatusBar extends Section {

    private String text;
    private Style style = Style.REVERSE;

    /**
     * Creates a status bar with the given text. A {@code null} text is treated as empty.
     *
     * @param text the bar text
     */
    public StatusBar(String text) {
        this.text = text == null ? "" : text;
    }

    /**
     * Sets the bar text. A {@code null} text is treated as empty.
     *
     * @param text the new text
     * @return this StatusBar for chaining
     */
    public StatusBar setText(String text) {
        this.text = text == null ? "" : text;
        requestRedraw();
        return this;
    }

    /**
     * Sets the bar style.
     *
     * @param style the style
     * @return this StatusBar for chaining
     */
    public StatusBar style(Style style) {
        this.style = style;
        requestRedraw();
        return this;
    }

    /**
     * Fills the bar with the style and draws the text.
     *
     * @param canvas the canvas to draw into
     */
    @Override
    protected void render(Canvas canvas) {
        canvas.fill(new Rect(0, 0, canvas.width(), canvas.height()), ' ', style);
        canvas.put(0, 0, text, style);
    }
}
