package com.tonic.vellum.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A node in a TreeView: a label, ordered children, and an expanded flag.
 */
public final class TreeNode
{
    private final String label;
    private final List<TreeNode> children = new ArrayList<>();
    private boolean expanded;

    /**
     * Creates a leaf node with the given label.
     *
     * @param label the label text; treated as empty if {@code null}
     */
    public TreeNode(String label)
    {
        this.label = label == null ? "" : label;
    }

    /**
     * Appends a child node.
     *
     * @param child the child node to add
     * @return this TreeNode for chaining
     */
    public TreeNode add(TreeNode child)
    {
        children.add(child);
        return this;
    }

    /**
     * @return the label text
     */
    public String label()
    {
        return label;
    }

    /**
     * @return an unmodifiable list of the children, in order
     */
    public List<TreeNode> children()
    {
        return Collections.unmodifiableList(children);
    }

    /**
     * @return {@code true} if this node has no children
     */
    public boolean isLeaf()
    {
        return children.isEmpty();
    }

    /**
     * @return {@code true} if expanded
     */
    public boolean isExpanded()
    {
        return expanded;
    }

    /**
     * Sets whether this node is expanded.
     *
     * @param expanded the new state
     * @return this TreeNode for chaining
     */
    public TreeNode expanded(boolean expanded)
    {
        this.expanded = expanded;
        return this;
    }

    void toggle()
    {
        expanded = !expanded;
    }
}
