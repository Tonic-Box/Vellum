package com.tonic.vellum.widget;

import com.tonic.vellum.Canvas;
import com.tonic.vellum.Section;

/**
 * Base for leaf widgets that draw a single, vertically-centered row. Handles the empty-bounds
 * guard and the centered row index; subclasses implement {@link #renderRow(Canvas, int)}.
 */
public abstract class SingleRowSection extends Section {

    @Override
    protected final void render(Canvas canvas) {
        if (canvas.width() <= 0 || canvas.height() <= 0) {
            return;
        }
        renderRow(canvas, canvas.height() / 2);
    }

    /** Draw the widget's content on row {@code y}. */
    protected abstract void renderRow(Canvas canvas, int y);
}
