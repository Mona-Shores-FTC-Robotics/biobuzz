package org.firstinspires.ftc.teamcode.vision;

import static org.firstinspires.ftc.teamcode.vision.HiveState.LEFT_CELL_UP;
import static org.firstinspires.ftc.teamcode.vision.HiveState.RIGHT_CELL_UP;
import static org.firstinspires.ftc.teamcode.vision.HiveState.TRANSITION;
import static org.firstinspires.ftc.teamcode.vision.HiveState.UNSEEN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;

public class HiveTrackerTest {

    private final HiveTracker hive = new HiveTracker();

    @After
    public void unmeasure() {
        HiveTracker.Tuning.tipSeconds = Double.NaN;
    }

    @Test
    public void startsUnseenAndTakesWhatItSees() {
        assertEquals(UNSEEN, hive.state());
        assertEquals(RIGHT_CELL_UP, hive.observe(RIGHT_CELL_UP, 0));
        assertTrue(hive.rightCellUp());
        assertTrue(hive.leftCellDown());
        assertFalse(hive.rightCellDown());
        assertEquals(0, hive.tips());
    }

    @Test
    public void aSettledPositionIsNotRememberedOutOfView() {
        hive.observe(LEFT_CELL_UP, 0);
        assertEquals(UNSEEN, hive.observe(UNSEEN, 20));
        assertFalse(hive.leftCellUp());
    }

    @Test
    public void theRightCellIsDownFromTheMomentTheTipStarts() {
        hive.observe(RIGHT_CELL_UP, 0);
        assertEquals(TRANSITION, hive.observe(TRANSITION, 100));
        assertTrue(hive.rightCellDown());
        assertFalse(hive.leftCellUp());
        assertFalse(hive.leftCellDown());
        assertEquals(LEFT_CELL_UP, hive.observe(LEFT_CELL_UP, 900));
        assertTrue(hive.leftCellUp());
        assertEquals(1, hive.tips());
    }

    @Test
    public void aTipIsAwayFromTheMatchStartEvenIfTheStartWasNotSeen() {
        hive.observe(TRANSITION, 0);
        assertTrue(hive.rightCellDown());
    }

    @Test
    public void theTipBackIsAwayFromTheLeft() {
        hive.observe(LEFT_CELL_UP, 0);
        hive.observe(TRANSITION, 100);
        assertTrue(hive.leftCellDown());
        assertFalse(hive.rightCellDown());
        hive.observe(RIGHT_CELL_UP, 900);
        assertEquals(2, hive.tips());
    }

    @Test
    public void withoutAMeasuredTipTimeNothingIsAssumed() {
        hive.observe(TRANSITION, 0);
        assertEquals(TRANSITION, hive.observe(TRANSITION, 60_000));
        assertEquals(UNSEEN, hive.observe(UNSEEN, 60_020));
        assertFalse(hive.assumed());
        assertEquals(0, hive.tips());
    }

    @Test
    public void aStartedTipFinishesAfterTheMeasuredTime() {
        HiveTracker.Tuning.tipSeconds = 1.5;
        hive.observe(RIGHT_CELL_UP, 0);
        hive.observe(TRANSITION, 1000);
        assertEquals(TRANSITION, hive.observe(UNSEEN, 2000)); // out of view mid-tip: still tipping
        assertEquals(LEFT_CELL_UP, hive.observe(UNSEEN, 2500));
        assertTrue(hive.assumed());
        assertTrue(hive.leftCellUp());
        assertEquals(LEFT_CELL_UP, hive.observe(UNSEEN, 9000)); // held until the camera says otherwise
        assertEquals(1, hive.tips());
        assertEquals(LEFT_CELL_UP, hive.observe(LEFT_CELL_UP, 9020));
        assertFalse(hive.assumed());
        assertEquals(1, hive.tips());
    }

    @Test
    public void whatTheCameraSeesOverridesAnAssumption() {
        HiveTracker.Tuning.tipSeconds = 1.0;
        hive.observe(TRANSITION, 0);
        hive.observe(UNSEEN, 1000);
        assertEquals(RIGHT_CELL_UP, hive.observe(RIGHT_CELL_UP, 1100)); // tipped back meanwhile
        assertFalse(hive.assumed());
        assertEquals(2, hive.tips());
    }

    @Test
    public void aTipThatSettlesBackWhereItStartedIsNotATip() {
        HiveTracker.Tuning.tipSeconds = 1.0;
        hive.observe(RIGHT_CELL_UP, 0);
        hive.observe(TRANSITION, 100);
        assertEquals(RIGHT_CELL_UP, hive.observe(RIGHT_CELL_UP, 400));
        assertEquals(RIGHT_CELL_UP, hive.observe(RIGHT_CELL_UP, 5000));
        assertEquals(0, hive.tips());
    }

    @Test
    public void resetGoesBackToTheMatchStart() {
        hive.observe(LEFT_CELL_UP, 0);
        hive.reset();
        assertEquals(UNSEEN, hive.state());
        assertEquals(0, hive.tips());
        hive.observe(TRANSITION, 10);
        assertTrue(hive.rightCellDown());
    }
}
