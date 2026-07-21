package com.tonic.vellum;

import com.tonic.vellum.style.Style;

final class Renderer
{
    static final String CSI = new String(new char[]{(char) 27, (char) 91});
    private static final String RESET = CSI + "0m";

    private final Buffer front;

    Renderer(int width, int height)
    {
        this.front = new Buffer(width, height);
        this.front.invalidate();
    }

    void resize(int width, int height)
    {
        front.resize(width, height);
        front.invalidate();
    }

    String flush(Buffer back)
    {
        StringBuilder sb = new StringBuilder();
        int w = back.width();
        int h = back.height();
        Style current = null;
        int lastX = -2;
        int lastY = -2;

        for (int y = 0; y < h; y++)
        {
            for (int x = 0; x < w; x++)
            {
                int bc = back.codePointAt(x, y);
                Style bs = back.styleAt(x, y);
                boolean changed = !(bc == front.codePointAt(x, y) && bs.equals(front.styleAt(x, y)));
                if (!changed)
                {
                    if (bc != Buffer.WIDE_CONTINUATION && CharWidth.of(bc) == 2)
                    {
                        x++;
                    }
                    continue;
                }
                boolean contiguous = (y == lastY && x == lastX + 1);
                if (!contiguous)
                {
                    sb.append(CSI).append(y + 1).append(';').append(x + 1).append('H');
                }
                if (current == null || !current.equals(bs))
                {
                    sb.append(sgr(bs));
                    current = bs;
                }
                if (bc != Buffer.WIDE_CONTINUATION && CharWidth.of(bc) == 2 && x + 1 < w)
                {
                    sb.appendCodePoint(bc);
                    front.set(x, y, bc, bs);
                    front.set(x + 1, y, back.codePointAt(x + 1, y), back.styleAt(x + 1, y));
                    lastX = x + 1;
                    x++;
                }
                else
                {
                    sb.appendCodePoint(bc == Buffer.WIDE_CONTINUATION ? ' ' : bc);
                    front.set(x, y, bc, bs);
                    lastX = x;
                }
                lastY = y;
            }
        }
        if (sb.length() > 0)
        {
            sb.append(RESET);
        }
        return sb.toString();
    }

    private static String sgr(Style s)
    {
        StringBuilder sb = new StringBuilder(CSI).append('0');
        if (s.isBold()) sb.append(";1");
        if (s.isDim()) sb.append(";2");
        if (s.isUnderline()) sb.append(";4");
        if (s.isReverse()) sb.append(";7");
        sb.append(';').append(s.foreground().foregroundSgr());
        sb.append(';').append(s.background().backgroundSgr());
        return sb.append('m').toString();
    }
}
