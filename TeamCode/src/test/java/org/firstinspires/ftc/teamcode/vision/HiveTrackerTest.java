package org.firstinspires.ftc.teamcode.vision;

import static org.firstinspires.ftc.teamcode.vision.HiveState.LEFT_CELL_UP;
import static org.firstinspires.ftc.teamcode.vision.HiveState.RIGHT_CELL_UP;
import static org.firstinspires.ftc.teamcode.vision.HiveState.TRANSITION;
import static org.firstinspires.ftc.teamcode.vision.HiveState.UNSEEN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.logging.HiveComponents;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HiveTrackerTest {

    private static final double NEVER = Double.POSITIVE_INFINITY;

    private final HiveTracker hive = new HiveTracker();

    /** Every test starts from "TIP time unmeasured" and sets it if it needs one. */
    @Before
    @After
    public void untune() {
        HiveTracker.Tuning.tipSeconds = Double.NaN;
        HiveTracker.Tuning.lostAfterMs = 200;
        HiveTracker.Tuning.minTags = 2;
        HiveTracker.Tuning.midTipMs = 250;
    }

    /** A frame with the HIVE well in view, four tags. */
    private HiveState see(HiveState seen, long nowMs) {
        return hive.observe(seen, 4, 0, true, nowMs);
    }

    /** A frame with the HIVE in view on one tag only. */
    private HiveState glimpse(HiveState seen, long nowMs) {
        return hive.observe(seen, 1, 0, true, nowMs);
    }

    /** A CELL seen mid-tip for long enough to count: two frames {@code midTipMs} apart. */
    private HiveState seeTipping(long fromMs) {
        see(TRANSITION, fromMs);
        return see(TRANSITION, fromMs + (long) HiveTracker.Tuning.midTipMs);
    }

    /** A loop with no tag for {@code lostForMs}. The camera's settled state lingers, as it does. */
    private HiveState lose(HiveState lingering, double lostForMs, boolean stillLooking, long nowMs) {
        return hive.observe(lingering, 0, lostForMs, stillLooking, nowMs);
    }

    @Test
    public void startsUnseenAndTakesWhatItSees() {
        assertEquals(UNSEEN, hive.observe(UNSEEN, 0, NEVER, false, 0));
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
    public void aCellSeenMidTipForLongEnoughIsATipToo() {
        see(RIGHT_CELL_UP, 0);
        assertEquals(RIGHT_CELL_UP, see(TRANSITION, 100)); // one frame proves nothing
        assertEquals(TRANSITION, see(TRANSITION, 350));
        assertTrue(hive.rightCellDown());
        assertFalse(hive.leftCellDown());
    }

    @Test
    public void aGlimpseAtAnInBetweenHeightIsNotATip() {
        see(RIGHT_CELL_UP, 0);
        see(TRANSITION, 100);
        assertEquals(RIGHT_CELL_UP, see(RIGHT_CELL_UP, 200)); // it was a misread, not a TIP
        see(TRANSITION, 300);
        assertEquals(RIGHT_CELL_UP, see(TRANSITION, 400)); // a fresh run, not long enough yet
        assertEquals(0, hive.tipsStarted());
        assertFalse(hive.tipping());
    }

    @Test
    public void aMidTipSeenOnOneTagDoesNotCount() {
        see(RIGHT_CELL_UP, 0);
        glimpse(TRANSITION, 100);
        glimpse(TRANSITION, 400);
        glimpse(TRANSITION, 700);
        assertEquals(RIGHT_CELL_UP, hive.state());
        assertEquals(0, hive.tipsStarted());
    }

    @Test
    public void aTipIsAwayFromTheMatchStartEvenIfTheStartWasNotSeen() {
        seeTipping(0);
        assertTrue(hive.rightCellDown());
    }

    @Test
    public void aSettledStateNeedsMoreThanOneTag() {
        assertEquals(UNSEEN, glimpse(LEFT_CELL_UP, 0));
        assertEquals(UNSEEN, glimpse(LEFT_CELL_UP, 100));
        assertFalse(hive.leftCellUp());
        assertEquals(LEFT_CELL_UP, see(LEFT_CELL_UP, 200));
        // And one tag cannot knock a seen state down either.
        assertEquals(LEFT_CELL_UP, glimpse(RIGHT_CELL_UP, 300));
        assertEquals(1, hive.tips());
    }

    @Test
    public void aHiveOnlyEverSeenOnOneTagCannotTipByVanishing() {
        glimpse(RIGHT_CELL_UP, 0);
        glimpse(RIGHT_CELL_UP, 100);
        assertEquals(UNSEEN, lose(RIGHT_CELL_UP, 300, true, 400)); // the phantom blue TIPs of 24 Oct
        assertEquals(0, hive.tipsStarted());
        assertFalse(hive.tipping());
    }

    @Test
    public void whileAPositionIsAssumedAMidTipSightingStartsNothing() {
        HiveTracker.Tuning.tipSeconds = 1.0;
        see(RIGHT_CELL_UP, 0);
        lose(RIGHT_CELL_UP, 200, true, 200);          // TIP 1 starts
        assertEquals(LEFT_CELL_UP, lose(UNSEEN, 1200, true, 1200)); // assumed
        assertTrue(hive.assumed());
        // The lowered CELL, glimpsed from behind, reads as mid-tip for a while: not a second TIP.
        see(TRANSITION, 1300);
        see(TRANSITION, 1600);
        assertEquals(LEFT_CELL_UP, see(TRANSITION, 2000));
        assertEquals(1, hive.tips());
        assertEquals(1, hive.tipsStarted());
        // Seeing it settled puts everything right, and a real TIP after that counts.
        assertEquals(LEFT_CELL_UP, see(LEFT_CELL_UP, 2100));
        assertFalse(hive.assumed());
        seeTipping(2200);
        assertEquals(2, hive.tipsStarted());
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
        seeTipping(0);
        lose(UNSEEN, 1000, true, 1250);
        assertEquals(LEFT_CELL_UP, lose(UNSEEN, 2000, true, 2250));
        assertFalse(hive.leftCellDown());
    }

    @Test
    public void whatTheCameraSeesOverridesAnAssumption() {
        HiveTracker.Tuning.tipSeconds = 1.0;
        seeTipping(0);
        lose(UNSEEN, 1000, true, 1250);
        assertEquals(RIGHT_CELL_UP, see(RIGHT_CELL_UP, 1350)); // tipped back meanwhile
        assertFalse(hive.assumed());
        assertEquals(2, hive.tips());
    }

    @Test
    public void aTipCountsAsStartedTheMomentItBegins() {
        see(RIGHT_CELL_UP, 0);
        assertEquals(0, hive.tipsStarted());
        lose(RIGHT_CELL_UP, 200, true, 200);
        assertEquals(1, hive.tipsStarted());
        assertTrue(hive.tipping());
        assertEquals(0, hive.tips()); // not finished yet
        see(LEFT_CELL_UP, 2000);
        assertEquals(1, hive.tipsStarted());
        assertFalse(hive.tipping());
        assertEquals(1, hive.tips());
    }

    @Test
    public void aTipNobodySawStartStillCountsAsStarted() {
        see(RIGHT_CELL_UP, 0);
        lose(RIGHT_CELL_UP, 300, false, 300); // looked away
        see(LEFT_CELL_UP, 5000); // it had tipped meanwhile
        assertEquals(1, hive.tipsStarted());
    }

    @Test
    public void resetGoesBackToTheMatchStart() {
        see(LEFT_CELL_UP, 0);
        hive.reset();
        assertEquals(UNSEEN, hive.state());
        assertEquals(0, hive.tips());
        assertEquals(0, hive.tipsStarted());
        seeTipping(10);
        assertTrue(hive.rightCellDown());
    }
    @Test
    public void theBeliefIsHeldOutOfViewAndSwingsThroughATip() {
        HiveTracker.Tuning.tipSeconds = 3.5;
        Alliance red = Alliance.RED;
        assertEquals("starts at the match-start position", RIGHT_CELL_UP, hive.position());
        assertEquals(0.0, HiveComponents.believedRad(red, hive, 0), 1e-12);

        see(LEFT_CELL_UP, 1000);
        double left = HiveComponents.fromBuiltRad(red, LEFT_CELL_UP);
        assertEquals(left, HiveComponents.believedRad(red, hive, 1000), 1e-12);
        assertEquals(UNSEEN, lose(LEFT_CELL_UP, 300, false, 1300));
        assertEquals("out of view: the last position is held", LEFT_CELL_UP, hive.position());
        assertEquals(left, HiveComponents.believedRad(red, hive, 1300), 1e-12);

        see(LEFT_CELL_UP, 2000);
        seeTipping(3000);                                   // the TIP started at 3000
        assertTrue(hive.tipping());
        assertEquals(0.5, hive.tipProgress(3000 + 1750), 1e-9);
        assertEquals("half way through tipSeconds, half way back", left / 2,
                HiveComponents.believedRad(red, hive, 3000 + 1750), 1e-9);
        assertEquals(1.0, hive.tipProgress(99_000), 1e-12);

        HiveTracker.Tuning.tipSeconds = Double.NaN;        // nothing assumed: drawn mid-swing
        assertEquals(0.5, hive.tipProgress(99_000), 1e-12);
    }

    @Test
    public void notTippingHasNoProgress() {
        HiveTracker.Tuning.tipSeconds = 3.5;
        assertEquals(0.0, hive.tipProgress(5000), 0.0);
    }
}
