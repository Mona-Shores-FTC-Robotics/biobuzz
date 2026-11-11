package org.firstinspires.ftc.teamcode.subsystems;

/** Encoder position hold for SWYFT's full-speed continuous mode, with PWM OFF between corrections. */
public final class SwyftPositionHold {
    public enum Phase { DISABLED, HOLDING, WAIT_STILL, DRIVE, SETTLE, FAULT }
    private Phase phase = Phase.DISABLED;
    private boolean driving;
    private boolean cooling;
    private double driveDirection;
    private long driveStartNs;
    private long offStartNs;
    private long quietStartNs = -1;

    public Phase phase() { return phase; }

    /** Interrupt immediately, preserving the settling interval if a pulse was in flight. */
    public void cancel(long nowNs) {
        if (driving) endDrive(nowNs);
        quietStartNs = -1;
        phase = Phase.DISABLED;
    }

    /**
     * Returns only -1, 0 or +1. Caller turns 0 into PWM disable, never a centre pulse.
     * A correction is at most 175 ms, followed by at least 250 ms OFF and continuous low rate
     * for the requested quiet interval. Entering the tolerance does not erase this cooldown.
     */
    public double power(boolean enabled, double errorDeg, double rateDps, double onMs,
                        double offMs, double toleranceDeg, double stillRateDps,
                        double quietMs, long nowNs) {
        if (!enabled) {
            cancel(nowNs);
            return 0;
        }
        if (!Double.isFinite(errorDeg) || !Double.isFinite(rateDps)
                || !Double.isFinite(onMs) || onMs <= 0 || !Double.isFinite(offMs) || offMs < 0
                || !Double.isFinite(toleranceDeg) || toleranceDeg < 0
                || !Double.isFinite(stillRateDps) || stillRateDps < 0
                || !Double.isFinite(quietMs) || quietMs < 0) {
            cancel(nowNs);
            phase = Phase.FAULT;
            return 0;
        }
        onMs = Math.min(onMs, 175);
        offMs = Math.min(Math.max(offMs, 250), 10_000);
        quietMs = Math.min(Math.max(quietMs, 50), 2000);
        boolean arrived = Math.abs(errorDeg) <= toleranceDeg;
        if (driving) {
            if (!arrived && Math.signum(errorDeg) == driveDirection
                    && (nowNs - driveStartNs) / 1e6 < onMs) return driveDirection;
            endDrive(nowNs);
            phase = Phase.SETTLE;
            return 0;
        }
        if (cooling) {
            if ((nowNs - offStartNs) / 1e6 < offMs) {
                phase = Phase.SETTLE;
                return 0;
            }
            cooling = false;
        }
        if (Math.abs(rateDps) > stillRateDps) quietStartNs = -1;
        else if (quietStartNs < 0) quietStartNs = nowNs;
        if (arrived) {
            phase = Phase.HOLDING;
            return 0;
        }
        if (quietStartNs < 0 || (nowNs - quietStartNs) / 1e6 < quietMs) {
            phase = Phase.WAIT_STILL;
            return 0;
        }
        driving = true;
        driveDirection = Math.signum(errorDeg);
        driveStartNs = nowNs;
        phase = Phase.DRIVE;
        return driveDirection;
    }

    private void endDrive(long nowNs) {
        driving = false;
        cooling = true;
        offStartNs = nowNs;
        quietStartNs = -1;
    }
}
