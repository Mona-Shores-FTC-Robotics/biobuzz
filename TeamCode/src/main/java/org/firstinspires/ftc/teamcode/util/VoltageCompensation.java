package org.firstinspires.ftc.teamcode.util;

/**
 * Scaling an open-loop number by the battery, so a motor does the same thing on a fresh battery
 * as on a tired one: {@code value × nominal / battery}. A DC motor's speed at a given power is
 * roughly proportional to the voltage behind it, so this holds the speed where it was at
 * {@code nominal} volts. Used for the launcher's velocity feedforward ({@code LauncherSubsystem})
 * and the feeder's power ({@code FeederSubsystem}).
 *
 * <p>The battery reading comes from {@code RobotOpMode}, once a second (a voltage read is a hub
 * round trip, so nothing here reads one). Every bad input means "no compensation", never a wild
 * number: a NaN, zero or negative battery or nominal, or a switch that is off, all give a scale of
 * exactly 1. A reading far from the nominal (a brownout, a sensor glitch) is limited to
 * {@link #MIN_SCALE}..{@link #MAX_SCALE}.
 */
public final class VoltageCompensation {

    /** The smallest scale applied: a battery reading at twice the nominal does not halve a command further. */
    public static final double MIN_SCALE = 0.5;

    /** The largest scale applied: 12 V nominal over 8 V, already a brownout. */
    public static final double MAX_SCALE = 1.5;

    private VoltageCompensation() {
    }

    /**
     * {@code nominal / battery}, limited to {@link #MIN_SCALE}..{@link #MAX_SCALE}; exactly 1 when
     * {@code on} is false or either voltage is NaN, infinite, zero or negative. Pure.
     */
    public static double scale(boolean on, double nominalVolts, double batteryVolts) {
        if (!on || !valid(nominalVolts) || !valid(batteryVolts)) return 1.0;
        return Math.max(MIN_SCALE, Math.min(MAX_SCALE, nominalVolts / batteryVolts));
    }

    /**
     * A power in -1..1 scaled by {@code scale} and kept in -1..1, sign kept:
     * {@code min(1, |power| × scale)}. A NaN power or scale gives 0 for the power and the power
     * unchanged for the scale. Pure.
     */
    public static double power(double power, double scale) {
        if (Double.isNaN(power)) return 0.0;
        if (!(scale > 0.0) || Double.isInfinite(scale)) return power;
        double magnitude = Math.min(1.0, Math.abs(power) * scale);
        return power < 0.0 ? -magnitude : magnitude;
    }

    /**
     * Whether {@code next} differs from {@code applied} by more than {@code step} as a fraction of
     * {@code applied}: a scale that moved by a hair (a tenth of a volt of battery noise) is not
     * worth a hub round trip. Always true when {@code applied} is NaN (nothing applied yet), and
     * when {@code next} is exactly 1 and {@code applied} is not (compensation switched off). Pure.
     */
    public static boolean worthApplying(double applied, double next, double step) {
        if (Double.isNaN(applied)) return true;
        if (next == applied) return false;
        if (next == 1.0) return true;
        return Math.abs(next - applied) > Math.abs(step * applied);
    }

    private static boolean valid(double volts) {
        return volts > 0.0 && !Double.isInfinite(volts);
    }
}
