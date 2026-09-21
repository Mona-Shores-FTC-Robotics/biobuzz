package org.firstinspires.ftc.teamcode.vision;

/**
 * An immutable 3D vector in inches.
 *
 * <p>Deliberately tiny and dependency-free: it holds no frame of its own, so the
 * method that returns one has to say which frame it is in. See {@link CameraMount}
 * for the two frames this package uses.
 */
public final class Vec3 {

    public static final Vec3 ZERO = new Vec3(0, 0, 0);

    private final double x;
    private final double y;
    private final double z;

    public Vec3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double x() { return x; }
    public double y() { return y; }
    public double z() { return z; }

    public Vec3 plus(Vec3 o) { return new Vec3(x + o.x, y + o.y, z + o.z); }
    public Vec3 minus(Vec3 o) { return new Vec3(x - o.x, y - o.y, z - o.z); }
    public Vec3 times(double s) { return new Vec3(x * s, y * s, z * s); }

    /** Euclidean length. */
    public double norm() { return Math.sqrt(x * x + y * y + z * z); }

    /** Length of the projection onto the XY plane — the "ground distance". */
    public double normXY() { return Math.hypot(x, y); }

    @Override
    public String toString() {
        return String.format("(%.2f, %.2f, %.2f)", x, y, z);
    }
}
