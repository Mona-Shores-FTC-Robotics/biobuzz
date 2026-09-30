package org.firstinspires.ftc.teamcode.vision;

import static org.firstinspires.ftc.teamcode.vision.HiveCellState.DOWN;
import static org.firstinspires.ftc.teamcode.vision.HiveCellState.UNKNOWN;
import static org.firstinspires.ftc.teamcode.vision.HiveCellState.UP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class HiveStateTest {

    @Test
    public void eitherSettledCellSaysWhichWayTheHiveIs() {
        assertEquals(HiveState.GARDEN_UP, HiveState.of(DOWN, UP, false));
        assertEquals(HiveState.GARDEN_UP, HiveState.of(UNKNOWN, UP, false));
        assertEquals(HiveState.GARDEN_UP, HiveState.of(DOWN, UNKNOWN, false));
        assertEquals(HiveState.LOADING_UP, HiveState.of(UP, DOWN, false));
        assertEquals(HiveState.LOADING_UP, HiveState.of(UNKNOWN, DOWN, false));
    }

    @Test
    public void aCellSeenBetweenItsRestingHeightsIsATipInProgress() {
        assertEquals(HiveState.TRANSITION, HiveState.of(UNKNOWN, UNKNOWN, true));
    }

    @Test
    public void nothingSeenOrCellsThatDisagreeSayNothing() {
        assertEquals(HiveState.UNSEEN, HiveState.of(UNKNOWN, UNKNOWN, false));
        assertEquals(HiveState.UNSEEN, HiveState.of(UP, UP, false));
        assertEquals(HiveState.UNSEEN, HiveState.of(DOWN, DOWN, true));
    }

    @Test
    public void leftGardenIsMidTipOrLoadingUpAndNeverUnseen() {
        assertTrue(HiveState.TRANSITION.leftGarden());
        assertTrue(HiveState.LOADING_UP.leftGarden());
        assertFalse(HiveState.GARDEN_UP.leftGarden());
        assertFalse(HiveState.UNSEEN.leftGarden());
    }

    @Test
    public void leftLoadingIsTheTipBack() {
        assertTrue(HiveState.TRANSITION.leftLoading());
        assertTrue(HiveState.GARDEN_UP.leftLoading());
        assertFalse(HiveState.LOADING_UP.leftLoading());
        assertFalse(HiveState.UNSEEN.leftLoading());
    }
}
