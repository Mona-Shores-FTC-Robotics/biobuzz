package org.firstinspires.ftc.teamcode.opmodes;

import java.util.Locale;

/**
 * How a battery held up under load during one run, and the INIT warning it leaves for the next.
 *
 * <p>Why (22 Nov 2026): the resting voltage at INIT does not tell a tired battery from a good one. On
 * 21 Nov the two R-Solo runs whose four drives took 8.8 to 8.9 s, against 7.1 to 7.9 s for the
 * rest, read 13.28 and 12.41 V at INIT, as good as any; under load their steady voltage
 * ({@code VoltageCompensation.Steady}) averaged 9.78 and 9.40 V, with dips to 5.9 and 6.1 V. Every
 * other Auto that day averaged 10.46 V or more. So the run's own average under load is what
 * {@code RobotOpMode} watches, and it warns at the next INIT while the same battery is still in.
 *
 * <p>Not thread-safe; one per OpMode. No allocation per reading.
 */
public final class BatterySag {

    /** Below this average steady voltage under load a run counts as sagging. See the class comment. */
    public static final double SAG_VOLTS = 10.0;

    /**
     * A battery reading this much higher at INIT than the last run's INIT is taken as a swap, and
     * the last run's warning is dropped: a battery rests back up only a few tenths in a few minutes.
     */
    public static final double SWAPPED_RISE_VOLTS = 0.3;

    /** Readings (one a second) before a run's average counts: a few seconds of driving says little. */
    public static final int MIN_READINGS = 10;

    private double sum;
    private int readings;

    /** One steady reading taken while the robot runs (after PLAY), volts. NaN and non-positive are ignored. */
    public void add(double steadyVolts) {
        if (!(steadyVolts > 0.0) || Double.isInfinite(steadyVolts)) return;
        sum += steadyVolts;
        readings++;
    }

    public int readings() {
        return readings;
    }

    /** The run's average steady voltage, NaN before the first reading. */
    public double meanVolts() {
        return readings == 0 ? Double.NaN : sum / readings;
    }

    /** True once the run has enough readings and its average is under {@link #SAG_VOLTS}. */
    public boolean sagged() {
        return readings >= MIN_READINGS && meanVolts() < SAG_VOLTS;
    }

    /** The match-log event for a run that has just been found sagging. */
    public String event() {
        return String.format(Locale.US, "battery sagging: %.2f V average under load over %d s, below %.1f V",
                meanVolts(), readings, SAG_VOLTS);
    }

    /**
     * The INIT warning for a battery that sagged in the last run and looks still to be in, or null.
     *
     * @param initVoltsNow  this INIT's reading
     * @param lastInitVolts the last run's INIT reading, NaN if none
     * @param lastMeanVolts the last run's average under load, NaN if none was recorded
     */
    public static String initNote(double initVoltsNow, double lastInitVolts, double lastMeanVolts) {
        if (Double.isNaN(initVoltsNow) || Double.isNaN(lastInitVolts) || Double.isNaN(lastMeanVolts)) return null;
        if (lastMeanVolts >= SAG_VOLTS) return null;
        if (initVoltsNow > lastInitVolts + SWAPPED_RISE_VOLTS) return null;
        return String.format(Locale.US,
                "last run averaged %.1f V under load: Autos drive about 1 s slower under %.0f V. Swap it", lastMeanVolts, SAG_VOLTS);
    }
}
