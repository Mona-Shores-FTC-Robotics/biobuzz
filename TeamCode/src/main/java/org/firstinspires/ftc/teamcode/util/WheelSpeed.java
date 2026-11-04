package org.firstinspires.ftc.teamcode.util;

/**
 * A wheel's surface speed, inches per second, from its motor's encoder: the one speed that compares
 * across mechanisms. Ticks per second do not: the feeder's 435 rpm motor counts 384.5 ticks per
 * output revolution and the flywheels' 6000 rpm motors 28, so the feeder's ~2200 ticks/s is about
 * 5.7 rev/s and the flywheels' 1595 about 57.
 */
public final class WheelSpeed {

    private WheelSpeed() {
    }

    /**
     * {@code |ticksPerSecond| / ticksPerRev × ratio × π × diameterInches}: encoder ticks per second
     * at the motor's output shaft, that shaft's ticks per revolution, wheel revolutions per output
     * revolution (1 when the wheel is on the shaft), and the wheel's diameter. NaN when any of the
     * last three is NaN, zero, negative or infinite: unmeasured shows as unknown, never a guess. Pure.
     */
    public static double surfaceInchesPerSecond(double ticksPerSecond, double ticksPerRev, double ratio,
                                                double diameterInches) {
        if (!positive(ticksPerRev) || !positive(ratio) || !positive(diameterInches) || Double.isNaN(ticksPerSecond)) {
            return Double.NaN;
        }
        return Math.abs(ticksPerSecond) / ticksPerRev * ratio * Math.PI * diameterInches;
    }

    private static boolean positive(double value) {
        return value > 0.0 && !Double.isInfinite(value);
    }
}
