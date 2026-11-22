package org.firstinspires.ftc.teamcode.subsystems;

import java.util.Locale;

/**
 * Finds the turret's home mark from its home switch: a REV Magnetic Limit Switch on the chassis
 * and a magnet on the back of the turret, over the switch when the turret is on its mark
 * ({@code DeviceNames.TURRET_HOME}, 19429). Pure: it is fed the switch and the encoder count once a
 * loop, so a test can run any pass.
 *
 * <p>A Hall switch is on over a band a few degrees wide, not at a point, so one reading only says
 * "somewhere in the band". A <b>pass</b> is what fixes the mark: the turret enters the band on one
 * side and leaves it on the other, still turning the same way, and the mark is the middle of the
 * two edges ({@link #markCounts()}), plus the band's measured offset from the mark. The switch and
 * the encoder come from the same bulk read, so an edge lies between the last loop's count and
 * this loop's: each edge is taken as the midpoint of the two, which is at most half a loop's
 * travel off, and the two edges' errors mostly cancel.
 *
 * <p>A pass is not taken when:
 * <ul>
 *   <li>the turret turned back inside the band and left the side it came in (no far edge);</li>
 *   <li>it crossed either edge faster than the limit given (a loop's travel is too long to place
 *       the edge);</li>
 *   <li>the band read narrower or wider than the limits given (a flicker, or the switch stuck on).</li>
 * </ul>
 */
public final class TurretHomeSwitch {

    /** What the last step found. */
    public enum Result {
        /** Nothing new. */
        NONE,
        /** A pass: {@link #markCounts()}, {@link #bandDeg()} and {@link #passDirection()} are this pass's. */
        PASS,
        /** The turret left the band but the pass was not taken; {@link #rejectReason()} says why. */
        REJECTED
    }

    private boolean known;
    private boolean wasOn;
    private long lastCounts;

    private boolean inBand;
    /** The entry edge, counts (the midpoint of the loop before and the loop it was seen). */
    private double entryCounts;
    /** +1 entering counter-clockwise, -1 clockwise, 0 when the turret was not seen turning. */
    private int entryDirection;
    private boolean entryTooFast;

    private double markCounts = Double.NaN;
    private double bandDeg = Double.NaN;
    private int passDirection;
    private int passes;
    private String rejectReason;

    /** Forget everything: the next reading starts afresh (a new OpMode). */
    public void reset() {
        known = false;
        inBand = false;
        markCounts = Double.NaN;
        bandDeg = Double.NaN;
        passDirection = 0;
        passes = 0;
        rejectReason = null;
    }

    /**
     * One loop.
     *
     * @param magnet         the switch sees the magnet now
     * @param counts         the turret encoder's count now, the hub's own sign
     * @param encoder        counts to degrees; null makes every step {@link Result#NONE}
     * @param rateDps        the turret's rate now, degrees per second counter-clockwise
     * @param maxRateDps     an edge crossed faster than this is not placed; NaN or 0 for no limit
     * @param minBandDeg     a band narrower than this is a flicker
     * @param maxBandDeg     a band wider than this is the switch stuck on, or the turret parked in it
     * @param markOffsetDeg  where the mark is from the band's middle, degrees counter-clockwise
     */
    public Result step(boolean magnet, long counts, TurretEncoder encoder, double rateDps, double maxRateDps,
                       double minBandDeg, double maxBandDeg, double markOffsetDeg) {
        if (encoder == null) {
            known = false;
            inBand = false;
            return Result.NONE;
        }
        if (!known) {
            // The first reading: a turret already in the band has no entry edge, so no pass yet.
            known = true;
            wasOn = magnet;
            lastCounts = counts;
            inBand = false;
            return Result.NONE;
        }
        double edgeCounts = (lastCounts + (double) counts) / 2.0;
        int moving = movingDirection(encoder, counts - lastCounts, rateDps);
        boolean tooFast = maxRateDps > 0.0 && !Double.isNaN(rateDps) && Math.abs(rateDps) > maxRateDps;
        Result result = Result.NONE;
        if (magnet && !wasOn) {
            inBand = true;
            entryCounts = edgeCounts;
            entryDirection = moving;
            entryTooFast = tooFast;
        } else if (!magnet && wasOn && inBand) {
            inBand = false;
            double widthDeg = Math.abs(edgeCounts - entryCounts) * encoder.degreesPerCount();
            if (entryDirection == 0 || moving != entryDirection) {
                result = reject("turned back inside the band");
            } else if (entryTooFast || tooFast) {
                result = reject("crossed an edge faster than " + Math.round(maxRateDps) + "°/s");
            } else if (widthDeg < minBandDeg) {
                result = reject(String.format(Locale.US, "band %.1f° wide, under %.1f°: a flicker",
                        widthDeg, minBandDeg));
            } else if (widthDeg > maxBandDeg) {
                result = reject(String.format(Locale.US, "band %.1f° wide, over %.1f°",
                        widthDeg, maxBandDeg));
            } else {
                double middle = (entryCounts + edgeCounts) / 2.0;
                double offsetCounts = Double.isNaN(markOffsetDeg) ? 0.0
                        : markOffsetDeg / (encoder.direction * encoder.degreesPerCount());
                markCounts = middle + offsetCounts;
                bandDeg = widthDeg;
                passDirection = entryDirection;
                passes++;
                rejectReason = null;
                result = Result.PASS;
            }
        }
        wasOn = magnet;
        lastCounts = counts;
        return result;
    }

    private Result reject(String why) {
        rejectReason = why;
        return Result.REJECTED;
    }

    /** +1 counter-clockwise, -1 clockwise, 0 still: from the count's change, else the rate. */
    private static int movingDirection(TurretEncoder encoder, long deltaCounts, double rateDps) {
        if (deltaCounts != 0) {
            return encoder.degrees(deltaCounts, 0) > 0.0 ? 1 : -1;
        }
        if (Double.isNaN(rateDps) || rateDps == 0.0) return 0;
        return rateDps > 0.0 ? 1 : -1;
    }

    /** The mark's count from the last pass, the hub's sign; NaN before one. */
    public double markCounts() {
        return markCounts;
    }

    /** The last pass's band width, degrees; NaN before one. */
    public double bandDeg() {
        return bandDeg;
    }

    /** The last pass's direction: +1 counter-clockwise, -1 clockwise, 0 before one. */
    public int passDirection() {
        return passDirection;
    }

    /** Passes taken since {@link #reset()}. */
    public int passes() {
        return passes;
    }

    /** Why the last pass was not taken, or null. */
    public String rejectReason() {
        return rejectReason;
    }

    /** Whether the switch saw the magnet at the last step. */
    public boolean magnet() {
        return known && wasOn;
    }
}
