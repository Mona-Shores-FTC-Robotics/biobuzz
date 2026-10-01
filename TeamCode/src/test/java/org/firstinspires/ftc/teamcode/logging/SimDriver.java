package org.firstinspires.ftc.teamcode.logging;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.vision.HiveGeometry;
import org.firstinspires.ftc.teamcode.vision.HiveState;

import java.util.ArrayList;
import java.util.List;

/**
 * The simulated robot's driver: picks up real pieces from {@link FieldSim}, carries them to its
 * alliance's raised CELL and launches them, and goes round to the other side when the HIVE tips.
 *
 * <p>It reads the simulation's truth (where every piece is, which CELL is up), which a real robot
 * cannot; it is here to make a believable match for the logger, <b>not BIOBUZZ strategy</b>.
 *
 * <p>Movement is a polyline route from the current pose to a goal, around the HIVE frame, driven
 * with a smooth start and stop. Each loop {@link #update} advances it and reports what the
 * mechanisms should be doing.
 */
final class SimDriver {

    static final double HALF = FieldSim.ROBOT_SIZE_IN / 2;
    static final double AVERAGE_SPEED_IN_PER_S = 38;
    /** How far back from the raised CELL's opening the robot shoots from. */
    static final double SHOT_DISTANCE_IN = 38;
    static final double SHOT_INTERVAL_S = 0.45;
    static final double CREEP_S = 1.3;

    /** The HIVE frame's footprint, grown by the robot's half-diagonal: the robot stays out. */
    static final double KEEP_OUT_MIN_X = FieldSim.CENTRE_IN - HiveGeometry.FRAME_WIDTH_IN / 2 - 13;
    static final double KEEP_OUT_MAX_X = FieldSim.CENTRE_IN + HiveGeometry.FRAME_WIDTH_IN / 2 + 13;
    static final double KEEP_OUT_MIN_Y = FieldSim.CENTRE_IN - HiveGeometry.FRAME_DEPTH_IN / 2 - 13;
    static final double KEEP_OUT_MAX_Y = FieldSim.CENTRE_IN + HiveGeometry.FRAME_DEPTH_IN / 2 + 13;

    /** A timed drive along a polyline of Pedro poses {x, y, heading}. */
    static final class Route {
        final double start, end;
        final List<double[]> points;
        final double[] cumulative;
        final boolean showPath;

        Route(double start, List<double[]> points, boolean showPath) {
            this.points = points;
            this.showPath = showPath;
            cumulative = new double[points.size()];
            for (int i = 1; i < points.size(); i++) {
                cumulative[i] = cumulative[i - 1] + Math.hypot(points.get(i)[0] - points.get(i - 1)[0],
                        points.get(i)[1] - points.get(i - 1)[1]);
            }
            double turn = Math.abs(AdvantageScopeFrame.wrap(last()[2] - points.get(0)[2]));
            this.start = start;
            this.end = start + Math.max(0.6, length() / AVERAGE_SPEED_IN_PER_S + 0.25 * turn);
        }

        Route(double start, double duration, double[] from, double[] to) {
            this.points = new ArrayList<>();
            points.add(from);
            points.add(to);
            showPath = false;
            cumulative = new double[] {0, Math.hypot(to[0] - from[0], to[1] - from[1])};
            this.start = start;
            this.end = start + duration;
        }

        double length() {
            return cumulative[cumulative.length - 1];
        }

        double[] last() {
            return points.get(points.size() - 1);
        }

        /** Smooth start and stop (smoothstep) along the polyline; heading eases from first to last. */
        double[] at(double t) {
            double u = Math.max(0, Math.min(1, (t - start) / (end - start)));
            double s = u * u * (3 - 2 * u);
            double along = s * length();
            int i = 1;
            while (i < cumulative.length - 1 && cumulative[i] < along) i++;
            double segment = cumulative[i] - cumulative[i - 1];
            double f = segment < 1e-9 ? 1 : (along - cumulative[i - 1]) / segment;
            double[] a = points.get(i - 1), b = points.get(i);
            double h0 = points.get(0)[2];
            return new double[] {a[0] + (b[0] - a[0]) * f, a[1] + (b[1] - a[1]) * f,
                    h0 + AdvantageScopeFrame.wrap(last()[2] - h0) * s};
        }

        /** The route as packed AdvantageScope poses, for {@code /Path/Active}. */
        double[] packed() {
            int n = 24;
            double[] out = new double[3 * (n + 1)];
            for (int i = 0; i <= n; i++) {
                double[] p = at(start + (end - start) * i / n);
                out[3 * i] = AdvantageScopeFrame.xMeters(p[0], p[1]);
                out[3 * i + 1] = AdvantageScopeFrame.yMeters(p[0], p[1]);
                out[3 * i + 2] = AdvantageScopeFrame.headingRad(p[2]);
            }
            return out;
        }
    }

    enum Task { PLAN, TO_PICKUP, CREEP, TO_SHOT, AIM, FIRE }

    /** What the mechanisms should do this loop. */
    static final class Output {
        boolean intake;
        boolean spin;
        boolean fire;
        /** Lane that fired this loop (0 left, 1 centre, 2 right), or −1. */
        int shotLane = -1;
        /** The predicted arc of that shot, Pedro inches, or null. */
        List<double[]> shotArc;
    }

    private final FieldSim sim;
    private final Alliance alliance;
    private double[] pose;
    private Task task = Task.PLAN;
    private Route route;
    private double[] creepTo;
    private int shotEnd;
    private double nextShot;
    private int lane;
    private final Output out = new Output();

    SimDriver(FieldSim sim, Alliance alliance, double[] start) {
        this.sim = sim;
        this.alliance = alliance;
        this.pose = start.clone();
    }

    double[] pose() {
        return pose.clone();
    }

    /** The route being driven, or null while still. */
    Route route() {
        return task == Task.TO_PICKUP || task == Task.TO_SHOT || task == Task.CREEP ? route : null;
    }

    Task task() {
        return task;
    }

    /**
     * One loop. {@code launcherReady} is the flywheels' state, which the match owns.
     */
    Output update(double t, boolean enabled, boolean auto, boolean launcherReady) {
        out.intake = false;
        out.spin = false;
        out.fire = false;
        out.shotLane = -1;
        out.shotArc = null;
        if (!enabled) {
            task = Task.PLAN;
            return out;
        }
        FieldSim.Rocker hive = sim.rocker(alliance);
        switch (task) {
            case PLAN:
                plan(t, auto);
                break;
            case TO_PICKUP:
                pose = route.at(t);
                if (t >= route.end) {
                    task = Task.CREEP;
                    route = new Route(t, CREEP_S, pose, creepTo);
                }
                break;
            case CREEP:
                pose = route.at(t);
                out.intake = true;
                if (sim.stored.size() >= FieldSim.PLACEHOLDER_ROBOT_CAPACITY || t >= route.end + 0.3) {
                    task = Task.PLAN;
                }
                break;
            case TO_SHOT:
                pose = route.at(t);
                out.spin = true;
                if (t >= route.end) task = Task.AIM;
                break;
            case AIM:
                out.spin = true;
                if (hive.state() == HiveState.TRANSITION) break; // wait for it to settle
                if (hive.raisedEnd() != shotEnd) {
                    task = Task.PLAN; // it tipped while we drove: go round
                    break;
                }
                if (launcherReady) {
                    task = Task.FIRE;
                    nextShot = t;
                }
                break;
            case FIRE:
                out.spin = true;
                out.fire = true;
                if (hive.raisedEnd() != shotEnd) {
                    // The HIVE is tipping or has tipped: hold what is left and re-plan once it settles.
                    out.fire = false;
                    if (hive.state() != HiveState.TRANSITION) task = Task.PLAN;
                    break;
                }
                if (t >= nextShot) {
                    double[] aim = hive.aimPoint();
                    double[] from = sim.exitPoint();
                    double[] v = aim == null ? null : sim.launch(aim);
                    if (v != null) {
                        out.shotLane = lane;
                        lane = (lane + 1) % 3;
                        out.shotArc = FieldSim.arc(from, v, aim[2] - 4, 30);
                    }
                    nextShot = t + SHOT_INTERVAL_S;
                }
                if (sim.stored.isEmpty()) {
                    out.fire = false;
                    task = Task.PLAN;
                }
                break;
            default:
                break;
        }
        return out;
    }

    private void plan(double t, boolean auto) {
        int held = sim.stored.size();
        double[] pickup = held < FieldSim.PLACEHOLDER_ROBOT_CAPACITY ? choosePickup() : null;
        FieldSim.Rocker hive = sim.rocker(alliance);
        if (held > 0 && (pickup == null || held >= FieldSim.PLACEHOLDER_ROBOT_CAPACITY)) {
            if (hive.state() == HiveState.TRANSITION) return;
            shotEnd = hive.raisedEnd();
            double[] aim = hive.aimPoint();
            double y = hive.openingCentre()[1] + shotEnd * SHOT_DISTANCE_IN;
            double[] goal = {hive.centreX, y, Math.atan2(aim[1] - y, aim[0] - hive.centreX)};
            route = new Route(t, path(pose, goal), auto);
            task = Task.TO_SHOT;
        } else if (pickup != null) {
            double[] end = {pickup[0], pickup[1], pickup[2]};
            double[] approach = {pickup[3], pickup[4], pickup[2]};
            creepTo = end;
            route = new Route(t, path(pose, approach), auto);
            task = Task.TO_PICKUP;
        }
    }

    /**
     * The nearest piece this robot can take: POLLEN or its own NECTAR, at rest on the tiles or at
     * the bottom of a Flower, out from under the HIVE, and inside the intake from a pose the robot
     * fits in. Returns {@code {endX, endY, heading, approachX, approachY}} or null.
     */
    private double[] choosePickup() {
        double best = Double.POSITIVE_INFINITY;
        double[] choice = null;
        FieldSim.Kind ownNectar = alliance == Alliance.RED ? FieldSim.Kind.RED_NECTAR : FieldSim.Kind.BLUE_NECTAR;
        for (FieldSim.Piece p : sim.pieces) {
            if (p.where != FieldSim.Where.FIELD || p.cell != null || p.z > 6) continue;
            if (p.kind != FieldSim.Kind.POLLEN && p.kind != ownNectar) continue;
            if (Math.abs(p.vx) + Math.abs(p.vy) + Math.abs(p.vz) > 2) continue;
            if (p.x > KEEP_OUT_MIN_X + 11 && p.x < KEEP_OUT_MAX_X - 11
                    && p.y > KEEP_OUT_MIN_Y + 11 && p.y < KEEP_OUT_MAX_Y - 11) {
                continue; // under the HIVE frame
            }
            double[] option = pickupPose(p);
            if (option == null) continue;
            double cost = pathLength(path(pose, new double[] {option[3], option[4], option[2]}));
            if (cost < best) {
                best = cost;
                choice = option;
            }
        }
        return choice;
    }

    private double[] pickupPose(FieldSim.Piece p) {
        double size = FieldSim.FIELD_SIZE_IN;
        double heading;
        double[] walls = {p.x, size - p.x, p.y, size - p.y};
        int nearest = 0;
        for (int i = 1; i < 4; i++) if (walls[i] < walls[nearest]) nearest = i;
        if (walls[nearest] < 14) {
            heading = new double[] {Math.PI, 0, -Math.PI / 2, Math.PI / 2}[nearest];
        } else {
            heading = Math.atan2(p.y - pose[1], p.x - pose[0]);
        }
        double c = Math.cos(heading), s = Math.sin(heading);
        double reach = HALF * (Math.abs(c) + Math.abs(s)) + 0.5;
        double ex = clamp(p.x - c * (HALF - 1), reach, size - reach);
        double ey = clamp(p.y - s * (HALF - 1), reach, size - reach);
        // Still inside the intake once clamped against the walls?
        double lx = (p.x - ex) * c + (p.y - ey) * s, ly = -(p.x - ex) * s + (p.y - ey) * c;
        if (lx < HALF - 2 || lx > HALF + 3 || Math.abs(ly) > FieldSim.PLACEHOLDER_INTAKE_HALF_WIDTH_IN - 1) return null;
        if (insideKeepOut(ex, ey)) return null;
        double ax = clamp(ex - c * 10, reach, size - reach);
        double ay = clamp(ey - s * 10, reach, size - reach);
        if (insideKeepOut(ax, ay)) return null;
        return new double[] {ex, ey, heading, ax, ay};
    }

    /** A route from {@code from} to {@code to} that keeps out of the HIVE frame: straight, or round its corners. */
    static List<double[]> path(double[] from, double[] to) {
        List<double[]> straight = new ArrayList<>();
        straight.add(from);
        straight.add(to);
        if (!crossesKeepOut(from, to)) return straight;
        double m = 2;
        double[][] corners = {
                {KEEP_OUT_MIN_X - m, KEEP_OUT_MIN_Y - m}, {KEEP_OUT_MIN_X - m, KEEP_OUT_MAX_Y + m},
                {KEEP_OUT_MAX_X + m, KEEP_OUT_MAX_Y + m}, {KEEP_OUT_MAX_X + m, KEEP_OUT_MIN_Y - m}};
        // Shortest path through the corners (a tiny visibility graph).
        int n = corners.length + 2;
        double[][] nodes = new double[n][];
        nodes[0] = from;
        for (int i = 0; i < corners.length; i++) nodes[i + 1] = corners[i];
        nodes[n - 1] = to;
        double[] dist = new double[n];
        int[] prev = new int[n];
        boolean[] done = new boolean[n];
        java.util.Arrays.fill(dist, Double.POSITIVE_INFINITY);
        java.util.Arrays.fill(prev, -1);
        dist[0] = 0;
        for (int iter = 0; iter < n; iter++) {
            int u = -1;
            for (int i = 0; i < n; i++) if (!done[i] && (u < 0 || dist[i] < dist[u])) u = i;
            if (u < 0 || dist[u] == Double.POSITIVE_INFINITY) break;
            done[u] = true;
            for (int v = 0; v < n; v++) {
                if (done[v] || crossesKeepOut(nodes[u], nodes[v])) continue;
                double d = dist[u] + Math.hypot(nodes[v][0] - nodes[u][0], nodes[v][1] - nodes[u][1]);
                if (d < dist[v]) {
                    dist[v] = d;
                    prev[v] = u;
                }
            }
        }
        if (prev[n - 1] < 0) return straight;
        List<double[]> out = new ArrayList<>();
        for (int v = n - 1; v >= 0; v = prev[v]) {
            double[] p = nodes[v];
            out.add(0, p.length >= 3 ? p : new double[] {p[0], p[1], 0});
            if (v == 0) break;
        }
        return out;
    }

    static double pathLength(List<double[]> points) {
        double d = 0;
        for (int i = 1; i < points.size(); i++) {
            d += Math.hypot(points.get(i)[0] - points.get(i - 1)[0], points.get(i)[1] - points.get(i - 1)[1]);
        }
        return d;
    }

    static boolean insideKeepOut(double x, double y) {
        return x > KEEP_OUT_MIN_X && x < KEEP_OUT_MAX_X && y > KEEP_OUT_MIN_Y && y < KEEP_OUT_MAX_Y;
    }

    /** Whether the segment passes through the keep-out box (touching its edge is fine). */
    static boolean crossesKeepOut(double[] a, double[] b) {
        double t0 = 0, t1 = 1;
        double dx = b[0] - a[0], dy = b[1] - a[1];
        double[] p = {-dx, dx, -dy, dy};
        double[] q = {a[0] - KEEP_OUT_MIN_X, KEEP_OUT_MAX_X - a[0], a[1] - KEEP_OUT_MIN_Y, KEEP_OUT_MAX_Y - a[1]};
        for (int i = 0; i < 4; i++) {
            if (Math.abs(p[i]) < 1e-12) {
                if (q[i] <= 0) return false;
            } else {
                double r = q[i] / p[i];
                if (p[i] < 0) t0 = Math.max(t0, r);
                else t1 = Math.min(t1, r);
            }
        }
        return t1 - t0 > 1e-6;
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
