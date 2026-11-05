package org.firstinspires.ftc.teamcode.subsystems;

/**
 * Whether the lane is full, read from the colour sensor as a state, not a count: a piece held
 * under the sensor for more than {@code fullAfterMs} while the intake is taking in means the lane
 * behind it is packed and nothing more can come in. A piece passing through was under it for at
 * most about a second in the 1 Nov logs; one that stops there has nowhere to go.
 *
 * <p>The mentor's rule (2 Nov 2026): the indicator's green, the intake's full hold and the Autos'
 * {@code IntakeFull} all read this one signal. {@link LaneTracker} counts arrivals and shots, which
 * drifts on any missed event, so it stays in the log only.
 *
 * <p>A held piece is full wherever the sensor is mounted (slot 4, the entrance, since early Nov
 * 2026; slot 3 before). The hold starts only once the intake has taken in with the piece there:
 * a piece parked under the sensor while the intake was idle all along says nothing about whether
 * the lane would take another. From then on it counts whether or not the intake keeps running
 * (5 Nov 2026: drivers let go of the intake about 0.35 s after the 4th piece came in, and a hold
 * that counted only intaking time never reached 1 s, so the lane never read full). Once full,
 * it stays full while the piece is there, intake or not (the intake holding for it does not undo
 * it), and clears the moment the piece leaves.
 *
 * <p>Presence has the same hysteresis as {@link LaneTracker}: it starts above {@code presentAbove}
 * and ends below {@code leaveBelow}. Each presence's whole dwell is reported when it ends
 * ({@link #endedDwellMs()}), so a match log shows passing pieces and parked ones side by side and
 * normal-play logs can tighten the hold time from them.
 *
 * <p>Pure: no SDK, no allocation, so a test can drive it.
 */
public final class LaneFull {

    /** The most time one reading adds while taking in, ms: twice the 50 ms read interval. */
    static final double MAX_STEP_MS = 100;

    /** The most one reading adds once the intake has stopped with a piece under it, ms: the 250 ms idle read and some. */
    static final double MAX_IDLE_STEP_MS = 400;

    private boolean present;
    private long presentSinceMs;
    private long lastMs;
    private boolean hasLast;
    /** Time under the sensor while taking in, ms, this presence. */
    private double heldWhileIntakingMs;
    /** Time under the sensor since the intake first took in during this presence, intake or not, ms. */
    private double heldSinceTakingInMs;
    private boolean full;
    private double endedDwellMs = Double.NaN;
    private boolean endedFull;
    private long departures;

    /**
     * One fresh sensor reading.
     *
     * @param nowMs        a monotonic clock, ms
     * @param brightness   the sensor's clear channel; NaN reads as no piece, so the roller is
     *                     never held stopped by a bad reading
     * @param intaking     the intake is taking in
     * @param presentAbove a piece is there above this brightness; NaN: never (full is off)
     * @param leaveBelow   and gone below this; NaN, or above {@code presentAbove}: the same as
     *                     {@code presentAbove}, as {@code LaneTracker} does
     * @param fullAfterMs  held this long while taking in is full; NaN or negative: never, and a
     *                     full already latched clears at once
     */
    public void step(long nowMs, double brightness, boolean intaking,
                     double presentAbove, double leaveBelow, double fullAfterMs) {
        endedDwellMs = Double.NaN;
        // At most MAX_STEP_MS a reading: after a slow idle read (ColorSensorSubsystem.readIdleEveryMs)
        // the gap is mostly time the intake was not running, which must not count toward full.
        double elapsed = hasLast ? Math.min(MAX_STEP_MS, Math.max(0, nowMs - lastMs)) : 0;
        long previousMs = hasLast ? lastMs : nowMs;
        lastMs = nowMs;
        hasLast = true;
        double leave = Double.isNaN(leaveBelow) ? presentAbove : Math.min(leaveBelow, presentAbove);
        boolean wasPresent = present;
        if (Double.isNaN(presentAbove) || Double.isNaN(brightness)) {
            present = false;
        } else if (present) {
            present = !(brightness < leave);
        } else {
            present = brightness > presentAbove;
        }
        if (present && !wasPresent) {
            presentSinceMs = nowMs;
            heldWhileIntakingMs = 0;
            heldSinceTakingInMs = 0;
        } else if (present && intaking) {
            heldWhileIntakingMs += elapsed;
            heldSinceTakingInMs += elapsed;
        } else if (present && heldWhileIntakingMs > 0) {
            // The intake stopped with a piece that came in under it: it keeps counting, at the real
            // gap between readings (the idle rate is 250 ms), up to MAX_IDLE_STEP_MS for a stall.
            heldSinceTakingInMs += Math.min(MAX_IDLE_STEP_MS, Math.max(0, nowMs - previousMs));
        } else if (!present && wasPresent) {
            endedDwellMs = nowMs - presentSinceMs;
            endedFull = full;
            departures++;
        }
        boolean enabled = fullAfterMs >= 0;
        if (!present || !enabled) {
            full = false;
        } else if (!full && heldWhileIntakingMs > 0 && heldSinceTakingInMs >= fullAfterMs) {
            // Some time taking in is always needed: a zero hold never makes an idle intake full.
            full = true;
        }
    }

    /** The lane is full: a piece has been held under the sensor long enough while taking in. */
    public boolean full() {
        return full;
    }

    /** A piece is under the sensor, as of the last reading. */
    public boolean present() {
        return present;
    }

    /** How long the piece under the sensor has been there, intake or not, ms; 0 with none. */
    public double presentForMs() {
        return present ? lastMs - presentSinceMs : 0;
    }

    /** How long the piece under the sensor has been there while taking in, ms; 0 with none. */
    public double heldWhileIntakingMs() {
        return present ? heldWhileIntakingMs : 0;
    }

    /**
     * When the last {@link #step} saw a piece leave: how long it had been under the sensor, ms,
     * intake or not. NaN on every other step.
     */
    public double endedDwellMs() {
        return endedDwellMs;
    }

    /**
     * Pieces that have left the sensor so far, one count each: a caller that logs each departure
     * once compares this with the count it last logged, since the sensor reads less often than
     * the loop runs.
     */
    public long departures() {
        return departures;
    }

    /** Whether the piece that just left ({@link #endedDwellMs()}) had made the lane full. */
    public boolean endedFull() {
        return endedFull;
    }
}
