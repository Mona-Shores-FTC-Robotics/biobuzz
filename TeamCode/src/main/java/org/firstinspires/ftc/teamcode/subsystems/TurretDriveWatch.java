package org.firstinspires.ftc.teamcode.subsystems;

/**
 * Notices a turret that is being driven and is not doing what it was told. Pure, unit tested.
 *
 * <p>A turret with no stops has no "too far": with a sign wrong the loop drives away from its
 * target at full power for ever, and with the encoder unplugged it drives blind. Neither can be
 * seen in the error, which the shortest way round keeps under half a turn. So this watches the
 * power against the rate the encoder reports:
 *
 * <ul>
 *   <li>{@link Fault#WRONG_WAY}: driven one way, turning the other, for {@code wrongWayMs}. The
 *       servo's sign or the encoder's is wrong.</li>
 *   <li>{@link Fault#NOT_MOVING}: driven hard and not turning for {@code stallMs}. The encoder is
 *       not counting (unplugged, or another mechanism's is in the socket), the servo is not
 *       turning, or the turret is jammed.</li>
 * </ul>
 *
 * <p>The caller drops the hold on a fault. The instance keeps only the two clocks.
 */
public final class TurretDriveWatch {

    public enum Fault { NONE, WRONG_WAY, NOT_MOVING }

    private long wrongSinceNs;
    private boolean wrong;
    private long stalledSinceNs;
    private boolean stalled;

    /** Forget both clocks: a new hold. */
    public void reset() {
        wrong = false;
        stalled = false;
    }

    /**
     * One loop.
     *
     * @param power      the power asked for, counter-clockwise positive
     * @param rateDps    the encoder's rate, counter-clockwise positive; NaN counts as not moving
     * @param drivePower a power at least this big is "driven" for the wrong-way check
     * @param hardPower  a power at least this big is "driven hard" for the not-moving check
     * @param movingDps  a rate at least this big is "turning"
     * @param wrongWayMs how long turning against the power is a fault
     * @param stallMs    how long driven hard without turning is a fault
     * @param nowNs      {@code System.nanoTime()}
     */
    public Fault step(double power, double rateDps, double drivePower, double hardPower, double movingDps,
                      double wrongWayMs, double stallMs, long nowNs) {
        double rate = Double.isNaN(rateDps) ? 0.0 : rateDps;
        boolean turning = Math.abs(rate) >= movingDps;

        boolean against = Math.abs(power) >= drivePower && turning && Math.signum(rate) != Math.signum(power);
        if (!against) {
            wrong = false;
        } else if (!wrong) {
            wrong = true;
            wrongSinceNs = nowNs;
        }

        boolean stuck = Math.abs(power) >= hardPower && !turning;
        if (!stuck) {
            stalled = false;
        } else if (!stalled) {
            stalled = true;
            stalledSinceNs = nowNs;
        }

        if (wrong && (nowNs - wrongSinceNs) / 1e6 >= wrongWayMs) {
            return Fault.WRONG_WAY;
        }
        if (stalled && (nowNs - stalledSinceNs) / 1e6 >= stallMs) {
            return Fault.NOT_MOVING;
        }
        return Fault.NONE;
    }
}
