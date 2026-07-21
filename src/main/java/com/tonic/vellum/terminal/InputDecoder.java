package com.tonic.vellum.terminal;

import com.tonic.vellum.input.Key;
import com.tonic.vellum.input.KeyEvent;

final class InputDecoder
{
    interface IntSource
    {
        int TIMEOUT = -2;
        int EOF = -1;

        int read(long timeoutMillis);
    }

    private static final long SEQ_TIMEOUT_MS = 30;

    private InputDecoder()
    {
    }

    static KeyEvent readKey(IntSource source, long timeoutMillis)
    {
        int c = source.read(timeoutMillis);
        if (c == IntSource.TIMEOUT || c == IntSource.EOF)
        {
            return null;
        }
        return decode(c, source);
    }

    private static KeyEvent decode(int c, IntSource source)
    {
        switch (c)
        {
            case 9:  return KeyEvent.special(Key.TAB);
            case 13:
            case 10: return KeyEvent.special(Key.ENTER);
            case 127:
            case 8:  return KeyEvent.special(Key.BACKSPACE);
            case 27: return decodeEscape(source);
            default:
                break;
        }
        if (c >= 1 && c < 32)
        {
            char letter = (char) ('a' + (c - 1));
            return KeyEvent.character(letter, true, false, false);
        }
        if (c == 0)
        {
            return KeyEvent.special(Key.UNKNOWN);
        }
        return KeyEvent.character((char) c);
    }

    private static KeyEvent decodeEscape(IntSource source)
    {
        int next = source.read(SEQ_TIMEOUT_MS);
        if (next == IntSource.TIMEOUT || next == IntSource.EOF)
        {
            return KeyEvent.special(Key.ESCAPE);
        }
        if (next == '[' || next == 'O')
        {
            return decodeCsi(source);
        }
        if (next >= 32)
        {
            return KeyEvent.character((char) next, false, true, false);
        }
        return KeyEvent.special(Key.ESCAPE);
    }

    private static KeyEvent decodeCsi(IntSource source)
    {
        StringBuilder params = new StringBuilder();
        int b;
        while (true)
        {
            b = source.read(SEQ_TIMEOUT_MS);
            if (b == IntSource.TIMEOUT || b == IntSource.EOF)
            {
                return KeyEvent.special(Key.UNKNOWN);
            }
            if ((b >= '0' && b <= '9') || b == ';')
            {
                params.append((char) b);
            }
            else
            {
                break;
            }
        }
        int modifier = parseModifier(params.toString());
        boolean shift = modifier == 2 || modifier == 4 || modifier == 6 || modifier == 8;
        boolean alt = modifier == 3 || modifier == 4 || modifier == 7 || modifier == 8;
        boolean ctrl = modifier == 5 || modifier == 6 || modifier == 7 || modifier == 8;

        Key key = letterKey((char) b, params.toString());
        if (key == null)
        {
            return KeyEvent.special(Key.UNKNOWN);
        }
        return KeyEvent.special(key, ctrl, alt, shift);
    }

    private static Key letterKey(char fin, String params)
    {
        switch (fin)
        {
            case 'A': return Key.UP;
            case 'B': return Key.DOWN;
            case 'C': return Key.RIGHT;
            case 'D': return Key.LEFT;
            case 'H': return Key.HOME;
            case 'F': return Key.END;
            case 'Z': return Key.SHIFT_TAB;
            case '~': return tildeKey(params);
            default:  return null;
        }
    }

    private static Key tildeKey(String params)
    {
        int first = leadingNumber(params);
        switch (first)
        {
            case 1:
            case 7: return Key.HOME;
            case 3: return Key.DELETE;
            case 4:
            case 8: return Key.END;
            case 5: return Key.PAGE_UP;
            case 6: return Key.PAGE_DOWN;
            default: return Key.UNKNOWN;
        }
    }

    private static int parseModifier(String params)
    {
        int semi = params.indexOf(';');
        return semi < 0 ? 1 : parseLeadingInt(params, semi + 1, 1);
    }

    private static int leadingNumber(String params)
    {
        return parseLeadingInt(params, 0, -1);
    }

    private static int parseLeadingInt(String s, int from, int fallback)
    {
        int end = from;
        while (end < s.length() && Character.isDigit(s.charAt(end)))
        {
            end++;
        }
        return end == from ? fallback : Integer.parseInt(s.substring(from, end));
    }
}
