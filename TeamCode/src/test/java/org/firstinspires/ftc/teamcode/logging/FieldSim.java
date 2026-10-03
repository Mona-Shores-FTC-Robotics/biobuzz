package org.firstinspires.ftc.teamcode.logging;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.HiveGeometry;
import org.firstinspires.ftc.teamcode.vision.HiveState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * POLLEN, NECTAR and the two HIVE rockers, simulated so a {@code .wpilog} shows them move the way
 * they would: launched in arcs, bouncing off the tiles, walls, robot and each other, rolling to the
 * back of a raised CELL, tipping the HIVE, and spilling out of the CELL that goes down.
 *
 * <p><b>Where it comes from.</b> The approach is FuelSim's (Team 5000 Hammerheads, MIT licence), as
 * Wavelength 3572 used it in FRC 2026 ({@code wavelength3572/Robot-2026}, {@code util/FuelSim}):
 * point-mass spheres, gravity, restitution and friction, fixed sub-steps, the robot as a moving box
 * and the intake as a capture zone. What is BIOBUZZ's own is the HIVE: each rocker is two open-ended
 * boxes on an axle, built from {@link HiveGeometry} (the Competition Manual) and checked against
 * AdvantageScope's field CAD, and it tips because the pieces in it push it over.
 *
 * <p><b>Frame.</b> Pedro inches, seconds, z up from the tiles. Converted to AdvantageScope's frame
 * only when poses are packed for the log ({@link #pieces}, {@link #hiveComponents}).
 *
 * <p><b>Calibrated, measured and placeholder.</b> Sizes and the HIVE come from the manual and the CAD.
 * What makes a HIVE tip, how long a tip takes, how heavy NECTAR is and how high a piece bounces come
 * from {@link HiveCalibration}: things a team measures on a field, which the simulation is fitted
 * to (see {@link HiveCalibration#fit}). The rest (friction, the robot) are the
 * {@code PLACEHOLDER_} constants below. Nothing here runs on a robot.
 */
final class FieldSim {

    // ---- Measured -----------------------------------------------------------------------------

    static final double GRAVITY_IN_PER_S2 = 386.09;
    static final double FIELD_SIZE_IN = 2 * AdvantageScopeFrame.PEDRO_FIELD_CENTER_IN;
    static final double CENTRE_IN = AdvantageScopeFrame.PEDRO_FIELD_CENTER_IN;

    /** Half the width of AdvantageScope's POLLEN model ({@code model_0.glb}, ±0.0356 m). */
    static final double POLLEN_RADIUS_IN = 1.40;
    /** Half the width of AdvantageScope's NECTAR models ({@code model_1/2.glb}, ±0.046 m). */
    static final double NECTAR_RADIUS_IN = 1.80;

    /** A settled CELL's tilt, and so the rocker's end stops. */
    static final double TILT_RAD = Math.toRadians(HiveGeometry.CELL_TILT_DEG);
    static final double PIVOT_Z_IN = HiveGeometry.PIVOT_AXIS_HEIGHT_IN;
    /** Along the rocker from the axle: a CELL's closed back and its open end. */
    static final double CELL_BACK_IN = HiveGeometry.CELL_SPACING_IN / 2;
    static final double CELL_OPENING_IN = CELL_BACK_IN + HiveGeometry.CELL_DEPTH_IN;
    static final double CELL_HALF_WIDTH_IN = HiveGeometry.OPENING_WIDTH_IN / 2;
    /**
     * A CELL's floor, relative to the axle with the rocker level. Derived from the manual: the
     * raised opening's bottom edge is {@link HiveGeometry#OPENING_BOTTOM_HEIGHT_IN} up. The CAD has
     * it at −1.45 in; this gives −1.34.
     */
    static final double CELL_FLOOR_IN = (HiveGeometry.OPENING_BOTTOM_HEIGHT_IN - PIVOT_Z_IN
            - CELL_OPENING_IN * Math.sin(TILT_RAD)) / Math.cos(TILT_RAD);
    static final double CELL_ROOF_IN = CELL_FLOOR_IN + HiveGeometry.OPENING_HEIGHT_IN;
    /** The red HIVE is on the low-x side (CAD); each is centred half the centre spacing out. */
    static final double RED_HIVE_X_IN = CENTRE_IN - HiveGeometry.HIVE_CENTER_TO_CENTER_IN / 2;
    static final double BLUE_HIVE_X_IN = CENTRE_IN + HiveGeometry.HIVE_CENTER_TO_CENTER_IN / 2;

    /** The HIVE frame's two foot bars, which run along y at its sides (CAD: 2.2 in tall). */
    static final double FOOT_BAR_HALF_SPAN_X_IN = HiveGeometry.FRAME_WIDTH_IN / 2;
    static final double FOOT_BAR_HALF_LENGTH_IN = HiveGeometry.FRAME_DEPTH_IN / 2;
    static final double FOOT_BAR_HALF_WIDTH_IN = 1.0;
    static final double FOOT_BAR_HEIGHT_IN = 2.2;

    /** An FTC robot's starting-size limit; the simulated robot is that box. */
    static final double ROBOT_SIZE_IN = 18.0;
    /** The standard design's intake ({@link RobotDesign#intakeWidthIn}), for planners that aim at it. */
    static final double INTAKE_HALF_WIDTH_IN = 7.0;
    /** Competition Manual G407: a robot may not control more than 4 SCORING ELEMENTS. */
    static final int ROBOT_CAPACITY = 4;
    /** Competition Manual §10.3.4: every robot starts the match holding exactly 4 POLLEN. */
    static final int PRELOAD_POLLEN = 4;
    /** How far a rocker must swing off its stop before its TIP counts as started. */
    static final double TIP_STARTED_RAD = Math.toRadians(5);
    /**
     * The dampers at each end of the rocker (mentor review: the TIP looked too fast at the end): in
     * the last few degrees before a stop the swing slows to this fraction. The calibration still
     * makes a whole TIP take its measured time, so the middle of the swing is quicker to match.
     * Placeholders until the TIP is filmed (issue #147).
     */
    static final double PLACEHOLDER_DAMPER_ZONE_RAD = Math.toRadians(6);
    static final double PLACEHOLDER_DAMPER_FACTOR = 0.3;

    // ---- Placeholders: not published, replace with measurements -------------------------------

    static final double PLACEHOLDER_WALL_RESTITUTION = 0.45;
    static final double PLACEHOLDER_PIECE_RESTITUTION = 0.5;
    static final double PLACEHOLDER_HIVE_RESTITUTION = 0.2;
    static final double PLACEHOLDER_ROBOT_RESTITUTION = 0.1;
    /** Fraction of sliding speed lost per second in contact with a surface. */
    static final double PLACEHOLDER_CONTACT_FRICTION = 2.5;
    /** Rolling resistance on foam tiles: a steady slowing, so a rolling piece stops, in/s². */
    static final double PLACEHOLDER_ROLLING_DECEL_IN_PER_S2 = 12.0;
    /** Height of the simulated robot's body; pieces hit it below this. */
    static final double PLACEHOLDER_ROBOT_HEIGHT_IN = 14.0;
    /** Where a launched piece leaves the robot: forward of centre, and up. */
    static final double PLACEHOLDER_EXIT_FORWARD_IN = 4.0;
    /** Above the robot body ({@link #PLACEHOLDER_ROBOT_HEIGHT_IN}), so a launch clears its own robot. */
    static final double PLACEHOLDER_EXIT_HEIGHT_IN = 17.0;
    /** Shot-to-shot spread: speed as a fraction, and angle in radians, one sigma. */
    static final double PLACEHOLDER_SPEED_SPREAD = 0.015;
    static final double PLACEHOLDER_ANGLE_SPREAD_RAD = Math.toRadians(0.8);

    static final int SUBSTEPS = 20;

    /**
     * For sensitivity checks only: scale the placeholder friction and shot spread, to see whether
     * a conclusion survives the guesses being wrong. 1 is the placeholder itself.
     */
    static double frictionScale = 1;
    /** Scales every bounce (tiles, walls, robots, the HIVE, other pieces), for testing what the guesses change. */
    static double bounceScale = 1;
    /** Scales how fast a loaded rocker swings over, for the same reason. */
    static double swingScale = 1;

    static double bounce(double e) {
        return Math.min(0.95, e * bounceScale);
    }
    static double spreadScale = 1;
    /**
     * How untidy spills are (mentor review: pieces ended up lined against the wall). 1 = the
     * placeholders below, 0 = none. Each piece rolls with its own resistance, the tiles are slightly
     * uneven, and a piece leaving a CELL gets a small random kick and spin. Drawn from its own
     * random stream, so the shots of a seed are unchanged.
     */
    static double spillVariety = 1;
    static final double PLACEHOLDER_ROLL_SPREAD = 0.35;
    static final double PLACEHOLDER_TILE_SLOPE_IN_PER_S2 = 3.0;
    static final double PLACEHOLDER_SPILL_KICK_IN_PER_S = 4.0;
    /**
     * A spilled piece's speed as it leaves the lowered CELL, as a fraction of what it gathered rolling
     * down the CELL's floor (only out of a CELL that is tipping or down: a shot rebounding out of the
     * raised CELL keeps its speed). Fitted to the 3 Oct 2026 films (IMG_1957–1960, 120 fps): real
     * pieces pour off the lip and drop nearly straight down, first touching the tiles close under it,
     * about 4 ft out from the alliance wall (from a photo of the box that caught them, ±6 in), about
     * 1.15 s after the rocker starts to move. Rolling freely they flew another foot toward the wall.
     * At 0.25 the simulated first touch is 44–48 in out, 1.15 s after the TIP starts.
     * {@link SpillLandingTest} checks the fit.
     */
    static final double FILMED_SPILL_EXIT_SCALE = 0.25;
    static double spillExitScale = FILMED_SPILL_EXIT_SCALE;
    /** Robots' restitution on its own, apart from bounceScale (mentor review). */
    static double robotRestitution = PLACEHOLDER_ROBOT_RESTITUTION;
    // ---- Air: off unless a run asks for it (AutoSim's launcher aims as if there were none) ---------

    /** AndyMark's masses: POLLEN 0.055 lb, NECTAR 0.091 lb. */
    static final double POLLEN_MASS_KG = 0.0249;
    static final double NECTAR_MASS_KG = 0.0413;
    static final double AIR_DENSITY_KG_PER_M3 = 1.2;
    /**
     * Drag and backspin lift of a 26-hole (indoor) pickleball, which POLLEN and NECTAR are built like:
     * free-flight measurements found C_D ≈ 0.45 and, with backspin, C_L ≈ 0.2 (Tennis Warehouse
     * University, "Pickleball Aerodynamics"). Lift grows to that by a spin number r·ω/v of
     * {@link #LIFT_FULL_SPIN}; topspin pushes down the same way. Not measured on BIOBUZZ pieces.
     */
    static final double PLACEHOLDER_DRAG_COEFFICIENT = 0.45;
    static final double PLACEHOLDER_LIFT_COEFFICIENT = 0.20;
    static final double LIFT_FULL_SPIN = 0.25;

    /** Drag and spin lift on pieces in flight. */
    boolean air;

    static double massKg(Kind kind) {
        return kind == Kind.POLLEN ? POLLEN_MASS_KG : NECTAR_MASS_KG;
    }

    /** Applies drag and spin lift for {@code h} seconds. */
    private static void applyAir(Piece p, double h) {
        double speedIn = Math.sqrt(p.vx * p.vx + p.vy * p.vy + p.vz * p.vz);
        if (speedIn < 1e-6) return;
        double radiusM = p.kind.radius * AdvantageScopeFrame.METERS_PER_INCH;
        double area = Math.PI * radiusM * radiusM;
        // a = (½ ρ C A / m) v², in m/s² for v in m/s; in inches that is k · 0.0254 · v_in².
        double k = 0.5 * AIR_DENSITY_KG_PER_M3 * area / massKg(p.kind) * AdvantageScopeFrame.METERS_PER_INCH;
        double drag = k * PLACEHOLDER_DRAG_COEFFICIENT * speedIn * h;
        p.vx -= drag * p.vx;
        p.vy -= drag * p.vy;
        p.vz -= drag * p.vz;
        // Lift along ω × v.
        double lx = p.wy * p.vz - p.wz * p.vy, ly = p.wz * p.vx - p.wx * p.vz, lz = p.wx * p.vy - p.wy * p.vx;
        double ln = Math.sqrt(lx * lx + ly * ly + lz * lz);
        if (ln < 1e-9) return;
        double w = Math.sqrt(p.wx * p.wx + p.wy * p.wy + p.wz * p.wz);
        double spinNumber = w * p.kind.radius / speedIn;
        double cl = PLACEHOLDER_LIFT_COEFFICIENT * Math.min(1, spinNumber / LIFT_FULL_SPIN);
        double lift = k * cl * speedIn * speedIn * h / ln;
        p.vx += lift * lx;
        p.vy += lift * ly;
        p.vz += lift * lz;
    }

    /** Contacts slower than this do not bounce. */
    static final double RESTING_IN_PER_S = 6.0;

    // ---- State --------------------------------------------------------------------------------

    enum Kind {
        POLLEN(POLLEN_RADIUS_IN),
        RED_NECTAR(NECTAR_RADIUS_IN),
        BLUE_NECTAR(NECTAR_RADIUS_IN);

        final double radius;

        Kind(double radius) {
            this.radius = radius;
        }

        static Kind of(String name) {
            switch (name) {
                case "Pollen": return POLLEN;
                case "Red Nectar": return RED_NECTAR;
                case "Blue Nectar": return BLUE_NECTAR;
                default: throw new IllegalArgumentException("unknown piece " + name);
            }
        }
    }

    enum Where { FIELD, OUTSIDE, ROBOT }

    static final class Piece {
        final Kind kind;
        Where where;
        double x, y, z, vx, vy, vz;
        /** Orientation (Pedro frame), so a rolling piece visibly rolls. */
        double qw = 1, qx, qy, qz;
        double wx, wy, wz;
        /** Index into {@link #flowers} while it is still stacked in a Flower holder, else −1. */
        int flower = -1;
        /** The CELL it is in, or null. */
        HiveCell cell;
        /** This piece's rolling resistance relative to the placeholder (pieces differ; see spillVariety). */
        double rollScale = 1;
        /** An intake that just failed to grab it does not try again before this time. */
        double rejectedUntil = -1;

        Piece(Kind kind, Where where, double x, double y, double z) {
            this.kind = kind;
            this.where = where;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    /** One alliance's HIVE: a rocker on the shared axle, with a CELL at each end. */
    static final class Rocker {
        final Alliance alliance;
        final double centreX;
        /** About +x (Pedro); positive raises the high-y (SCORING) end. */
        double angle;
        double rate;
        int tips;
        /** Held on its stop whatever is in it: for fitting the calibration. */
        boolean locked;
        /** Seconds the last completed TIP took, from leaving one stop to reaching the other. */
        double lastTipSeconds = Double.NaN;
        private double tipFrom;
        private double leftStopAt;
        /**
         * TIPs started: counts up once a rocker has swung {@link #TIP_STARTED_RAD} off its stop,
         * like the robot's {@code HiveTracker.tipsStarted()}; a piece rolling in that lifts it a
         * little does not count.
         */
        int tipsStarted;
        private boolean startCounted;

        Rocker(Alliance alliance, double centreX) {
            this.alliance = alliance;
            this.centreX = centreX;
            angle = builtAngle();
            tipFrom = angle;
        }

        /** As the CAD has it, which is the match start: each alliance's RIGHT CELL up. */
        double builtAngle() {
            return angleFor(HiveState.RIGHT_CELL_UP);
        }

        /** The red RIGHT CELL is the AUDIENCE (low y) one, the blue one the SCORING one. */
        double angleFor(HiveState state) {
            boolean scoringUp = (alliance == Alliance.BLUE) == (state == HiveState.RIGHT_CELL_UP);
            return scoringUp ? TILT_RAD : -TILT_RAD;
        }

        HiveState state() {
            if (angle >= TILT_RAD - 1e-9) return alliance == Alliance.BLUE ? HiveState.RIGHT_CELL_UP : HiveState.LEFT_CELL_UP;
            if (angle <= -TILT_RAD + 1e-9) return alliance == Alliance.BLUE ? HiveState.LEFT_CELL_UP : HiveState.RIGHT_CELL_UP;
            return HiveState.TRANSITION;
        }

        /** The CELL at the high-y end ({@code end = +1}) or the low-y end ({@code −1}). */
        HiveCell cell(int end) {
            if (alliance == Alliance.RED) return end > 0 ? HiveCell.RED_SCORING : HiveCell.RED_AUDIENCE;
            return end > 0 ? HiveCell.BLUE_SCORING : HiveCell.BLUE_AUDIENCE;
        }

        /** The end that is up now: +1, −1, or 0 mid-tip. */
        int raisedEnd() {
            HiveState s = state();
            if (s == HiveState.TRANSITION) return 0;
            return angle > 0 ? 1 : -1;
        }

        /** World (Pedro) → rocker frame {u across, v along, w up from the axle, rocker level}. */
        double[] toLocal(double x, double y, double z) {
            double dy = y - CENTRE_IN, dz = z - PIVOT_Z_IN;
            double c = Math.cos(angle), s = Math.sin(angle);
            return new double[] {x - centreX, dy * c + dz * s, -dy * s + dz * c};
        }

        double[] toWorld(double u, double v, double w) {
            double c = Math.cos(angle), s = Math.sin(angle);
            return new double[] {centreX + u, CENTRE_IN + v * c - w * s, PIVOT_Z_IN + v * s + w * c};
        }

        /** A rocker-frame direction in the world. */
        double[] dirToWorld(double u, double v, double w) {
            double c = Math.cos(angle), s = Math.sin(angle);
            return new double[] {u, v * c - w * s, v * s + w * c};
        }

        /** Which end's CELL the local point is inside, or 0. */
        static int cellAt(double[] local) {
            double v = Math.abs(local[1]);
            boolean inside = Math.abs(local[0]) < CELL_HALF_WIDTH_IN && v > CELL_BACK_IN && v < CELL_OPENING_IN
                    && local[2] > CELL_FLOOR_IN && local[2] < CELL_ROOF_IN;
            return inside ? (local[1] > 0 ? 1 : -1) : 0;
        }

        /** The middle of the raised CELL's opening, in the world, or null mid-tip. */
        double[] openingCentre() {
            int end = raisedEnd();
            if (end == 0) return null;
            return toWorld(0, end * CELL_OPENING_IN, (CELL_FLOOR_IN + CELL_ROOF_IN) / 2);
        }

        /** A point just inside the raised CELL, the best place to aim. */
        double[] aimPoint() {
            int end = raisedEnd();
            if (end == 0) return null;
            return toWorld(0, end * (CELL_OPENING_IN - 2.5), CELL_FLOOR_IN + 0.45 * HiveGeometry.OPENING_HEIGHT_IN);
        }
    }

    final List<Piece> pieces = new ArrayList<>();
    final List<double[]> flowers = new ArrayList<>();
    final Rocker red = new Rocker(Alliance.RED, RED_HIVE_X_IN);
    final Rocker blue = new Rocker(Alliance.BLUE, BLUE_HIVE_X_IN);
    private final Rocker[] rockers = {red, blue};
    final Random random;
    /** Spill and catch variety, apart from {@link #random} so a seed's shots do not change. */
    private final Random variety;
    /** A gentle unevenness per 12 in square of tiles: the sideways pull, in/s². */
    private final double[][][] tileSlope = new double[12][12][2];
    private final List<String> events = new ArrayList<>();

    /**
     * A robot on the field, as its caller last placed it: an 18 in box that pushes pieces, an intake
     * that takes them, and the pieces it holds. Several can share a field, each driven by its own
     * caller.
     */
    static final class Bot {
        private boolean present;
        private double x, y, h, vx, vy, w;
        private double prevX, prevY, prevH;
        private boolean intaking;
        private double lastCaptureAt = Double.NEGATIVE_INFINITY;
        final List<Piece> stored = new ArrayList<>();
        /** Its mechanisms; {@link RobotDesign#standard} unless the caller sets one. */
        RobotDesign design = RobotDesign.standard();

        /**
         * Where the robot is this loop. The previous pose and this one are blended across the
         * sub-steps, so a moving robot sweeps pieces instead of jumping over them.
         */
        void set(double x, double y, double heading, double vx, double vy, double omega, boolean intake) {
            if (!present) {
                prevX = x;
                prevY = y;
                prevH = heading;
            } else {
                prevX = this.x;
                prevY = this.y;
                prevH = h;
            }
            present = true;
            this.x = x;
            this.y = y;
            h = heading;
            this.vx = vx;
            this.vy = vy;
            w = omega;
            intaking = intake;
        }

        double[] pose() {
            return new double[] {x, y, h};
        }

        /** Whether its launcher is set to throw over the back (RobotDesign#launchesBothWays). */
        boolean launchingBack;

        /** Where a launched piece leaves it, {@code sideIn} to its left of the centre line. */
        double[] exitPoint(double sideIn) {
            double toward = launchingBack ? h + Math.PI : h;
            double c = Math.cos(toward), s = Math.sin(toward);
            return new double[] {x + PLACEHOLDER_EXIT_FORWARD_IN * c - sideIn * s,
                    y + PLACEHOLDER_EXIT_FORWARD_IN * s + sideIn * c, PLACEHOLDER_EXIT_HEIGHT_IN};
        }
    }

    /** Every robot on the field; the first is the one the single-robot methods below act on. */
    final List<Bot> bots = new ArrayList<>();
    final Bot main = addBot();
    /** What {@link #main} holds. */
    final List<Piece> stored = main.stored;
    /** Robots that stand still (a partner that does not move): {x, y, heading} each, 18 in square. */
    final List<double[]> parkedRobots = new ArrayList<>();

    /** Another robot on the field, which its caller places each loop with {@link Bot#set}. */
    Bot addBot() {
        Bot b = new Bot();
        bots.add(b);
        return b;
    }

    /** The physical constants {@link HiveCalibration#fit} chose. */
    static final class Physics {
        /** NECTAR's weight, in POLLEN weights. */
        final double nectarWeight;
        /** What holds a settled HIVE on its stop, in POLLEN-weight inches. */
        final double holdTorque;
        /** How fast the damped rocker swings when pushed by its whole holding torque, rad/s. */
        final double swingRadPerS;
        final double tileRestitution;

        Physics(double nectarWeight, double holdTorque, double swingRadPerS, double tileRestitution) {
            this.nectarWeight = nectarWeight;
            this.holdTorque = holdTorque;
            this.swingRadPerS = swingRadPerS;
            this.tileRestitution = tileRestitution;
        }
    }

    final Physics physics;
    /** Simulated seconds since the start. */
    double time;

    /** The field as it starts, fitted to the current {@link HiveCalibration}. */
    FieldSim(List<HiveAssets.StagedPiece> staged, long seed) {
        this(staged, seed, HiveCalibration.current().fit());
    }

    FieldSim(List<HiveAssets.StagedPiece> staged, long seed, Physics physics) {
        this.physics = physics;
        random = new Random(seed);
        variety = new Random(seed * 7919L + 13);
        for (int i = 0; i < tileSlope.length; i++) {
            for (int j = 0; j < tileSlope[i].length; j++) {
                double a = variety.nextDouble() * 2 * Math.PI, m = variety.nextDouble() * PLACEHOLDER_TILE_SLOPE_IN_PER_S2;
                tileSlope[i][j][0] = m * Math.cos(a);
                tileSlope[i][j][1] = m * Math.sin(a);
            }
        }
        for (HiveAssets.StagedPiece s : staged) {
            Piece p = new Piece(Kind.of(s.kind), s.holder.equals("outside") ? Where.OUTSIDE : Where.FIELD, s.x, s.y, s.z);
            if (s.holder.equals("flower")) p.flower = flowerIndex(s.x, s.y);
            p.rollScale = Math.exp(PLACEHOLDER_ROLL_SPREAD * variety.nextGaussian());
            pieces.add(p);
        }
        for (Piece p : pieces) updateCell(p);
        events.clear(); // the NECTAR that starts in each raised CELL was not scored
    }

    private int flowerIndex(double x, double y) {
        for (int i = 0; i < flowers.size(); i++) {
            if (Math.hypot(flowers.get(i)[0] - x, flowers.get(i)[1] - y) < 1.0) return i;
        }
        flowers.add(new double[] {x, y});
        return flowers.size() - 1;
    }

    double weight(Kind kind) {
        return kind == Kind.POLLEN ? 1.0 : physics.nectarWeight;
    }

    /**
     * The pieces in a rocker's CELLs pushing it toward its other stop, in POLLEN-weight inches:
     * each piece's weight times how far it is past the axle, positive toward a TIP.
     */
    double tippingTorque(Rocker r) {
        double torque = 0;
        for (Piece p : pieces) {
            if (p.where == Where.FIELD && p.cell != null && p.cell.alliance() == r.alliance) {
                torque += weight(p.kind) * (p.y - CENTRE_IN);
            }
        }
        return Math.signum(r.angle) * torque;
    }

    /**
     * A piece gently placed in a rocker's raised CELL the way field staff calibrate a HIVE (Event
     * Field Setup Guide §12.2): against the back skin, in the first free spot along it from the
     * CELL wall nearest the field perimeter, and in the next row out once the back row is full.
     * Returns null mid-tip.
     */
    Piece placeInRaisedCell(Rocker r, Kind kind) {
        int end = r.raisedEnd();
        if (end == 0) return null;
        double rad = kind.radius;
        double outward = r.alliance == Alliance.RED ? -1 : 1; // the perimeter side
        for (int row = 0; row < 4; row++) {
            for (double u = CELL_HALF_WIDTH_IN - rad; u >= -(CELL_HALF_WIDTH_IN - rad); u -= 0.1) {
                double v = end * (CELL_BACK_IN + rad + 0.05 + row * 2 * POLLEN_RADIUS_IN);
                double w = CELL_FLOOR_IN + rad + 0.05;
                double[] at = r.toWorld(outward * u, v, w);
                if (free(at, rad)) {
                    Piece p = new Piece(kind, Where.FIELD, at[0], at[1], at[2]);
                    pieces.add(p);
                    updateCell(p);
                    return p;
                }
            }
        }
        throw new IllegalStateException("the CELL is full");
    }

    /**
     * A piece tossed into a rocker's raised CELL: in through the middle of the opening, moving
     * toward the back at {@code speed} in/s. Returns null mid-tip.
     */
    Piece tossIntoRaisedCell(Rocker r, Kind kind, double speed) {
        int end = r.raisedEnd();
        if (end == 0) return null;
        double[] at = r.toWorld(0, end * (CELL_OPENING_IN - kind.radius - 0.5),
                (CELL_FLOOR_IN + CELL_ROOF_IN) / 2);
        double[] v = r.dirToWorld(0, -end * speed, 0);
        Piece p = new Piece(kind, Where.FIELD, at[0], at[1], at[2]);
        p.vx = v[0];
        p.vy = v[1];
        p.vz = v[2];
        pieces.add(p);
        updateCell(p);
        return p;
    }

    private boolean free(double[] at, double rad) {
        for (Piece q : pieces) {
            if (q.where != Where.FIELD) continue;
            double d = Math.sqrt((q.x - at[0]) * (q.x - at[0]) + (q.y - at[1]) * (q.y - at[1])
                    + (q.z - at[2]) * (q.z - at[2]));
            if (d < q.kind.radius + rad + 0.05) return false;
        }
        return true;
    }

    Rocker rocker(Alliance alliance) {
        return alliance == Alliance.BLUE ? blue : red;
    }

    /**
     * Puts {@link #PRELOAD_POLLEN} POLLEN in the robot: those staged outside the field nearest the
     * given wall (x = 0 for red), which is where the field CAD keeps the alliances' preloads.
     */
    void preload(Alliance alliance) {
        preload(main, alliance);
    }

    /** As {@link #preload(Alliance)}, into {@code bot}: the next 4 of the alliance's preloads. */
    void preload(Bot bot, Alliance alliance) {
        double wallX = alliance == Alliance.BLUE ? FIELD_SIZE_IN : 0;
        List<Piece> outside = new ArrayList<>();
        for (Piece p : pieces) {
            if (p.where == Where.OUTSIDE && p.kind == Kind.POLLEN && Math.abs(p.x - wallX) < 10) outside.add(p);
        }
        outside.sort((a, b) -> Double.compare(a.y, b.y));
        for (int i = 0; i < PRELOAD_POLLEN && i < outside.size(); i++) {
            Piece p = outside.get(i);
            p.where = Where.ROBOT;
            bot.stored.add(p);
        }
    }

    /**
     * The partner robot: it stands still at {@code pose} ({x, y, heading}) for the whole run, and
     * its 4 preloaded POLLEN start on the tiles at {@code spots}, which the manual allows as long as
     * they touch it (§10.3.4, G304). Its POLLEN are the alliance's other preloads.
     */
    void stagePartner(Alliance alliance, double[] pose, double[][] spots) {
        parkedRobots.add(pose);
        stagePreloads(alliance, spots);
    }

    /** Puts the next of the alliance's preloaded POLLEN on the tiles at {@code spots}. */
    void stagePreloads(Alliance alliance, double[][] spots) {
        double wallX = alliance == Alliance.BLUE ? FIELD_SIZE_IN : 0;
        int i = 0;
        for (Piece p : pieces) {
            if (i >= spots.length) break;
            if (p.where != Where.OUTSIDE || p.kind != Kind.POLLEN || Math.abs(p.x - wallX) >= 10) continue;
            p.where = Where.FIELD;
            p.x = spots[i][0];
            p.y = spots[i][1];
            p.z = POLLEN_RADIUS_IN;
            i++;
        }
        if (i < spots.length) throw new IllegalStateException("no partner preloads left to stage");
    }

    /**
     * The robot runs its intake backwards and sets one held piece on the tiles, at rest,
     * {@link #PLACEHOLDER_SET_DOWN_ROLL_IN} past its front edge: piece {@code slot} of a row of 4 across the front (slot 0 at the robot's left).
     * Staging pieces for later frees the robot to collect 4 more (G407 counts only what it controls).
     * Returns false if it holds none.
     */
    boolean setDown(Bot bot, int slot) {
        if (bot.stored.isEmpty()) return false;
        Piece p = bot.stored.remove(bot.stored.size() - 1);
        double c = Math.cos(bot.h), s = Math.sin(bot.h);
        double ahead = bot.design.frameIn / 2 + p.kind.radius + PLACEHOLDER_SET_DOWN_ROLL_IN;
        double left = (1.5 - slot) * (2 * POLLEN_RADIUS_IN + 0.2);
        p.where = Where.FIELD;
        p.cell = null;
        p.x = bot.x + ahead * c - left * s;
        p.y = bot.y + ahead * s + left * c;
        p.z = p.kind.radius;
        p.vx = p.vy = p.vz = 0;
        p.wx = p.wy = p.wz = 0;
        events.add("set down: " + name(p.kind) + " (" + bot.stored.size() + " held)");
        return true;
    }

    /** How far a piece the intake sets down rolls clear of the robot's front, in (a guess; film one). */
    static final double PLACEHOLDER_SET_DOWN_ROLL_IN = 1.5;

    /** Whether {@code (x, y)} is under the HIVE frame's footprint (Competition Manual §9.6.1). */
    static boolean underHive(double x, double y) {
        return Math.abs(x - CENTRE_IN) < FOOT_BAR_HALF_SPAN_X_IN && Math.abs(y - CENTRE_IN) < FOOT_BAR_HALF_LENGTH_IN;
    }

    /**
     * A FLOWER holder's footprint radius, in: a tube around a 2.8 in POLLEN. Not published; measure
     * one. The bottom POLLEN comes out of its retrieval opening, so an intake takes it with the
     * robot's front against the tube.
     */
    static final double PLACEHOLDER_FLOWER_RADIUS_IN = 2.0;

    /** Whether an {@code size}-square robot at {@code (x, y, heading)} overlaps any FLOWER holder. */
    boolean hitsFlower(double x, double y, double heading, double size) {
        double c = Math.cos(heading), s = Math.sin(heading), half = size / 2;
        for (double[] f : flowers) {
            double lx = (f[0] - x) * c + (f[1] - y) * s, ly = -(f[0] - x) * s + (f[1] - y) * c;
            double dx = Math.max(0, Math.abs(lx) - half), dy = Math.max(0, Math.abs(ly) - half);
            if (dx * dx + dy * dy < PLACEHOLDER_FLOWER_RADIUS_IN * PLACEHOLDER_FLOWER_RADIUS_IN) return true;
        }
        return false;
    }

    /**
     * Whether a robot footprint point at {@code (x, y)} is in the HIVE frame's feet: the bars along
     * its two sides, the only part a robot under 25.5 in tall cannot drive through.
     */
    static boolean inHiveFrame(double x, double y) {
        return Math.abs(Math.abs(x - CENTRE_IN) - FOOT_BAR_HALF_SPAN_X_IN) < FOOT_BAR_HALF_WIDTH_IN
                && Math.abs(y - CENTRE_IN) < FOOT_BAR_HALF_LENGTH_IN;
    }

    /**
     * The alliance's own LOADING ZONE (Event Field Setup Guide §8.3): red on tile A5 against the
     * x = 0 wall, blue on F2 against the far wall. Returns {xMin, xMax, yMin, yMax}.
     */
    static double[] loadingZone(Alliance alliance) {
        double tile = FIELD_SIZE_IN / 6, depth = 11;
        if (alliance == Alliance.BLUE) return new double[] {FIELD_SIZE_IN - depth, FIELD_SIZE_IN, tile, 2 * tile};
        return new double[] {0, depth, 4 * tile, 5 * tile};
    }

    /**
     * A drive-team member enters one of the alliance's NECTAR from its ALLIANCE AREA: it lands on
     * the tiles in the LOADING ZONE (G427), allowed once for each TIP of their HIVE (G426). Returns
     * false once all 5 are in.
     */
    boolean enterNectar(Alliance alliance) {
        Kind kind = alliance == Alliance.BLUE ? Kind.BLUE_NECTAR : Kind.RED_NECTAR;
        double[] zone = loadingZone(alliance);
        for (Piece p : pieces) {
            if (p.where != Where.OUTSIDE || p.kind != kind) continue;
            double y = (zone[2] + zone[3]) / 2;
            for (double dy = 0; dy < 10; dy += 4) {
                double[] at = {(zone[0] + zone[1]) / 2, y + dy, NECTAR_RADIUS_IN + 0.05};
                if (free(at, NECTAR_RADIUS_IN)) {
                    y += dy;
                    break;
                }
            }
            p.where = Where.FIELD;
            p.x = (zone[0] + zone[1]) / 2;
            p.y = y;
            p.z = NECTAR_RADIUS_IN + 4; // dropped in, not placed
            p.vx = p.vy = p.vz = 0;
            events.add("human: " + name(kind) + " into the LOADING ZONE");
            return true;
        }
        return false;
    }

    /** Messages since the last call: shots scored, tips, spills. */
    List<String> drainEvents() {
        List<String> out = new ArrayList<>(events);
        events.clear();
        return out;
    }

    /** Places {@link #main}; see {@link Bot#set}. */
    void setRobot(double x, double y, double heading, double vx, double vy, double omega, boolean intake) {
        main.set(x, y, heading, vx, vy, omega, intake);
    }

    // ---- Launching ----------------------------------------------------------------------------

    /** The ballistic launch (no drag) that puts a piece on {@code target}, or null if out of reach. */
    double[] launchVelocity(double[] from, double[] target) {
        return launchVelocity(from, target, 6);
    }

    /**
     * As {@link #launchVelocity(double[], double[])}, with the arc {@code extraPitchDeg} steeper than
     * the flattest one that comes down into the opening.
     */
    double[] launchVelocity(double[] from, double[] target, double extraPitchDeg) {
        double dx = target[0] - from[0], dy = target[1] - from[1];
        double d = Math.hypot(dx, dy), dz = target[2] - from[2];
        if (d < 1e-6) return null;
        // Steep enough to come down into the opening, not up into its lip.
        double pitch = Math.min(Math.toRadians(78), Math.atan(2 * dz / d) + Math.toRadians(extraPitchDeg));
        pitch = Math.max(pitch, Math.toRadians(35));
        double denominator = 2 * Math.cos(pitch) * Math.cos(pitch) * (d * Math.tan(pitch) - dz);
        if (denominator <= 0) return null;
        double speed = Math.sqrt(GRAVITY_IN_PER_S2 * d * d / denominator);
        return new double[] {speed * Math.cos(pitch) * dx / d, speed * Math.cos(pitch) * dy / d, speed * Math.sin(pitch)};
    }

    /** The ballistic launch at a pitch fixed by the launcher, or null if that arc cannot reach. */
    double[] launchVelocityAtPitch(double[] from, double[] target, double pitch) {
        double dx = target[0] - from[0], dy = target[1] - from[1];
        double d = Math.hypot(dx, dy), dz = target[2] - from[2];
        if (d < 1e-6) return null;
        double denominator = 2 * Math.cos(pitch) * Math.cos(pitch) * (d * Math.tan(pitch) - dz);
        if (denominator <= 0) return null;
        double speed = Math.sqrt(GRAVITY_IN_PER_S2 * d * d / denominator);
        return new double[] {speed * Math.cos(pitch) * dx / d, speed * Math.cos(pitch) * dy / d, speed * Math.sin(pitch)};
    }

    /** Where a launched piece leaves the robot. */
    double[] exitPoint() {
        return exitPoint(0);
    }

    /** Where a launched piece leaves the robot, {@code sideIn} to its left of the centre line. */
    double[] exitPoint(double sideIn) {
        return main.exitPoint(sideIn);
    }

    /**
     * Launches the robot's next stored piece at {@code target}, with a little shot-to-shot spread.
     * Returns the launch velocity, or null if the robot is empty or the target is out of reach.
     */
    double[] launch(double[] target) {
        return launch(target, 0, 0, 1);
    }

    /**
     * Launches the next stored piece at {@code target} from {@code sideIn} left of the centre line.
     * {@code yawErrorRad} turns the shot off its aim (a frame-fixed launcher not quite facing the
     * CELL), and {@code spreadScaleShot} widens the spread for this shot (a catapult's volley).
     * NECTAR leaves at the design's {@link RobotDesign#nectarSpeedFactor}.
     */
    double[] launch(double[] target, double yawErrorRad, double sideIn, double spreadScaleShot) {
        return launch(main, target, yawErrorRad, sideIn, spreadScaleShot);
    }

    /** As {@link #launch(double[], double, double, double)}, from {@code bot}. */
    double[] launch(Bot bot, double[] target, double yawErrorRad, double sideIn, double spreadScaleShot) {
        List<Piece> stored = bot.stored;
        if (stored.isEmpty()) return null;
        double[] from = bot.exitPoint(sideIn);
        from[2] += volleyUpIn;
        double[] v = Double.isNaN(bot.design.fixedPitchDeg)
                ? launchVelocity(from, target, bot.design.arcExtraPitchDeg)
                : launchVelocityAtPitch(from, target, Math.toRadians(bot.design.fixedPitchDeg));
        if (v == null) return null;
        double spread = spreadScale * spreadScaleShot;
        double speed = 1 + PLACEHOLDER_SPEED_SPREAD * spread * noise(0);
        if (!bot.design.dedicatedLaunchers) {
            speed *= stored.get(0).kind == Kind.POLLEN ? bot.design.pollenSpeedFactor : bot.design.nectarSpeedFactor;
        }
        double yaw = yawErrorRad + PLACEHOLDER_ANGLE_SPREAD_RAD * spread * noise(1);
        double c = Math.cos(yaw), s = Math.sin(yaw);
        double carried = bot.design.compensatesMotion ? 0 : 1;
        double vx = (v[0] * c - v[1] * s) * speed + carried * bot.vx;
        double vy = (v[0] * s + v[1] * c) * speed + carried * bot.vy;
        double vz = v[2] * speed * (1 + PLACEHOLDER_ANGLE_SPREAD_RAD * spread * noise(2));
        Piece p = stored.remove(0);
        p.where = Where.FIELD;
        p.x = from[0];
        p.y = from[1];
        p.z = from[2];
        p.vx = vx;
        p.vy = vy;
        p.vz = vz;
        p.wx = 0;
        p.wy = -12;
        p.wz = 0;
        return new double[] {vx, vy, vz};
    }

    /**
     * A catapult's throw: while set, every piece launched shares these three errors (speed, yaw,
     * pitch, in standard deviations) plus {@link #volleyResidual} of its own, and leaves
     * {@link #volleyUpIn} higher than a single shot (the clump's second layer).
     */
    double[] volleyNoise;
    double volleyResidual = 1;
    double volleyUpIn = 0;

    private double noise(int k) {
        return volleyNoise == null ? random.nextGaussian() : volleyNoise[k] + volleyResidual * random.nextGaussian();
    }

    /** The arc a piece launched with {@code v} from {@code from} follows until it comes down to {@code floorZ}. */
    static List<double[]> arc(double[] from, double[] v, double floorZ, int points) {
        double a = 0.5 * GRAVITY_IN_PER_S2;
        double t = (v[2] + Math.sqrt(v[2] * v[2] + 4 * a * (from[2] - floorZ))) / (2 * a);
        List<double[]> out = new ArrayList<>();
        for (int i = 0; i <= points; i++) {
            double s = t * i / points;
            out.add(new double[] {from[0] + v[0] * s, from[1] + v[1] * s, from[2] + v[2] * s - a * s * s});
        }
        return out;
    }

    // ---- Stepping -----------------------------------------------------------------------------

    private double[][] sub = new double[0][];

    /** Advances everything by {@code dt} seconds (one robot loop). */
    void step(double dt) {
        if (sub.length != bots.size()) sub = new double[bots.size()][3];
        double h = dt / SUBSTEPS;
        for (int k = 1; k <= SUBSTEPS; k++) {
            time += h;
            double f = (double) k / SUBSTEPS;
            int nb = bots.size();
            for (int b = 0; b < nb; b++) {
                Bot bot = bots.get(b);
                sub[b][0] = bot.prevX + (bot.x - bot.prevX) * f;
                sub[b][1] = bot.prevY + (bot.y - bot.prevY) * f;
                sub[b][2] = bot.prevH + AdvantageScopeFrame.wrap(bot.h - bot.prevH) * f;
            }

            for (Rocker r : rockers) stepRocker(r, h);
            for (Piece p : pieces) {
                if (p.where != Where.FIELD) continue;
                p.vz -= GRAVITY_IN_PER_S2 * h;
                if (air && p.z > p.kind.radius + 0.5) applyAir(p, h);
                p.x += p.vx * h;
                p.y += p.vy * h;
                p.z += p.vz * h;
                integrateSpin(p, h);
            }
            collidePieces();
            for (Piece p : pieces) {
                if (p.where != Where.FIELD) continue;
                Bot taker = null;
                for (int b = 0; b < nb && taker == null; b++) {
                    Bot bot = bots.get(b);
                    if (bot.present && bot.intaking && bot.stored.size() < ROBOT_CAPACITY && canTake(bot, p)
                            && inIntake(bot, p, sub[b][0], sub[b][1], sub[b][2]) && grabs(bot, p)) taker = bot;
                }
                if (taker != null) {
                    capture(taker, p);
                    continue;
                }
                boolean contact = false;
                for (Rocker r : rockers) contact |= collideRocker(p, r);
                contact |= collideFootBars(p);
                for (int b = 0; b < nb; b++) {
                    if (bots.get(b).present) contact |= collideRobot(bots.get(b), p, sub[b][0], sub[b][1], sub[b][2]);
                }
                for (double[] parked : parkedRobots) contact |= collideParked(p, parked);
                contact |= collideField(p);
                if (p.flower >= 0) holdInFlower(p);
                if (contact) {
                    applyFriction(p, h);
                    // Uneven tiles move a rolling piece; one at rest stays put (static friction).
                    if (p.z < p.kind.radius + 0.3 && p.cell == null && spillVariety > 0 && Math.hypot(p.vx, p.vy) > 1) {
                        double[] g = tileSlope[(int) Math.max(0, Math.min(11, p.x / 12))][(int) Math.max(0, Math.min(11, p.y / 12))];
                        p.vx += g[0] * spillVariety * h;
                        p.vy += g[1] * spillVariety * h;
                    }
                }
                updateCell(p);
            }
        }
    }

    private void stepRocker(Rocker r, double h) {
        if (r.locked) return;
        double hold = physics.holdTorque;
        // The empty rocker is top-heavy, so it leans whichever way it already leans; at a stop that
        // is the holding torque, which the pieces in the raised CELL must beat.
        double torque = hold * Math.sin(r.angle) / Math.sin(TILT_RAD);
        for (Piece p : pieces) {
            if (p.where == Where.FIELD && p.cell != null && p.cell.alliance() == r.alliance) {
                torque -= weight(p.kind) * (p.y - CENTRE_IN);
            }
        }
        double before = r.angle;
        if ((r.angle >= TILT_RAD && torque >= 0) || (r.angle <= -TILT_RAD && torque <= 0)) {
            r.rate = 0;
        } else {
            r.rate = torque / hold * physics.swingRadPerS * swingScale;
            if (Math.signum(r.rate) == Math.signum(r.angle) && TILT_RAD - Math.abs(r.angle) < PLACEHOLDER_DAMPER_ZONE_RAD) {
                r.rate *= PLACEHOLDER_DAMPER_FACTOR;
            }
            r.angle = Math.max(-TILT_RAD, Math.min(TILT_RAD, r.angle + r.rate * h));
        }
        boolean settledNow = Math.abs(Math.abs(r.angle) - TILT_RAD) < 1e-12;
        boolean wasSettled = Math.abs(Math.abs(before) - TILT_RAD) < 1e-12;
        if (wasSettled && !settledNow) {
            r.tipFrom = before;
            r.leftStopAt = time - h;
            r.startCounted = false;
        }
        if (!settledNow && !r.startCounted && Math.abs(r.angle - r.tipFrom) > TIP_STARTED_RAD) {
            r.tipsStarted++;
            r.startCounted = true;
        }
        if (!wasSettled && settledNow) {
            r.rate = 0;
            // A rocker that lifts off its stop and settles back (a piece rolling to the back of the
            // CELL) has not tipped.
            if (Math.signum(r.angle) != Math.signum(r.tipFrom)) {
                r.tips++;
                r.lastTipSeconds = time - r.leftStopAt;
                events.add(r.alliance + " HIVE tipped: " + r.state() + " (" + r.cell(r.raisedEnd()).clusterName() + " up)");
            }
        }
    }

    private void collidePieces() {
        for (int i = 0; i < pieces.size(); i++) {
            Piece a = pieces.get(i);
            if (a.where != Where.FIELD) continue;
            for (int j = i + 1; j < pieces.size(); j++) {
                Piece b = pieces.get(j);
                if (b.where != Where.FIELD) continue;
                double reach = a.kind.radius + b.kind.radius;
                double dx = b.x - a.x;
                if (dx > reach || dx < -reach) continue;
                double dy = b.y - a.y, dz = b.z - a.z;
                double d2 = dx * dx + dy * dy + dz * dz;
                if (d2 >= reach * reach || d2 < 1e-12) continue;
                double d = Math.sqrt(d2);
                double nx = dx / d, ny = dy / d, nz = dz / d;
                double ma = weight(a.kind), mb = weight(b.kind);
                double push = (reach - d) / (ma + mb);
                a.x -= nx * push * mb;
                a.y -= ny * push * mb;
                a.z -= nz * push * mb;
                b.x += nx * push * ma;
                b.y += ny * push * ma;
                b.z += nz * push * ma;
                double vn = (b.vx - a.vx) * nx + (b.vy - a.vy) * ny + (b.vz - a.vz) * nz;
                if (vn < 0) {
                    double e = vn > -RESTING_IN_PER_S ? 0 : bounce(PLACEHOLDER_PIECE_RESTITUTION);
                    double j2 = -(1 + e) * vn / (1 / ma + 1 / mb);
                    a.vx -= j2 * nx / ma;
                    a.vy -= j2 * ny / ma;
                    a.vz -= j2 * nz / ma;
                    b.vx += j2 * nx / mb;
                    b.vy += j2 * ny / mb;
                    b.vz += j2 * nz / mb;
                }
            }
        }
    }

    /** Both CELLs of a rocker, as thin double-sided plates: floor, roof, two sides and the back. */
    private boolean collideRocker(Piece p, Rocker r) {
        double[] local = r.toLocal(p.x, p.y, p.z);
        double rad = p.kind.radius;
        // Quick reject: nowhere near this rocker.
        if (Math.abs(local[0]) > CELL_HALF_WIDTH_IN + rad || Math.abs(local[1]) > CELL_OPENING_IN + rad
                || local[2] < CELL_FLOOR_IN - rad || local[2] > CELL_ROOF_IN + rad) {
            return false;
        }
        boolean hit = false;
        for (int end = -1; end <= 1; end += 2) {
            double vNear = end * CELL_BACK_IN, vFar = end * CELL_OPENING_IN;
            double vLo = Math.min(vNear, vFar), vHi = Math.max(vNear, vFar);
            // floor and roof: w fixed
            hit |= plate(p, r, 2, CELL_FLOOR_IN, 0, -CELL_HALF_WIDTH_IN, CELL_HALF_WIDTH_IN, 1, vLo, vHi);
            hit |= plate(p, r, 2, CELL_ROOF_IN, 0, -CELL_HALF_WIDTH_IN, CELL_HALF_WIDTH_IN, 1, vLo, vHi);
            // sides: u fixed
            hit |= plate(p, r, 0, -CELL_HALF_WIDTH_IN, 1, vLo, vHi, 2, CELL_FLOOR_IN, CELL_ROOF_IN);
            hit |= plate(p, r, 0, CELL_HALF_WIDTH_IN, 1, vLo, vHi, 2, CELL_FLOOR_IN, CELL_ROOF_IN);
            // back: v fixed
            hit |= plate(p, r, 1, vNear, 0, -CELL_HALF_WIDTH_IN, CELL_HALF_WIDTH_IN, 2, CELL_FLOOR_IN, CELL_ROOF_IN);
        }
        return hit;
    }

    /**
     * A rectangle in the rocker frame: axis {@code fixed} at {@code at}, the other two axes over the
     * given ranges. Pushes the piece out and bounces it off the plate's own motion.
     */
    private boolean plate(Piece p, Rocker r, int fixed, double at, int a1, double lo1, double hi1,
                          int a2, double lo2, double hi2) {
        double[] local = r.toLocal(p.x, p.y, p.z);
        double[] q = local.clone();
        q[fixed] = at;
        q[a1] = Math.max(lo1, Math.min(hi1, local[a1]));
        q[a2] = Math.max(lo2, Math.min(hi2, local[a2]));
        double nx = local[0] - q[0], ny = local[1] - q[1], nz = local[2] - q[2];
        double d = Math.sqrt(nx * nx + ny * ny + nz * nz);
        double rad = p.kind.radius;
        if (d >= rad) return false;
        if (d < 1e-9) {
            nx = ny = nz = 0;
            if (fixed == 0) nx = 1;
            else if (fixed == 1) ny = 1;
            else nz = 1;
            d = 0;
        } else {
            nx /= d;
            ny /= d;
            nz /= d;
        }
        double[] pos = r.toWorld(q[0] + nx * rad, q[1] + ny * rad, q[2] + nz * rad);
        double[] n = r.dirToWorld(nx, ny, nz);
        double[] contact = r.toWorld(q[0], q[1], q[2]);
        // The plate's own velocity at the contact: rotation about the x axis through the pivot.
        double pvy = -r.rate * (contact[2] - PIVOT_Z_IN);
        double pvz = r.rate * (contact[1] - CENTRE_IN);
        p.x = pos[0];
        p.y = pos[1];
        p.z = pos[2];
        bounce(p, n, 0, pvy, pvz, bounce(PLACEHOLDER_HIVE_RESTITUTION));
        return true;
    }

    private boolean collideFootBars(Piece p) {
        boolean hit = false;
        for (int side = -1; side <= 1; side += 2) {
            double cx = CENTRE_IN + side * FOOT_BAR_HALF_SPAN_X_IN;
            hit |= box(p, cx, CENTRE_IN, 0, FOOT_BAR_HALF_WIDTH_IN, FOOT_BAR_HALF_LENGTH_IN, FOOT_BAR_HEIGHT_IN,
                    0, 0, 0, bounce(PLACEHOLDER_HIVE_RESTITUTION));
        }
        return hit;
    }

    private boolean collideRobot(Bot bot, Piece p, double bx, double by, double bh) {
        double half = bot.design.frameIn / 2;
        return box(p, bx, by, bh, half, half, PLACEHOLDER_ROBOT_HEIGHT_IN, bot.vx, bot.vy, bot.w,
                bounce(robotRestitution));
    }

    private boolean collideParked(Piece p, double[] at) {
        double half = ROBOT_SIZE_IN / 2;
        return box(p, at[0], at[1], at[2], half, half, PLACEHOLDER_ROBOT_HEIGHT_IN, 0, 0, 0,
                bounce(robotRestitution));
    }

    /**
     * A box standing on the tiles, centred at {@code (cx, cy)} and turned {@code heading}, moving at
     * {@code (vx, vy)} and turning at {@code omega}.
     */
    private boolean box(Piece p, double cx, double cy, double heading, double halfX, double halfY, double height,
                        double vx, double vy, double omega, double restitution) {
        double c = Math.cos(heading), s = Math.sin(heading);
        double lx = (p.x - cx) * c + (p.y - cy) * s;
        double ly = -(p.x - cx) * s + (p.y - cy) * c;
        double rad = p.kind.radius;
        if (Math.abs(lx) > halfX + rad || Math.abs(ly) > halfY + rad || p.z > height + rad) return false;
        double qx = Math.max(-halfX, Math.min(halfX, lx));
        double qy = Math.max(-halfY, Math.min(halfY, ly));
        double qz = Math.max(0, Math.min(height, p.z));
        double nx = lx - qx, ny = ly - qy, nz = p.z - qz;
        double d = Math.sqrt(nx * nx + ny * ny + nz * nz);
        double depth;
        if (d < 1e-9) {
            // Centre inside the box: out through the nearest face.
            double ex = halfX - Math.abs(lx), ey = halfY - Math.abs(ly), ez = height - p.z;
            if (ex <= ey && ex <= ez) {
                nx = Math.signum(lx) == 0 ? 1 : Math.signum(lx);
                ny = nz = 0;
                depth = ex + rad;
            } else if (ey <= ez) {
                ny = Math.signum(ly) == 0 ? 1 : Math.signum(ly);
                nx = nz = 0;
                depth = ey + rad;
            } else {
                nz = 1;
                nx = ny = 0;
                depth = ez + rad;
            }
        } else {
            if (d >= rad) return false;
            nx /= d;
            ny /= d;
            nz /= d;
            depth = rad - d;
        }
        double wnx = nx * c - ny * s, wny = nx * s + ny * c;
        p.x += wnx * depth;
        p.y += wny * depth;
        p.z += nz * depth;
        double rx0 = p.x - cx, ry0 = p.y - cy;
        bounce(p, new double[] {wnx, wny, nz}, vx - omega * ry0, vy + omega * rx0, 0, restitution);
        return true;
    }

    private boolean collideField(Piece p) {
        double r = p.kind.radius;
        boolean hit = false;
        if (p.z < r) {
            p.z = r;
            if (p.vz < 0) p.vz = -p.vz * bounce(physics.tileRestitution);
            if (Math.abs(p.vz) < 8) p.vz = 0; // settle instead of buzzing
            hit = true;
        }
        if (p.x < r) {
            p.x = r;
            if (p.vx < 0) p.vx = -p.vx * bounce(PLACEHOLDER_WALL_RESTITUTION);
            hit = true;
        }
        if (p.x > FIELD_SIZE_IN - r) {
            p.x = FIELD_SIZE_IN - r;
            if (p.vx > 0) p.vx = -p.vx * bounce(PLACEHOLDER_WALL_RESTITUTION);
            hit = true;
        }
        if (p.y < r) {
            p.y = r;
            if (p.vy < 0) p.vy = -p.vy * bounce(PLACEHOLDER_WALL_RESTITUTION);
            hit = true;
        }
        if (p.y > FIELD_SIZE_IN - r) {
            p.y = FIELD_SIZE_IN - r;
            if (p.vy > 0) p.vy = -p.vy * bounce(PLACEHOLDER_WALL_RESTITUTION);
            hit = true;
        }
        return hit;
    }

    /** A Flower holder is a tube: what is stacked in it stays over its centre. */
    private void holdInFlower(Piece p) {
        double[] f = flowers.get(p.flower);
        p.x = f[0];
        p.y = f[1];
        p.vx = 0;
        p.vy = 0;
    }

    /** Reflects the piece's velocity relative to a surface moving at {@code (sx, sy, sz)}. */
    private static void bounce(Piece p, double[] n, double sx, double sy, double sz, double restitution) {
        double rx0 = p.vx - sx, ry0 = p.vy - sy, rz0 = p.vz - sz;
        double vn = rx0 * n[0] + ry0 * n[1] + rz0 * n[2];
        if (vn < 0) {
            // A slow contact is a resting one: no bounce, or stacked pieces buzz.
            double k = (1 + (vn > -RESTING_IN_PER_S ? 0 : restitution)) * vn;
            rx0 -= k * n[0];
            ry0 -= k * n[1];
            rz0 -= k * n[2];
        }
        p.vx = rx0 + sx;
        p.vy = ry0 + sy;
        p.vz = rz0 + sz;
    }

    private static void applyFriction(Piece p, double h) {
        double speed = Math.hypot(p.vx, p.vy);
        double slower = Math.max(0, speed * (1 - PLACEHOLDER_CONTACT_FRICTION * frictionScale * h)
                - PLACEHOLDER_ROLLING_DECEL_IN_PER_S2 * frictionScale * (1 + (p.rollScale - 1) * spillVariety) * h);
        double keep = speed < 1e-9 ? 0 : slower / speed;
        p.vx *= keep;
        p.vy *= keep;
        // Rolling: spin to match the ground speed.
        p.wx = p.vy / p.kind.radius;
        p.wy = -p.vx / p.kind.radius;
        p.wz = 0;
    }

    private static void integrateSpin(Piece p, double h) {
        double w = Math.sqrt(p.wx * p.wx + p.wy * p.wy + p.wz * p.wz);
        if (w < 1e-9) return;
        double half = 0.5 * w * h, s = Math.sin(half) / w;
        double dw = Math.cos(half), dx = p.wx * s, dy = p.wy * s, dz = p.wz * s;
        double nw = dw * p.qw - dx * p.qx - dy * p.qy - dz * p.qz;
        double nx = dw * p.qx + dx * p.qw + dy * p.qz - dz * p.qy;
        double ny = dw * p.qy - dx * p.qz + dy * p.qw + dz * p.qx;
        double nz = dw * p.qz + dx * p.qy - dy * p.qx + dz * p.qw;
        double norm = Math.sqrt(nw * nw + nx * nx + ny * ny + nz * nz);
        p.qw = nw / norm;
        p.qx = nx / norm;
        p.qy = ny / norm;
        p.qz = nz / norm;
    }

    /**
     * Whether the intake may take this piece now: one piece per {@link RobotDesign#intakeIntervalS},
     * NECTAR only if the robot launches it, and out of a FLOWER only the bottom POLLEN, through the
     * retrieval opening, one per {@link RobotDesign#flowerPullS} (Competition Manual §9.7, G418).
     */
    private boolean canTake(Bot bot, Piece p) {
        RobotDesign design = bot.design;
        double lastCaptureAt = bot.lastCaptureAt;
        if (p.kind != Kind.POLLEN && !design.launchesNectar) return false;
        if (p.flower >= 0) {
            return p.kind == Kind.POLLEN && p.z < RobotDesign.FLOWER_OPENING_HEIGHT_IN
                    && time - lastCaptureAt >= design.flowerPullS;
        }
        return time - lastCaptureAt >= design.intakeIntervalS;
    }

    /**
     * Whether the intake holds on to a piece that reached it (mentor review: catching was perfect).
     * Too fast relative to the robot and it bounces off; otherwise it is kept with the design's grab
     * chance, and one that got away is not tried again for 0.3 s. Pieces in a FLOWER are pulled out
     * by the intake, so always held.
     */
    private boolean grabs(Bot bot, Piece p) {
        if (p.flower >= 0) return true;
        if (time < p.rejectedUntil) return false;
        double rel = Math.hypot(p.vx - bot.vx, p.vy - bot.vy);
        if (rel > bot.design.intakeMaxSpeedInPerS || variety.nextDouble() > bot.design.intakeGrabChance) {
            p.rejectedUntil = time + 0.3;
            return false;
        }
        return true;
    }

    private boolean inIntake(Bot bot, Piece p, double bx, double by, double bh) {
        RobotDesign design = bot.design;
        double c = Math.cos(bh), s = Math.sin(bh);
        double lx = (p.x - bx) * c + (p.y - by) * s;
        double ly = -(p.x - bx) * s + (p.y - by) * c;
        if (design.intakeAtBack) lx = -lx;
        double mouth = design.frameIn / 2 + design.intakeReachIn;
        return lx > mouth - 2 && lx < mouth + 3 && Math.abs(ly) < design.intakeWidthIn / 2 && p.z < 6;
    }

    private void capture(Bot bot, Piece p) {
        List<Piece> stored = bot.stored;
        bot.lastCaptureAt = time;
        p.where = Where.ROBOT;
        p.flower = -1;
        p.cell = null;
        p.vx = p.vy = p.vz = 0;
        p.wx = p.wy = p.wz = 0;
        stored.add(p);
        events.add("intake: " + name(p.kind) + " (" + stored.size() + " held)");
    }

    private void updateCell(Piece p) {
        HiveCell now = null;
        for (Rocker r : rockers) {
            int end = Rocker.cellAt(r.toLocal(p.x, p.y, p.z));
            if (end != 0) now = r.cell(end);
        }
        if (now != p.cell) {
            if (now != null && p.cell == null) events.add("score: " + name(p.kind) + " into " + now.clusterName());
            if (now == null && p.cell != null) {
                events.add("spill: " + name(p.kind) + " out of " + p.cell.clusterName());
                if (fromLoweredCell(p.cell)) {
                    p.vx *= spillExitScale;
                    p.vy *= spillExitScale;
                    p.vz *= spillExitScale;
                }
                if (spillVariety > 0 && variety != null) {
                    double k = PLACEHOLDER_SPILL_KICK_IN_PER_S * spillVariety;
                    p.vx += k * variety.nextGaussian();
                    p.vy += k * variety.nextGaussian();
                    p.wz += 6 * spillVariety * variety.nextGaussian();
                }
            }
            p.cell = now;
        }
    }

    /**
     * Whether {@code cell} is not its rocker's raised one: tipping or down, so a piece leaving it is a
     * spill. A shot that rebounds out of the raised CELL keeps its speed.
     */
    private boolean fromLoweredCell(HiveCell cell) {
        for (Rocker r : rockers) {
            if (r.alliance != cell.alliance()) continue;
            int raised = r.raisedEnd();
            return raised == 0 || r.cell(raised) != cell;
        }
        return false;
    }

    /** Pieces in a CELL. */
    int count(HiveCell cell) {
        int n = 0;
        for (Piece p : pieces) if (p.where == Where.FIELD && p.cell == cell) n++;
        return n;
    }

    static String name(Kind k) {
        switch (k) {
            case POLLEN: return "POLLEN";
            case RED_NECTAR: return "red NECTAR";
            default: return "blue NECTAR";
        }
    }

    // ---- For the log --------------------------------------------------------------------------

    /**
     * Pieces of {@code kind} as AdvantageScope Pose3d values (meters, Center/Rotated), packed
     * {@code x, y, z, qw, qx, qy, qz}: those the robot holds ({@code held}, drawn inside it), or all
     * the others. Kept apart so a moving robot does not rewrite every piece on the field each loop.
     */
    double[] pieces(Kind kind, boolean held) {
        int n = 0;
        for (Piece p : pieces) if (p.kind == kind && (p.where == Where.ROBOT) == held) n++;
        double[] out = new double[7 * n];
        int i = 0;
        for (Piece p : pieces) {
            if (p.kind != kind || (p.where == Where.ROBOT) != held) continue;
            double x = p.x, y = p.y, z = p.z;
            if (p.where == Where.ROBOT) {
                for (Bot bot : bots) {
                    int slot = bot.stored.indexOf(p);
                    if (slot < 0) continue;
                    x = bot.x - 2.5 * Math.cos(bot.h);
                    y = bot.y - 2.5 * Math.sin(bot.h);
                    z = 4 + slot * 2.2 * p.kind.radius;
                }
            }
            // Pedro → Center/Rotated is a quarter turn about z, (x, y, z) → (−y, x, z); a rotation's
            // axis turns with it.
            double qw = p.qw, qx = -p.qy, qy = p.qx, qz = p.qz;
            // Rounded to a millimetre (and the rotation to match), so a piece at rest writes the
            // same value every loop and the log only grows while something moves.
            out[i++] = mm(AdvantageScopeFrame.xMeters(x, y));
            out[i++] = mm(AdvantageScopeFrame.yMeters(x, y));
            out[i++] = mm(z * AdvantageScopeFrame.METERS_PER_INCH);
            out[i++] = Math.round(qw * 1e3) / 1e3;
            out[i++] = Math.round(qx * 1e3) / 1e3;
            out[i++] = Math.round(qy * 1e3) / 1e3;
            out[i++] = Math.round(qz * 1e3) / 1e3;
        }
        return out;
    }

    private static double mm(double meters) {
        return Math.round(meters * 1e3) / 1e3;
    }

    /**
     * The two rockers as component poses for the {@value HiveAssets#ROBOT_NAME} asset placed at the
     * origin: each turned about the axle by how far it is from its as-built angle. Pedro's x axis
     * is Center/Rotated's y axis, so that is a rotation about y through {@code (0, 0, pivot)}.
     */
    double[] hiveComponents() {
        double[] out = new double[14];
        Rocker[] order = new Rocker[2];
        order[HiveAssets.RED_COMPONENT] = red;
        order[HiveAssets.BLUE_COMPONENT] = blue;
        double h = PIVOT_Z_IN * AdvantageScopeFrame.METERS_PER_INCH;
        for (int i = 0; i < 2; i++) {
            double d = order[i].angle - order[i].builtAngle();
            int k = 7 * i;
            out[k] = -h * Math.sin(d);
            out[k + 1] = 0;
            out[k + 2] = h * (1 - Math.cos(d));
            out[k + 3] = Math.cos(d / 2);
            out[k + 4] = 0;
            out[k + 5] = Math.sin(d / 2);
            out[k + 6] = 0;
        }
        return out;
    }

    /** Points in Pedro inches as Pose3d values with no rotation, for a trajectory. */
    static double[] trajectory(List<double[]> points) {
        double[] out = new double[7 * points.size()];
        int i = 0;
        for (double[] p : points) {
            out[i++] = AdvantageScopeFrame.xMeters(p[0], p[1]);
            out[i++] = AdvantageScopeFrame.yMeters(p[0], p[1]);
            out[i++] = p[2] * AdvantageScopeFrame.METERS_PER_INCH;
            out[i++] = 1;
            out[i++] = 0;
            out[i++] = 0;
            out[i++] = 0;
        }
        return out;
    }
}
