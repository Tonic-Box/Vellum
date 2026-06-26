package com.tonic.vellum.widget;

import com.tonic.vellum.Canvas;
import com.tonic.vellum.Section;

/**
 * Base for leaf widgets that draw a single, vertically centered row. Handles the empty-bounds
 * guard and the centered row index; subclasses implement {@link #renderRow(Canvas, int)}.
 */
public abstract class SingleRowSection extends Section {

    /**
     * Renders the widget. Skips empty bounds and otherwise delegates to
     * {@link #renderRow(Canvas, int)} with the vertically centered row index.
     *
     * @param canvas the canvas to draw into
     */
    @Override
    protected final void render(Canvas canvas) {
        if (canvas.width() <= 0 || canvas.height() <= 0) {
            return;
        }
        renderRow(canvas, canvas.height() / 2);
    }

    /**
     * Draws the widget's content on row {@code y}.
     *
     * @param canvas the canvas to draw into
     * @param y the centered row index to draw on
     */
    protected abstract void renderRow(Canvas canvas, int y);
}
