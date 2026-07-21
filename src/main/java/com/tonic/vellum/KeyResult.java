package com.tonic.vellum;

/**
 * Outcome of Section.onKey: whether the key was handled or should keep propagating.
 */
public enum KeyResult
{
    /**
     * The key was handled; propagation stops.
     */
    CONSUMED,
    /**
     * The key was not handled; it bubbles to the parent chain and the focus manager.
     */
    UNHANDLED
}
