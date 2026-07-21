package com.tonic.vellum.widget;

import com.tonic.vellum.CharWidth;

import java.util.ArrayList;
import java.util.List;

final class TextWrap
{
    private TextWrap()
    {
    }

    static List<String> wrap(String line, int width)
    {
        List<String> out = new ArrayList<>();
        if (width <= 0 || line.isEmpty())
        {
            out.add(line);
            return out;
        }
        StringBuilder current = new StringBuilder();
        int currentWidth = 0;
        for (String word : line.split(" ", -1))
        {
            int wordWidth = CharWidth.width(word);
            int separator = current.length() == 0 ? 0 : 1;
            if (currentWidth + separator + wordWidth <= width)
            {
                if (separator == 1)
                {
                    current.append(' ');
                    currentWidth += 1;
                }
                current.append(word);
                currentWidth += wordWidth;
                continue;
            }
            if (current.length() > 0)
            {
                out.add(current.toString());
                current.setLength(0);
                currentWidth = 0;
            }
            if (wordWidth <= width)
            {
                current.append(word);
                currentWidth = wordWidth;
            }
            else
            {
                List<String> chunks = hardBreak(word, width);
                for (int i = 0; i < chunks.size() - 1; i++)
                {
                    out.add(chunks.get(i));
                }
                String last = chunks.get(chunks.size() - 1);
                current.append(last);
                currentWidth = CharWidth.width(last);
            }
        }
        out.add(current.toString());
        return out;
    }

    static List<String> wrapAll(List<String> lines, int width)
    {
        List<String> out = new ArrayList<>();
        for (String line : lines)
        {
            out.addAll(wrap(line, width));
        }
        return out;
    }

    private static List<String> hardBreak(String word, int width)
    {
        List<String> out = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int currentWidth = 0;
        int i = 0;
        while (i < word.length())
        {
            int cp = word.codePointAt(i);
            i += Character.charCount(cp);
            int w = CharWidth.of(cp);
            if (currentWidth + w > width && current.length() > 0)
            {
                out.add(current.toString());
                current.setLength(0);
                currentWidth = 0;
            }
            current.appendCodePoint(cp);
            currentWidth += w;
        }
        out.add(current.toString());
        return out;
    }
}
