package com.tonic.vellum.input;

/** Logical key identity. Printable input is reported as {@link #CHAR} with the char in {@code ch()}. */
public enum Key {
    UP, DOWN, LEFT, RIGHT,
    ENTER, ESCAPE, TAB, SHIFT_TAB, BACKSPACE, DELETE,
    HOME, END, PAGE_UP, PAGE_DOWN,
    CHAR,
    CTRL_TAB,
    UNKNOWN
}
