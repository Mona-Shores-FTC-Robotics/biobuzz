package org.firstinspires.ftc.teamcode.util;

/**
 * Says when a slow, optional read or write is due again: at most once per period, and at once
 * after {@link #reset()}.
 *
 * <p>For the hardware calls the bulk read does not cover, such as a motor's current, which cost a
 * round trip to the hub each (2–3 ms on the Expansion Hub). A value only shown and logged does not
 * need one every loop.
 *
 * <p>Plain Java with no SDK types, so it is unit tested. The caller passes the time.
 */
public final class Throttle {

    private final long periodNs;
    private final long offsetNs;
    private long lastNs;
    private boolean armed = true;

    /**
     * @param periodMs at most one per this many milliseconds
     * @param offsetMs how late in each period this one falls, so two throttles with the same
     *                 period, started together, do not land in the same loop
     */
    public Throttle(double periodMs, double offsetMs) {
        this.periodNs = (long) (Math.max(0.0, periodMs) * 1e6);
        this.offsetNs = (long) (Math.max(0.0, offsetMs) * 1e6);
    }

    /** Whether it is due at {@code nowNs}; if so, counts this as the call. */
    public boolean due(long nowNs) {
        if (armed) {
            armed = false;
            if (offsetNs == 0) {
                lastNs = nowNs;
                return true;
            }
            lastNs = nowNs - periodNs + offsetNs; // first due one offset from now
            return false;
        }
        if (nowNs - lastNs < periodNs) return false;
        lastNs = nowNs;
        return true;
    }

    /** The next {@link #due} is true at once (with no offset) or after the offset. */
    public void reset() {
        armed = true;
    }
}
