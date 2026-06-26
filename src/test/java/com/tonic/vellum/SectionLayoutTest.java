package com.tonic.vellum;

import com.tonic.vellum.geom.Rect;
import com.tonic.vellum.layout.Constraint;
import com.tonic.vellum.layout.Slot;
import com.tonic.vellum.layout.Split;
import com.tonic.vellum.style.Style;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SectionLayoutTest {

    /** A leaf that fills its bounds with a single character and counts renders. */
    static final class Box extends Section {
        final char c;
        int renders;

        Box(char c) { this.c = c; }

        @Override
        protected void render(Canvas canvas) {
            renders++;
            canvas.fill(canvas.bounds(), c, Style.NORMAL);
        }
    }

    /** Recursively render a tree the way the run loop will (pre-order, dirty-agnostic). */
    private static void renderTree(Section s, Buffer buf) {
        s.renderInto(buf);
        for (Section child : s.children()) {
            renderTree(child, buf);
        }
    }

    private static String row(Buffer buf, int y) {
        StringBuilder sb = new StringBuilder();
        for (int x = 0; x < buf.width(); x++) {
            sb.append((char) buf.codePointAt(x, y));
        }
        return sb.toString();
    }

    @Test
    void splitAssignsChildBoundsOnResize() {
        Box a = new Box('A');
        Box b = new Box('B');
        Section root = Split.vertical(
                Slot.of(Constraint.fixed(2), a),
                Slot.of(Constraint.fill(), b));

        root.resizeRoot(new Rect(0, 0, 4, 5));

        assertEquals(new Rect(0, 0, 4, 2), a.bounds());
        assertEquals(new Rect(0, 2, 4, 3), b.bounds());
    }

    @Test
    void layoutCascadesThroughNestedSplits() {
        Box menu = new Box('M');
        Box detail = new Box('D');
        Box header = new Box('H');
        Section body = Split.horizontal(
                Slot.of(Constraint.fixed(3), menu),
                Slot.of(Constraint.fill(), detail));
        Section root = Split.vertical(
                Slot.of(Constraint.fixed(1), header),
                Slot.of(Constraint.fill(), body));

        root.resizeRoot(new Rect(0, 0, 10, 4));

        assertEquals(new Rect(0, 0, 10, 1), header.bounds());
        assertEquals(new Rect(0, 1, 10, 3), body.bounds());
        assertEquals(new Rect(0, 1, 3, 3), menu.bounds());
        assertEquals(new Rect(3, 1, 7, 3), detail.bounds());
    }

    @Test
    void renderTreePaintsLeavesClippedToBounds() {
        Box a = new Box('A');
        Box b = new Box('B');
        Section root = Split.vertical(
                Slot.of(Constraint.fixed(2), a),
                Slot.of(Constraint.fill(), b));
        root.resizeRoot(new Rect(0, 0, 4, 5));

        Buffer buf = new Buffer(4, 5);
        renderTree(root, buf);

        assertEquals("AAAA", row(buf, 0));
        assertEquals("AAAA", row(buf, 1));
        assertEquals("BBBB", row(buf, 2));
        assertEquals("BBBB", row(buf, 4));
    }

    @Test
    void requestRedrawWithoutHostDoesNotThrow() {
        Box a = new Box('A');
        a.requestRedraw();
        assertTrue(a.isDirty());
        assertFalse(a.isFocused());
    }

    @Test
    void childrenReflectsConstructionOrder() {
        Box a = new Box('A');
        Box b = new Box('B');
        Section root = Split.horizontal(
                Slot.of(Constraint.fill(), a),
                Slot.of(Constraint.fill(), b));
        List<Section> kids = root.children();
        assertEquals(2, kids.size());
        assertSame(a, kids.get(0));
        assertSame(b, kids.get(1));
    }
}
