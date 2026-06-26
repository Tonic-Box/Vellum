package com.tonic.vellum.input;

/**
 * An immutable, normalized key event. The terminal driver translates raw escape
 * sequences into these; sections never see raw bytes.
 */
public final class KeyEvent {

    private final Key code;
    private final char ch;
    private final boolean ctrl;
    private final boolean alt;
    private final boolean shift;

    public KeyEvent(Key code, char ch, boolean ctrl, boolean alt, boolean shift) {
        this.code = code;
        this.ch = ch;
        this.ctrl = ctrl;
        this.alt = alt;
        this.shift = shift;
    }

    /** A non-printable key with no modifiers. */
    public static KeyEvent special(Key code) {
        return new KeyEvent(code, '\0', false, false, false);
    }

    /** A non-printable key with modifiers. */
    public static KeyEvent special(Key code, boolean ctrl, boolean alt, boolean shift) {
        return new KeyEvent(code, '\0', ctrl, alt, shift);
    }

    /** A printable character with no modifiers. */
    public static KeyEvent character(char ch) {
        return new KeyEvent(Key.CHAR, ch, false, false, false);
    }

    /** A printable character with modifiers. */
    public static KeyEvent character(char ch, boolean ctrl, boolean alt, boolean shift) {
        return new KeyEvent(Key.CHAR, ch, ctrl, alt, shift);
    }

    /** Logical key. */
    public Key code() { return code; }

    /** The character, valid when {@code code() == CHAR}. */
    public char ch() { return ch; }

    public boolean ctrl() { return ctrl; }

    public boolean alt() { return alt; }

    public boolean shift() { return shift; }

    /** True when this event's logical key equals {@code k}. */
    public boolean is(Key k) { return code == k; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("KeyEvent[").append(code);
        if (code == Key.CHAR) sb.append(" '").append(ch).append('\'');
        if (ctrl) sb.append(" +ctrl");
        if (alt) sb.append(" +alt");
        if (shift) sb.append(" +shift");
        return sb.append(']').toString();
    }
}
