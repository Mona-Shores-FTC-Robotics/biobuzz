package org.firstinspires.ftc.teamcode.subsystems;

/**
 * One launcher wheel's encoder check: cut a wheel whose encoder is silent, and try it again.
 * Pure, one per wheel, so a test drives a cable that drops out and comes back.
 *
 * <p>A velocity loop with no encoder feedback drives its motor to full power, so a commanded wheel
 * that reads no speed for {@code faultAfterMs} <b>without a break</b> is cut (commanded to zero).
 * A single zero reading in a running wheel is not that: the silence has to last.
 *
 * <p>A cut wheel is tried again every {@code retryAfterMs}: commanded for another
 * {@code faultAfterMs}, restored if it reads speed, cut again if it does not. Until it reads speed
 * the fault stays reported. Why: at the Belding Qualifier (7 Nov 2026) a wheel's reading dropped
 * out once in a match on each robot, the wheel stayed cut until the launcher next happened to be
 * spun down, and nothing could be launched for 29 s and 23 s (#496). A wheel whose cable really is
 * out is driven for {@code faultAfterMs} in every {@code retryAfterMs + faultAfterMs}.
 */
final class EncoderWatch {

    /** Cut: commanded to zero until the next retry. */
    private boolean cut;
    /** Cut at least once and not yet seen moving since: still reported. */
    private boolean unproven;
    /** When the wheel last moved, or its command (or this try) began. */
    private long quietSinceNs = -1;
    private long cutNs;
    private int retries;

    /** The command starts over: spin-up, the end of an unjam, a spin-down. Clears any fault. */
    void restart(long nowNs) {
        cut = false;
        unproven = false;
        quietSinceNs = nowNs;
    }

    /**
     * One step, every loop the wheel is asked to spin forwards.
     *
     * @param target       the wheel's target, ticks per second
     * @param measured     its measured speed
     * @param faultAfterMs unbroken silence that cuts it
     * @param silentFloor  below this speed a wheel is "not moving"
     * @param retryAfterMs how long a cut wheel waits before it is tried again; 0, negative or NaN
     *                     never retries (the rule before #496: cut until {@link #restart})
     */
    void update(double target, double measured, long nowNs,
                double faultAfterMs, double silentFloor, double retryAfterMs) {
        if (quietSinceNs < 0) quietSinceNs = nowNs;
        if (cut) {
            if (retryAfterMs > 0.0 && (nowNs - cutNs) / 1e6 >= retryAfterMs) {
                cut = false;
                quietSinceNs = nowNs;
                retries++;
            }
            return;
        }
        if (!(target > 0.0) || Math.abs(measured) >= silentFloor) {
            quietSinceNs = nowNs;
            if (target > 0.0) unproven = false;
            return;
        }
        if (LauncherSubsystem.encoderSilent(target, measured, (nowNs - quietSinceNs) / 1e6,
                faultAfterMs, silentFloor)) {
            cut = true;
            unproven = true;
            cutNs = nowNs;
        }
    }

    /** The wheel must be commanded to zero this loop. */
    boolean cut() {
        return cut;
    }

    /** Report the fault: cut, or being tried again and not yet seen moving. */
    boolean fault() {
        return cut || unproven;
    }

    /** How many times a cut wheel has been tried again this OpMode. */
    int retries() {
        return retries;
    }
}
