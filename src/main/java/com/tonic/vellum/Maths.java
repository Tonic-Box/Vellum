package com.tonic.vellum;

/** Small numeric helpers. */
public final class Maths {

    private Maths() {}

    /**
     * Constrain {@code value} to the inclusive range {@code [min, max]}.
     *
     * @param value the value to constrain
     * @param min the lower bound, inclusive
     * @param max the upper bound, inclusive
     * @return the clamped value
     */
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Constrain {@code value} to the inclusive range {@code [min, max]}.
     *
     * @param value the value to constrain
     * @param min the lower bound, inclusive
     * @param max the upper bound, inclusive
     * @return the clamped value
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
