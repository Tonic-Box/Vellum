package com.tonic.vellum;

/**
 * The engine seam a live {@link Section} talks to. Implemented by {@link App}; kept
 * package-private so it never appears in the public API, and so {@code Section} does not
 * depend on the concrete app type. A detached section has a {@code null} host.
 */
interface Host {

    /** Request that the run loop repaint dirty sections on its next iteration. */
    void requestRepaint();

    /** Throw {@link IllegalStateException} if the caller is not on the UI thread. */
    void assertUiThread();

    /** True if the given section is currently on the focus path. */
    boolean isOnFocusPath(Section section);
}
