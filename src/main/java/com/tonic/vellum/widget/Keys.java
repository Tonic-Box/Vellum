package com.tonic.vellum.widget;

import com.tonic.vellum.input.Key;
import com.tonic.vellum.input.KeyEvent;

final class Keys
{
    private Keys()
    {
    }

    static boolean isActivation(KeyEvent key)
    {
        return key.is(Key.ENTER) || (key.is(Key.CHAR) && key.ch() == ' ');
    }
}
