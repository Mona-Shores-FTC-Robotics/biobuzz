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
 *
 * <p>Two encoders sharpen it, each only on a robot that has it in its socket (the turret's encoder
 * moved off the intake's, {@code TurretSubsystem.encoderOnDriveMotor}; the feeder's own plugged in,
 * {@code FeederSubsystem.encoderFitted}). Neither is on a robot yet: <b>not run on a robot</b>.
 * <ul>
 *   <li><b>The roller stalled</b> while driven in, with the entry piece still: the grinding kind of
 *       jam (M-6, Q-39) is called after {@code stallJamMs} of stall and quiet together, not the
 *       full {@code jamMs}. A roller turning freely says nothing (M-1's free roller was a jam too),
 *       so the quiet timer still covers that kind.</li>
 *   <li><b>The feeder stopped</b> while feeding: a ball pinned at the feeder, so nothing can leave.
 *       {@code feederStallMs} of it is a jam whether or not a piece is at the entry, since backing
 *       the lane out takes the stack's push off the pinned ball. A ball held in the throat beam is
 *       still the throat's ({@code IntakeSubsystem} passes the feeder as turning then).</li>
 * </ul>
 */
final class FeedJamClear {

    private boolean wasFeeding;
    private boolean wasPresent;
    private int lastShots;
    private long quietSinceNs;
    /** When the roller's stall and the feeder's stop began; -1 while not stalled. */
    private long rollerStallSinceNs = -1;
    private long feederStallSinceNs = -1;
    private Cause lastCause = Cause.NONE;
    private long backUntilNs = -1;
    private int burstsThisFeed;
    private int bursts;

    /** What called the last burst, for the match log. */
    enum Cause {
        NONE("none"), QUIET("entry piece still, nothing fired"),
        ROLLER_STALLED("roller stalled on the entry piece"), FEEDER_STOPPED("feeder stopped while feeding");

        final String text;

        Cause(String text) {
            this.text = text;
        }
    }

    /** As the full {@link #update(boolean, boolean, boolean, boolean, int, long, double, double, double, double, int)}, with no encoder: the quiet timer alone. */
    boolean update(boolean feeding, boolean present, int shots, long nowNs,
                   double jamMs, double backMs, int maxBursts) {
        return update(feeding, present, false, false, shots, nowNs, jamMs, Double.NaN, Double.NaN,
                backMs, maxBursts);
    }

    /**
     * One step, every loop.
     *
     * @param feeding   the feeder is feeding (gate open) and nothing else has the roller (no
     *                  reject, no eject)
     * @param present   a piece is under the lane's entry sensor
     * @param rollerStalled the roller is driven in and not turning (false without its own encoder)
     * @param feederStalled the feeder is driven and not turning (false without its own encoder)
     * @param shots     the launcher's shot count; a change is movement
     * @param jamMs     quiet this long is a jam; NaN or not positive never clears
     * @param stallJamMs with the roller stalled, stall and quiet together this long is a jam; NaN off
     * @param feederStallMs the feeder stopped this long is a jam; NaN off
     * @param backMs    how long a burst backs out
     * @param maxBursts bursts in one feed; 0 or less never clears
     * @return back out this loop
     */
    boolean update(boolean feeding, boolean present, boolean rollerStalled, boolean feederStalled,
                   int shots, long nowNs, double jamMs, double stallJamMs, double feederStallMs,
                   double backMs, int maxBursts) {
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
            rollerStallSinceNs = -1;
            feederStallSinceNs = -1;
        }
        if (backUntilNs >= 0) {
            if (nowNs < backUntilNs) return true;
            backUntilNs = -1;
            quietSinceNs = nowNs; // feeding again: time the next quiet spell afresh
            wasPresent = present;
            lastShots = shots;
            rollerStallSinceNs = -1;
            feederStallSinceNs = -1;
            return false;
        }
        rollerStallSinceNs = rollerStalled ? (rollerStallSinceNs < 0 ? nowNs : rollerStallSinceNs) : -1;
        feederStallSinceNs = feederStalled ? (feederStallSinceNs < 0 ? nowNs : feederStallSinceNs) : -1;
        if (present != wasPresent || shots != lastShots) {
            quietSinceNs = nowNs;
            wasPresent = present;
            lastShots = shots;
        }
        if (!(backMs > 0.0) || burstsThisFeed >= maxBursts) return false;
        double quietMs = (nowNs - quietSinceNs) / 1e6;
        Cause cause = Cause.NONE;
        if (feederStallSinceNs >= 0 && feederStallMs > 0.0 && (nowNs - feederStallSinceNs) / 1e6 >= feederStallMs) {
            cause = Cause.FEEDER_STOPPED;
        } else if (present && rollerStallSinceNs >= 0 && stallJamMs > 0.0
                && (nowNs - rollerStallSinceNs) / 1e6 >= stallJamMs && quietMs >= stallJamMs) {
            cause = Cause.ROLLER_STALLED;
        } else if (present && jamMs > 0.0 && quietMs >= jamMs) {
            cause = Cause.QUIET;
        }
        if (cause == Cause.NONE) return false;
        lastCause = cause;
        burstsThisFeed++;
        bursts++;
        backUntilNs = nowNs + (long) (backMs * 1e6);
        return true;
    }

    /** What called the last burst; {@link Cause#NONE} before the first. */
    Cause lastCause() {
        return lastCause;
    }

    /** Bursts since this object was made: the match log counts them. */
    int bursts() {
        return bursts;
    }
}
