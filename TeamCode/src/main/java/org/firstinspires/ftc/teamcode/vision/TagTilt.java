package org.firstinspires.ftc.teamcode.vision;

/**
 * Which way a tag's face points, from the three angles the Limelight reports, for each way those
 * angles could be meant to combine. {@code Vision: Tag Tilt} shows all of them side by side so the
 * robot can tell us which one is real. Pure math.
 *
 * <h2>What is known</h2>
 *
 * Limelight sends a tag's pose in camera space as {@code [x, y, z, rx, ry, rz]} (metres, degrees),
 * camera {@code +X} right, {@code +Y} down, {@code +Z} out of the lens. The SDK copies it into a
 * {@code Pose3D} as {@code new YawPitchRollAngles(DEGREES, a[5], a[4], a[3])} (Hardware 12.0.0,
 * {@code LLResult.createPose3DRobot}). So, against the names Limelight's docs use:
 * <ul>
 *   <li>{@code getRoll()} is {@code rx}, a turn about camera X, which Limelight calls "pitch";</li>
 *   <li>{@code getPitch()} is {@code ry}, about camera Y, which Limelight calls "yaw";</li>
 *   <li>{@code getYaw()} is {@code rz}, about camera Z, which Limelight calls "roll".</li>
 * </ul>
 *
 * <h2>What is not</h2>
 *
 * The order the three turns are applied in. Each {@link Order} is one candidate: {@code XYZ} means
 * the rotation matrix is {@code Rx(rx) · Ry(ry) · Rz(rz)}. For a tag seen nearly head-on the
 * candidates agree; they part company as the other angles grow, which is how the robot tells them
 * apart.
 */
public final class TagTilt {

    /** The six ways three turns about X, Y and Z can be multiplied together. */
    public enum Order { XYZ, XZY, YXZ, YZX, ZXY, ZYX }

    private TagTilt() {
    }

    /**
     * The tag's face direction in camera space under {@code order}: the tag's own Z axis, flipped if
     * need be so it points back toward the camera (the side of the tag the camera can see). That
     * makes the answer independent of which way Limelight points the tag's Z axis.
     *
     * @param positionCamera where the tag is, in camera space (any unit)
     */
    public static Vec3 faceCamera(double rxDeg, double ryDeg, double rzDeg, Order order,
                                  Vec3 positionCamera) {
        String axes = order.name();
        double[] angles = {rxDeg, ryDeg, rzDeg};
        // R = A · B · C applied to e_z: rotate by C first, then B, then A.
        double[] v = {0, 0, 1};
        for (int i = 2; i >= 0; i--) {
            char axis = axes.charAt(i);
            v = rotate(axis, angles[axis - 'X'], v);
        }
        Vec3 normal = new Vec3(v[0], v[1], v[2]);
        double towardTag = normal.x() * positionCamera.x() + normal.y() * positionCamera.y()
                + normal.z() * positionCamera.z();
        return towardTag > 0 ? normal.times(-1) : normal;
    }

    /** {@link #faceCamera} turned into robot axes by {@link CameraMount}'s rotation (no offset). */
    public static Vec3 faceRobot(double rxDeg, double ryDeg, double rzDeg, Order order,
                                 Vec3 positionCamera) {
        Vec3 face = faceCamera(rxDeg, ryDeg, rzDeg, order, positionCamera);
        return CameraMount.toRobotFrame(face).minus(CameraMount.toRobotFrame(Vec3.ZERO));
    }

    /**
     * How far a direction points above horizontal, degrees; negative points down. The underside of
     * a CELL faces down, so its tags read negative, and a TIP changes this one-for-one with the
     * rocker's angle when the tags' faces are square to the axle.
     */
    public static double elevationDeg(Vec3 direction) {
        double norm = direction.norm();
        if (norm == 0) return Double.NaN;
        return Math.toDegrees(Math.asin(Math.max(-1.0, Math.min(1.0, direction.z() / norm))));
    }

    /**
     * The rocker's angle implied by a tag row's height, degrees: {@code +tilt} at the UP height,
     * {@code -tilt} at the DOWN height, 0 level. The row swings on an arc about the axle, so its
     * height above the midpoint is {@code r · sin(angle)}, and the two resting heights fix
     * {@code r}. NaN if either resting height is unmeasured; clamped to the resting angles beyond
     * them.
     *
     * <p>Approximate: it ignores how far the row sits above or below the axle, which moves the
     * height a little mid-swing. Good to a few degrees, which is enough to say which way a CELL is
     * going and how far it has got.
     */
    public static double rockerAngleFromHeightDeg(double rowHeightIn, double upHeightIn,
                                                  double downHeightIn, double restingTiltDeg) {
        if (Double.isNaN(rowHeightIn) || Double.isNaN(upHeightIn) || Double.isNaN(downHeightIn)
                || upHeightIn <= downHeightIn) {
            return Double.NaN;
        }
        double mid = (upHeightIn + downHeightIn) / 2.0;
        double half = (upHeightIn - downHeightIn) / 2.0;
        double sinAngle = (rowHeightIn - mid) / half * Math.sin(Math.toRadians(restingTiltDeg));
        double limit = Math.sin(Math.toRadians(restingTiltDeg));
        sinAngle = Math.max(-limit, Math.min(limit, sinAngle));
        return Math.toDegrees(Math.asin(sinAngle));
    }

    private static double[] rotate(char axis, double deg, double[] v) {
        double r = Math.toRadians(deg);
        double c = Math.cos(r);
        double s = Math.sin(r);
        switch (axis) {
            case 'X': return new double[] {v[0], c * v[1] - s * v[2], s * v[1] + c * v[2]};
            case 'Y': return new double[] {c * v[0] + s * v[2], v[1], -s * v[0] + c * v[2]};
            default:  return new double[] {c * v[0] - s * v[1], s * v[0] + c * v[1], v[2]};
        }
    }
}
