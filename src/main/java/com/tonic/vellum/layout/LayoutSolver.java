package com.tonic.vellum.layout;

import com.tonic.vellum.geom.Rect;

import java.util.List;

/**
 * Pure constraint solver. {@link #solve} divides an available rect among children along
 * one axis, guaranteeing the result <em>exactly tiles</em> the available extent: no gaps,
 * no overlap, every cell covered.
 *
 * <p>Algorithm: fixed and percent sizes form each item's base; min/max/fill items also
 * carry a flex weight (min/max have a floor/cap). Remaining space is distributed across
 * weights respecting caps; any rounding remainder goes to the last flexible item (or, if
 * none, stretches the last item) so the sizes always sum to the extent.
 */
public final class LayoutSolver {

    private static final int UNCAPPED = Integer.MAX_VALUE;

    private LayoutSolver() {}

    public static Rect[] solve(Rect available, List<Constraint> constraints, Axis axis) {
        int n = constraints.size();
        Rect[] result = new Rect[n];
        if (n == 0) {
            return result;
        }

        int extent = axis == Axis.HORIZONTAL ? available.width() : available.height();
        int[] size = new int[n];
        int[] weight = new int[n];
        int[] cap = new int[n];

        for (int i = 0; i < n; i++) {
            Constraint c = constraints.get(i);
            switch (c.kind()) {
                case FIXED:
                    size[i] = c.value();
                    weight[i] = 0;
                    cap[i] = c.value();
                    break;
                case PERCENT:
                    size[i] = (int) Math.round(c.value() * (long) extent / 100.0);
                    weight[i] = 0;
                    cap[i] = size[i];
                    break;
                case MIN:
                    size[i] = c.value();
                    weight[i] = 1;
                    cap[i] = UNCAPPED;
                    break;
                case MAX:
                    size[i] = 0;
                    weight[i] = 1;
                    cap[i] = c.value();
                    break;
                case FILL:
                default:
                    size[i] = 0;
                    weight[i] = c.value();
                    cap[i] = UNCAPPED;
                    break;
            }
        }

        int total = sum(size);
        if (total > extent) {
            shrinkToFit(size, total - extent);
        } else if (total < extent) {
            distribute(size, weight, cap, extent - total);
        }

        return toRects(available, size, axis);
    }

    /** Distribute {@code remaining} cells across weighted items, respecting caps. */
    private static void distribute(int[] size, int[] weight, int[] cap, int remaining) {
        while (remaining > 0) {
            int totalWeight = 0;
            for (int i = 0; i < size.length; i++) {
                if (weight[i] > 0 && size[i] < cap[i]) {
                    totalWeight += weight[i];
                }
            }
            if (totalWeight == 0) {
                break;
            }
            int share = remaining / totalWeight;
            if (share == 0) {
                break; // fall through to remainder rounding below
            }
            for (int i = 0; i < size.length && remaining > 0; i++) {
                if (weight[i] == 0 || size[i] >= cap[i]) {
                    continue;
                }
                int grant = share * weight[i];
                int room = cap[i] == UNCAPPED ? grant : Math.min(grant, cap[i] - size[i]);
                size[i] += room;
                remaining -= room;
            }
        }
        if (remaining > 0) {
            assignRemainder(size, weight, cap, remaining);
        }
    }

    /** Hand leftover cells one-by-one to the last flexible item with room, else the last item. */
    private static void assignRemainder(int[] size, int[] weight, int[] cap, int remaining) {
        int target = -1;
        for (int i = size.length - 1; i >= 0; i--) {
            if (weight[i] > 0 && size[i] < cap[i]) {
                target = i;
                break;
            }
        }
        if (target >= 0) {
            int room = cap[target] == UNCAPPED ? remaining : Math.min(remaining, cap[target] - size[target]);
            size[target] += room;
            remaining -= room;
        }
        if (remaining > 0) {
            // degenerate (all fixed/capped and underfilled): stretch the last item to avoid a gap
            size[size.length - 1] += remaining;
        }
    }

    /** Reduce trailing items to remove {@code over} cells, clamping at zero. */
    private static void shrinkToFit(int[] size, int over) {
        for (int i = size.length - 1; i >= 0 && over > 0; i--) {
            int take = Math.min(over, size[i]);
            size[i] -= take;
            over -= take;
        }
    }

    private static Rect[] toRects(Rect available, int[] size, Axis axis) {
        Rect[] result = new Rect[size.length];
        if (axis == Axis.HORIZONTAL) {
            int x = available.x();
            for (int i = 0; i < size.length; i++) {
                result[i] = new Rect(x, available.y(), size[i], available.height());
                x += size[i];
            }
        } else {
            int y = available.y();
            for (int i = 0; i < size.length; i++) {
                result[i] = new Rect(available.x(), y, available.width(), size[i]);
                y += size[i];
            }
        }
        return result;
    }

    private static int sum(int[] a) {
        int s = 0;
        for (int v : a) {
            s += v;
        }
        return s;
    }
}
