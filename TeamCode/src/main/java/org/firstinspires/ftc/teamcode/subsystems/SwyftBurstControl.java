package org.firstinspires.ftc.teamcode.subsystems;

/**
 * Pulse timing for programmable SWYFT continuous mode: full direction or PWM OFF, never neutral.
 * The caller maps +/-1 to endpoint signals and 0 to per-port PWM disable. No proportional speed,
 * active reverse braking, or centre-offset correction: those assumptions belong to other servos.
 */
public final class SwyftBurstControl {
    private boolean active;
    private double lastDirection;
    private long cycleStartNs;

    public void reset() {
        active = false;
    }

    /**
     * A requested direction (any finite nonzero value) produces bounded full-speed pulses.
     * Invalid timings stop. On time is capped at 100 ms and off time is clamped to 100..10000 ms.
     * A direction reversal starts with a full off interval before driving the other way.
     */
    public double power(double requested, double onMs, double offMs, long nowNs) {
        if (!Double.isFinite(requested) || requested == 0 || !Double.isFinite(onMs)
                || !Double.isFinite(offMs) || onMs <= 0 || offMs < 0) {
            reset();
            return 0;
        }
        onMs = Math.min(onMs, 100);
        offMs = Math.min(Math.max(offMs, 100), 10_000);
        double direction = Math.signum(requested);
        if (!active) {
            active = true;
            lastDirection = direction;
            cycleStartNs = nowNs;
        } else if (direction != lastDirection) {
            lastDirection = direction;
            cycleStartNs = nowNs;
            // Start this new cycle in its pause rather than at a powered endpoint.
            cycleStartNs += (long) (offMs * 1_000_000);
        }
        double elapsedMs = (nowNs - cycleStartNs) / 1_000_000.0;
        if (elapsedMs < 0) return 0;
        if (elapsedMs >= onMs + offMs) {
            cycleStartNs = nowNs;
            elapsedMs = 0;
        }
        return elapsedMs < onMs ? direction : 0;
    }
}
