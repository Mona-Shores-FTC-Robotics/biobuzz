package org.firstinspires.ftc.teamcode.subsystems;

/**
 * The turret's firing arc: the directions, either side of forward, where a ball leaves clear of the
 * robot. Behind that the ball would go back over or into the robot (mentor, 18 Nov 2026), so the
 * turret never holds a direction there ({@link #hold}) and a fire button never feeds while the aim
 * is there ({@link #allowsFeed}). The limits are {@link TurretSubsystem#arcLeftDeg} and
 * {@link TurretSubsystem#arcRightDeg}, degrees from the turret's 0 ({@link TurretSubsystem#angleDegrees()},
 * where the shots go straight ahead), counter-clockwise (left) positive.
 *
 * <p>Pure, so it is tested without a robot. An angle is a direction: every one is wrapped into
 * (-180, 180] first, and since the arc is about 0 and less than a whole turn, the way from one
 * direction inside it to another never crosses the back ({@link #errorDeg}).
 */
public final class TurretArc {

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

    /** Whether {@code deg} is a direction the turret may fire in. True with no arc; false for NaN with one. */
    public static boolean contains(double deg) {
        return contains(deg, left(), right());
    }

    /**
     * Where the turret holds for an aim at {@code deg}: {@code deg} itself inside the arc; outside,
     * an edge. {@code lastEdge} is the edge it held the loop before (NaN if it was inside): it keeps
     * that edge while the target stays behind, so a target straight behind does not flick the turret
     * from one edge to the other across the back. Entering the dead zone it takes the nearer edge.
     */
    public static double hold(double deg, double lastEdge) {
        return hold(deg, lastEdge, left(), right());
    }

    /**
     * Target minus angle for the turret's loop. With an arc, the way that stays inside it: both are
     * wrapped and subtracted, so the turret never turns through the back (it may be more than 180°
     * the long way round the front). With none, the shortest way round, as before.
     */
    public static double errorDeg(double targetDeg, double angleDeg) {
        return errorDeg(targetDeg, angleDeg, left(), right());
    }

    /**
     * Whether a fire button may feed: the turret points inside the arc (when it reads an angle), and
     * so does the aim when there is one ({@code aimDeg} NaN: no target, the turret's own direction
     * decides).
     */
    public static boolean allowsFeed(double turretDeg, double aimDeg) {
        return allowsFeed(turretDeg, aimDeg, left(), right());
    }

    // ------------------------------------------------------------ the same, with the limits passed in

    static boolean active(double left, double right) {
        return !Double.isNaN(left) && !Double.isNaN(right) && left + right < 360.0;
    }

    static boolean contains(double deg, double left, double right) {
        if (!active(left, right)) return true;
        if (Double.isNaN(deg)) return false;
        double w = TurretEncoder.wrap180(deg);
        return w <= left && w >= -right;
    }

    static double hold(double deg, double lastEdge, double left, double right) {
        if (Double.isNaN(deg) || contains(deg, left, right)) return deg;
        if (lastEdge == left || lastEdge == -right) return lastEdge;
        return TurretEncoder.wrap180(deg) > 0 ? left : -right;
    }

    static double errorDeg(double targetDeg, double angleDeg, double left, double right) {
        if (!active(left, right)) return TurretServoControl.shortestErrorDeg(targetDeg, angleDeg);
        return TurretEncoder.wrap180(targetDeg) - TurretEncoder.wrap180(angleDeg);
    }

    static boolean allowsFeed(double turretDeg, double aimDeg, double left, double right) {
        if (!active(left, right)) return true;
        // A turret with no reading (no encoder) is not held back for it: the aim still decides.
        if (!Double.isNaN(turretDeg) && !contains(turretDeg, left, right)) return false;
        return Double.isNaN(aimDeg) || contains(aimDeg, left, right);
    }

    private static double guard(double deg) {
        if (Double.isNaN(deg)) return Double.NaN;
        return Math.max(0.0, Math.min(180.0, Math.abs(deg)));
    }
}
