package com.tonic.vellum.widget;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpinnerSparklineTest {

    @Test
    void spinnerCyclesFrames() {
        Spinner spinner = new Spinner();
        RecordingCanvas c = new RecordingCanvas(5, 1);

        spinner.render(c);
        assertEquals('|', c.charAt(0, 0));
        spinner.tick();
        spinner.render(c);
        assertEquals('/', c.charAt(0, 0));
        spinner.tick();
        spinner.tick();
        spinner.tick(); // wraps back to the first frame
        spinner.render(c);
        assertEquals('|', c.charAt(0, 0));
    }

    @Test
    void spinnerShowsLabel() {
        Spinner spinner = new Spinner().label("Loading");
        RecordingCanvas c = new RecordingCanvas(12, 1);
        spinner.render(c);
        assertEquals('|', c.charAt(0, 0));
        assertEquals('L', c.charAt(2, 0));
    }

    @Test
    void sparklineMapsValuesToBlockLevels() {
        Sparkline sparkline = new Sparkline().setValues(new double[]{0, 1, 2, 3, 4, 5, 6, 7});
        RecordingCanvas c = new RecordingCanvas(8, 1);
        sparkline.render(c);

        assertEquals(Blocks.LOWER[0], c.charAt(0, 0)); // minimum
        assertEquals(Blocks.LOWER[7], c.charAt(7, 0)); // maximum
    }

    @Test
    void sparklineRightAlignsAndScrollsOldestOff() {
        Sparkline sparkline = new Sparkline().setValues(new double[]{0, 0, 0, 9});
        RecordingCanvas c = new RecordingCanvas(2, 1); // only 2 columns -> last two values
        sparkline.render(c);

        // last two values are {0, 9}; min 0 max 9 -> level 0 then level 7
        assertEquals(Blocks.LOWER[0], c.charAt(0, 0));
        assertEquals(Blocks.LOWER[7], c.charAt(1, 0));
    }
}
