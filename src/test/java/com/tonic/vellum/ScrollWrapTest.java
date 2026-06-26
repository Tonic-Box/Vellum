package com.tonic.vellum;

import com.tonic.vellum.geom.Rect;
import com.tonic.vellum.input.Key;
import com.tonic.vellum.input.KeyEvent;
import com.tonic.vellum.widget.LogSection;
import com.tonic.vellum.widget.ScrollSection;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScrollWrapTest {

    /** A follow-tail scroll whose protected append/key handling are reachable via subclassing. */
    private static final class KeyableScroll extends ScrollSection {
        KeyableScroll() {
            followTail(true);
        }

        void add(String line) {
            appendLine(line);
        }

        void key(Key code) {
            onKey(KeyEvent.special(code));
        }
    }

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
        ((Section) log).resizeRoot(new Rect(0, 0, 5, 2)); // width 5, height 2

        // wraps to ["aaa", "bbb", "ccc"] -> 3 display lines, viewport 2
        log.append("aaa bbb ccc");

        Buffer buf = new Buffer(5, 2);
        ((Section) log).renderInto(buf);

        // follow-tail pins the bottom: the last two display lines are shown
        assertEquals("bbb", row(buf, 0).trim());
        assertEquals("ccc", row(buf, 1).trim());
    }

    @Test
    void unwrappedScrollSectionUnchanged() {
        LogSection log = new LogSection();
        ((Section) log).resizeRoot(new Rect(0, 0, 20, 2));
        log.append("first");
        log.append("second");

        Buffer buf = new Buffer(20, 2);
        ((Section) log).renderInto(buf);
        assertEquals("first", row(buf, 0).trim());
        assertEquals("second", row(buf, 1).trim());
    }

    @Test
    void maxLinesDropsOldest() {
        LogSection log = new LogSection();
        log.maxLines(3);
        for (int i = 0; i < 5; i++) {
            log.append("line" + i);
        }
        assertEquals(3, log.lineCount());

        ((Section) log).resizeRoot(new Rect(0, 0, 10, 3));
        Buffer buf = new Buffer(10, 3);
        ((Section) log).renderInto(buf);
        // oldest dropped; follow-tail shows line2, line3, line4
        assertEquals("line2", row(buf, 0).trim());
        assertEquals("line4", row(buf, 2).trim());
    }

    @Test
    void logSectionIsBoundedByDefault() {
        LogSection log = new LogSection();
        for (int i = 0; i < LogSection.DEFAULT_MAX_LINES + 10; i++) {
            log.append("x");
        }
        assertEquals(LogSection.DEFAULT_MAX_LINES, log.lineCount());
    }

    @Test
    void scrollingToBottomReArmsFollowTail() {
        KeyableScroll log = new KeyableScroll();
        ((Section) log).resizeRoot(new Rect(0, 0, 10, 3)); // 3 visible rows
        for (int i = 0; i < 10; i++) {
            log.add("line" + i);
        }
        assertEquals(7, log.scrollTop()); // followed to the bottom (10 lines, 3 visible)

        log.key(Key.UP); // scrolling up stops following
        int afterUp = log.scrollTop();
        log.add("a");
        assertEquals(afterUp, log.scrollTop(), "append must not pin while not following");

        for (int i = 0; i < 5; i++) {
            log.key(Key.DOWN); // scroll back to the bottom; follow re-arms there
        }
        int atBottom = log.scrollTop();
        log.add("b");
        assertTrue(log.scrollTop() > atBottom, "append pins again after scrolling to the bottom");
    }

    @Test
    void followTailSetterPinsToBottom() {
        KeyableScroll log = new KeyableScroll();
        ((Section) log).resizeRoot(new Rect(0, 0, 10, 3));
        for (int i = 0; i < 10; i++) {
            log.add("line" + i);
        }
        assertEquals(7, log.scrollTop());

        log.key(Key.UP); // stop following and scroll up
        assertEquals(6, log.scrollTop());

        log.followTail(true); // re-enabling pins to the bottom immediately
        assertEquals(7, log.scrollTop());
    }
}
