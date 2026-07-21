package com.tonic.vellum.terminal;

/**
 * Factory for the platform terminal driver. The concrete implementation is internal.
 */
public final class Terminals
{
    private Terminals()
    {
    }

    /**
     * Opens the system terminal, JLine-backed by default.
     *
     * @return the platform terminal driver
     * @throws IllegalStateException if no interactive ANSI terminal is available
     */
    public static Terminal system()
    {
        return new JLineTerminal();
    }
}
