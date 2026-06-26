package com.tonic.vellum.widget;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProgressBarTest {

    @Test
    void fillsProportionalToValue() {
        ProgressBar bar = new ProgressBar().value(0.5).showPercent(false);
        RecordingCanvas c = new RecordingCanvas(10, 1);
        bar.render(c);

        for (int x = 0; x < 5; x++) {
            assertEquals(Blocks.FULL, c.charAt(x, 0));
        }
        assertEquals(' ', c.charAt(5, 0));
    }

    @Test
    void fractionalCellUsesAPartialBlock() {
        ProgressBar bar = new ProgressBar().value(0.55).showPercent(false); // 5.5 cells of 10
        RecordingCanvas c = new RecordingCanvas(10, 1);
        bar.render(c);

        assertEquals(Blocks.FULL, c.charAt(4, 0));
        assertEquals(Blocks.LEFT[3], c.charAt(5, 0)); // 4/8 partial
    }

    @Test
    void progressByCountAndPercentOverlay() {
        ProgressBar bar = new ProgressBar().progress(1, 4); // 25%
        assertEquals(0.25, bar.value(), 1e-9);

        RecordingCanvas c = new RecordingCanvas(10, 1);
        bar.render(c);
        // "25%" centered at x=3
        assertEquals('2', c.charAt(3, 0));
        assertEquals('5', c.charAt(4, 0));
        assertEquals('%', c.charAt(5, 0));
    }

    @Test
    void valueClampsToUnitRange() {
        assertEquals(1.0, new ProgressBar().value(5).value(), 1e-9);
        assertEquals(0.0, new ProgressBar().value(-2).value(), 1e-9);
    }
}
