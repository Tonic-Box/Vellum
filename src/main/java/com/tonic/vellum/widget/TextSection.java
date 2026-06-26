package com.tonic.vellum.widget;

import com.tonic.vellum.Canvas;
import com.tonic.vellum.Section;
import com.tonic.vellum.style.Style;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Multi-line static or settable text, drawn top-down. Lines split on {@code \n}; optional word wrapping. */
public final class TextSection extends Section {

    private List<String> lines;
    private Style style = Style.NORMAL;
    private boolean wrap;

    public TextSection(String text) {
        this.lines = split(text);
    }

    public TextSection setText(String text) {
        this.lines = split(text);
        requestRedraw();
        return this;
    }

    public TextSection style(Style style) {
        this.style = style;
        requestRedraw();
        return this;
    }

    /** Enable width-aware word wrapping (default off). */
    public TextSection wrap(boolean wrap) {
        this.wrap = wrap;
        requestRedraw();
        return this;
    }

    private static List<String> split(String text) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(text.split("\n", -1)));
    }

    @Override
    protected void render(Canvas canvas) {
        List<String> display = wrap ? TextWrap.wrapAll(lines, canvas.width()) : lines;
        for (int i = 0; i < display.size() && i < canvas.height(); i++) {
            canvas.put(0, i, display.get(i), style);
        }
    }
}
