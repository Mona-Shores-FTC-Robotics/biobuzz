package org.firstinspires.ftc.teamcode.vision;

import org.firstinspires.ftc.teamcode.util.Alliance;

/**
 * Counts the TIPs of one alliance's HIVE since the match started.
 *
 * <p>A HIVE has two stable positions: GARDEN CELL up (how every match starts) or LOADING CELL up.
 * Either CELL's settled state says which: a GARDEN CELL seen UP, or a LOADING CELL seen DOWN,
 * means the GARDEN side is up, and the reverse. Each change of that position is one TIP. A frame
 * where the two CELLs disagree says nothing and is ignored, and so is any CELL not currently
 * settled, so a HIVE mid-tip or out of view never counts.
 *
 * <p>The count starts from the match-start position, so it is right for an Autonomous run from
 * the start of the match. It cannot see a HIVE that tips twice while neither CELL is in view.
 *
 * <p>Auto conditions read it as "the Nth TIP has happened" ({@code HiveTip1}, {@code HiveTip2}):
 * once true, true for the rest of the match, whoever tipped it.
 */
public final class HiveTipCounter {

    private final HiveCell loading;
    private final HiveCell garden;
    private boolean loadingUp;
    private int tips;

    public HiveTipCounter(Alliance alliance) {
        this.loading = HiveCell.loadingCell(alliance);
        this.garden = HiveCell.gardenCell(alliance);
    }

    /** The alliance's LOADING CELL, or null for UNKNOWN. */
    public HiveCell loadingCell() { return loading; }

    /**
     * Feeds the two CELLs' settled states. Call it every time the states may have changed.
     *
     * @return the TIP count after this observation
     */
    public int observe(HiveCellState loadingState, HiveCellState gardenState) {
        boolean saysLoadingUp = loadingState == HiveCellState.UP || gardenState == HiveCellState.DOWN;
        boolean saysGardenUp = loadingState == HiveCellState.DOWN || gardenState == HiveCellState.UP;
        if (saysLoadingUp == saysGardenUp) return tips; // nothing settled, or the CELLs disagree
        if (saysLoadingUp != loadingUp) {
            loadingUp = saysLoadingUp;
            tips++;
        }
        return tips;
    }

    public int tips() { return tips; }

    /** Back to the match-start position: GARDEN CELL up, no TIPs. */
    public void reset() {
        loadingUp = false;
        tips = 0;
    }
}
