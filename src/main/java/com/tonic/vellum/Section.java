package com.tonic.vellum;

import com.tonic.vellum.geom.Point;
import com.tonic.vellum.geom.Rect;
import com.tonic.vellum.input.KeyEvent;

import java.util.Collections;
import java.util.List;

/**
 * The base unit of a Vellum UI. Every visible element is a Section, and containers are
 * Sections that hold other Sections. The framework assigns each section its bounds during
 * layout, clips a {@link Canvas} to those bounds, routes keys to the focused section, and
 * repaints the dirty subtree.
 *
 * <p>Subclasses override {@link #render} (required) and any lifecycle or input hooks they
 * need. Container subclasses additionally override {@link #children()} and lay out their
 * children with {@link #place(Section, Rect)}.
 */
public abstract class Section {

    private Rect bounds = new Rect(0, 0, 0, 0);
    private Section parent;
    private boolean dirty = true;
    private boolean mounted;
    private Host host;

    // ---- rendering ----

    /**
     * Draw this section. The canvas is already clipped to this section's bounds and uses
     * local coordinates ({@code (0,0)} = top-left). Out-of-bounds writes are discarded.
     *
     * @param canvas the clipped drawing surface for this section
     */
    protected abstract void render(Canvas canvas);

    // ---- input ----

    /**
     * Handle a key.
     *
     * @param key the key event to handle
     * @return {@link KeyResult#CONSUMED} to stop propagation, or {@link KeyResult#UNHANDLED}
     *         to let it bubble to the parent and focus manager
     */
    protected KeyResult onKey(KeyEvent key) {
        return KeyResult.UNHANDLED;
    }

    // ---- lifecycle hooks (UI thread only; default no-op) ----

    /** Became part of the live render path / shown. */
    protected void onMount() {}

    /** Removed from the live render path / hidden. */
    protected void onUnmount() {}

    /** This section received focus. */
    protected void onFocusGained() {}

    /** This section lost focus. */
    protected void onFocusLost() {}

    /**
     * Called when the bounds change (layout or terminal resize).
     *
     * @param newBounds the section's new bounds
     */
    protected void onResize(Rect newBounds) {}

    /**
     * Return the text cursor position in local coordinates, or {@code null} to keep it
     * hidden. Queried once per repaint for the focused section only (the deepest node on
     * the focus path). Override in editable widgets; default hides the cursor.
     *
     * @return the cursor position in local coordinates, or {@code null} to hide it
     */
    protected Point cursor() {
        return null;
    }

    // ---- user-callable API ----

    /**
     * Mark this section dirty; the framework batches and repaints the dirty subtree.
     *
     * @throws IllegalStateException if called off the UI thread
     */
    public final void requestRedraw() {
        if (host != null) {
            host.assertUiThread();
        }
        dirty = true;
        if (host != null) {
            host.requestRepaint();
        }
    }

    /**
     * Mark a child (and its entire live subtree) dirty, e.g. when re-showing a hidden subtree.
     *
     * @param target the subtree root to mark dirty
     * @throws IllegalStateException if called off the UI thread
     */
    protected final void redrawSubtree(Section target) {
        if (host != null) {
            host.assertUiThread();
        }
        markSubtreeDirty(target);
        if (host != null) {
            host.requestRepaint();
        }
    }

    private static void markSubtreeDirty(Section section) {
        section.dirty = true;
        for (Section child : section.children()) {
            markSubtreeDirty(child);
        }
    }

    /**
     * Notify the framework that this container changed its active focus target so the focus
     * path, key routing, and cursor track the new target. Call on the UI thread after the
     * change (containers implementing {@link com.tonic.vellum.focus.FocusContainer}).
     *
     * @throws IllegalStateException if called off the UI thread
     */
    protected final void refreshFocus() {
        if (host != null) {
            host.assertUiThread();
            host.refreshFocus();
        }
    }

    /**
     * Report whether this section currently holds input focus (is on the focus path).
     *
     * @return {@code true} if this section is on the focus path
     */
    public final boolean isFocused() {
        return host != null && host.isOnFocusPath(this);
    }

    /**
     * Return this section's current bounds.
     *
     * @return the section's bounds
     */
    public final Rect bounds() {
        return bounds;
    }

    /**
     * Return this section's parent.
     *
     * @return the parent section, or {@code null} if this is the root or unattached
     */
    public final Section parent() {
        return parent;
    }

    /**
     * Wrap this section in a {@link com.tonic.vellum.widget.BorderSection}.
     *
     * @return the wrapping bordered section
     */
    public final Section bordered() {
        return new com.tonic.vellum.widget.BorderSection(this);
    }

    /**
     * Wrap this section in a titled border.
     *
     * @param title the border title
     * @return the wrapping bordered section
     */
    public final Section bordered(String title) {
        return new com.tonic.vellum.widget.BorderSection(this).title(title);
    }

    // ---- container API (for subclasses that own children) ----

    /**
     * Return the children on this section's live render path. Default: none. Containers
     * override this; the framework walks it for layout, mounting, repainting, and focus.
     * A {@code TabHost} returns only its active child, so only the active tab is live.
     *
     * @return the live child sections; never {@code null}
     */
    protected List<Section> children() {
        return Collections.emptyList();
    }

    /**
     * Assign bounds to a child and link it as this section's child. When the bounds change
     * the child is marked dirty and {@link #onResize} fires, cascading layout downward.
     *
     * @param child the child section to place
     * @param childBounds the bounds to assign to the child
     */
    protected final void place(Section child, Rect childBounds) {
        child.parent = this;
        boolean changed = !childBounds.equals(child.bounds);
        child.bounds = childBounds;
        if (changed) {
            child.dirty = true;
            child.onResize(childBounds);
        }
    }

    /**
     * Bring a child (and its subtree) onto the live render path: attach the host, set the
     * mounted flag, fire {@link #onMount}, and recurse. Used by containers that show
     * children on demand (e.g. {@code TabHost.select}).
     *
     * @param child the child section to mount
     */
    protected final void mount(Section child) {
        child.parent = this;
        child.attach(host);
        child.mounted = true;
        child.onMount();
        for (Section grandchild : child.children()) {
            child.mount(grandchild);
        }
    }

    /**
     * Remove a child (and its subtree) from the live render path: fire {@link #onUnmount}
     * depth-first and clear the mounted flag. The instance is retained.
     *
     * @param child the child section to unmount
     */
    protected final void unmount(Section child) {
        for (Section grandchild : child.children()) {
            child.unmount(grandchild);
        }
        if (child.mounted) {
            child.mounted = false;
            child.onUnmount();
        }
    }

    // ---- engine seam (package-private; invoked by the kernel only) ----

    /** Attach the engine host to this section (propagates on mount). */
    void attach(Host host) {
        this.host = host;
    }

    /** Mount this section as the root of the live tree: attach, mark, fire onMount, recurse. */
    void mountAsRoot(Host host) {
        attach(host);
        this.mounted = true;
        onMount();
        for (Section child : children()) {
            mount(child);
        }
    }

    /** Unmount the live tree (children depth-first, then this root) at shutdown. */
    void unmountAsRoot() {
        for (Section child : children()) {
            unmount(child);
        }
        if (mounted) {
            mounted = false;
            onUnmount();
        }
    }

    /** Set this section's bounds (root only) and cascade layout via onResize. */
    void resizeRoot(Rect newBounds) {
        this.bounds = newBounds;
        this.dirty = true;
        onResize(newBounds);
    }

    boolean isDirty() {
        return dirty;
    }

    void markDirty() {
        this.dirty = true;
    }

    /** Render this section into the back buffer, clearing its rect first if it is a leaf. */
    void renderInto(Buffer buffer) {
        try {
            if (children().isEmpty()) {
                buffer.clearRect(bounds);
            }
            render(new ClippedCanvas(buffer, bounds));
        } finally {
            dirty = false; // clear even on failure so a broken render does not spin every frame
        }
    }

    KeyResult dispatchKey(KeyEvent key) {
        return onKey(key);
    }

    void dispatchFocusGained() {
        onFocusGained();
    }

    void dispatchFocusLost() {
        onFocusLost();
    }
}
