package org.firstinspires.ftc.teamcode.subsystems;

/**
 * The turret's absolute angle from two absolute encoders geared to it at different ratios: the
 * "two-encoder" (vernier, Chinese-remainder) trick. Plain Java, so all of it is unit tested at a
 * laptop; the OctoQuad read is the caller's.
 *
 * <h2>The trick</h2>
 *
 * <p>An absolute encoder geared up from the turret (19429's Cypher Max on the 64T drive gear turns
 * 2.75 times per turret turn) knows where it is inside its own turn, but not which of its turns the
 * turret is on. A second absolute encoder on a gear with a different tooth count turns a different
 * number of times per turret turn, so the pair of readings drifts apart by a fixed amount for every
 * turn of the first one. For each turn the first encoder could be on, this predicts what the second
 * should read, and keeps the one turn whose prediction matches. That gives the turret's angle at
 * power-on, with nothing moving and no home mark.
 *
 * <p>Two numbers say whether a reading can be trusted:
 *
 * <ul>
 *   <li>{@link Solution#residualDeg}: how far the second encoder is from the best prediction, in
 *       its own degrees. Noise, backlash and the encoders' own error (the Cypher Max states
 *       ±1.2°) all land here. Above {@code maxResidualDeg}, the readings disagree: an encoder
 *       slipped, a gear jumped a tooth, or a value below is wrong.</li>
 *   <li>{@link Solution#marginDeg}: how far the runner-up turn's prediction is from the reading.
 *       The answer is only unambiguous while the runner-up is well clear of the winner; below
 *       {@code minMarginDeg} it is refused.</li>
 * </ul>
 *
 * <p>{@link #separationDeg} is the design check: the closest any two candidate turns ever come for
 * a pair of ratios over a travel. Half of it is the most error the readings can carry before the
 * wrong turn wins. Pick the second gear to make it large, not just "different by one tooth": two
 * gears a tooth apart drift slowly, which gives a long unambiguous range but candidates only a few
 * degrees apart.
 *
 * <h2>Inputs</h2>
 *
 * <p>Each encoder's reading is a fraction of its own turn, [0, 1). Its home reading is that
 * fraction with the turret at its home mark. Its direction is +1 if its reading rises as the turret
 * turns counter-clockwise seen from above (Pedro's frame), -1 if it falls; a gear mesh reverses it,
 * and the Cypher Max's absolute output counts the opposite way to its quadrature, so measure it.
 */
public final class TurretAbsolute {

    private TurretAbsolute() {
    }

    /**
     * Candidates this far past the travel are still considered, so a turret sitting on a stop with
     * a little noise is not pushed onto the wrong turn.
     */
    public static final double TRAVEL_SLACK_DEG = 10.0;

    /** One solve. {@link #ok()} says whether {@link #turretDeg} can be used. */
    public static final class Solution {
        /** Why there is no angle, or null when {@link #turretDeg} is good. */
        public final String problem;
        /** Turret degrees from home, counter-clockwise positive, not wrapped; NaN with a problem. */
        public final double turretDeg;
        /** Encoder B's distance from the winning prediction, degrees of B; NaN if not solved. */
        public final double residualDeg;
        /** Encoder B's distance from the runner-up's prediction, degrees of B; NaN if none. */
        public final double marginDeg;

        Solution(String problem, double turretDeg, double residualDeg, double marginDeg) {
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
     * The turret's angle from the two readings.
     *
     * @param fracA       encoder A's reading, a fraction of its turn [0, 1); NaN for no reading
     * @param fracB       encoder B's reading, the same way
     * @param homeA       A's reading with the turret at home, [0, 1); NaN until measured
     * @param homeB       B's reading with the turret at home
     * @param ratioA      A's turns per turret turn, positive (2.75 for 64T on a 176T ring)
     * @param ratioB      B's turns per turret turn, positive and different from A's
     * @param dirA        +1 or -1, see the class comment
     * @param dirB        +1 or -1
     * @param eachWayDeg  how far the turret can be from home either way, degrees; the search is kept
     *                    inside it, so a smaller travel gives a bigger margin
     * @param maxResidualDeg readings further apart than this disagree
     * @param minMarginDeg   a runner-up closer than this makes the answer ambiguous
     */
    public static Solution solve(double fracA, double fracB, double homeA, double homeB,
                                 double ratioA, double ratioB, int dirA, int dirB,
                                 double eachWayDeg, double maxResidualDeg, double minMarginDeg) {
        String bad = checkSettings(homeA, homeB, ratioA, ratioB, dirA, dirB, eachWayDeg);
        if (bad != null) {
            return new Solution(bad, Double.NaN, Double.NaN, Double.NaN);
        }
        if (Double.isNaN(fracA) || Double.isNaN(fracB)) {
            return new Solution("no reading from encoder " + (Double.isNaN(fracA) ? "A" : "B"),
                    Double.NaN, Double.NaN, Double.NaN);
        }
        // A's turns since home, inside one turn; the candidates add whole turns of A.
        double partA = dirA * wrapHalf(fracA - homeA);
        double windowDeg = eachWayDeg + TRAVEL_SLACK_DEG;
        double eachWayTurnsA = windowDeg / 360.0 * ratioA;
        int kMin = (int) Math.floor(-eachWayTurnsA - partA) - 1;
        int kMax = (int) Math.ceil(eachWayTurnsA - partA) + 1;

        double bestErr = Double.POSITIVE_INFINITY;
        double secondErr = Double.POSITIVE_INFINITY;
        double bestTurret = Double.NaN;
        for (int k = kMin; k <= kMax; k++) {
            double turnsA = partA + k;
            double turretTurns = turnsA / ratioA;
            if (Math.abs(turretTurns * 360.0) > windowDeg) {
                continue;
            }
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
        if (Double.isNaN(bestTurret)) {
            return new Solution("no candidate inside the travel", Double.NaN, Double.NaN, Double.NaN);
        }
        double residualDeg = bestErr * 360.0;
        double marginDeg = Double.isInfinite(secondErr) ? Double.NaN : secondErr * 360.0;
        if (residualDeg > maxResidualDeg) {
            return new Solution(String.format(java.util.Locale.US,
                    "encoders disagree by %.1f° (limit %.1f°): slipped, or a ratio or home is wrong",
                    residualDeg, maxResidualDeg), Double.NaN, residualDeg, marginDeg);
        }
        if (!Double.isNaN(marginDeg) && marginDeg < minMarginDeg) {
            return new Solution(String.format(java.util.Locale.US,
                    "ambiguous: the next turn is %.1f° away (need %.1f°)", marginDeg, minMarginDeg),
                    Double.NaN, residualDeg, marginDeg);
        }
        return new Solution(null, bestTurret * 360.0, residualDeg, marginDeg);
    }

    /**
     * The design check: over a turret travel of {@code eachWayDeg} either way from home, the
     * smallest distance, in degrees of encoder B, between the predictions of any two of A's turns.
     * Half of it is the error budget. Depends only on the ratios and the travel, not on the homes.
     */
    public static double separationDeg(double ratioA, double ratioB, double eachWayDeg) {
        // Two candidates whole A-turns apart (dk) differ in B by dk * ratioB / ratioA turns.
        // Inside the travel, dk can be up to the number of A turns across the whole travel.
        int maxDk = (int) Math.floor(2.0 * (eachWayDeg + TRAVEL_SLACK_DEG) / 360.0 * ratioA);
        double closest = Double.POSITIVE_INFINITY;
        for (int dk = 1; dk <= maxDk; dk++) {
            double d = Math.abs(wrapHalf(dk * ratioB / ratioA)) * 360.0;
            closest = Math.min(closest, d);
        }
        return closest;
    }

    /**
     * How far the turret turns before the pair of readings repeats, in turret turns, for tooth
     * counts: the ring's, and the gear on each encoder. The search never needs to go further.
     */
    public static double unambiguousTurretTurns(int ringTeeth, int gearTeethA, int gearTeethB) {
        if (ringTeeth <= 0 || gearTeethA <= 0 || gearTeethB <= 0) {
            throw new IllegalArgumentException("tooth counts must be positive");
        }
        return lcm(gearTeethA, gearTeethB) / (double) ringTeeth;
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
                                        int dirA, int dirB, double eachWayDeg) {
        if (!(ratioA > 0) || !(ratioB > 0) || Double.isInfinite(ratioA) || Double.isInfinite(ratioB)) {
            return "gear ratios not set (absRatioA / absRatioB)";
        }
        if (Math.abs(ratioA - ratioB) < 1e-9) {
            return "the two ratios are equal: the trick needs different gears";
        }
        if ((dirA != 1 && dirA != -1) || (dirB != 1 && dirB != -1)) {
            return "directions not measured (absDirectionA / absDirectionB)";
        }
        if (Double.isNaN(homeA) || Double.isNaN(homeB)) {
            return "home readings not measured (absHomeA / absHomeB)";
        }
        if (!(eachWayDeg > 0) || Double.isInfinite(eachWayDeg)) {
            return "travel not set (absEachWayDeg)";
        }
        return null;
    }

    /** {@code turns} wrapped into [-0.5, 0.5). */
    static double wrapHalf(double turns) {
        return turns - Math.floor(turns + 0.5);
    }

    private static long lcm(long a, long b) {
        return a / gcd(a, b) * b;
    }

    private static long gcd(long a, long b) {
        while (b != 0) {
            long t = a % b;
            a = b;
            b = t;
        }
        return a;
    }
}
