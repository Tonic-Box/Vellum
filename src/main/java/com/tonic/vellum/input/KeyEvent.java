package com.tonic.vellum.input;

/**
 * An immutable, normalized key event. The terminal driver translates raw escape sequences
 * into these.
 */
public final class KeyEvent {

    private final Key code;
    private final char ch;
    private final boolean ctrl;
    private final boolean alt;
    private final boolean shift;

    /**
     * Creates a key event.
     *
     * @param code the logical key
     * @param ch the character, valid when {@code code} is {@link Key#CHAR}
     * @param ctrl whether the Ctrl modifier is held
     * @param alt whether the Alt modifier is held
     * @param shift whether the Shift modifier is held
     */
    public KeyEvent(Key code, char ch, boolean ctrl, boolean alt, boolean shift) {
        this.code = code;
        this.ch = ch;
        this.ctrl = ctrl;
        this.alt = alt;
        this.shift = shift;
    }

    /**
     * Creates a non-printable key event with no modifiers.
     *
     * @param code the logical key
     * @return the key event
     */
    public static KeyEvent special(Key code) {
        return new KeyEvent(code, '\0', false, false, false);
    }

    /**
     * Creates a non-printable key event with modifiers.
     *
     * @param code the logical key
     * @param ctrl whether the Ctrl modifier is held
     * @param alt whether the Alt modifier is held
     * @param shift whether the Shift modifier is held
     * @return the key event
     */
    public static KeyEvent special(Key code, boolean ctrl, boolean alt, boolean shift) {
        return new KeyEvent(code, '\0', ctrl, alt, shift);
    }

    /**
     * Creates a printable character event with no modifiers.
     *
     * @param ch the character
     * @return the key event
     */
    public static KeyEvent character(char ch) {
        return new KeyEvent(Key.CHAR, ch, false, false, false);
    }

    /**
     * Creates a printable character event with modifiers.
     *
     * @param ch the character
     * @param ctrl whether the Ctrl modifier is held
     * @param alt whether the Alt modifier is held
     * @param shift whether the Shift modifier is held
     * @return the key event
     */
    public static KeyEvent character(char ch, boolean ctrl, boolean alt, boolean shift) {
        return new KeyEvent(Key.CHAR, ch, ctrl, alt, shift);
    }

    /** @return the logical key */
    public Key code() { return code; }

    /** @return the character, valid when {@code code() == CHAR} */
    public char ch() { return ch; }

    /** @return true when the Ctrl modifier is held */
    public boolean ctrl() { return ctrl; }

    /** @return true when the Alt modifier is held */
    public boolean alt() { return alt; }

    /** @return true when the Shift modifier is held */
    public boolean shift() { return shift; }

    /**
     * Reports whether this event's logical key equals {@code k}.
     *
     * @param k the key to compare against
     * @return true when the logical key equals {@code k}
     */
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
