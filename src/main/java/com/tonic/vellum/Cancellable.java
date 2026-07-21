package com.tonic.vellum;

/**
 * Handle for a scheduled task; cancel to stop further runs.
 */
public interface Cancellable
{
    /**
     * Cancels the scheduled task. Idempotent.
     */
    void cancel();

    /**
     * @return true once cancel() has been called
     */
    boolean isCancelled();
}
