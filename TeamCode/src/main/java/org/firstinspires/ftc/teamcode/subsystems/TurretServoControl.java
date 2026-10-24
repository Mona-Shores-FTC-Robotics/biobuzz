package org.firstinspires.ftc.teamcode.subsystems;

/**
 * The control law that holds a continuous servo on an angle, as tuned on the bench (24 Oct 2026,
 * {@code Turret Bench}). Pure: given the error and the clock, the power. Unit tested.
 *
 * <p>A continuous servo is not proportional: below some power it does not move at all, above it
 * it moves fast, and at "zero" it may still creep. So:
 *
 * <ul>
 *   <li><b>Inside the tolerance</b>: 0.</li>
 *   <li><b>Driving</b>, error beyond {@code creepBelowDeg}: {@code minPower + kP * error}, the
 *       floor the servo needs to move at all plus a gain on top, signed toward the target.</li>
 *   <li><b>Creeping</b>, the last degrees: pulses of {@code minPower}, {@code creepOnMs} on then
 *       {@code creepOffMs} off, so each step is a fraction of a degree and it lands in the band
 *       instead of jumping over it. The servo reads its signal every 20 ms, so a pulse is timed
 *       in milliseconds, not loops.</li>
 * </ul>
 *
 * <p>Power here is counter-clockwise positive; the caller applies the servo's own sign and its
 * centre offset. The instance keeps only the creep clock.
 */
public final class TurretServoControl {

    /** The hold's tunables. One instance, fields written from Panels each loop; no allocation in the loop. */
    public static final class Gains {
        public double kP;
        public double minPower;
        public double maxPower;
        public double creepBelowDeg;
        public double toleranceDeg;
        public double creepOnMs;
        public double creepOffMs;

        public Gains(double kP, double minPower, double maxPower, double creepBelowDeg,
                     double toleranceDeg, double creepOnMs, double creepOffMs) {
            set(kP, minPower, maxPower, creepBelowDeg, toleranceDeg, creepOnMs, creepOffMs);
        }

        /** Guarded: NaN, negative and infinite values become the harmless end, never a fast servo. */
        public void set(double kP, double minPower, double maxPower, double creepBelowDeg,
                        double toleranceDeg, double creepOnMs, double creepOffMs) {
            this.kP = nonNegative(kP);
            this.minPower = unit(minPower);
            this.maxPower = unit(maxPower);
            this.creepBelowDeg = nonNegative(creepBelowDeg);
            this.toleranceDeg = nonNegative(toleranceDeg);
            this.creepOnMs = nonNegative(creepOnMs);
            this.creepOffMs = nonNegative(creepOffMs);
        }
    }

    private long creepCycleStartNs;

    /**
     * The power for this loop, counter-clockwise positive, within {@code ±gains.maxPower}.
     *
     * @param errorDeg target minus angle, degrees; NaN gives 0
     * @param nowNs    {@code System.nanoTime()}, for the creep clock
     */
    public double power(double errorDeg, Gains gains, long nowNs) {
        if (Double.isNaN(errorDeg)) {
            return 0.0;
        }
        double magnitude = Math.abs(errorDeg);
        if (magnitude <= gains.toleranceDeg) {
            return 0.0;
        }
        double power;
        if (magnitude < gains.creepBelowDeg) {
            power = creepPulseOn(gains, nowNs) ? gains.minPower : 0.0;
        } else {
            power = gains.minPower + gains.kP * magnitude;
        }
        power = Math.min(power, gains.maxPower);
        return Math.copySign(power, errorDeg);
    }

    private boolean creepPulseOn(Gains gains, long nowNs) {
        if (gains.creepOffMs <= 0.0) {
            return true;
        }
        double cycleMs = gains.creepOnMs + gains.creepOffMs;
        double intoCycle = (nowNs - creepCycleStartNs) / 1e6;
        if (intoCycle < 0 || intoCycle >= cycleMs) {
            creepCycleStartNs = nowNs;
            intoCycle = 0;
        }
        return intoCycle < gains.creepOnMs;
    }

    private static double nonNegative(double value) {
        return value > 0.0 && !Double.isInfinite(value) ? value : 0.0;
    }

    private static double unit(double value) {
        return value > 0.0 ? Math.min(value, 1.0) : 0.0;
    }
}
