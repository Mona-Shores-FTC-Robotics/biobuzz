package org.firstinspires.ftc.teamcode.logging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * What happens inside the robot, drawn for AdvantageScope (doc/advantagescope-internals.md, issue #166): each held
 * piece at its place on the transfer's path, and the CAD model's moving parts as component poses. It only draws
 * what {@link FieldSim} and {@link AutoSim} decided and changes no outcome.
 *
 * <p>It reads the simulator once a loop ({@link #record}), and writes everything at the end ({@link #write}).
 * A piece climbs the J during the 0.15 s before the simulator launches it, so drawing that climb needs to know
 * when it will be launched.
 *
 * <p><b>Robot frame</b> (the CAD model's): X forward, z up, inches, origin on the floor under the chassis centre.
 * All pieces travel on the centre line. The numbers are the transfer's (doc/transfer.md on spike/164-transfer).
 * The simulator tracks only which pieces are held, and in what order. So where a piece is between those events
 * is interpolated from the transfer's timings:
 * <ul>
 *   <li><b>Entry:</b> from under the roller, up the ramp and back along the lane to its place in the queue, at the
 *   lane's 27 in/s.</li>
 *   <li><b>Queue:</b> nose to tail, the front piece's centre at X {@link FieldSim#LANE_FIRST_POLLEN_X} (POLLEN) or
 *   {@link FieldSim#LANE_FIRST_NECTAR_X} (NECTAR). Each piece moves up at 27 in/s when the one ahead leaves.</li>
 *   <li><b>Firing:</b> the front piece waits {@link #J_SPIN_UP_S} while the J spins up. It then goes round the J
 *   and up the turret axis to the launcher's exit, arriving as the simulator launches it,
 *   {@link #CLIMB_S} after the fire command.</li>
 * </ul>
 */
final class RobotInternalsLog {

    /** The CAD model's component poses, in this order (agreed with the CAD chat): the key's suffix after a robot's prefix. */
    static final String COMPONENTS = "/Internals/Components";
    static final int EXTRACTOR = 0, ROLLER = 1, TURRET = 2, J_ARM = 3, COUNT = 4;

    /** Lane speed: a hollow ball rolls at about 0.4 of the floor strands' 68 in/s. */
    static final double LANE_IN_PER_S = 27;
    /** Fire command to the piece leaving the transfer, and the J's spin-up within it. */
    static final double CLIMB_S = 0.15, J_SPIN_UP_S = 0.05;

    /** Where an entering piece is first drawn: its centre just ahead of the roller's axle, on the tiles. */
    static final double ENTRY_X = 10.0;
    /** The ramp: from X 8.0 (0.05 in up) to the lane floor at X 5.8, 0.9 in up. */
    static final double RAMP_START_X = 8.0, RAMP_START_Z = 0.05, LANE_START_X = 5.8, LANE_FLOOR_Z = 0.9;
    /** The roller: axle at rest (X, z), radius, the most it floats, and how far a POLLEN squeezes its gecko tread. */
    static final double ROLLER_X = 8.56, ROLLER_Z = 3.35, ROLLER_RADIUS = 0.95, ROLLER_FLOAT_MAX = 1.3, ROLLER_SQUEEZE = 0.4;
    /** The J-wheel's axle at rest, its radius (48 mm) and how far it grips a POLLEN. The outer J's radius about that axle. */
    static final double J_AXLE_X = -1.32, J_AXLE_Z = 4.54, J_WHEEL_RADIUS = 0.945, J_GRIP = 0.1, OUTER_J_RADIUS = 3.64;
    /**
     * The J-wheel's arm: pivot (X, z), 60 mm to the axle at 30 deg, and the most it lifts: 0.99 in at the axle
     * (the CAD chat's model, cad/advantagescope/Robot_BIOBUZZ/extractor_poses.json "model_3").
     */
    static final double J_PIVOT_X = 0.72, J_PIVOT_Z = 3.36, J_ARM_MAX_DEG = 36.8;
    /** The turret's axis. */
    static final double TURRET_X = -3.17;
    /**
     * How fast the drawn turret turns (the simulator aims instantly; a placeholder until the turret is built), so a
     * viewer sees it turn. {@code TurretErrorDeg} shows how far the drawing lags the aim.
     */
    static final double PLACEHOLDER_TURRET_DEG_PER_S = 360;

    private static final double M = AdvantageScopeFrame.METERS_PER_INCH;

    private final FieldSim sim;
    private final FieldSim.Rocker rocker;
    private final List<Track> tracks = new ArrayList<>();

    RobotInternalsLog(FieldSim sim, FieldSim.Rocker rocker) {
        this.sim = sim;
        this.rocker = rocker;
    }

    /**
     * A robot to draw, under {@code prefix}. With {@code transfer}, its pieces travel the transfer's path; otherwise
     * they sit single file on the lane floor, as {@link FieldSim}'s held pieces always have. With {@code cad}, it is
     * drawn with the CAD model, so its components and readouts are written too.
     */
    void track(String prefix, FieldSim.Bot body, RobotDesign design, boolean cad, boolean transfer) {
        tracks.add(new Track(prefix, body, design, cad, transfer));
    }

    /** One loop: the {@code index}th tracked robot's state now. {@code aiming}: its launcher is spun up or firing (the turret tracks the CELL). */
    void record(int index, boolean aiming, long us) {
        Track t = tracks.get(index);
        Frame f = new Frame();
        f.us = us;
        f.pose = t.body.pose();
        f.stored = t.body.stored.toArray(new FieldSim.Piece[0]);
        f.extractorDown = t.body.extractorDown;
        double[] aim = aiming ? rocker.aimPoint() : null;
        f.aim = aim == null ? null : new double[] {aim[0], aim[1]};
        // A piece that has left since the last loop was launched if it carries a new shot (FieldSim#launch): the
        // simulator launched it in this loop.
        if (!t.frames.isEmpty()) {
            List<FieldSim.Piece> now = Arrays.asList(f.stored);
            for (FieldSim.Piece p : t.frames.get(t.frames.size() - 1).stored) {
                if (!now.contains(p) && p.shotFrom != null && p.shotFrom != t.shotSeen.get(p)) t.launchedAt.put(p, us);
            }
        }
        for (FieldSim.Piece p : f.stored) t.shotSeen.put(p, p.shotFrom);
        t.frames.add(f);
    }

    /** Writes every loop recorded: the held pieces, and each transfer robot's components and readouts. */
    void write(WpiLog log) throws IOException {
        int frames = tracks.isEmpty() ? 0 : tracks.get(0).frames.size();
        double[][] heldLast = new double[3][];
        for (Track t : tracks) t.start();
        for (int i = 0; i < frames; i++) {
            long us = tracks.get(0).frames.get(i).us;
            List<List<double[]>> held = new ArrayList<>();
            for (int k = 0; k < 3; k++) held.add(new ArrayList<>());
            for (Track t : tracks) t.step(log, i, held);
            for (int k = 0; k < 3; k++) {
                double[] packed = new double[7 * held.get(k).size()];
                for (int j = 0; j < held.get(k).size(); j++) System.arraycopy(held.get(k).get(j), 0, packed, 7 * j, 7);
                if (!Arrays.equals(packed, heldLast[k])) {
                    log.putPose3dArray(HELD_KEYS[k], packed, us);
                    heldLast[k] = packed;
                }
            }
        }
    }

    static final String[] HELD_KEYS = {FieldSimLog.KEY_HELD_POLLEN, FieldSimLog.KEY_HELD_RED_NECTAR, FieldSimLog.KEY_HELD_BLUE_NECTAR};

    private static final class Frame {
        long us;
        double[] pose;
        FieldSim.Piece[] stored;
        double extractorDown;
        double[] aim;
    }

    private final class Track {
        final String prefix;
        final FieldSim.Bot body;
        final RobotDesign design;
        final boolean cad, transfer;
        final List<Frame> frames = new ArrayList<>();
        final Map<FieldSim.Piece, Long> launchedAt = new IdentityHashMap<>();
        final Map<FieldSim.Piece, double[]> shotSeen = new IdentityHashMap<>();
        /** Where each held piece is drawn: inches along its path ({@link Path}). */
        final Map<FieldSim.Piece, Double> along = new IdentityHashMap<>();
        /** Where a firing piece was when its climb started. */
        final Map<FieldSim.Piece, Double> climbFrom = new IdentityHashMap<>();
        final Map<Double, Path> paths = new HashMap<>();
        double turretYaw;
        long lastUs;
        double[] componentsLast;
        final double[] readoutsLast = new double[6];

        Track(String prefix, FieldSim.Bot body, RobotDesign design, boolean cad, boolean transfer) {
            this.prefix = prefix;
            this.body = body;
            this.design = design;
            this.cad = cad;
            this.transfer = transfer;
        }

        void start() {
            Arrays.fill(readoutsLast, Double.NaN);
            if (!frames.isEmpty()) lastUs = frames.get(0).us;
        }

        Path path(double r) {
            return paths.computeIfAbsent(r, k -> new Path(k, design.exitForwardIn, design.exitHeightIn));
        }

        void step(WpiLog log, int i, List<List<double[]>> held) throws IOException {
            Frame f = frames.get(i);
            double dt = (f.us - lastUs) / 1e6;
            lastUs = f.us;
            // Which pieces are in the queue, and which are climbing (to be launched within CLIMB_S).
            List<FieldSim.Piece> queue = new ArrayList<>();
            List<FieldSim.Piece> climbing = new ArrayList<>();
            if (!transfer) queue.addAll(Arrays.asList(f.stored));
            else for (FieldSim.Piece p : f.stored) {
                Long at = launchedAt.get(p);
                if (at != null && at - f.us <= Math.round(CLIMB_S * 1e6)) climbing.add(p);
                else queue.add(p);
            }
            along.keySet().retainAll(Arrays.asList(f.stored));
            // The queue: nose to tail from the J, as FieldSim#hasRoom counts it.
            double x = Double.NaN, d = 0;
            for (FieldSim.Piece p : queue) {
                double dk = 2 * p.kind.radius;
                x = Double.isNaN(x) ? (p.kind == FieldSim.Kind.POLLEN ? FieldSim.LANE_FIRST_POLLEN_X : FieldSim.LANE_FIRST_NECTAR_X) : x + (d + dk) / 2;
                d = dk;
                Path path = path(p.kind.radius);
                double target = path.alongAtX(x);
                Double s = along.get(p);
                // Preloads (and anything already held when the log starts) start in their place; a piece taken
                // in starts under the roller. Without the transfer, every piece is simply in its place.
                if (!transfer || (s == null && i == 0)) s = target;
                else if (s == null) s = 0.0;
                s = s < target ? Math.min(target, s + LANE_IN_PER_S * dt) : target;
                along.put(p, s);
            }
            for (FieldSim.Piece p : climbing) {
                double toGo = (launchedAt.get(p) - f.us) / 1e6;
                Path path = path(p.kind.radius);
                double from = climbFrom.computeIfAbsent(p, k -> along.getOrDefault(k, path.alongAtX(LANE_START_X)));
                double moving = CLIMB_S - J_SPIN_UP_S;
                double u = Math.max(0, Math.min(1, (moving - toGo) / moving));
                along.put(p, from + u * (path.length - from));
            }
            // Each piece's place, the roller's float and the J arm's lift.
            double rise = 0, lift = 0;
            for (FieldSim.Piece p : f.stored) {
                Path path = path(p.kind.radius);
                double[] xz = path.at(along.get(p));
                double r = p.kind.radius;
                // Roller: rises until it clears the piece, less the squeeze a POLLEN gets (so only a NECTAR lifts it).
                double reach = ROLLER_RADIUS + r - ROLLER_SQUEEZE, dx = xz[0] - ROLLER_X;
                if (Math.abs(dx) < reach) rise = Math.max(rise, xz[1] + Math.sqrt(reach * reach - dx * dx) - ROLLER_Z);
                lift = Math.max(lift, jLift(xz, r));
                held.get(p.kind.ordinal()).add(piecePose(f.pose, xz[0], xz[1], along.get(p), r));
            }
            rise = Math.min(ROLLER_FLOAT_MAX, rise);
            if (!cad) return;
            double error = 0;
            if (f.aim != null) {
                // Toward the aim point while the launcher is spun up or firing; otherwise it holds its last angle.
                double c = Math.cos(f.pose[2]), s = Math.sin(f.pose[2]);
                double ax = f.pose[0] + TURRET_X * c, ay = f.pose[1] + TURRET_X * s;
                double want = AdvantageScopeFrame.wrap(Math.atan2(f.aim[1] - ay, f.aim[0] - ax) - f.pose[2]);
                double turn = AdvantageScopeFrame.wrap(want - turretYaw), most = Math.toRadians(PLACEHOLDER_TURRET_DEG_PER_S) * dt;
                turretYaw = AdvantageScopeFrame.wrap(turretYaw + Math.max(-most, Math.min(most, turn)));
                error = AdvantageScopeFrame.wrap(want - turretYaw);
            }
            double[] components = components(f.extractorDown, rise, turretYaw, lift);
            for (int k = 0; k < components.length; k++) components[k] = Math.round(components[k] * 1e5) / 1e5;
            if (!Arrays.equals(components, componentsLast)) {
                log.putPose3dArray(prefix + COMPONENTS, components, f.us);
                componentsLast = components;
            }
            double[] readouts = {
                    Math.round(AutoSim.EXTRACTOR_STOWED_DEG * (1 - f.extractorDown) * 10) / 10.0,
                    Math.round(rise * 100) / 100.0,
                    Math.round(Math.toDegrees(turretYaw) * 10) / 10.0,
                    Math.round(Math.toDegrees(lift) * 10) / 10.0,
                    climbing.size(),
                    Math.round(Math.toDegrees(error) * 10) / 10.0};
            String[] names = {"ExtractorDeg", "RollerRiseIn", "TurretYawDeg", "JArmDeg", "Climbing", "TurretErrorDeg"};
            for (int k = 0; k < readouts.length; k++) {
                if (readouts[k] != readoutsLast[k]) {
                    log.put(prefix + "/Internals/" + names[k], readouts[k], f.us);
                    readoutsLast[k] = readouts[k];
                }
            }
        }
    }

    /** How far the J-wheel's arm must lift (rad) to clear a piece at {@code xz}, less the wheel's grip. */
    static double jLift(double[] xz, double r) {
        double need = J_WHEEL_RADIUS + r - J_GRIP;
        double ox = J_AXLE_X - J_PIVOT_X, oz = J_AXLE_Z - J_PIVOT_Z;
        for (double deg = 0; deg <= J_ARM_MAX_DEG; deg += 0.5) {
            double a = Math.toRadians(deg), c = Math.cos(a), s = Math.sin(a);
            // Rotation about +Y by +a: (x, z) -> (x c + z s, -x s + z c). The arm points rearward, so +a lifts the wheel.
            double wx = J_PIVOT_X + ox * c + oz * s, wz = J_PIVOT_Z - ox * s + oz * c;
            if (Math.hypot(xz[0] - wx, xz[1] - wz) >= need) return a;
        }
        return Math.toRadians(J_ARM_MAX_DEG);
    }

    /**
     * The four component poses, translation (m) then quaternion (w, x, y, z): the extractor (as
     * {@link AutoSim#cadComponents}), the roller raised {@code riseIn}, the turret turned {@code yaw} about its axis,
     * and the J arm lifted {@code lift} about its pivot.
     */
    static double[] components(double extractorDown, double riseIn, double yaw, double lift) {
        double[] out = new double[7 * COUNT];
        System.arraycopy(AutoSim.cadComponents(extractorDown), 0, out, 7 * EXTRACTOR, 7);
        out[7 * ROLLER + 2] = riseIn * M;
        out[7 * ROLLER + 3] = 1;
        // About +Z through (TURRET_X, 0): translation = p - R p.
        double px = TURRET_X * M;
        int t = 7 * TURRET;
        out[t] = px - px * Math.cos(yaw);
        out[t + 1] = -px * Math.sin(yaw);
        out[t + 3] = Math.cos(yaw / 2);
        out[t + 6] = Math.sin(yaw / 2);
        // About +Y through the arm pivot.
        double ax = J_PIVOT_X * M, az = J_PIVOT_Z * M, c = Math.cos(lift), s = Math.sin(lift);
        int j = 7 * J_ARM;
        out[j] = ax - (ax * c + az * s);
        out[j + 2] = az - (-ax * s + az * c);
        out[j + 3] = Math.cos(lift / 2);
        out[j + 5] = Math.sin(lift / 2);
        return out;
    }

    /**
     * A held piece at {@code x} ahead of the robot's centre and {@code z} up, on its centre line, as an
     * AdvantageScope Pose3d (Center/Rotated, metres). It rolls as it goes: turned {@code along / r} about the
     * robot's left axis.
     */
    static double[] piecePose(double[] pose, double x, double z, double along, double r) {
        double c = Math.cos(pose[2]), s = Math.sin(pose[2]);
        double fx = pose[0] + x * c, fy = pose[1] + x * s;
        double half = -along / r / 2;
        // About the robot's left axis (-sin h, cos h, 0) in Pedro; Pedro -> Center/Rotated turns an axis (x, y) to (-y, x).
        double qw = Math.cos(half), qxP = -s * Math.sin(half), qyP = c * Math.sin(half);
        return new double[] {
                mm(AdvantageScopeFrame.xMeters(fx, fy)), mm(AdvantageScopeFrame.yMeters(fx, fy)), mm(z * M),
                Math.round(qw * 1e3) / 1e3, Math.round(-qyP * 1e3) / 1e3, Math.round(qxP * 1e3) / 1e3, 0};
    }

    private static double mm(double meters) {
        return Math.round(meters * 1e3) / 1e3;
    }

    /**
     * A piece's path through the transfer, for one piece radius, in the robot frame (X, z), inches: under the
     * roller, up the ramp, back along the lane floor, round the outer J (its centre on a circle about the
     * J-wheel's axle), up the turret axis, and across to the launcher's exit.
     */
    static final class Path {
        final double[] xs, zs, cum;
        final double length;

        Path(double r, double exitX, double exitZ) {
            List<double[]> pts = new ArrayList<>();
            pts.add(new double[] {ENTRY_X, r});
            pts.add(new double[] {ROLLER_X, r});
            pts.add(new double[] {RAMP_START_X, RAMP_START_Z + r});
            pts.add(new double[] {LANE_START_X, LANE_FLOOR_Z + r});
            // The outer J: the piece's centre runs OUTER_J_RADIUS - r from the wheel's axle, from straight below it
            // to straight behind it. Straight below is the lane floor plus r (the J starts at the floor).
            double rj = OUTER_J_RADIUS - r;
            for (int k = 0; k <= 16; k++) {
                double a = Math.PI / 2 * k / 16;
                pts.add(new double[] {J_AXLE_X - rj * Math.sin(a), J_AXLE_Z - rj * Math.cos(a)});
            }
            double column = J_AXLE_X - rj;
            pts.add(new double[] {column, exitZ - 1});
            pts.add(new double[] {exitX, exitZ});
            xs = new double[pts.size()];
            zs = new double[pts.size()];
            cum = new double[pts.size()];
            for (int k = 0; k < pts.size(); k++) {
                xs[k] = pts.get(k)[0];
                zs[k] = pts.get(k)[1];
                if (k > 0) cum[k] = cum[k - 1] + Math.hypot(xs[k] - xs[k - 1], zs[k] - zs[k - 1]);
            }
            length = cum[cum.length - 1];
        }

        /** (X, z) at {@code s} inches along. */
        double[] at(double s) {
            s = Math.max(0, Math.min(length, s));
            int k = 1;
            while (k < cum.length - 1 && cum[k] < s) k++;
            double seg = cum[k] - cum[k - 1], u = seg == 0 ? 0 : (s - cum[k - 1]) / seg;
            return new double[] {xs[k - 1] + u * (xs[k] - xs[k - 1]), zs[k - 1] + u * (zs[k] - zs[k - 1])};
        }

        /** How far along the path a centre at {@code x} is, on the way in (entry to the J). */
        double alongAtX(double x) {
            for (int k = 1; k < xs.length; k++) {
                if (xs[k] <= x && xs[k - 1] >= x && xs[k - 1] != xs[k]) {
                    return cum[k - 1] + (xs[k - 1] - x) / (xs[k - 1] - xs[k]) * (cum[k] - cum[k - 1]);
                }
            }
            return x > xs[0] ? 0 : cum[3];
        }
    }
}
