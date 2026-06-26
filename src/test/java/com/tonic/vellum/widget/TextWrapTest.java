package com.tonic.vellum.widget;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TextWrapTest {

    private static final char CJK = 0x4E00; // wide

    @Test
    void wrapsAtWordBoundaries() {
        assertEquals(Arrays.asList("the quick", "brown fox"),
                TextWrap.wrap("the quick brown fox", 10));
    }

    @Test
    void hardBreaksLongWords() {
        assertEquals(Arrays.asList("abc", "def", "gh"), TextWrap.wrap("abcdefgh", 3));
    }

    @Test
    void wideGlyphsNeverStraddleBoundary() {
        String word = new String(new char[]{CJK, CJK, CJK}); // each width 2
        List<String> out = TextWrap.wrap(word, 3);
        // width 3 holds only one width-2 glyph per line
        assertEquals(3, out.size());
        for (String line : out) {
            assertEquals(2, com.tonic.vellum.CharWidth.width(line));
        }
    }

    @Test
    void emptyAndDegenerateInputs() {
        assertEquals(Arrays.asList(""), TextWrap.wrap("", 5));
        assertEquals(Arrays.asList("anything"), TextWrap.wrap("anything", 0));
    }

    @Test
    void preservesInternalSpacingWhenItFits() {
        assertEquals(Arrays.asList("a  b"), TextWrap.wrap("a  b", 10));
    }

    @Test
    void textSectionWrapsToCanvasWidth() {
        TextSection text = new TextSection("the quick brown fox").wrap(true);
        RecordingCanvas c = new RecordingCanvas(10, 4);
        text.render(c);
        assertEquals("the quick ", row(c, 0));
        assertEquals("brown fox ", row(c, 1));
    }

    private static String row(RecordingCanvas c, int y) {
        StringBuilder sb = new StringBuilder();
        for (int x = 0; x < c.width(); x++) {
            sb.append(c.charAt(x, y));
        }
        return sb.toString().replace('\0', ' ');
    }
}
