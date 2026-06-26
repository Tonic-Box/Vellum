package com.tonic.vellum.widget;

/**
 * A {@link ScrollSection} specialized for logs: follows the tail by default and retains a
 * bounded number of lines (the oldest are dropped). Feed lines via {@link #append} (typically
 * from a background thread through {@code App.post}). Call {@code maxLines(0)} for unlimited.
 */
public final class LogSection extends ScrollSection {

    /** Default retained line cap, to bound memory for long-running logs. */
    public static final int DEFAULT_MAX_LINES = 5000;

    public LogSection() {
        followTail(true);
        maxLines(DEFAULT_MAX_LINES);
    }

    /** Append a log line. Must be called on the UI thread (use {@code App.post} from others). */
    public LogSection append(String line) {
        appendLine(line);
        return this;
    }
}
