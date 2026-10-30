package org.firstinspires.ftc.teamcode.localization;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.FieldFrame;

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
 * <p><b>Measured on 20245 (30 Oct 2026):</b> squared into either red corner, the robot's centre (the
 * middle of the wheelbase, the point the Pinpoint's offsets are measured from; the nose does not
 * count) is {@link #WALL_TO_CENTRE_IN} from each of the two walls: the robot is about 16 in square
 * without its nose. Blue's corners are red's turned half a turn about the field centre, as the whole
 * field is (manual Figure 9-2), not measured on their own. A spot set to NaN makes its button say it
 * has no spot.
 */
public final class ResetPositions {

    private ResetPositions() {
    }

    /** Which corner, as the alliance's driver sees it from behind their wall. */
    public enum Corner { LEFT, RIGHT }

    /**
     * From each wall the robot is squared against to its centre, inches. Measured by tape on 20245,
     * 30 Oct 2026: about 8.1 to both walls, in both red corners.
     */
    static final double WALL_TO_CENTRE_IN = 8.1;

    private static final double FAR = FieldFrame.FIELD_SIZE_INCHES - WALL_TO_CENTRE_IN;

    /**
     * Red left: tile A6, against the red-side wall and the back wall (away from the audience), next
     * to red's LOADING ZONE in A5; robot squared into both walls with the intake toward the blue
     * side.
     */
    public static final Pose RED_LEFT = new Pose(WALL_TO_CENTRE_IN, FAR, 0.0);

    /**
     * Red right: tile A1, against the red-side wall and the audience wall, the GARDEN corner; robot
     * squared into both walls with the intake toward the blue side.
     */
    public static final Pose RED_RIGHT = new Pose(WALL_TO_CENTRE_IN, WALL_TO_CENTRE_IN, 0.0);

    /**
     * Blue left: tile F1, against the blue-side wall and the audience wall, next to blue's LOADING
     * ZONE in F2; robot squared into both walls with the intake toward the red side. Red's left
     * turned half a turn.
     */
    public static final Pose BLUE_LEFT = halfTurn(RED_LEFT);

    /**
     * Blue right: tile F6, against the blue-side wall and the back wall, the GARDEN corner. Red's
     * right turned half a turn.
     */
    public static final Pose BLUE_RIGHT = halfTurn(RED_RIGHT);

    /** {@code red} turned half a turn about the field centre: blue's matching spot. */
    private static Pose halfTurn(Pose red) {
        return new Pose(2 * FieldFrame.FIELD_CENTRE_INCHES - red.x(),
                2 * FieldFrame.FIELD_CENTRE_INCHES - red.y(), red.heading() + Math.PI);
    }

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
