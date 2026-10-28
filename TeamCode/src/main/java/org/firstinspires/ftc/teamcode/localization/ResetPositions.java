package org.firstinspires.ftc.teamcode.localization;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.util.Alliance;

/**
 * The marked spot the drive team backs the robot into when the pose is lost: one per alliance, a
 * whole pose. The robot is squared against a wall there, so the heading is known as exactly as the
 * position, and the reset sets all three — which also gives a TeleOp started with no Auto handoff
 * a field heading.
 *
 * <p>A fixed spot, not "the nearest corner": choosing the nearest corner trusts the very pose that
 * is being reset.
 *
 * <p><b>Not measured yet: NaN until it is.</b> To measure: pick a spot on each alliance's side
 * with a wall to square against and a tile seam to line a wheel up with, place the robot there,
 * and read the pose off a robot whose pose is known (a declared start, then driven there, with
 * the camera agreeing). Write the placement in words next to the numbers, as
 * {@link StartPositions} does. Until then the reset button says it has no spot.
 */
public final class ResetPositions {

    private ResetPositions() {
    }

    /** Red: not measured. */
    public static final Pose RED = new Pose(Double.NaN, Double.NaN, Double.NaN);

    /** Blue: not measured. Measure it on the field rather than mirror red: placement is by hand. */
    public static final Pose BLUE = new Pose(Double.NaN, Double.NaN, Double.NaN);

    /** {@code alliance}'s reset spot, or null if it is not measured or the alliance is unknown. */
    public static Pose of(Alliance alliance) {
        Pose pose = alliance == Alliance.RED ? RED : alliance == Alliance.BLUE ? BLUE : null;
        if (pose == null || Double.isNaN(pose.x()) || Double.isNaN(pose.y())
                || Double.isNaN(pose.heading())) {
            return null;
        }
        return pose;
    }
}
