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
 * Holds N child sections and renders exactly one (the active tab) plus a one-row tab bar.
 * Child instances are retained across switches; only the visibility lifecycle
 * ({@code onMount}/{@code onUnmount}) fires, so a hidden tab can pause work and resume when
 * shown.
 *
 * <p>As a {@link FocusContainer}, the active content is part of the focus path while the
 * tab host is focused, so the content receives keys first (for example scrolling) and the
 * tab host handles {@code LEFT}/{@code RIGHT} only when the content leaves them unhandled.
 */
public final class TabHost extends Section implements FocusContainer {

    private final List<String> titles = new ArrayList<>();
    private final List<Section> contents = new ArrayList<>();
    private int active = 0;
    private boolean showTabBar = true;
    private boolean live = false;

    /**
     * Adds a tab with the given title and content.
     *
     * @param title the tab title
     * @param content the tab content section
     * @return this TabHost for chaining
     */
    public TabHost add(String title, Section content) {
        titles.add(title);
        contents.add(content);
        return this;
    }

    /**
     * Sets whether the tab bar is shown.
     *
     * @param show {@code true} to show the tab bar
     * @return this TabHost for chaining
     */
    public TabHost showTabBar(boolean show) {
        this.showTabBar = show;
        return this;
    }

    /**
     * Returns the index of the active tab.
     *
     * @return the active tab index
     */
    public int active() {
        return active;
    }

    /**
     * Returns the number of tabs.
     *
     * @return the tab count
     */
    public int count() {
        return contents.size();
    }

    /**
     * Switches to the tab at the given index, firing the visibility lifecycle on the outgoing
     * and incoming children. Out-of-range indices and the already-active index are ignored.
     *
     * @param index the tab index to activate
     */
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

    /**
     * Switches to the tab with the given title. Unknown titles are ignored.
     *
     * @param title the tab title to activate
     */
    public void select(String title) {
        int index = titles.indexOf(title);
        if (index >= 0) {
            select(index);
        }
    }

    /**
     * Returns the active content as the sole child section, or an empty list when there are no
     * tabs.
     *
     * @return the active child, or an empty list
     */
    @Override
    protected List<Section> children() {
        return contents.isEmpty()
                ? Collections.emptyList()
                : Collections.singletonList(contents.get(active));
    }

    /**
     * Marks the host as mounted.
     */
    @Override
    protected void onMount() {
        live = true;
    }

    /**
     * Marks the host as unmounted.
     */
    @Override
    protected void onUnmount() {
        live = false;
    }

    /**
     * Places the active content within the content area.
     *
     * @param newBounds the new bounds of this section
     */
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

    /**
     * Draws the tab bar.
     *
     * @param canvas the canvas to draw into
     */
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

    /**
     * Handles {@code LEFT}/{@code RIGHT}/{@code CTRL_TAB} to switch tabs.
     *
     * @param key the key event
     * @return the key handling result
     */
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

    /**
     * Returns the focus targets, which are the active content.
     *
     * @return the focus targets
     */
    @Override
    public List<Section> focusTargets() {
        return children();
    }

    /**
     * Returns the active focus target, or {@code null} when there are no tabs.
     *
     * @return the active content, or {@code null}
     */
    @Override
    public Section activeFocusTarget() {
        return contents.isEmpty() ? null : contents.get(active);
    }

    /**
     * Returns {@code false}; the tab host has no internal Tab stops, so Tab crosses panes.
     *
     * @param forward {@code true} to advance forward, {@code false} backward
     * @return {@code false} always
     */
    @Override
    public boolean advanceFocus(boolean forward) {
        return false; // no internal Tab stops in v1: TAB crosses panes
    }
}
