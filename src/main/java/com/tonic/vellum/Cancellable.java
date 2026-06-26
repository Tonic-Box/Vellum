package com.tonic.vellum;

/** Handle for a scheduled task; cancel to stop further runs. */
public interface Cancellable {

    /** Cancel the scheduled task. Idempotent. */
    void cancel();

    /** True once {@link #cancel()} has been called. */
    boolean isCancelled();
}
