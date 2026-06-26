package com.tonic.vellum.layout;

/**
 * A sizing rule for one child of a {@link Split}. Immutable. Created via the static
 * factories; the kind tag lets {@link LayoutSolver} dispatch without a subclass per kind.
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

    /** Exactly {@code cells} along the main axis. */
    public static Constraint fixed(int cells) {
        return new Constraint(Kind.FIXED, Math.max(0, cells));
    }

    /** A percentage of the original available extent. */
    public static Constraint percent(int pct) {
        return new Constraint(Kind.PERCENT, Math.max(0, pct));
    }

    /** An equal share of the remaining space. */
    public static Constraint fill() {
        return fill(1);
    }

    /** A weighted share of the remaining space. */
    public static Constraint fill(int weight) {
        return new Constraint(Kind.FILL, Math.max(1, weight));
    }

    /** At least {@code cells}, growing into remaining space. */
    public static Constraint min(int cells) {
        return new Constraint(Kind.MIN, Math.max(0, cells));
    }

    /** At most {@code cells}, growing into remaining space up to that cap. */
    public static Constraint max(int cells) {
        return new Constraint(Kind.MAX, Math.max(0, cells));
    }

    public Kind kind() { return kind; }

    public int value() { return value; }

    @Override
    public String toString() {
        return "Constraint[" + kind + " " + value + "]";
    }
}
