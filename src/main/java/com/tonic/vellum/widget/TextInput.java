package com.tonic.vellum.widget;

import com.tonic.vellum.Canvas;
import com.tonic.vellum.CharWidth;
import com.tonic.vellum.KeyResult;
import com.tonic.vellum.Maths;
import com.tonic.vellum.Section;
import com.tonic.vellum.geom.Point;
import com.tonic.vellum.input.KeyEvent;
import com.tonic.vellum.style.Style;

import java.util.function.Consumer;

/**
 * A single-line editable text field. Supports insert, backspace, delete, caret movement
 * (Left/Right/Home/End), and horizontal scrolling when the text is wider than the field.
 * Enter fires {@code onSubmit}; edits fire {@code onChange}. The cursor is reported through
 * {@link #cursor()} and positioned by the framework when this field is focused.
 *
 * <p>Note: do not pair a printable quit key with a focused {@code TextInput} (the quit key
 * would be typed). Use a non-printable quit such as Ctrl-C or Escape.
 */
public final class TextInput extends Section {

    private final StringBuilder text = new StringBuilder();
    private int caret;
    private int scroll;
    private String placeholder = "";
    private Style style = Style.NORMAL;
    private Style placeholderStyle = Style.DIM;
    private Consumer<String> onSubmit = s -> { };
    private Consumer<String> onChange = s -> { };

    /**
     * Creates an empty field.
     */
    public TextInput() {
    }

    /**
     * Creates a field initialized with the given text.
     *
     * @param initial the initial text
     */
    public TextInput(String initial) {
        setText(initial);
    }

    /**
     * Sets the field text, moving the caret to the end. A {@code null} value clears the field.
     *
     * @param value the new text
     * @return this TextInput for chaining
     */
    public TextInput setText(String value) {
        text.setLength(0);
        if (value != null) {
            text.append(value);
        }
        caret = text.length();
        scroll = 0;
        requestRedraw();
        return this;
    }

    /**
     * Returns the current field text.
     *
     * @return the current text
     */
    public String text() {
        return text.toString();
    }

    /**
     * Sets the placeholder shown when the field is empty and unfocused. A {@code null} value is
     * treated as empty.
     *
     * @param placeholder the placeholder text
     * @return this TextInput for chaining
     */
    public TextInput placeholder(String placeholder) {
        this.placeholder = placeholder == null ? "" : placeholder;
        requestRedraw();
        return this;
    }

    /**
     * Sets the style used to draw the text.
     *
     * @param style the text style
     * @return this TextInput for chaining
     */
    public TextInput style(Style style) {
        this.style = style;
        requestRedraw();
        return this;
    }

    /**
     * Sets the style used to draw the placeholder.
     *
     * @param style the placeholder style
     * @return this TextInput for chaining
     */
    public TextInput placeholderStyle(Style style) {
        this.placeholderStyle = style;
        requestRedraw();
        return this;
    }

    /**
     * Sets the handler called with the current text when Enter is pressed.
     *
     * @param handler the submit handler
     * @return this TextInput for chaining
     */
    public TextInput onSubmit(Consumer<String> handler) {
        this.onSubmit = handler;
        return this;
    }

    /**
     * Sets the handler called with the current text after every edit.
     *
     * @param handler the change handler
     * @return this TextInput for chaining
     */
    public TextInput onChange(Consumer<String> handler) {
        this.onChange = handler;
        return this;
    }

    /**
     * Renders the field text or, when empty and unfocused, the placeholder.
     *
     * @param canvas the canvas to draw into
     */
    @Override
    protected void render(Canvas canvas) {
        int w = canvas.width();
        if (w <= 0 || canvas.height() <= 0) {
            return;
        }
        ensureCaretVisible(w);

        if (text.length() == 0 && !isFocused() && !placeholder.isEmpty()) {
            canvas.put(0, 0, placeholder, placeholderStyle);
            return;
        }
        // draw from the first visible character; Canvas.put truncates by display width
        canvas.put(0, 0, text.substring(scroll), style);
    }

    /**
     * Returns the caret position relative to the field, accounting for horizontal scroll.
     *
     * @return the cursor point within the field
     */
    @Override
    protected Point cursor() {
        return new Point(columnWidth(Math.min(scroll, caret), caret), 0);
    }

    /**
     * Handles editing and caret-movement keys.
     *
     * @param key the key event
     * @return the key handling result
     */
    @Override
    protected KeyResult onKey(KeyEvent key) {
        switch (key.code()) {
            case LEFT:
                if (caret > 0) {
                    caret = Character.offsetByCodePoints(text, caret, -1);
                    requestRedraw();
                }
                return KeyResult.CONSUMED;
            case RIGHT:
                if (caret < text.length()) {
                    caret = Character.offsetByCodePoints(text, caret, 1);
                    requestRedraw();
                }
                return KeyResult.CONSUMED;
            case HOME:
                moveCaret(0);
                return KeyResult.CONSUMED;
            case END:
                moveCaret(text.length());
                return KeyResult.CONSUMED;
            case BACKSPACE:
                if (caret > 0) {
                    int prev = Character.offsetByCodePoints(text, caret, -1);
                    text.delete(prev, caret);
                    caret = prev;
                    edited();
                }
                return KeyResult.CONSUMED;
            case DELETE:
                if (caret < text.length()) {
                    int next = Character.offsetByCodePoints(text, caret, 1);
                    text.delete(caret, next);
                    edited();
                }
                return KeyResult.CONSUMED;
            case ENTER:
                onSubmit.accept(text.toString());
                return KeyResult.CONSUMED;
            case CHAR:
                if (!key.ctrl() && !key.alt() && key.ch() >= ' ') {
                    text.insert(caret, key.ch());
                    caret++;
                    edited();
                    return KeyResult.CONSUMED;
                }
                return KeyResult.UNHANDLED;
            default:
                return KeyResult.UNHANDLED;
        }
    }

    private void moveCaret(int to) {
        int clamped = Maths.clamp(to, 0, text.length());
        if (clamped != caret) {
            caret = clamped;
            requestRedraw();
        }
    }

    private void edited() {
        requestRedraw();
        onChange.accept(text.toString());
    }

    /** Advance {@code scroll} (by whole code points) so the caret's display column fits the width. */
    private void ensureCaretVisible(int width) {
        if (scroll > caret) {
            scroll = caret;
        }
        int maxColumn = Math.max(0, width - 1);
        while (scroll < caret && columnWidth(scroll, caret) > maxColumn) {
            scroll = Character.offsetByCodePoints(text, scroll, 1);
        }
    }

    private int columnWidth(int from, int to) {
        return CharWidth.width(text.substring(from, to));
    }
}
