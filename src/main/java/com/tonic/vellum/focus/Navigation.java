package com.tonic.vellum.focus;

import com.tonic.vellum.Section;
import com.tonic.vellum.input.Key;

import java.util.List;

/**
 * Optional spatial focus movement: when the focused section leaves an arrow key
 * unhandled, the navigation strategy may move focus to an adjacent target.
 */
public interface Navigation {

    /**
     * Chooses the focus target reached by pressing {@code direction} from {@code current}.
     *
     * @param current the currently focused section
     * @param direction the arrow key pressed
     * @param targets the candidate focus targets
     * @return the target to focus, or {@code null} to leave focus unchanged
     */
    Section resolve(Section current, Key direction, List<Section> targets);

    /**
     * Returns an adjacency-based navigation strategy using the targets' on-screen bounds.
     *
     * @return the spatial navigation strategy
     */
    static Navigation spatial() {
        return new SpatialNavigation();
    }
}
