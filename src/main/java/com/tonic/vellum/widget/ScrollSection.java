package com.tonic.vellum.widget;

import com.tonic.vellum.Canvas;
import com.tonic.vellum.KeyResult;
import com.tonic.vellum.Section;
import com.tonic.vellum.input.KeyEvent;
import com.tonic.vellum.style.Style;

import java.util.ArrayList;
import java.util.List;

/**
 * Vertically scrollable lines of text with {@code UP}/{@code DOWN}/{@code PAGE_*}/
 * {@code HOME}/{@code END}. Optional follow-tail keeps the view pinned to the bottom as
 * lines are appended; scrolling up turns it off, {@code END} turns it back on.
 */
public class ScrollSection extends Section {

    private final List<String> lines = new ArrayList<>();
    private final Viewport viewport = new Viewport();
    private Style style = Style.NORMAL;
    private boolean followTail = false;
    private int maxLines = 0; // 0 = unlimited

    /** Cap the retained lines, dropping the oldest beyond {@code max}; 0 means unlimited. */
    public ScrollSection maxLines(int max) {
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

    public ScrollSection followTail(boolean follow) {
        this.followTail = follow;
        return this;
    }

    /** Enable width-aware word wrapping; scrolling then operates over wrapped display lines. */
    public ScrollSection wrap(boolean wrap) {
        this.wrap = wrap;
        this.displayDirty = true;
        requestRedraw();
        return this;
    }

    public ScrollSection style(Style style) {
        this.style = style;
        requestRedraw();
        return this;
    }

    public ScrollSection setLines(List<String> newLines) {
        lines.clear();
        lines.addAll(newLines);
        trim();
        displayDirty = true;
        if (followTail) {
            scrollToBottom();
        }
        requestRedraw();
        return this;
    }

    /** Append one line; pins to the bottom when following the tail. */
    protected void appendLine(String line) {
        lines.add(line);
        trim();
        displayDirty = true;
        if (followTail) {
            scrollToBottom();
        }
        requestRedraw();
    }

    private void trim() {
        if (maxLines > 0 && lines.size() > maxLines) {
            lines.subList(0, lines.size() - maxLines).clear();
        }
    }

    public int lineCount() {
        return lines.size();
    }

    public int scrollTop() {
        return viewport.top();
    }

    @Override
    protected void render(Canvas canvas) {
        List<String> visible = visibleLines();
        viewport.set(viewport.top(), visible.size(), viewportHeight());
        int top = viewport.top();
        for (int i = 0; i < canvas.height() && top + i < visible.size(); i++) {
            canvas.put(0, i, visible.get(top + i), style);
        }
    }

    @Override
    protected KeyResult onKey(KeyEvent key) {
        switch (key.code()) {
            case UP:        scrollBy(-1); followTail = false; return KeyResult.CONSUMED;
            case DOWN:      scrollBy(1); return KeyResult.CONSUMED;
            case PAGE_UP:   scrollBy(-viewportHeight()); followTail = false; return KeyResult.CONSUMED;
            case PAGE_DOWN: scrollBy(viewportHeight()); return KeyResult.CONSUMED;
            case HOME:      setTop(0); followTail = false; return KeyResult.CONSUMED;
            case END:       setTop(Viewport.maxTop(visibleLines().size(), viewportHeight())); followTail = true; return KeyResult.CONSUMED;
            default:        return KeyResult.UNHANDLED;
        }
    }

    private void scrollToBottom() {
        viewport.toBottom(visibleLines().size(), viewportHeight());
    }

    private void scrollBy(int delta) {
        setTop(viewport.top() + delta);
    }

    private void setTop(int newTop) {
        int before = viewport.top();
        viewport.set(newTop, visibleLines().size(), viewportHeight());
        if (viewport.top() != before) {
            requestRedraw();
        }
    }

    private int viewportHeight() {
        return Math.max(1, bounds().height());
    }

    /** The lines to scroll over: wrapped display lines when wrapping, otherwise the logical lines. */
    private List<String> visibleLines() {
        if (!wrap) {
            return lines;
        }
        int width = bounds().width();
        if (displayDirty || width != cachedWidth) {
            displayLines = TextWrap.wrapAll(lines, width);
            cachedWidth = width;
            displayDirty = false;
        }
        return displayLines;
    }
}
