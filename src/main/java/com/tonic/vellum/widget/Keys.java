package com.tonic.vellum.widget;

import com.tonic.vellum.input.Key;
import com.tonic.vellum.input.KeyEvent;

/** Shared key predicates for widgets. */
final class Keys {

    private Keys() {}

    /** True for an activation key: ENTER or SPACE. */
    static boolean isActivation(KeyEvent key) {
        return key.is(Key.ENTER) || (key.is(Key.CHAR) && key.ch() == ' ');
    }
}
