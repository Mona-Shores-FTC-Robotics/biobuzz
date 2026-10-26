package org.firstinspires.ftc.teamcode.logging;

/**
 * Says, once, when a mechanism has been asked to move and its encoder never did.
 *
 * <p>The hub cannot tell an empty motor port from a stalled motor: both read zero speed and idle
 * current, with no error. On 25 Oct 2026 the Autos on 20245 commanded a launcher that was not on
 * the robot, and the only sign in the log was a target of 1000 ticks/s beside a measured 0 for the
 * whole run. This turns that into one event line the moment it is known.
 *
 * <p>Pure: the OpMode feeds it what it already has each loop (asked to move or not, measured
 * ticks/s, the clock) and logs the line it returns. It allocates nothing per loop; the line is
 * built once.
 */
public final class MotionWatch {

    /** How long a mechanism may be commanded without its encoder moving before it is reported. */
    public static final double STALL_AFTER_S = 1.0;
    /** Slower than this, the encoder counts as not moving. Well under any mechanism's working speed. */
    public static final double MOVING_TICKS_PER_SECOND = 20.0;

    private final String line;
    private long commandedSinceNs = -1;
    private boolean reported;

    /** @param name how the mechanism is called in the event line ({@code intake}, {@code feeder}). */
    public MotionWatch(String name) {
        this.line = name + ": commanded for " + STALL_AFTER_S + " s, encoder never moved"
                + " (not plugged in, no encoder cable, or jammed)";
    }

    /**
     * One loop's check.
     *
     * @param commanded       whether the mechanism is being asked to move this loop
     * @param ticksPerSecond  its encoder speed this loop
     * @param nowNs           the clock, nanoseconds
     * @return the event line the first time it has been commanded for {@link #STALL_AFTER_S}
     *         without moving; null otherwise. Re-arms when the command stops or it moves, so a
     *         mechanism that later jams is reported again.
     */
    public String check(boolean commanded, double ticksPerSecond, long nowNs) {
        if (!commanded || Math.abs(ticksPerSecond) >= MOVING_TICKS_PER_SECOND) {
            commandedSinceNs = -1;
            reported = false;
            return null;
        }
        if (commandedSinceNs < 0) {
            commandedSinceNs = nowNs;
            return null;
        }
        if (!reported && nowNs - commandedSinceNs >= (long) (STALL_AFTER_S * 1e9)) {
            reported = true;
            return line;
        }
        return null;
    }
}
