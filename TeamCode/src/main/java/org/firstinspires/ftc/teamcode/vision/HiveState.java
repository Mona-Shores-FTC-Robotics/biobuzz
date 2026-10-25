package org.firstinspires.ftc.teamcode.vision;

/**
 * Which way one alliance's HIVE is. LEFT and RIGHT are as that alliance's drivers see them from
 * their alliance area; a HIVE's two CELLs share one axle, so one is up exactly when the other is
 * down.
 *
 * <p>Every match starts {@link #RIGHT_CELL_UP}. A TIP swings it through {@link #TRANSITION} to
 * {@link #LEFT_CELL_UP}, and a TIP back returns it. {@link #UNSEEN} means nothing says which: no
 * CELL of this HIVE is in view, its geometry is not measured yet, or the CELLs disagree. It is
 * never taken as a TIP.
 */
public enum HiveState {
    RIGHT_CELL_UP,
    TRANSITION,
    LEFT_CELL_UP,
    UNSEEN;

    /**
     * What one camera frame says about the HIVE, from its two CELLs.
     *
     * @param left       the LEFT CELL's settled state (UNKNOWN when not settled)
     * @param right      the RIGHT CELL's settled state
     * @param midTipSeen a CELL of this HIVE is in view with its tags at a height between the two
     *                   resting positions, which is what a CELL mid-tip looks like
     */
    public static HiveState of(HiveCellState left, HiveCellState right, boolean midTipSeen) {
        boolean leftUp = left == HiveCellState.UP || right == HiveCellState.DOWN;
        boolean rightUp = right == HiveCellState.UP || left == HiveCellState.DOWN;
        if (leftUp && !rightUp) return LEFT_CELL_UP;
        if (rightUp && !leftUp) return RIGHT_CELL_UP;
        if (!leftUp && midTipSeen) return TRANSITION;
        return UNSEEN;
    }

    /** True for the two resting positions. */
    public boolean settled() {
        return this == RIGHT_CELL_UP || this == LEFT_CELL_UP;
    }

    /** The other resting position; only meaningful for a settled state. */
    HiveState flipped() {
        return this == RIGHT_CELL_UP ? LEFT_CELL_UP : RIGHT_CELL_UP;
    }
}
