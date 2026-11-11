package org.firstinspires.ftc.teamcode.vision;

import com.bylazar.configurables.annotations.Configurable;

/**
 * One alliance's HIVE over the match: which way it is, and how many TIPs the camera has confirmed.
 *
 * <p>Only what the camera sees settled moves it. It starts from the match-start position,
 * {@link HiveState#RIGHT_CELL_UP}. Until something is seen its {@link #state()} is
 * {@link HiveState#UNSEEN}, except in an Autonomous: there, at PLAY with the alliance locked, the
 * match-start position is assumed ({@link #assumeMatchStart()}; the manual fixes it), so the HIVE
 * reads RIGHT_CELL_UP, {@link #assumed()}, through the camera's gaps until the camera sees it
 * settle. {@link HiveSubsystem} feeds it every loop:
 * <ul>
 *   <li>A settled state the camera sees on {@link Tuning#minTags} or more tags is taken as it is.
 *       If it differs from the last settled sighting, that is one TIP; if it is the same, none,
 *       however long the HIVE was out of view. A single tag flickers in and out at range, and a
 *       state built on one would vanish and "TIP" every time it did (five phantom TIPs on the
 *       untouched blue HIVE, 24 Oct 2026).</li>
 *   <li>Out of view is {@link HiveState#UNSEEN}, and says nothing about a TIP: anyone can TIP the
 *       HIVE meanwhile, and nobody may have. Tags that vanish used to start a TIP that was assumed
 *       finished {@link Tuning#tipSeconds} later; at the Belding Qualifier (7 Nov 2026, 17 match
 *       logs, both HIVEs) that started 263 TIPs, nearly all as the robot turned or drove away, and
 *       the camera's next settled sighting contradicted 197 of the 236 it could check. The tracker
 *       counted 309 TIPs where settled sightings show 63.</li>
 *   <li>A TIP is seen to start only when a CELL reads mid-tip for {@link Tuning#midTipMs} running
 *       while the robot is steady (see {@link #observe(HiveState, int, double, boolean, long)}).
 *       It reads {@link HiveState#TRANSITION} until the camera sees the HIVE settle, which counts
 *       it if the HIVE settled the other way. With no settled sighting
 *       {@link Tuning#tipSeconds} + {@link Tuning#confirmSeconds} after it started, the HIVE is
 *       UNSEEN again and nothing is counted: a TIP is never assumed to have finished. One frame at
 *       an in-between height is not a TIP: a lowered CELL glimpsed from behind at an odd angle
 *       reads that way (24 Oct 2026).</li>
 *   <li>While the match-start position is assumed, a mid-tip sighting starts nothing: the tracker
 *       has not seen the HIVE settle yet, so the glimpse is more likely the lowered CELL misread.
 *       The next settled sighting puts everything right.</li>
 * </ul>
 *
 * <p>So {@link #tips()} is "at least": two TIPs between two looks leave the HIVE where it was and
 * count as none. Which CELL is up is right from the moment the camera sees the HIVE settle.
 *
 * <p>Only {@link HiveSubsystem} feeds it; everyone else reads it. The assumed match-start position
 * is never used for a position fix: {@code CellFix} reads only what the camera settled on.
 */
public final class HiveTracker {

    /** HIVE timing. */
    @Configurable
    public static class Tuning {
        /**
         * Seconds from the start of a TIP to the HIVE settled the other way; NaN, zero or negative
         * means a TIP seen to start reads TRANSITION until the camera sees the HIVE settle. 3.5:
         * the mock HIVE took 2.5 to 3.5 s to spill 3 POLLEN added onto 3 NECTAR, timed by hand on
         * 24 Oct 2026. HIVE lesson 3 (#129) times it properly, on a real HIVE. Used to draw the
         * TIP in the match log and, with {@link #confirmSeconds}, to give up on one not seen to
         * finish.
         */
        public static double tipSeconds = 3.5;

        /**
         * Seconds past {@link #tipSeconds} the camera has to see the HIVE settle before a TIP seen
         * to start is dropped (the HIVE reads UNSEEN, nothing counted). 1.5: room for the camera's
         * own settle (3 frames over 250 ms) and a slow TIP; it only delays "unknown".
         */
        public static double confirmSeconds = 1.5;

        /**
         * How long the HIVE's tags must be out of the frame before it reads UNSEEN, ms. Long enough
         * to ride out a dropped frame.
         */
        public static double lostAfterMs = 200;

        /** Tags of this HIVE that must be in the frame for a sighting to settle or start a TIP. */
        public static int minTags = 2;

        /** How long a CELL must read mid-tip, ms, before that starts a TIP. */
        public static double midTipMs = 250;
    }

    /** When the current run of mid-tip sightings began; NaN while none. */
    private double midTipSinceMs = Double.NaN;

    private HiveState settled = HiveState.RIGHT_CELL_UP;
    private HiveState state = HiveState.UNSEEN;
    private long tipStartMs;
    private boolean tipping;
    private boolean assumed;
    private int tips;
    private int tipsStarted;

    /** {@link #observe(HiveState, int, double, boolean, long)} with the robot steady. */
    HiveState observe(HiveState seen, int tagsSeen, double lostForMs, long nowMs) {
        return observe(seen, tagsSeen, lostForMs, true, nowMs);
    }

    /**
     * Feeds one loop.
     *
     * <p>A mid-tip sighting counts only while the robot is {@code steady}. Driving or turning, the
     * camera's frame lags the pose it is placed with, so a CELL at rest reads at an in-between
     * height. In 13 of 17 R-Solo runs on 4 and 5 Nov 2026 (20245) a TIP started while the robot
     * drove at 18 to 40 in/s or turned at 60 to 480 &deg;/s, most as it drove off its first launch
     * spot. The cost: a real TIP that happens while the robot drives is not caught mid-tip, only
     * when the camera next sees the HIVE settle.
     *
     * @param seen      what the camera says (see {@link HiveState#of})
     * @param tagsSeen  how many of this HIVE's tags are in the frame behind {@code seen}
     * @param lostForMs ms since a tag of this HIVE was last in a frame; infinite if never
     * @param steady    the robot is slow and hardly turning ({@code HiveSubsystem.Tuning}), so a
     *                  mid-tip sighting can be believed
     * @param nowMs     a monotonic clock, in milliseconds
     * @return the state after this loop
     */
    HiveState observe(HiveState seen, int tagsSeen, double lostForMs, boolean steady, long nowMs) {
        // The camera's settled state outlives the tags a little; out of view is out of view.
        boolean lost = Tuning.lostAfterMs > 0 ? !(lostForMs < Tuning.lostAfterMs) : Double.isInfinite(lostForMs);
        if (lost) seen = HiveState.UNSEEN;
        boolean solid = tagsSeen >= Math.max(1, Tuning.minTags);

        if (seen != HiveState.TRANSITION || !solid || !steady) {
            midTipSinceMs = Double.NaN;
        }

        if (seen.settled()) {
            if (solid) {
                settleAt(seen);
                assumed = false;
            }
            // On too few tags: not trusted; whatever we had stands.
        } else if (seen == HiveState.TRANSITION) {
            if (solid && steady && !assumed) {
                if (Double.isNaN(midTipSinceMs)) midTipSinceMs = nowMs;
                if (tipping || nowMs - midTipSinceMs >= Math.max(0.0, Tuning.midTipMs)) {
                    startTip((long) midTipSinceMs);
                }
            }
        } else if (!tipping && !assumed) {
            state = HiveState.UNSEEN; // out of view says nothing: not a TIP, and not "still there"
        }

        if (tipping && unconfirmed(nowMs)) {
            // Seen to start, never seen to finish: unknown, and nothing counted.
            tipping = false;
            state = HiveState.UNSEEN;
        }
        return state;
    }

    private void startTip(long startMs) {
        if (!tipping) {
            tipping = true;
            tipStartMs = startMs;
            tipsStarted++;
        }
        state = HiveState.TRANSITION;
    }

    private void settleAt(HiveState position) {
        if (position != settled) {
            tips++;
            if (!tipping) tipsStarted++; // it TIPped where the camera could not see it start
        }
        settled = position;
        state = position;
        tipping = false;
    }

    /** The TIP in progress has gone too long without the camera seeing the HIVE settle. */
    private boolean unconfirmed(long nowMs) {
        double extra = Tuning.confirmSeconds > 0 ? Tuning.confirmSeconds : 0.0;
        return Tuning.tipSeconds > 0 && nowMs - tipStartMs >= (Tuning.tipSeconds + extra) * 1000.0;
    }

    public HiveState state() { return state; }

    /** True while the state is the match-start position an Autonomous assumed, not yet seen. */
    public boolean assumed() { return assumed; }

    /**
     * TIPs the camera has confirmed since the match started: each time it saw the HIVE settled the
     * other way from the last time. At least this many happened; an even number between two looks
     * shows as none.
     */
    public int tips() { return tips; }

    /**
     * TIPs <em>started</em> since the match started: counts up the moment a TIP is seen to begin,
     * where {@link #tips()} waits for the camera to see it finish. A TIP seen to start that settles
     * back where it started, or is never seen to finish, still counted here.
     */
    public int tipsStarted() { return tipsStarted; }

    /** A TIP has started and not yet settled. */
    public boolean tipping() { return tipping; }

    /**
     * Where the HIVE last rested as far as the robot knows: the match-start position until the
     * camera sees it settled, then the last settled sighting, held while the HIVE is out of view or
     * tipping. Never
     * {@link HiveState#UNSEEN} or {@link HiveState#TRANSITION}. For drawing the robot's belief in
     * the match log; deciding anything goes through {@link #state()}.
     */
    public HiveState position() { return settled; }

    /**
     * How far through the TIP in progress, 0 to 1, by {@link Tuning#tipSeconds}: 0 when not
     * tipping, and 0.5 throughout when {@code tipSeconds} assumes nothing. {@code nowMs} is on the
     * clock {@link #observe} is fed.
     */
    public double tipProgress(long nowMs) {
        if (!tipping) return 0;
        double ms = Tuning.tipSeconds * 1000.0;
        if (!(ms > 0)) return 0.5;
        return Math.max(0.0, Math.min(1.0, (nowMs - tipStartMs) / ms));
    }

    public boolean rightCellUp() { return state == HiveState.RIGHT_CELL_UP; }

    public boolean leftCellUp() { return state == HiveState.LEFT_CELL_UP; }

    /** The RIGHT CELL is down or on its way down: LEFT_CELL_UP, or a TIP away from RIGHT_CELL_UP. */
    public boolean rightCellDown() {
        return state == HiveState.LEFT_CELL_UP
                || (state == HiveState.TRANSITION && settled == HiveState.RIGHT_CELL_UP);
    }

    /** The LEFT CELL is down or on its way down: RIGHT_CELL_UP, or a TIP away from LEFT_CELL_UP. */
    public boolean leftCellDown() {
        return state == HiveState.RIGHT_CELL_UP
                || (state == HiveState.TRANSITION && settled == HiveState.LEFT_CELL_UP);
    }

    /**
     * An Autonomous's PLAY: the HIVE is where the manual starts it, {@link HiveState#RIGHT_CELL_UP},
     * held as {@link #assumed()} until the camera next sees it settle (which replaces it). Counts
     * no TIP.
     */
    public void assumeMatchStart() {
        settled = HiveState.RIGHT_CELL_UP;
        state = HiveState.RIGHT_CELL_UP;
        assumed = true;
        tipping = false;
        midTipSinceMs = Double.NaN;
    }

    /** Back to the match-start position, nothing seen, no TIPs. */
    void reset() {
        settled = HiveState.RIGHT_CELL_UP;
        state = HiveState.UNSEEN;
        tipping = false;
        assumed = false;
        tips = 0;
        tipsStarted = 0;
        midTipSinceMs = Double.NaN;
    }
}
