package org.firstinspires.ftc.teamcode.logging;

/**
 * The simulator set to the Belding Qualifier (7 Nov 2026): what the 17 Auto match logs, the stream video and the
 * official scores showed, in place of the placeholders. Off by default; {@code BIOBUZZ_AUTO_CALIBRATION=belding}
 * (a study) or {@code "calibration": "belding"} (a request) turns it on. README, "Simulating an Auto", has the
 * study that runs R-Solo and L-NoCross with it, and their TIP counts beside Belding's.
 *
 * <p>It is also the engine of every event's calibration: {@link FruitportCalibration} ({@code "fruitport"}) turns
 * this on and puts the Fruitport Qualifier's rows, loads and times in place of Belding's ({@link #STAY_IN}, the load
 * tables, {@link #PICK_DWELL_S}, {@link #wholeFeedChance}, {@link #rollerFillsLane}), keeping every number Fruitport
 * has no evidence for.
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
 *   <li><b>The dwell before a TIP</b>: no longer set here. The simulator's own ({@link FieldSim#tipDwellRange}) is
 *       Belding's films since 22 Nov 2026, at the CELL's weight and well past it, counted from the last ball, so every
 *       run has it. A request's {@code "tipDwellS"} still replaces it ({@link #dwellOverride}).</li>
 *   <li><b>Where the HIVE tips</b> ({@link #tipPoint}): Field 1 at about 7.0 POLLEN weights, Field 2 at about
 *       7.95 to 8.0 (PR #519). Odd qualification matches were on Field 1, so odd seeds are Field 1 and even
 *       seeds Field 2. The points are set for launched pieces, which rest nearer the axle than the Guide's
 *       placed ones. Field 1 wandered: it tipped on seven POLLEN twice and held seven once, so two odd seeds in
 *       three use {@link #FIELD_1_TIP_POINT} (seven launched POLLEN tip it, six do not) and the third
 *       {@link #FIELD_1_HOLDS_SEVEN_POINT} (it takes eight). Both are below the Guide's band, as that field was
 *       (it tipped on 6.95). Field 2 uses {@link #FIELD_2_TIP_POINT}: eight tip it, seven do not.</li>
 *   <li><b>How long a volley and a pick take</b>: the shot counter undercounted after every FLOWER pick, so
 *       those feeds ran their whole set time (volleys of 3.6 to 4.0 s) where the simulator's ended at the fourth
 *       shot; only the preload volley ends on the count. Off by default since 19 Nov 2026, when 20245's logs showed the
 *       counter working ({@link #WHOLE_FEED}, {@code BIOBUZZ_BELDING_WHOLE_FEED=1} for Belding's feeds). A pick stays
 *       {@link #PICK_DWELL_S} longer.</li>
 *   <li><b>No knock from a hit</b> ({@link #apply}): every AUTO TIP sat out its dwell.</li>
 * </ul>
 *
 * <p><b>Not set here</b> (still the simulator's own): drive times (the 270 degree curve to the LEFT spots took
 * 3.5 to 4.6 s against 2.9 s) and contact with other robots (Q-27).
 */
final class BeldingCalibration {
    private BeldingCalibration() {}

    /** On for this JVM's runs; a request or a test may set it. {@code fruitport} turns it on too ({@link FruitportCalibration}). */
    static boolean on = "belding".equals(System.getenv("BIOBUZZ_AUTO_CALIBRATION"))
            || "fruitport".equals(System.getenv("BIOBUZZ_AUTO_CALIBRATION"));


    /**
     * Launch range from the turret axis, inches, and the fraction of launched pieces that stay in the CELL. Belding's
     * rows; another event's calibration puts its own here ({@link FruitportCalibration#use}).
     */
    static double[][] STAY_IN = {{36.0, env("BIOBUZZ_BELDING_STAY_CLOSE", 0.70)},
            {42.4, env("BIOBUZZ_BELDING_STAY_CATCH", 0.95)}, {53.9, env("BIOBUZZ_BELDING_STAY_START", 0.90)}};

    /**
     * What-ifs for "what would the robot need": {@code BIOBUZZ_BELDING_STAY_CLOSE/_CATCH/_START} set a row of
     * {@link #STAY_IN} (better aim or a tuned speed), {@code BIOBUZZ_BELDING_WHOLE_FEED=1} runs every
     * volley after a robot's first for its whole set feed (Belding's shot counter, which undercounted; off by default
     * since 19 Nov 2026: 20245's logs of 18 Nov ended 17 of 19 LaunchAlls at their fourth counted shot, fed 1.7 to 2.7 s), {@code BIOBUZZ_BELDING_PICK_DWELL} sets {@link #PICK_DWELL_S},
     * and {@code BIOBUZZ_BELDING_ROBOT} picks whose loads are drawn ({@link #load}).
     */
    static final boolean WHOLE_FEED = "1".equals(System.getenv("BIOBUZZ_BELDING_WHOLE_FEED"));

    /**
     * The best case, a request's {@code "scenario": "best"} (mentor, 19 Nov 2026: "We should be assuming 4 pickups
     * from FLOWERs and GARDENs ... the best-case scenario should not be worrying about it"): every robot's every FLOWER
     * pick and GARDEN gather is four POLLEN ({@link #robotFor} "full", topped up with POLLEN only), every shot stays in
     * (in-rates 1, unless the request sets them), and every volley ends at its fourth counted shot (never
     * {@link #WHOLE_FEED}: 20245's logs of 18 Nov 2026 ended 17 of 19 LaunchAlls there). The HIVEs, the TIP dwell and
     * the fields stay the calibration's. A failure is then one named case at a time: a fail, a short pick, a dead
     * partner, a slow swing.
     */
    static boolean bestCase;

    /** Whether a LaunchAll after a robot's first runs its whole set feed: Belding's undercounting shot counter, not in the best case. */
    static boolean wholeFeed() {
        return WHOLE_FEED && !bestCase;
    }

    /**
     * The chance that one LaunchAll after a robot's first runs its whole set feed although four balls went: the shot
     * counter missed one. 0 for Belding (all or nothing there, {@link #WHOLE_FEED}); an event that measured the
     * share sets it ({@link FruitportCalibration#WHOLE_FEED_CHANCE}).
     */
    static double wholeFeedChance = 0;

    /**
     * Whether a LaunchAll holding fewer than four pieces, or none, runs its whole set feed
     * ({@code LaunchCards.launchAllFeedS}) rather than ending at its last ball, which is the simulator's own
     * shortcut. Off for Belding, as its runs were fitted; on where an event's logs show it
     * ({@link FruitportCalibration}).
     */
    static boolean shortVolleysRunTheFeed = false;

    /** {@link #wholeFeed()} for one volley: always with {@link #WHOLE_FEED}, else by {@link #wholeFeedChance}; never in the best case. */
    static boolean wholeFeed(java.util.Random luck) {
        if (bestCase) return false;
        return WHOLE_FEED || (wholeFeedChance > 0 && luck.nextDouble() < wholeFeedChance);
    }

    /**
     * A request's in-rates ({@code "ourInRate"}, {@code "partnerInRate"}): the fraction of a robot's launched pieces
     * that stay in, from any range, in place of {@link #STAY_IN}'s Belding rows. NaN keeps the rows. Ours is the run's
     * first Auto (index 0), the partner every other robot. Why (mentor, 19 Nov 2026): our robots now put 90 to 95% in
     * from the AUTO launch spots; 0.92 is the realistic case.
     */
    static double ourInRate = Double.NaN, partnerInRate = Double.NaN;

    /** The fraction of the robot at {@code index} that stays in from {@code rangeIn}: its in-rate if set, else the rows. */
    static double stayInFraction(double rangeIn, int index) {
        double rate = index == 0 ? ourInRate : partnerInRate;
        return Double.isNaN(rate) ? stayInFraction(rangeIn) : rate;
    }

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
    static double PICK_DWELL_S = env("BIOBUZZ_BELDING_PICK_DWELL", 0.7);

    /**
     * How many pieces each robot's volley after a FLOWER pick actually fired at Belding, counted from the launcher
     * wheels' dips in the match logs (one sharp dip a ball; within one of the video count on the nine volleys that
     * were also counted on video, never under; capped at the lane's four). Infrared's loads came up short on
     * both FLOWERs; Ultraviolet's were full but for Q-1's wedge.
     */
    static int[] IR_WALL = {0, 2, 4, 2, 1}, IR_SCORING = {0, 4, 3, 4, 2, 0, 4, 4};
    static int[] UV_WALL = {4, 4, 4, 4, 4, 4}, UV_SCORING = {1, 4, 4};

    /**
     * {@code BIOBUZZ_BELDING_ROBOT}: whose loads a run draws from: "19429" (Infrared), "20245" (Ultraviolet), "both"
     * (the default: every logged volley) or "full" (every load is four: the pick, the gather, the lane and the feed all work).
     * {@code BIOBUZZ_BELDING_ROBOT_OURS} and {@code BIOBUZZ_BELDING_ROBOT_PARTNER} set it for one robot of a pair:
     * ours is the run's first Auto (a request's {@code spec}), the partner every other robot; unset, each falls back to
     * {@code BIOBUZZ_BELDING_ROBOT}. So "which of our robots should be R" is two runs of the pair with the two swapped.
     */
    static final String ROBOT = System.getenv("BIOBUZZ_BELDING_ROBOT") == null ? "both" : System.getenv("BIOBUZZ_BELDING_ROBOT");
    static final String OURS = System.getenv("BIOBUZZ_BELDING_ROBOT_OURS");
    static final String PARTNER = System.getenv("BIOBUZZ_BELDING_ROBOT_PARTNER");

    /** Whose loads the robot at {@code index} in the run draws from: index 0 is ours ({@link #robotFor(int, String, String, String)}). */
    static String robotFor(int index) {
        if (bestCase) return "full";
        return robotFor(index, OURS, PARTNER, ROBOT);
    }

    /** {@code ours} for index 0, {@code partner} for any other, each falling back to {@code all} when null or blank. */
    static String robotFor(int index, String ours, String partner, String all) {
        String own = index == 0 ? ours : partner;
        return own == null || own.trim().isEmpty() ? all : own.trim();
    }

    /**
     * Whether {@code robot} ("19429", "20245", ...) has the roller FLOWER stick: 19429 since 17 Nov 2026
     * ({@code RobotDifferences}: {@code FlowerStickSubsystem.roller = true}; 20245 keeps the passive stick).
     * {@code BeldingCalibrationTest} fails if master's table stops saying so.
     *
     * <p>With it, the robot fills its lane, four or what it has room for, on every FLOWER pick and GARDEN gather that
     * reaches the pieces (the pick or the gather ran to its end, the stick down): the mentor's observation on
     * 19429, 19 Nov 2026, "4 POLLEN every time", from picks and gathers. An observation, not yet a measurement:
     * to confirm with match logs as volleys of 4 counted from the launcher wheels' dips ({@code read_logs.py
     * volleys}), since the lane counter is unreliable during gathers. It replaces the Belding draw ({@link #load},
     * which predates the roller) and the gather's touch model for that robot.
     */
    static boolean rollerStick(String robot) {
        return "19429".equals(robot);
    }

    /**
     * Whether a roller stick fills the lane at all: the mentor's observation of 19 Nov 2026, which Belding's draw
     * (before the roller) cannot speak to. Off for an event whose logs counted that robot's volleys with the roller
     * fitted ({@link FruitportCalibration}: 19429's fired four in 4 of 10), so its draw is used instead.
     */
    static boolean rollerFillsLane = true;

    /** Whether the robot at {@code index} fills its lane on a pick or a gather that reaches the pieces ({@link #rollerStick}). */
    static boolean rollerFills(int index) {
        return on && rollerFillsLane && rollerStick(robotFor(index));
    }

    /** Whether every load of the robot at {@code index} is four ({@link #robotFor(int)} "full"): a FLOWER pick's, and a GARDEN gather's too. */
    static boolean fullLoads(int index) {
        return "full".equals(robotFor(index));
    }

    /** The pieces of a pick that reach the launcher, drawn from {@code robot}'s logged volleys at that FLOWER. */
    static int load(String robot, boolean scoringFlower, java.util.Random luck) {
        int[] ir = scoringFlower ? IR_SCORING : IR_WALL, uv = scoringFlower ? UV_SCORING : UV_WALL;
        switch (robot) {
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
     * Sets the simulator's statics for a calibrated run (call only when {@link #on}): no knock from a hit at all (no
     * CELL at Belding was knocked over under its tipping weight), and a request's dwell ({@link #dwellOverride}) if it
     * has one.
     */
    /**
     * A request's {@code "tipDwellS"} (seconds, or {@code [least, most]}): the dwell before every TIP, in place of
     * the simulator's measured at-weight range, and as the well-past dwell its shortest, for a designed slow-swing case;
     * null keeps the simulator's own ({@link FieldSim#tipDwellRange}). Set before {@link #apply}.
     */
    static double[] dwellOverride;

    static void apply() {
        dwellBefore = FieldSim.tipDwellRange;
        impulseBefore = FieldSim.impulseScale;
        if (dwellOverride != null) {
            FieldSim.tipDwellRange = dwellOverride;
            FieldSim.tipDwellOverloadedS = dwellOverride[0];
        }
        FieldSim.impulseScale = 0;
    }

    /** Puts back what {@link #apply} replaced; harmless when it never ran. */
    static void restore() {
        if (Double.isNaN(impulseBefore)) return;
        FieldSim.tipDwellRange = dwellBefore;
        FieldSim.tipDwellOverloadedS = FieldSim.MEASURED_TIP_DWELL_WELL_PAST_S;
        FieldSim.impulseScale = impulseBefore;
        impulseBefore = Double.NaN;
    }

    /** A shot that does not stay in leaves this much slower than the range needs: it drops in front of the CELL. */
    static final double MISS_SPEED_FACTOR = 0.85;
    /** A shot that stays in keeps this much of the placeholder spread, so it still lands in different places. */
    static final double HIT_SPREAD = 0.0;

    // BIOBUZZ_AUTO_CALIBRATION=fruitport: Fruitport's numbers in place of the ones above, from this JVM's first run.
    static {
        if ("fruitport".equals(System.getenv("BIOBUZZ_AUTO_CALIBRATION"))) FruitportCalibration.use();
    }
}
