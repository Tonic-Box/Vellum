package com.tonic.vellum.layout;

import com.tonic.vellum.Canvas;
import com.tonic.vellum.Section;
import com.tonic.vellum.geom.Rect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A layout container that divides its bounds among children along one {@link Axis} by
 * {@link Constraint}s. Layout is itself a Section, so the whole UI is one tree.
 *
 * <p>Construct with the typed varargs form {@code Split.vertical(Slot.of(c, s), ...)} or
 * the fluent form {@code Split.vertical().add(c, s).add(c, s)}. Children are added at
 * construction, before the split enters the live tree.
 */
public final class Split extends Section {

    private final Axis axis;
    private final List<Constraint> constraints = new ArrayList<>();
    private final List<Section> sections = new ArrayList<>();

    private Split(Axis axis) {
        this.axis = axis;
    }

    public static Split horizontal(Slot... slots) {
        return create(Axis.HORIZONTAL, slots);
    }

    public static Split vertical(Slot... slots) {
        return create(Axis.VERTICAL, slots);
    }

    private static Split create(Axis axis, Slot[] slots) {
        Split split = new Split(axis);
        for (Slot slot : slots) {
            split.add(slot.constraint(), slot.section());
        }
        return split;
    }

    /** Append a constrained child. Call at construction, before the split enters the tree. */
    public Split add(Constraint size, Section child) {
        constraints.add(size);
        sections.add(child);
        return this;
    }

    @Override
    protected List<Section> children() {
        return Collections.unmodifiableList(sections);
    }

    @Override
    protected void onResize(Rect newBounds) {
        relayout();
    }

    private void relayout() {
        if (sections.isEmpty()) {
            return;
        }
        Rect[] rects = LayoutSolver.solve(bounds(), constraints, axis);
        for (int i = 0; i < sections.size(); i++) {
            place(sections.get(i), rects[i]);
        }
    }

    @Override
    protected void render(Canvas canvas) {
        // A Split draws nothing of its own; its children tile its bounds exactly.
    }
}
