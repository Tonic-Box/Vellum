package com.tonic.vellum;

/** Handle to an open overlay. Close it to remove the overlay and restore focus beneath. */
public interface OverlayHandle {

    /** Remove the overlay and pop its focus scope. UI-thread-only. Idempotent. */
    void close();

    /**
     * Reports whether the overlay is still open.
     *
     * @return {@code true} until {@link #close()} is called
     */
    boolean isOpen();
}
