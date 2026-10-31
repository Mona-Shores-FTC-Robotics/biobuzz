package org.firstinspires.ftc.teamcode.vision;

import com.bylazar.configurables.annotations.Configurable;

/**
 * One alliance's HIVE over the match: which way it is, which way it is going, and how many times
 * it has TIPped.
 *
 * <p>It starts from the match-start position, {@link HiveState#RIGHT_CELL_UP}, so it is right for
 * an Autonomous run from the start of the match. {@link HiveSubsystem} feeds it every loop:
 * <ul>
 *   <li>A settled state the camera sees on {@link Tuning#minTags} or more tags is taken as it is.
 *       A single tag flickers in and out at range, and a state built on one would vanish and
 *       "TIP" every time it did (five phantom TIPs on the untouched blue HIVE, 24 Oct 2026).</li>
 *   <li>A TIP starts when a CELL is seen mid-tip for {@link Tuning#midTipMs} running, <em>or</em>
 *       when a CELL seen settled drops out of view while the robot keeps looking where it was: a
 *       CELL's tags turn away from the camera as soon as it starts to move. (A robot or POLLEN
 *       blocking the view for longer than {@link Tuning#lostAfterMs} looks the same; if the CELL
 *       reappears where it was, the TIP is cancelled, but a trigger may already have fired.) One
 *       frame at an in-between height is not a TIP: a lowered CELL glimpsed from behind at an odd
 *       angle reads that way, and started a second TIP that put the tracker on the wrong CELL
 *       (24 Oct 2026).</li>
 *   <li>A TIP, once started, finishes (the HIVE is built that way). It reads
 *       {@link HiveState#TRANSITION} until the camera sees the HIVE settle, or until
 *       {@link Tuning#tipSeconds} has passed, after which the other CELL is assumed up and held
 *       until the camera next sees the HIVE.</li>
 *   <li>While a position is assumed rather than seen, a mid-tip sighting starts nothing: the
 *       tracker has not seen the HIVE settle since it guessed, so the glimpse is more likely the
 *       same TIP, or the lowered CELL misread, than a new one. The next settled sighting puts
 *       everything right.</li>
 *   <li>Otherwise, out of view is {@link HiveState#UNSEEN}: a position seen earlier is not
 *       remembered once the robot looks away, because anyone can TIP the HIVE meanwhile.</li>
 * </ul>
 *
 * <p>Only {@link HiveSubsystem} feeds it; everyone else reads it. An assumed position is never used
 * for a position fix: {@code CellFix} reads only what the camera settled on.
 */
public final class HiveTracker {

    /** HIVE timing. */
    @Configurable
    public static class Tuning {
        /**
         * Seconds from the start of a TIP to the HIVE settled the other way; NaN, zero or negative
         * assumes nothing, and a TIP ends only when seen. 3.5: the mock HIVE took 2.5 to 3.5 s to
         * spill 3 POLLEN added onto 3 NECTAR, timed by hand on 24 Oct 2026. This is only the
         * fallback for a TIP the camera does not see finish, so it errs late: assuming the other
         * CELL up while the HIVE is still moving would aim the turret at a CELL in motion. HIVE
         * lesson 3 (#129) times it properly, on a real HIVE.
         */
        public static double tipSeconds = 3.5;

        /**
         * How long a settled CELL must be out of view, while the robot keeps looking, before that
         * counts as the start of a TIP, ms. Long enough to ride out a dropped frame; short enough
         * not to delay the Auto. Set on a field.
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

    /**
     * Feeds one loop.
     *
     * @param seen         what the camera says (see {@link HiveState#of})
     * @param tagsSeen     how many of this HIVE's tags are in the frame behind {@code seen}
     * @param lostForMs    ms since a tag of this HIVE was last in a frame; infinite if never
     * @param stillLooking the robot has not turned or moved since that frame, so the HIVE would
     *                     still be in view if nothing about it had changed
     * @param nowMs        a monotonic clock, in milliseconds
     * @return the state after this loop
     */
    HiveState observe(HiveState seen, int tagsSeen, double lostForMs, boolean stillLooking, long nowMs) {
        // The camera's settled state outlives the tags a little; out of view is out of view.
        boolean lost = Tuning.lostAfterMs > 0 ? !(lostForMs < Tuning.lostAfterMs) : Double.isInfinite(lostForMs);
        if (lost) seen = HiveState.UNSEEN;
        boolean solid = tagsSeen >= Math.max(1, Tuning.minTags);

        if (seen != HiveState.TRANSITION || !solid) {
            midTipSinceMs = Double.NaN;
        }

        if (lost) {
            if (state.settled() && !assumed && stillLooking && !Double.isInfinite(lostForMs)) {
                startTip(nowMs - (long) lostForMs); // it vanished where the robot is still looking
            } else if (!tipping && !assumed) {
                state = HiveState.UNSEEN;
            }
        } else if (seen.settled()) {
            if (solid) {
                settleAt(seen);
                assumed = false;
            }
            // On too few tags: not trusted; whatever we had stands.
        } else if (seen == HiveState.TRANSITION) {
            if (solid && !assumed) {
                if (Double.isNaN(midTipSinceMs)) midTipSinceMs = nowMs;
                if (tipping || nowMs - midTipSinceMs >= Math.max(0.0, Tuning.midTipMs)) {
                    startTip((long) midTipSinceMs);
                }
            }
        } else if (!tipping && !assumed) {
            state = HiveState.UNSEEN;
        }

        if (tipping && tipDone(nowMs)) {
            settleAt(settled.flipped());
            assumed = true;
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

    private boolean tipDone(long nowMs) {
        return Tuning.tipSeconds > 0 && nowMs - tipStartMs >= Tuning.tipSeconds * 1000.0;
    }

    public HiveState state() { return state; }

    /** True when the current state comes from a finished TIP rather than from seeing the HIVE. */
    public boolean assumed() { return assumed; }

    /** TIPs since the match started, seen or assumed. */
    public int tips() { return tips; }

    /**
     * TIPs <em>started</em> since the match started: counts up the moment a TIP begins, where
     * {@link #tips()} waits for it to finish. A TIP that settles back where it started still
     * counted here. The {@code Tip} trigger watches this.
     */
    public int tipsStarted() { return tipsStarted; }

    /** A TIP has started and not yet settled. */
    public boolean tipping() { return tipping; }

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
