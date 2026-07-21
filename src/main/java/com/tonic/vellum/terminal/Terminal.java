package com.tonic.vellum.terminal;

import com.tonic.vellum.input.KeyEvent;

/**
 * The terminal driver contract. The default implementation is JLine-backed, and any
 * driver that satisfies these methods can replace it without touching the public API.
 */
public interface Terminal extends AutoCloseable
{
    /**
     * Switches the terminal into raw (unbuffered, no-echo) mode.
     */
    void enterRawMode();

    /**
     * Switches to the alternate screen buffer so the user's scrollback is preserved.
     */
    void enterAlternateScreen();

    /**
     * Restores the terminal to the main screen, cooked mode, and shown cursor. Idempotent.
     */
    void restore();

    /**
     * @return the current size in cells
     */
    TerminalSize size();

    /**
     * Registers a callback invoked when the terminal resizes.
     *
     * @param listener the callback, which may run off the UI thread
     */
    void setResizeListener(Runnable listener);

    /**
     * Reads and decodes the next key, blocking up to {@code timeoutMillis}.
     *
     * @param timeoutMillis maximum time to wait in milliseconds
     * @return the decoded key event, or {@code null} when the timeout elapses with no input
     */
    KeyEvent readKey(long timeoutMillis);

    /**
     * Queues an already-formed ANSI string for output.
     *
     * @param text the raw string to write
     */
    void write(String text);

    /**
     * Flushes queued output to the terminal.
     */
    void flush();

    /**
     * Hides the cursor.
     */
    void hideCursor();

    /**
     * Shows the cursor.
     */
    void showCursor();

    /**
     * Positions the cursor at a zero-based cell coordinate.
     *
     * @param x column in cells
     * @param y row in cells
     */
    void moveCursor(int x, int y);

    @Override
    default void close()
    {
        restore();
    }
}
