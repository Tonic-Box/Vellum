package com.tonic.vellum.widget;

import com.tonic.vellum.Canvas;
import com.tonic.vellum.KeyResult;
import com.tonic.vellum.Section;
import com.tonic.vellum.input.KeyEvent;
import com.tonic.vellum.style.Style;

import java.util.ArrayList;
import java.util.List;

/**
 * Vertically scrollable lines of text, scrolled with UP/DOWN/PAGE_UP/PAGE_DOWN/HOME/END.
 * Optional follow-tail keeps the view pinned to the bottom as lines are appended.
 */
public class ScrollSection extends Section
{
    private final List<String> lines = new ArrayList<>();
    private final Viewport viewport = new Viewport();
    private Style style = Style.NORMAL;
    private boolean followTail = false;
    private int maxLines = 0;

    /**
     * Caps the retained lines, dropping the oldest beyond {@code max}; 0 means unlimited.
     *
     * @param max the maximum retained lines, or 0 for unlimited
     * @return this ScrollSection for chaining
     */
    public ScrollSection maxLines(int max)
    {
        this.maxLines = Math.max(0, max);
        trim();
        displayDirty = true;
        requestRedraw();
        return this;
    }

    private boolean wrap;
    private List<String> displayLines = new ArrayList<>();
    private boolean displayDirty = true;
    private int cachedWidth = -1;

    /**
     * Sets whether the view stays pinned to the bottom as lines are appended. Enabling it
     * scrolls to the bottom immediately.
     *
     * @param follow {@code true} to follow the tail
     * @return this ScrollSection for chaining
     */
    public ScrollSection followTail(boolean follow)
    {
        this.followTail = follow;
        if (follow)
        {
            scrollToBottom();
            requestRedraw();
        }
        return this;
    }

    /**
     * Enables width-aware word wrapping; scrolling then operates over wrapped display lines.
     *
     * @param wrap {@code true} to enable wrapping
     * @return this ScrollSection for chaining
     */
    public ScrollSection wrap(boolean wrap)
    {
        this.wrap = wrap;
        this.displayDirty = true;
        requestRedraw();
        return this;
    }

    /**
     * Sets the text style.
     *
     * @param style the style
     * @return this ScrollSection for chaining
     */
    public ScrollSection style(Style style)
    {
        this.style = style;
        requestRedraw();
        return this;
    }

    /**
     * Replaces all lines; scrolls to the bottom when following the tail.
     *
     * @param newLines the new lines
     * @return this ScrollSection for chaining
     */
    public ScrollSection setLines(List<String> newLines)
    {
        lines.clear();
        lines.addAll(newLines);
        trim();
        displayDirty = true;
        if (followTail)
        {
            scrollToBottom();
        }
        requestRedraw();
        return this;
    }

    /**
     * Appends one line; pins to the bottom when following the tail.
     *
     * @param line the line to append
     */
    protected void appendLine(String line)
    {
        lines.add(line);
        trim();
        displayDirty = true;
        if (followTail)
        {
            scrollToBottom();
        }
        requestRedraw();
    }

    private void trim()
    {
        if (maxLines > 0 && lines.size() > maxLines)
        {
            lines.subList(0, lines.size() - maxLines).clear();
        }
    }

    /**
     * @return the number of logical lines
     */
    public int lineCount()
    {
        return lines.size();
    }

    /**
     * @return the index of the first visible line
     */
    public int scrollTop()
    {
        return viewport.top();
    }

    @Override
    protected void render(Canvas canvas)
    {
        List<String> visible = visibleLines();
        viewport.set(viewport.top(), visible.size(), viewportHeight());
        int top = viewport.top();
        for (int i = 0; i < canvas.height() && top + i < visible.size(); i++)
        {
            canvas.put(0, i, visible.get(top + i), style);
        }
    }

    @Override
    protected KeyResult onKey(KeyEvent key)
    {
        switch (key.code())
        {
            case UP:        scrollBy(-1); followTail = false; return KeyResult.CONSUMED;
            case DOWN:      scrollBy(1); followTail = atBottom(); return KeyResult.CONSUMED;
            case PAGE_UP:   scrollBy(-viewportHeight()); followTail = false; return KeyResult.CONSUMED;
            case PAGE_DOWN: scrollBy(viewportHeight()); followTail = atBottom(); return KeyResult.CONSUMED;
            case HOME:      setTop(0); followTail = false; return KeyResult.CONSUMED;
            case END:       setTop(Viewport.maxTop(visibleLines().size(), viewportHeight())); followTail = true; return KeyResult.CONSUMED;
            default:        return KeyResult.UNHANDLED;
        }
    }

    private boolean atBottom()
    {
        return viewport.top() == Viewport.maxTop(visibleLines().size(), viewportHeight());
    }

    private void scrollToBottom()
    {
        viewport.toBottom(visibleLines().size(), viewportHeight());
    }

    private void scrollBy(int delta)
    {
        setTop(viewport.top() + delta);
    }

    private void setTop(int newTop)
    {
        int before = viewport.top();
        viewport.set(newTop, visibleLines().size(), viewportHeight());
        if (viewport.top() != before)
        {
            requestRedraw();
        }
    }

    private int viewportHeight()
    {
        return Math.max(1, bounds().height());
    }

    private List<String> visibleLines()
    {
        if (!wrap)
        {
            return lines;
        }
        int width = bounds().width();
        if (displayDirty || width != cachedWidth)
        {
            displayLines = TextWrap.wrapAll(lines, width);
            cachedWidth = width;
            displayDirty = false;
        }
        return displayLines;
    }
}
