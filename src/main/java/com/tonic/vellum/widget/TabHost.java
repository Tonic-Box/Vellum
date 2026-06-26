package com.tonic.vellum.widget;

import com.tonic.vellum.Canvas;
import com.tonic.vellum.CharWidth;
import com.tonic.vellum.KeyResult;
import com.tonic.vellum.Section;
import com.tonic.vellum.focus.FocusContainer;
import com.tonic.vellum.geom.Rect;
import com.tonic.vellum.input.KeyEvent;
import com.tonic.vellum.style.Style;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds N child sections and renders exactly one (the active tab) plus a one-row tab bar,
 * swapping instantly. Child instances are always retained across switches; only visibility
 * lifecycle ({@code onMount}/{@code onUnmount}) fires, so a hidden tab can pause work and
 * resume when shown.
 *
 * <p>As a {@link FocusContainer}, the active content is part of the focus path while the
 * tab host is focused, so the content receives keys first (e.g. scrolling) and the tab host
 * handles {@code LEFT}/{@code RIGHT} only when the content leaves them unhandled.
 */
public final class TabHost extends Section implements FocusContainer {

    private final List<String> titles = new ArrayList<>();
    private final List<Section> contents = new ArrayList<>();
    private int active = 0;
    private boolean showTabBar = true;
    private boolean live = false;

    public TabHost add(String title, Section content) {
        titles.add(title);
        contents.add(content);
        return this;
    }

    public TabHost showTabBar(boolean show) {
        this.showTabBar = show;
        return this;
    }

    public int active() {
        return active;
    }

    public int count() {
        return contents.size();
    }

    /** Switch to a tab by index, firing visibility lifecycle on the outgoing/incoming children. */
    public void select(int index) {
        if (index < 0 || index >= contents.size() || index == active) {
            return;
        }
        if (live) {
            unmount(contents.get(active));
        }
        active = index;
        if (live) {
            Section incoming = contents.get(active);
            place(incoming, contentRect());
            mount(incoming);
            redrawSubtree(incoming);
            requestRedraw();
            refreshFocus(); // the active focus target changed; keep focus on the new content
        }
    }

    /** Switch to a tab by title. */
    public void select(String title) {
        int index = titles.indexOf(title);
        if (index >= 0) {
            select(index);
        }
    }

    @Override
    protected List<Section> children() {
        return contents.isEmpty()
                ? Collections.emptyList()
                : Collections.singletonList(contents.get(active));
    }

    @Override
    protected void onMount() {
        live = true;
    }

    @Override
    protected void onUnmount() {
        live = false;
    }

    @Override
    protected void onResize(Rect newBounds) {
        if (!contents.isEmpty()) {
            place(contents.get(active), contentRect());
        }
    }

    private Rect contentRect() {
        Rect b = bounds();
        return showTabBar ? b.splitTop(1)[1] : b;
    }

    @Override
    protected void render(Canvas canvas) {
        if (!showTabBar) {
            return;
        }
        canvas.fill(new Rect(0, 0, canvas.width(), 1), ' ', Style.NORMAL);
        boolean focused = isFocused();
        int x = 0;
        for (int i = 0; i < titles.size() && x < canvas.width(); i++) {
            String label = " " + titles.get(i) + " ";
            Style style = Style.NORMAL;
            if (i == active) {
                style = focused ? Style.REVERSE : Style.DIM_REVERSE;
            }
            canvas.put(x, 0, label, style);
            x += CharWidth.width(label);
        }
    }

    @Override
    protected KeyResult onKey(KeyEvent key) {
        switch (key.code()) {
            case LEFT:
                select(Math.max(0, active - 1));
                return KeyResult.CONSUMED;
            case RIGHT:
                select(Math.min(count() - 1, active + 1));
                return KeyResult.CONSUMED;
            case CTRL_TAB:
                if (count() > 0) {
                    select((active + 1) % count());
                }
                return KeyResult.CONSUMED;
            default:
                return KeyResult.UNHANDLED;
        }
    }

    // ---- FocusContainer: the active content is the internal focus stop ----

    @Override
    public List<Section> focusTargets() {
        return children();
    }

    @Override
    public Section activeFocusTarget() {
        return contents.isEmpty() ? null : contents.get(active);
    }

    @Override
    public boolean advanceFocus(boolean forward) {
        return false; // no internal Tab stops in v1: TAB crosses panes
    }
}
