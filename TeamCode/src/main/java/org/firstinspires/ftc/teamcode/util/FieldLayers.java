package org.firstinspires.ftc.teamcode.util;

import com.bylazar.configurables.annotations.Configurable;

/**
 * Which robots the Panels Field view draws, switched in Panels → FieldLayers: each is drawn over
 * the others, so with all on it can be hard to tell which outline is which.
 *
 * <ul>
 *   <li>{@link #odometry}, in the alliance's colour (red, blue, or white with none): where the
 *       Pinpoint has the robot. In INIT it sits exactly on the declared start, since the pose is
 *       set there.</li>
 *   <li>{@link #declaredStart}, green, INIT only: where the OpMode says the robot starts.</li>
 *   <li>{@link #camera}, orange: where the camera puts the robot (INIT, and Vision: HIVE &amp;
 *       Pose).</li>
 *   <li>{@link #shadow}, purple: the shadow pose, what still fixes would have done; drawn only
 *       once it differs from odometry.</li>
 *   <li>{@link #marks}, grey crosses: the measured spots (starts, calibration spots, reset
 *       corners), each with a tick the way the robot faces there. Park on one and see which
 *       outline is centred on its cross.</li>
 *   <li>{@link #frame}: the 16 × 16 in frame around each drivetrain outline.</li>
 * </ul>
 */
@Configurable
public final class FieldLayers {

    public static boolean odometry = true;
    public static boolean declaredStart = true;
    public static boolean camera = true;
    /** The shadow pose (what still fixes would do), purple; drawn only once it differs from odometry. */
    public static boolean shadow = true;
    /** The measured spots, grey crosses; see the class comment. */
    public static boolean marks = true;
    public static boolean frame = true;

    private FieldLayers() {
    }
}
