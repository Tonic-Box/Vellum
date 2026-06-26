package com.tonic.vellum.widget;

import com.tonic.vellum.Canvas;
import com.tonic.vellum.KeyResult;
import com.tonic.vellum.input.Key;
import com.tonic.vellum.input.KeyEvent;
import com.tonic.vellum.style.Style;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * A single-choice option list. The cursor moves with arrows; ENTER or SPACE chooses the
 * option under the cursor. The chosen option is marked {@code (o)}; others {@code ( )}.
 * Built on {@link AbstractListSection}.
 */
public final class RadioGroup extends AbstractListSection {

    private final List<String> options;
    private int chosen;
    private IntConsumer onChange = i -> { };

    public RadioGroup(String... options) {
        this.options = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(options)));
    }

    public int chosenIndex() {
        return chosen;
    }

    public RadioGroup choose(int index) {
        if (index >= 0 && index < options.size() && index != chosen) {
            chosen = index;
            onChange.accept(chosen);
            requestRedraw();
        }
        return this;
    }

    public RadioGroup onChange(IntConsumer handler) {
        this.onChange = handler;
        return this;
    }

    @Override
    protected int rowCount() {
        return options.size();
    }

    @Override
    protected void renderRow(Canvas row, int index, Style style) {
        row.put(0, 0, (index == chosen ? "(o) " : "( ) ") + options.get(index), style);
    }

    @Override
    protected void onActivate(int index) {
        choose(index);
    }

    @Override
    protected KeyResult onKey(KeyEvent key) {
        if (key.is(Key.CHAR) && key.ch() == ' ') {
            if (rowCount() > 0) {
                onActivate(selectedIndex());
            }
            return KeyResult.CONSUMED;
        }
        return super.onKey(key);
    }
}
