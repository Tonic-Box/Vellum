package com.tonic.vellum.geom;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RectTest {

    @Test
    void edgesAndContainment() {
        Rect r = new Rect(2, 3, 10, 5);
        assertEquals(12, r.right());
        assertEquals(8, r.bottom());
        assertTrue(r.contains(2, 3));
        assertTrue(r.contains(11, 7));
        assertFalse(r.contains(12, 7));
        assertFalse(r.contains(1, 3));
    }

    @Test
    void negativeDimensionsClampToZero() {
        Rect r = new Rect(0, 0, -4, -1);
        assertTrue(r.isEmpty());
        assertEquals(0, r.width());
        assertEquals(0, r.height());
    }

    @Test
    void insetShrinksAllSides() {
        Rect r = new Rect(0, 0, 10, 10).inset(2);
        assertEquals(new Rect(2, 2, 6, 6), r);
    }

    @Test
    void insetClampsWhenLargerThanRect() {
        Rect r = new Rect(0, 0, 4, 4).inset(3);
        assertTrue(r.isEmpty());
    }

    @Test
    void takeStripsAreClamped() {
        Rect r = new Rect(0, 0, 8, 6);
        assertEquals(new Rect(0, 0, 8, 2), r.takeTop(2));
        assertEquals(new Rect(0, 4, 8, 2), r.takeBottom(2));
        assertEquals(new Rect(0, 0, 3, 6), r.takeLeft(3));
        assertEquals(new Rect(5, 0, 3, 6), r.takeRight(3));
        assertEquals(new Rect(0, 0, 8, 6), r.takeTop(100));
    }

    @Test
    void splitsTileExactlyWithNoGapOrOverlap() {
        Rect r = new Rect(1, 1, 8, 6);

        Rect[] v = r.splitTop(2);
        assertEquals(new Rect(1, 1, 8, 2), v[0]);
        assertEquals(new Rect(1, 3, 8, 4), v[1]);
        assertEquals(r.height(), v[0].height() + v[1].height());

        Rect[] h = r.splitLeft(3);
        assertEquals(new Rect(1, 1, 3, 6), h[0]);
        assertEquals(new Rect(4, 1, 5, 6), h[1]);
        assertEquals(r.width(), h[0].width() + h[1].width());

        Rect[] b = r.splitBottom(2);
        assertEquals(new Rect(1, 5, 8, 2), b[0]);
        assertEquals(new Rect(1, 1, 8, 4), b[1]);

        Rect[] rt = r.splitRight(3);
        assertEquals(new Rect(6, 1, 3, 6), rt[0]);
        assertEquals(new Rect(1, 1, 5, 6), rt[1]);
    }

    @Test
    void equalityAndHashCode() {
        assertEquals(new Rect(1, 2, 3, 4), new Rect(1, 2, 3, 4));
        assertEquals(new Rect(1, 2, 3, 4).hashCode(), new Rect(1, 2, 3, 4).hashCode());
        assertNotEquals(new Rect(1, 2, 3, 4), new Rect(1, 2, 3, 5));
    }

    @Test
    void intersectClampsToOverlap() {
        Rect a = new Rect(0, 0, 10, 10);
        assertEquals(new Rect(2, 3, 4, 5), a.intersect(new Rect(2, 3, 4, 5)));   // contained
        assertEquals(new Rect(8, 8, 2, 2), a.intersect(new Rect(8, 8, 10, 10))); // partial overlap
        assertTrue(a.intersect(new Rect(20, 20, 5, 5)).isEmpty());               // disjoint
    }
}
