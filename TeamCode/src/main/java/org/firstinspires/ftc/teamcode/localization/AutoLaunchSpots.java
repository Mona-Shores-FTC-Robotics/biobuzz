package org.firstinspires.ftc.teamcode.localization;

import com.pedropathing.math.Pose;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.firstinspires.ftc.teamcode.localization.generated.TeamSpots;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.FieldFrame;
import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.HiveCellState;
import org.firstinspires.ftc.teamcode.vision.Vec3;

/**
 * The spots an Auto launches from, besides its own start: a HIVE spot and an OFFSET spot at each
 * end of our HIVE, drawn for red (mentor, 16 Nov 2026). They are team spots under the same names
 * in {@code TeamCode/autos/spots.json}, which is what the Visualizer and so every generated Auto
 * reads; each pose here is read from the generated {@link TeamSpots}, which the Visualizer writes
 * with it, so a spot is moved in the Visualizer only. The drivers' Square has its own twelve
 * ({@link LaunchSpots}), which an Auto does not use.
 *
 * <p><b>Why not Square's.</b> Square's CLOSE row stops 4 in back from where it is drawn, for the
 * HIVE's lip, and there every CLOSE spot overlaps a robot still on that end's start. These are
 * placed so two of our robots can hold HIVE and OFFSET at one end together:
 * <ul>
 *   <li>{@link Spot#hive HIVE}: in front of the CELL, 40 in from its raised row (Square's CLOSE
 *       range), slid toward the centre line and turned back to aim: half an inch short of the line
 *       on the left, where the turret's side leaves 2 in to spare; 6 degrees round on the right.</li>
 *   <li>OFFSET: on our wall's side of it, about 29 degrees off square. On the left, 44 in out and
 *       2 in from a robot on HIVE. On the right, 52 in out: 4 in from a robot on the fix-TIP spot
 *       below (2 in was not enough for a robot arriving there in the simulator) and further still
 *       from one on HIVE. It also clears a robot left on that end's start, which HIVE does not.</li>
 * </ul>
 *
 * <p><b>One more, for one job:</b> {@code RIGHT_AUTO_FIX_TIP}, where a left-start Auto that has
 * crossed under the HIVE fires its preloads at the RIGHT CELL when the partner's TIP did not come
 * (mentor, 16 Nov 2026). It is square to the wall and in line with the CELL, its back edge 18 in
 * from the audience wall: no robot may start more than 18 in deep, so it clears whatever is still
 * on {@code RIGHT_START}, and it keeps the robot out of the lanes a partner drives to the WALL
 * FLOWER and the GARDEN. That puts it 35 in from the row, where volleys have hit the HIVE's lip:
 * the price of standing clear.
 *
 * <p><b>Which one a volley uses</b> is each Auto's to work out. HIVE is preferred; a pair uses one
 * each; a robot that comes under the HIVE shoots from HIVE, one that comes round the outside lane
 * from OFFSET. A left-start Auto moves up from {@code LEFT_START} to {@code LEFT_AUTO_HIVE} for
 * its first volley; a right-start Auto fires its preloads from {@code RIGHT_START}, where it is
 * placed, so that volley waits on no drive. An Auto may also launch from a spot of its own
 * (mentor, 17 Nov 2026); {@code AutoVolleySpotsTest} holds every spot an Auto on master launches
 * from, other than its start, to {@link #launchSpotProblem}.
 *
 * <p>Each heading points the turret's axis ({@code TurretSubsystem}: 3.33 in behind the centre,
 * 2.68 in right of it) at the CELL's raised row from where the spot is; move the turret's numbers
 * and the headings in {@code spots.json} move with them. Geometry only: none has been
 * launched from (16 Nov 2026; {@code doc/robot-todo.md}).
 */
public final class AutoLaunchSpots {

    private AutoLaunchSpots() {
    }

    /** Whether team spot {@code name} is an Auto launch spot: every team spot named …_AUTO_… is. */
    public static boolean isLaunchSpot(String name) {
        return name.contains("_AUTO_");
    }

    private static final List<TeamSpot> ALL = launchSpots();

    private static List<TeamSpot> launchSpots() {
        List<TeamSpot> out = new ArrayList<>();
        for (TeamSpot spot : TeamSpots.all()) {
            if (isLaunchSpot(spot.name)) out.add(spot);
        }
        return Collections.unmodifiableList(out);
    }

    /**
     * Every Auto launch spot, drawn for red: the team spots named …_AUTO_…, so one made in the
     * Visualizer (a new spot for one Auto, LEFT_AUTO_HIVE_2) is one as soon as it is saved, with
     * nothing to add here. {@link Spot} names the five the team placed by hand on 16 Nov 2026.
     */
    public static List<TeamSpot> all() {
        return ALL;
    }

    /** The raised row of the CELL a spot at red {@code y} serves (the CELL at its end), for {@code alliance}. */
    public static Vec3 cellRowFor(double y, Alliance alliance) {
        boolean left = LaunchSpots.leftHalf(Alliance.RED, y);
        HiveCell cell = left ? HiveCell.leftCell(alliance) : HiveCell.rightCell(alliance);
        return cell == null ? null : HiveFieldPoints.rowCentre(cell, HiveCellState.UP);
    }

    /**
     * How far a launch spot's heading may be from the one that aims the turret at its CELL,
     * degrees: the Visualizer sets it to a tenth as the spot is made or moved.
     */
    public static final double AIM_TOLERANCE_DEG = 0.3;

    /**
     * Why a robot centred at ({@code x}, {@code y}) and facing {@code headingRad}, drawn for red, is
     * not a place for an Auto to launch from, or null when it is one: with the turret ({@code
     * turretForwardIn} ahead of the centre and {@code turretLeftIn} left of it, locked straight
     * ahead) it aims at the raised row of the CELL at its end, within {@link #AIM_TOLERANCE_DEG},
     * and the frame is inside the field and a quarter inch short of the centre line. Every spot an
     * Auto launches from is held to it, a team spot or one of the Auto's own.
     */
    public static String launchSpotProblem(double x, double y, double headingRad,
                                           double turretForwardIn, double turretLeftIn) {
        Vec3 row = cellRowFor(y, Alliance.RED);
        if (row == null) return "has no CELL at its end";
        double aimed = LaunchSpots.aimTurret(x, y, row.x(), row.y(), turretForwardIn, turretLeftIn);
        double offDeg = Math.toDegrees(Math.IEEEremainder(aimed - headingRad, 2 * Math.PI));
        if (Math.abs(offDeg) > AIM_TOLERANCE_DEG) {
            return String.format(Locale.ROOT, "faces %.1f deg; the turret aims from there at %.1f deg:"
                    + " move it in the Visualizer, which aims it", degrees(headingRad), degrees(aimed));
        }
        return frameProblem(x, y, headingRad);
    }

    /** Footprint check for both fixed and independently aimed shots. Coordinates are red. */
    public static String frameProblem(double x, double y, double headingRad) {
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(headingRad)) {
            return "has an unknown pose";
        }
        double half = LaunchSpots.FRAME_HALF_IN, c = Math.cos(headingRad), s = Math.sin(headingRad);
        double[][] local = {{half, half}, {half, -half}, {-half, -half}, {-half, half}};
        for (double[] l : local) {
            double cx = x + l[0] * c - l[1] * s, cy = y + l[0] * s + l[1] * c;
            if (cx > FieldFrame.FIELD_CENTRE_INCHES - 0.25) {
                return String.format(Locale.ROOT, "reaches x %.1f, past the centre line less a quarter inch", cx);
            }
            if (cx < 0 || cy < 0 || cy > FieldFrame.FIELD_SIZE_INCHES) {
                return String.format(Locale.ROOT, "leaves the field at (%.1f, %.1f)", cx, cy);
            }
        }
        return null;
    }

    /**
     * How far inside the firing arc's edges the turret's aim must be at a spot an Auto fires
     * {@code LaunchOneAimed} from, degrees (mentor, 28 Nov 2026).
     */
    public static final double AIMED_SHOT_ARC_MARGIN_DEG = 5.0;

    /**
     * Why a robot centred at ({@code x}, {@code y}) and facing {@code headingRad}, drawn for red, may not fire a
     * turret-aimed shot ({@code LaunchOneAimed}) from there, or null when it may: the turret's aim at the raised
     * row of the CELL at its end is inside the firing arc ({@code arcLeftDeg}, {@code arcRightDeg}) by
     * {@link #AIMED_SHOT_ARC_MARGIN_DEG}, and the frame is inside the field and short of the centre line. The
     * heading need not aim a locked turret: that is the exception this shot is (CLAUDE.md, "Localization").
     */
    public static String aimedShotProblem(double x, double y, double headingRad, double turretForwardIn,
                                          double turretLeftIn, double arcLeftDeg, double arcRightDeg) {
        Vec3 row = cellRowFor(y, Alliance.RED);
        if (row == null) return "has no CELL at its end";
        double aimed = LaunchSpots.aimTurret(x, y, row.x(), row.y(), turretForwardIn, turretLeftIn);
        double turretDeg = Math.toDegrees(Math.IEEEremainder(aimed - headingRad, 2 * Math.PI));
        double m = AIMED_SHOT_ARC_MARGIN_DEG;
        if (turretDeg > arcLeftDeg - m || turretDeg < -(arcRightDeg - m)) {
            return String.format(Locale.ROOT, "faces %.1f deg, which puts the turret's aim %.1f deg %s of forward:"
                            + " outside the firing arc less %.0f deg (%.0f left, %.0f right)", degrees(headingRad),
                    Math.abs(turretDeg), turretDeg >= 0 ? "left" : "right", m, arcLeftDeg, arcRightDeg);
        }
        return frameProblem(x, y, headingRad);
    }

    /** A heading in degrees, [0, 360). */
    private static double degrees(double rad) {
        return (Math.toDegrees(rad) % 360 + 360) % 360;
    }

    /** A spot, drawn for red: its team spot of the same name, whose heading aims the turret from there. */
    public enum Spot {
        LEFT_AUTO_HIVE(true, TeamSpots.LEFT_AUTO_HIVE),
        LEFT_AUTO_OFFSET(false, TeamSpots.LEFT_AUTO_OFFSET),
        RIGHT_AUTO_HIVE(true, TeamSpots.RIGHT_AUTO_HIVE),
        RIGHT_AUTO_OFFSET(false, TeamSpots.RIGHT_AUTO_OFFSET),
        /** Square, in line with the RIGHT CELL, its back 18 in from the wall: see the class comment. */
        RIGHT_AUTO_FIX_TIP(false, TeamSpots.RIGHT_AUTO_FIX_TIP);

        /** As drawn, for red. */
        public final Pose red;
        /** The HIVE spot of a pair (else its OFFSET one, or the fix-TIP spot, which pairs with nothing). */
        public final boolean hive;
        /** Serves our LEFT CELL (else the RIGHT one): which half of the axle line it is on. */
        public final boolean left;

        Spot(boolean hive, TeamSpot spot) {
            this.red = spot.pose();
            this.hive = hive;
            this.left = LaunchSpots.leftHalf(Alliance.RED, spot.y);
        }

        /** The spot for {@code alliance}: blue's is red's turned half a turn about the field centre. */
        public Pose pose(Alliance alliance) {
            return alliance == Alliance.BLUE ? LaunchSpots.turned(red) : red;
        }

        /** The raised row of the CELL this spot serves, for {@code alliance}; null with no alliance. */
        public Vec3 cellRow(Alliance alliance) {
            return cellRowFor(red.y(), alliance);
        }
    }
}
