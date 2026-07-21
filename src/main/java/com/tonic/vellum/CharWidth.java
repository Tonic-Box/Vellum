package com.tonic.vellum;

/**
 * Terminal display width of characters and code points: 0 for zero-width, 2 for East
 * Asian wide characters and common emoji, 1 otherwise. Best-effort per code point; not
 * grapheme-cluster aware.
 */
public final class CharWidth
{
    private CharWidth()
    {
    }

    /**
     * Returns the display width of a BMP character.
     *
     * @param c the character to measure
     * @return the display width (0, 1, or 2)
     */
    public static int of(char c)
    {
        return of((int) c);
    }

    /**
     * Returns the total display width of a sequence (sum of its code points' widths).
     *
     * @param text the sequence to measure
     * @return the total display width
     */
    public static int width(CharSequence text)
    {
        int total = 0;
        int i = 0;
        int n = text.length();
        while (i < n)
        {
            int cp = Character.codePointAt(text, i);
            i += Character.charCount(cp);
            total += of(cp);
        }
        return total;
    }

    /**
     * Returns the display width of a Unicode code point.
     *
     * @param cp the code point to measure
     * @return the display width (0, 1, or 2)
     */
    public static int of(int cp)
    {
        if (cp == 0)
        {
            return 0;
        }
        if (cp <= 0xFFFF)
        {
            return ofBmp((char) cp);
        }
        return ofAstral(cp);
    }

    private static int ofBmp(char c)
    {
        if (c == 0x200B || c == 0x200C || c == 0x200D || c == 0xFEFF)
        {
            return 0;
        }
        int type = Character.getType(c);
        if (type == Character.NON_SPACING_MARK || type == Character.ENCLOSING_MARK)
        {
            return 0;
        }
        return isWideBmp(c) ? 2 : 1;
    }

    private static int ofAstral(int cp)
    {
        int type = Character.getType(cp);
        if (type == Character.NON_SPACING_MARK || type == Character.ENCLOSING_MARK)
        {
            return 0;
        }
        if ((cp >= 0x1F300 && cp <= 0x1FAFF)
                || (cp >= 0x20000 && cp <= 0x3FFFD))
        {
            return 2;
        }
        return 1;
    }

    private static boolean isWideBmp(char c)
    {
        return (c >= 0x1100 && c <= 0x115F)
                || (c >= 0x2E80 && c <= 0x303E)
                || (c >= 0x3041 && c <= 0x33FF)
                || (c >= 0x3400 && c <= 0x4DBF)
                || (c >= 0x4E00 && c <= 0x9FFF)
                || (c >= 0xA000 && c <= 0xA4CF)
                || (c >= 0xAC00 && c <= 0xD7A3)
                || (c >= 0xF900 && c <= 0xFAFF)
                || (c >= 0xFE10 && c <= 0xFE19)
                || (c >= 0xFE30 && c <= 0xFE6F)
                || (c >= 0xFF00 && c <= 0xFF60)
                || (c >= 0xFFE0 && c <= 0xFFE6);
    }
}
