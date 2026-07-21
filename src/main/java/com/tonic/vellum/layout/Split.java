package com.tonic.vellum.layout;

import com.tonic.vellum.Canvas;
import com.tonic.vellum.Section;
import com.tonic.vellum.geom.Rect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A section that divides its bounds among child sections along one axis according to
 * their constraints. Children are added at construction, before the split enters the
 * live tree.
 */
public class Split extends Section
{
    private final Axis axis;
    private final List<Constraint> constraints = new ArrayList<>();
    private final List<Section> sections = new ArrayList<>();

    /**
     * Creates an empty split along the given axis.
     *
     * @param axis the axis to divide along
     */
    protected Split(Axis axis)
    {
        this.axis = axis;
    }

    /**
     * Creates a horizontal split from the given slots.
     *
     * @param slots the constrained children, in order
     * @return the split
     */
    public static Split horizontal(Slot... slots)
    {
        return create(Axis.HORIZONTAL, slots);
    }

    /**
     * Creates a vertical split from the given slots.
     *
     * @param slots the constrained children, in order
     * @return the split
     */
    public static Split vertical(Slot... slots)
    {
        return create(Axis.VERTICAL, slots);
    }

    private static Split create(Axis axis, Slot[] slots)
    {
        Split split = new Split(axis);
        for (Slot slot : slots)
        {
            split.add(slot.constraint(), slot.section());
        }
        return split;
    }

    /**
     * Appends a constrained child. Call at construction, before the split enters the tree.
     *
     * @param size the constraint sizing the child
     * @param child the child section
     * @return this Split for chaining
     */
    public Split add(Constraint size, Section child)
    {
        constraints.add(size);
        sections.add(child);
        return this;
    }

    @Override
    protected List<Section> children()
    {
        return Collections.unmodifiableList(sections);
    }

    @Override
    protected void onResize(Rect newBounds)
    {
        relayout();
    }

    private void relayout()
    {
        if (sections.isEmpty())
        {
            return;
        }
        Rect[] rects = LayoutSolver.solve(bounds(), constraints, axis);
        for (int i = 0; i < sections.size(); i++)
        {
            place(sections.get(i), rects[i]);
        }
    }

    @Override
    protected void render(Canvas canvas)
    {
    }
}
