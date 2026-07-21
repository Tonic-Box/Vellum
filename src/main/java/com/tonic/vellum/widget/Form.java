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
 * A stack of fields with internal Tab traversal: Tab and Shift-Tab cycle the fields,
 * descending depth-first into nested forms. ESC fires the cancel handler.
 */
public final class Form extends Split implements FocusContainer
{
    private final List<Section> fields = new ArrayList<>();
    private int active;
    private Runnable onCancel;

    /**
     * Creates a vertical form whose fields are stacked top to bottom.
     */
    public Form()
    {
        super(Axis.VERTICAL);
    }

    private Form(Axis axis)
    {
        super(axis);
    }

    /**
     * @return a new horizontal form, fields laid out left to right
     */
    public static Form row()
    {
        return new Form(Axis.HORIZONTAL);
    }

    /**
     * Adds a focusable field spanning one cell along the form's axis.
     *
     * @param field the field to add
     * @return this Form for chaining
     */
    public Form addField(Section field)
    {
        return addField(field, Constraint.fixed(1));
    }

    /**
     * Adds a focusable field spanning a fixed number of cells along the form's axis.
     *
     * @param field the field to add
     * @param size the number of cells to span
     * @return this Form for chaining
     */
    public Form addField(Section field, int size)
    {
        return addField(field, Constraint.fixed(size));
    }

    /**
     * Adds a focusable field sized by an explicit layout constraint.
     *
     * @param field the field to add
     * @param size the constraint sizing the field along the form's axis
     * @return this Form for chaining
     */
    public Form addField(Section field, Constraint size)
    {
        add(size, field);
        fields.add(field);
        return this;
    }

    /**
     * Adds non-focusable decoration (such as a label) spanning a fixed number of cells.
     *
     * @param decoration the section to add
     * @param size the number of cells to span
     * @return this Form for chaining
     */
    public Form addStatic(Section decoration, int size)
    {
        add(Constraint.fixed(size), decoration);
        return this;
    }

    /**
     * Sets the handler run when ESC is pressed while the form has focus.
     *
     * @param handler the cancel handler
     * @return this Form for chaining
     */
    public Form onCancel(Runnable handler)
    {
        this.onCancel = handler;
        return this;
    }

    @Override
    protected KeyResult onKey(KeyEvent key)
    {
        if (key.is(Key.ESCAPE) && onCancel != null)
        {
            onCancel.run();
            return KeyResult.CONSUMED;
        }
        return KeyResult.UNHANDLED;
    }

    @Override
    public List<Section> focusTargets()
    {
        return Collections.unmodifiableList(fields);
    }

    @Override
    public Section activeFocusTarget()
    {
        return fields.isEmpty() ? null : fields.get(active);
    }

    @Override
    public boolean advanceFocus(boolean forward)
    {
        if (fields.isEmpty())
        {
            return false;
        }
        Section field = fields.get(active);
        if (field instanceof FocusContainer && ((FocusContainer) field).advanceFocus(forward))
        {
            return true;
        }
        if (fields.size() <= 1)
        {
            return false;
        }
        int n = fields.size();
        active = (((active + (forward ? 1 : -1)) % n) + n) % n;
        if (fields.get(active) instanceof Form)
        {
            ((Form) fields.get(active)).enter(forward);
        }
        return true;
    }

    void enter(boolean forward)
    {
        active = fields.isEmpty() ? 0 : (forward ? 0 : fields.size() - 1);
        if (!fields.isEmpty() && fields.get(active) instanceof Form)
        {
            ((Form) fields.get(active)).enter(forward);
        }
    }
}
