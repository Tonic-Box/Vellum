package com.tonic.vellum.terminal;

import com.tonic.vellum.input.Key;
import com.tonic.vellum.input.KeyEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InputDecoderTest {

    /** Scripted source: yields the given ints, then TIMEOUT once exhausted. */
    private static InputDecoder.IntSource source(int... bytes) {
        return new InputDecoder.IntSource() {
            private int i = 0;

            @Override
            public int read(long timeoutMillis) {
                return i < bytes.length ? bytes[i++] : TIMEOUT;
            }
        };
    }

    private static KeyEvent decode(int... bytes) {
        return InputDecoder.readKey(source(bytes), 1000);
    }

    @Test
    void printableCharacter() {
        KeyEvent k = decode('a');
        assertEquals(Key.CHAR, k.code());
        assertEquals('a', k.ch());
        assertFalse(k.ctrl());
    }

    @Test
    void plainControlKeys() {
        assertEquals(Key.TAB, decode(9).code());
        assertEquals(Key.ENTER, decode(13).code());
        assertEquals(Key.ENTER, decode(10).code());
        assertEquals(Key.BACKSPACE, decode(127).code());
    }

    @Test
    void arrowKeys() {
        assertEquals(Key.UP, decode(27, '[', 'A').code());
        assertEquals(Key.DOWN, decode(27, '[', 'B').code());
        assertEquals(Key.RIGHT, decode(27, '[', 'C').code());
        assertEquals(Key.LEFT, decode(27, '[', 'D').code());
    }

    @Test
    void ss3ArrowKeys() {
        assertEquals(Key.UP, decode(27, 'O', 'A').code());
        assertEquals(Key.LEFT, decode(27, 'O', 'D').code());
    }

    @Test
    void navigationAndEditingKeys() {
        assertEquals(Key.HOME, decode(27, '[', 'H').code());
        assertEquals(Key.END, decode(27, '[', 'F').code());
        assertEquals(Key.DELETE, decode(27, '[', '3', '~').code());
        assertEquals(Key.PAGE_UP, decode(27, '[', '5', '~').code());
        assertEquals(Key.PAGE_DOWN, decode(27, '[', '6', '~').code());
        assertEquals(Key.SHIFT_TAB, decode(27, '[', 'Z').code());
    }

    @Test
    void modifiedArrowParsesCtrl() {
        KeyEvent k = decode(27, '[', '1', ';', '5', 'C');
        assertEquals(Key.RIGHT, k.code());
        assertTrue(k.ctrl());
        assertFalse(k.shift());
    }

    @Test
    void bareEscapeWhenNoSequenceFollows() {
        assertEquals(Key.ESCAPE, decode(27).code());
    }

    @Test
    void altCharacter() {
        KeyEvent k = decode(27, 'x');
        assertEquals(Key.CHAR, k.code());
        assertEquals('x', k.ch());
        assertTrue(k.alt());
    }

    @Test
    void ctrlLetter() {
        KeyEvent k = decode(1); // Ctrl-A
        assertEquals(Key.CHAR, k.code());
        assertEquals('a', k.ch());
        assertTrue(k.ctrl());
    }

    @Test
    void nulByteDecodesToUnknown() {
        assertEquals(Key.UNKNOWN, decode(0).code()); // NUL has no Ctrl-letter mapping
    }

    @Test
    void timeoutReturnsNull() {
        assertNull(InputDecoder.readKey(source(), 5));
    }
}
