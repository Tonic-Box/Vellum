package com.tonic.vellum.style;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StyleTest {

    @Test
    void settersAreImmutableAndReturnNewInstances() {
        Style base = Style.NORMAL;
        Style bold = base.bold(true);
        assertNotSame(base, bold);
        assertFalse(base.isBold());
        assertTrue(bold.isBold());
    }

    @Test
    void constantsCompose() {
        assertTrue(Style.REVERSE.isReverse());
        assertTrue(Style.DIM_REVERSE.isReverse());
        assertTrue(Style.DIM_REVERSE.isDim());
        assertTrue(Style.BOLD.isBold());
        assertTrue(Style.DIM.isDim());
        assertTrue(Style.UNDERLINE.isUnderline());
    }

    @Test
    void underlineIsIndependentAndImmutable() {
        Style u = Style.NORMAL.underline(true);
        assertTrue(u.isUnderline());
        assertFalse(Style.NORMAL.isUnderline());
        assertEquals(u, Style.NORMAL.underline(true));
        assertNotEquals(u, Style.NORMAL);
    }

    @Test
    void valueEquality() {
        Style a = Style.NORMAL.fg(Color.RED).bg(Color.BLUE).bold(true);
        Style b = Style.NORMAL.fg(Color.RED).bg(Color.BLUE).bold(true);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, a.dim(true));
    }

    @Test
    void colorSgr() {
        assertEquals("31", Color.RED.foregroundSgr());
        assertEquals("41", Color.RED.backgroundSgr());
        assertEquals("91", Color.BRIGHT_RED.foregroundSgr());
        assertEquals("101", Color.BRIGHT_RED.backgroundSgr());
        assertEquals("39", Color.DEFAULT.foregroundSgr());
        assertEquals("49", Color.DEFAULT.backgroundSgr());
        assertEquals("38;5;200", Color.ansi256(200).foregroundSgr());
        assertEquals("48;5;200", Color.ansi256(200).backgroundSgr());
        assertEquals("38;2;255;128;0", Color.rgb(255, 128, 0).foregroundSgr());
        assertEquals("48;2;255;128;0", Color.rgb(255, 128, 0).backgroundSgr());
    }

    @Test
    void colorValueEquality() {
        assertEquals(Color.rgb(10, 20, 30), Color.rgb(10, 20, 30));
        assertEquals(Color.rgb(10, 20, 30).hashCode(), Color.rgb(10, 20, 30).hashCode());
        assertNotEquals(Color.rgb(10, 20, 30), Color.rgb(10, 20, 31));
        assertNotEquals(Color.ansi256(1), Color.RED);
        assertEquals(Color.ansi256(99), Color.ansi256(99));
    }

    @Test
    void colorChannelsClamp() {
        assertEquals(Color.rgb(255, 255, 255), Color.rgb(999, 999, 999));
        assertEquals(Color.rgb(0, 0, 0), Color.rgb(-5, -1, -100));
        assertEquals(Color.ansi256(255), Color.ansi256(1000));
    }
}
