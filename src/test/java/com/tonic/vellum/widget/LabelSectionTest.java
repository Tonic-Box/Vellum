package com.tonic.vellum.widget;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LabelSectionTest {

    private static final char CJK = 0x4E00; // wide

    @Test
    void centerAlignmentUsesDisplayWidth() {
        // two wide glyphs = 4 display columns; centered in 10 -> starts at (10-4)/2 = 3
        LabelSection label = new LabelSection(new String(new char[]{CJK, CJK}))
                .alignment(Alignment.CENTER);
        RecordingCanvas c = new RecordingCanvas(10, 1);
        label.render(c);

        assertEquals(CJK, c.charAt(3, 0));
        assertEquals(' ', c.charAt(2, 0));
    }

    @Test
    void rightAlignmentUsesDisplayWidth() {
        LabelSection label = new LabelSection(new String(new char[]{CJK, CJK}))
                .alignment(Alignment.RIGHT);
        RecordingCanvas c = new RecordingCanvas(10, 1);
        label.render(c);

        // 4 columns of content, right-aligned in 10 -> starts at column 6
        assertEquals(CJK, c.charAt(6, 0));
        assertEquals(' ', c.charAt(5, 0));
    }
}
