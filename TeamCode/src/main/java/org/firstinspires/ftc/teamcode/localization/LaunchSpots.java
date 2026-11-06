package org.firstinspires.ftc.teamcode.localization;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.FieldFrame;
import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.HiveCellState;
import org.firstinspires.ftc.teamcode.vision.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The eight spots the TeleOp's "go to launch spot" button may drive to, and which one
 * ({@link #choose}): always the one nearest the robot when the button is pressed. Never one chosen
 * from what the camera believes about the HIVE: the field gets crowded, and the driver stays near
 * where they are (mentor, 4 Nov 2026).
 *
 * <p>Each is a team spot ({@code TeamCode/autos/spots.json}, drawn for red), turned half a turn
 * about the field centre for blue:
 * <ul>
 *   <li>The two starts, {@code RIGHT_START} and {@code LEFT_START}, which are also our preload
 *       launch spots: read from {@link StartPositions}, where they are measured. A start is on the
 *       wall, back against it, so the button stops {@code offWallIn} out from it along the start's
 *       facing, and turns to face the raised row of that half's CELL ({@link HiveFieldPoints}).</li>
 *   <li>The four launch spots the Autos fire from, a CATCH in front of each of our CELLs and an
 *       OFFSET off to its side ({@code TeamCode/autos/README.md}), at the heading the Autos launch
 *       at; and a CORNER for each CELL, the long shot (about 55 in) from the corner of our wall. No generated robot-side copy of these exists, so their numbers are here, and
 *       {@code TeamSpotsTest} fails while one differs from its team spot: move a spot in the
 *       Visualizer (Save team spots), then here.</li>
 * </ul>
 *
 * <p>Each CELL is scored only from its own half, and every spot is on its CELL's half: RIGHT_ spots
 * are below the axle line for red (our RIGHT CELL), LEFT_ above, so after the half turn a RIGHT_
 * spot still serves blue's RIGHT CELL.
 */
public final class LaunchSpots {

    private LaunchSpots() {
    }

    /**
     * A spot, drawn for red.
     *
     * <p><b>Adding one</b> is one constant here, once it is a team spot in {@code spots.json} (made
     * in the Visualizer): its x, y and launch heading as drawn, and {@code TeamSpotsTest} checks it
     * against the file. Its half, so its CELL, follows from its y, and {@link #choose} takes it
     * whenever it is the nearest.
     */
    public enum Spot {
        RIGHT_START(StartPositions.RED_AUDIENCE),
        RIGHT_LAUNCH_CATCH(57.5, 18, 91.9),
        RIGHT_LAUNCH_OFFSET(41, 20, 68.7),
        /**
         * The long shot, about 55 in (the speed table's 1635 row): where 20245 fired about 50 shots
         * on 4 Nov 2026, read from that evening's match log.
         */
        RIGHT_LAUNCH_CORNER(30.2, 10.7, 54),
        LEFT_START(StartPositions.RED_FAR),
        LEFT_LAUNCH_CATCH(57.5, 123.5, 273.4),
        LEFT_LAUNCH_OFFSET(41, 121.5, 296.4),
        /**
         * RIGHT_LAUNCH_CORNER's place mirrored across the HIVE's axle line (y 70.75), at the same
         * range from the LEFT CELL. Its heading is not the mirror (306°): the turret sits 1.87 in
         * right of the tracked point, so the robot is not left-right symmetric, and 302.6° is the
         * heading that points the turret axis at the LEFT CELL's raised row, as the other LEFT
         * spots were drawn. Not yet fired from on a robot; next to PARTNER_STAGED_POLLEN.
         */
        LEFT_LAUNCH_CORNER(30.2, 130.8, 302.6);

        /** As drawn, for red: the team spot itself (for a start, on the wall). */
        public final Pose red;
        /** A start on the wall: driven to {@code offWallIn} out from it, aimed at the CELL. */
        public final boolean onWall;
        /** Serves our LEFT CELL (else the RIGHT one): which half of the axle line it is on. */
        public final boolean left;

        Spot(double x, double y, double headingDeg) {
            this(new Pose(x, y, Math.toRadians(headingDeg)), false);
        }

        Spot(StartPosition start) {
            this(start.pose, true);
        }

        Spot(Pose red, boolean onWall) {
            this.red = red;
            this.onWall = onWall;
            this.left = leftHalf(Alliance.RED, red.y());
        }

        /**
         * Where the button drives for {@code alliance}: the spot, or for a start {@code offWallIn}
         * out from it facing the raised row of its half's CELL; worked out for red and turned half a
         * turn about the field centre for blue. A NaN or negative {@code offWallIn} is 0.
         */
        public Pose target(Alliance alliance, double offWallIn) {
            Pose p = red;
            if (onWall) {
                double out = offWallIn > 0.0 ? offWallIn : 0.0;
                double x = red.x() + out * Math.cos(red.heading());
                double y = red.y() + out * Math.sin(red.heading());
                HiveCell cell = left ? HiveCell.leftCell(Alliance.RED) : HiveCell.rightCell(Alliance.RED);
                Vec3 row = HiveFieldPoints.rowCentre(cell, HiveCellState.UP);
                double heading = row == null ? red.heading() : Math.atan2(row.y() - y, row.x() - x);
                p = new Pose(x, y, heading);
            }
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

        /** "RIGHT_LAUNCH_CATCH (nearest, 23 in)", or the refusal. */
        @Override
        public String toString() {
            return refused() ? "refused: " + refusal
                    : String.format(Locale.US, "%s (nearest, %.0f in)", spot, distanceIn);
        }
    }

    /**
     * Which spot to drive to; a pure function of what the robot knows. Refused without a pose, with
     * a pose that is not referenced (field coordinates would mean nothing), with no alliance, or with
     * the robot's centre {@code pastCentreIn} or more past the centre line, on the other alliance's
     * side. Every spot is on our side, so a drive from within that reach only comes home across the
     * line, never out across it. Otherwise the spot whose {@link Spot#target} is nearest the robot.
     *
     * <p>{@code pastCentreIn} 0 (or NaN, or negative) asks for the robot strictly on our side. It was
     * that until 6 Nov 2026: that day's practice refused the press 10 times with the robot 3 to 4 in
     * past the line (19429 and 20245 match logs), where the drivers pick up and wanted it to work.
     */
    public static Choice choose(Pose pose, boolean referenced, Alliance alliance, double offWallIn,
                                double pastCentreIn) {
        if (pose == null) return Choice.refuse("no pose (no Pinpoint)");
        if (!referenced) return Choice.refuse("pose not on the field yet (corner reset or camera reset first)");
        if (alliance != Alliance.RED && alliance != Alliance.BLUE) return Choice.refuse("no alliance");
        if (!withinReach(alliance, pose.x(), pastCentreIn)) {
            return Choice.refuse(String.format(Locale.US,
                    "robot %.0f in past the centre line, more than the %.0f in it may drive back from",
                    pastLineIn(alliance, pose.x()), reach(pastCentreIn)));
        }
        Spot best = null;
        Pose bestTarget = null;
        double bestIn = Double.POSITIVE_INFINITY;
        for (Spot spot : Spot.values()) {
            Pose t = spot.target(alliance, offWallIn);
            double d = Math.hypot(t.x() - pose.x(), t.y() - pose.y());
            if (d < bestIn) {
                best = spot;
                bestTarget = t;
                bestIn = d;
            }
        }
        return new Choice(best, bestTarget, bestIn, null);
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

    /** {@code p} turned half a turn about the field centre: red's pose to blue's. */
    static Pose turned(Pose p) {
        double c = FieldFrame.FIELD_CENTRE_INCHES;
        return new Pose(2 * c - p.x(), 2 * c - p.y(), p.heading() + Math.PI);
    }

    /** The spots whose numbers are written here (not read from a start), for {@code TeamSpotsTest}. */
    static Spot[] writtenHere() {
        List<Spot> spots = new ArrayList<>();
        for (Spot spot : Spot.values()) {
            if (!spot.onWall) spots.add(spot);
        }
        return spots.toArray(new Spot[0]);
    }
}
