package org.firstinspires.ftc.teamcode.vision;

import static org.firstinspires.ftc.teamcode.vision.HiveCellState.DOWN;
import static org.firstinspires.ftc.teamcode.vision.HiveCellState.UNKNOWN;
import static org.firstinspires.ftc.teamcode.vision.HiveCellState.UP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

public class HiveStateTest {

    @Test
    public void eitherSettledCellSaysWhichWayTheHiveIs() {
        assertEquals(HiveState.RIGHT_CELL_UP, HiveState.of(DOWN, UP, false));
        assertEquals(HiveState.RIGHT_CELL_UP, HiveState.of(UNKNOWN, UP, false));
        assertEquals(HiveState.RIGHT_CELL_UP, HiveState.of(DOWN, UNKNOWN, false));
        assertEquals(HiveState.LEFT_CELL_UP, HiveState.of(UP, DOWN, false));
        assertEquals(HiveState.LEFT_CELL_UP, HiveState.of(UNKNOWN, DOWN, false));
    }

    @Test
    public void aCellBetweenItsRestingHeightsIsATransition() {
        assertEquals(HiveState.TRANSITION, HiveState.of(UNKNOWN, UNKNOWN, true));
    }

    @Test
    public void nothingSettledOrCellsThatDisagreeAreUnseen() {
        assertEquals(HiveState.UNSEEN, HiveState.of(UNKNOWN, UNKNOWN, false));
        assertEquals(HiveState.UNSEEN, HiveState.of(UP, UP, false));
        assertEquals(HiveState.UNSEEN, HiveState.of(DOWN, DOWN, true));
    }

    @Test
    public void leftAndRightAreAsEachAllianceSeesThem() {
        assertEquals(HiveCell.RED_SCORING, HiveCell.leftCell(Alliance.RED));
        assertEquals(HiveCell.RED_AUDIENCE, HiveCell.rightCell(Alliance.RED));
        assertEquals(HiveCell.BLUE_AUDIENCE, HiveCell.leftCell(Alliance.BLUE));
        assertEquals(HiveCell.BLUE_SCORING, HiveCell.rightCell(Alliance.BLUE));
        assertNull(HiveCell.leftCell(Alliance.UNKNOWN));
        assertNull(HiveCell.rightCell(Alliance.UNKNOWN));
    }
}
