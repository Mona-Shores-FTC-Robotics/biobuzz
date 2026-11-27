package org.firstinspires.ftc.teamcode.logging;

/**
 * The simulator set to the Fruitport Qualifier (USMIFRQ2, 21 Nov 2026): what the 15 match logs on
 * {@code events/2026-11-fruitport} (19429: 7, 20245: 8) and the official AUTO TIP counts (FTCScout,
 * {@code tools/event-review/data/fruitport/ftcscout/matches.json}) showed. Off by default;
 * {@code BIOBUZZ_AUTO_CALIBRATION=fruitport} (a study) or {@code "calibration": "fruitport"} (a request) turns it on.
 *
 * <p>It runs on {@link BeldingCalibration}'s engine: {@link #use} turns that on and puts Fruitport's numbers in
 * place of Belding's, and every number Fruitport has no evidence for stays Belding's (listed below). Every count
 * here can be read again with {@code read_logs.py steps} on the match logs: each LaunchAll's balls are counted from
 * the launcher wheels' dips, a robot's first volley from its shot counter (the dips of a volley fired from a
 * standstill are not read).
 *
 * <p><b>What Fruitport sets, and from what:</b>
 * <ul>
 *   <li><b>How many pieces a volley after a FLOWER pick fires</b> ({@link #IR_WALL}, {@link #IR_SCORING},
 *       {@link #UV_WALL}, {@link #UV_SCORING}), 20 volleys. Infrared (19429, the roller stick, limp that day):
 *       after the WALL FLOWER 1, 3, 4, 3, 4, 1 (Q-2, Q-17, Q-23, Q-28, Q-39, M-6; Q-23's volley was 5, one of them a
 *       preload left from the first), after the SCORING FLOWER 3, 4, 4, 2 (Q-2, Q-17, Q-28, Q-39): four in 4 of 10.
 *       Ultraviolet (20245, the passive stick): after the WALL FLOWER 4 six times (Q-4, Q-11, Q-20, Q-28, Q-42, M-7),
 *       after the SCORING FLOWER 0, 4, 4, 4 (Q-4, Q-11, Q-28, Q-42): four in 9 of 10. So 19429's roller stick does
 *       not fill the lane here ({@link BeldingCalibration#rollerFillsLane} off): the measured volleys replace the
 *       mentor's 19 Nov observation, until its stick, held out since 22 Nov, is logged.</li>
 *   <li><b>How many launched pieces stay in the CELL</b> ({@link #STAY_IN}): <b>0.94 at {@link #AUTO_HIVE_RANGE_IN}</b>
 *       (39.2 in), the one range Fruitport's Autos fired from: {@code RIGHT_AUTO_HIVE} at the RIGHT CELL and its
 *       twin {@code LEFT_AUTO_HIVE} at the LEFT CELL are the same distance out. It is the one share that best fits
 *       both ends together (89 shots):
 *       <ul>
 *         <li>The preloads from {@code RIGHT_AUTO_HIVE} made the first TIP in 14 of 14 runs that fired them (51
 *             shots by the shot counter: ten volleys of 4, three of 3, one of 2). The three volleys of 3 tipped the
 *             CELL on its three starting NECTAR, which needs all three in.</li>
 *         <li>The LEFT CELL starts empty and R-Solo's two volleys from {@code LEFT_AUTO_HIVE} were its only pieces
 *             (no partner fired there in a qualifier). Eight fired made the second TIP once in three (20245: Q-42
 *             yes; Q-11 and Q-28 no; 24 shots), and seven fired never did (19429: Q-17, Q-28; 14 shots).</li>
 *       </ul>
 *       Taken apart, the two ends read 0.95 or better (the preloads, 1 s after PLAY) and 0.87 (the LEFT volleys,
 *       after a 4 s drive backward and a pick: 0.87 to the eighth is one in three), but with three volley pairs
 *       at the LEFT end the logs do not show the two differ (a likelihood ratio of 2.4, under the 3.8 that would),
 *       so the simulator, which keys on range, gets the one number. It sits between the mentor's 0.90 to 0.95 for
 *       the AUTO launch spots (19 Nov 2026). <b>Kept from Belding:</b> 0.90 at 53.9 in ({@code RIGHT_START}; no
 *       Fruitport Auto fired from further out than 39.2 in, so {@code RIGHT_AUTO_OFFSET}, 51.2 in, rests on
 *       Belding's nine preload volleys) and 0.70 at 36 in (nor from nearer; Belding's thinnest number).
 *       {@code BIOBUZZ_FRUITPORT_STAY_AUTO_HIVE} sets the measured row, and a request's {@code "ourInRate"} /
 *       {@code "partnerInRate"} still replace every row.</li>
 *   <li><b>How long a FLOWER pick takes</b> ({@link #PICK_DWELL_S}): the 22 Auto picks took 3.03 to 4.00 s card to
 *       card (19429: median 3.79 over 10; 20245: 3.66 over 12), with a logged dwell of 1.7 to 1.8 s after the stall
 *       call, which comes about 0.3 s after contact: the lane-full signal ended no dwell early, so each pick stayed
 *       master's whole dwell ({@code FlowerPicker.dwell.dwellMs}, 2 s). The floor is that dwell, and the simulator's
 *       pick then takes 3.6 to 3.7 s where its own ends at a full lane, about 3.1 s.</li>
 *   <li><b>How long a volley takes</b> ({@link #WHOLE_FEED_CHANCE}): of the 13 volleys after a robot's first that
 *       fired four or more balls, the shot counter ended 9 at the fourth shot (cards of 1.8 to 2.7 s) and missed a
 *       ball in 4, which ran the whole 3.5 s feed (3.6 to 3.9 s): 19429 Q-23 and Q-39, 20245 Q-20 and Q-42. So each
 *       such volley runs whole with that chance. A volley of fewer than four, or of none, always runs its whole set
 *       feed ({@link BeldingCalibration#shortVolleysRunTheFeed}): all 8 such volleys did (19429's of 1, 3, 3, 3, 2
 *       and 1 balls and its M-1 volley, which fed nothing with four held; 20245's one of none, Q-4; cards of 3.5 to
 *       4.1 s), where the simulator's own shortcut
 *       ends a volley at its last ball and an empty one at once. The set time is master's today
 *       ({@code LaunchCards.launchAllFeedS}), shorter than Fruitport's 3.5 s.</li>
 * </ul>
 *
 * <p><b>Kept from Belding, for want of Fruitport evidence:</b>
 * <ul>
 *   <li><b>Where the HIVE tips</b> ({@link BeldingCalibration#tipPoint}: Field 1 on odd seeds, Field 2 on even ones).
 *       Fruitport's counts fit both and fix neither: seven POLLEN fired at the empty LEFT CELL did not tip it (2 of
 *       2), and three fired onto the RIGHT CELL's three NECTAR did (3 of 3).</li>
 *   <li><b>GARDEN loads.</b> No Fruitport Auto gathered the GARDEN: a gather is the simulator's own (the mouth's
 *       touch and the corner wheels, {@code FieldSim.rake}).</li>
 *   <li><b>The TIP lag</b> (every run's, {@link FieldSim#tipDwellRange}, Belding's films) and <b>no knock from a
 *       hit</b>.</li>
 *   <li><b>The 0.90 at 53.9 in and the 0.70 at 36 in</b> (above).</li>
 * </ul>
 *
 * <p><b>Not set here</b> (the simulator's own): drive times. Fruitport's Autos drove 2 to 3 times slower than a
 * driver ({@code doc/robot-todo.md}, "Why Auto drives slowly"): {@code WALL_FLOWER_STAGING} backward to
 * {@code LEFT_AUTO_HIVE} took 3.0 to 4.2 s (11 drives).
 */
final class FruitportCalibration {
    private FruitportCalibration() {}

    /** Whether Fruitport's numbers are the ones in {@link BeldingCalibration} now. */
    static boolean on;

    /**
     * The launch range Fruitport's Autos fired from, inches from the turret axis to the CELL's aim point:
     * {@code RIGHT_AUTO_HIVE}'s and {@code LEFT_AUTO_HIVE}'s both ({@code FruitportCalibrationTest} holds them to it).
     */
    static final double AUTO_HIVE_RANGE_IN = 39.2;

    /** Range and the fraction that stays in: Belding's CLOSE row, Fruitport's one measured row, Belding's RIGHT_START row. */
    static final double[][] STAY_IN = {
            {36.0, 0.70},
            {AUTO_HIVE_RANGE_IN, env("BIOBUZZ_FRUITPORT_STAY_AUTO_HIVE", 0.94)},
            {53.9, 0.90}};

    /** Balls each robot's volley after a FLOWER pick fired, by the launcher wheels' dips, capped at the lane's four. */
    static final int[] IR_WALL = {1, 3, 4, 3, 4, 1}, IR_SCORING = {3, 4, 4, 2};
    static final int[] UV_WALL = {4, 4, 4, 4, 4, 4}, UV_SCORING = {0, 4, 4, 4};

    /** The least a pick stays on the FLOWER, s: master's whole dwell, as every Fruitport pick stayed. */
    static final double PICK_DWELL_S = env("BIOBUZZ_FRUITPORT_PICK_DWELL", 2.0);

    /** The chance a volley of four after a robot's first runs its whole set feed: the shot counter missed a ball in 4 of 13. */
    static final double WHOLE_FEED_CHANCE = env("BIOBUZZ_FRUITPORT_WHOLE_FEED", 4.0 / 13);

    private static double env(String name, double otherwise) {
        String v = System.getenv(name);
        return v == null ? otherwise : Double.parseDouble(v);
    }

    /** Belding's numbers, kept while Fruitport's are in their place. */
    private static double[][] stayInBefore;
    private static int[] irWallBefore, irScoringBefore, uvWallBefore, uvScoringBefore;
    private static double pickDwellBefore, wholeFeedChanceBefore;
    private static boolean rollerFillsBefore, beldingOnBefore, shortVolleysBefore;

    /** Turns the calibration engine on with Fruitport's numbers; {@link #restore} puts Belding's back. Harmless twice. */
    static void use() {
        if (on) return;
        on = true;
        beldingOnBefore = BeldingCalibration.on;
        stayInBefore = BeldingCalibration.STAY_IN;
        irWallBefore = BeldingCalibration.IR_WALL;
        irScoringBefore = BeldingCalibration.IR_SCORING;
        uvWallBefore = BeldingCalibration.UV_WALL;
        uvScoringBefore = BeldingCalibration.UV_SCORING;
        pickDwellBefore = BeldingCalibration.PICK_DWELL_S;
        wholeFeedChanceBefore = BeldingCalibration.wholeFeedChance;
        rollerFillsBefore = BeldingCalibration.rollerFillsLane;
        shortVolleysBefore = BeldingCalibration.shortVolleysRunTheFeed;
        BeldingCalibration.on = true;
        BeldingCalibration.STAY_IN = STAY_IN;
        BeldingCalibration.IR_WALL = IR_WALL;
        BeldingCalibration.IR_SCORING = IR_SCORING;
        BeldingCalibration.UV_WALL = UV_WALL;
        BeldingCalibration.UV_SCORING = UV_SCORING;
        BeldingCalibration.PICK_DWELL_S = PICK_DWELL_S;
        BeldingCalibration.wholeFeedChance = WHOLE_FEED_CHANCE;
        BeldingCalibration.rollerFillsLane = false;
        BeldingCalibration.shortVolleysRunTheFeed = true;
    }

    /** Puts Belding's numbers back, and the engine's switch as {@link #use} found it; harmless when {@link #use} never ran. */
    static void restore() {
        if (!on) return;
        on = false;
        BeldingCalibration.on = beldingOnBefore;
        BeldingCalibration.STAY_IN = stayInBefore;
        BeldingCalibration.IR_WALL = irWallBefore;
        BeldingCalibration.IR_SCORING = irScoringBefore;
        BeldingCalibration.UV_WALL = uvWallBefore;
        BeldingCalibration.UV_SCORING = uvScoringBefore;
        BeldingCalibration.PICK_DWELL_S = pickDwellBefore;
        BeldingCalibration.wholeFeedChance = wholeFeedChanceBefore;
        BeldingCalibration.rollerFillsLane = rollerFillsBefore;
        BeldingCalibration.shortVolleysRunTheFeed = shortVolleysBefore;
    }
}
