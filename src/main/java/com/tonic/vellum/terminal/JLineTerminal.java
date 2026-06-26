package com.tonic.vellum.terminal;

import com.tonic.vellum.input.KeyEvent;
import org.jline.terminal.Attributes;
import org.jline.terminal.Size;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.NonBlockingReader;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * JLine-backed {@link Terminal}. All JLine imports are confined to this file so the rest
 * of the framework never depends on JLine; replacing this class with another driver
 * requires no changes elsewhere. Key decoding is delegated to {@link InputDecoder}, fed
 * by the JLine non-blocking reader.
 */
final class JLineTerminal implements Terminal {

    /** Control Sequence Introducer: ESC + '['. Built from char codes to keep the source ASCII. */
    private static final String CSI = new String(new char[]{(char) 27, (char) 91});
    private static final String ALT_SCREEN_ON = CSI + "?1049h";
    private static final String ALT_SCREEN_OFF = CSI + "?1049l";
    private static final String CURSOR_HIDE = CSI + "?25l";
    private static final String CURSOR_SHOW = CSI + "?25h";

    private final org.jline.terminal.Terminal terminal;
    private final NonBlockingReader reader;
    private final PrintWriter writer;
    private final InputDecoder.IntSource source;

    private Attributes savedAttributes;
    private boolean altScreen;
    private boolean restored;

    JLineTerminal() {
        try {
            this.terminal = TerminalBuilder.builder()
                    .system(true)
                    .nativeSignals(true)
                    .build();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to open system terminal", e);
        }
        String type = terminal.getType();
        if (type == null || type.startsWith(org.jline.terminal.Terminal.TYPE_DUMB)) {
            try {
                terminal.close();
            } catch (IOException ignored) {
                // closing a dumb terminal; nothing to recover
            }
            throw new IllegalStateException(
                    "Vellum requires an interactive ANSI terminal; none was detected (terminal type: "
                            + type + "). Run the application directly in a terminal, not through a pipe or non-TTY launcher.");
        }
        this.reader = terminal.reader();
        this.writer = terminal.writer();
        this.source = timeout -> {
            try {
                return reader.read(timeout <= 0 ? 1 : timeout);
            } catch (IOException e) {
                return InputDecoder.IntSource.EOF;
            }
        };
    }

    @Override
    public void enterRawMode() {
        if (savedAttributes == null) {
            savedAttributes = terminal.getAttributes();
        }
        terminal.enterRawMode();
    }

    @Override
    public void enterAlternateScreen() {
        write(ALT_SCREEN_ON);
        altScreen = true;
        flush();
    }

    @Override
    public void restore() {
        if (restored) {
            return;
        }
        restored = true;
        try {
            showCursor();
            if (altScreen) {
                write(ALT_SCREEN_OFF);
                altScreen = false;
            }
            flush();
            if (savedAttributes != null) {
                terminal.setAttributes(savedAttributes);
            }
        } finally {
            try {
                terminal.close();
            } catch (IOException ignored) {
                // best-effort restore
            }
        }
    }

    @Override
    public TerminalSize size() {
        Size s = terminal.getSize();
        return new TerminalSize(s.getColumns(), s.getRows());
    }

    @Override
    public void setResizeListener(Runnable listener) {
        terminal.handle(org.jline.terminal.Terminal.Signal.WINCH, signal -> {
            if (listener != null) {
                listener.run();
            }
        });
    }

    @Override
    public KeyEvent readKey(long timeoutMillis) {
        return InputDecoder.readKey(source, timeoutMillis);
    }

    @Override
    public void write(String text) {
        writer.write(text);
    }

    @Override
    public void flush() {
        writer.flush();
    }

    @Override
    public void hideCursor() {
        write(CURSOR_HIDE);
    }

    @Override
    public void showCursor() {
        write(CURSOR_SHOW);
    }

    @Override
    public void moveCursor(int x, int y) {
        write(CSI + (y + 1) + ";" + (x + 1) + "H");
    }
}
