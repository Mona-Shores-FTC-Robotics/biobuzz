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
 *       second before the last), the CELL sat 2.6 to 3.4 s, overloaded or not.</li>
 *   <li><b>Where the HIVE tips</b> ({@link #tipPoint}): Field 1 at about 7.0 POLLEN weights, Field 2 at about
 *       7.95 to 8.0 (PR #519). Odd qualification matches were on Field 1, so odd seeds are Field 1 and even
 *       seeds Field 2.</li>
 * </ul>
 *
 * <p><b>Not set here</b> (still the simulator's own): drive times (the 270 degree curve to the LEFT spots took
 * 3.5 to 4.6 s against 2.9 s), the two SCORING FLOWER picks that came back short (Q-1's wedge, Q-4's stop short),
 * the shot counter, and contact with other robots (Q-27).
 */
final class BeldingCalibration {
    private BeldingCalibration() {}

    /** On for this JVM's runs; a request or a test may set it. */
    static boolean on = "belding".equals(System.getenv("BIOBUZZ_AUTO_CALIBRATION"));

    /** Launch range from the turret axis, inches, and the fraction of launched pieces that stay in the CELL. */
    static final double[][] STAY_IN = {{36.0, 0.65}, {42.4, 0.92}, {53.9, 0.82}};

    /** Seconds a CELL over its tipping weight sits before the rocker moves: {shortest, longest}. */
    static final double[] DWELL_S = {2.6, 3.4};

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

    /** Where this run's HIVEs tip in the Guide's band: Field 1 on odd seeds, Field 2 on even ones. */
    static double tipPoint(long seed) {
        return seed % 2 != 0 ? HiveCalibration.BELDING_FIELD_1 : HiveCalibration.BELDING_FIELD_2;
    }

    /** A shot that does not stay in leaves this much slower than the range needs: it drops in front of the CELL. */
    static final double MISS_SPEED_FACTOR = 0.85;
    /** A shot that stays in keeps this much of the placeholder spread, so it still lands in different places. */
    static final double HIT_SPREAD = 0.25;
}
