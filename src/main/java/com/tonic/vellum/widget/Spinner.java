package com.tonic.vellum.widget;

import com.tonic.vellum.App;
import com.tonic.vellum.Canvas;
import com.tonic.vellum.Cancellable;
import com.tonic.vellum.style.Style;

import java.time.Duration;

/**
 * An indeterminate progress indicator that cycles frames; advance with tick() or animate on a timer with start(App).
 */
public final class Spinner extends SingleRowSection
{
    private static final char[] FRAMES = {'|', '/', '-', '\\'};
    private static final Duration INTERVAL = Duration.ofMillis(120);

    private String label = "";
    private Style style = Style.NORMAL;
    private int frame;

    /**
     * Sets the label shown next to the spinner.
     *
     * @param label the label text; treated as empty if null
     * @return this Spinner for chaining
     */
    public Spinner label(String label)
    {
        this.label = label == null ? "" : label;
        requestRedraw();
        return this;
    }

    /**
     * Sets the style used to draw the spinner and label.
     *
     * @param style the style
     * @return this Spinner for chaining
     */
    public Spinner style(Style style)
    {
        this.style = style;
        requestRedraw();
        return this;
    }

    /**
     * Advances to the next animation frame.
     */
    public void tick()
    {
        frame = (frame + 1) % FRAMES.length;
        requestRedraw();
    }

    /**
     * Animates the spinner on the app timer until the returned handle is cancelled.
     *
     * @param app the application providing the timer
     * @return a handle that stops the animation when cancelled
     */
    public Cancellable start(App app)
    {
        return app.scheduleAtFixedRate(INTERVAL, INTERVAL, this::tick);
    }

    @Override
    protected void renderRow(Canvas canvas, int y)
    {
        String text = label.isEmpty() ? String.valueOf(FRAMES[frame]) : FRAMES[frame] + " " + label;
        canvas.put(0, y, text, style);
    }
}
