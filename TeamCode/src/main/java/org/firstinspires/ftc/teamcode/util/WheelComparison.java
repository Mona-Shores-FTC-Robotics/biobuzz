package org.firstinspires.ftc.teamcode.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Compares drive wheels measured under the same command and says which one is off, and why.
 *
 * <p>The judgement half of the {@code Drive Motor Check} OpMode, kept free of SDK types so the
 * thresholds can be unit tested. The OpMode measures; this decides.
 *
 * <h2>What each finding means</h2>
 *
 * <table>
 *   <caption>Findings</caption>
 *   <tr><th>Finding</th><th>Measured</th><th>Usual cause</th></tr>
 *   <tr><td>{@link Finding#NO_ENCODER}</td><td>~0 RPM while commanded</td>
 *       <td>Encoder cable unplugged. Driving is unaffected; the measurement is not</td></tr>
 *   <tr><td>{@link Finding#WRONG_WAY}</td><td>Spins opposite to its command</td>
 *       <td>Motor direction wrong in the robot's Pedro file, or motor wires swapped</td></tr>
 *   <tr><td>{@link Finding#SLOW_DRAG}</td><td>Slow, and drawing clearly more current</td>
 *       <td>Something resists it: bearing, rubbing wheel, bent shaft, tight chain</td></tr>
 *   <tr><td>{@link Finding#SLOW_WEAK}</td><td>Slow, current normal or low</td>
 *       <td>The motor itself: worn brushes, damaged gearbox, or a tired motor</td></tr>
 * </table>
 *
 * <p>A <em>different gear ratio</em> is not in the table on purpose: goBILDA's encoder sits on the
 * motor shaft, before the gearbox, so a wrong-ratio gearbox reads the same RPM as the others while
 * its wheel turns at a different speed. The OpMode's hand-turn check during init catches that
 * instead — one wheel turn reads a different tick count.
 */
public final class WheelComparison {

    private WheelComparison() {}

    public enum Finding {
        OK,
        NO_ENCODER,
        WRONG_WAY,
        SLOW_DRAG,
        SLOW_WEAK
    }

    /** One wheel's averaged measurement over a phase. */
    public static final class Sample {
        public final String name;
        public final double commandedPower;
        public final double ticksPerSec;
        public final double amps;

        public Sample(String name, double commandedPower, double ticksPerSec, double amps) {
            this.name = name;
            this.commandedPower = commandedPower;
            this.ticksPerSec = ticksPerSec;
            this.amps = amps;
        }
    }

    /** One wheel's verdict. {@code percentOfFastest} is NaN for a wheel with no encoder. */
    public static final class Result {
        public final String name;
        public final double rpm;
        public final double percentOfFastest;
        public final double amps;
        public final Finding finding;

        Result(String name, double rpm, double percentOfFastest, double amps, Finding finding) {
            this.name = name;
            this.rpm = rpm;
            this.percentOfFastest = percentOfFastest;
            this.amps = amps;
            this.finding = finding;
        }
    }

    /** Below this |power| a wheel counts as not commanded, and is not judged. */
    static final double COMMANDED_POWER = 0.1;

    /** A commanded wheel under this many RPM (motor shaft) is treated as having no encoder. */
    static final double NO_ENCODER_RPM = 30.0;

    /**
     * Judges wheels that were all given the same job.
     *
     * @param ticksPerRev encoder ticks per motor-shaft revolution (28 for a goBILDA 5202/5203)
     * @param slowPercent how far below the fastest wheel, in percent, counts as slow
     * @param dragCurrentRatio a slow wheel drawing at least this multiple of the other wheels'
     *     median current is dragging; below it, the motor is weak
     */
    public static List<Result> compare(List<Sample> samples, double ticksPerRev,
                                       double slowPercent, double dragCurrentRatio) {
        double[] rpm = new double[samples.size()];
        boolean[] noEncoder = new boolean[samples.size()];
        double fastest = 0.0;
        for (int i = 0; i < samples.size(); i++) {
            Sample s = samples.get(i);
            rpm[i] = Math.abs(s.ticksPerSec) / ticksPerRev * 60.0;
            noEncoder[i] = commanded(s) && rpm[i] < NO_ENCODER_RPM;
            if (commanded(s) && !noEncoder[i]) {
                fastest = Math.max(fastest, rpm[i]);
            }
        }

        List<Result> results = new ArrayList<>();
        for (int i = 0; i < samples.size(); i++) {
            Sample s = samples.get(i);
            double percent = noEncoder[i] || fastest == 0.0 ? Double.NaN : rpm[i] / fastest * 100.0;
            Finding finding;
            if (!commanded(s)) {
                finding = Finding.OK;
            } else if (noEncoder[i]) {
                finding = Finding.NO_ENCODER;
            } else if (Math.signum(s.ticksPerSec) != Math.signum(s.commandedPower)) {
                finding = Finding.WRONG_WAY;
            } else if (percent < 100.0 - slowPercent) {
                double others = medianAmpsExcept(samples, i);
                finding = s.amps >= others * dragCurrentRatio ? Finding.SLOW_DRAG : Finding.SLOW_WEAK;
            } else {
                finding = Finding.OK;
            }
            results.add(new Result(s.name, rpm[i], percent, s.amps, finding));
        }
        return results;
    }

    /** One line per wheel that needs attention, or a single all-clear line. */
    public static List<String> verdict(List<Result> results) {
        List<String> lines = new ArrayList<>();
        for (Result r : results) {
            switch (r.finding) {
                case NO_ENCODER:
                    lines.add(r.name + ": reads ~0 RPM. Encoder cable unplugged? Plug it in and"
                            + " re-run; this wheel was not measured.");
                    break;
                case WRONG_WAY:
                    lines.add(r.name + ": spins OPPOSITE to its command. Check its direction in"
                            + " the robot's pedro/robots file, or its motor wiring.");
                    break;
                case SLOW_DRAG:
                    lines.add(String.format(Locale.US, "%s: %.0f%% of fastest at %.1f A, well above"
                            + " the others. Something is dragging it: bearing, rubbing, bent shaft."
                            + " Hand-spin it with the robot off.", r.name, r.percentOfFastest,
                            r.amps));
                    break;
                case SLOW_WEAK:
                    lines.add(String.format(Locale.US, "%s: %.0f%% of fastest at %.1f A, normal"
                            + " current. The motor or gearbox is weak. Try swapping it.",
                            r.name, r.percentOfFastest, r.amps));
                    break;
                default:
                    break;
            }
        }
        if (lines.isEmpty()) {
            lines.add("All wheels within tolerance of each other.");
        }
        return lines;
    }

    private static boolean commanded(Sample s) {
        return Math.abs(s.commandedPower) >= COMMANDED_POWER;
    }

    private static double medianAmpsExcept(List<Sample> samples, int skip) {
        List<Double> amps = new ArrayList<>();
        for (int i = 0; i < samples.size(); i++) {
            if (i != skip && commanded(samples.get(i))) {
                amps.add(samples.get(i).amps);
            }
        }
        if (amps.isEmpty()) {
            return Double.POSITIVE_INFINITY;
        }
        Collections.sort(amps);
        int n = amps.size();
        return n % 2 == 1 ? amps.get(n / 2) : (amps.get(n / 2 - 1) + amps.get(n / 2)) / 2.0;
    }

    /** Convenience for tests and callers building four samples at once. */
    public static List<Sample> samples(Sample... samples) {
        return Arrays.asList(samples);
    }
}
