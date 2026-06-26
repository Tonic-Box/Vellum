package com.tonic.vellum.layout;

import com.tonic.vellum.geom.Rect;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class LayoutSolverTest {

    private static int[] sizes(Rect available, List<Constraint> cs, Axis axis) {
        Rect[] rects = LayoutSolver.solve(available, cs, axis);
        int[] out = new int[rects.length];
        for (int i = 0; i < rects.length; i++) {
            out[i] = axis == Axis.VERTICAL ? rects[i].height() : rects[i].width();
        }
        return out;
    }

    @Test
    void fixedFillFixed() {
        List<Constraint> cs = Arrays.asList(Constraint.fixed(3), Constraint.fill(), Constraint.fixed(1));
        assertArrayEquals(new int[]{3, 6, 1}, sizes(new Rect(0, 0, 20, 10), cs, Axis.VERTICAL));
    }

    @Test
    void percentOfOriginalExtent() {
        List<Constraint> cs = Arrays.asList(Constraint.percent(50), Constraint.fill());
        assertArrayEquals(new int[]{5, 5}, sizes(new Rect(0, 0, 10, 10), cs, Axis.VERTICAL));
    }

    @Test
    void weightedFill() {
        List<Constraint> cs = Arrays.asList(Constraint.fill(1), Constraint.fill(3));
        assertArrayEquals(new int[]{2, 6}, sizes(new Rect(0, 0, 8, 8), cs, Axis.HORIZONTAL));
    }

    @Test
    void minGrowsFromFloor() {
        List<Constraint> cs = Arrays.asList(Constraint.min(4), Constraint.fill());
        assertArrayEquals(new int[]{7, 3}, sizes(new Rect(0, 0, 10, 10), cs, Axis.VERTICAL));
    }

    @Test
    void maxCapsAndRedistributes() {
        List<Constraint> cs = Arrays.asList(Constraint.max(3), Constraint.fill());
        assertArrayEquals(new int[]{3, 7}, sizes(new Rect(0, 0, 10, 10), cs, Axis.VERTICAL));
    }

    @Test
    void overSubscribedShrinksTrailing() {
        List<Constraint> cs = Arrays.asList(Constraint.fixed(8), Constraint.fixed(8));
        assertArrayEquals(new int[]{8, 2}, sizes(new Rect(0, 0, 10, 10), cs, Axis.HORIZONTAL));
    }

    @Test
    void horizontalProducesContiguousRects() {
        List<Constraint> cs = Arrays.asList(Constraint.fixed(5), Constraint.fill(), Constraint.fixed(4));
        Rect[] r = LayoutSolver.solve(new Rect(2, 1, 20, 6), cs, Axis.HORIZONTAL);
        assertEquals(2, r[0].x());
        assertEquals(r[0].right(), r[1].x());
        assertEquals(r[1].right(), r[2].x());
        assertEquals(22, r[2].right());
        for (Rect rect : r) {
            assertEquals(1, rect.y());
            assertEquals(6, rect.height());
        }
    }

    @Test
    void randomizedConstraintsAlwaysTileExactly() {
        Random rnd = new Random(42);
        for (int trial = 0; trial < 2000; trial++) {
            int extent = rnd.nextInt(60);
            int count = 1 + rnd.nextInt(6);
            List<Constraint> cs = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                switch (rnd.nextInt(6)) {
                    case 0: cs.add(Constraint.fixed(rnd.nextInt(15))); break;
                    case 1: cs.add(Constraint.percent(rnd.nextInt(60))); break;
                    case 2: cs.add(Constraint.fill(1 + rnd.nextInt(4))); break;
                    case 3: cs.add(Constraint.fill()); break;
                    case 4: cs.add(Constraint.min(rnd.nextInt(10))); break;
                    default: cs.add(Constraint.max(1 + rnd.nextInt(10))); break;
                }
            }
            Axis axis = rnd.nextBoolean() ? Axis.HORIZONTAL : Axis.VERTICAL;
            Rect available = new Rect(0, 0, axis == Axis.HORIZONTAL ? extent : 7,
                    axis == Axis.VERTICAL ? extent : 7);
            Rect[] rects = LayoutSolver.solve(available, cs, axis);

            int pos = axis == Axis.HORIZONTAL ? available.x() : available.y();
            for (Rect r : rects) {
                int start = axis == Axis.HORIZONTAL ? r.x() : r.y();
                int size = axis == Axis.HORIZONTAL ? r.width() : r.height();
                assertTrue(size >= 0, "negative size");
                assertEquals(pos, start, "gap or overlap");
                pos += size;
            }
            assertEquals(axis == Axis.HORIZONTAL ? available.right() : available.bottom(), pos,
                    "children must tile the full extent");
        }
    }

    @Test
    void constraintEqualityAndHashCode() {
        assertEquals(Constraint.fixed(3), Constraint.fixed(3));
        assertEquals(Constraint.fixed(3).hashCode(), Constraint.fixed(3).hashCode());
        assertNotEquals(Constraint.fixed(3), Constraint.fixed(4));
        assertNotEquals(Constraint.fixed(3), Constraint.percent(3)); // same value, different kind
    }
}
