package com.tonic.vellum.style;

import com.tonic.vellum.Maths;

/**
 * A terminal color: the terminal default, one of the 16 named ANSI colors, a 256-color
 * palette index, or 24-bit RGB. Immutable value type; instances compare by value.
 */
public final class Color {

    private enum Kind { DEFAULT, NAMED, INDEXED, RGB }

    private final Kind kind;
    private final int a; // NAMED: ansi 0-15; INDEXED: 0-255; RGB: red
    private final int b; // RGB: green
    private final int c; // RGB: blue

    private Color(Kind kind, int a, int b, int c) {
        this.kind = kind;
        this.a = a;
        this.b = b;
        this.c = c;
    }

    private static Color named(int index) {
        return new Color(Kind.NAMED, index, 0, 0);
    }

    /** The terminal's default color. */
    public static final Color DEFAULT = new Color(Kind.DEFAULT, 0, 0, 0);

    public static final Color BLACK = named(0);
    public static final Color RED = named(1);
    public static final Color GREEN = named(2);
    public static final Color YELLOW = named(3);
    public static final Color BLUE = named(4);
    public static final Color MAGENTA = named(5);
    public static final Color CYAN = named(6);
    public static final Color WHITE = named(7);
    public static final Color BRIGHT_BLACK = named(8);
    public static final Color BRIGHT_RED = named(9);
    public static final Color BRIGHT_GREEN = named(10);
    public static final Color BRIGHT_YELLOW = named(11);
    public static final Color BRIGHT_BLUE = named(12);
    public static final Color BRIGHT_MAGENTA = named(13);
    public static final Color BRIGHT_CYAN = named(14);
    public static final Color BRIGHT_WHITE = named(15);

    /** A 256-color palette index (0-255). */
    public static Color ansi256(int index) {
        return new Color(Kind.INDEXED, clamp(index, 255), 0, 0);
    }

    /** A 24-bit truecolor (each channel 0-255). */
    public static Color rgb(int red, int green, int blue) {
        return new Color(Kind.RGB, clamp(red, 255), clamp(green, 255), clamp(blue, 255));
    }

    /** SGR parameter(s) for using this color as foreground (without the leading separator). */
    public String foregroundSgr() {
        return sgr(30, 90, 38, 39);
    }

    /** SGR parameter(s) for using this color as background (without the leading separator). */
    public String backgroundSgr() {
        return sgr(40, 100, 48, 49);
    }

    /**
     * Build the SGR parameters for this color given the role's base codes: the standard and
     * bright bases for the 16 named colors, the {@code 38}/{@code 48} selector for
     * indexed/RGB, and the reset code for the terminal default.
     */
    private String sgr(int base, int brightBase, int extended, int dflt) {
        switch (kind) {
            case NAMED:   return Integer.toString(a < 8 ? base + a : brightBase + (a - 8));
            case INDEXED: return extended + ";5;" + a;
            case RGB:     return extended + ";2;" + a + ";" + b + ";" + c;
            default:      return Integer.toString(dflt);
        }
    }

    private static int clamp(int value, int max) {
        return Maths.clamp(value, 0, max);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Color)) return false;
        Color other = (Color) o;
        return kind == other.kind && a == other.a && b == other.b && c == other.c;
    }

    @Override
    public int hashCode() {
        int h = kind.hashCode();
        h = 31 * h + a;
        h = 31 * h + b;
        h = 31 * h + c;
        return h;
    }

    @Override
    public String toString() {
        switch (kind) {
            case NAMED:   return "Color[ansi " + a + "]";
            case INDEXED: return "Color[ansi256 " + a + "]";
            case RGB:     return "Color[rgb " + a + "," + b + "," + c + "]";
            default:      return "Color[default]";
        }
    }
}
