package com.tonic.vellum;

import com.tonic.vellum.geom.Rect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The root compositor: a base section that fills the screen plus a stack of overlay
 * sections drawn on top in insertion order (z-order). Keeping everything in one Section
 * tree preserves the single mental model; overlays are simply later children that may
 * overlap the base. The framework wraps the user's root in a Stage automatically.
 */
final class Stage extends Section {

    private final Section base;
    private final List<Section> overlays = new ArrayList<>();
    private final List<Placement> placements = new ArrayList<>();

    Stage(Section base) {
        this.base = base;
    }

    @Override
    protected List<Section> children() {
        if (overlays.isEmpty()) {
            return Collections.singletonList(base);
        }
        List<Section> all = new ArrayList<>(overlays.size() + 1);
        all.add(base);
        all.addAll(overlays);
        return all;
    }

    @Override
    protected void onResize(Rect newBounds) {
        place(base, newBounds);
        for (int i = 0; i < overlays.size(); i++) {
            place(overlays.get(i), placements.get(i).resolve(newBounds));
        }
    }

    @Override
    protected void render(Canvas canvas) {
        // the compositor draws nothing of its own; base and overlays render themselves
    }

    void addOverlay(Section content, Placement placement) {
        overlays.add(content);
        placements.add(placement);
        place(content, placement.resolve(bounds()));
        mount(content);
        redrawSubtree(content);
        requestRedraw();
    }

    void removeOverlay(Section content) {
        int i = overlays.indexOf(content);
        if (i < 0) {
            return;
        }
        unmount(content);
        overlays.remove(i);
        placements.remove(i);
        // restore whatever the overlay was covering
        redrawSubtree(base);
        for (Section remaining : overlays) {
            redrawSubtree(remaining);
        }
        requestRedraw();
    }
}
