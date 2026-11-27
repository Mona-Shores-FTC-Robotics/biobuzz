package org.firstinspires.ftc.teamcode.localization;

import com.pedropathing.math.Pose;

import java.util.Locale;

/**
 * Keeps a camera still fix off the pose while a volley is feeding, and hands it back when the
 * volley ends ({@link LocalizationTuning#holdFixesWhileFeeding}). A fix that moves the pose under a
 * volley makes Pedro's hold drive the robot after it, and moves the turret's aim with it: Fruitport
 * Q-42 (20245), a 2.5 in fix 0.4 s into the first LEFT volley, the robot dragged 2 in while the
 * first ball left. Still beats exact during a volley.
 *
 * <p>Each loop, {@link #observe} first, with whether the gate is open; a fix that passes while
 * {@link #holding()} goes to {@link #hold} instead of the filter. When the feed ends, {@link #observe}
 * returns the release: the newest held fix, to apply stamped with the last feeding loop's time, or
 * dropped if the pose moved more than {@link LocalizationTuning#heldFixMaxMoveIn} or turned more than
 * {@link LocalizationTuning#heldFixMaxTurnDeg} since its frames (the robot was pushed, or driven
 * during the feed): then the next stop takes a fresh fix. No hardware: tested on its own.
 */
public final class VolleyFixHold {

    /** What to do with a held fix when the volley ends. */
    public static final class Release {
        /** Whether to put {@link #fix} into the filter; false: dropped. */
        public final boolean apply;
        /** The fix, x and y (heading NaN: position only). */
        public final Pose fix;
        public final Pose variance;
        /** The time to stamp it with: the last feeding loop's, when the robot was where its frames saw it. */
        public final long stampNs;
        /** The pose at the volley's end, for the shadows' "before". */
        public final Pose at;
        /** One line for the match log. */
        public final String note;

        Release(boolean apply, Pose fix, Pose variance, long stampNs, Pose at, String note) {
            this.apply = apply;
            this.fix = fix;
            this.variance = variance;
            this.stampNs = stampNs;
            this.at = at;
            this.note = note;
        }
    }

    private boolean feeding;
    private Pose heldFix;
    private Pose heldVariance;
    private long heldCaptureNs;
    private Pose heldAtPose;
    private Pose lastFeedingPose;
    private long lastFeedingNs;
    private int held;
    private int dropped;

    /**
     * Call once a loop, before any fix is offered: {@code feedingNow}, whether the gate is open;
     * {@code pose}, the pose now. Returns the release when a feed has just ended with a fix held,
     * else null.
     */
    public Release observe(boolean feedingNow, Pose pose, long nowNs) {
        Release release = null;
        if (feedingNow) {
            lastFeedingPose = pose;
            lastFeedingNs = nowNs;
        } else if (feeding && heldFix != null) {
            Pose at = lastFeedingPose != null ? lastFeedingPose : pose;
            double movedIn = Math.hypot(at.x() - heldAtPose.x(), at.y() - heldAtPose.y());
            double turnedDeg = Math.abs(Math.toDegrees(
                    Math.IEEEremainder(at.heading() - heldAtPose.heading(), 2 * Math.PI)));
            double jumpIn = Math.hypot(heldFix.x() - at.x(), heldFix.y() - at.y());
            if (movedIn <= LocalizationTuning.heldFixMaxMoveIn && turnedDeg <= LocalizationTuning.heldFixMaxTurnDeg) {
                release = new Release(true, heldFix, heldVariance, lastFeedingNs > 0 ? lastFeedingNs : heldCaptureNs, at,
                        String.format(Locale.US, "still fix %d held through the volley, applied as it ended: moved the pose %.1f in",
                                held, jumpIn));
            } else {
                dropped++;
                release = new Release(false, heldFix, heldVariance, heldCaptureNs, at,
                        String.format(Locale.US, "still fix %d held through the volley, dropped: the robot moved %.1f in"
                                + " and turned %.1f° since its frames", held, movedIn, turnedDeg));
            }
            heldFix = null;
        }
        feeding = feedingNow;
        return release;
    }

    /** Whether a fix that passes now should be held: a volley is feeding and holding is on. */
    public boolean holding() {
        return feeding && LocalizationTuning.holdFixesWhileFeeding;
    }

    /** Holds a passed fix until the feed ends; a later one replaces it (the newest frames win). */
    public void hold(double x, double y, Pose variance, long captureNs, Pose pose) {
        heldFix = new Pose(x, y, Double.NaN);
        heldVariance = variance;
        heldCaptureNs = captureNs;
        heldAtPose = pose;
        held++;
    }

    /** Fixes held so far, and how many of them were dropped. */
    public int held() { return held; }

    public int dropped() { return dropped; }
}
