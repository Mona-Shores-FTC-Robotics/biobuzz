package org.firstinspires.ftc.teamcode.opmodes;

import org.firstinspires.ftc.teamcode.subsystems.TurretEncoder;

/**
 * The driver's heading lock (R1, held; mentor, 25 Nov 2026: "rotate to true angle and lock the
 * stick to move that way for speed"), for the cycle between a pickup and a launch spot.
 *
 * <ul>
 *   <li><b>On the press</b> the target is the nearest field cardinal (0, 90, 180 or 270°; every
 *   {@link TeleOpConfig#headingLockStepDeg}) to the heading the field-centric drive uses
 *   ({@code DriveSubsystem.heading()}, 0 = the driver's forward, which PLAY and a corner reset set
 *   to the alliance's, itself a field cardinal, so the cardinals are the field's).</li>
 *   <li><b>While held</b> the drive turns to that heading and holds it: a P turn with a floor
 *   (as the chassis aim's, {@link ChassisAimTurn}, which this robot needs to turn at all), a stop
 *   band, and a D term on the Pinpoint's turn rate. The right stick's turn is ignored; let go of R1
 *   to turn.</li>
 *   <li><b>The left stick snaps</b> to the nearest axis of the frame it drives in (the field's,
 *   field-centric): inside {@link TeleOpConfig#headingLockConeDeg} of an axis the stick points
 *   straight along it, with the stick's whole magnitude, so a stick pushed 15° off still drives dead
 *   straight at full speed. Outside the cone it passes through unchanged.</li>
 *   <li><b>On release</b> the sticks pass through exactly as given, the next loop; the drive's turn
 *   ramp ({@code DriveSubsystem.turnAccelPerSec}) carries the turn from the lock's to the stick's,
 *   so the heading command does not jump.</li>
 * </ul>
 *
 * <p>With no heading to hold ({@code headingOk} false: no Pinpoint, a faulted one, or the
 * robot-centric TeleOp) only the stick snap works, in whatever frame the drive is in. A referenced
 * pose is not needed: the lock holds the driver's frame, as field-centric driving does.
 *
 * <p>Allocates nothing; one {@link #update} per loop. Pure but for the {@link TeleOpConfig}
 * reads, so a test drives it.
 */
public final class HeadingLock {

    private boolean held;
    private boolean holding;
    private boolean pressed;
    private boolean released;
    private double targetRad = Double.NaN;
    private double forward;
    private double strafe;
    private double turn;

    /**
     * One step. {@code forward}, {@code strafe}, {@code turn} are the sticks as the drive takes them
     * (+forward, +strafe left, +turn counter-clockwise); read the result from {@link #forward()},
     * {@link #strafe()} and {@link #turn()}.
     *
     * @param held        R1 is held
     * @param headingOk   a heading to hold is available this loop
     * @param headingRad  the drive's field-centric heading, radians, CCW
     * @param rateRadPerS the turn rate, radians/s, CCW; NaN skips the D term
     */
    public void update(boolean held, boolean headingOk, double headingRad, double rateRadPerS,
                       double forward, double strafe, double turn) {
        pressed = held && !this.held;
        released = !held && this.held;
        this.held = held;
        if (!held) {
            holding = false;
            targetRad = Double.NaN;
            this.forward = forward;
            this.strafe = strafe;
            this.turn = turn;
            return;
        }
        boolean canHold = headingOk && !Double.isNaN(headingRad);
        if (canHold && !holding) {
            // The press, or a heading that came back mid-hold: the nearest cardinal from here.
            targetRad = nearestCardinalRad(headingRad, stepDeg());
        }
        holding = canHold;
        if (!holding) targetRad = Double.NaN;

        double cone = Math.toRadians(coneDeg());
        double angle = Math.atan2(strafe, forward);
        double quarter = Math.PI / 2.0;
        long axis = Math.round(angle / quarter);
        double magnitude = Math.hypot(forward, strafe);
        if (magnitude > 0.0 && Math.abs(angle - axis * quarter) <= cone) {
            switch ((int) Math.floorMod(axis, 4L)) {
                case 0: this.forward = magnitude; this.strafe = 0.0; break;
                case 1: this.forward = 0.0; this.strafe = magnitude; break;
                case 2: this.forward = -magnitude; this.strafe = 0.0; break;
                default: this.forward = 0.0; this.strafe = -magnitude; break;
            }
        } else {
            this.forward = forward;
            this.strafe = strafe;
        }
        this.turn = holding ? holdTurn(errorRad(targetRad, headingRad), rateRadPerS) : turn;
    }

    /** The heading being held, radians, field-centric; NaN while not holding. */
    public double targetRad() {
        return targetRad;
    }

    /** R1 is held and the drive is holding {@link #targetRad()}. */
    public boolean holding() {
        return holding;
    }

    /** R1 is held, holding a heading or only snapping the stick. */
    public boolean held() {
        return held;
    }

    /** R1 went down this loop. */
    public boolean pressed() {
        return pressed;
    }

    /** R1 came up this loop. */
    public boolean released() {
        return released;
    }

    public double forward() {
        return forward;
    }

    public double strafe() {
        return strafe;
    }

    public double turn() {
        return turn;
    }

    /** The multiple of {@code stepDeg} nearest {@code headingRad}, radians, in (-π, π]. */
    static double nearestCardinalRad(double headingRad, double stepDeg) {
        double step = Math.toRadians(stepDeg);
        double snapped = Math.round(headingRad / step) * step;
        return Math.toRadians(TurretEncoder.wrap180(Math.toDegrees(snapped)));
    }

    /** {@code target - heading}, wrapped to [-π, π). */
    static double errorRad(double targetRad, double headingRad) {
        return Math.toRadians(TurretEncoder.wrap180(Math.toDegrees(targetRad - headingRad)));
    }

    /**
     * The turn that holds the heading: P with a floor and a stop band ({@link ChassisAimTurn}),
     * less {@code kD} times the turn rate, never past the max turn.
     */
    static double holdTurn(double errorRad, double rateRadPerS) {
        double max = unit(TeleOpConfig.headingLockMaxTurn);
        double min = Math.min(unit(TeleOpConfig.headingLockMinTurn), max);
        double stop = TeleOpConfig.headingLockStopDeg > 0.0 ? Math.min(TeleOpConfig.headingLockStopDeg, 10.0) : 0.0;
        double kp = TeleOpConfig.headingLockKp > 0.0 ? Math.min(TeleOpConfig.headingLockKp, 10.0) : 0.0;
        double kd = TeleOpConfig.headingLockKd > 0.0 ? Math.min(TeleOpConfig.headingLockKd, 2.0) : 0.0;
        double p = ChassisAimTurn.power(Math.toDegrees(errorRad), kp, min, max, stop);
        double d = Double.isNaN(rateRadPerS) ? 0.0 : kd * rateRadPerS;
        return Math.max(-max, Math.min(max, p - d));
    }

    /**
     * {@link TeleOpConfig#headingLockConeDeg}, guarded: NaN or negative is 0 (no snap), never past
     * 45° (where it would snap every direction).
     */
    static double coneDeg() {
        double v = TeleOpConfig.headingLockConeDeg;
        return v > 0.0 ? Math.min(v, 45.0) : 0.0;
    }

    /**
     * {@link TeleOpConfig#headingLockStepDeg}, guarded to a whole fraction of a turn, from 15° to
     * 180°: 360 / n for the nearest whole n in [2, 24]. NaN or not positive is 90.
     */
    static double stepDeg() {
        double v = TeleOpConfig.headingLockStepDeg;
        if (!(v > 0.0)) return 90.0;
        long n = Math.max(2L, Math.min(24L, Math.round(360.0 / v)));
        return 360.0 / n;
    }

    private static double unit(double v) {
        return v > 0.0 ? Math.min(v, 1.0) : 0.0;
    }
}
