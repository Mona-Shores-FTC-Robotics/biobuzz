package org.firstinspires.ftc.teamcode.subsystems;

/**
 * Keeps a position-servo turret where its encoder says it should be. Pure, so it is unit tested.
 *
 * <p>Why: the turret's servo senses only its own shaft, within one turn, and it turns the turret
 * through 2.75:1 gearing (the goBILDA turret). Twirl the turret by hand while the servo is limp
 * and the servo comes back out of step by a fraction of its turn: three turret turns are 8.25
 * servo turns, so at PLAY "home" puts the turret about 30° off its mark (19429, 6 Nov 2026). The
 * Cypher Max on the drive gear counts every turn, and is zeroed at PLAY with the turret on its
 * mark, so it knows where the turret really is.
 *
 * <p>How: whenever the turret has been still for {@code stillMs} (the servo has finished moving),
 * the angle the encoder reads is compared with the one asked for, and the error, over
 * {@code degreesPerUnit}, is added to an offset on every position sent. The servo then moves, the
 * turret is no longer still, and the next correction waits for it to settle again; one or two
 * corrections close it. The offset is clamped so the position sent stays inside the limits: an
 * error too big to correct (near half a servo turn out of step) leaves the turret at a limit, and
 * {@link #outOfReach()} says so.
 *
 * <p>The caller passes everything; this keeps only the offset and the still clock.
 */
public final class TurretRehome {

    private double offset;
    private long stillSinceNs;
    private boolean still;
    private boolean outOfReach;
    private int corrections;

    /** Forget everything: a new OpMode, or a new zero. */
    public void reset() {
        offset = 0.0;
        still = false;
        outOfReach = false;
        corrections = 0;
    }

    /**
     * One loop. Returns the position to send: {@code position} plus the offset, inside
     * {@code [lo, hi]}.
     *
     * @param position       the position the angle asked for would be with no offset
     * @param targetDeg      that angle, degrees from home, counter-clockwise positive
     * @param angleDeg       where the encoder says the turret is; NaN skips correcting
     * @param rateDps        the encoder's rate; NaN counts as moving
     * @param degreesPerUnit turret degrees per +1.0 of position; NaN or 0 skips correcting
     * @param lo             lowest position allowed
     * @param hi             highest position allowed
     * @param stillDps       slower than this, the turret counts as still
     * @param stillMs        still this long, the servo has finished: correct
     * @param toleranceDeg   errors this small are left alone
     * @param nowNs          {@code System.nanoTime()}
     */
    public double step(double position, double targetDeg, double angleDeg, double rateDps,
                       double degreesPerUnit, double lo, double hi, double stillDps, double stillMs,
                       double toleranceDeg, long nowNs) {
        boolean usable = !Double.isNaN(angleDeg) && !Double.isNaN(targetDeg) && !Double.isNaN(degreesPerUnit)
                && !Double.isInfinite(degreesPerUnit) && degreesPerUnit != 0.0;
        boolean isStill = !Double.isNaN(rateDps) && Math.abs(rateDps) < stillDps;
        if (!isStill) {
            still = false;
        } else if (!still) {
            still = true;
            stillSinceNs = nowNs;
        }
        if (usable && still && (nowNs - stillSinceNs) / 1e6 >= stillMs) {
            double errorDeg = targetDeg - angleDeg;
            if (Math.abs(errorDeg) > toleranceDeg) {
                double wanted = offset + errorDeg / degreesPerUnit;
                double clamped = Math.max(lo - position, Math.min(hi - position, wanted));
                outOfReach = clamped != wanted;
                if (clamped != offset) {
                    offset = clamped;
                    corrections++;
                }
            } else {
                outOfReach = false;
            }
            // Wait for the servo to move and settle again before the next look.
            stillSinceNs = nowNs;
        }
        return Math.max(lo, Math.min(hi, position + offset));
    }

    /** The offset on every position sent, in position units. */
    public double offset() {
        return offset;
    }

    /** The last correction wanted more than the limits allow: the turret sits at a limit, not home. */
    public boolean outOfReach() {
        return outOfReach;
    }

    /** How many corrections were made since {@link #reset()}. */
    public int corrections() {
        return corrections;
    }
}
