package org.firstinspires.ftc.teamcode.subsystems;

/**
 * The turret's firing arc: the directions, either side of the robot's real forward, where a ball
 * leaves clear of the robot. Behind that the ball would go back over or into the robot (mentor, 18
 * Nov 2026), so the turret never holds a direction there ({@link #hold}) and a fire button never
 * feeds while the turret or the aim is there ({@link #allowsFeed}). The limits are
 * {@link TurretSubsystem#arcLeftDeg} and {@link TurretSubsystem#arcRightDeg}, degrees either side
 * of the arc's centre, counter-clockwise (left) positive.
 *
 * <p>The centre is the operator's aim trim (mentor, 18 Nov 2026), in the turret's encoder frame
 * ({@link TurretSubsystem#angleDegrees()}): LOCKED holds straight ahead at the trim, so the trim is
 * where the robot's real forward reads, and it corrects a turret zeroed off its mark. A turret
 * zeroed 60° off and trimmed +60° fires from encoder -30° to +150°, not from -90° to +90°. With no
 * trim the centre is 0, where the shots go straight ahead. An Auto has no trim.
 *
 * <p>Pure, so it is tested without a robot. An angle is a direction: every one is taken from the
 * centre and wrapped into (-180, 180] first, and since the arc is less than a whole turn, the way
 * from one direction inside it to another never crosses the back ({@link #errorDeg}).
 */
public final class TurretArc {

    /** Two edges closer than this are the same edge: the centre shift costs a rounding. */
    private static final double SAME_EDGE_DEG = 1e-6;

    private TurretArc() {}

    /** The left limit, degrees, guarded: NaN (no arc) stays NaN, anything else is kept inside [0, 180]. */
    public static double left() {
        return guard(TurretSubsystem.arcLeftDeg);
    }

    /** The right limit, degrees, a positive number, guarded as {@link #left()}. */
    public static double right() {
        return guard(TurretSubsystem.arcRightDeg);
    }

    /** Whether an arc is set: both limits are numbers, and they leave some of the back closed. */
    public static boolean active() {
        return active(left(), right());
    }

    /** Whether {@code deg} is a direction the turret may fire in, about {@code centreDeg}. True with no arc; false for NaN with one. */
    public static boolean contains(double deg, double centreDeg) {
        return contains(deg, centreDeg, left(), right());
    }

    /**
     * Where the turret holds for an aim at {@code deg}: {@code deg} itself inside the arc about
     * {@code centreDeg}; outside, an edge. {@code lastEdge} is the edge it held the loop before
     * (NaN if it was inside): it keeps that edge while the target stays behind, so a target
     * straight behind does not flick the turret from one edge to the other across the back.
     * Entering the dead zone it takes the nearer edge.
     */
    public static double hold(double deg, double lastEdge, double centreDeg) {
        return hold(deg, lastEdge, centreDeg, left(), right());
    }

    /**
     * Target minus angle for the turret's loop. With an arc, the way that stays inside it: both are
     * taken from the centre, wrapped and subtracted, so the turret never turns through the robot's
     * real back (it may be more than 180° the long way round the front). With none, the shortest
     * way round, as before.
     */
    public static double errorDeg(double targetDeg, double angleDeg, double centreDeg) {
        return errorDeg(targetDeg, angleDeg, centreDeg, left(), right());
    }

    /**
     * Whether a fire button may feed: the turret points inside the arc about {@code centreDeg}
     * (when it reads an angle), and so does the aim when there is one ({@code aimDeg} NaN: no
     * target, the turret's own direction decides).
     */
    public static boolean allowsFeed(double turretDeg, double aimDeg, double centreDeg) {
        return allowsFeed(turretDeg, aimDeg, centreDeg, left(), right());
    }

    // ------------------------------------------------------------ the same, with the limits passed in

    static boolean active(double left, double right) {
        return !Double.isNaN(left) && !Double.isNaN(right) && left + right < 360.0;
    }

    /** {@code deg} from the centre, wrapped; a NaN centre counts as 0. */
    private static double rel(double deg, double centre) {
        return TurretEncoder.wrap180(deg - (Double.isNaN(centre) ? 0.0 : centre));
    }

    static boolean contains(double deg, double left, double right) {
        return contains(deg, 0.0, left, right);
    }

    static boolean contains(double deg, double centre, double left, double right) {
        if (!active(left, right)) return true;
        if (Double.isNaN(deg)) return false;
        double w = rel(deg, centre);
        return w <= left && w >= -right;
    }

    static double hold(double deg, double lastEdge, double left, double right) {
        return hold(deg, lastEdge, 0.0, left, right);
    }

    static double hold(double deg, double lastEdge, double centre, double left, double right) {
        if (Double.isNaN(deg) || contains(deg, centre, left, right)) return deg;
        double c = Double.isNaN(centre) ? 0.0 : centre;
        double leftEdge = TurretEncoder.wrap180(c + left), rightEdge = TurretEncoder.wrap180(c - right);
        if (!Double.isNaN(lastEdge)) {
            if (Math.abs(TurretEncoder.wrap180(lastEdge - leftEdge)) < SAME_EDGE_DEG) return leftEdge;
            if (Math.abs(TurretEncoder.wrap180(lastEdge - rightEdge)) < SAME_EDGE_DEG) return rightEdge;
        }
        return rel(deg, c) > 0 ? leftEdge : rightEdge;
    }

    static double errorDeg(double targetDeg, double angleDeg, double left, double right) {
        return errorDeg(targetDeg, angleDeg, 0.0, left, right);
    }

    static double errorDeg(double targetDeg, double angleDeg, double centre, double left, double right) {
        if (!active(left, right)) return TurretServoControl.shortestErrorDeg(targetDeg, angleDeg);
        return rel(targetDeg, centre) - rel(angleDeg, centre);
    }

    static boolean allowsFeed(double turretDeg, double aimDeg, double left, double right) {
        return allowsFeed(turretDeg, aimDeg, 0.0, left, right);
    }

    static boolean allowsFeed(double turretDeg, double aimDeg, double centre, double left, double right) {
        if (!active(left, right)) return true;
        // A turret with no reading (no encoder) is not held back for it: the aim still decides.
        if (!Double.isNaN(turretDeg) && !contains(turretDeg, centre, left, right)) return false;
        return Double.isNaN(aimDeg) || contains(aimDeg, centre, left, right);
    }

    private static double guard(double deg) {
        if (Double.isNaN(deg)) return Double.NaN;
        return Math.max(0.0, Math.min(180.0, Math.abs(deg)));
    }
}
