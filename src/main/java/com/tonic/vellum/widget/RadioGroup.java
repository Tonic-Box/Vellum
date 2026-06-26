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

    /**
     * Creates a radio group with the given options. The first option is chosen initially.
     *
     * @param options the option labels in order
     */
    public RadioGroup(String... options) {
        this.options = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(options)));
    }

    /**
     * Returns the index of the chosen option.
     *
     * @return the chosen option index
     */
    public int chosenIndex() {
        return chosen;
    }

    /**
     * Chooses the option at the given index; fires {@code onChange} when it changes. Out-of-range
     * indices are ignored.
     *
     * @param index the option index to choose
     * @return this RadioGroup for chaining
     */
    public RadioGroup choose(int index) {
        if (index >= 0 && index < options.size() && index != chosen) {
            chosen = index;
            onChange.accept(chosen);
            requestRedraw();
        }
        return this;
    }

    /**
     * Sets the handler called with the chosen index whenever the choice changes.
     *
     * @param handler the change handler
     * @return this RadioGroup for chaining
     */
    public RadioGroup onChange(IntConsumer handler) {
        this.onChange = handler;
        return this;
    }

    /**
     * Returns the number of options.
     *
     * @return the option count
     */
    @Override
    protected int rowCount() {
        return options.size();
    }

    /**
     * Draws the option label prefixed with its chosen or unchosen marker.
     *
     * @param row the single-row canvas to draw into
     * @param index the option index
     * @param style the style the row has been filled with
     */
    @Override
    protected void renderRow(Canvas row, int index, Style style) {
        row.put(0, 0, (index == chosen ? "(o) " : "( ) ") + options.get(index), style);
    }

    /**
     * Chooses the activated option.
     *
     * @param index the index of the activated option
     */
    @Override
    protected void onActivate(int index) {
        choose(index);
    }

    /**
     * Handles SPACE to choose the option under the cursor; otherwise defers to the base
     * navigation keys.
     *
     * @param key the key event
     * @return the key handling result
     */
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
