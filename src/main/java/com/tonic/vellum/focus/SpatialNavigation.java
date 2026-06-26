package com.tonic.vellum.focus;

import com.tonic.vellum.Section;
import com.tonic.vellum.geom.Rect;
import com.tonic.vellum.input.Key;

import java.util.List;

/**
 * Picks the nearest focus target in the pressed arrow's direction, using the targets'
 * bounds. Candidates not strictly in the direction are ignored; among the rest the one
 * with the smallest primary-axis gap (then cross-axis offset) wins.
 */
final class SpatialNavigation implements Navigation {

    @Override
    public Section resolve(Section current, Key direction, List<Section> targets) {
        Rect from = current.bounds();
        double fromCx = from.x() + from.width() / 2.0;
        double fromCy = from.y() + from.height() / 2.0;

        Section best = null;
        double bestScore = Double.MAX_VALUE;

        for (Section candidate : targets) {
            if (candidate == current) {
                continue;
            }
            Rect r = candidate.bounds();
            double cx = r.x() + r.width() / 2.0;
            double cy = r.y() + r.height() / 2.0;
            double dx = cx - fromCx;
            double dy = cy - fromCy;

            if (!inDirection(direction, dx, dy)) {
                continue;
            }
            double primary = isHorizontal(direction) ? Math.abs(dx) : Math.abs(dy);
            double cross = isHorizontal(direction) ? Math.abs(dy) : Math.abs(dx);
            double score = primary + cross * 2.0;
            if (score < bestScore) {
                bestScore = score;
                best = candidate;
            }
        }
        return best;
    }

    private static boolean inDirection(Key direction, double dx, double dy) {
        switch (direction) {
            case LEFT:  return dx < 0;
            case RIGHT: return dx > 0;
            case UP:    return dy < 0;
            case DOWN:  return dy > 0;
            default:    return false;
        }
    }

    private static boolean isHorizontal(Key direction) {
        return direction == Key.LEFT || direction == Key.RIGHT;
    }
}
