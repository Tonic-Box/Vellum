package com.tonic.vellum.widget;

import com.tonic.vellum.KeyResult;
import com.tonic.vellum.Section;
import com.tonic.vellum.focus.FocusContainer;
import com.tonic.vellum.input.Key;
import com.tonic.vellum.input.KeyEvent;
import com.tonic.vellum.layout.Axis;
import com.tonic.vellum.layout.Constraint;
import com.tonic.vellum.layout.Split;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A vertical stack of fields with internal Tab traversal. Reuses {@link Split} for layout
 * and implements {@link FocusContainer} so Tab/Shift-Tab cycle the fields; nesting another
 * {@code Form} (e.g. a horizontal button row) is traversed depth-first. ESC fires
 * {@code onCancel}. Used by {@link Dialogs} and for any multi-field UI.
 */
public final class Form extends Split implements FocusContainer {

    private final List<Section> fields = new ArrayList<>();
    private int active;
    private Runnable onCancel;

    public Form() {
        super(Axis.VERTICAL);
    }

    private Form(Axis axis) {
        super(axis);
    }

    /** A horizontal form (e.g. a row of buttons), traversed left-to-right by Tab. */
    public static Form row() {
        return new Form(Axis.HORIZONTAL);
    }

    /** Add a focusable field one cell along the form's axis. */
    public Form addField(Section field) {
        return addField(field, Constraint.fixed(1));
    }

    /** Add a focusable field spanning {@code size} cells along the form's axis. */
    public Form addField(Section field, int size) {
        return addField(field, Constraint.fixed(size));
    }

    /** Add a focusable field sized by an explicit constraint (e.g. {@code Constraint.fill()}). */
    public Form addField(Section field, Constraint size) {
        add(size, field);
        fields.add(field);
        return this;
    }

    /** Add non-focusable decoration (e.g. a label) spanning {@code size} cells along the axis. */
    public Form addStatic(Section decoration, int size) {
        add(Constraint.fixed(size), decoration);
        return this;
    }

    /** Fired when ESC is pressed while the form has focus. */
    public Form onCancel(Runnable handler) {
        this.onCancel = handler;
        return this;
    }

    @Override
    protected KeyResult onKey(KeyEvent key) {
        if (key.is(Key.ESCAPE) && onCancel != null) {
            onCancel.run();
            return KeyResult.CONSUMED;
        }
        return KeyResult.UNHANDLED;
    }

    @Override
    public List<Section> focusTargets() {
        return Collections.unmodifiableList(fields);
    }

    @Override
    public Section activeFocusTarget() {
        return fields.isEmpty() ? null : fields.get(active);
    }

    @Override
    public boolean advanceFocus(boolean forward) {
        if (fields.isEmpty()) {
            return false;
        }
        Section field = fields.get(active);
        if (field instanceof FocusContainer && ((FocusContainer) field).advanceFocus(forward)) {
            return true;
        }
        if (fields.size() <= 1) {
            return false; // nothing of our own to move to; let the parent ring decide
        }
        int n = fields.size();
        active = (((active + (forward ? 1 : -1)) % n) + n) % n; // cycle within the form
        if (fields.get(active) instanceof Form) {
            ((Form) fields.get(active)).enter(forward);
        }
        return true;
    }

    /** Reset to the leading (forward) or trailing edge when focus enters this form. */
    void enter(boolean forward) {
        active = fields.isEmpty() ? 0 : (forward ? 0 : fields.size() - 1);
        if (!fields.isEmpty() && fields.get(active) instanceof Form) {
            ((Form) fields.get(active)).enter(forward);
        }
    }
}
