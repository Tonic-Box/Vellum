package com.tonic.vellum;

import com.tonic.vellum.geom.Rect;
import com.tonic.vellum.style.Color;
import com.tonic.vellum.style.Style;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RenderingCoreTest {

    private static final String CSI = Renderer.CSI;

    @Test
    void canvasDiscardsOutOfBoundsWrites() {
        Buffer buf = new Buffer(5, 3);
        Canvas c = new ClippedCanvas(buf, new Rect(0, 0, 5, 3));
        c.put(-1, 0, 'X');
        c.put(5, 0, 'X');
        c.put(0, 3, 'X');
        c.put(2, 1, 'A');
        assertEquals('A', buf.codePointAt(2, 1));
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 5; x++) {
                if (!(x == 2 && y == 1)) assertEquals(' ', buf.codePointAt(x, y));
            }
        }
    }

    @Test
    void clipTranslatesAndRestrictsToSubRect() {
        Buffer buf = new Buffer(10, 10);
        Canvas root = new ClippedCanvas(buf, new Rect(0, 0, 10, 10));
        Canvas sub = root.clip(new Rect(3, 2, 4, 4));
        assertEquals(4, sub.width());
        assertEquals(4, sub.height());
        sub.put(0, 0, 'Q');
        assertEquals('Q', buf.codePointAt(3, 2));
        sub.put(4, 0, 'Z');
        assertEquals(' ', buf.codePointAt(7, 2));
    }

    @Test
    void nestedClipCannotEscapeParent() {
        Buffer buf = new Buffer(10, 10);
        Canvas root = new ClippedCanvas(buf, new Rect(0, 0, 10, 10));
        Canvas a = root.clip(new Rect(2, 2, 5, 5));
        Canvas b = a.clip(new Rect(0, 0, 100, 100));
        assertEquals(5, b.width());
        assertEquals(5, b.height());
        b.put(4, 4, '*');
        assertEquals('*', buf.codePointAt(6, 6));
        b.put(5, 5, '!');
        assertEquals(' ', buf.codePointAt(7, 7));
    }

    @Test
    void putStringTruncatesAtWidth() {
        Buffer buf = new Buffer(4, 1);
        Canvas c = new ClippedCanvas(buf, new Rect(0, 0, 4, 1));
        c.put(2, 0, "hello");
        assertEquals('h', buf.codePointAt(2, 0));
        assertEquals('e', buf.codePointAt(3, 0));
    }

    @Test
    void rendererFirstFlushPaintsEverythingThenIdleEmitsNothing() {
        Buffer back = new Buffer(3, 1);
        Renderer r = new Renderer(3, 1);
        String first = r.flush(back);
        assertFalse(first.isEmpty());
        assertEquals("", r.flush(back));
    }

    @Test
    void rendererEmitsOnlyChangedCellsAndBatchesRuns() {
        Buffer back = new Buffer(10, 2);
        Renderer r = new Renderer(10, 2);
        r.flush(back);

        Canvas c = new ClippedCanvas(back, new Rect(0, 0, 10, 2));
        c.put(3, 0, "AB");
        String out = r.flush(back);

        assertEquals(1, countOccurrences(out, CSI + "1;4H"));
        assertTrue(out.contains("AB"));
        assertEquals(1, countCursorMoves(out));
    }

    @Test
    void rendererEmitsStyleOnlyOnChange() {
        Buffer back = new Buffer(4, 1);
        Renderer r = new Renderer(4, 1);
        r.flush(back);

        Canvas c = new ClippedCanvas(back, new Rect(0, 0, 4, 1));
        Style red = Style.NORMAL.fg(Color.RED);
        c.put(0, 0, 'a', red);
        c.put(1, 0, 'b', red);
        c.put(2, 0, 'c', Style.NORMAL);
        String out = r.flush(back);

        assertEquals(1, countOccurrences(out, ";31"));
    }

    @Test
    void rendererEmitsTruecolorAnd256Sgr() {
        Buffer back = new Buffer(3, 1);
        Renderer r = new Renderer(3, 1);
        r.flush(back);

        Canvas c = new ClippedCanvas(back, new Rect(0, 0, 3, 1));
        c.put(0, 0, 'a', Style.NORMAL.fg(Color.rgb(255, 128, 0)));
        c.put(1, 0, 'b', Style.NORMAL.bg(Color.ansi256(200)));
        String out = r.flush(back);

        assertTrue(out.contains("38;2;255;128;0"), "truecolor foreground SGR");
        assertTrue(out.contains("48;5;200"), "256-color background SGR");
    }

    @Test
    void bufferReadersIgnoreOutOfRange() {
        Buffer b = new Buffer(2, 2);
        assertEquals(' ', b.codePointAt(-1, 0));
        assertEquals(' ', b.codePointAt(5, 0));
        assertEquals(' ', b.codePointAt(0, 9));
        assertEquals(Style.NORMAL, b.styleAt(-1, -1));
        assertEquals(Style.NORMAL, b.styleAt(10, 10));
    }

    /** Count CSI sequences whose final byte is 'H' (cursor moves), vs 'm' (style). */
    private static int countCursorMoves(String s) {
        int count = 0;
        int idx = 0;
        while ((idx = s.indexOf(CSI, idx)) >= 0) {
            int h = s.indexOf('H', idx);
            int m = s.indexOf('m', idx);
            if (h >= 0 && (m < 0 || h < m)) count++;
            idx += CSI.length();
        }
        return count;
    }

    private static int countOccurrences(String haystack, String needle) {
        int count = 0;
        int idx = 0;
        while ((idx = haystack.indexOf(needle, idx)) >= 0) {
            count++;
            idx += needle.length();
        }
        return count;
    }
}
