package com.tonic.vellum.widget;

import com.tonic.vellum.Canvas;
import com.tonic.vellum.style.Style;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * A single-choice option list; ENTER or SPACE chooses the option under the cursor, marked {@code (o)}.
 */
public final class RadioGroup extends AbstractListSection
{
    private final List<String> options;
    private int chosen;
    private IntConsumer onChange = i -> { };

    /**
     * Creates a radio group with the given options. The first option is chosen initially.
     *
     * @param options the option labels in order
     */
    public RadioGroup(String... options)
    {
        this.options = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(options)));
    }

    /**
     * @return the index of the chosen option
     */
    public int chosenIndex()
    {
        return chosen;
    }

    /**
     * Chooses the option at the given index; fires {@code onChange} when it changes. Out-of-range
     * indices are ignored.
     *
     * @param index the option index to choose
     * @return this RadioGroup for chaining
     */
    public RadioGroup choose(int index)
    {
        if (index >= 0 && index < options.size() && index != chosen)
        {
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
    public RadioGroup onChange(IntConsumer handler)
    {
        this.onChange = handler;
        return this;
    }

    @Override
    protected int rowCount()
    {
        return options.size();
    }

    @Override
    protected void renderRow(Canvas row, int index, Style style)
    {
        row.put(0, 0, (index == chosen ? "(o) " : "( ) ") + options.get(index), style);
    }

    @Override
    protected void onActivate(int index)
    {
        choose(index);
    }
}
