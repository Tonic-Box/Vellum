package com.tonic.vellum.widget;

import com.tonic.vellum.Maths;

/**
 * The vertical scroll position shared by the scrollable list widgets: the index of the first
 * visible row, with the clamping math common to free scrolling and selection-driven scrolling.
 */
final class Viewport {

    private int top;

    int top() {
        return top;
    }

    /** Set the top index, clamped to a valid scroll range. */
    void set(int newTop, int count, int viewport) {
        top = Maths.clamp(newTop, 0, maxTop(count, viewport));
    }

    void scrollBy(int delta, int count, int viewport) {
        set(top + delta, count, viewport);
    }

    void toBottom(int count, int viewport) {
        set(maxTop(count, viewport), count, viewport);
    }

    /** Scroll the minimum amount to bring {@code index} into the viewport. */
    void ensureVisible(int index, int count, int viewport) {
        if (index < top) {
            top = index;
        } else if (index >= top + viewport) {
            top = index - viewport + 1;
        }
        top = Maths.clamp(top, 0, maxTop(count, viewport));
    }

    static int maxTop(int count, int viewport) {
        return Math.max(0, count - Math.max(1, viewport));
    }
}
