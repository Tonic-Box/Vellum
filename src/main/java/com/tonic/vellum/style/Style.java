package com.tonic.vellum.style;

import java.util.Objects;

/**
 * Immutable text styling value. Setters return new instances; instances are safely
 * shared across cells and compared by value (the renderer diffs styles by equality).
 */
public final class Style {

    /** Default foreground/background, no attributes. */
    public static final Style NORMAL = new Style(Color.DEFAULT, Color.DEFAULT, false, false, false, false);
    /** Inverted foreground/background. */
    public static final Style REVERSE = NORMAL.reverse(true);
    /** Dimmed inverse, used for "parked" (focus-lost) selections. */
    public static final Style DIM_REVERSE = REVERSE.dim(true);
    /** Bold attribute. */
    public static final Style BOLD = NORMAL.bold(true);
    /** Dim attribute. */
    public static final Style DIM = NORMAL.dim(true);
    /** Underline attribute. */
    public static final Style UNDERLINE = NORMAL.underline(true);

    private final Color fg;
    private final Color bg;
    private final boolean bold;
    private final boolean reverse;
    private final boolean dim;
    private final boolean underline;

    private Style(Color fg, Color bg, boolean bold, boolean reverse, boolean dim, boolean underline) {
        this.fg = fg;
        this.bg = bg;
        this.bold = bold;
        this.reverse = reverse;
        this.dim = dim;
        this.underline = underline;
    }

    public Color foreground() { return fg; }
    public Color background() { return bg; }
    public boolean isBold() { return bold; }
    public boolean isReverse() { return reverse; }
    public boolean isDim() { return dim; }
    public boolean isUnderline() { return underline; }

    public Style fg(Color c) { return new Style(c, bg, bold, reverse, dim, underline); }
    public Style bg(Color c) { return new Style(fg, c, bold, reverse, dim, underline); }
    public Style bold(boolean b) { return new Style(fg, bg, b, reverse, dim, underline); }
    public Style reverse(boolean b) { return new Style(fg, bg, bold, b, dim, underline); }
    public Style dim(boolean b) { return new Style(fg, bg, bold, reverse, b, underline); }
    public Style underline(boolean b) { return new Style(fg, bg, bold, reverse, dim, b); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Style)) return false;
        Style s = (Style) o;
        return bold == s.bold && reverse == s.reverse && dim == s.dim && underline == s.underline
                && fg.equals(s.fg) && bg.equals(s.bg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fg, bg, bold, reverse, dim, underline);
    }

    @Override
    public String toString() {
        return "Style[fg=" + fg + ", bg=" + bg
                + (bold ? ", bold" : "") + (reverse ? ", reverse" : "")
                + (dim ? ", dim" : "") + (underline ? ", underline" : "") + "]";
    }
}
