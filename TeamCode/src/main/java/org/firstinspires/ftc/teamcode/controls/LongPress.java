package org.firstinspires.ftc.teamcode.controls;

/**
 * A button that does something only when held for a while: once per hold, at the moment the hold
 * reaches {@code holdMs}. A tap does nothing. For an action that would hurt if done by accident,
 * such as resetting field-centric forward mid-match.
 */
public final class LongPress {

    private final long holdNs;
    private long downSinceNs;
    private boolean down;
    private boolean fired;

    public LongPress(long holdMs) {
        this.holdNs = holdMs * 1_000_000L;
    }

    /** One step, every loop. True on the one loop the hold reaches its length. */
    public boolean update(boolean held, long nowNs) {
        if (!held) {
            down = false;
            fired = false;
            return false;
        }
        if (!down) {
            down = true;
            downSinceNs = nowNs;
        }
        if (!fired && nowNs - downSinceNs >= holdNs) {
            fired = true;
            return true;
        }
        return false;
    }

    /** Held, not yet long enough: for a "keep holding" hint. */
    public boolean pending() {
        return down && !fired;
    }
}
