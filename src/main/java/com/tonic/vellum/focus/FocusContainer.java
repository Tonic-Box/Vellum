package com.tonic.vellum.focus;

import com.tonic.vellum.Section;

import java.util.List;

/**
 * Optional capability for a {@link Section} that manages internal focus stops, such as a
 * form with several fields or a tab host whose bar and content are separate stops. The
 * focus manager consults this before advancing at the parent level, enabling nested Tab
 * traversal without forcing every Section to participate.
 */
public interface FocusContainer {

    /**
     * Returns the focusable descendants this container manages internally.
     *
     * @return the focus targets, in traversal order
     */
    List<Section> focusTargets();

    /**
     * Returns the currently focused internal target.
     *
     * @return the active focus target, or {@code null} when none is focused
     */
    Section activeFocusTarget();

    /**
     * Moves internal focus in the given direction.
     *
     * @param forward true to advance, false to move back
     * @return true when focus moved internally, false at an edge so the parent advances instead
     */
    boolean advanceFocus(boolean forward);
}
