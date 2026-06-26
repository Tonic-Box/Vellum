package com.tonic.vellum.focus;

import com.tonic.vellum.Section;

import java.util.List;

/**
 * Optional capability for a {@link Section} that manages internal focus stops (e.g. a
 * form with several fields, or a tab host whose bar and content are separate stops). The
 * focus manager consults this before advancing at the parent level, enabling correct
 * nested Tab traversal without forcing every Section to participate.
 */
public interface FocusContainer {

    /** Ordered focusable descendants this container manages internally. */
    List<Section> focusTargets();

    /** The currently focused internal target, or {@code null}. */
    Section activeFocusTarget();

    /** Move internal focus; return {@code false} at an edge so the parent advances instead. */
    boolean advanceFocus(boolean forward);
}
