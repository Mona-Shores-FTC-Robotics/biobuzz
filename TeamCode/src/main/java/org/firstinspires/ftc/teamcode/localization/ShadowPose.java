package org.firstinspires.ftc.teamcode.localization;

import com.pedropathing.math.Pose;

/**
 * A second pose that rides on the real one and takes the stationary camera fixes the real one is
 * only shown ({@link LocalizationTuning#stillFixes} off): what the pose <i>would</i> be with still
 * fixes on, logged beside the real pose so a practice session judges the fixes before anyone drives
 * on them. Pure arithmetic: the shadow is the real pose plus an offset in field x, y; odometry moves
 * both the same, a still fix sets the offset so the shadow sits on the fix (x, y; heading is the
 * real one), and any hard set of the real pose (a start, a handoff, a reset, a relocalize) clears
 * the offset, since the real pose was just put somewhere on purpose.
 *
 * <p>A snap, not Pedro's blend: a still fix is five agreeing frames with a single frame's variance,
 * which the filter takes almost whole, and a snap is the plainer thing to read in a log. What to
 * judge it by is not the camera (the shadow is fed the camera, so it agrees with it) but places the
 * robot's position is known another way: back on a start mark, squared into a wall, or the other
 * HIVE. 2 Nov 2026.
 */
public final class ShadowPose {

    private double offsetX;
    private double offsetY;
    private int fixesApplied;
    private double lastFixMoveIn = Double.NaN;

    /** The shadow for the real pose {@code real}. Null for null. */
    public Pose of(Pose real) {
        return real == null ? null : new Pose(real.x() + offsetX, real.y() + offsetY, real.heading());
    }

    /** A still fix at field ({@code fixX}, {@code fixY}) with the real pose at {@code real}: the shadow moves onto it. */
    public void applyFix(double fixX, double fixY, Pose real) {
        double nx = fixX - real.x();
        double ny = fixY - real.y();
        lastFixMoveIn = Math.hypot(nx - offsetX, ny - offsetY);
        offsetX = nx;
        offsetY = ny;
        fixesApplied++;
    }

    /** The real pose was set on purpose: the shadow goes with it. */
    public void follow() {
        offsetX = 0;
        offsetY = 0;
    }

    public double offsetX() { return offsetX; }
    public double offsetY() { return offsetY; }

    /** How far the shadow sits from the real pose, inches. */
    public double offsetIn() { return Math.hypot(offsetX, offsetY); }

    public int fixesApplied() { return fixesApplied; }

    /** How far the last fix moved the shadow, inches; NaN before the first. */
    public double lastFixMoveIn() { return lastFixMoveIn; }
}
