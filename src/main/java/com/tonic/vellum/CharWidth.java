package com.tonic.vellum;

/**
 * Terminal display width of a character or code point: 0 for zero-width (combining marks
 * and explicit zero-width characters), 2 for East Asian wide and fullwidth characters and
 * common emoji, 1 otherwise. Best-effort, range-based East Asian Width; not a full
 * grapheme-cluster implementation (emoji ZWJ sequences are measured per code point).
 */
public final class CharWidth {

    private CharWidth() {}

    /**
     * Return the display width of a BMP character.
     *
     * @param c the character to measure
     * @return the display width (0, 1, or 2)
     */
    public static int of(char c) {
        return of((int) c);
    }

    /**
     * Return the total display width of a sequence (sum of its code points' widths).
     *
     * @param text the sequence to measure
     * @return the total display width
     */
    public static int width(CharSequence text) {
        int total = 0;
        int i = 0;
        int n = text.length();
        while (i < n) {
            int cp = Character.codePointAt(text, i);
            i += Character.charCount(cp);
            total += of(cp);
        }
        return total;
    }

    /**
     * Return the display width of a Unicode code point.
     *
     * @param cp the code point to measure
     * @return the display width (0, 1, or 2)
     */
    public static int of(int cp) {
        if (cp == 0) {
            return 0; // wide-continuation sentinel
        }
        if (cp <= 0xFFFF) {
            return ofBmp((char) cp);
        }
        return ofAstral(cp);
    }

    private static int ofBmp(char c) {
        if (c == 0x200B || c == 0x200C || c == 0x200D || c == 0xFEFF) {
            return 0; // zero-width space / non-joiner / joiner / BOM
        }
        int type = Character.getType(c);
        if (type == Character.NON_SPACING_MARK || type == Character.ENCLOSING_MARK) {
            return 0;
        }
        return isWideBmp(c) ? 2 : 1;
    }

    private static int ofAstral(int cp) {
        int type = Character.getType(cp);
        if (type == Character.NON_SPACING_MARK || type == Character.ENCLOSING_MARK) {
            return 0;
        }
        if ((cp >= 0x1F300 && cp <= 0x1FAFF)    // emoji and pictographs
                || (cp >= 0x20000 && cp <= 0x3FFFD)) { // CJK Extension B-G and compat supplement
            return 2;
        }
        return 1;
    }

    private static boolean isWideBmp(char c) {
        return (c >= 0x1100 && c <= 0x115F)   // Hangul Jamo
                || (c >= 0x2E80 && c <= 0x303E)   // CJK radicals, Kangxi, CJK symbols
                || (c >= 0x3041 && c <= 0x33FF)   // Hiragana, Katakana, CJK symbols/compat
                || (c >= 0x3400 && c <= 0x4DBF)   // CJK Extension A
                || (c >= 0x4E00 && c <= 0x9FFF)   // CJK Unified Ideographs
                || (c >= 0xA000 && c <= 0xA4CF)   // Yi
                || (c >= 0xAC00 && c <= 0xD7A3)   // Hangul Syllables
                || (c >= 0xF900 && c <= 0xFAFF)   // CJK Compatibility Ideographs
                || (c >= 0xFE10 && c <= 0xFE19)   // Vertical forms
                || (c >= 0xFE30 && c <= 0xFE6F)   // CJK Compatibility / small forms
                || (c >= 0xFF00 && c <= 0xFF60)   // Fullwidth forms
                || (c >= 0xFFE0 && c <= 0xFFE6);  // Fullwidth signs
    }
}
