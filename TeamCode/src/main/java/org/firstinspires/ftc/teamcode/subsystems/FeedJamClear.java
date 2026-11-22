package org.firstinspires.ftc.teamcode.subsystems;

/**
 * Backs the lane out for a moment when a feed has stuck: the gate is open and the feeder feeding,
 * a piece sits under the lane's entry sensor, and nothing has moved for a while, no shot counted
 * and no piece arriving at or leaving the sensor. Pure, so a test drives every case;
 * {@link IntakeSubsystem} owns the numbers and turns the answer into the roller and stars backing
 * out (the prefeed follows the stars, {@code PrefeedSubsystem.requestFor}).
 *
 * <p>Why: at the Fruitport Qualifier (21 Nov 2026) 19429 stuck in 22 feeds of its match and
 * practice logs, 20245 in 1 of 37 logs. Two kinds, the same signal: the piece at the entry wedged
 * with the roller grinding on it at 3.6 to 10 A (M-6's preloads and its TeleOp feed at T 15, Q-39's
 * third volley), or the whole stack still with the roller free (M-1's four preloads, gate open
 * 3.5 s at launch speed, the feeder never loaded). Each time the driver's eject, backing the lane
 * out, freed it and the next feed fired. {@link WedgeClear} cannot help: it times a stall by the
 * roller's encoder, which since 17 Nov 2026 is the turret's, and it leaves a piece under the sensor
 * alone. Across those logs no feed that was firing went 1.2 s with the entry piece still and no
 * shot counted, and the stuck ones all did.
 *
 * <p>The rule: while feeding with a piece under the entry sensor, a quiet spell of {@code jamMs}
 * (time since the feed started, the last counted shot, the last change at the sensor, or the last
 * burst, whichever is latest) backs out for {@code backMs}, then feeds again; at most
 * {@code maxBursts} bursts in one feed. A feed that ends, or the sensor going clear, starts afresh.
 */
final class FeedJamClear {

    private boolean wasFeeding;
    private boolean wasPresent;
    private int lastShots;
    private long quietSinceNs;
    private long backUntilNs = -1;
    private int burstsThisFeed;
    private int bursts;

    /**
     * One step, every loop.
     *
     * @param feeding   the feeder is feeding (gate open) and nothing else has the roller (no
     *                  reject, no eject)
     * @param present   a piece is under the lane's entry sensor
     * @param shots     the launcher's shot count; a change is movement
     * @param jamMs     quiet this long is a jam; NaN or not positive never clears
     * @param backMs    how long a burst backs out
     * @param maxBursts bursts in one feed; 0 or less never clears
     * @return back out this loop
     */
    boolean update(boolean feeding, boolean present, int shots, long nowNs,
                   double jamMs, double backMs, int maxBursts) {
        if (!feeding) {
            wasFeeding = false;
            backUntilNs = -1;
            return false;
        }
        if (!wasFeeding) {
            wasFeeding = true;
            burstsThisFeed = 0;
            backUntilNs = -1;
            quietSinceNs = nowNs;
            wasPresent = present;
            lastShots = shots;
        }
        if (backUntilNs >= 0) {
            if (nowNs < backUntilNs) return true;
            backUntilNs = -1;
            quietSinceNs = nowNs; // feeding again: time the next quiet spell afresh
            wasPresent = present;
            lastShots = shots;
            return false;
        }
        if (present != wasPresent || shots != lastShots) {
            quietSinceNs = nowNs;
            wasPresent = present;
            lastShots = shots;
        }
        if (!present) return false;
        if (!(jamMs > 0.0) || !(backMs > 0.0) || burstsThisFeed >= maxBursts) return false;
        if ((nowNs - quietSinceNs) / 1e6 >= jamMs) {
            burstsThisFeed++;
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
}
