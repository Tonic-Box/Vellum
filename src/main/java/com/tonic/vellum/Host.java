package com.tonic.vellum;

interface Host
{
    void requestRepaint();

    void assertUiThread();

    boolean isOnFocusPath(Section section);

    void refreshFocus();
}
