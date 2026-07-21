package com.tonic.vellum.focus;

import com.tonic.vellum.Section;
import com.tonic.vellum.input.Key;

import java.util.List;

/**
 * Strategy that moves focus to an adjacent target when the focused section leaves an arrow key unhandled.
 */
public interface Navigation
{
    /**
     * Chooses the focus target reached by moving in the given direction from the current section.
     *
     * @param current the currently focused section
     * @param direction the arrow key pressed
     * @param targets the candidate focus targets
     * @return the target to focus, or {@code null} to leave focus unchanged
     */
    Section resolve(Section current, Key direction, List<Section> targets);

    /**
     * @return an adjacency-based navigation strategy using the targets' on-screen bounds
     */
    static Navigation spatial()
    {
        return new SpatialNavigation();
    }
}
