package org.firstinspires.ftc.teamcode.logging;

/**
 * The moving parts of AdvantageScope's robot model ({@code cad/advantagescope/Robot_BIOBUZZ}, built
 * from the Onshape CAD by {@code from_onshape.py}) and how a log poses them: one {@code Pose3d}
 * per component, in the robot frame, under {@link #KEY}. The match log writes it from the angles
 * the OpMode reports ({@link MatchLog#component}); the simulator can write the same key.
 *
 * <p>Component {@code c} is drawn in {@code model_c.glb} as the CAD has it, at
 * {@link #DRAWN_AT_DEG}{@code [c]}. Shown at angle {@code a}, it is turned by
 * {@code t = a − drawnAt} about {@link #AXIS_DIRECTION}{@code [c]} through
 * {@link #AXIS_POINT_M}{@code [c]}: translation {@code p − R(t) p}, rotation
 * {@code (cos t/2, sin t/2 · d)}.
 *
 * <p>The numbers are {@code Robot_BIOBUZZ/components.json}'s, copied here because the robot cannot
 * read the repository; {@code RobotComponentsTest} fails while the two differ. When the CAD moves a
 * component, rebuild the model and copy the new numbers.
 */
public final class RobotComponents {

    /** The key AdvantageScope's layouts show as the robot model's components. */
    public static final String KEY = "/Internals/Components";

    /** The turret: {@code TurretSubsystem.angleDegrees()}, counter-clockwise from home (the nose). */
    public static final int TURRET = 0;
    /** The FLOWER stick: {@code FlowerStickSubsystem.commandedDegrees()}, swung out from stowed. */
    public static final int FLOWER_STICK = 1;
    public static final int COUNT = 2;

    static final String[] NAMES = {"turret", "flower stick"};
    /** A point on each component's axis: robot frame, metres. */
    static final double[][] AXIS_POINT_M = {{-0.0844, -0.06791, 0.33035}, {0.15449, 0.13883, 0.18462}};
    /** Each axis's direction, a unit vector; a positive angle turns counter-clockwise looking down it. */
    static final double[][] AXIS_DIRECTION = {{-0.00107, 0.0, 1.0}, {0.00107, 1.0, 0.0}};
    /**
     * The angle each component is drawn at in the CAD: the turret's hood faces 51.5 deg left of the
     * nose, and the stick is drawn stowed.
     */
    static final double[] DRAWN_AT_DEG = {51.5, 0.0};

    private RobotComponents() {}

    /** Component {@code c} at {@code angleDeg}, written as x, y, z, w, i, j, k into {@code out} from {@code 7 * c}. */
    public static void pose(int c, double angleDeg, double[] out) {
        double t = Math.toRadians(angleDeg - DRAWN_AT_DEG[c]);
        double[] p = AXIS_POINT_M[c];
        double[] a = AXIS_DIRECTION[c];
        double norm = Math.sqrt(a[0] * a[0] + a[1] * a[1] + a[2] * a[2]);   // components.json rounds it
        double dx = a[0] / norm, dy = a[1] / norm, dz = a[2] / norm;
        double cos = Math.cos(t), sin = Math.sin(t);
        double dot = dx * p[0] + dy * p[1] + dz * p[2];
        // R p by Rodrigues: p cos t + (d × p) sin t + d (d · p)(1 − cos t)
        double rx = p[0] * cos + (dy * p[2] - dz * p[1]) * sin + dx * dot * (1 - cos);
        double ry = p[1] * cos + (dz * p[0] - dx * p[2]) * sin + dy * dot * (1 - cos);
        double rz = p[2] * cos + (dx * p[1] - dy * p[0]) * sin + dz * dot * (1 - cos);
        double half = Math.sin(t / 2);
        int k = 7 * c;
        out[k] = p[0] - rx;
        out[k + 1] = p[1] - ry;
        out[k + 2] = p[2] - rz;
        out[k + 3] = Math.cos(t / 2);
        out[k + 4] = half * dx;
        out[k + 5] = half * dy;
        out[k + 6] = half * dz;
    }
}
