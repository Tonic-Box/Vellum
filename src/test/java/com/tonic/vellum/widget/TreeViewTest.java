package com.tonic.vellum.widget;

import com.tonic.vellum.input.Key;
import com.tonic.vellum.input.KeyEvent;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class TreeViewTest {

    private static TreeView sampleTree() {
        TreeNode a = new TreeNode("a").add(new TreeNode("a1")).add(new TreeNode("a2"));
        TreeNode root = new TreeNode("root").add(a).add(new TreeNode("b")).expanded(true);
        return new TreeView(root);
    }

    @Test
    void flattensVisibleNodes() {
        TreeView tree = sampleTree();
        // root expanded, a and b collapsed -> root, a, b
        assertEquals(3, tree.rowCount());
    }

    @Test
    void activatingAParentExpandsAndReflattens() {
        TreeView tree = sampleTree();
        tree.select(1); // node "a"
        tree.onKey(KeyEvent.special(Key.ENTER)); // expand a
        assertEquals(5, tree.rowCount()); // root, a, a1, a2, b

        tree.onKey(KeyEvent.special(Key.LEFT)); // collapse a
        assertEquals(3, tree.rowCount());
    }

    @Test
    void rightExpandsLeftCollapses() {
        TreeView tree = sampleTree();
        tree.select(1);
        tree.onKey(KeyEvent.special(Key.RIGHT));
        assertEquals(5, tree.rowCount());
        tree.onKey(KeyEvent.special(Key.LEFT));
        assertEquals(3, tree.rowCount());
    }

    @Test
    void activatingALeafFiresCallback() {
        AtomicReference<String> picked = new AtomicReference<>();
        TreeView tree = sampleTree().onSelectNode(node -> picked.set(node.label()));
        tree.select(2); // node "b" (a leaf)
        tree.onKey(KeyEvent.special(Key.ENTER));
        assertEquals("b", picked.get());
    }

    @Test
    void rendersIndentAndMarkers() {
        TreeView tree = sampleTree();
        RecordingCanvas c = new RecordingCanvas(20, 3);
        tree.render(c);

        assertEquals('v', c.charAt(0, 0)); // root expanded marker
        assertEquals('r', c.charAt(2, 0)); // "v root"
        assertEquals('>', c.charAt(2, 1)); // child "a" collapsed, indented by 2
        assertEquals('a', c.charAt(4, 1));
    }
}
