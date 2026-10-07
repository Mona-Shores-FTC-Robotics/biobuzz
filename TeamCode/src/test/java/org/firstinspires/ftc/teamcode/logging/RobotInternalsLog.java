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
 * A piece is fed up into the flywheels during the 0.15 s before the simulator launches it, so drawing that needs
 * to know when it will be launched.
 *
 * <p><b>Robot frame</b> (the CAD model's): X forward, z up, inches, origin on the floor under the chassis centre.
 * All pieces travel on the centre line. The transfer is v2 (the CAD chat's 12d34bb, on the mentor's CAD): a ramp, a
 * wheel lane rising 17 deg, a feeder cup on the turret's axis, and two foam feeder wheels that pop a piece straight up
 * into the launcher's flywheels. The simulator tracks only which pieces are held, and in what order. So where a piece
 * is between those events is interpolated:
 * <ul>
 *   <li><b>Entry:</b> from under the roller, up the ramp and along the lane to its place in the queue, at
 *   {@link #LANE_IN_PER_S}.</li>
 *   <li><b>Queue:</b> the front piece seated in the cup, the rest nose to tail behind it along the lane. Each moves up
 *   at {@link #LANE_IN_PER_S} when the one ahead leaves; while a piece is being fed, the front feeder holds the next
 *   one back at the lane's end.</li>
 *   <li><b>Firing:</b> the piece in the cup waits {@link #FEED_SPIN_UP_S} while the feeders spin up. It then rises
 *   straight up through the flywheels to the launcher's exit, arriving as the simulator launches it,
 *   {@link #CLIMB_S} after the fire command.</li>
 * </ul>
 */
final class RobotInternalsLog {

    /** The CAD model's component poses, in this order (agreed with the CAD chat): the key's suffix after a robot's prefix. */
    static final String COMPONENTS = "/Internals/Components";
    static final int EXTRACTOR = 0, ROLLER = 1, TURRET = 2, FRONT_FEEDER = 3, INTAKE_ROLLER = 4, REAR_FEEDER = 7, COUNT = 8;

    /**
     * A part that spins about a fixed axle while its mechanism runs: the intake roller (which also rises with the
     * carriage), the launcher's flywheels and the two feeder wheels. The logs are 50 Hz, so a real roller or flywheel
     * speed would alias; each turns at a slow display rate instead ({@link #DISPLAY_REV_PER_S}), only to show that it
     * is running.
     */
    static final class Spinner {
        final int component;
        final double[] pointM, axis;

        /** {@code axis} is the direction a positive (running) angle turns it about, through {@code pointM}. */
        Spinner(int component, double[] pointM, double[] axis) {
            double n = Math.sqrt(axis[0] * axis[0] + axis[1] * axis[1] + axis[2] * axis[2]);
            this.component = component;
            this.pointM = pointM;
            this.axis = new double[] {axis[0] / n, axis[1] / n, axis[2] / n};
        }
    }

    /** How fast a running roller, flywheel or feeder is drawn turning: slow enough to read at 50 Hz (14 deg a frame). */
    static final double DISPLAY_REV_PER_S = 2;
    /*
     * The CAD chat's axes (12d34bb, cad/advantagescope/Robot_BIOBUZZ/extractor_poses.json), metres; a positive angle
     * about each axis as given is the way it turns when running.
     */
    /** The intake roller about its resting axle, +Y: its bottom moves rearward, pulling a piece in. */
    static final Spinner INTAKE_ROLLER_SPIN = new Spinner(INTAKE_ROLLER, new double[] {0.217424, 0, 0.084963}, new double[] {0, 1, 0});
    /** The launcher's two flywheel axles, left (+Y) and right (-Y), two 96 mm wheels each: both throw the piece up. */
    static final Spinner[] FLYWHEELS = {
            new Spinner(5, new double[] {-0.0762, 0.092525, 0.168808}, new double[] {-1, 0, 0}),
            new Spinner(6, new double[] {-0.0762, -0.084521, 0.168808}, new double[] {1, 0, 0})};
    /** The front (+Y axis) and rear (-Y axis) feeder wheels under the cup: both pop the piece up. */
    static final Spinner[] FEEDERS = {
            new Spinner(FRONT_FEEDER, new double[] {-0.013843, 0, 0.039624}, new double[] {0, 1, 0}),
            new Spinner(REAR_FEEDER, new double[] {-0.130683, 0, 0.039624}, new double[] {0, -1, 0})};

    /** Lane speed: about 0.4 of the lane's drive speed, as a hollow ball rolls on a moving floor. */
    static final double LANE_IN_PER_S = 27;
    /** Fire command to the piece leaving the transfer, and the feeders' spin-up within it. */
    static final double CLIMB_S = 0.15, FEED_SPIN_UP_S = 0.05;

    /** Where an entering piece is first drawn: its centre just ahead of the roller's axle, on the tiles. */
    static final double ENTRY_X = 10.0;
    /** The ramp: from X 8.0 (0.05 in up) to X 5.8, 0.9 in up, where the lane starts. */
    static final double RAMP_START_X = 8.0, RAMP_START_Z = 0.05, LANE_START_X = 5.8, LANE_FLOOR_Z = 0.9;
    /** The lane's end: its ball-bottom line rises 17 deg from the ramp's top to here, over the front feeder. */
    static final double LANE_END_X = -0.545, LANE_END_Z = 2.873;
    /** The feeder cup on the turret's axis, and a seated piece's centre height: POLLEN 3.00, NECTAR 3.67. */
    static final double CUP_X = -2.845, CUP_Z_POLLEN = 3.00, CUP_Z_NECTAR = 3.67;
    /** The roller: axle at rest (X, z), radius, the most it floats, and how far a POLLEN squeezes its gecko tread. */
    static final double ROLLER_X = 8.56, ROLLER_Z = 3.35, ROLLER_RADIUS = 0.95, ROLLER_FLOAT_MAX = 1.3, ROLLER_SQUEEZE = 0.4;

    /** The turret's axis: the bearing's inner race, 4 mm left of the centre line (the CAD chat, 12d34bb). */
    static final double TURRET_X = -0.072215 / 0.0254, TURRET_Y = 0.004 / 0.0254;
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
    void record(int index, boolean aiming, boolean launcherOn, long us) {
        Track t = tracks.get(index);
        Frame f = new Frame();
        f.us = us;
        f.intakeOn = t.body.intaking();
        f.launcherOn = launcherOn;
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
                if (!now.contains(p) && p.shotFrom != null && p.shotFrom != t.shotSeen.get(p)) t.launchedAt.computeIfAbsent(p, k -> new ArrayList<>()).add(us);
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
        boolean intakeOn, launcherOn;
    }

    private final class Track {
        final String prefix;
        final FieldSim.Bot body;
        final RobotDesign design;
        final boolean cad, transfer;
        final List<Frame> frames = new ArrayList<>();
        /** When each piece was launched: a piece shot, missed and taken in again can be launched more than once. */
        final Map<FieldSim.Piece, List<Long>> launchedAt = new IdentityHashMap<>();
        /** The launch each climbing piece is climbing toward. */
        final Map<FieldSim.Piece, Long> climbingTo = new IdentityHashMap<>();
        final Map<FieldSim.Piece, double[]> shotSeen = new IdentityHashMap<>();
        /** Where each held piece is drawn: inches along its path ({@link Path}). */
        final Map<FieldSim.Piece, Double> along = new IdentityHashMap<>();
        /** Where a firing piece was when its climb started. */
        final Map<FieldSim.Piece, Double> climbFrom = new IdentityHashMap<>();
        final Map<Double, Path> paths = new HashMap<>();
        double turretYaw, rollerSpin, flywheelSpin, feederSpin;
        long lastUs;
        double[] componentsLast;
        final double[] readoutsLast = new double[5];

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
                // Launched within CLIMB_S from now. A piece shot earlier and taken in again is in the queue again.
                Long next = null;
                for (long at : launchedAt.getOrDefault(p, java.util.Collections.emptyList())) {
                    if (at > f.us && at - f.us <= Math.round(CLIMB_S * 1e6)) next = at;
                }
                if (next != null) {
                    climbing.add(p);
                    climbingTo.put(p, next);
                } else {
                    queue.add(p);
                    climbingTo.remove(p);
                    climbFrom.remove(p);
                }
            }
            along.keySet().retainAll(Arrays.asList(f.stored));
            // The queue: the front piece seated in the cup (held back at the lane's end while another is being fed),
            // the rest nose to tail behind it along the lane.
            double behind = 0, lastR = 0;
            for (int q = 0; q < queue.size(); q++) {
                FieldSim.Piece p = queue.get(q);
                Path path = path(p.kind.radius);
                double front = climbing.isEmpty() ? path.cup : path.laneEnd;
                if (q > 0) behind += lastR + p.kind.radius;
                lastR = p.kind.radius;
                double target = Math.max(0, front - behind);
                Double s = along.get(p);
                // Preloads (and anything already held when the log starts) start in their place; a piece taken
                // in starts under the roller. Without the transfer, every piece is simply in its place.
                if (!transfer || (s == null && i == 0)) s = target;
                else if (s == null) s = 0.0;
                s = s < target ? Math.min(target, s + LANE_IN_PER_S * dt) : target;
                along.put(p, s);
            }
            for (FieldSim.Piece p : climbing) {
                double toGo = (climbingTo.get(p) - f.us) / 1e6;
                Path path = path(p.kind.radius);
                double from = climbFrom.computeIfAbsent(p, k -> along.getOrDefault(k, path.cup));
                double moving = CLIMB_S - FEED_SPIN_UP_S;
                double u = Math.max(0, Math.min(1, (moving - toGo) / moving));
                along.put(p, from + u * (path.length - from));
            }
            // Each piece's place, and the roller's float.
            double rise = 0;
            for (FieldSim.Piece p : f.stored) {
                Path path = path(p.kind.radius);
                double[] xz = path.at(along.get(p));
                double r = p.kind.radius;
                // Roller: rises until it clears the piece, less the squeeze a POLLEN gets (so only a NECTAR lifts it).
                double reach = ROLLER_RADIUS + r - ROLLER_SQUEEZE, dx = xz[0] - ROLLER_X;
                if (Math.abs(dx) < reach) rise = Math.max(rise, xz[1] + Math.sqrt(reach * reach - dx * dx) - ROLLER_Z);
                held.get(p.kind.ordinal()).add(piecePose(f.pose, xz[0], xz[1], along.get(p), r));
            }
            rise = Math.min(ROLLER_FLOAT_MAX, rise);
            if (!cad) return;
            double error = 0;
            if (f.aim != null) {
                // Toward the aim point while the launcher is spun up or firing; otherwise it holds its last angle.
                double c = Math.cos(f.pose[2]), s = Math.sin(f.pose[2]);
                double ax = f.pose[0] + TURRET_X * c - TURRET_Y * s, ay = f.pose[1] + TURRET_X * s + TURRET_Y * c;
                double want = AdvantageScopeFrame.wrap(Math.atan2(f.aim[1] - ay, f.aim[0] - ax) - f.pose[2]);
                double turn = AdvantageScopeFrame.wrap(want - turretYaw), most = Math.toRadians(PLACEHOLDER_TURRET_DEG_PER_S) * dt;
                turretYaw = AdvantageScopeFrame.wrap(turretYaw + Math.max(-most, Math.min(most, turn)));
                error = AdvantageScopeFrame.wrap(want - turretYaw);
            }
            double turn = 2 * Math.PI * DISPLAY_REV_PER_S * dt;
            if (f.intakeOn) rollerSpin = (rollerSpin + turn) % (2 * Math.PI);
            if (f.launcherOn) flywheelSpin = (flywheelSpin + turn) % (2 * Math.PI);
            if (!climbing.isEmpty()) feederSpin = (feederSpin + turn) % (2 * Math.PI);
            double[] components = components(f.extractorDown, rise, turretYaw, rollerSpin, flywheelSpin, feederSpin);
            for (int k = 0; k < components.length; k++) components[k] = Math.round(components[k] * 1e5) / 1e5;
            if (!Arrays.equals(components, componentsLast)) {
                log.putPose3dArray(prefix + COMPONENTS, components, f.us);
                componentsLast = components;
            }
            double[] readouts = {
                    Math.round(AutoSim.EXTRACTOR_STOWED_DEG * (1 - f.extractorDown) * 10) / 10.0,
                    Math.round(rise * 100) / 100.0,
                    Math.round(Math.toDegrees(turretYaw) * 10) / 10.0,
                    climbing.size(),
                    Math.round(Math.toDegrees(error) * 10) / 10.0};
            String[] names = {"ExtractorDeg", "RollerRiseIn", "TurretYawDeg", "Climbing", "TurretErrorDeg"};
            for (int k = 0; k < readouts.length; k++) {
                if (readouts[k] != readoutsLast[k]) {
                    log.put(prefix + "/Internals/" + names[k], readouts[k], f.us);
                    readoutsLast[k] = readouts[k];
                }
            }
        }
    }

    /** As {@link #components(double, double, double, double, double, double)}, with nothing spinning. */
    static double[] components(double extractorDown, double riseIn, double yaw) {
        return components(extractorDown, riseIn, yaw, 0, 0, 0);
    }

    /**
     * The component poses, translation (m) then quaternion (w, x, y, z):
     * <ul>
     *   <li>the extractor, as {@link AutoSim#cadComponents};</li>
     *   <li>the roller's carriage, raised {@code riseIn};</li>
     *   <li>the turret ring, turned {@code yaw} about its axis;</li>
     *   <li>the intake roller, turned {@code rollerSpin} about its axle and raised with the carriage;</li>
     *   <li>each flywheel, turned {@code flywheelSpin}, and each feeder, turned {@code feederSpin}, about its axle.</li>
     * </ul>
     */
    static double[] components(double extractorDown, double riseIn, double yaw, double rollerSpin, double flywheelSpin, double feederSpin) {
        double[] out = new double[7 * COUNT];
        System.arraycopy(AutoSim.cadComponents(extractorDown, 0), 0, out, 7 * EXTRACTOR, 7);
        out[7 * ROLLER + 2] = riseIn * M;
        out[7 * ROLLER + 3] = 1;
        about(out, TURRET, new double[] {TURRET_X * M, TURRET_Y * M, 0}, new double[] {0, 0, 1}, yaw);
        about(out, INTAKE_ROLLER_SPIN, rollerSpin);
        out[7 * INTAKE_ROLLER + 2] += riseIn * M;
        for (Spinner w : FLYWHEELS) about(out, w, flywheelSpin);
        for (Spinner w : FEEDERS) about(out, w, feederSpin);
        return out;
    }

    private static void about(double[] out, Spinner w, double angle) {
        about(out, w.component, w.pointM, w.axis, angle);
    }

    /** Component {@code c}: turned {@code angle} about the unit {@code axis} through {@code p} (translation = p - R p). */
    private static void about(double[] out, int c, double[] p, double[] axis, double angle) {
        double cs = Math.cos(angle), sn = Math.sin(angle), dot = axis[0] * p[0] + axis[1] * p[1] + axis[2] * p[2];
        // Rodrigues: R p = p cos + (axis x p) sin + axis (axis . p)(1 - cos).
        double[] cross = {axis[1] * p[2] - axis[2] * p[1], axis[2] * p[0] - axis[0] * p[2], axis[0] * p[1] - axis[1] * p[0]};
        int k = 7 * c;
        for (int i = 0; i < 3; i++) out[k + i] = p[i] - (p[i] * cs + cross[i] * sn + axis[i] * dot * (1 - cs));
        out[k + 3] = Math.cos(angle / 2);
        for (int i = 0; i < 3; i++) out[k + 4 + i] = axis[i] * Math.sin(angle / 2);
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
     * A piece's path through the transfer, for one piece radius, in the robot frame (X, z), inches: under the roller,
     * up the ramp, up the rising lane to its end over the front feeder, down into the cup, and straight up the turret's
     * axis through the flywheels to the launcher's exit height.
     */
    static final class Path {
        final double[] xs, zs, cum;
        final double length;
        /** How far along a piece is at the lane's end, and seated in the cup. */
        final double laneEnd, cup;

        Path(double r, double exitX, double exitZ) {
            List<double[]> pts = new ArrayList<>();
            pts.add(new double[] {ENTRY_X, r});
            pts.add(new double[] {ROLLER_X, r});
            pts.add(new double[] {RAMP_START_X, RAMP_START_Z + r});
            pts.add(new double[] {LANE_START_X, LANE_FLOOR_Z + r});
            pts.add(new double[] {LANE_END_X, LANE_END_Z + r});
            int laneEndAt = pts.size() - 1;
            // Seated in the cup: POLLEN's and NECTAR's measured heights, between them by size.
            double u = (r - FieldSim.POLLEN_RADIUS_IN) / (FieldSim.NECTAR_RADIUS_IN - FieldSim.POLLEN_RADIUS_IN);
            pts.add(new double[] {CUP_X, CUP_Z_POLLEN + u * (CUP_Z_NECTAR - CUP_Z_POLLEN)});
            int cupAt = pts.size() - 1;
            // Straight up the turret's axis, through the flywheels' nip and the turret's bore, to the exit's height. The
            // simulator's shot leaves from the design's exit point (exitX), which may sit a little off the axis.
            pts.add(new double[] {CUP_X, exitZ});
            xs = new double[pts.size()];
            zs = new double[pts.size()];
            cum = new double[pts.size()];
            for (int k = 0; k < pts.size(); k++) {
                xs[k] = pts.get(k)[0];
                zs[k] = pts.get(k)[1];
                if (k > 0) cum[k] = cum[k - 1] + Math.hypot(xs[k] - xs[k - 1], zs[k] - zs[k - 1]);
            }
            length = cum[cum.length - 1];
            laneEnd = cum[laneEndAt];
            cup = cum[cupAt];
        }

        /** Where a piece sits seated in the cup. */
        double[] cupPoint() {
            return at(cup);
        }

        /** (X, z) at {@code s} inches along. */
        double[] at(double s) {
            s = Math.max(0, Math.min(length, s));
            int k = 1;
            while (k < cum.length - 1 && cum[k] < s) k++;
            double seg = cum[k] - cum[k - 1], u = seg == 0 ? 0 : (s - cum[k - 1]) / seg;
            return new double[] {xs[k - 1] + u * (xs[k] - xs[k - 1]), zs[k - 1] + u * (zs[k] - zs[k - 1])};
        }

    }
}
