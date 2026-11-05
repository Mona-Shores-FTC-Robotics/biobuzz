package org.firstinspires.ftc.teamcode.localization;

/**
 * Whether the Pinpoint has been lost for the rest of the OpMode: a fault that holds for
 * {@link #LOST_AFTER_MS} after PLAY. Pure, so a test can drive it.
 *
 * <p>From every match log of 30 Oct to 5 Nov 2026 (320 runs, 64 bad reads): 52 of the 61 faults
 * while running cleared on the next loop and 9 within 220 ms, all leaving the pose where it should
 * be. Twice a fault held for the whole run (4 Nov, R-Solo 21:26 and the run after it) with y frozen
 * at 223 in: the SDK's driver keeps a coordinate that jumps more than 5000 mm from its last good
 * value, so once its stored value is that far from the device's, every later read is refused and
 * nothing short of a restart clears it, a corner or camera reset included. That run fired at a
 * 136 in range and lost its paths. So a fault this long is not a blip, and the pose is not believed
 * again until the OpMode ends.
 *
 * <p>Before PLAY nothing is decided here: an Autonomous's INIT already warns
 * ({@code PinpointInitFault}), and a restart then is the fix.
 */
public final class PinpointLoss {

    /** A fault after PLAY must hold this long to count: longer than every blip in the logs (220 ms). */
    public static final long LOST_AFTER_MS = 500;

    private long faultSinceMs = -1;
    private String lostStatus;

    /**
     * One loop.
     *
     * @param started whether PLAY has been pressed
     * @param status  the Pinpoint's status as of its last read ({@code READY}, {@code FAULT_…}), or null
     * @param nowMs   a millisecond clock
     */
    public void observe(boolean started, String status, long nowMs) {
        if (lostStatus != null) return;
        boolean fault = started && status != null && status.startsWith("FAULT");
        if (!fault) {
            faultSinceMs = -1;
            return;
        }
        if (faultSinceMs < 0) faultSinceMs = nowMs;
        if (nowMs - faultSinceMs >= LOST_AFTER_MS) lostStatus = status;
    }

    /** True once a fault has held {@link #LOST_AFTER_MS} after PLAY; it stays true. */
    public boolean lost() {
        return lostStatus != null;
    }

    /** The status that lost it ({@code FAULT_BAD_READ}), or null while not lost. */
    public String lostStatus() {
        return lostStatus;
    }
}
