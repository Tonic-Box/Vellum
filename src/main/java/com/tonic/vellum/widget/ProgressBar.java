package com.tonic.vellum.widget;

import com.tonic.vellum.Canvas;
import com.tonic.vellum.Maths;
import com.tonic.vellum.style.Style;

/**
 * A horizontal progress bar over the range 0 to 1, filled with block glyphs and a partial
 * block for the fractional cell. Optionally overlays a centered percentage.
 */
public final class ProgressBar extends SingleRowSection {

    private double value;
    private boolean showPercent = true;
    private Style filledStyle = Style.NORMAL;
    private Style emptyStyle = Style.DIM;

    /**
     * Sets progress as a fraction, clamped to the range [0, 1].
     *
     * @param value the progress fraction
     * @return this ProgressBar for chaining
     */
    public ProgressBar value(double value) {
        this.value = Maths.clamp(value, 0.0, 1.0);
        requestRedraw();
        return this;
    }

    /**
     * Sets progress as a ratio of current to max; a non-positive max yields zero progress.
     *
     * @param current the current amount
     * @param max the maximum amount
     * @return this ProgressBar for chaining
     */
    public ProgressBar progress(int current, int max) {
        return value(max <= 0 ? 0 : (double) current / max);
    }

    /**
     * Sets whether the centered percentage is shown.
     *
     * @param show {@code true} to overlay the percentage
     * @return this ProgressBar for chaining
     */
    public ProgressBar showPercent(boolean show) {
        this.showPercent = show;
        requestRedraw();
        return this;
    }

    /**
     * Sets the style of the filled portion.
     *
     * @param style the filled style
     * @return this ProgressBar for chaining
     */
    public ProgressBar filledStyle(Style style) {
        this.filledStyle = style;
        requestRedraw();
        return this;
    }

    /**
     * Sets the style of the empty portion.
     *
     * @param style the empty style
     * @return this ProgressBar for chaining
     */
    public ProgressBar emptyStyle(Style style) {
        this.emptyStyle = style;
        requestRedraw();
        return this;
    }

    /**
     * Returns the current progress fraction.
     *
     * @return the progress in the range [0, 1]
     */
    public double value() {
        return value;
    }

    /**
     * Renders the progress bar row.
     */
    @Override
    protected void renderRow(Canvas canvas, int y) {
        int w = canvas.width();
        double exact = value * w;
        int full = (int) Math.floor(exact);

        for (int x = 0; x < w; x++) {
            if (x < full) {
                canvas.put(x, y, Blocks.FULL, filledStyle);
            } else {
                canvas.put(x, y, ' ', emptyStyle);
            }
        }
        int eighths = (int) Math.round((exact - full) * 8);
        if (full < w && eighths > 0) {
            canvas.put(full, y, Blocks.LEFT[eighths - 1], filledStyle);
        }

        if (showPercent) {
            String text = Math.round(value * 100) + "%";
            int px = Math.max(0, (w - text.length()) / 2);
            for (int i = 0; i < text.length() && px + i < w; i++) {
                int cx = px + i;
                canvas.put(cx, y, text.charAt(i), cx < full ? filledStyle : emptyStyle);
            }
        }
    }
}
