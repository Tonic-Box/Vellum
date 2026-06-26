package com.tonic.vellum.widget;

/** Block-element glyphs for bars and sparklines. */
final class Blocks {

    /** Full cell. */
    static final char FULL = '\u2588';

    /** Lower partial blocks 1/8..8/8 (vertical bars, sparklines). */
    static final char[] LOWER = {
            '\u2581', '\u2582', '\u2583', '\u2584', '\u2585', '\u2586', '\u2587', '\u2588'
    };

    /** Left partial blocks 1/8..8/8 (horizontal fill). */
    static final char[] LEFT = {
            '\u258F', '\u258E', '\u258D', '\u258C', '\u258B', '\u258A', '\u2589', '\u2588'
    };

    private Blocks() {}
}
