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
 * All pieces travel on the centre line. The transfer is the CAD chat's 2f78001, on the mentor's CAD: a ramp, a flat
 * lane, and a feeder with a sprung pad opposite it that hold the lead piece against a backstop and drive it straight up
 * into the launcher's flywheels. The simulator tracks only which pieces are held, and in what order. So where a piece
 * is between those events is interpolated:
 * <ul>
 *   <li><b>Entry:</b> from under the roller, up the ramp and along the lane to its place in the queue, at
 *   {@link #LANE_IN_PER_S}.</li>
 *   <li><b>Queue:</b> the lead piece held between the feeders, the rest nose to tail behind it on the flat lane. Each
 *   moves up at {@link #LANE_IN_PER_S} when the one ahead leaves; while a piece is being fed, the next waits right
 *   behind the hold and rolls in once it has gone.</li>
 *   <li><b>Firing:</b> the held piece waits {@link #FEED_SPIN_UP_S} while the feeders spin up. It then rises straight
 *   up the turret's axis through the flywheels to the launcher's exit height, arriving as the simulator launches it,
 *   {@link #CLIMB_S} after the fire command.</li>
 * </ul>
 */
final class RobotInternalsLog {

    /** The CAD model's component poses, in this order (agreed with the CAD chat): the key's suffix after a robot's prefix. */
    static final String COMPONENTS = "/Internals/Components";
    static final int EXTRACTOR = 0, ROLLER = 1, TURRET = 2, FEEDER = 3, INTAKE_ROLLER = 4, PAD = 7, COUNT = 8;

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
    static final Spinner INTAKE_ROLLER_SPIN = new Spinner(INTAKE_ROLLER, new double[] {0.218973, 0, 0.08636}, new double[] {0, 1, 0});
    /** The launcher's two flywheel axles, left (+Y) and right (-Y), two 96 mm wheels each: both throw the piece up. */
    static final Spinner[] FLYWHEELS = {
            new Spinner(5, new double[] {-0.05588, 0.076525, 0.168808}, new double[] {-1, 0, 0}),
            new Spinner(6, new double[] {-0.05588, -0.068521, 0.168808}, new double[] {1, 0, 0})};
    /** The feeder: two 72 mm wheels on a shaft along X, left of the held piece; it drives the piece up. */
    static final Spinner FEEDER_SPIN = new Spinner(FEEDER, new double[] {-0.051943, 0.07283, 0.081619}, new double[] {-1, 0, 0});
    /**
     * The sprung foam pad opposite the feeder, hinged along X at its foot: it doesn't spin. A positive angle swings its
     * top out, 0 at rest (a POLLEN) and {@link #PAD_NECTAR_DEG} while a NECTAR is in the feeder.
     */
    static final double[] PAD_HINGE_M = {-0.051943, -0.04826, 0.041148}, PAD_AXIS = {1, 0, 0};
    static final double PAD_NECTAR_DEG = 29;

    /** Lane speed: about 0.4 of the lane's drive speed, as a hollow ball rolls on a moving floor. */
    static final double LANE_IN_PER_S = 27;
    /** Fire command to the piece leaving the transfer, and the feeders' spin-up within it. */
    static final double CLIMB_S = 0.15, FEED_SPIN_UP_S = 0.05;

    /** Where an entering piece is first drawn: its centre just ahead of the roller's axle, on the tiles. */
    static final double ENTRY_X = 10.0;
    /** The ramp: from X 8.0 (0.05 in up) to X 5.7, 1.3 in up, where the flat lane starts (ball-bottom at 1.3). */
    static final double RAMP_START_X = 8.0, RAMP_START_Z = 0.05, LANE_START_X = 5.7, LANE_FLOOR_Z = 1.3;
    /**
     * Where the lead piece is held, between the feeders against the backstop at X -3.87: a POLLEN's centre at -2.455,
     * a NECTAR's on the turret's axis at -2.045. Pressed between the feeder and the pad, a POLLEN sits 0.15 in left of the
     * centre line and a NECTAR 0.21 in right (the CAD chat, 2f78001). It is fed straight up the column at
     * {@link #COLUMN_X}, from that side offset.
     */
    static final double HOLD_X_POLLEN = -2.455, HOLD_X_NECTAR = -2.045, COLUMN_X = -2.045;
    static final double HOLD_Y_POLLEN = 0.15, HOLD_Y_NECTAR = -0.21;
    /** How far before the hold a piece starts moving over to its side offset. */
    static final double HOLD_Y_BLEND_IN = 1.0;
    /** The roller: axle at rest (X, z), radius, the most it floats, and how far a POLLEN squeezes its gecko tread. */
    static final double ROLLER_X = 8.621, ROLLER_Z = 3.40, ROLLER_RADIUS = 1.0, ROLLER_FLOAT_MAX = 1.3, ROLLER_SQUEEZE = 0.4;

    /** The turret's axis: the bearing's inner race, 4 mm left of the centre line (the CAD chat, ac817a6). */
    static final double TURRET_X = -0.051895 / 0.0254, TURRET_Y = 0.004 / 0.0254;

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
    void record(int index, boolean aiming, boolean launcherOn, double turretYawRad, long us) {
        Track t = tracks.get(index);
        Frame f = new Frame();
        f.us = us;
        f.turretYaw = turretYawRad;
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
        /** The simulator's turret yaw (AutoSim.Bot#turretYaw), rad left of forward. */
        double turretYaw;
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
            // The queue: the lead piece held between the feeders, the rest nose to tail behind it, each the two radii
            // further forward (the CAD chat's slots). While a piece is being fed, the next waits right behind it.
            double x = Double.NaN, lastR = 0;
            if (!climbing.isEmpty()) {
                FieldSim.Piece c = climbing.get(0);
                x = holdX(c.kind.radius);
                lastR = c.kind.radius;
            }
            for (FieldSim.Piece p : queue) {
                Path path = path(p.kind.radius);
                x = Double.isNaN(x) ? holdX(p.kind.radius) : x + lastR + p.kind.radius;
                lastR = p.kind.radius;
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
                double toGo = (climbingTo.get(p) - f.us) / 1e6;
                Path path = path(p.kind.radius);
                double from = climbFrom.computeIfAbsent(p, k -> along.getOrDefault(k, path.hold));
                double moving = CLIMB_S - FEED_SPIN_UP_S;
                double u = Math.max(0, Math.min(1, (moving - toGo) / moving));
                along.put(p, from + u * (path.length - from));
            }
            // Each piece's place, the roller's float, and the pad (swung out while a NECTAR is in the feeder).
            double rise = 0, pad = 0;
            for (FieldSim.Piece p : f.stored) {
                Path path = path(p.kind.radius);
                double sAlong = along.get(p);
                double[] xz = path.at(sAlong);
                double r = p.kind.radius;
                if (p.kind != FieldSim.Kind.POLLEN && sAlong > path.hold - 0.3 && xz[1] < path.holdPoint()[1] + FEEDER_REACH_IN) {
                    pad = Math.toRadians(PAD_NECTAR_DEG);
                }
                double y = holdY(r) * Math.max(0, Math.min(1, 1 - (path.hold - sAlong) / HOLD_Y_BLEND_IN));
                // Roller: rises until it clears the piece, less the squeeze a POLLEN gets (so only a NECTAR lifts it).
                double reach = ROLLER_RADIUS + r - ROLLER_SQUEEZE, dx = xz[0] - ROLLER_X;
                if (Math.abs(dx) < reach) rise = Math.max(rise, xz[1] + Math.sqrt(reach * reach - dx * dx) - ROLLER_Z);
                held.get(p.kind.ordinal()).add(piecePose(f.pose, xz[0], y, xz[1], sAlong, r));
            }
            rise = Math.min(ROLLER_FLOAT_MAX, rise);
            if (!cad) return;
            // The turret is the simulator's (AutoSim slews it at RobotDesign#turretSlewRadPerS and pre-aims it by the
            // mentor's side-of-field rule, 9 Oct 2026); TurretErrorDeg is how far it is off the raised CELL's aim.
            turretYaw = f.turretYaw;
            double error = 0;
            if (f.aim != null) {
                double c = Math.cos(f.pose[2]), s = Math.sin(f.pose[2]);
                double ax = f.pose[0] + TURRET_X * c - TURRET_Y * s, ay = f.pose[1] + TURRET_X * s + TURRET_Y * c;
                double want = AdvantageScopeFrame.wrap(Math.atan2(f.aim[1] - ay, f.aim[0] - ax) - f.pose[2]);
                error = AdvantageScopeFrame.wrap(want - turretYaw);
            }
            double turn = 2 * Math.PI * DISPLAY_REV_PER_S * dt;
            if (f.intakeOn) rollerSpin = (rollerSpin + turn) % (2 * Math.PI);
            if (f.launcherOn) flywheelSpin = (flywheelSpin + turn) % (2 * Math.PI);
            if (!climbing.isEmpty()) feederSpin = (feederSpin + turn) % (2 * Math.PI);
            double[] components = components(f.extractorDown, rise, turretYaw, rollerSpin, flywheelSpin, feederSpin, pad);
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
     *   <li>each flywheel, turned {@code flywheelSpin}, and the feeder, turned {@code feederSpin}, about its axle;</li>
     *   <li>the pad, swung out about its hinge (0 unless given).</li>
     * </ul>
     */
    static double[] components(double extractorDown, double riseIn, double yaw, double rollerSpin, double flywheelSpin, double feederSpin) {
        return components(extractorDown, riseIn, yaw, rollerSpin, flywheelSpin, feederSpin, 0);
    }

    /** As above, with the pad swung out {@code pad} (rad) about its hinge. */
    static double[] components(double extractorDown, double riseIn, double yaw, double rollerSpin, double flywheelSpin, double feederSpin,
                               double pad) {
        double[] out = new double[7 * COUNT];
        System.arraycopy(AutoSim.cadComponents(extractorDown, 0), 0, out, 7 * EXTRACTOR, 7);
        out[7 * ROLLER + 2] = riseIn * M;
        out[7 * ROLLER + 3] = 1;
        about(out, TURRET, new double[] {TURRET_X * M, TURRET_Y * M, 0}, new double[] {0, 0, 1}, yaw);
        about(out, INTAKE_ROLLER_SPIN, rollerSpin);
        out[7 * INTAKE_ROLLER + 2] += riseIn * M;
        for (Spinner w : FLYWHEELS) about(out, w, flywheelSpin);
        about(out, FEEDER_SPIN, feederSpin);
        about(out, PAD, PAD_HINGE_M, PAD_AXIS, pad);
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
     * A held piece at {@code x} ahead of the robot's centre, {@code y} to its left and {@code z} up, as an
     * AdvantageScope Pose3d (Center/Rotated, metres). It rolls as it goes: turned {@code along / r} about the
     * robot's left axis.
     */
    static double[] piecePose(double[] pose, double x, double y, double z, double along, double r) {
        double c = Math.cos(pose[2]), s = Math.sin(pose[2]);
        double fx = pose[0] + x * c - y * s, fy = pose[1] + x * s + y * c;
        double half = -along / r / 2;
        // About the robot's left axis (-sin h, cos h, 0) in Pedro; Pedro -> Center/Rotated turns an axis (x, y) to (-y, x).
        double qw = Math.cos(half), qxP = -s * Math.sin(half), qyP = c * Math.sin(half);
        return new double[] {
                mm(AdvantageScopeFrame.xMeters(fx, fy)), mm(AdvantageScopeFrame.yMeters(fx, fy)), mm(z * M),
                Math.round(qw * 1e3) / 1e3, Math.round(-qyP * 1e3) / 1e3, Math.round(qxP * 1e3) / 1e3, 0};
    }

    /** How far to the left a piece of radius {@code r} sits when held, between POLLEN's and NECTAR's by size. */
    static double holdY(double r) {
        double u = (r - FieldSim.POLLEN_RADIUS_IN) / (FieldSim.NECTAR_RADIUS_IN - FieldSim.POLLEN_RADIUS_IN);
        return HOLD_Y_POLLEN + u * (HOLD_Y_NECTAR - HOLD_Y_POLLEN);
    }

    /** How far up from its hold a fed piece is still in the feeder (the wheel's 72 mm, roughly), for the pad. */
    static final double FEEDER_REACH_IN = 2.0;

    /** Where a piece of radius {@code r} is held: POLLEN's and NECTAR's measured places, between them by size. */
    static double holdX(double r) {
        double u = (r - FieldSim.POLLEN_RADIUS_IN) / (FieldSim.NECTAR_RADIUS_IN - FieldSim.POLLEN_RADIUS_IN);
        return HOLD_X_POLLEN + u * (HOLD_X_NECTAR - HOLD_X_POLLEN);
    }

    private static double mm(double meters) {
        return Math.round(meters * 1e3) / 1e3;
    }

    /**
     * A piece's path through the transfer, for one piece radius, in the robot frame (X, z), inches: under the roller,
     * up the ramp, along the flat lane to the hold between the feeders, and straight up the turret's axis through the
     * flywheels to the launcher's exit height.
     */
    static final class Path {
        final double[] xs, zs, cum;
        final double length;
        /** How far along a piece is when held between the feeders. */
        final double hold;

        Path(double r, double exitX, double exitZ) {
            List<double[]> pts = new ArrayList<>();
            pts.add(new double[] {ENTRY_X, r});
            pts.add(new double[] {ROLLER_X, r});
            pts.add(new double[] {RAMP_START_X, RAMP_START_Z + r});
            pts.add(new double[] {LANE_START_X, LANE_FLOOR_Z + r});
            pts.add(new double[] {holdX(r), LANE_FLOOR_Z + r});
            int holdAt = pts.size() - 1;
            // Up the column on the turret's axis, through the flywheels' nip and the turret's bore, to the exit's height.
            // The simulator's shot leaves from the design's exit point (exitX), which may sit a little off the axis.
            pts.add(new double[] {COLUMN_X, exitZ});
            xs = new double[pts.size()];
            zs = new double[pts.size()];
            cum = new double[pts.size()];
            for (int k = 0; k < pts.size(); k++) {
                xs[k] = pts.get(k)[0];
                zs[k] = pts.get(k)[1];
                if (k > 0) cum[k] = cum[k - 1] + Math.hypot(xs[k] - xs[k - 1], zs[k] - zs[k - 1]);
            }
            length = cum[cum.length - 1];
            hold = cum[holdAt];
        }

        /** How far along the path, on the way in (entry to the hold), a centre at {@code x} is. */
        double alongAtX(double x) {
            for (int k = 1; k < xs.length && cum[k - 1] < hold; k++) {
                if (xs[k] <= x && x <= xs[k - 1] && xs[k - 1] != xs[k]) {
                    return cum[k - 1] + (xs[k - 1] - x) / (xs[k - 1] - xs[k]) * (cum[k] - cum[k - 1]);
                }
            }
            return x > xs[0] ? 0 : hold;
        }

        /** Where a piece sits held between the feeders. */
        double[] holdPoint() {
            return at(hold);
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
