package com.tonic.vellum;

import com.tonic.vellum.input.KeyEvent;
import com.tonic.vellum.terminal.Terminal;
import com.tonic.vellum.terminal.TerminalSize;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/** A headless {@link Terminal} for driving the run loop in tests: scripted keys in, output captured. */
final class FakeTerminal implements Terminal {

    private volatile TerminalSize size;
    private final LinkedBlockingQueue<KeyEvent> keys = new LinkedBlockingQueue<>();
    final StringBuilder output = new StringBuilder();

    volatile boolean rawMode;
    volatile boolean altScreen;
    volatile boolean cursorHidden;
    volatile boolean restored;
    private Runnable resizeListener;

    FakeTerminal(int columns, int rows) {
        this.size = new TerminalSize(columns, rows);
    }

    /** Queue a key to be returned by {@link #readKey(long)}. */
    void send(KeyEvent key) {
        keys.add(key);
    }

    /** Number of keys not yet consumed by the run loop. */
    int pendingKeyCount() {
        return keys.size();
    }

    void fireResize() {
        if (resizeListener != null) {
            resizeListener.run();
        }
    }

    /** Change the reported size and signal a resize. */
    void resizeTo(int columns, int rows) {
        this.size = new TerminalSize(columns, rows);
        fireResize();
    }

    @Override
    public void enterRawMode() {
        rawMode = true;
    }

    @Override
    public void enterAlternateScreen() {
        altScreen = true;
    }

    @Override
    public void restore() {
        restored = true;
        rawMode = false;
        altScreen = false;
    }

    @Override
    public TerminalSize size() {
        return size;
    }

    @Override
    public void setResizeListener(Runnable listener) {
        this.resizeListener = listener;
    }

    @Override
    public KeyEvent readKey(long timeoutMillis) {
        try {
            return keys.poll(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    @Override
    public void write(String text) {
        synchronized (output) {
            output.append(text);
        }
    }

    @Override
    public void flush() {
        // no-op: output is captured eagerly
    }

    @Override
    public void hideCursor() {
        cursorHidden = true;
    }

    @Override
    public void showCursor() {
        cursorHidden = false;
    }

    volatile int cursorX = -1;
    volatile int cursorY = -1;

    @Override
    public void moveCursor(int x, int y) {
        cursorX = x;
        cursorY = y;
    }
}
