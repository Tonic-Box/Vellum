package com.tonic.vellum;

import com.tonic.vellum.geom.Rect;

/** Computes an overlay's bounds from the current screen bounds. */
public interface Placement {

    Rect resolve(Rect screen);

    /** Centered, clamped to the screen, at the given size. */
    static Placement centered(int width, int height) {
        return screen -> {
            int w = Math.min(width, screen.width());
            int h = Math.min(height, screen.height());
            int x = screen.x() + (screen.width() - w) / 2;
            int y = screen.y() + (screen.height() - h) / 2;
            return new Rect(x, y, w, h);
        };
    }

    /** A fixed rectangle, independent of screen size. */
    static Placement fixed(Rect rect) {
        return screen -> rect;
    }
}
