package com.tonic.vellum;

import com.tonic.vellum.geom.Rect;
import com.tonic.vellum.widget.LogSection;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScrollWrapTest {

    private static String row(Buffer buf, int y) {
        StringBuilder sb = new StringBuilder();
        for (int x = 0; x < buf.width(); x++) {
            sb.append((char) buf.codePointAt(x, y));
        }
        return sb.toString();
    }

    @Test
    void wrappedLogScrollsOverDisplayLinesAndFollowsTail() {
        LogSection log = new LogSection();
        log.wrap(true); // follows tail by default
        Section s = log;
        s.resizeRoot(new Rect(0, 0, 5, 2)); // width 5, height 2

        // wraps to ["aaa", "bbb", "ccc"] -> 3 display lines, viewport 2
        log.append("aaa bbb ccc");

        Buffer buf = new Buffer(5, 2);
        s.renderInto(buf);

        // follow-tail pins the bottom: the last two display lines are shown
        assertEquals("bbb", row(buf, 0).trim());
        assertEquals("ccc", row(buf, 1).trim());
    }

    @Test
    void unwrappedScrollSectionUnchanged() {
        LogSection log = new LogSection();
        Section s = log;
        s.resizeRoot(new Rect(0, 0, 20, 2));
        log.append("first");
        log.append("second");

        Buffer buf = new Buffer(20, 2);
        s.renderInto(buf);
        assertEquals("first", row(buf, 0).trim());
        assertEquals("second", row(buf, 1).trim());
    }
}
