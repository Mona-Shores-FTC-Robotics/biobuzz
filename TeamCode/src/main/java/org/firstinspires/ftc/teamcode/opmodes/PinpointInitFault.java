package org.firstinspires.ftc.teamcode.opmodes;

import java.util.Locale;

/**
 * What an Autonomous's INIT tells the drive team about the Pinpoint.
 *
 * <p>A hard fault ("y pod not detected") is kept once seen: on 1 Nov 2026 it dropped out on and off,
 * and a Pinpoint that is sometimes missing is still a Pinpoint to fix.
 *
 * <p>{@code FAULT_BAD_READ} is not that. It is one I2C read that failed, and the status is
 * {@code READY} again on the next loop (about 15 ms in the 1 Nov logs), with the Auto running fine.
 * Kept like a hard fault, a single dropped read at INIT sat on the warning list until PLAY. So a bad
 * read is shown only while it holds for {@link #BAD_READ_HOLD_MS}, and goes when the Pinpoint reads
 * well again. Every status change still reaches the match log through {@code RobotOpMode}.
 */
final class PinpointInitFault {

    /** A bad read must last this long before INIT warns about it. */
    static final long BAD_READ_HOLD_MS = 500;

    private static final String BAD_READ = "FAULT_BAD_READ";

    private String hardFault;
    private long badReadSinceMs = -1;
    private boolean badReadHeld;

    /**
     * One INIT loop.
     *
     * @param hasHeading whether the drive has a Pinpoint at all
     * @param status     the Pinpoint's status as of its last read ({@code READY}, {@code FAULT_…}), or null
     * @param nowMs      a millisecond clock
     */
    void observe(boolean hasHeading, String status, long nowMs) {
        if (!hasHeading) {
            hardFault = "not found";
            return;
        }
        boolean fault = status != null && status.startsWith("FAULT");
        if (fault && !status.equals(BAD_READ)) {
            if (hardFault == null) {
                hardFault = status.substring("FAULT_".length()).replace('_', ' ').toLowerCase(Locale.US);
            }
        }
        if (fault && status.equals(BAD_READ)) {
            if (badReadSinceMs < 0) badReadSinceMs = nowMs;
            badReadHeld = nowMs - badReadSinceMs >= BAD_READ_HOLD_MS;
        } else {
            badReadSinceMs = -1;
            badReadHeld = false;
        }
    }

    /** The one line for the warning list ("y pod not detected"), or null when there is nothing to fix. */
    String problem() {
        if (hardFault != null) return hardFault;
        return badReadHeld ? "bad read" : null;
    }
}
