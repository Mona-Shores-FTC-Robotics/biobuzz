package org.firstinspires.ftc.teamcode.logging;

/**
 * The simulator set to the Belding Qualifier (7 Nov 2026): what the 17 Auto match logs, the stream video and the
 * official scores showed, in place of the placeholders. Off by default; {@code BIOBUZZ_AUTO_CALIBRATION=belding}
 * (a study) or {@code "calibration": "belding"} (a request) turns it on. README, "Simulating an Auto", has the
 * study that runs R-Solo and L-NoCross with it, and their TIP counts beside Belding's.
 *
 * <p><b>What it sets, and from what:</b>
 * <ul>
 *   <li><b>How many launched pieces stay in the CELL, by launch range</b> ({@link #stayInFraction}). The physics
 *       launcher (one pitch, a forgiving CELL) scored about the same from every spot; the matches did not.
 *       RIGHT_START, 53.9 in at 1600 ticks/s: 0.82. The preloads made the first TIP in 7 of 9 R-Solo runs, and
 *       three in is usually enough; Q-31's put 3 of 4 in, and the 5 Nov video's fourth ball fell off the front
 *       lip. LEFT_LAUNCH_CATCH, 42 in at 1521-1528: 0.92. L-NoCross put 8 of 8 in often enough for a second TIP
 *       in 3 of 5 runs, and 0.92 gives 8 of 8 half the time. The CLOSE
 *       spots, 36 in at an extrapolated 1459-1480: 0.65. R-Solo's two LEFT volleys from there made no second TIP
 *       in 7 runs, which bounds it below about 0.8; on the Q-13 stream balls drop back out of the CELL; and Q-31's
 *       volley from RIGHT_LAUNCH_CLOSE put 3 in. 0.65 is the middle of what those allow, an estimate until the
 *       robot fires counted volleys from there ({@code doc/robot-todo.md} section 7d).</li>
 *   <li><b>The dwell before a TIP</b> ({@link #DWELL_S}): the partner's TIP was seen 4.0 to 4.4 s after its last
 *       shot left (M-3, M-4; PR #517), and R-Solo's preload TIP came at about 7.5 s (Q-4). Less the flight, the
 *       swing and the camera's settle, and counted from the ball that takes the CELL over (often the third, half a
 *       second before the last), the CELL sat 3.6 to 4.2 s, overloaded or not.</li>
 *   <li><b>Where the HIVE tips</b> ({@link #tipPoint}): Field 1 at about 7.0 POLLEN weights, Field 2 at about
 *       7.95 to 8.0 (PR #519). Odd qualification matches were on Field 1, so odd seeds are Field 1 and even
 *       seeds Field 2. The points are set for launched pieces, which rest nearer the axle than the Guide's
 *       placed ones: {@link #FIELD_1_TIP_POINT}, where seven launched POLLEN tip it about two times in three
 *       (it tipped on seven twice and held seven once), and {@link #FIELD_2_TIP_POINT}, where eight tip it
 *       nearly every time. Field 1's is below the Guide's band, as that field was (it tipped on 6.95).</li>
 *   <li><b>How long a volley and a pick take</b>: the shot counter undercounted after every FLOWER pick, so
 *       those feeds ran their whole set time (volleys of 3.6 to 4.0 s) where the simulator's ended at the fourth
 *       shot; only the preload volley ends on the count. A pick stays {@link #PICK_DWELL_S} longer.</li>
 *   <li><b>No knock from a hit</b> ({@link #apply}): every AUTO TIP sat out its dwell.</li>
 * </ul>
 *
 * <p><b>Not set here</b> (still the simulator's own): drive times (the 270 degree curve to the LEFT spots took
 * 3.5 to 4.6 s against 2.9 s), the two SCORING FLOWER picks that came back short (Q-1's wedge, Q-4's stop short),
 * and contact with other robots (Q-27).
 */
final class BeldingCalibration {
    private BeldingCalibration() {}

    /** On for this JVM's runs; a request or a test may set it. */
    static boolean on = "belding".equals(System.getenv("BIOBUZZ_AUTO_CALIBRATION"));

    /**
     * {@code BIOBUZZ_AUTO_BELDING_GUARD=1}: the endgame guard as the Belding build ran it (master at 7ed351f: a volley
     * before the park), for replaying R-Solo and L-NoCross as they ran there. Off, the guard is master's
     * ({@code BuiltAuto.parkAfterVolley}), which is what a new Auto will run with.
     */
    static boolean guardAsAtBelding = "1".equals(System.getenv("BIOBUZZ_AUTO_BELDING_GUARD"));

    /** Launch range from the turret axis, inches, and the fraction of launched pieces that stay in the CELL. */
    static final double[][] STAY_IN = {{36.0, 0.65}, {42.4, 0.92}, {53.9, 0.82}};

    /** Seconds a CELL over its tipping weight sits before the rocker moves: {shortest, longest}. */
    static final double[] DWELL_S = {3.6, 4.2};

    /** The fraction that stays in from {@code rangeIn}: straight lines between the rows, held beyond them. */
    static double stayInFraction(double rangeIn) {
        if (Double.isNaN(rangeIn) || rangeIn <= STAY_IN[0][0]) return STAY_IN[0][1];
        for (int i = 1; i < STAY_IN.length; i++) {
            if (rangeIn <= STAY_IN[i][0]) {
                double f = (rangeIn - STAY_IN[i - 1][0]) / (STAY_IN[i][0] - STAY_IN[i - 1][0]);
                return STAY_IN[i - 1][1] + f * (STAY_IN[i][1] - STAY_IN[i - 1][1]);
            }
        }
        return STAY_IN[STAY_IN.length - 1][1];
    }

    /**
     * Field 1's and Field 2's tip points in the Guide's band, for launched pieces (see the class comment).
     * {@code BIOBUZZ_BELDING_F1} and {@code BIOBUZZ_BELDING_F2} override them, for refitting.
     */
    static double FIELD_1_TIP_POINT = env("BIOBUZZ_BELDING_F1", -0.3);
    static double FIELD_2_TIP_POINT = env("BIOBUZZ_BELDING_F2", 0.5);

    private static double env(String name, double otherwise) {
        String v = System.getenv(name);
        return v == null ? otherwise : Double.parseDouble(v);
    }

    /** Where this run's HIVEs tip in the Guide's band: Field 1 on odd seeds, Field 2 on even ones. */
    static double tipPoint(long seed) {
        return seed % 2 != 0 ? FIELD_1_TIP_POINT : FIELD_2_TIP_POINT;
    }

    /**
     * Seconds a FLOWER pick stays on the FLOWER even once the lane is full: the logged picks took 3.3 to 3.8 s card
     * to card, the simulator's about 2.9 s.
     */
    static final double PICK_DWELL_S = 0.7;

    /** What {@link #apply} replaced, for {@link #restore}. */
    private static double[] dwellBefore;
    private static double impulseBefore = Double.NaN;

    /**
     * Sets the simulator's statics for a calibrated run (call only when {@link #on}): the dwell, overloaded or not,
     * and no knock from a hit. At Belding every AUTO TIP sat out its dwell, however hard the volley landed; the
     * placeholder knock tipped the CELL the moment the last ball hit, 0.7 s after it left.
     */
    static void apply() {
        dwellBefore = FieldSim.tipDwellRange;
        impulseBefore = FieldSim.impulseScale;
        FieldSim.tipDwellRange = DWELL_S;
        FieldSim.tipDwellOverloadedS = DWELL_S[0];
        FieldSim.impulseScale = 0;
    }

    /** Puts back what {@link #apply} replaced; harmless when it never ran. */
    static void restore() {
        if (Double.isNaN(impulseBefore)) return;
        FieldSim.tipDwellRange = dwellBefore;
        FieldSim.tipDwellOverloadedS = FieldSim.FILMED_TIP_DWELL_OVERLOADED_S;
        FieldSim.impulseScale = impulseBefore;
        impulseBefore = Double.NaN;
    }

    /** A shot that does not stay in leaves this much slower than the range needs: it drops in front of the CELL. */
    static final double MISS_SPEED_FACTOR = 0.85;
    /** A shot that stays in keeps this much of the placeholder spread, so it still lands in different places. */
    static final double HIT_SPREAD = 0.0;
}
