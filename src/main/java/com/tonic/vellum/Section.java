package com.tonic.vellum;

import com.tonic.vellum.geom.Point;
import com.tonic.vellum.geom.Rect;
import com.tonic.vellum.input.KeyEvent;

import java.util.Collections;
import java.util.List;

/**
 * The single universal unit of a Vellum UI. Every visible thing is a Section; containers
 * are Sections that hold Sections. The framework assigns each section its bounds during
 * layout, clips a {@link Canvas} to those bounds, routes keys to the focused section, and
 * batches repaints of the dirty subtree.
 *
 * <p>Subclasses override {@link #render} (required) and any lifecycle/input hooks they
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
     */
    protected abstract void render(Canvas canvas);

    // ---- input ----

    /**
     * Handle a key. Return {@link KeyResult#CONSUMED} to stop propagation, or
     * {@link KeyResult#UNHANDLED} to let it bubble to the parent and focus manager.
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

    /** Bounds changed (layout or terminal resize). */
    protected void onResize(Rect newBounds) {}

    /**
     * The text cursor position in local coordinates, or {@code null} to keep it hidden.
     * Queried once per repaint for the focused section only (the deepest node on the focus
     * path). Override in editable widgets; default hides the cursor.
     */
    protected Point cursor() {
        return null;
    }

    // ---- user-callable API ----

    /** Mark this section dirty; the framework batches and repaints the dirty subtree. */
    public final void requestRedraw() {
        if (host != null) {
            host.assertUiThread();
        }
        dirty = true;
        if (host != null) {
            host.requestRepaint();
        }
    }

    /** Mark a child (and its entire live subtree) dirty, e.g. when re-showing a hidden subtree. */
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

    /** True if this section currently holds input focus (is on the focus path). */
    public final boolean isFocused() {
        return host != null && host.isOnFocusPath(this);
    }

    public final Rect bounds() {
        return bounds;
    }

    public final Section parent() {
        return parent;
    }

    /** Convenience: wrap this section in a {@link com.tonic.vellum.widget.BorderSection}. */
    public final Section bordered() {
        return new com.tonic.vellum.widget.BorderSection(this);
    }

    /** Convenience: wrap this section in a titled border. */
    public final Section bordered(String title) {
        return new com.tonic.vellum.widget.BorderSection(this).title(title);
    }

    // ---- container API (for subclasses that own children) ----

    /**
     * The children on this section's live render path. Default: none. Containers override
     * this; the framework walks it for layout, mounting, repainting, and focus. A
     * {@code TabHost} returns only its active child, which is why only the active tab is
     * ever "live".
     */
    protected List<Section> children() {
        return Collections.emptyList();
    }

    /**
     * Assign bounds to a child and link it as this section's child. When the bounds change
     * the child is marked dirty and {@link #onResize} fires, cascading layout downward.
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
