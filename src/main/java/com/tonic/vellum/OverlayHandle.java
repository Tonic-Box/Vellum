package com.tonic.vellum;

/**
 * Handle to an open overlay. Close it to remove the overlay and restore focus beneath.
 */
public interface OverlayHandle
{
    /**
     * Removes the overlay and pops its focus scope. UI-thread-only. Idempotent.
     */
    void close();

    /**
     * @return true until close() is called
     */
    boolean isOpen();
}
