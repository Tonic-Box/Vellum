package com.tonic.vellum.widget;

import com.tonic.vellum.Canvas;
import com.tonic.vellum.Maths;
import com.tonic.vellum.style.Style;

/**
 * A horizontal progress bar (0..1), filled with block glyphs (with a partial block for the
 * fractional cell). Optionally overlays a centered percentage.
 */
public final class ProgressBar extends SingleRowSection {

    private double value;
    private boolean showPercent = true;
    private Style filledStyle = Style.NORMAL;
    private Style emptyStyle = Style.DIM;

    /** Set progress as a fraction in [0, 1]. */
    public ProgressBar value(double value) {
        this.value = Maths.clamp(value, 0.0, 1.0);
        requestRedraw();
        return this;
    }

    /** Set progress as {@code current} out of {@code max}. */
    public ProgressBar progress(int current, int max) {
        return value(max <= 0 ? 0 : (double) current / max);
    }

    public ProgressBar showPercent(boolean show) {
        this.showPercent = show;
        requestRedraw();
        return this;
    }

    public ProgressBar filledStyle(Style style) {
        this.filledStyle = style;
        requestRedraw();
        return this;
    }

    public ProgressBar emptyStyle(Style style) {
        this.emptyStyle = style;
        requestRedraw();
        return this;
    }

    public double value() {
        return value;
    }

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
