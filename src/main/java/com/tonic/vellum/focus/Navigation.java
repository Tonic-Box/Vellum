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
     * Choose the focus target reached by pressing {@code direction} from {@code current},
     * or {@code null} to leave focus unchanged.
     */
    Section resolve(Section current, Key direction, List<Section> targets);

    /** Adjacency-based navigation using the targets' on-screen bounds. */
    static Navigation spatial() {
        return new SpatialNavigation();
    }
}
