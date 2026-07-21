package com.tonic.vellum;

import com.tonic.vellum.geom.Point;
import com.tonic.vellum.geom.Rect;
import com.tonic.vellum.input.KeyEvent;

import java.util.Collections;
import java.util.List;

/**
 * The base unit of a Vellum UI: every visible element is a Section, and containers are
 * Sections that hold other Sections. The framework assigns bounds during layout, routes
 * keys to the focused section, and repaints dirty subtrees.
 */
public abstract class Section
{
    private Rect bounds = new Rect(0, 0, 0, 0);
    private Section parent;
    private boolean dirty = true;
    private boolean mounted;
    private Host host;

    /**
     * Draws this section. The canvas is already clipped to this section's bounds and uses
     * local coordinates ((0,0) = top-left); out-of-bounds writes are discarded.
     *
     * @param canvas the clipped drawing surface for this section
     */
    protected abstract void render(Canvas canvas);

    /**
     * Handles a key.
     *
     * @param key the key event to handle
     * @return CONSUMED to stop propagation, or UNHANDLED to let it bubble to the parent
     *         and focus manager
     */
    protected KeyResult onKey(KeyEvent key)
    {
        return KeyResult.UNHANDLED;
    }

    /**
     * Called when this section joins the live render path.
     */
    protected void onMount()
    {
    }

    /**
     * Called when this section leaves the live render path.
     */
    protected void onUnmount()
    {
    }

    /**
     * Called when this section gains focus.
     */
    protected void onFocusGained()
    {
    }

    /**
     * Called when this section loses focus.
     */
    protected void onFocusLost()
    {
    }

    /**
     * Called when the bounds change (layout or terminal resize).
     *
     * @param newBounds the section's new bounds
     */
    protected void onResize(Rect newBounds)
    {
    }

    /**
     * Returns the text cursor position, queried once per repaint for the focused section
     * only. Override in editable widgets; the default hides the cursor.
     *
     * @return the cursor position in local coordinates, or null to hide it
     */
    protected Point cursor()
    {
        return null;
    }

    /**
     * Marks this section dirty; the framework batches and repaints the dirty subtree.
     *
     * @throws IllegalStateException if called off the UI thread
     */
    public final void requestRedraw()
    {
        if (host != null)
        {
            host.assertUiThread();
        }
        dirty = true;
        if (host != null)
        {
            host.requestRepaint();
        }
    }

    /**
     * Marks a child and its entire live subtree dirty, e.g. when re-showing a hidden subtree.
     *
     * @param target the subtree root to mark dirty
     * @throws IllegalStateException if called off the UI thread
     */
    protected final void redrawSubtree(Section target)
    {
        if (host != null)
        {
            host.assertUiThread();
        }
        markSubtreeDirty(target);
        if (host != null)
        {
            host.requestRepaint();
        }
    }

    private static void markSubtreeDirty(Section section)
    {
        section.dirty = true;
        for (Section child : section.children())
        {
            markSubtreeDirty(child);
        }
    }

    /**
     * Notifies the framework that this container changed its active focus target so the
     * focus path, key routing, and cursor track the new target. Call on the UI thread
     * after the change.
     *
     * @throws IllegalStateException if called off the UI thread
     */
    protected final void refreshFocus()
    {
        if (host != null)
        {
            host.assertUiThread();
            host.refreshFocus();
        }
    }

    /**
     * @return true if this section is on the focus path
     */
    public final boolean isFocused()
    {
        return host != null && host.isOnFocusPath(this);
    }

    /**
     * @return this section's current bounds
     */
    public final Rect bounds()
    {
        return bounds;
    }

    /**
     * @return the parent section, or null if this is the root or unattached
     */
    public final Section parent()
    {
        return parent;
    }

    /**
     * Wraps this section in a border.
     *
     * @return the wrapping bordered section
     */
    public final Section bordered()
    {
        return new com.tonic.vellum.widget.BorderSection(this);
    }

    /**
     * Wraps this section in a titled border.
     *
     * @param title the border title
     * @return the wrapping bordered section
     */
    public final Section bordered(String title)
    {
        return new com.tonic.vellum.widget.BorderSection(this).title(title);
    }

    /**
     * Returns the children on this section's live render path; containers override this.
     * The framework walks it for layout, mounting, repainting, and focus.
     *
     * @return the live child sections; never null
     */
    protected List<Section> children()
    {
        return Collections.emptyList();
    }

    /**
     * Assigns bounds to a child and links it as this section's child. When the bounds
     * change the child is marked dirty and onResize fires, cascading layout downward.
     *
     * @param child the child section to place
     * @param childBounds the bounds to assign to the child
     */
    protected final void place(Section child, Rect childBounds)
    {
        child.parent = this;
        boolean changed = !childBounds.equals(child.bounds);
        child.bounds = childBounds;
        if (changed)
        {
            child.dirty = true;
            child.onResize(childBounds);
        }
    }

    /**
     * Brings a child and its subtree onto the live render path: attaches the host, sets
     * the mounted flag, fires onMount, and recurses. Used by containers that show
     * children on demand.
     *
     * @param child the child section to mount
     */
    protected final void mount(Section child)
    {
        child.parent = this;
        child.attach(host);
        child.mounted = true;
        child.onMount();
        for (Section grandchild : child.children())
        {
            child.mount(grandchild);
        }
    }

    /**
     * Removes a child and its subtree from the live render path: fires onUnmount
     * depth-first and clears the mounted flag. The instance is retained.
     *
     * @param child the child section to unmount
     */
    protected final void unmount(Section child)
    {
        for (Section grandchild : child.children())
        {
            child.unmount(grandchild);
        }
        if (child.mounted)
        {
            child.mounted = false;
            child.onUnmount();
        }
    }

    void attach(Host host)
    {
        this.host = host;
    }

    void mountAsRoot(Host host)
    {
        attach(host);
        this.mounted = true;
        onMount();
        for (Section child : children())
        {
            mount(child);
        }
    }

    void unmountAsRoot()
    {
        for (Section child : children())
        {
            unmount(child);
        }
        if (mounted)
        {
            mounted = false;
            onUnmount();
        }
    }

    void resizeRoot(Rect newBounds)
    {
        this.bounds = newBounds;
        this.dirty = true;
        onResize(newBounds);
    }

    boolean isDirty()
    {
        return dirty;
    }

    void markDirty()
    {
        this.dirty = true;
    }

    void renderInto(Buffer buffer)
    {
        try
        {
            if (children().isEmpty())
            {
                buffer.clearRect(bounds);
            }
            render(new ClippedCanvas(buffer, bounds));
        }
        finally
        {
            dirty = false;
        }
    }

    KeyResult dispatchKey(KeyEvent key)
    {
        return onKey(key);
    }

    void dispatchFocusGained()
    {
        onFocusGained();
    }

    void dispatchFocusLost()
    {
        onFocusLost();
    }
}
