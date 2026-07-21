package com.tonic.vellum.style;

import java.util.Objects;

/**
 * An immutable text styling value; setters return new instances and instances compare by value.
 */
public final class Style
{
    /**
     * Default foreground and background with no attributes.
     */
    public static final Style NORMAL = new Style(Color.DEFAULT, Color.DEFAULT, false, false, false, false);
    /**
     * Inverted foreground and background.
     */
    public static final Style REVERSE = NORMAL.reverse(true);
    /**
     * Dimmed inverse.
     */
    public static final Style DIM_REVERSE = REVERSE.dim(true);
    /**
     * Bold attribute.
     */
    public static final Style BOLD = NORMAL.bold(true);
    /**
     * Dim attribute.
     */
    public static final Style DIM = NORMAL.dim(true);
    /**
     * Underline attribute.
     */
    public static final Style UNDERLINE = NORMAL.underline(true);

    private final Color fg;
    private final Color bg;
    private final boolean bold;
    private final boolean reverse;
    private final boolean dim;
    private final boolean underline;

    private Style(Color fg, Color bg, boolean bold, boolean reverse, boolean dim, boolean underline)
    {
        this.fg = fg;
        this.bg = bg;
        this.bold = bold;
        this.reverse = reverse;
        this.dim = dim;
        this.underline = underline;
    }

    /**
     * @return the foreground color
     */
    public Color foreground()
    {
        return fg;
    }

    /**
     * @return the background color
     */
    public Color background()
    {
        return bg;
    }

    /**
     * @return true when the bold attribute is set
     */
    public boolean isBold()
    {
        return bold;
    }

    /**
     * @return true when the reverse attribute is set
     */
    public boolean isReverse()
    {
        return reverse;
    }

    /**
     * @return true when the dim attribute is set
     */
    public boolean isDim()
    {
        return dim;
    }

    /**
     * @return true when the underline attribute is set
     */
    public boolean isUnderline()
    {
        return underline;
    }

    /**
     * Sets the foreground color.
     *
     * @param c the foreground color
     * @return a new style with the foreground changed
     */
    public Style fg(Color c)
    {
        return new Style(c, bg, bold, reverse, dim, underline);
    }

    /**
     * Sets the background color.
     *
     * @param c the background color
     * @return a new style with the background changed
     */
    public Style bg(Color c)
    {
        return new Style(fg, c, bold, reverse, dim, underline);
    }

    /**
     * Sets the bold attribute.
     *
     * @param b whether bold is enabled
     * @return a new style with the bold attribute changed
     */
    public Style bold(boolean b)
    {
        return new Style(fg, bg, b, reverse, dim, underline);
    }

    /**
     * Sets the reverse attribute.
     *
     * @param b whether reverse is enabled
     * @return a new style with the reverse attribute changed
     */
    public Style reverse(boolean b)
    {
        return new Style(fg, bg, bold, b, dim, underline);
    }

    /**
     * Sets the dim attribute.
     *
     * @param b whether dim is enabled
     * @return a new style with the dim attribute changed
     */
    public Style dim(boolean b)
    {
        return new Style(fg, bg, bold, reverse, b, underline);
    }

    /**
     * Sets the underline attribute.
     *
     * @param b whether underline is enabled
     * @return a new style with the underline attribute changed
     */
    public Style underline(boolean b)
    {
        return new Style(fg, bg, bold, reverse, dim, b);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Style)) return false;
        Style s = (Style) o;
        return bold == s.bold && reverse == s.reverse && dim == s.dim && underline == s.underline
                && fg.equals(s.fg) && bg.equals(s.bg);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(fg, bg, bold, reverse, dim, underline);
    }

    @Override
    public String toString()
    {
        return "Style[fg=" + fg + ", bg=" + bg
                + (bold ? ", bold" : "") + (reverse ? ", reverse" : "")
                + (dim ? ", dim" : "") + (underline ? ", underline" : "") + "]";
    }
}
