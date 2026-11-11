package org.firstinspires.ftc.teamcode.subsystems;

import java.util.Locale;

/**
 * The turret's absolute angle from two absolute encoders geared to it at different ratios: the
 * "two-encoder" (vernier, Chinese-remainder) trick. Plain Java, so all of it is unit tested at a
 * laptop; the OctoQuad read is the caller's.
 *
 * <h2>The trick</h2>
 *
 * <p>An absolute encoder geared up from the turret (a Cypher Max on a 64T gear against the 176T
 * ring turns 2.75 times per turret turn) knows where it is inside its own turn, but not which of
 * its turns the turret is on. A second absolute encoder on a gear with a different tooth count (44T:
 * 4.0 turns per turret turn) turns a different number of times per turret turn, so the pair of
 * readings drifts apart by a fixed amount for every turn of the first one. For each turn the first
 * encoder could be on, this predicts what the second should read, and keeps the one whose
 * prediction matches. That gives the turret's angle at power-on, with nothing moving and no home
 * mark. EasyCRT (YAMS, Team 6911) documents the same search.
 *
 * <h2>A turret with no stops</h2>
 *
 * <p>The turret has no stops and no wires, so at power-on it may be any number of turns from home.
 * The pair of readings repeats after {@link #periodTurretTurns} turret turns (4 for 64T and 44T on
 * the 176T ring: lcm(64, 44) / 176). Because that is a <b>whole</b> number of turret turns, every
 * repeat points the same way, so the pair always gives the turret's true pointing direction. A pair
 * of ratios whose readings repeat on a fraction of a turret turn could not, and is refused. The
 * search covers the whole period, and the answer is wrapped into (-180, 180] degrees from home.
 *
 * <p>Two numbers say whether a reading can be trusted:
 *
 * <ul>
 *   <li>{@link Solution#residualDeg}: how far the second encoder is from the best prediction, in
 *       its own degrees. Noise, backlash and the encoders' own error (the Cypher Max states
 *       ±1.2°) all land here. Above {@code maxResidualDeg}, the readings disagree: an encoder
 *       slipped, a gear jumped a tooth, or a value below is wrong.</li>
 *   <li>{@link Solution#marginDeg}: how far the runner-up's prediction is from the reading. Below
 *       {@code minMarginDeg} the answer is ambiguous and refused.</li>
 * </ul>
 *
 * <p>{@link #separationDeg} is the design check: the closest any two candidates ever come, in
 * degrees of encoder B (32.7° for 64T and 44T). Half of it is the most error the readings can
 * carry before the wrong candidate wins. Two gears a tooth apart would leave only a few degrees.
 *
 * <h2>Inputs</h2>
 *
 * <p>Each encoder's reading is a fraction of its own turn, [0, 1). Its home reading is that
 * fraction with the turret at its home mark. Its direction is +1 if its reading rises as the turret
 * turns counter-clockwise seen from above (Pedro's frame), -1 if it falls; the Cypher Max's absolute
 * output counts the opposite way to its quadrature, so measure it.
 *
 * <p>Read both encoders at the same instant with the turret still (one OctoQuad read in INIT does
 * both): a turret moving between the two reads looks like a disagreement.
 */
public final class TurretAbsolute {

    private TurretAbsolute() {
    }

    /** The longest period searched for, in turret turns. Real gears repeat far sooner. */
    static final int MAX_PERIOD_TURRET_TURNS = 64;

    /** One solve. {@link #ok()} says whether {@link #turretDeg} can be used. */
    public static final class Solution {
        /** Why there is no angle, or null when {@link #turretDeg} is good. */
        public final String problem;
        /** Turret degrees from home, counter-clockwise positive, in (-180, 180]; NaN with a problem. */
        public final double turretDeg;
        /** Encoder B's distance from the winning prediction, degrees of B; NaN if not solved. */
        public final double residualDeg;
        /** Encoder B's distance from the runner-up's prediction, degrees of B; NaN if none. */
        public final double marginDeg;

        public Solution(String problem, double turretDeg, double residualDeg, double marginDeg) {
            this.problem = problem;
            this.turretDeg = turretDeg;
            this.residualDeg = residualDeg;
            this.marginDeg = marginDeg;
        }

        public boolean ok() {
            return problem == null;
        }
    }

    /**
     * The turret's pointing angle from the two readings.
     *
     * @param fracA          encoder A's reading, a fraction of its turn [0, 1); NaN for no reading
     * @param fracB          encoder B's reading, the same way
     * @param homeA          A's reading with the turret at home, [0, 1); NaN until measured
     * @param homeB          B's reading with the turret at home
     * @param ratioA         A's turns per turret turn, positive (2.75 for 64T on a 176T ring)
     * @param ratioB         B's turns per turret turn, positive and different from A's
     * @param dirA           +1 or -1, see the class comment
     * @param dirB           +1 or -1
     * @param maxResidualDeg readings further apart than this disagree
     * @param minMarginDeg   a runner-up closer than this makes the answer ambiguous
     */
    public static Solution solve(double fracA, double fracB, double homeA, double homeB,
                                 double ratioA, double ratioB, int dirA, int dirB,
                                 double maxResidualDeg, double minMarginDeg) {
        String bad = checkSettings(homeA, homeB, ratioA, ratioB, dirA, dirB);
        if (bad != null) {
            return new Solution(bad, Double.NaN, Double.NaN, Double.NaN);
        }
        if (Double.isNaN(fracA) || Double.isNaN(fracB)) {
            return new Solution("no reading from encoder " + (Double.isNaN(fracA) ? "A" : "B"),
                    Double.NaN, Double.NaN, Double.NaN);
        }
        int period = periodTurretTurns(ratioA, ratioB);
        // A's turns since home, inside one turn; each candidate adds a whole turn of A. Over one
        // period A makes period * ratioA whole turns, and each is one candidate.
        double partA = dirA * wrapHalf(fracA - homeA);
        int candidates = (int) Math.round(period * ratioA);
        double bestErr = Double.POSITIVE_INFINITY;
        double secondErr = Double.POSITIVE_INFINITY;
        double bestTurret = Double.NaN;
        for (int k = 0; k < candidates; k++) {
            double turretTurns = (partA + k) / ratioA;
            double predictedB = homeB + dirB * turretTurns * ratioB;
            double err = Math.abs(wrapHalf(fracB - predictedB));
            if (err < bestErr) {
                secondErr = bestErr;
                bestErr = err;
                bestTurret = turretTurns;
            } else if (err < secondErr) {
                secondErr = err;
            }
        }
        double residualDeg = bestErr * 360.0;
        double marginDeg = Double.isInfinite(secondErr) ? Double.NaN : secondErr * 360.0;
        if (residualDeg > maxResidualDeg) {
            return new Solution(String.format(Locale.US,
                    "encoders disagree by %.1f° (limit %.1f°): turret moving, a gear slipped, or a value is wrong",
                    residualDeg, maxResidualDeg), Double.NaN, residualDeg, marginDeg);
        }
        if (!Double.isNaN(marginDeg) && marginDeg < minMarginDeg) {
            return new Solution(String.format(Locale.US,
                    "ambiguous: the runner-up is %.1f° away (need %.1f°)", marginDeg, minMarginDeg),
                    Double.NaN, residualDeg, marginDeg);
        }
        return new Solution(null, wrap180(bestTurret * 360.0), residualDeg, marginDeg);
    }

    /**
     * After how many turret turns both readings first repeat (the turret turns over which both
     * encoders make whole turns), when that is a whole number of turret turns; 0 when it is not,
     * or is longer than {@link #MAX_PERIOD_TURRET_TURNS}: then two turret angles that point
     * different ways give the same pair, and the pair cannot give a direction.
     */
    public static int periodTurretTurns(double ratioA, double ratioB) {
        int maxTurnsA = (int) Math.ceil(MAX_PERIOD_TURRET_TURNS * ratioA);
        for (int turnsA = 1; turnsA <= maxTurnsA; turnsA++) {
            if (isWhole(turnsA * ratioB / ratioA)) {
                double turret = turnsA / ratioA;
                return isWhole(turret) ? (int) Math.rint(turret) : 0;
            }
        }
        return 0;
    }

    /**
     * The design check: the smallest distance, in degrees of encoder B, between the predictions of
     * any two candidates over one period. Half of it is the error budget. NaN if the ratios have
     * no whole-turn period.
     */
    public static double separationDeg(double ratioA, double ratioB) {
        int period = periodTurretTurns(ratioA, ratioB);
        if (period == 0) {
            return Double.NaN;
        }
        int candidates = (int) Math.round(period * ratioA);
        double closest = Double.POSITIVE_INFINITY;
        for (int dk = 1; dk < candidates; dk++) {
            closest = Math.min(closest, Math.abs(wrapHalf(dk * ratioB / ratioA)) * 360.0);
        }
        return closest;
    }

    /**
     * A pulse width to a fraction of the encoder's turn, [0, 1), from that encoder's own measured
     * narrowest and widest pulse; NaN if the width is outside them by more than a few microseconds
     * (no encoder, or the wrong wire). The Cypher Max's PWM runs at 490 to 603 Hz part to part, so
     * its pulse widths differ from encoder to encoder: each one's range is measured on
     * {@code Bench: OctoQuad}, never assumed.
     */
    public static double fractionFromPulse(int pulseUs, double minUs, double maxUs) {
        if (Double.isNaN(minUs) || Double.isNaN(maxUs) || !(maxUs > minUs)) {
            return Double.NaN;
        }
        double slack = 4.0;
        if (pulseUs < minUs - slack || pulseUs > maxUs + slack) {
            return Double.NaN;
        }
        double f = (pulseUs - minUs) / (maxUs - minUs);
        f = f - Math.floor(f);
        return f >= 1.0 ? 0.0 : f;
    }

    private static String checkSettings(double homeA, double homeB, double ratioA, double ratioB,
                                        int dirA, int dirB) {
        if (!(ratioA > 0) || !(ratioB > 0) || Double.isInfinite(ratioA) || Double.isInfinite(ratioB)) {
            return "gear ratios not set (turnsPerTurretTurn / absRatioB)";
        }
        if (Math.abs(ratioA - ratioB) < 1e-9) {
            return "the two ratios are equal: the trick needs different gears";
        }
        if (periodTurretTurns(ratioA, ratioB) == 0) {
            return "these ratios never repeat on a whole turret turn: no pointing direction";
        }
        if ((dirA != 1 && dirA != -1) || (dirB != 1 && dirB != -1)) {
            return "directions not measured (absDirectionA / absDirectionB)";
        }
        if (Double.isNaN(homeA) || Double.isNaN(homeB)) {
            return "home readings not measured (absHomeA / absHomeB)";
        }
        return null;
    }

    private static boolean isWhole(double x) {
        return Math.abs(x - Math.rint(x)) < 1e-6;
    }

    /** {@code turns} wrapped into [-0.5, 0.5). */
    static double wrapHalf(double turns) {
        return turns - Math.floor(turns + 0.5);
    }

    /** {@code deg} wrapped into (-180, 180]. */
    static double wrap180(double deg) {
        double wrapped = deg % 360.0;
        if (wrapped <= -180.0) {
            wrapped += 360.0;
        } else if (wrapped > 180.0) {
            wrapped -= 360.0;
        }
        return wrapped;
    }
}
