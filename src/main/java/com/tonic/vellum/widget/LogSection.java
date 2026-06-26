package com.tonic.vellum.widget;

/**
 * A {@link ScrollSection} specialized for logs: follows the tail by default, with a public
 * {@link #append} for feeding lines (typically from a background thread via {@code App.post}).
 */
public final class LogSection extends ScrollSection {

    public LogSection() {
        followTail(true);
    }

    /** Append a log line. Must be called on the UI thread (use {@code App.post} from others). */
    public LogSection append(String line) {
        appendLine(line);
        return this;
    }
}
