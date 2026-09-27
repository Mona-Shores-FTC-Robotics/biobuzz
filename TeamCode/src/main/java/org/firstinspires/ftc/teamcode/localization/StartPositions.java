package org.firstinspires.ftc.teamcode.localization;

/**
 * Every legal BIOBUZZ starting position, in one place, so each Autonomous names one rather than
 * typing coordinates.
 *
 * <p><b>Empty until measured.</b> The legal start tiles come from the game manual; each pose comes
 * from placing the robot on the tile the way the drive team will, then reading the pose — not from
 * a drawing. Add one like this:
 *
 * <pre>{@code
 * public static final StartPosition RED_AUDIENCE =
 *         new StartPosition("Red audience", Alliance.RED, new Pose(x, y, Math.toRadians(h)));
 * }</pre>
 *
 * <p>Each also needs its driver forward heading in {@code FieldFrame}, measured the same day.
 */
public final class StartPositions {

    private StartPositions() {
    }
}
