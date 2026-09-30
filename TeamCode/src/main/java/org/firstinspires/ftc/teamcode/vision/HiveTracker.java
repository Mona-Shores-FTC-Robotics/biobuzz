package org.firstinspires.ftc.teamcode.vision;

import com.bylazar.configurables.annotations.Configurable;

/**
 * One alliance's HIVE over the match: which way it is, which way it is going, and how many times
 * it has TIPped.
 *
 * <p>It starts from the match-start position, {@link HiveState#RIGHT_CELL_UP}, so it is right for
 * an Autonomous run from the start of the match. Each camera frame's {@link HiveState} is fed to
 * {@link #observe}:
 * <ul>
 *   <li>A settled state is taken as seen.</li>
 *   <li>A CELL seen mid-tip starts a TIP away from the last settled position. A TIP, once started,
 *       finishes (the HIVE is built that way), so after {@link Tuning#tipSeconds} the tracker
 *       reports the other CELL up without waiting to see it, and holds that until the camera next
 *       sees the HIVE settled. While the TIP runs it reports {@link HiveState#TRANSITION}, in view
 *       or not.</li>
 *   <li>Unseen otherwise is {@link HiveState#UNSEEN}: a position seen earlier is not remembered,
 *       because anyone can TIP the HIVE while the camera looks away.</li>
 * </ul>
 *
 * <p>Only {@link LimelightVisionSubsystem} feeds it; everyone else reads it. An assumed position is
 * never used for a position fix: {@code CellFix} reads only what the camera settled on.
 *
 * <p>Until {@link Tuning#tipSeconds} is measured nothing is assumed: a TIP ends only when the
 * camera sees it settle, and an unseen TIP reads {@link HiveState#UNSEEN}.
 */
public final class HiveTracker {

    /** Measured HIVE facts. */
    @Configurable
    public static class Tuning {
        /**
         * Seconds from a CELL first seen mid-tip to the HIVE settled the other way. NaN until
         * measured (film a TIP); NaN, zero or negative assumes nothing.
         */
        public static double tipSeconds = Double.NaN;
    }

    private HiveState settled = HiveState.RIGHT_CELL_UP;
    private HiveState state = HiveState.UNSEEN;
    private long tipStartMs;
    private boolean tipping;
    private boolean assumed;
    private int tips;

    /**
     * Feeds one camera frame.
     *
     * @param seen  what the frame says (see {@link HiveState#of})
     * @param nowMs a monotonic clock, in milliseconds
     * @return the state after this frame
     */
    HiveState observe(HiveState seen, long nowMs) {
        if (seen.settled()) {
            settleAt(seen);
            assumed = false;
        } else if (seen == HiveState.TRANSITION) {
            if (!tipping) {
                tipping = true;
                tipStartMs = nowMs;
            }
            state = HiveState.TRANSITION;
        } else if (!tipping && !assumed) {
            state = HiveState.UNSEEN;
        }
        if (tipping && tipDone(nowMs)) {
            settleAt(settled.flipped());
            assumed = true;
        } else if (tipping && seen == HiveState.UNSEEN && !tipTimed()) {
            state = HiveState.UNSEEN; // nothing to assume from until the TIP time is measured
        }
        return state;
    }

    private void settleAt(HiveState position) {
        if (position != settled) tips++;
        settled = position;
        state = position;
        tipping = false;
    }

    private static boolean tipTimed() {
        return Tuning.tipSeconds > 0;
    }

    private boolean tipDone(long nowMs) {
        return tipTimed() && nowMs - tipStartMs >= Tuning.tipSeconds * 1000.0;
    }

    public HiveState state() { return state; }

    /** True when the current state comes from a finished TIP rather than from seeing the HIVE. */
    public boolean assumed() { return assumed; }

    /** TIPs since the match started, seen or assumed. */
    public int tips() { return tips; }

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
    }
}
