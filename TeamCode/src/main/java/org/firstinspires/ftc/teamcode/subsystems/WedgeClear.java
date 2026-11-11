package org.firstinspires.ftc.teamcode.subsystems;

/**
 * Backs the intake out for a moment when its roller is wedged: stalled at high current while taking
 * in, with nothing under the lane sensor. Pure, so a test drives every case; {@link IntakeSubsystem}
 * owns the numbers and decides when the roller is "taking in".
 *
 * <p>Why: at the Belding Qualifier (7 Nov 2026) the roller stalled in 8 of the 17 match logs: under
 * 50 ticks/s (it runs at about 2400) at 7 to 10 A (under 3.4 A nine times in ten). Most stalls let
 * go inside 0.8 s; Infrared's in Q26 held for 13 s with an empty lane, pieces wedged side by side
 * at the mouth, until the driver noticed. A stall with a piece under the lane sensor is the lane
 * backed up, not a wedge, and is never cleared: backing out there would throw a held piece away.
 *
 * <p>The rule: stalled for {@code stallMs} without a break, the roller and stars back out for
 * {@code backMs}, then take in again. After {@code maxBursts} bursts in a row with the roller never
 * running free in between, it stops trying until the roller runs free or the intake is asked
 * again: a hard jam is the driver's (the eject button), not a pulse for the rest of the match.
 */
final class WedgeClear {

    private long stalledSinceNs = -1;
    private long backUntilNs = -1;
    private int burstsInARow;
    private int bursts;

    /**
     * One step, every loop.
     *
     * @param takingIn  the roller is taking in for the OpMode and nothing else has it (not full,
     *                  no reject, no piece under the lane sensor)
     * @param ticksPerSecond the roller's speed
     * @param amps      the roller's current as last read; NaN never counts as a stall
     * @param stallTicksPerSecond below this speed the roller is not turning
     * @param stallAmps at or above this current it is being held
     * @param stallMs   unbroken stall before a burst
     * @param backMs    how long a burst backs out
     * @param maxBursts bursts in a row before it gives up; 0 or less never clears
     * @return back out this loop
     */
    boolean update(boolean takingIn, double ticksPerSecond, double amps, long nowNs,
                   double stallTicksPerSecond, double stallAmps, double stallMs, double backMs, int maxBursts) {
        if (!takingIn) {
            stalledSinceNs = -1;
            backUntilNs = -1;
            burstsInARow = 0;
            return false;
        }
        if (backUntilNs >= 0) {
            if (nowNs < backUntilNs) return true;
            backUntilNs = -1;
            stalledSinceNs = -1; // the roller starts from a stop again: time the stall afresh
            return false;
        }
        boolean turning = !(Math.abs(ticksPerSecond) < stallTicksPerSecond);
        if (turning) {
            stalledSinceNs = -1;
            burstsInARow = 0;
            return false;
        }
        if (!(amps >= stallAmps)) {
            stalledSinceNs = -1;
            return false;
        }
        if (stalledSinceNs < 0) stalledSinceNs = nowNs;
        if ((nowNs - stalledSinceNs) / 1e6 >= stallMs && burstsInARow < maxBursts && backMs > 0.0) {
            burstsInARow++;
            bursts++;
            backUntilNs = nowNs + (long) (backMs * 1e6);
            return true;
        }
        return false;
    }

    /** Bursts since this object was made: the match log counts them. */
    int bursts() {
        return bursts;
    }

    /** It has stopped trying: {@code maxBursts} bursts in a row did not free the roller. */
    boolean gaveUp(int maxBursts) {
        return burstsInARow >= maxBursts && maxBursts > 0;
    }
}
