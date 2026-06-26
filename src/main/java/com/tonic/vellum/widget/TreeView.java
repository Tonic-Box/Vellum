package com.tonic.vellum.widget;

import com.tonic.vellum.Canvas;
import com.tonic.vellum.KeyResult;
import com.tonic.vellum.input.KeyEvent;
import com.tonic.vellum.style.Style;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A scrollable, navigable tree. The visible (expanded) nodes are flattened to rows; arrows
 * move the cursor, ENTER/SPACE toggle a parent or select a leaf, and LEFT/RIGHT collapse and
 * expand the node under the cursor. Built on {@link AbstractListSection}.
 */
public final class TreeView extends AbstractListSection {

    private static final class Entry {
        final TreeNode node;
        final int depth;

        Entry(TreeNode node, int depth) {
            this.node = node;
            this.depth = depth;
        }
    }

    private final TreeNode root;
    private final boolean showRoot;
    private List<Entry> visible = new ArrayList<>();
    private boolean stale = true;
    private Consumer<TreeNode> onSelectNode = node -> { };

    /**
     * Creates a tree view showing the root node.
     *
     * @param root the root node of the tree
     */
    public TreeView(TreeNode root) {
        this(root, true);
    }

    /**
     * Creates a tree view, optionally hiding the root node.
     *
     * @param root the root node of the tree
     * @param showRoot {@code true} to show the root row, {@code false} to show only its children
     */
    public TreeView(TreeNode root, boolean showRoot) {
        this.root = root;
        this.showRoot = showRoot;
    }

    /**
     * Sets the handler invoked with a leaf node when it is activated.
     *
     * @param handler consumer receiving the activated leaf node
     * @return this TreeView for chaining
     */
    public TreeView onSelectNode(Consumer<TreeNode> handler) {
        this.onSelectNode = handler;
        return this;
    }

    /**
     * Re-flattens the visible rows after the tree is mutated outside the view.
     *
     * @return this TreeView for chaining
     */
    public TreeView refresh() {
        stale = true;
        requestRedraw();
        return this;
    }

    /**
     * Returns the node under the cursor.
     *
     * @return the selected node, or {@code null} if there is no selection
     */
    public TreeNode selectedNode() {
        List<Entry> rows = entries();
        int i = selectedIndex();
        return i >= 0 && i < rows.size() ? rows.get(i).node : null;
    }

    /**
     * Returns the number of visible rows.
     */
    @Override
    protected int rowCount() {
        return entries().size();
    }

    /**
     * Renders the tree row at the given index with indentation and an expand marker.
     */
    @Override
    protected void renderRow(Canvas row, int index, Style style) {
        Entry entry = entries().get(index);
        StringBuilder sb = new StringBuilder();
        for (int d = 0; d < entry.depth; d++) {
            sb.append("  ");
        }
        sb.append(entry.node.isLeaf() ? "  " : entry.node.isExpanded() ? "v " : "> ");
        sb.append(entry.node.label());
        row.put(0, 0, sb.toString(), style);
    }

    /**
     * Selects the row's leaf node or toggles its parent node when activated.
     */
    @Override
    protected void onActivate(int index) {
        TreeNode node = entries().get(index).node;
        if (node.isLeaf()) {
            onSelectNode.accept(node);
        } else {
            node.toggle();
            stale = true;
            requestRedraw();
        }
    }

    /**
     * Handles RIGHT and LEFT to expand and collapse; other keys defer to the base (arrows,
     * ENTER/SPACE activation).
     */
    @Override
    protected KeyResult onKey(KeyEvent key) {
        switch (key.code()) {
            case RIGHT:
                setExpanded(true);
                return KeyResult.CONSUMED;
            case LEFT:
                setExpanded(false);
                return KeyResult.CONSUMED;
            default:
                return super.onKey(key);
        }
    }

    private void setExpanded(boolean expand) {
        TreeNode node = selectedNode();
        if (node != null && !node.isLeaf() && node.isExpanded() != expand) {
            node.expanded(expand);
            stale = true;
            requestRedraw();
        }
    }

    private List<Entry> entries() {
        if (stale) {
            visible = new ArrayList<>();
            if (showRoot) {
                flatten(root, 0);
            } else {
                for (TreeNode child : root.children()) {
                    flatten(child, 0);
                }
            }
            stale = false;
        }
        return visible;
    }

    private void flatten(TreeNode node, int depth) {
        visible.add(new Entry(node, depth));
        if (node.isExpanded()) {
            for (TreeNode child : node.children()) {
                flatten(child, depth + 1);
            }
        }
    }
}
