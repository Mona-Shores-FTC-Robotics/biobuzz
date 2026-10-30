package org.firstinspires.ftc.teamcode.localization;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.util.Alliance;

/**
 * The marked spots the drive team backs the robot into when the pose is lost: two per alliance, the
 * corners on that alliance's wall to the driver's left and right, each a whole pose. The robot is
 * squared into both walls there, so the heading is known as exactly as the position, and the reset
 * sets all three, which also gives a TeleOp started with no Auto handoff a field heading.
 *
 * <p>The driver says which corner by the button held (D-pad left or right), never "the nearest
 * corner": choosing the nearest corner trusts the very pose that is being reset. The right corner
 * is the GARDEN's: its POLLEN may be in the way, so the left (LOADING ZONE) corner is the one to
 * reach for first (29 Oct 2026); the right one was added on 30 Oct for when the robot is there.
 *
 * <p><b>Not measured yet: NaN until it is.</b> To measure a corner: start from a known pose (the
 * declared start, the camera agreeing), drive into the corner, square the robot into both walls
 * with the intake toward the other alliance, and read x, y and heading off the Pose line. Write the
 * placement in words next to the numbers, as {@link StartPositions} does. Until then that button
 * says it has no spot.
 */
public final class ResetPositions {

    private ResetPositions() {
    }

    /** Which corner, as the alliance's driver sees it from behind their wall. */
    public enum Corner { LEFT, RIGHT }

    /**
     * Red left: tile A6, against the red-side wall and the back wall (away from the audience), next
     * to red's LOADING ZONE in A5; robot squared into both walls with the intake toward the blue
     * side. Not measured.
     */
    public static final Pose RED_LEFT = new Pose(Double.NaN, Double.NaN, Double.NaN);

    /**
     * Red right: tile A1, against the red-side wall and the audience wall, the GARDEN corner; robot
     * squared into both walls with the intake toward the blue side. Not measured.
     */
    public static final Pose RED_RIGHT = new Pose(Double.NaN, Double.NaN, Double.NaN);

    /**
     * Blue left: tile F1, against the blue-side wall and the audience wall, next to blue's LOADING
     * ZONE in F2; robot squared into both walls with the intake toward the red side: red's left
     * turned half a turn about the field centre, as the whole field is (manual Figure 9-2). Not
     * measured. Measure it on the field rather than derive it from red: placement is by hand.
     */
    public static final Pose BLUE_LEFT = new Pose(Double.NaN, Double.NaN, Double.NaN);

    /**
     * Blue right: tile F6, against the blue-side wall and the back wall, the GARDEN corner; red's
     * right turned half a turn. Not measured; measure it on the field, as {@link #BLUE_LEFT}.
     */
    public static final Pose BLUE_RIGHT = new Pose(Double.NaN, Double.NaN, Double.NaN);

    /** The raw pose for a corner, NaN while unmeasured; null for an unknown alliance. */
    static Pose raw(Alliance alliance, Corner corner) {
        if (alliance == Alliance.RED) return corner == Corner.LEFT ? RED_LEFT : RED_RIGHT;
        if (alliance == Alliance.BLUE) return corner == Corner.LEFT ? BLUE_LEFT : BLUE_RIGHT;
        return null;
    }

    /** {@code alliance}'s spot in {@code corner}, or null if it is not measured or the alliance is unknown. */
    public static Pose of(Alliance alliance, Corner corner) {
        Pose pose = raw(alliance, corner);
        if (pose == null || Double.isNaN(pose.x()) || Double.isNaN(pose.y())
                || Double.isNaN(pose.heading())) {
            return null;
        }
        return pose;
    }
}
