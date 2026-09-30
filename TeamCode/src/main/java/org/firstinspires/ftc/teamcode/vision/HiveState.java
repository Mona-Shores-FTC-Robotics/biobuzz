package org.firstinspires.ftc.teamcode.vision;

/**
 * Which way one alliance's HIVE is, as the camera sees it now.
 *
 * <p>Every match starts {@link #GARDEN_UP}. A TIP swings it through {@link #TRANSITION} to
 * {@link #LOADING_UP}, and a TIP back returns it. {@link #UNSEEN} means the camera cannot say: no
 * CELL of this HIVE is in view, its geometry is not measured yet, or the CELLs disagree. It is
 * never taken as a TIP.
 */
public enum HiveState {
    GARDEN_UP,
    TRANSITION,
    LOADING_UP,
    UNSEEN;

    /**
     * The HIVE's state from its two CELLs.
     *
     * @param loading    the LOADING CELL's settled state (UNKNOWN when not settled)
     * @param garden     the GARDEN CELL's settled state
     * @param midTipSeen a CELL of this HIVE is in view with its tags at a height between the two
     *                   resting positions, which is what a CELL mid-tip looks like
     */
    public static HiveState of(HiveCellState loading, HiveCellState garden, boolean midTipSeen) {
        boolean loadingUp = loading == HiveCellState.UP || garden == HiveCellState.DOWN;
        boolean gardenUp = garden == HiveCellState.UP || loading == HiveCellState.DOWN;
        if (loadingUp && !gardenUp) return LOADING_UP;
        if (gardenUp && !loadingUp) return GARDEN_UP;
        if (!loadingUp && midTipSeen) return TRANSITION;
        return UNSEEN;
    }

    /** The HIVE has left its match-start position: mid-tip or settled LOADING_UP. */
    public boolean leftGarden() {
        return this == TRANSITION || this == LOADING_UP;
    }

    /** The HIVE has left LOADING_UP: mid-tip or settled back GARDEN_UP. */
    public boolean leftLoading() {
        return this == TRANSITION || this == GARDEN_UP;
    }
}
