package org.firstinspires.ftc.teamcode.localization;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.FieldFrame;
import org.firstinspires.ftc.teamcode.vision.HiveState;

import java.util.Locale;

/**
 * The four launch spots the Autos fire from, for the TeleOp's "go to launch spot" button, and which
 * one to go to ({@link #choose}).
 *
 * <p>Each is a team spot ({@code TeamCode/autos/spots.json}, drawn for red in the Visualizer), with
 * the heading the Autos launch at: one CATCH in front of each of our CELLs, just outside where its
 * spill lands, and one OFFSET off to the side and angled at it ({@code TeamCode/autos/README.md}).
 * RIGHT_ and LEFT_ are the CELL each serves, as our drive team sees it, so after the half turn for
 * blue a RIGHT_ spot still serves blue's RIGHT CELL. There is no generated robot-side copy of the
 * spots, so the numbers are here, and {@code TeamSpotsTest} fails while one differs from its spot:
 * move a spot in the Visualizer (Save team spots), then here.
 */
public final class LaunchSpots {

    private LaunchSpots() {
    }

    /** A launch spot, drawn for red: x, y inches, heading degrees, Pedro field frame. */
    public enum Spot {
        RIGHT_LAUNCH_CATCH(false, 57.5, 18, 91.9),
        RIGHT_LAUNCH_OFFSET(false, 41, 20, 68.7),
        LEFT_LAUNCH_CATCH(true, 57.5, 123.5, 273.4),
        LEFT_LAUNCH_OFFSET(true, 41, 121.5, 296.4);

        /** Serves our LEFT CELL (else the RIGHT one). */
        public final boolean left;
        /** As drawn, for red. */
        public final Pose red;

        Spot(boolean left, double x, double y, double headingDeg) {
            this.left = left;
            this.red = new Pose(x, y, Math.toRadians(headingDeg));
        }

        /** Where this spot is for {@code alliance}: as drawn for red, turned half a turn about the centre for blue. */
        public Pose pose(Alliance alliance) {
            if (alliance != Alliance.BLUE) return red;
            double c = FieldFrame.FIELD_CENTRE_INCHES;
            return new Pose(2 * c - red.x(), 2 * c - red.y(), red.heading() + Math.PI);
        }
    }

    /** Why a spot was chosen. */
    public enum Why {
        /** Our HIVE's tracker says that half's CELL is up. */
        CELL_UP("CELL up"),
        /** The tracker does not know (UNSEEN, or mid-TIP): the half the robot is on. */
        NEAREST("CELL unknown, nearer half");

        public final String text;

        Why(String text) {
            this.text = text;
        }
    }

    /** What {@link #choose} decided: a spot, or a refusal (then {@link #spot} is null). */
    public static final class Choice {
        public final Spot spot;
        /** The spot for our alliance; null with a refusal. */
        public final Pose target;
        public final Why why;
        /** The HIVE state the choice was made from. */
        public final HiveState hive;
        /** Why nothing is driven, for the Driver Station and the log; null when a spot was chosen. */
        public final String refusal;

        private Choice(Spot spot, Pose target, Why why, HiveState hive, String refusal) {
            this.spot = spot;
            this.target = target;
            this.why = why;
            this.hive = hive;
            this.refusal = refusal;
        }

        static Choice refuse(String why, HiveState hive) {
            return new Choice(null, null, null, hive, why);
        }

        public boolean refused() {
            return refusal != null;
        }

        /** "RIGHT_LAUNCH_CATCH (CELL up, RIGHT_CELL_UP)", or the refusal. */
        @Override
        public String toString() {
            return refused() ? "refused: " + refusal
                    : String.format(Locale.US, "%s (%s, %s)", spot, why.text, hive);
        }
    }

    /**
     * Which launch spot to drive to, and why; a pure function of what the robot knows.
     *
     * <ul>
     *   <li>Refused without a pose, with a pose that is not referenced (field coordinates would mean
     *       nothing), with no alliance, or with the robot on or past the centre line: never drive
     *       across it. Both ends of a straight drive on our side of the line are on our side, so
     *       the drive stays there.</li>
     *   <li>The half: the one whose CELL of our HIVE is up ({@code hive}, our HIVE's tracker: each
     *       CELL can only be scored from its own half); if the tracker does not say, the half the
     *       robot is on, by the HIVE's axle line ({@link HiveFieldPoints#AXLE_Y_IN}).</li>
     *   <li>On that half, the nearer of its CATCH and its OFFSET.</li>
     * </ul>
     */
    public static Choice choose(Pose pose, boolean referenced, Alliance alliance, HiveState hive) {
        if (pose == null) return Choice.refuse("no pose (no Pinpoint)", hive);
        if (!referenced) {
            return Choice.refuse("pose not on the field yet (corner reset or camera reset first)", hive);
        }
        if (alliance != Alliance.RED && alliance != Alliance.BLUE) return Choice.refuse("no alliance", hive);
        if (!onOurSide(alliance, pose.x())) {
            return Choice.refuse("robot on the other alliance's side of the centre line", hive);
        }
        boolean left;
        Why why;
        if (hive == HiveState.LEFT_CELL_UP || hive == HiveState.RIGHT_CELL_UP) {
            left = hive == HiveState.LEFT_CELL_UP;
            why = Why.CELL_UP;
        } else {
            left = leftHalf(alliance, pose.y());
            why = Why.NEAREST;
        }
        Spot best = null;
        double bestIn = Double.POSITIVE_INFINITY;
        for (Spot spot : Spot.values()) {
            if (spot.left != left) continue;
            Pose p = spot.pose(alliance);
            double d = Math.hypot(p.x() - pose.x(), p.y() - pose.y());
            if (d < bestIn) {
                best = spot;
                bestIn = d;
            }
        }
        return new Choice(best, best.pose(alliance), why, hive, null);
    }

    /** The robot's centre is strictly on {@code alliance}'s side of the centre line (red: low x). */
    static boolean onOurSide(Alliance alliance, double x) {
        return alliance == Alliance.BLUE ? x > FieldFrame.FIELD_CENTRE_INCHES : x < FieldFrame.FIELD_CENTRE_INCHES;
    }

    /**
     * Whether field {@code y} is on our LEFT CELL's half: red's left CELL is at the scoring end
     * (past the axle), blue's at the audience end, since blue's side is red's turned half a turn.
     */
    static boolean leftHalf(Alliance alliance, double y) {
        boolean scoringEnd = y >= HiveFieldPoints.AXLE_Y_IN;
        return alliance == Alliance.BLUE ? !scoringEnd : scoringEnd;
    }

    /** The team spot names, for {@code TeamSpotsTest}. */
    static String[] teamSpots() {
        Spot[] spots = Spot.values();
        String[] names = new String[spots.length];
        for (int i = 0; i < spots.length; i++) names[i] = spots[i].name();
        return names;
    }
}
