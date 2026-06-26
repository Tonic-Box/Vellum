package com.tonic.vellum.widget;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ViewportTest {

    @Test
    void maxTopAccountsForViewportAndCount() {
        assertEquals(0, Viewport.maxTop(3, 5));   // fewer rows than the viewport
        assertEquals(7, Viewport.maxTop(10, 3));  // 10 - 3
        assertEquals(9, Viewport.maxTop(10, 0));  // viewport treated as at least 1
    }

    @Test
    void setAndScrollClampToRange() {
        Viewport v = new Viewport();
        v.set(100, 10, 3);
        assertEquals(7, v.top());
        v.set(-5, 10, 3);
        assertEquals(0, v.top());
        v.scrollBy(2, 10, 3);
        assertEquals(2, v.top());
        v.toBottom(10, 3);
        assertEquals(7, v.top());
    }

    @Test
    void ensureVisibleScrollsMinimally() {
        Viewport v = new Viewport();
        v.ensureVisible(5, 10, 3); // bring index 5 into a 3-row viewport
        assertEquals(3, v.top());
        v.ensureVisible(4, 10, 3); // already visible (3..5)
        assertEquals(3, v.top());
        v.ensureVisible(0, 10, 3); // above the window
        assertEquals(0, v.top());
    }
}
