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

    /**
     * Draw and send the Field view at most this often, ms. Each send is about 7–8 ms of the loop
     * (20245's logs, 3–4 Nov 2026: Panels builds and serialises the canvas on the loop thread), and
     * Panels on its own sends every 100 ms, so it was the p95 of {@code /Loop/PageMs}. 250, four
     * a second, still shows the robot moving to someone watching; raise it in Panels for a
     * smoother view on the bench. NaN, zero, negative or less than Panels' own 100: Panels' rate.
     */
    public static double drawEveryMs = 250;

    private FieldLayers() {
    }
}
