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

    /**
     * Pairs a constraint with the section it sizes.
     *
     * @param constraint the sizing constraint
     * @param section the section to size
     * @return the slot
     */
    public static Slot of(Constraint constraint, Section section) {
        return new Slot(constraint, section);
    }

    /** @return the sizing constraint */
    public Constraint constraint() { return constraint; }

    /** @return the section */
    public Section section() { return section; }
}
