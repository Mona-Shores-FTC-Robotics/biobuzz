package org.firstinspires.ftc.teamcode.subsystems;

/**
 * One IR break-beam, read once a loop: whether it is broken now, for how long, and how many pieces
 * have passed it. A <b>pass</b> is a break that lasted at least {@code minBlockedReads} readings and
 * then cleared: the piece has gone by. One reading alone is never a piece (a hub's digital inputs
 * are polled, not latched, so a real piece at lane speed blocks a beam for 120 to 250 ms, six to a
 * dozen loops; #505), which keeps a one-loop flicker from counting.
 *
 * <p>The beam's own wiring decides which level is "broken": the Adafruit receivers (#213) are open
 * collector and read LOW when broken, which {@code BeamBreakSubsystem} turns into {@code blocked}
 * before this class sees it.
 *
 * <p>Pure: no SDK, no allocation, so a test can drive it.
 */
public final class BeamBreak {

    private boolean hasLast;
    private boolean blocked;
    private long blockedSinceMs;
    private long clearSinceMs;
    private long lastMs;
    private int blockedReads;
    private int passes;
    private boolean passedThisStep;
    private long lastPassMs = -1;
    private long lastPassBlockedMs;

    /**
     * One reading.
     *
     * @param nowMs           a monotonic clock, ms
     * @param blockedNow      the beam is broken in this reading
     * @param minBlockedReads readings a break must last to be a piece; below 1 counts as 1
     */
    public void step(long nowMs, boolean blockedNow, int minBlockedReads) {
        passedThisStep = false;
        if (!hasLast) {
            hasLast = true;
            blocked = blockedNow;
            blockedSinceMs = nowMs;
            clearSinceMs = nowMs;
            blockedReads = blockedNow ? 1 : 0;
            lastMs = nowMs;
            return;
        }
        lastMs = nowMs;
        if (blockedNow) {
            if (!blocked) {
                blockedSinceMs = nowMs;
                blockedReads = 0;
            }
            blockedReads++;
        } else if (blocked) {
            clearSinceMs = nowMs;
            if (blockedReads >= Math.max(1, minBlockedReads)) {
                passes++;
                passedThisStep = true;
                lastPassMs = nowMs;
                lastPassBlockedMs = nowMs - blockedSinceMs;
            }
            blockedReads = 0;
        }
        blocked = blockedNow;
    }

    /** The beam is broken, as of the last reading. */
    public boolean blocked() {
        return blocked;
    }

    /** How long the beam has been broken, ms; 0 while it is clear. */
    public double blockedForMs() {
        return hasLast && blocked ? lastMs - blockedSinceMs : 0;
    }

    /** How long the beam has been clear, ms; 0 while it is broken, and since the first reading if never broken. */
    public double clearForMs() {
        return hasLast && !blocked ? lastMs - clearSinceMs : 0;
    }

    /** Pieces that have passed since this object was made. */
    public int passes() {
        return passes;
    }

    /** The last {@link #step} saw a piece finish passing. */
    public boolean passedThisStep() {
        return passedThisStep;
    }

    /** When the last piece finished passing, ms on the step clock; -1 before the first. */
    public long lastPassMs() {
        return lastPassMs;
    }

    /** How long the last piece to pass blocked the beam, ms: for the bench check (#505: 120 to 250 ms). */
    public long lastPassBlockedMs() {
        return lastPassBlockedMs;
    }
}
