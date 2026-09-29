package org.firstinspires.ftc.teamcode.vision;

import static org.firstinspires.ftc.teamcode.vision.HiveCellState.DOWN;
import static org.firstinspires.ftc.teamcode.vision.HiveCellState.UNKNOWN;
import static org.firstinspires.ftc.teamcode.vision.HiveCellState.UP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

public class HiveTipCounterTest {

    private final HiveTipCounter red = new HiveTipCounter(Alliance.RED);

    @Test
    public void theMatchStartPositionIsNotATip() {
        assertEquals(0, red.observe(DOWN, UP));
        assertEquals(0, red.observe(DOWN, UNKNOWN));
        assertEquals(0, red.observe(UNKNOWN, UP));
    }

    @Test
    public void eachChangeOfPositionIsOneTip() {
        assertEquals(1, red.observe(UP, DOWN));
        assertEquals(1, red.observe(UP, DOWN));
        assertEquals(2, red.observe(DOWN, UP));
        assertEquals(3, red.observe(UP, UNKNOWN));
    }

    @Test
    public void eitherCellAloneIsEnough() {
        assertEquals(1, red.observe(UNKNOWN, DOWN));
        assertEquals(2, red.observe(DOWN, UNKNOWN));
    }

    @Test
    public void nothingSettledOrCellsThatDisagreeCountNothing() {
        assertEquals(0, red.observe(UNKNOWN, UNKNOWN));
        assertEquals(0, red.observe(UP, UP));
        assertEquals(0, red.observe(DOWN, DOWN));
        assertEquals(1, red.observe(UP, DOWN));
        assertEquals(1, red.observe(UNKNOWN, UNKNOWN));
    }

    @Test
    public void resetGoesBackToTheMatchStart() {
        red.observe(UP, DOWN);
        red.reset();
        assertEquals(0, red.tips());
        assertEquals(0, red.observe(DOWN, UP));
    }

    @Test
    public void eachAllianceWatchesItsOwnCells() {
        assertEquals(HiveCell.RED_SCORING, HiveCell.loadingCell(Alliance.RED));
        assertEquals(HiveCell.RED_AUDIENCE, HiveCell.gardenCell(Alliance.RED));
        assertEquals(HiveCell.BLUE_AUDIENCE, HiveCell.loadingCell(Alliance.BLUE));
        assertEquals(HiveCell.BLUE_SCORING, HiveCell.gardenCell(Alliance.BLUE));
        assertNull(HiveCell.loadingCell(Alliance.UNKNOWN));
    }
}
