package com.tonic.vellum;

import com.tonic.vellum.focus.FocusContainer;
import com.tonic.vellum.focus.Navigation;
import com.tonic.vellum.input.Key;
import com.tonic.vellum.input.KeyEvent;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Owns a stack of {@link FocusScope}s. The base UI is the bottom scope; each modal overlay
 * pushes a new one. Only the top scope is active: it receives keys, owns Tab/Shift-Tab and
 * spatial navigation, and defines the focus path. Pushing or popping a scope moves focus
 * across layers, firing focus hooks and repainting affected sections.
 */
final class FocusManager {

    private final Host host;
    private final Deque<FocusScope> scopes = new ArrayDeque<>();

    FocusManager(List<Section> order, Navigation navigation, Host host) {
        this.host = host;
        this.scopes.push(new FocusScope(order, navigation));
    }

    /** Establish initial focus on the base scope. */
    void start(Section initial) {
        FocusScope scope = top();
        if (scope.order.isEmpty()) {
            return;
        }
        scope.index = indexOf(scope, initial, 0);
        scope.recomputePath();
        gain(scope.focused());
    }

    /** Push a modal focus scope (e.g. for an overlay) and focus its initial target. */
    void pushScope(List<Section> order, Navigation navigation, Section initial) {
        lose(top().focused());
        FocusScope scope = new FocusScope(order, navigation);
        scope.index = order.isEmpty() ? -1 : indexOf(scope, initial, 0);
        scopes.push(scope);
        scope.recomputePath();
        gain(scope.focused());
        host.requestRepaint();
    }

    /** Pop the top scope, restoring focus to the scope beneath. The base scope never pops. */
    void popScope() {
        if (scopes.size() <= 1) {
            return;
        }
        FocusScope removed = scopes.pop();
        lose(removed.focused());
        gain(top().focused());
        host.requestRepaint();
    }

    boolean isOnFocusPath(Section section) {
        return top().path.contains(section);
    }

    /** The deepest node on the active focus path (the section receiving keys), or null. */
    Section focusedSection() {
        return top().focused();
    }

    /** Dispatch a key: deepest focus-path node first, then bubble, then focus navigation. */
    void dispatchKey(KeyEvent key) {
        FocusScope scope = top();
        Section deepest = scope.focused();
        if (deepest != null && deliverWithBubble(deepest, key)) {
            return;
        }
        if (key.is(Key.TAB)) {
            advance(scope, true);
        } else if (key.is(Key.SHIFT_TAB)) {
            advance(scope, false);
        } else if (scope.navigation != null && scope.current() != null && isArrow(key)) {
            Section dest = scope.navigation.resolve(scope.current(), key.code(), scope.order);
            if (dest != null) {
                focusTo(scope, scope.order.indexOf(dest));
            }
        }
    }

    private boolean deliverWithBubble(Section target, KeyEvent key) {
        Section s = target;
        while (s != null) {
            if (s.dispatchKey(key) == KeyResult.CONSUMED) {
                return true;
            }
            s = s.parent();
        }
        return false;
    }

    private void advance(FocusScope scope, boolean forward) {
        if (scope.order.isEmpty()) {
            return;
        }
        Section target = scope.current();
        if (target instanceof FocusContainer) {
            Section before = scope.focused();
            if (((FocusContainer) target).advanceFocus(forward)) {
                scope.recomputePath();
                Section after = scope.focused();
                if (after != before) {
                    lose(before);
                    gain(after);
                } else {
                    markDirtyWithAncestors(target);
                }
                host.requestRepaint();
                return;
            }
        }
        int n = scope.order.size();
        int next = (((scope.index + (forward ? 1 : -1)) % n) + n) % n;
        focusTo(scope, next);
    }

    private void focusTo(FocusScope scope, int newIndex) {
        if (newIndex < 0 || newIndex == scope.index) {
            return;
        }
        lose(scope.focused());
        scope.index = newIndex;
        scope.recomputePath();
        gain(scope.focused());
        host.requestRepaint();
    }

    private void gain(Section section) {
        if (section != null) {
            section.dispatchFocusGained();
            markDirtyWithAncestors(section);
        }
    }

    private void lose(Section section) {
        if (section != null) {
            section.dispatchFocusLost();
            markDirtyWithAncestors(section);
        }
    }

    private void markDirtyWithAncestors(Section section) {
        Section s = section;
        while (s != null) {
            s.markDirty();
            s = s.parent();
        }
    }

    private FocusScope top() {
        return scopes.peek();
    }

    private static int indexOf(FocusScope scope, Section target, int fallback) {
        if (target == null) {
            return scope.order.isEmpty() ? -1 : fallback;
        }
        int i = scope.order.indexOf(target);
        return i >= 0 ? i : fallback;
    }

    private static boolean isArrow(KeyEvent key) {
        return key.is(Key.UP) || key.is(Key.DOWN) || key.is(Key.LEFT) || key.is(Key.RIGHT);
    }
}
