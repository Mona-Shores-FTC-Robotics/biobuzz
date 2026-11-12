package org.firstinspires.ftc.teamcode.localization;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.FieldFrame;
import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.HiveCellState;
import org.firstinspires.ftc.teamcode.vision.Vec3;

import java.util.Locale;

/**
 * The twelve spots the TeleOp's two "go to launch spot" buttons drive to, and which one
 * ({@link #choose}): the spot of the asked {@link Row} that is in line with the robot.
 *
 * <p>Two rows on each half of our HIVE, drawn for red and turned half a turn about the field centre
 * for blue:
 * <ul>
 *   <li>{@link Row#WALL} (the driver's Square): three spots along the side wall, each touching it.
 *       At Belding (7 Nov 2026) the open-floor spots were hit from the flank as the gate opened; a
 *       wall behind the robot stops a push from the front.</li>
 *   <li>{@link Row#CLOSE} (the driver's Triangle): three spots in front of them, about 36 in from the
 *       CELL, on open floor. The side two overlap the middle one, so they are alternatives.</li>
 * </ul>
 * and three {@link Column}s in each row: {@link Column#HIVE} straight in front of the CELL,
 * {@link Column#NEAR} on our side of it, {@link Column#FAR} past it (a few inches past the centre
 * line; fair in TELEOP).
 *
 * <p><b>Which one.</b> The half of the field the robot is in picks LEFT or RIGHT, and the robot's
 * place along the wall picks the column: whichever of that half's three WALL spots is nearest along
 * the wall. The row is the button's. So the driver's rule is "Square for the wall, Triangle for
 * close, the one in front of where I am", and sliding along the wall before the press picks
 * another. It replaced "the nearest of the spots" (mentor, 11 Nov 2026): with twelve spots that
 * sent half of Belding's presses to open floor by a few inches' difference. Never a spot chosen
 * from what the camera believes about the HIVE (mentor, 4 Nov 2026).
 *
 * <p><b>Placed for the turret, locked forward</b> ({@code TurretSubsystem}: 3.33 in behind the
 * centre, 1.87 in right of it). A HIVE-column spot is squared to the wall with the turret's axis in
 * line with the CELL's raised row, so the WALL one has its whole back on the wall. The others turn
 * to point the turret's axis at the row ({@link #aimTurret}) and, in the WALL row, touch the wall
 * with one corner of the frame. LEFT and RIGHT are not mirror images: the turret is not on the
 * centre line, and LEFT_WALL_NEAR sits against the far side of the SCORING FLOWER.
 *
 * <p>But for {@code RIGHT_START}, these are not team spots: they are written only here and
 * {@code TeamCode/autos/spots.json} does not list them. The Clockwise pair launches from the wall
 * row, so its {@code .pp} files carry the same numbers and {@code ClockwiseAutoTest} compares
 * them: move a wall spot here and export those two Autos again. {@code LaunchSpotsTest} checks that each
 * WALL spot touches its wall without crossing it and that no two spots that may be used together
 * overlap.
 */
public final class LaunchSpots {

    private LaunchSpots() {
    }

    /** Which button: the row along the side wall, or the one in front of it. */
    public enum Row { WALL, CLOSE }

    /** Where along the wall: on our side of the HIVE, straight in front of the CELL, or past it. */
    public enum Column { NEAR, HIVE, FAR }

    /** The frame is square: from its centre to its back, front and either side (measured, {@link ResetPositions}). */
    static final double FRAME_HALF_IN = ResetPositions.WALL_TO_CENTRE_IN;

    /**
     * A spot, drawn for red: x, y and the heading that aims the turret from there (degrees).
     *
     * <p><b>Moving one:</b> change its numbers here; {@code LaunchSpotsTest} fails a WALL spot that
     * no longer touches its wall and two neighbours that overlap. A HIVE-column spot keeps its drawn
     * heading (squared to the wall); the others are re-aimed from their place with the turret's
     * offsets as the robot has them, so their drawn heading is only what that comes to today.
     */
    public enum Spot {
        RIGHT_WALL_NEAR(Row.WALL, Column.NEAR, 35.9, 10.6, 66.9),
        /**
         * The RIGHT half's WALL-row, HIVE-column spot is our measured start itself
         * ({@link StartPositions#RED_AUDIENCE}, the team spot {@code RIGHT_START}): the robot starts
         * there, fires its preloads there, and Square brings it back there, so it is one point
         * with one name (mentor, 12 Nov 2026). Squared to the wall as placed, the turret's axis is
         * half an inch off the row's centre. The LEFT half's is its own spot: {@code LEFT_START} is
         * placed on the centre-line seam, 2.4 in from where the turret lines up.
         */
        RIGHT_START(Row.WALL, Column.HIVE, StartPositions.RED_AUDIENCE.pose),
        RIGHT_WALL_FAR(Row.WALL, Column.FAR, 75.8, 10.6, 112.8),
        RIGHT_CLOSE_NEAR(Row.CLOSE, Column.NEAR, 45.2, 27.9, 70.0),
        RIGHT_CLOSE_HIVE(Row.CLOSE, Column.HIVE, 56.1, 25.3, 90.0),
        RIGHT_CLOSE_FAR(Row.CLOSE, Column.FAR, 66.9, 27.6, 110.0),
        /** Its corner is against the SCORING FLOWER's far side, the FLOWER between it and LEFT_WALL_HIVE. */
        LEFT_WALL_NEAR(Row.WALL, Column.NEAR, 37.8, 130.7, 295.5),
        /** Back on the wall, the SCORING FLOWER about 2 in from its side. */
        LEFT_WALL_HIVE(Row.WALL, Column.HIVE, 59.9, 133.4, 270.0),
        LEFT_WALL_FAR(Row.WALL, Column.FAR, 79.5, 130.9, 247.5),
        LEFT_CLOSE_NEAR(Row.CLOSE, Column.NEAR, 49.1, 113.9, 290.0),
        LEFT_CLOSE_HIVE(Row.CLOSE, Column.HIVE, 59.9, 116.2, 270.0),
        LEFT_CLOSE_FAR(Row.CLOSE, Column.FAR, 70.8, 113.6, 250.0);

        public final Row row;
        public final Column column;
        /** As drawn, for red. */
        public final Pose red;
        /** Serves our LEFT CELL (else the RIGHT one): which half of the axle line it is on. */
        public final boolean left;
        /** Turned to point the turret's axis at its CELL from where it is drawn; a HIVE-column spot is not. */
        public final boolean aimed;

        Spot(Row row, Column column, double x, double y, double headingDeg) {
            this(row, column, new Pose(x, y, Math.toRadians(headingDeg)));
        }

        Spot(Row row, Column column, Pose red) {
            this.row = row;
            this.column = column;
            this.red = red;
            this.left = leftHalf(Alliance.RED, red.y());
            this.aimed = column != Column.HIVE;
        }

        /** The spot of {@code row} and {@code column} on the LEFT half ({@code left}) or the RIGHT. */
        static Spot of(boolean left, Row row, Column column) {
            for (Spot spot : values()) {
                if (spot.left == left && spot.row == row && spot.column == column) return spot;
            }
            throw new IllegalStateException("no " + (left ? "LEFT " : "RIGHT ") + row + " " + column + " spot");
        }

        /** As {@link #target(Alliance, double, double, double)} with the turret on the robot's centre. */
        public Pose target(Alliance alliance, double wallGapIn) {
            return target(alliance, wallGapIn, 0.0, 0.0);
        }

        /**
         * Where the button drives for {@code alliance}: worked out for red and turned half a turn
         * about the field centre for blue. A WALL spot is drawn touching its wall and is driven to
         * {@code wallGapIn} out from it (NaN or negative: 0, touching). Every spot but a HIVE-column
         * one then faces its CELL's raised row with the turret's axis ({@link #aimTurret}): the
         * turret sits {@code turretForwardIn} ahead of the centre and {@code turretLeftIn} to its
         * left, locked straight ahead.
         */
        public Pose target(Alliance alliance, double wallGapIn, double turretForwardIn, double turretLeftIn) {
            double gap = row == Row.WALL && wallGapIn > 0.0 ? wallGapIn : 0.0;
            double x = red.x();
            double y = red.y() + (left ? -gap : gap); // the LEFT wall is the high-y one
            double heading = red.heading();
            Vec3 cell = cellRow(Alliance.RED);
            if (aimed && cell != null) {
                heading = aimTurret(x, y, cell.x(), cell.y(), turretForwardIn, turretLeftIn);
            }
            Pose p = new Pose(x, y, heading);
            return alliance == Alliance.BLUE ? turned(p) : p;
        }

        /** The raised row of the CELL this spot serves, for {@code alliance}; null with no alliance. */
        public Vec3 cellRow(Alliance alliance) {
            HiveCell cell = left ? HiveCell.leftCell(alliance) : HiveCell.rightCell(alliance);
            return cell == null ? null : HiveFieldPoints.rowCentre(cell, HiveCellState.UP);
        }
    }

    /**
     * Whether a camera-vs-odometry gap at a spot is big enough to suggest a corner reset:
     * {@code gapIn} past {@code thresholdIn}. A threshold that is NaN, zero or negative never warns;
     * a NaN gap (no camera frame) never warns.
     */
    public static boolean driftWarns(double gapIn, double thresholdIn) {
        return thresholdIn > 0.0 && gapIn > thresholdIn;
    }

    /** What {@link #choose} decided: a spot, or a refusal (then {@link #spot} is null). */
    public static final class Choice {
        public final Spot spot;
        /** Where the drive goes, for our alliance; null with a refusal. */
        public final Pose target;
        /** From the robot to {@link #target}, inches; NaN with a refusal. */
        public final double distanceIn;
        /** Why nothing is driven, for the Driver Station and the log; null when a spot was chosen. */
        public final String refusal;

        private Choice(Spot spot, Pose target, double distanceIn, String refusal) {
            this.spot = spot;
            this.target = target;
            this.distanceIn = distanceIn;
            this.refusal = refusal;
        }

        static Choice refuse(String why) {
            return new Choice(null, null, Double.NaN, why);
        }

        public boolean refused() {
            return refusal != null;
        }

        /** "RIGHT_WALL_NEAR (in line, 23 in)", or the refusal. */
        @Override
        public String toString() {
            return refused() ? "refused: " + refusal
                    : String.format(Locale.US, "%s (in line, %.0f in)", spot, distanceIn);
        }
    }

    /**
     * Which spot to drive to; a pure function of what the robot knows. Refused without a pose, with
     * a pose that is not referenced (field coordinates would mean nothing), with no alliance, or with
     * the robot's centre {@code pastCentreIn} or more past the centre line, on the other alliance's
     * side. Otherwise the spot of {@code row} on the robot's half of the field, in the
     * {@link #columnFor column} the robot is in line with.
     *
     * <p>{@code pastCentreIn} 0 (or NaN, or negative) asks for the robot strictly on our side. It was
     * that until 6 Nov 2026: that day's practice refused the press 10 times with the robot 3 to 4 in
     * past the line (19429 and 20245 match logs), where the drivers pick up and wanted it to work.
     */
    public static Choice choose(Pose pose, boolean referenced, Alliance alliance, Row row, double wallGapIn,
                                double pastCentreIn) {
        return choose(pose, referenced, alliance, row, wallGapIn, pastCentreIn, 0.0, 0.0);
    }

    /** {@link #choose}, with each turned spot's heading aiming the turret ({@link Spot#target(Alliance, double, double, double)}). */
    public static Choice choose(Pose pose, boolean referenced, Alliance alliance, Row row, double wallGapIn,
                                double pastCentreIn, double turretForwardIn, double turretLeftIn) {
        if (pose == null) return Choice.refuse("no pose (no Pinpoint)");
        if (!referenced) return Choice.refuse("pose not on the field yet (corner reset or camera reset first)");
        if (alliance != Alliance.RED && alliance != Alliance.BLUE) return Choice.refuse("no alliance");
        if (!withinReach(alliance, pose.x(), pastCentreIn)) {
            return Choice.refuse(String.format(Locale.US,
                    "robot %.0f in past the centre line, more than the %.0f in it may drive back from",
                    pastLineIn(alliance, pose.x()), reach(pastCentreIn)));
        }
        boolean left = leftHalf(alliance, pose.y());
        // The spots are drawn for red: read the robot's place along the wall in red's frame.
        double xAsRed = alliance == Alliance.BLUE ? 2 * FieldFrame.FIELD_CENTRE_INCHES - pose.x() : pose.x();
        Spot spot = Spot.of(left, row == null ? Row.WALL : row, columnFor(left, xAsRed));
        Pose target = spot.target(alliance, wallGapIn, turretForwardIn, turretLeftIn);
        return new Choice(spot, target, Math.hypot(target.x() - pose.x(), target.y() - pose.y()), null);
    }

    /**
     * The column a robot at {@code xAsRed} (its x in red's frame) is in line with, on the LEFT half
     * ({@code left}) or the RIGHT: that of the half's WALL spot nearest it along the wall. Both rows
     * use the WALL row's columns, so Square and Triangle pressed from one place go to the wall spot
     * and the close spot of the same column. Level between two, the one nearer the HIVE's front.
     */
    static Column columnFor(boolean left, double xAsRed) {
        Column best = Column.HIVE;
        double bestIn = Math.abs(Spot.of(left, Row.WALL, Column.HIVE).red.x() - xAsRed);
        for (Column column : new Column[] {Column.NEAR, Column.FAR}) {
            double d = Math.abs(Spot.of(left, Row.WALL, column).red.x() - xAsRed);
            if (d < bestIn) {
                best = column;
                bestIn = d;
            }
        }
        return best;
    }

    /**
     * How far the robot's centre is past the centre line onto the other alliance's side, inches
     * (red's side is low x): negative on our side.
     */
    static double pastLineIn(Alliance alliance, double x) {
        double c = FieldFrame.FIELD_CENTRE_INCHES;
        return alliance == Alliance.BLUE ? c - x : x - c;
    }

    /**
     * The robot's centre is less than {@code pastCentreIn} past the centre line; with 0 (or NaN, or
     * negative), strictly on {@code alliance}'s side.
     */
    static boolean withinReach(Alliance alliance, double x, double pastCentreIn) {
        return pastLineIn(alliance, x) < reach(pastCentreIn);
    }

    private static double reach(double pastCentreIn) {
        return pastCentreIn > 0.0 ? pastCentreIn : 0.0;
    }

    /**
     * Whether field {@code y} is on our LEFT CELL's half: red's left CELL is at the scoring end
     * (past the axle), blue's at the audience end, since blue's side is red's turned half a turn.
     */
    static boolean leftHalf(Alliance alliance, double y) {
        boolean scoringEnd = y >= HiveFieldPoints.AXLE_Y_IN;
        return alliance == Alliance.BLUE ? !scoringEnd : scoringEnd;
    }

    /**
     * The heading, radians, at which a robot whose centre is at ({@code x}, {@code y}) points its
     * turret's axis (a turret {@code forwardIn} ahead and {@code leftIn} left of the centre, locked
     * straight ahead) at ({@code targetX}, {@code targetY}). The axis moves as the robot turns, so
     * it is solved by turning to the target from the axis a few times; with the turret a few inches
     * off a target tens of inches away it settles at once. Offsets of 0: the centre's bearing.
     */
    static double aimTurret(double x, double y, double targetX, double targetY, double forwardIn, double leftIn) {
        double f = Double.isNaN(forwardIn) ? 0.0 : forwardIn;
        double l = Double.isNaN(leftIn) ? 0.0 : leftIn;
        double h = Math.atan2(targetY - y, targetX - x);
        for (int i = 0; i < 8; i++) {
            double ax = x + f * Math.cos(h) - l * Math.sin(h);
            double ay = y + f * Math.sin(h) + l * Math.cos(h);
            h = Math.atan2(targetY - ay, targetX - ax);
        }
        return h;
    }

    /** {@code p} turned half a turn about the field centre: red's pose to blue's. */
    static Pose turned(Pose p) {
        double c = FieldFrame.FIELD_CENTRE_INCHES;
        return new Pose(2 * c - p.x(), 2 * c - p.y(), p.heading() + Math.PI);
    }
}
