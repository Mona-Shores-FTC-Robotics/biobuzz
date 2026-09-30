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

    private static final double NEVER = Double.POSITIVE_INFINITY;

    private final HiveTracker hive = new HiveTracker();

    @After
    public void untune() {
        HiveTracker.Tuning.tipSeconds = Double.NaN;
        HiveTracker.Tuning.lostAfterMs = 200;
    }

    /** A frame with the HIVE in view. */
    private HiveState see(HiveState seen, long nowMs) {
        return hive.observe(seen, 0, true, nowMs);
    }

    /** A loop with no tag for {@code lostForMs}. The camera's settled state lingers, as it does. */
    private HiveState lose(HiveState lingering, double lostForMs, boolean stillLooking, long nowMs) {
        return hive.observe(lingering, lostForMs, stillLooking, nowMs);
    }

    @Test
    public void startsUnseenAndTakesWhatItSees() {
        assertEquals(UNSEEN, hive.observe(UNSEEN, NEVER, false, 0));
        assertEquals(RIGHT_CELL_UP, see(RIGHT_CELL_UP, 0));
        assertTrue(hive.rightCellUp());
        assertTrue(hive.leftCellDown());
        assertFalse(hive.rightCellDown());
        assertEquals(0, hive.tips());
    }

    @Test
    public void tagsThatVanishWhileTheRobotKeepsLookingAreATip() {
        see(RIGHT_CELL_UP, 1000);
        assertEquals(RIGHT_CELL_UP, lose(RIGHT_CELL_UP, 100, true, 1100)); // a dropped frame
        assertEquals(TRANSITION, lose(RIGHT_CELL_UP, 200, true, 1200));
        assertTrue(hive.rightCellDown());
        assertFalse(hive.leftCellUp());
        assertEquals(TRANSITION, lose(UNSEEN, 5000, false, 6000)); // turning away now changes nothing
        assertEquals(LEFT_CELL_UP, see(LEFT_CELL_UP, 7000));
        assertEquals(1, hive.tips());
    }

    @Test
    public void tagsLostBecauseTheRobotTurnedAwayAreJustUnseen() {
        see(RIGHT_CELL_UP, 1000);
        assertEquals(UNSEEN, lose(RIGHT_CELL_UP, 300, false, 1300));
        assertFalse(hive.rightCellDown());
        assertEquals(UNSEEN, lose(UNSEEN, 900, true, 1900)); // looking back at nothing says nothing
        assertFalse(hive.rightCellDown());
    }

    @Test
    public void aCellThatReappearsWhereItWasCancelsTheTip() {
        see(RIGHT_CELL_UP, 0);
        lose(RIGHT_CELL_UP, 300, true, 300); // a robot drove through the view
        assertEquals(RIGHT_CELL_UP, see(RIGHT_CELL_UP, 600));
        assertFalse(hive.rightCellDown());
        assertEquals(0, hive.tips());
    }

    @Test
    public void aCellSeenMidTipIsATipToo() {
        see(RIGHT_CELL_UP, 0);
        assertEquals(TRANSITION, see(TRANSITION, 100));
        assertTrue(hive.rightCellDown());
        assertFalse(hive.leftCellDown());
    }

    @Test
    public void aTipIsAwayFromTheMatchStartEvenIfTheStartWasNotSeen() {
        see(TRANSITION, 0);
        assertTrue(hive.rightCellDown());
    }

    @Test
    public void theTipBackIsAwayFromTheLeft() {
        see(LEFT_CELL_UP, 0);
        lose(LEFT_CELL_UP, 250, true, 250);
        assertTrue(hive.leftCellDown());
        assertFalse(hive.rightCellDown());
        see(RIGHT_CELL_UP, 2000);
        assertEquals(2, hive.tips());
    }

    @Test
    public void withoutAMeasuredTipTimeATipLastsUntilSeen() {
        see(RIGHT_CELL_UP, 0);
        lose(RIGHT_CELL_UP, 200, true, 200);
        assertEquals(TRANSITION, lose(UNSEEN, 60_000, true, 60_000));
        assertFalse(hive.assumed());
        assertEquals(0, hive.tips());
    }

    @Test
    public void aStartedTipFinishesTheMeasuredTimeAfterTheTagsVanished() {
        HiveTracker.Tuning.tipSeconds = 1.5;
        see(RIGHT_CELL_UP, 1000);
        lose(RIGHT_CELL_UP, 200, true, 1200); // vanished at 1000
        assertEquals(TRANSITION, lose(UNSEEN, 1400, true, 2400));
        assertEquals(LEFT_CELL_UP, lose(UNSEEN, 1500, true, 2500));
        assertTrue(hive.assumed());
        assertTrue(hive.leftCellUp());
        assertEquals(LEFT_CELL_UP, lose(UNSEEN, 8000, false, 9000)); // held while out of view
        assertEquals(1, hive.tips());
        assertEquals(LEFT_CELL_UP, see(LEFT_CELL_UP, 9020));
        assertFalse(hive.assumed());
        assertEquals(1, hive.tips());
    }

    @Test
    public void anAssumedPositionDoesNotVanish() {
        HiveTracker.Tuning.tipSeconds = 1.0;
        see(TRANSITION, 0);
        lose(UNSEEN, 1000, true, 1000);
        assertEquals(LEFT_CELL_UP, lose(UNSEEN, 2000, true, 2000));
        assertFalse(hive.leftCellDown());
    }

    @Test
    public void whatTheCameraSeesOverridesAnAssumption() {
        HiveTracker.Tuning.tipSeconds = 1.0;
        see(TRANSITION, 0);
        lose(UNSEEN, 1000, true, 1000);
        assertEquals(RIGHT_CELL_UP, see(RIGHT_CELL_UP, 1100)); // tipped back meanwhile
        assertFalse(hive.assumed());
        assertEquals(2, hive.tips());
    }

    @Test
    public void resetGoesBackToTheMatchStart() {
        see(LEFT_CELL_UP, 0);
        hive.reset();
        assertEquals(UNSEEN, hive.state());
        assertEquals(0, hive.tips());
        see(TRANSITION, 10);
        assertTrue(hive.rightCellDown());
    }
}
