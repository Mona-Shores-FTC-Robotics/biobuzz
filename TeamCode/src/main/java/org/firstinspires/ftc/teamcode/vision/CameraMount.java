package org.firstinspires.ftc.teamcode.vision;

import com.bylazar.configurables.annotations.Configurable;

/**
 * Where the Limelight sits on the robot, and the transform from what it reports
 * into the robot frame.
 *
 * <h2>The two frames</h2>
 *
 * <b>Camera frame</b> — as the Limelight reports {@code targetpose_cameraspace}:
 * {@code +X} right, {@code +Y} down, {@code +Z} out of the lens. (Positions come
 * off the wire in metres; {@link LimelightVisionSubsystem} converts to inches via
 * {@code Position.toUnit} before anything here sees them.)
 *
 * <p><b>Robot frame</b> — ours: {@code +X} forward, {@code +Y} left, {@code +Z}
 * up, origin at the point Pedro's localizer tracks, at floor level. This is the
 * frame a turret controller wants: bearing is {@code atan2(y, x)}, so a positive
 * bearing means "target is to the left, rotate counter-clockwise".
 *
 * <h2>Verify the axis convention before trusting a range</h2>
 *
 * The {@code +X} right / {@code +Y} down / {@code +Z} forward reading above is
 * Limelight's documented convention, but the FTC SDK wrapper passes the three
 * numbers straight through from the camera's JSON without normalising them, so
 * nothing in the SDK proves it. Run {@code VisionTagDump}, put a tag straight
 * ahead, and confirm that the forward distance lands in {@code Z} and that the
 * signs of {@code X}/{@code Y} match a tag held left-of-centre and above centre.
 * If they don't, fix it in {@link #cameraAxesToRobotAxes} — one place, and the
 * unit tests will tell you whether the rest of the chain still holds.
 *
 * <p>The mounting numbers below are placeholders until someone measures the real
 * robot. They are live-tunable in Panels so that can be done on the field.
 */
@Configurable
public class CameraMount {

    /** Forward (+toward the front) from the robot origin to the lens, inches. */
    public static double mountForwardIn = 0.0;

    /** Left (+toward the robot's left) from the robot origin to the lens, inches. */
    public static double mountLeftIn = 0.0;

    /** Height of the lens above the floor, inches. */
    public static double mountUpIn = 0.0;

    /**
     * Camera pitch, degrees, positive = aimed upward.
     *
     * <p>Expect this to be well above zero: the HIVE is overhead and the tags are
     * on the undersides of the cells, so the camera has to look up at them.
     */
    public static double pitchDeg = 0.0;

    /** Camera yaw relative to robot forward, degrees, positive = rotated left (CCW). */
    public static double yawDeg = 0.0;

    private CameraMount() {
        // Configuration holder; the transform methods are static.
    }

    /**
     * Reinterprets a camera-frame vector into robot-style axes (forward, left, up)
     * <em>without</em> applying any mounting rotation or translation.
     *
     * <p>This is the single place the camera's axis convention is encoded. If
     * {@code VisionTagDump} shows the camera using different axes, change it here.
     */
    public static Vec3 cameraAxesToRobotAxes(Vec3 cameraSpace) {
        return new Vec3(
                cameraSpace.z(),   // camera forward -> robot forward
                -cameraSpace.x(),  // camera right   -> robot left is the negation
                -cameraSpace.y()); // camera down    -> robot up is the negation
    }

    /**
     * Full camera-frame to robot-frame transform using the current mounting
     * configuration: reinterpret axes, rotate by pitch then yaw, then translate
     * out to the lens position.
     *
     * @param cameraSpace a point in the camera frame, inches
     * @return the same point in the robot frame, inches
     */
    public static Vec3 toRobotFrame(Vec3 cameraSpace) {
        return toRobotFrame(cameraSpace, pitchDeg, yawDeg, mountForwardIn, mountLeftIn, mountUpIn);
    }

    /**
     * Pure form of {@link #toRobotFrame(Vec3)} taking the mounting explicitly, so
     * it can be unit tested without touching the live tunables.
     */
    public static Vec3 toRobotFrame(
            Vec3 cameraSpace,
            double pitchDeg,
            double yawDeg,
            double mountForwardIn,
            double mountLeftIn,
            double mountUpIn) {

        Vec3 aligned = cameraAxesToRobotAxes(cameraSpace);

        // Pitch: rotate about the robot's +Y (left) axis. A camera pitched up by p
        // sees its own forward axis pointing along (cos p, 0, sin p) in robot axes,
        // and its own up axis along (-sin p, 0, cos p).
        double p = Math.toRadians(pitchDeg);
        double cp = Math.cos(p);
        double sp = Math.sin(p);
        double px = aligned.x() * cp - aligned.z() * sp;
        double py = aligned.y();
        double pz = aligned.x() * sp + aligned.z() * cp;

        // Yaw: rotate about the robot's +Z (up) axis, positive counter-clockwise.
        double w = Math.toRadians(yawDeg);
        double cw = Math.cos(w);
        double sw = Math.sin(w);
        double yx = px * cw - py * sw;
        double yy = px * sw + py * cw;

        return new Vec3(yx + mountForwardIn, yy + mountLeftIn, pz + mountUpIn);
    }
}
