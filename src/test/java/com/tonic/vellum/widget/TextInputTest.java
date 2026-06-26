package com.tonic.vellum.widget;

import com.tonic.vellum.geom.Point;
import com.tonic.vellum.input.Key;
import com.tonic.vellum.input.KeyEvent;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class TextInputTest {

    private static void type(TextInput in, String s) {
        for (char c : s.toCharArray()) {
            in.onKey(KeyEvent.character(c));
        }
    }

    private static String rendered(TextInput in, int width) {
        RecordingCanvas c = new RecordingCanvas(width, 1);
        in.render(c);
        StringBuilder sb = new StringBuilder();
        for (int x = 0; x < width; x++) {
            sb.append(c.charAt(x, 0));
        }
        return sb.toString().replace('\0', ' ');
    }

    @Test
    void typingInsertsAtCaret() {
        TextInput in = new TextInput();
        type(in, "hi");
        assertEquals("hi", in.text());

        in.onKey(KeyEvent.special(Key.LEFT));
        type(in, "X");
        assertEquals("hXi", in.text());
    }

    @Test
    void backspaceAndDelete() {
        TextInput in = new TextInput("abc");      // caret at end
        in.onKey(KeyEvent.special(Key.BACKSPACE));
        assertEquals("ab", in.text());

        in.onKey(KeyEvent.special(Key.HOME));
        in.onKey(KeyEvent.special(Key.DELETE));
        assertEquals("b", in.text());
    }

    @Test
    void caretMovementClamps() {
        TextInput in = new TextInput("ab");
        in.onKey(KeyEvent.special(Key.END));
        in.onKey(KeyEvent.special(Key.RIGHT)); // already at end
        type(in, "!");
        assertEquals("ab!", in.text());

        in.onKey(KeyEvent.special(Key.HOME));
        in.onKey(KeyEvent.special(Key.LEFT)); // already at start
        type(in, "*");
        assertEquals("*ab!", in.text());
    }

    @Test
    void onChangeAndOnSubmitFire() {
        AtomicInteger changes = new AtomicInteger();
        AtomicReference<String> submitted = new AtomicReference<>();
        TextInput in = new TextInput()
                .onChange(s -> changes.incrementAndGet())
                .onSubmit(submitted::set);
        type(in, "go");
        assertEquals(2, changes.get());
        in.onKey(KeyEvent.special(Key.ENTER));
        assertEquals("go", submitted.get());
    }

    @Test
    void controlCharactersAreNotInserted() {
        TextInput in = new TextInput();
        in.onKey(KeyEvent.character('c', true, false, false)); // Ctrl-C
        assertEquals("", in.text());
    }

    @Test
    void rendersTextAndScrollsWithCaret() {
        TextInput in = new TextInput("hello world");
        assertEquals("ld ", rendered(in, 3)); // caret at end sits in the trailing empty column
        Point cur = in.cursor();
        assertEquals(2, cur.x()); // caret sits at the last visible column
        assertEquals(0, cur.y());
    }

    @Test
    void placeholderShownWhenEmptyAndUnfocused() {
        TextInput in = new TextInput().placeholder("search");
        assertEquals("search    ", rendered(in, 10));
    }

    @Test
    void caretMovesByDisplayColumnOverWideGlyph() {
        char cjk = 0x4E00;
        TextInput in = new TextInput("a" + cjk + "b");
        renderAt(in, 10);
        in.onKey(KeyEvent.special(Key.HOME));
        renderAt(in, 10);
        assertEquals(0, in.cursor().x());

        in.onKey(KeyEvent.special(Key.RIGHT)); // past 'a'
        renderAt(in, 10);
        assertEquals(1, in.cursor().x());

        in.onKey(KeyEvent.special(Key.RIGHT)); // past the wide glyph
        renderAt(in, 10);
        assertEquals(3, in.cursor().x()); // advanced by two columns
    }

    @Test
    void backspaceDeletesWholeCodePoint() {
        String astral = new StringBuilder().appendCodePoint(0x1F600).append('x').toString();
        TextInput in = new TextInput(astral); // caret at end
        in.onKey(KeyEvent.special(Key.BACKSPACE)); // removes 'x'
        assertEquals(new StringBuilder().appendCodePoint(0x1F600).toString(), in.text());
        in.onKey(KeyEvent.special(Key.BACKSPACE)); // removes the astral code point (both halves)
        assertEquals("", in.text());
    }

    @Test
    void scrollKeepsCaretVisibleWithWideChars() {
        char cjk = 0x4E00;
        TextInput in = new TextInput("aaaa" + cjk); // display width 6, caret at end
        renderAt(in, 3);
        assertEquals(2, in.cursor().x()); // caret pinned to the last visible column
    }

    private static void renderAt(TextInput in, int width) {
        in.render(new RecordingCanvas(width, 1));
    }
}
