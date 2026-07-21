package com.tonic.vellum;

import com.tonic.vellum.geom.Rect;

/**
 * Computes an overlay's bounds from the current screen bounds.
 */
public interface Placement
{
    /**
     * Resolves the overlay bounds for the given screen bounds.
     *
     * @param screen the current screen bounds
     * @return the overlay bounds
     */
    Rect resolve(Rect screen);

    /**
     * Creates a placement centered on and clamped to the screen at the given size.
     *
     * @param width the desired width
     * @param height the desired height
     * @return a centered placement
     */
    static Placement centered(int width, int height)
    {
        return screen ->
        {
            int w = Math.min(width, screen.width());
            int h = Math.min(height, screen.height());
            int x = screen.x() + (screen.width() - w) / 2;
            int y = screen.y() + (screen.height() - h) / 2;
            return new Rect(x, y, w, h);
        };
    }

    /**
     * Creates a placement at a fixed rectangle, independent of screen size.
     *
     * @param rect the fixed overlay bounds
     * @return a fixed placement
     */
    static Placement fixed(Rect rect)
    {
        return screen -> rect;
    }
}
