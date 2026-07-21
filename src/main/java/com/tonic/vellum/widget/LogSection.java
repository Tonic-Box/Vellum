package com.tonic.vellum.widget;

/**
 * A ScrollSection for logs: follows the tail by default and retains a bounded number of
 * lines, dropping the oldest. Call {@code maxLines(0)} for unlimited.
 */
public final class LogSection extends ScrollSection
{
    /**
     * Default retained line cap, to bound memory for long-running logs.
     */
    public static final int DEFAULT_MAX_LINES = 5000;

    /**
     * Creates a log section that follows the tail and retains DEFAULT_MAX_LINES lines.
     */
    public LogSection()
    {
        followTail(true);
        maxLines(DEFAULT_MAX_LINES);
    }

    /**
     * Appends a log line. Must be called on the UI thread (use {@code App.post} from others).
     *
     * @param line the line to append
     * @return this LogSection for chaining
     */
    public LogSection append(String line)
    {
        appendLine(line);
        return this;
    }
}
