package org.firstinspires.ftc.teamcode.vision;

import com.bylazar.configurables.annotations.Configurable;

/**
 * One alliance's HIVE over the match: which way it is, which way it is going, and how many times
 * it has TIPped.
 *
 * <p>It starts from the match-start position, {@link HiveState#RIGHT_CELL_UP}, so it is right for
 * an Autonomous run from the start of the match. {@link HiveSubsystem} feeds it every loop:
 * <ul>
 *   <li>A settled state the camera sees is taken as it is.</li>
 *   <li>A TIP starts when a CELL is seen mid-tip, <em>or</em> when a CELL seen settled drops out of
 *       view while the robot keeps looking where it was: a CELL's tags turn away from the camera
 *       as soon as it starts to move. (A robot or POLLEN blocking the view for longer than
 *       {@link Tuning#lostAfterMs} looks the same; if the CELL reappears where it was, the TIP is
 *       cancelled, but a trigger may already have fired.)</li>
 *   <li>A TIP, once started, finishes (the HIVE is built that way). It reads
 *       {@link HiveState#TRANSITION} until the camera sees the HIVE settle, or until
 *       {@link Tuning#tipSeconds} has passed, after which the other CELL is assumed up and held
 *       until the camera next sees the HIVE.</li>
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
         * Seconds from the start of a TIP to the HIVE settled the other way. NaN until measured
         * (film a TIP); NaN, zero or negative assumes nothing, and a TIP ends only when seen.
         */
        public static double tipSeconds = Double.NaN;

        /**
         * How long a settled CELL must be out of view, while the robot keeps looking, before that
         * counts as the start of a TIP, ms. Long enough to ride out a dropped frame; short enough
         * not to delay the Auto. Set on a field.
         */
        public static double lostAfterMs = 200;
    }

    private HiveState settled = HiveState.RIGHT_CELL_UP;
    private HiveState state = HiveState.UNSEEN;
    private long tipStartMs;
    private boolean tipping;
    private boolean assumed;
    private int tips;

    /**
     * Feeds one loop.
     *
     * @param seen         what the camera says (see {@link HiveState#of})
     * @param lostForMs    ms since a tag of this HIVE was last in a frame; infinite if never
     * @param stillLooking the robot has not turned or moved since that frame, so the HIVE would
     *                     still be in view if nothing about it had changed
     * @param nowMs        a monotonic clock, in milliseconds
     * @return the state after this loop
     */
    HiveState observe(HiveState seen, double lostForMs, boolean stillLooking, long nowMs) {
        // The camera's settled state outlives the tags a little; out of view is out of view.
        boolean lost = Tuning.lostAfterMs > 0 ? !(lostForMs < Tuning.lostAfterMs) : Double.isInfinite(lostForMs);
        if (lost) seen = HiveState.UNSEEN;

        if (seen.settled()) {
            settleAt(seen);
            assumed = false;
        } else if (seen == HiveState.TRANSITION) {
            startTip(nowMs);
        } else if (state.settled() && !assumed && stillLooking && !Double.isInfinite(lostForMs)) {
            startTip(nowMs - (long) lostForMs); // it vanished where the robot is still looking
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
        }
        state = HiveState.TRANSITION;
    }

    private void settleAt(HiveState position) {
        if (position != settled) tips++;
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
