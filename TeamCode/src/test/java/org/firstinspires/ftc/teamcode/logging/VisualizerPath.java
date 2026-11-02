package org.firstinspires.ftc.teamcode.logging;

import com.pedropathing.api.Paths;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.pedropathing.paths.interpolator.Interpolator;
import com.pedropathing.paths.interpolator.PiecewiseInterpolator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Pedro Visualizer file ({@code .pp}, format 1.5.0) turned into <b>real Pedro 3 paths</b>, so the
 * simulated log drives what the robot would drive.
 *
 * <p>Each path is built with the same Pedro 3.0.1 calls the Visualizer's own code export emits
 * ({@code Pedro-Pathing/Visualizer, src/lib/codegen}): {@code Paths.line / curve / through / path},
 * then {@code .linear / .constant / .tangent / .reverseTangent / .heading(Interpolator...)}. Every
 * point and heading in the log comes from Pedro's {@code Path.get(t)}, not from our own geometry.
 *
 * <p>Follows the Visualizer's rules for what a file means:
 * <ul>
 *   <li>Coordinates are inches on the Visualizer's 141.5 in field, origin at the image's bottom-left;
 *       headings are degrees in the file.</li>
 *   <li>A compound path ("group") is one Pedro path; if it has a heading, that heading spans the whole
 *       group and its children's are ignored.</li>
 *   <li>The {@code sequence} lists segment ids; consecutive segments of one group are one path, as in
 *       the export. Wait steps and per-path {@code waitBeforeMs}/{@code waitAfterMs} are pauses.</li>
 *   <li>Piecewise headings become {@code Interpolator.piecewise().until(end, ...)}, reversed pieces
 *       {@code .reverse()} (never on constant), and the last piece is stretched to 1.0, as the export
 *       does.</li>
 * </ul>
 *
 * <p>Only format 1.5.0 is read. A file from an older Visualizer throws with a message saying to open
 * and re-save it, rather than being guessed at.
 *
 * <p>Timing is an approximation: each path runs a trapezoidal profile at the file's
 * {@code maxVelocity}/{@code maxAcceleration}/{@code maxDeceleration}, from rest to rest, and the
 * distance travelled is turned into Pedro's curve parameter with {@code curve.parameter(completion)}.
 * Pedro's follower carries speed through joins; that changes when the robot gets somewhere, not
 * where it goes.
 */
final class VisualizerPath {

    static final String FORMAT = "1.5.0";

    /** One Pedro path on the timeline, or a pause. */
    static final class Step {
        final String name;
        final Path path; // null for a pause
        final double startS, driveStartS, driveEndS, endS;

        Step(String name, Path path, double startS, double driveStartS, double driveEndS, double endS) {
            this.name = name;
            this.path = path;
            this.startS = startS;
            this.driveStartS = driveStartS;
            this.driveEndS = driveEndS;
            this.endS = endS;
        }
    }

    /** An atomic segment as the Visualizer draws it, kept for the geometry cross-check. */
    static final class Segment {
        final String id;
        final Path path;
        final boolean throughPoints;

        Segment(String id, Path path, boolean throughPoints) {
            this.id = id;
            this.path = path;
            this.throughPoints = throughPoints;
        }
    }

    private static final PoseFactory POSES = PoseFactory.degrees();

    final String sourceName;
    final String version;
    final String fieldMap;
    final Pose start;
    final double maxVelocity, maxAcceleration, maxDeceleration;
    final List<Step> steps = new ArrayList<>();
    final List<Segment> segments = new ArrayList<>();
    final double durationS;

    @SuppressWarnings("unchecked")
    VisualizerPath(String sourceName, String json) {
        this.sourceName = sourceName;
        Map<String, Object> root = (Map<String, Object>) MiniJson.parse(json);
        version = String.valueOf(root.get("version"));
        if (!FORMAT.equals(version)) {
            throw new IllegalArgumentException(sourceName + " is Visualizer format " + version
                    + "; this reads " + FORMAT + ". Open it in the current Visualizer and save it again.");
        }
        Map<String, Object> settings = map(root.get("settings"));
        fieldMap = String.valueOf(settings.get("fieldMap"));
        maxVelocity = num(settings, "maxVelocity", 40);
        maxAcceleration = num(settings, "maxAcceleration", 30);
        maxDeceleration = num(settings, "maxDeceleration", maxAcceleration);

        Map<String, Object> sp = map(root.get("startPoint"));
        start = POSES.of(num(sp, "x", 0), num(sp, "y", 0), num(sp, "headingDeg", 0));

        // Build every top-level path, remembering which top-level path each segment id belongs to.
        List<Map<String, Object>> lines = list(root.get("lines"));
        Map<String, Integer> rootOf = new HashMap<>();
        List<Path> built = new ArrayList<>();
        Pose cursor = start;
        for (int i = 0; i < lines.size(); i++) {
            Map<String, Object> line = lines.get(i);
            indexIds(line, i, rootOf);
            Pose[] end = new Pose[1];
            built.add(build(line, cursor, null, end));
            cursor = end[0];
        }

        // Walk the sequence: consecutive segments of one top-level path are one step.
        List<Map<String, Object>> sequence = list(root.get("sequence"));
        if (sequence.isEmpty()) {
            for (int i = 0; i < lines.size(); i++) sequence.add(pathItem((String) lines.get(i).get("id")));
        }
        double t = 0;
        int previousRoot = -1;
        for (Map<String, Object> item : sequence) {
            if ("wait".equals(item.get("kind"))) {
                double d = num(item, "durationMs", 0) / 1000.0;
                steps.add(new Step(String.valueOf(item.get("name")), null, t, t, t, t + d));
                t += d;
                previousRoot = -1;
                continue;
            }
            Integer r = rootOf.get(String.valueOf(item.get("lineId")));
            if (r == null || r == previousRoot) continue;
            previousRoot = r;
            Map<String, Object> line = lines.get(r);
            Path path = built.get(r);
            double driveStart = t + num(line, "waitBeforeMs", 0) / 1000.0;
            double driveEnd = driveStart + profileTime(path.curve.length());
            double end = driveEnd + num(line, "waitAfterMs", 0) / 1000.0;
            steps.add(new Step(name(line), path, t, driveStart, driveEnd, end));
            t = end;
        }
        durationS = t;
    }

    static VisualizerPath read(java.nio.file.Path file) throws IOException {
        return new VisualizerPath(file.getFileName().toString(),
                new String(Files.readAllBytes(file), StandardCharsets.UTF_8));
    }

    /** Where the robot is {@code timeS} into the Auto, from Pedro's own {@code Path.get(t)}. */
    Pose poseAt(double timeS) {
        Pose last = start;
        for (Step s : steps) {
            if (s.path == null) continue;
            if (timeS < s.driveStartS) return last;
            if (timeS <= s.driveEndS) {
                double length = s.path.curve.length();
                double completion = length == 0 ? 1 : distanceAt(timeS - s.driveStartS, length) / length;
                return s.path.get(clamp01(s.path.curve.parameter(clamp01(completion))));
            }
            last = s.path.endPose();
        }
        return last;
    }

    Step stepAt(double timeS) {
        for (Step s : steps) if (timeS >= s.startS && timeS < s.endS) return s;
        return null;
    }

    /** Evenly spaced poses along a Pedro path, for drawing it. */
    static Pose[] sample(Path path, int n) {
        Pose[] out = new Pose[n + 1];
        for (int k = 0; k <= n; k++) out[k] = path.get(clamp01(path.curve.parameter((double) k / n)));
        return out;
    }

    // ---- Building Pedro paths, as the Visualizer's export does ----------------------------------

    /**
     * @param inheritedHeading non-null when an enclosing group supplies the heading, in which case
     *                         this path gets none of its own
     * @param endOut receives the pose this path ends at, which the next path starts from
     */
    private Path build(Map<String, Object> line, Pose from, Object inheritedHeading, Pose[] endOut) {
        if ("compound".equals(line.get("kind"))) {
            Map<String, Object> groupHeading = map(line.get("heading"));
            boolean groupHasHeading = !groupHeading.isEmpty();
            List<Path> children = new ArrayList<>();
            Pose cursor = from;
            for (Map<String, Object> child : list(line.get("segments"))) {
                Pose[] end = new Pose[1];
                children.add(build(child, cursor, groupHasHeading ? groupHeading : inheritedHeading, end));
                cursor = end[0];
            }
            Path group = Paths.path(children.toArray(new Path[0]));
            endOut[0] = cursor;
            return groupHasHeading ? group.heading(interpolator(groupHeading, group, from, cursor)) : group;
        }

        Map<String, Object> endPoint = map(line.get("endPoint"));
        Pose end = POSES.of(num(endPoint, "x", 0), num(endPoint, "y", 0), 0);
        List<Map<String, Object>> through = list(line.get("throughPoints"));
        List<Map<String, Object>> controls = list(line.get("controlPoints"));
        Path path;
        if (!through.isEmpty()) {
            List<Pose> poses = new ArrayList<>();
            poses.add(from);
            for (Map<String, Object> p : through) poses.add(POSES.of(num(p, "x", 0), num(p, "y", 0), 0));
            poses.add(end);
            path = Paths.through(poses.toArray(new Pose[0]));
        } else if (controls.isEmpty()) {
            path = Paths.line(from, end);
        } else {
            List<Pose> poses = new ArrayList<>();
            poses.add(from);
            for (Map<String, Object> p : controls) poses.add(POSES.of(num(p, "x", 0), num(p, "y", 0), 0));
            poses.add(end);
            path = Paths.curve(poses.toArray(new Pose[0]));
        }
        segments.add(new Segment(String.valueOf(line.get("id")), path, !through.isEmpty()));
        endOut[0] = end;
        if (inheritedHeading != null) return path;
        return path.heading(interpolator(map(line.get("heading")), path, from, end));
    }

    private static Interpolator interpolator(Map<String, Object> heading, Path path, Pose from, Pose to) {
        String type = String.valueOf(heading.get("type"));
        switch (type) {
            case "constant":
                return Interpolator.constant(POSES.of(to.x(), to.y(), num(heading, "degrees", 0)));
            case "linear":
                return Interpolator.linear(POSES.of(from.x(), from.y(), num(heading, "startDeg", 0)),
                        POSES.of(to.x(), to.y(), num(heading, "endDeg", 0)));
            case "tangential":
                return Boolean.TRUE.equals(heading.get("reverse"))
                        ? Interpolator.tangent.reverse() : Interpolator.tangent;
            case "piecewise":
                return piecewise(map(heading.get("piecewiseHeading")), path, to);
            default:
                throw new IllegalArgumentException("unknown heading type " + type);
        }
    }

    private static Interpolator piecewise(Map<String, Object> spec, Path path, Pose to) {
        List<Map<String, Object>> pieces = list(spec.get("segments"));
        Collections.sort(pieces, (a, b) -> Double.compare(num(a, "startProgress", 0), num(b, "startProgress", 0)));
        PiecewiseInterpolator p = Interpolator.piecewise();
        double previous = 0;
        Double lastEndAngle = null;
        for (int i = 0; i < pieces.size(); i++) {
            Map<String, Object> piece = pieces.get(i);
            double until = Math.min(Math.max(num(piece, "endProgress", 1), 0), 1);
            if (i == pieces.size() - 1) until = 1.0; // the export stretches the last piece to 1
            if (until <= previous) continue;
            Map<String, Object> params = map(piece.get("parameters"));
            boolean linked = Boolean.TRUE.equals(piece.get("continueFromPrevious")) && lastEndAngle != null;
            String kind = String.valueOf(piece.get("interpolationType"));
            Interpolator it;
            switch (kind) {
                case "constant": {
                    double deg = linked ? lastEndAngle : num(params, "degrees", 0);
                    it = Interpolator.constant(POSES.of(to.x(), to.y(), deg));
                    lastEndAngle = deg;
                    break;
                }
                case "linear": {
                    double s = linked ? lastEndAngle : num(params, "startDeg", 0);
                    double e = num(params, "endDeg", 0);
                    it = Interpolator.linear(POSES.of(to.x(), to.y(), s), POSES.of(to.x(), to.y(), e));
                    lastEndAngle = e;
                    break;
                }
                case "tangential":
                    it = Interpolator.tangent;
                    lastEndAngle = null;
                    break;
                case "facing-point": {
                    Map<String, Object> point = map(params.get("point"));
                    if (point.isEmpty()) continue; // the export skips these too
                    it = Interpolator.facingPoint(POSES.of(num(point, "x", 0), num(point, "y", 0), 0));
                    lastEndAngle = null;
                    break;
                }
                default:
                    throw new IllegalArgumentException("unknown piecewise type " + kind);
            }
            if (Boolean.TRUE.equals(piece.get("reversed")) && !"constant".equals(kind)) it = it.reverse();
            p.until(until, it);
            previous = until;
        }
        return p;
    }

    // ---- Timing -----------------------------------------------------------------------------------

    private double profileTime(double length) {
        double v = maxVelocity, a = maxAcceleration, d = maxDeceleration;
        double ramps = v * v / (2 * a) + v * v / (2 * d);
        if (length >= ramps) return v / a + v / d + (length - ramps) / v;
        double peak = Math.sqrt(2 * length * a * d / (a + d));
        return peak / a + peak / d;
    }

    private double distanceAt(double t, double length) {
        double v = maxVelocity, a = maxAcceleration, d = maxDeceleration;
        double ramps = v * v / (2 * a) + v * v / (2 * d);
        double peak = length >= ramps ? v : Math.sqrt(2 * length * a * d / (a + d));
        double tAccel = peak / a;
        double sAccel = peak * peak / (2 * a);
        double tCruise = (length - sAccel - peak * peak / (2 * d)) / peak;
        if (t <= tAccel) return 0.5 * a * t * t;
        if (t <= tAccel + tCruise) return sAccel + peak * (t - tAccel);
        double td = Math.min(t - tAccel - tCruise, peak / d);
        return Math.min(length, sAccel + peak * tCruise + peak * td - 0.5 * d * td * td);
    }

    // ---- JSON helpers -------------------------------------------------------------------------------

    private static void indexIds(Map<String, Object> line, int root, Map<String, Integer> rootOf) {
        rootOf.put(String.valueOf(line.get("id")), root);
        for (Map<String, Object> child : list(line.get("segments"))) indexIds(child, root, rootOf);
    }

    private static Map<String, Object> pathItem(String id) {
        Map<String, Object> m = new HashMap<>();
        m.put("kind", "path");
        m.put("lineId", id);
        return m;
    }

    private static String name(Map<String, Object> line) {
        Object n = line.get("name");
        return n == null || String.valueOf(n).isEmpty() ? String.valueOf(line.get("id")) : String.valueOf(n);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object o) {
        return o instanceof Map ? (Map<String, Object>) o : new HashMap<String, Object>();
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> list(Object o) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (o instanceof List) for (Object x : (List<Object>) o) out.add((Map<String, Object>) x);
        return out;
    }

    private static double num(Map<String, Object> m, String key, double fallback) {
        Object v = m.get(key);
        return v instanceof Double ? (Double) v : fallback;
    }

    private static double clamp01(double v) {
        return Math.max(0, Math.min(1, v));
    }
}
