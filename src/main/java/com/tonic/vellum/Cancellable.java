package com.tonic.vellum;

/** Handle for a scheduled task; cancel to stop further runs. */
public interface Cancellable {

    /** Cancel the scheduled task. Idempotent. */
    void cancel();

    /**
     * Reports whether the task has been cancelled.
     *
     * @return {@code true} once {@link #cancel()} has been called
     */
    boolean isCancelled();
}
