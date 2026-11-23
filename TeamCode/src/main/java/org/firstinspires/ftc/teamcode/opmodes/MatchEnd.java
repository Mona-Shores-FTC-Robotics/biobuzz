package org.firstinspires.ftc.teamcode.opmodes;

/**
 * The TeleOp's end of the match: from {@link #TELEOP_S} less a margin on the robot's own clock
 * (seconds since its TeleOp PLAY), the TeleOp launches nothing more. The feeder stops (a feed under
 * way is cut, unjam too) and the launcher's wheels spin down to 0, and nothing spins them up again
 * until the OpMode is stopped. Pure, so a test drives it.
 *
 * <p>Why (Fruitport, 21 Nov 2026): 20245 fed after the field's 0:00 in Q-20, Q-42, M-3 and M-7, a
 * ball leaving up to about 2 s after it. Its clock was not the cause: the field read 0:00 at
 * 119.8 to 120.3 s after the robot's TeleOp PLAY in all four. A Circle pressed at or after 0:00
 * fed (Q-20 121.9 s, M-3 120.3 s, M-7 120.0 s), and one pressed before it fed late, once the arc
 * turn and the spin-up were done (Q-42: pressed 119.8 s, balls out 121.3 and 121.7 s); and the
 * TeleOp runs until the drive team presses stop, 3 to 8 s after 0:00. The manual: G404, "ROBOTS
 * must no longer have powered movement after the end of TELEOP", and §10.5, "Achievements scored
 * ... after the MATCH ends at 0:00 may be subject to penalties".
 *
 * <p>The margin ({@code TeleOpConfig.matchEndMarginS}, 1.0 s) covers a ball's time from gate to
 * air (about 0.4 to 0.5 s at Fruitport) and a field 0:00 that comes up to a few tenths early on
 * the robot's clock. Drive, intake and turret stay the drive team's to stop at 0:00.
 * It runs only with {@code TeleOpConfig.stopLaunchingAtMatchEnd} on, which is off by default.
 */
final class MatchEnd {

    /** The TeleOp period, seconds (manual §10.4: TELEOP is 2:00). */
    static final double TELEOP_S = 120.0;

    /** The most margin a typo can ask for: more would give away the last shots of the match. */
    static final double MAX_MARGIN_S = 10.0;

    private boolean over;

    /**
     * Seconds after the TeleOp's PLAY at which launching stops: {@link #TELEOP_S} less the margin,
     * clamped to [0, {@link #MAX_MARGIN_S}]. NaN or negative is no margin, never a later stop.
     */
    static double cutoffS(double marginS) {
        double m = Double.isNaN(marginS) || marginS < 0.0 ? 0.0 : Math.min(marginS, MAX_MARGIN_S);
        return TELEOP_S - m;
    }

    /**
     * One loop. Returns true on the loop launching ends, once, so the OpMode stops the launcher
     * and logs it there. Once over it stays over: a clock that read NaN later does not reopen it.
     *
     * @param secondsSincePlay the robot's seconds since this TeleOp's PLAY
     * @param marginS          {@code TeleOpConfig.matchEndMarginS}
     */
    boolean update(double secondsSincePlay, double marginS) {
        if (over) return false;
        if (secondsSincePlay >= cutoffS(marginS)) {
            over = true;
            return true;
        }
        return false;
    }

    /** Launching has ended: no feed, no spin-up. */
    boolean over() {
        return over;
    }
}
