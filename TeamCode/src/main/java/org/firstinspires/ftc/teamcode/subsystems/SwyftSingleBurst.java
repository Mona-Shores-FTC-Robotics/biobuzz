package org.firstinspires.ftc.teamcode.subsystems;

/** One bounded diagnostic drive per trigger/bumper press; release all requests to rearm. */
public final class SwyftSingleBurst {
    private boolean latched;
    private double direction;
    private long startNs;

    public double power(double requested, double durationMs, long nowNs) {
        if (!Double.isFinite(requested) || requested == 0) {
            latched = false;
            return 0;
        }
        if (!latched) {
            latched = true;
            direction = Math.signum(requested);
            startNs = nowNs;
        }
        // A held request cannot retrigger, change direction, or outlive half a second.
        if (!Double.isFinite(durationMs) || durationMs <= 0 || Math.signum(requested) != direction) return 0;
        double elapsedMs = (nowNs - startNs) / 1_000_000.0;
        return elapsedMs >= 0 && elapsedMs < Math.min(durationMs, 500) ? direction : 0;
    }
}
