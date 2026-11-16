package org.firstinspires.ftc.teamcode.subsystems;

/**
 * The control law that keeps a continuous servo's turret on an angle that may itself be moving.
 * Pure: given the error and the two rates, the power. Unit tested against a model of the servo.
 *
 * <p>The turret has no stops, so an angle is a direction: the error is always the shortest way
 * round ({@link #shortestErrorDeg}), never more than half a turn.
 *
 * <p>The power is the sum of two parts, then lifted over the servo's floor:
 *
 * <ul>
 *   <li><b>Feedforward</b>: the target's own rate over the turret's top speed. The target is a
 *       chassis-frame angle, so when the robot turns one way the target moves the other way at the
 *       same rate; feeding that rate forward turns the turret against the robot before any error
 *       has built up. A target rate under {@code stillDps} counts as none (gyro noise).</li>
 *   <li><b>Correction</b>: {@code kP} times the error, less {@code kD} times the turret's rate
 *       relative to the target's, so the power eases off as the turret closes on the target and it
 *       slows onto it. The damping only eases the correction; it never turns it round, because a
 *       continuous servo does not brake, it reverses (a reversing brake swung the turret 100° either
 *       side of its target on the bench, 24 Oct 2026). Inside the tolerance the correction is 0.</li>
 *   <li><b>The floor</b>: below {@code minPower} the servo does not turn at all, so any power that
 *       is not zero is sent as {@code minPower} plus the rest of the range in proportion.</li>
 * </ul>
 *
 * <p>This is the 24 Oct 2026 hold law (in git history under this name) with the feedforward and
 * the shortest way round added, and its creep pulses left out: they stepped a servo whose speed
 * did not follow its command onto a still target, and a pulsed turret cannot follow a moving one.
 *
 * <p>Power here is counter-clockwise positive; the caller applies the servo's own sign and its
 * stop trim. Nothing is kept between calls.
 */
public final class TurretServoControl {

    /** The law's tunables. One instance, fields written from Panels each loop; no allocation in the loop. */
    public static final class Gains {
        /** Power per degree of error. */
        public double kP;
        /** Power per degree per second of the turret's rate relative to the target's. 0: no damping. */
        public double kD;
        /** The least power that turns the turret. */
        public double minPower;
        public double maxPower;
        /** Turret degrees per second at power 1.0, for the feedforward. 0: no feedforward. */
        public double topSpeedDps;
        /** The share of the target's rate fed forward: 1 is all of it, 0 none. */
        public double feedforward;
        public double toleranceDeg;
        /** A target rate slower than this is treated as still, deg/s. */
        public double stillDps;

        public Gains(double kP, double kD, double minPower, double maxPower, double topSpeedDps,
                     double feedforward, double toleranceDeg, double stillDps) {
            set(kP, kD, minPower, maxPower, topSpeedDps, feedforward, toleranceDeg, stillDps);
        }

        /** Guarded: NaN, negative and infinite values become the harmless end, never a fast servo. */
        public void set(double kP, double kD, double minPower, double maxPower, double topSpeedDps,
                        double feedforward, double toleranceDeg, double stillDps) {
            this.kP = nonNegative(kP);
            this.kD = nonNegative(kD);
            this.maxPower = unit(maxPower);
            this.minPower = Math.min(unit(minPower), this.maxPower);
            this.topSpeedDps = nonNegative(topSpeedDps);
            this.feedforward = Math.min(nonNegative(feedforward), 2.0);
            this.toleranceDeg = nonNegative(toleranceDeg);
            this.stillDps = nonNegative(stillDps);
        }
    }

    /** Target minus angle the shortest way round, degrees in (-180, 180]. NaN if either is. */
    public static double shortestErrorDeg(double targetDeg, double angleDeg) {
        return TurretEncoder.wrap180(targetDeg - angleDeg);
    }

    /**
     * The power for this loop, counter-clockwise positive, within {@code ±gains.maxPower}.
     *
     * @param errorDeg      target minus angle, the shortest way round, degrees; NaN gives 0
     * @param rateDps       the turret's rate, degrees per second counter-clockwise; NaN counts as 0
     * @param targetRateDps the target's rate, same sign convention; NaN counts as 0
     */
    public static double power(double errorDeg, double rateDps, double targetRateDps, Gains gains) {
        if (Double.isNaN(errorDeg)) {
            return 0.0;
        }
        double targetRate = Double.isNaN(targetRateDps) || Math.abs(targetRateDps) < gains.stillDps
                ? 0.0 : targetRateDps;
        double rate = Double.isNaN(rateDps) ? 0.0 : rateDps;

        double forward = gains.topSpeedDps > 0.0 ? gains.feedforward * targetRate / gains.topSpeedDps : 0.0;

        double correction = 0.0;
        if (Math.abs(errorDeg) > gains.toleranceDeg) {
            correction = gains.kP * errorDeg - gains.kD * (rate - targetRate);
            if (correction != 0.0 && Math.signum(correction) != Math.signum(errorDeg)) {
                correction = 0.0;
            }
        }

        double wanted = forward + correction;
        if (wanted == 0.0) {
            return 0.0;
        }
        double lifted = gains.minPower + (1.0 - gains.minPower) * Math.min(1.0, Math.abs(wanted));
        return Math.copySign(Math.min(lifted, gains.maxPower), wanted);
    }

    private TurretServoControl() {
    }

    private static double nonNegative(double value) {
        return value > 0.0 && !Double.isInfinite(value) ? value : 0.0;
    }

    private static double unit(double value) {
        return value > 0.0 ? Math.min(value, 1.0) : 0.0;
    }
}
