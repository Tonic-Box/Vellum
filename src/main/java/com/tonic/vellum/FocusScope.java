package com.tonic.vellum;

import com.tonic.vellum.focus.FocusContainer;
import com.tonic.vellum.focus.Navigation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class FocusScope
{
    final List<Section> order;
    final Navigation navigation;
    int index = -1;
    List<Section> path = Collections.emptyList();

    FocusScope(List<Section> order, Navigation navigation)
    {
        this.order = order;
        this.navigation = navigation;
    }

    Section current()
    {
        return index >= 0 && index < order.size() ? order.get(index) : null;
    }

    Section focused()
    {
        return path.isEmpty() ? null : path.get(path.size() - 1);
    }

    void recomputePath()
    {
        List<Section> p = new ArrayList<>();
        Section s = current();
        while (s != null && !p.contains(s))
        {
            p.add(s);
            if (s instanceof FocusContainer)
            {
                s = ((FocusContainer) s).activeFocusTarget();
            }
            else
            {
                break;
            }
        }
        path = p;
    }
}
