package com.tonic.vellum.layout;

/**
 * An immutable sizing rule for one child of a {@link Split}. Created via the static
 * factory methods.
 */
public final class Constraint {

    /** The category of a constraint. */
    public enum Kind { FIXED, PERCENT, FILL, MIN, MAX }

    private final Kind kind;
    private final int value;

    private Constraint(Kind kind, int value) {
        this.kind = kind;
        this.value = value;
    }

    /**
     * Creates a fixed-size constraint of exactly {@code cells} along the main axis.
     *
     * @param cells the size in cells, clamped to non-negative
     * @return the constraint
     */
    public static Constraint fixed(int cells) {
        return new Constraint(Kind.FIXED, Math.max(0, cells));
    }

    /**
     * Creates a constraint sized as a percentage of the original available extent.
     *
     * @param pct the percentage, clamped to non-negative
     * @return the constraint
     */
    public static Constraint percent(int pct) {
        return new Constraint(Kind.PERCENT, Math.max(0, pct));
    }

    /**
     * Creates a constraint taking an equal share of the remaining space.
     *
     * @return the constraint
     */
    public static Constraint fill() {
        return fill(1);
    }

    /**
     * Creates a constraint taking a weighted share of the remaining space.
     *
     * @param weight the relative weight, clamped to at least 1
     * @return the constraint
     */
    public static Constraint fill(int weight) {
        return new Constraint(Kind.FILL, Math.max(1, weight));
    }

    /**
     * Creates a constraint of at least {@code cells}, growing into remaining space.
     *
     * @param cells the minimum size in cells, clamped to non-negative
     * @return the constraint
     */
    public static Constraint min(int cells) {
        return new Constraint(Kind.MIN, Math.max(0, cells));
    }

    /**
     * Creates a constraint of at most {@code cells}, growing into remaining space up to
     * that cap.
     *
     * @param cells the maximum size in cells, clamped to non-negative
     * @return the constraint
     */
    public static Constraint max(int cells) {
        return new Constraint(Kind.MAX, Math.max(0, cells));
    }

    /** @return the constraint kind */
    public Kind kind() { return kind; }

    /** @return the constraint value in cells, percent, or weight depending on the kind */
    public int value() { return value; }

    @Override
    public String toString() {
        return "Constraint[" + kind + " " + value + "]";
    }
}
