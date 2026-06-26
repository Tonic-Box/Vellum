package com.tonic.vellum;

import com.tonic.vellum.geom.Rect;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WideCharTest {

    private static final char CJK = 0x4E00;        // CJK ideograph (wide)
    private static final char FULLWIDTH = 0xFF21;  // fullwidth 'A' (wide)
    private static final char COMBINING = 0x0301;  // combining acute (zero-width)
    private static final int CJK_EXT_B = 0x20000;  // astral CJK ideograph (wide)
    private static final int EMOJI = 0x1F600;      // astral emoji (wide)

    @Test
    void charWidthClassification() {
        assertEquals(1, CharWidth.of('A'));
        assertEquals(2, CharWidth.of(CJK));
        assertEquals(2, CharWidth.of(FULLWIDTH));
        assertEquals(0, CharWidth.of(COMBINING));
        assertEquals(0, CharWidth.of(Buffer.WIDE_CONTINUATION));
        assertEquals(2, CharWidth.of(CJK_EXT_B));
        assertEquals(2, CharWidth.of(EMOJI));
        assertEquals(1, CharWidth.of(0x1D400)); // mathematical bold A (astral, narrow)
    }

    @Test
    void stringDisplayWidth() {
        assertEquals(0, CharWidth.width(""));
        assertEquals(3, CharWidth.width("a" + CJK));      // 1 + 2
        assertEquals(4, CharWidth.width("" + CJK + FULLWIDTH));
        assertEquals(2, CharWidth.width(new StringBuilder().appendCodePoint(EMOJI).toString()));
    }

    @Test
    void wideCharOccupiesTwoCells() {
        Buffer buf = new Buffer(6, 1);
        Canvas c = new ClippedCanvas(buf, new Rect(0, 0, 6, 1));
        c.put(0, 0, CJK);
        assertEquals(CJK, buf.codePointAt(0, 0));
        assertEquals(Buffer.WIDE_CONTINUATION, buf.codePointAt(1, 0));
    }

    @Test
    void astralCodePointOccupiesTwoCellsAndRoundTrips() {
        Buffer buf = new Buffer(6, 1);
        Canvas c = new ClippedCanvas(buf, new Rect(0, 0, 6, 1));
        c.putCodePoint(0, 0, EMOJI);
        assertEquals(EMOJI, buf.codePointAt(0, 0));
        assertEquals(Buffer.WIDE_CONTINUATION, buf.codePointAt(1, 0));

        // also via put(String) with a surrogate pair
        Buffer buf2 = new Buffer(6, 1);
        Canvas c2 = new ClippedCanvas(buf2, new Rect(0, 0, 6, 1));
        c2.put(0, 0, new StringBuilder().appendCodePoint(CJK_EXT_B).append('A').toString());
        assertEquals(CJK_EXT_B, buf2.codePointAt(0, 0));
        assertEquals(Buffer.WIDE_CONTINUATION, buf2.codePointAt(1, 0));
        assertEquals('A', buf2.codePointAt(2, 0));
    }

    @Test
    void wideCharAtRightEdgeRendersSpace() {
        Buffer buf = new Buffer(6, 1);
        Canvas c = new ClippedCanvas(buf, new Rect(0, 0, 6, 1));
        c.put(5, 0, CJK);
        assertEquals(' ', buf.codePointAt(5, 0));
    }

    @Test
    void putStringTruncatesWithoutSplittingWideGlyph() {
        Buffer buf = new Buffer(3, 1);
        Canvas c = new ClippedCanvas(buf, new Rect(0, 0, 3, 1));
        c.put(0, 0, new String(new char[]{CJK, CJK}));
        assertEquals(CJK, buf.codePointAt(0, 0));
        assertEquals(Buffer.WIDE_CONTINUATION, buf.codePointAt(1, 0));
        assertEquals(' ', buf.codePointAt(2, 0));
    }

    @Test
    void combiningMarkIsDropped() {
        Buffer buf = new Buffer(4, 1);
        Canvas c = new ClippedCanvas(buf, new Rect(0, 0, 4, 1));
        c.put(0, 0, new String(new char[]{'e', COMBINING, 'x'}));
        assertEquals('e', buf.codePointAt(0, 0));
        assertEquals('x', buf.codePointAt(1, 0));
    }

    @Test
    void rendererEmitsAstralGlyphAsSurrogatePairOnceAndSkipsContinuation() {
        Buffer back = new Buffer(6, 1);
        Renderer r = new Renderer(6, 1);
        r.flush(back);

        Canvas c = new ClippedCanvas(back, new Rect(0, 0, 6, 1));
        c.putCodePoint(0, 0, EMOJI);
        c.put(2, 0, 'A');
        String out = r.flush(back);

        String emojiStr = new StringBuilder().appendCodePoint(EMOJI).toString();
        assertEquals(1, occurrences(out, emojiStr));
        assertEquals(1, count(out, 'A'));
        assertEquals(0, count(out, (char) 0), "continuation cell must not be emitted");
        assertEquals("", r.flush(back));
    }

    @Test
    void rendererUnwidensWhenWideReplacedByNarrow() {
        Buffer back = new Buffer(4, 1);
        Renderer r = new Renderer(4, 1);
        Canvas c = new ClippedCanvas(back, new Rect(0, 0, 4, 1));
        c.put(0, 0, CJK);
        r.flush(back);

        c.clear();
        c.put(0, 0, 'a');
        String out = r.flush(back);
        assertEquals(1, count(out, 'a'));
        assertEquals('a', back.codePointAt(0, 0));
        assertEquals(' ', back.codePointAt(1, 0));
    }

    private static int count(String s, char ch) {
        int n = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == ch) n++;
        }
        return n;
    }

    private static int occurrences(String haystack, String needle) {
        int n = 0;
        int i = 0;
        while ((i = haystack.indexOf(needle, i)) >= 0) {
            n++;
            i += needle.length();
        }
        return n;
    }
}
