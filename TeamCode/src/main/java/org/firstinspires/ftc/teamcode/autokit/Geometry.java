package org.firstinspires.ftc.teamcode.autokit;

import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.pedropathing.paths.PathSegment;

import java.util.List;

/** The small bits of geometry the cards need. Pure functions, so they are tested directly. */
final class Geometry {

    private Geometry() {}

    /**
     * How far along {@code path} the robot is, 0 to 1 by distance, while it follows segment
     * {@code segmentIndex}. Pedro's own {@code completion()} is per segment, so a compound path
     * needs the lengths of the segments already driven added in.
     */
    static double progress(Path path, int segmentIndex, Pose robot) {
        List<PathSegment> segments = path.getSegments();
        if (segments.isEmpty() || segmentIndex < 0) return 0;
        if (segmentIndex >= segments.size()) return 1;
        double total = 0;
        double before = 0;
        for (int i = 0; i < segments.size(); i++) {
            double length = segments.get(i).curve.length();
            if (i < segmentIndex) before += length;
            total += length;
        }
        if (total <= 0) return 1;
        PathSegment current = segments.get(segmentIndex);
        double t = current.curve.closestParameter(robot.toVector2D());
        double within = current.curve.pathCompletion(t) * current.curve.length();
        return clamp01((before + within) / total);
    }

    /**
     * True if a robot whose centre moves in a straight line from {@code a} to {@code b} comes within
     * {@code margin} inches of the polygon {@code corners} (or passes through it). Sampled every
     * half inch, which is finer than any keep-out needs.
     */
    static boolean lineHitsPolygon(Pose a, Pose b, Pose[] corners, double margin) {
        double length = Math.hypot(b.x() - a.x(), b.y() - a.y());
        int steps = Math.max(1, (int) Math.ceil(length / 0.5));
        for (int i = 0; i <= steps; i++) {
            double f = (double) i / steps;
            double x = a.x() + (b.x() - a.x()) * f;
            double y = a.y() + (b.y() - a.y()) * f;
            if (inside(x, y, corners) || distanceToEdges(x, y, corners) < margin) return true;
        }
        return false;
    }

    static boolean inside(double x, double y, Pose[] corners) {
        boolean in = false;
        for (int i = 0, j = corners.length - 1; i < corners.length; j = i++) {
            double xi = corners[i].x(), yi = corners[i].y(), xj = corners[j].x(), yj = corners[j].y();
            if ((yi > y) != (yj > y) && x < (xj - xi) * (y - yi) / (yj - yi) + xi) in = !in;
        }
        return in;
    }

    private static double distanceToEdges(double x, double y, Pose[] corners) {
        double best = Double.MAX_VALUE;
        for (int i = 0, j = corners.length - 1; i < corners.length; j = i++) {
            best = Math.min(best, distanceToSegment(x, y, corners[j], corners[i]));
        }
        return best;
    }

    private static double distanceToSegment(double x, double y, Pose a, Pose b) {
        double dx = b.x() - a.x(), dy = b.y() - a.y();
        double lengthSquared = dx * dx + dy * dy;
        double f = lengthSquared == 0 ? 0 : clamp01(((x - a.x()) * dx + (y - a.y()) * dy) / lengthSquared);
        return Math.hypot(x - (a.x() + f * dx), y - (a.y() + f * dy));
    }

    static double distance(Pose a, Pose b) {
        return Math.hypot(b.x() - a.x(), b.y() - a.y());
    }

    static double clamp01(double v) {
        return v < 0 ? 0 : v > 1 ? 1 : v;
    }
}
