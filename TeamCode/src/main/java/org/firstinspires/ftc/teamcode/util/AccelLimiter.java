package org.firstinspires.ftc.teamcode.util;

/**
 * Limits how fast a value can <em>grow</em> away from zero, but lets it shrink instantly.
 *
 * <p>Used on the driver's stick inputs so a light robot does not lurch when a stick is slammed
 * forward. Only speeding up is limited:
 * <ul>
 *   <li>Stick released, or pulled back part-way: the output follows immediately. A robot that
 *       keeps going after the driver lets go is worse than one that starts too quickly.</li>
 *   <li>Stick reversed: the output drops to zero at once, then ramps up in the new direction.
 *       Braking is instant for the same reason.</li>
 * </ul>
 *
 * <p>Rejected: a symmetric slew-rate limiter, which is the usual choice. It smooths braking too,
 * so the robot coasts after the stick is released. That is exactly wrong under a driver.
 *
 * <p>Plain Java with no SDK types, so it is unit tested.
 */
public final class AccelLimiter {

    private double current;

    /**
     * Moves toward {@code target} and returns the limited value.
     *
     * @param target        where the input wants to be
     * @param maxRisePerSec largest allowed growth in magnitude, per second
     * @param dtSec         seconds since the last call
     */
    public double step(double target, double maxRisePerSec, double dtSec) {
        boolean reversing = current != 0.0 && Math.signum(target) != Math.signum(current);
        if (reversing) {
            current = 0.0;
        }

        if (Math.abs(target) <= Math.abs(current)) {
            current = target;
        } else {
            double maxStep = Math.max(0.0, maxRisePerSec) * Math.max(0.0, dtSec);
            current += Math.signum(target) * Math.min(maxStep, Math.abs(target) - Math.abs(current));
        }
        return current;
    }

    /** Jumps straight to {@code value}, e.g. when the limit is bypassed for turbo. */
    public void reset(double value) {
        current = value;
    }

    public double value() {
        return current;
    }
}
