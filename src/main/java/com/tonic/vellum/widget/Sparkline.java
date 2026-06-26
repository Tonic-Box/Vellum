package com.tonic.vellum.widget;

import com.tonic.vellum.Canvas;
import com.tonic.vellum.style.Style;

import java.util.List;

/**
 * A one-row chart of a value series drawn with partial block glyphs, auto-scaled between the
 * series min and max. The newest values are on the right; older values scroll off the left
 * when the series is wider than the section.
 */
public final class Sparkline extends SingleRowSection {

    private double[] values = new double[0];
    private Style style = Style.NORMAL;

    public Sparkline setValues(double[] values) {
        this.values = values == null ? new double[0] : values.clone();
        requestRedraw();
        return this;
    }

    public Sparkline setValues(List<? extends Number> values) {
        if (values == null) {
            this.values = new double[0];
        } else {
            this.values = new double[values.size()];
            for (int i = 0; i < values.size(); i++) {
                this.values[i] = values.get(i).doubleValue();
            }
        }
        requestRedraw();
        return this;
    }

    public Sparkline style(Style style) {
        this.style = style;
        requestRedraw();
        return this;
    }

    @Override
    protected void renderRow(Canvas canvas, int y) {
        int w = canvas.width();
        if (values.length == 0) {
            return;
        }
        int n = Math.min(values.length, w);
        int from = values.length - n;

        double min = values[from];
        double max = values[from];
        for (int i = from; i < values.length; i++) {
            min = Math.min(min, values[i]);
            max = Math.max(max, values[i]);
        }
        double range = max - min;

        for (int i = 0; i < n; i++) {
            double norm = range <= 0 ? 0.5 : (values[from + i] - min) / range;
            int level = (int) Math.round(norm * (Blocks.LOWER.length - 1));
            canvas.put(w - n + i, y, Blocks.LOWER[level], style);
        }
    }
}
