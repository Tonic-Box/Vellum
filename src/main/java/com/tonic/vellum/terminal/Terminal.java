package com.tonic.vellum.terminal;

import com.tonic.vellum.input.KeyEvent;

/**
 * The internal terminal driver contract - the deliberate swap-out seam. The framework
 * talks only to this interface; the default implementation is JLine-backed, but any
 * driver that satisfies these methods (raw mode, alternate screen, normalized key input,
 * a writable sink, and clean restore) can replace it without touching the public API.
 */
public interface Terminal extends AutoCloseable {

    /** Switch the terminal into raw (unbuffered, no-echo) mode. */
    void enterRawMode();

    /** Switch to the alternate screen buffer so the user's scrollback is preserved. */
    void enterAlternateScreen();

    /** Fully restore the terminal: main screen, cooked mode, cursor shown. Idempotent. */
    void restore();

    /** Current size in cells. */
    TerminalSize size();

    /** Register a callback invoked (possibly off the UI thread) when the terminal resizes. */
    void setResizeListener(Runnable listener);

    /**
     * Read and decode the next key, blocking up to {@code timeoutMillis}. Returns
     * {@code null} when the timeout elapses with no input.
     */
    KeyEvent readKey(long timeoutMillis);

    /** Queue a raw string (already-formed ANSI) for output. */
    void write(String text);

    /** Flush queued output to the terminal. */
    void flush();

    void hideCursor();

    void showCursor();

    /** Position the cursor at a zero-based cell coordinate. */
    void moveCursor(int x, int y);

    @Override
    default void close() {
        restore();
    }
}
