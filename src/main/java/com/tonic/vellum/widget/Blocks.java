package com.tonic.vellum.widget;

/** Block-element glyphs for bars and sparklines. */
final class Blocks {

    /** Full cell. */
    static final char FULL = '█';

    /** Lower partial blocks 1/8..8/8 (vertical bars, sparklines). */
    static final char[] LOWER = {
            '▁', '▂', '▃', '▄', '▅', '▆', '▇', '█'
    };

    /** Left partial blocks 1/8..8/8 (horizontal fill). */
    static final char[] LEFT = {
            '▏', '▎', '▍', '▌', '▋', '▊', '▉', '█'
    };

    private Blocks() {}
}
