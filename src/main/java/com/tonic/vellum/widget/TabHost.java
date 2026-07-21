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
 * A tab container that renders the active tab's content plus a one-row tab bar. Children are
 * retained across switches with only the visibility lifecycle firing, and the active content
 * receives keys before the host's LEFT/RIGHT/CTRL_TAB switching.
 */
public final class TabHost extends Section implements FocusContainer
{
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
    public TabHost add(String title, Section content)
    {
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
    public TabHost showTabBar(boolean show)
    {
        this.showTabBar = show;
        return this;
    }

    /**
     * @return the active tab index
     */
    public int active()
    {
        return active;
    }

    /**
     * @return the tab count
     */
    public int count()
    {
        return contents.size();
    }

    /**
     * Switches to the tab at the given index, firing the visibility lifecycle on the outgoing
     * and incoming children. Out-of-range indices and the already-active index are ignored.
     *
     * @param index the tab index to activate
     */
    public void select(int index)
    {
        if (index < 0 || index >= contents.size() || index == active)
        {
            return;
        }
        if (live)
        {
            unmount(contents.get(active));
        }
        active = index;
        if (live)
        {
            Section incoming = contents.get(active);
            place(incoming, contentRect());
            mount(incoming);
            redrawSubtree(incoming);
            requestRedraw();
            refreshFocus();
        }
    }

    /**
     * Switches to the tab with the given title. Unknown titles are ignored.
     *
     * @param title the tab title to activate
     */
    public void select(String title)
    {
        int index = titles.indexOf(title);
        if (index >= 0)
        {
            select(index);
        }
    }

    @Override
    protected List<Section> children()
    {
        return contents.isEmpty()
                ? Collections.emptyList()
                : Collections.singletonList(contents.get(active));
    }

    @Override
    protected void onMount()
    {
        live = true;
    }

    @Override
    protected void onUnmount()
    {
        live = false;
    }

    @Override
    protected void onResize(Rect newBounds)
    {
        if (!contents.isEmpty())
        {
            place(contents.get(active), contentRect());
        }
    }

    private Rect contentRect()
    {
        Rect b = bounds();
        return showTabBar ? b.splitTop(1)[1] : b;
    }

    @Override
    protected void render(Canvas canvas)
    {
        if (!showTabBar)
        {
            return;
        }
        canvas.fill(new Rect(0, 0, canvas.width(), 1), ' ', Style.NORMAL);
        boolean focused = isFocused();
        int x = 0;
        for (int i = 0; i < titles.size() && x < canvas.width(); i++)
        {
            String label = " " + titles.get(i) + " ";
            Style style = Style.NORMAL;
            if (i == active)
            {
                style = focused ? Style.REVERSE : Style.DIM_REVERSE;
            }
            canvas.put(x, 0, label, style);
            x += CharWidth.width(label);
        }
    }

    @Override
    protected KeyResult onKey(KeyEvent key)
    {
        switch (key.code())
        {
            case LEFT:
                select(Math.max(0, active - 1));
                return KeyResult.CONSUMED;
            case RIGHT:
                select(Math.min(count() - 1, active + 1));
                return KeyResult.CONSUMED;
            case CTRL_TAB:
                if (count() > 0)
                {
                    select((active + 1) % count());
                }
                return KeyResult.CONSUMED;
            default:
                return KeyResult.UNHANDLED;
        }
    }

    @Override
    public List<Section> focusTargets()
    {
        return children();
    }

    @Override
    public Section activeFocusTarget()
    {
        return contents.isEmpty() ? null : contents.get(active);
    }

    @Override
    public boolean advanceFocus(boolean forward)
    {
        return false;
    }
}
