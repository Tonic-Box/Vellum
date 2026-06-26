package com.tonic.vellum.layout;

import com.tonic.vellum.Section;

/** A typed (constraint, section) pair for the varargs {@link Split} constructors. */
public final class Slot {

    private final Constraint constraint;
    private final Section section;

    private Slot(Constraint constraint, Section section) {
        this.constraint = constraint;
        this.section = section;
    }

    /** Pair a constraint with the section it sizes. */
    public static Slot of(Constraint constraint, Section section) {
        return new Slot(constraint, section);
    }

    public Constraint constraint() { return constraint; }

    public Section section() { return section; }
}
