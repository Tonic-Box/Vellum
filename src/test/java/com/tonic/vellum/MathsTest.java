package com.tonic.vellum;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MathsTest {

    @Test
    void clampsInts() {
        assertEquals(5, Maths.clamp(5, 0, 10));
        assertEquals(0, Maths.clamp(-3, 0, 10));
        assertEquals(10, Maths.clamp(15, 0, 10));
    }

    @Test
    void clampsDoubles() {
        assertEquals(0.5, Maths.clamp(0.5, 0.0, 1.0), 1e-9);
        assertEquals(1.0, Maths.clamp(2.0, 0.0, 1.0), 1e-9);
        assertEquals(0.0, Maths.clamp(-1.0, 0.0, 1.0), 1e-9);
    }
}
