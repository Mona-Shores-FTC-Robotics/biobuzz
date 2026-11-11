package org.firstinspires.ftc.teamcode.logging;

/**
 * The simulator set to the Belding Qualifier (7 Nov 2026): what the 17 Auto match logs, the stream video and the
 * official scores showed, in place of the placeholders. Off by default; {@code BIOBUZZ_AUTO_CALIBRATION=belding}
 * (a study) or {@code "calibration": "belding"} (a request) turns it on. README, "Simulating an Auto", has the
 * study that runs R-Solo and L-NoCross with it, and their TIP counts beside Belding's.
 *
 * <p><b>What it sets, and from what:</b>
 * <ul>
 *   <li><b>How many pieces a volley after a FLOWER pick fires</b> ({@link #load}). This is what decided the
 *       second TIP. Counted from the launcher wheels' dips ({@code read_logs.py volleys}), R-Solo's two LEFT
 *       volleys never fired eight pieces between them (0, 3, 4, 6, 6, 6 to 7 and 7), and L-NoCross made its second
 *       TIP in three of the four runs where eight were fired. Infrared's loads came up short on both FLOWERs;
 *       Ultraviolet's were full but for Q-1's wedge.</li>
 *   <li><b>How many launched pieces stay in the CELL, by launch range</b> ({@link #stayInFraction}). A piece
 *       that reaches a raised CELL stays in (mentor, 10 Nov 2026); the ones lost go wide, fall short, or go at a
 *       CELL that is down. 0.95 from the CATCH spots (42.4 in): eight fired made the TIP three times in four.
 *       0.90 from RIGHT_START (53.9 in): the preloads made the first TIP in 7 of 9 runs, and Q-31's put 3 of 4
 *       in. 0.70 from the CLOSE spots (36 in, below every measured row of {@code LaunchSpeedTable}), on thin
 *       evidence: Q-31's volley from RIGHT_LAUNCH_CLOSE put 3 in of the 4 or 5 it fired, and on the Q-13 stream
 *       balls from LEFT_LAUNCH_CLOSE drop in front of the HIVE. Every counted Auto shot was aimed within 0.8
 *       degrees by the pose, fired standing still, with the wheels at or above their target, so the logs do not
 *       say which shots went wide or short.</li>
 *   <li><b>The dwell before a TIP</b> ({@link #DWELL_S}): the partner's TIP was seen 4.0 to 4.4 s after its last
 *       shot left (M-3, M-4; PR #517), and R-Solo's preload TIP came at about 7.5 s (Q-4). Less the flight, the
 *       swing and the camera's settle, and counted from the ball that takes the CELL over (often the third, half a
 *       second before the last), the CELL sat 3.6 to 4.2 s, overloaded or not.</li>
 *   <li><b>Where the HIVE tips</b> ({@link #tipPoint}): Field 1 at about 7.0 POLLEN weights, Field 2 at about
 *       7.95 to 8.0 (PR #519). Odd qualification matches were on Field 1, so odd seeds are Field 1 and even
 *       seeds Field 2. The points are set for launched pieces, which rest nearer the axle than the Guide's
 *       placed ones. Field 1 wandered: it tipped on seven POLLEN twice and held seven once, so two odd seeds in
 *       three use {@link #FIELD_1_TIP_POINT} (seven launched POLLEN tip it, six do not) and the third
 *       {@link #FIELD_1_HOLDS_SEVEN_POINT} (it takes eight). Both are below the Guide's band, as that field was
 *       (it tipped on 6.95). Field 2 uses {@link #FIELD_2_TIP_POINT}: eight tip it, seven do not.</li>
 *   <li><b>How long a volley and a pick take</b>: the shot counter undercounted after every FLOWER pick, so
 *       those feeds ran their whole set time (volleys of 3.6 to 4.0 s) where the simulator's ended at the fourth
 *       shot; only the preload volley ends on the count. A pick stays {@link #PICK_DWELL_S} longer.</li>
 *   <li><b>No knock from a hit</b> ({@link #apply}): every AUTO TIP sat out its dwell.</li>
 * </ul>
 *
 * <p><b>Not set here</b> (still the simulator's own): drive times (the 270 degree curve to the LEFT spots took
 * 3.5 to 4.6 s against 2.9 s) and contact with other robots (Q-27).
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
    static final double[][] STAY_IN = {{36.0, env("BIOBUZZ_BELDING_STAY_CLOSE", 0.70)},
            {42.4, env("BIOBUZZ_BELDING_STAY_CATCH", 0.95)}, {53.9, env("BIOBUZZ_BELDING_STAY_START", 0.90)}};

    /**
     * What-ifs for "what would the robot need": {@code BIOBUZZ_BELDING_STAY_CLOSE/_CATCH/_START} set a row of
     * {@link #STAY_IN} (better aim or a tuned speed), {@code BIOBUZZ_BELDING_WHOLE_FEED=0} ends every volley at
     * its fourth shot (a shot counter that works), {@code BIOBUZZ_BELDING_PICK_DWELL} sets {@link #PICK_DWELL_S},
     * and {@code BIOBUZZ_BELDING_ROBOT} picks whose loads are drawn ({@link #load}).
     */
    static final boolean WHOLE_FEED = !"0".equals(System.getenv("BIOBUZZ_BELDING_WHOLE_FEED"));

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
    static double FIELD_1_TIP_POINT = env("BIOBUZZ_BELDING_F1", -0.45);
    /** Field 1 on the matches it held seven POLLEN: one in three (it tipped on seven twice and held seven once). */
    static double FIELD_1_HOLDS_SEVEN_POINT = env("BIOBUZZ_BELDING_F1_HOLDS", -0.3);
    static double FIELD_2_TIP_POINT = env("BIOBUZZ_BELDING_F2", 0.5);

    private static double env(String name, double otherwise) {
        String v = System.getenv(name);
        return v == null ? otherwise : Double.parseDouble(v);
    }

    /** Where this run's HIVEs tip in the Guide's band: Field 1 on odd seeds, Field 2 on even ones. */
    static double tipPoint(long seed) {
        if (seed % 2 == 0) return FIELD_2_TIP_POINT;
        return (seed / 2) % 3 == 0 ? FIELD_1_HOLDS_SEVEN_POINT : FIELD_1_TIP_POINT;
    }

    /**
     * Seconds a FLOWER pick stays on the FLOWER even once the lane is full: the logged picks took 3.3 to 3.8 s card
     * to card, the simulator's about 2.9 s.
     */
    static final double PICK_DWELL_S = env("BIOBUZZ_BELDING_PICK_DWELL", 0.7);

    /**
     * How many pieces each robot's volley after a FLOWER pick actually fired at Belding, counted from the launcher
     * wheels' dips in the match logs (one sharp dip a ball; within one of the video count on the nine volleys that
     * were also counted on video, never under; capped at the lane's four). Infrared's loads came up short on
     * both FLOWERs; Ultraviolet's were full but for Q-1's wedge.
     */
    static final int[] IR_WALL = {0, 2, 4, 2, 1}, IR_SCORING = {0, 4, 3, 4, 2, 0, 4, 4};
    static final int[] UV_WALL = {4, 4, 4, 4, 4, 4}, UV_SCORING = {1, 4, 4};

    /**
     * {@code BIOBUZZ_BELDING_ROBOT}: whose loads a run draws from: "19429" (Infrared), "20245" (Ultraviolet), "both"
     * (the default: every logged volley) or "full" (every load is four: the pick, the lane and the feed all work).
     */
    static final String ROBOT = System.getenv("BIOBUZZ_BELDING_ROBOT") == null ? "both" : System.getenv("BIOBUZZ_BELDING_ROBOT");

    /** The pieces of a pick that reach the launcher, drawn from {@link #ROBOT}'s logged volleys at that FLOWER. */
    static int load(boolean scoringFlower, java.util.Random luck) {
        int[] ir = scoringFlower ? IR_SCORING : IR_WALL, uv = scoringFlower ? UV_SCORING : UV_WALL;
        switch (ROBOT) {
            case "full": return 4;
            case "19429": return ir[luck.nextInt(ir.length)];
            case "20245": return uv[luck.nextInt(uv.length)];
            default:
                int i = luck.nextInt(ir.length + uv.length);
                return i < ir.length ? ir[i] : uv[i - ir.length];
        }
    }

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
