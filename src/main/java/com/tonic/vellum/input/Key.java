package com.tonic.vellum.input;

/** Logical key identity. Printable input is reported as {@link #CHAR} with the char in {@code ch()}. */
public enum Key {
    UP, DOWN, LEFT, RIGHT,
    ENTER, ESCAPE, TAB, SHIFT_TAB, BACKSPACE, DELETE,
    HOME, END, PAGE_UP, PAGE_DOWN,
    /** A printable character; the character is carried in {@code ch()}. */
    CHAR,
    /** Ctrl+Tab; recognized only where the terminal reports it. */
    CTRL_TAB,
    /** An unrecognized input sequence. */
    UNKNOWN
}
