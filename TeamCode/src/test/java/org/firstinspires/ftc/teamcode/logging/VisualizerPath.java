package org.firstinspires.ftc.teamcode.logging;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Pedro Visualizer {@code .pp} file, turned into a timeline of poses the simulated log can
 * follow.
 *
 * <p>Coordinates are the Visualizer's, which is Pedro's convention: inches on a 144 in field, heading
 * in degrees in the file (radians here). Each line is a Bezier curve from the previous end point
 * through its control points to its end point, as the Visualizer draws it.
 *
 * <p>Timing is an approximation: each line runs a trapezoidal profile at the file's
 * {@code settings.maxVelocity} / {@code maxAcceleration} / {@code maxDeceleration}, stopping at the
 * end of each line. Real Pedro following is faster through corners; that does not matter for
 * checking where things are drawn, which is what this is for.
 *
 * <p>Handles what DECODE's files use (linear and constant headings, {@code reverse}, 0 or more
 * control points, {@code wait} steps in {@code sequence}, per-line waits) plus tangential headings.
 * Files without a {@code sequence} run their lines in order.
 */
final class VisualizerPath {

    /** One Bezier line. */
    static final class Line {
        final String name;
        final double[][] points; // start, controls..., end; inches
        final String headingMode;
        final double startRad, endRad, constantRad;
        final boolean reverse;
        final double waitBeforeS, waitAfterS;
        final double lengthIn;
        private final double[] arcT;
        private final double[] arcS;

        Line(String name, double[][] points, String headingMode, double startRad, double endRad,
             double constantRad, boolean reverse, double waitBeforeS, double waitAfterS) {
            this.name = name;
            this.points = points;
            this.headingMode = headingMode;
            this.startRad = startRad;
            this.endRad = endRad;
            this.constantRad = constantRad;
            this.reverse = reverse;
            this.waitBeforeS = waitBeforeS;
            this.waitAfterS = waitAfterS;
            int n = 400;
            arcT = new double[n + 1];
            arcS = new double[n + 1];
            double[] prev = point(0);
            for (int k = 1; k <= n; k++) {
                double t = (double) k / n;
                double[] p = point(t);
                arcT[k] = t;
                arcS[k] = arcS[k - 1] + Math.hypot(p[0] - prev[0], p[1] - prev[1]);
                prev = p;
            }
            lengthIn = arcS[n];
        }

        /** De Casteljau: works for any number of control points. */
        double[] point(double t) {
            double[][] q = new double[points.length][];
            for (int k = 0; k < points.length; k++) q[k] = points[k].clone();
            for (int r = points.length - 1; r > 0; r--) {
                for (int k = 0; k < r; k++) {
                    q[k][0] = q[k][0] + (q[k + 1][0] - q[k][0]) * t;
                    q[k][1] = q[k][1] + (q[k + 1][1] - q[k][1]) * t;
                }
            }
            return q[0];
        }

        double heading(double t) {
            switch (headingMode) {
                case "constant":
                    return constantRad;
                case "tangential": {
                    double[] a = point(Math.max(0, t - 1e-3));
                    double[] b = point(Math.min(1, t + 1e-3));
                    double h = Math.atan2(b[1] - a[1], b[0] - a[0]);
                    return reverse ? h + Math.PI : h;
                }
                default: // linear, the shortest way round
                    return startRad + AdvantageScopeFrame.wrap(endRad - startRad) * t;
            }
        }

        /** Bezier parameter at arc length {@code s} inches. */
        double tAtDistance(double s) {
            if (s <= 0) return 0;
            if (s >= lengthIn) return 1;
            int lo = 0, hi = arcS.length - 1;
            while (hi - lo > 1) {
                int mid = (lo + hi) >>> 1;
                if (arcS[mid] < s) lo = mid; else hi = mid;
            }
            double f = (s - arcS[lo]) / (arcS[hi] - arcS[lo]);
            return arcT[lo] + f * (arcT[hi] - arcT[lo]);
        }
    }

    /** A step on the timeline: a line to drive, or a pause. */
    static final class Step {
        final Line line; // null for a wait
        final String name;
        final double startS, endS;
        final double driveStartS, driveEndS; // excludes the line's own waits

        Step(Line line, String name, double startS, double endS, double driveStartS, double driveEndS) {
            this.line = line;
            this.name = name;
            this.startS = startS;
            this.endS = endS;
            this.driveStartS = driveStartS;
            this.driveEndS = driveEndS;
        }
    }

    final String sourceName;
    final String version;
    final String fieldMap;
    final double[] start; // x in, y in, heading rad
    final List<Line> lines = new ArrayList<>();
    final List<Step> steps = new ArrayList<>();
    final double maxVelocity, maxAcceleration, maxDeceleration;
    final double durationS;

    @SuppressWarnings("unchecked")
    VisualizerPath(String sourceName, String json) {
        this.sourceName = sourceName;
        Map<String, Object> root = (Map<String, Object>) MiniJson.parse(json);
        version = String.valueOf(root.get("version"));
        Map<String, Object> settings = root.containsKey("settings")
                ? (Map<String, Object>) root.get("settings") : new HashMap<String, Object>();
        fieldMap = String.valueOf(settings.get("fieldMap"));
        maxVelocity = num(settings, "maxVelocity", 40);
        maxAcceleration = num(settings, "maxAcceleration", 30);
        maxDeceleration = num(settings, "maxDeceleration", maxAcceleration);

        Map<String, Object> sp = (Map<String, Object>) root.get("startPoint");
        List<Object> rawLines = (List<Object>) root.get("lines");
        Map<String, Map<String, Object>> byId = new HashMap<>();
        for (Object o : rawLines) {
            Map<String, Object> l = (Map<String, Object>) o;
            if (l.get("id") != null) byId.put((String) l.get("id"), l);
        }

        // Order of driving: the sequence if there is one, otherwise the lines as listed.
        List<Map<String, Object>> order = new ArrayList<>();
        if (root.get("sequence") instanceof List && !((List<Object>) root.get("sequence")).isEmpty()) {
            for (Object o : (List<Object>) root.get("sequence")) order.add((Map<String, Object>) o);
        } else {
            for (Object o : rawLines) {
                Map<String, Object> step = new HashMap<>();
                step.put("kind", "path");
                step.put("line", o);
                order.add(step);
            }
        }

        double[] cursor = {num(sp, "x", 0), num(sp, "y", 0)};
        double firstHeading = Double.NaN;
        double t = 0;
        for (Map<String, Object> s : order) {
            String kind = String.valueOf(s.get("kind"));
            if (kind.equals("wait")) {
                double d = num(s, "durationMs", 0) / 1000.0;
                steps.add(new Step(null, String.valueOf(s.get("name")), t, t + d, t, t));
                t += d;
                continue;
            }
            if (!kind.equals("path")) continue;
            Map<String, Object> l = s.containsKey("line")
                    ? (Map<String, Object>) s.get("line") : byId.get((String) s.get("lineId"));
            if (l == null) continue;
            Line line = parseLine(l, cursor);
            if (Double.isNaN(firstHeading)) firstHeading = line.heading(0);
            lines.add(line);
            double drive = profileTime(line.lengthIn);
            double d0 = t + line.waitBeforeS;
            double d1 = d0 + drive;
            double end = d1 + line.waitAfterS;
            steps.add(new Step(line, line.name, t, end, d0, d1));
            t = end;
            cursor = line.points[line.points.length - 1];
        }
        start = new double[] {num(sp, "x", 0), num(sp, "y", 0), Double.isNaN(firstHeading) ? 0 : firstHeading};
        durationS = t;
    }

    static VisualizerPath read(Path file) throws IOException {
        return new VisualizerPath(file.getFileName().toString(),
                new String(Files.readAllBytes(file), StandardCharsets.UTF_8));
    }

    /** Pose {x in, y in, heading rad} at {@code timeS} into the path. */
    double[] poseAt(double timeS) {
        double[] last = start;
        for (Step s : steps) {
            if (s.line == null) continue;
            Line l = s.line;
            if (timeS < s.driveStartS) return last;
            if (timeS <= s.driveEndS) {
                double tt = l.tAtDistance(distanceAt(timeS - s.driveStartS, l.lengthIn));
                double[] p = l.point(tt);
                return new double[] {p[0], p[1], l.heading(tt)};
            }
            double[] e = l.points[l.points.length - 1];
            last = new double[] {e[0], e[1], l.heading(1)};
        }
        return last;
    }

    /** The step running at {@code timeS}, or null before the first or after the last. */
    Step stepAt(double timeS) {
        for (Step s : steps) if (timeS >= s.startS && timeS < s.endS) return s;
        return null;
    }

    /** Samples of a line as Pedro poses, for drawing it. */
    static double[][] sample(Line l, int n) {
        double[][] out = new double[n + 1][];
        for (int k = 0; k <= n; k++) {
            double t = (double) k / n;
            double[] p = l.point(t);
            out[k] = new double[] {p[0], p[1], l.heading(t)};
        }
        return out;
    }

    // Trapezoidal (or triangular, if too short to reach full speed) profile from rest to rest.
    private double profileTime(double length) {
        double v = maxVelocity, a = maxAcceleration, d = maxDeceleration;
        double rampDist = v * v / (2 * a) + v * v / (2 * d);
        if (length >= rampDist) return v / a + v / d + (length - rampDist) / v;
        double peak = Math.sqrt(2 * length * a * d / (a + d));
        return peak / a + peak / d;
    }

    private double distanceAt(double t, double length) {
        double v = maxVelocity, a = maxAcceleration, d = maxDeceleration;
        double rampDist = v * v / (2 * a) + v * v / (2 * d);
        double peak = length >= rampDist ? v : Math.sqrt(2 * length * a * d / (a + d));
        double tAccel = peak / a;
        double sAccel = peak * peak / (2 * a);
        double sDecel = peak * peak / (2 * d);
        double tCruise = (length - sAccel - sDecel) / peak;
        if (t <= tAccel) return 0.5 * a * t * t;
        if (t <= tAccel + tCruise) return sAccel + peak * (t - tAccel);
        double td = Math.min(t - tAccel - tCruise, peak / d);
        return Math.min(length, sAccel + peak * tCruise + peak * td - 0.5 * d * td * td);
    }

    @SuppressWarnings("unchecked")
    private static Line parseLine(Map<String, Object> l, double[] from) {
        Map<String, Object> e = (Map<String, Object>) l.get("endPoint");
        List<Object> cps = l.get("controlPoints") instanceof List
                ? (List<Object>) l.get("controlPoints") : new ArrayList<Object>();
        double[][] pts = new double[cps.size() + 2][];
        pts[0] = from.clone();
        for (int k = 0; k < cps.size(); k++) {
            Map<String, Object> c = (Map<String, Object>) cps.get(k);
            pts[k + 1] = new double[] {num(c, "x", 0), num(c, "y", 0)};
        }
        pts[pts.length - 1] = new double[] {num(e, "x", 0), num(e, "y", 0)};
        double endDeg = num(e, "endDeg", num(e, "degrees", 0));
        return new Line(
                l.get("name") == null ? "path" : String.valueOf(l.get("name")),
                pts,
                String.valueOf(e.get("heading") == null ? "linear" : e.get("heading")),
                Math.toRadians(num(e, "startDeg", endDeg)),
                Math.toRadians(endDeg),
                Math.toRadians(num(e, "degrees", endDeg)),
                Boolean.TRUE.equals(e.get("reverse")),
                num(l, "waitBeforeMs", 0) / 1000.0,
                num(l, "waitAfterMs", 0) / 1000.0);
    }

    private static double num(Map<String, Object> m, String key, double fallback) {
        Object v = m.get(key);
        return v instanceof Double ? (Double) v : fallback;
    }
}
