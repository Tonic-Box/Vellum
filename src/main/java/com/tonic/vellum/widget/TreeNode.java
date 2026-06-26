package com.tonic.vellum.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A node in a {@link TreeView}: a label, ordered children, and an expanded flag. */
public final class TreeNode {

    private final String label;
    private final List<TreeNode> children = new ArrayList<>();
    private boolean expanded;

    public TreeNode(String label) {
        this.label = label == null ? "" : label;
    }

    /** Add a child; returns this node for chaining siblings. */
    public TreeNode add(TreeNode child) {
        children.add(child);
        return this;
    }

    public String label() {
        return label;
    }

    public List<TreeNode> children() {
        return Collections.unmodifiableList(children);
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public boolean isExpanded() {
        return expanded;
    }

    public TreeNode expanded(boolean expanded) {
        this.expanded = expanded;
        return this;
    }

    void toggle() {
        expanded = !expanded;
    }
}
