package com.tonic.vellum.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A node in a {@link TreeView}: a label, ordered children, and an expanded flag. */
public final class TreeNode {

    private final String label;
    private final List<TreeNode> children = new ArrayList<>();
    private boolean expanded;

    /**
     * Creates a leaf node with the given label.
     *
     * @param label the label text; treated as empty if {@code null}
     */
    public TreeNode(String label) {
        this.label = label == null ? "" : label;
    }

    /**
     * Appends a child node.
     *
     * @param child the child node to add
     * @return this TreeNode for chaining
     */
    public TreeNode add(TreeNode child) {
        children.add(child);
        return this;
    }

    /**
     * Returns this node's label.
     *
     * @return the label text
     */
    public String label() {
        return label;
    }

    /**
     * Returns this node's children in order.
     *
     * @return an unmodifiable list of the children
     */
    public List<TreeNode> children() {
        return Collections.unmodifiableList(children);
    }

    /**
     * Returns whether this node has no children.
     *
     * @return {@code true} if this node is a leaf
     */
    public boolean isLeaf() {
        return children.isEmpty();
    }

    /**
     * Returns whether this node is expanded.
     *
     * @return {@code true} if expanded
     */
    public boolean isExpanded() {
        return expanded;
    }

    /**
     * Sets whether this node is expanded.
     *
     * @param expanded {@code true} to expand, {@code false} to collapse
     * @return this TreeNode for chaining
     */
    public TreeNode expanded(boolean expanded) {
        this.expanded = expanded;
        return this;
    }

    void toggle() {
        expanded = !expanded;
    }
}
