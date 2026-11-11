package org.firstinspires.ftc.teamcode.vision;

import static org.firstinspires.ftc.teamcode.vision.HiveState.LEFT_CELL_UP;
import static org.firstinspires.ftc.teamcode.vision.HiveState.RIGHT_CELL_UP;
import static org.firstinspires.ftc.teamcode.vision.HiveState.TRANSITION;
import static org.firstinspires.ftc.teamcode.vision.HiveState.UNSEEN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pedropathing.math.Pose;

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
        HiveTracker.Tuning.confirmSeconds = 1.5;
        HiveTracker.Tuning.lostAfterMs = 200;
        HiveTracker.Tuning.minTags = 2;
        HiveTracker.Tuning.midTipMs = 250;
    }

    /** A frame with the HIVE well in view, four tags. */
    private HiveState see(HiveState seen, long nowMs) {
        return hive.observe(seen, 4, 0, nowMs);
    }

    /** A frame with the HIVE in view on one tag only. */
    private HiveState glimpse(HiveState seen, long nowMs) {
        return hive.observe(seen, 1, 0, nowMs);
    }

    /** A CELL seen mid-tip for long enough to count: two frames {@code midTipMs} apart. */
    private HiveState seeTipping(long fromMs) {
        see(TRANSITION, fromMs);
        return see(TRANSITION, fromMs + (long) HiveTracker.Tuning.midTipMs);
    }

    /** A loop with no tag for {@code lostForMs}. The camera's settled state lingers, as it does. */
    private HiveState lose(HiveState lingering, double lostForMs, long nowMs) {
        return hive.observe(lingering, 0, lostForMs, nowMs);
    }

    /** An Autonomous's PLAY: the match-start position, assumed, held through the camera's gaps. */
    @Test
    public void theMatchStartIsAssumedUntilTheCameraSettlesIt() {
        assertEquals(UNSEEN, hive.observe(UNSEEN, 0, NEVER, 0));
        hive.assumeMatchStart();
        assertEquals(RIGHT_CELL_UP, hive.state());
        assertTrue(hive.rightCellUp());
        assertTrue(hive.assumed());
        assertEquals(RIGHT_CELL_UP, lose(UNSEEN, NEVER, 100));
        assertEquals(RIGHT_CELL_UP, lose(UNSEEN, 1000, 200));
        assertEquals(0, hive.tips());
        // The camera sees the HIVE has TIPped: that replaces the assumption, and counts the TIP.
        assertEquals(LEFT_CELL_UP, see(LEFT_CELL_UP, 300));
        assertTrue(hive.leftCellUp());
        assertFalse(hive.assumed());
        assertEquals(1, hive.tips());
    }

    @Test
    public void seeingTheMatchStartClearsTheAssumptionWithNoTip() {
        hive.assumeMatchStart();
        assertEquals(RIGHT_CELL_UP, see(RIGHT_CELL_UP, 0));
        assertFalse(hive.assumed());
        assertEquals(0, hive.tips());
    }

    @Test
    public void startsUnseenAndTakesWhatItSees() {
        assertEquals(UNSEEN, hive.observe(UNSEEN, 0, NEVER, 0));
        assertEquals(RIGHT_CELL_UP, see(RIGHT_CELL_UP, 0));
        assertTrue(hive.rightCellUp());
        assertTrue(hive.leftCellDown());
        assertFalse(hive.rightCellDown());
        assertEquals(0, hive.tips());
    }

    /** Tags that leave the frame say nothing, whether the robot turned away or something blocked the view. */
    @Test
    public void tagsThatVanishAreUnseenNeverATip() {
        HiveTracker.Tuning.tipSeconds = 3.5;
        see(RIGHT_CELL_UP, 1000);
        assertEquals(RIGHT_CELL_UP, lose(RIGHT_CELL_UP, 100, 1100)); // a dropped frame
        assertEquals(UNSEEN, lose(RIGHT_CELL_UP, 200, 1200));
        assertFalse(hive.rightCellDown());
        assertFalse(hive.tipping());
        assertEquals(UNSEEN, lose(UNSEEN, 9000, 10_000)); // and no TIP is assumed later
        assertFalse(hive.assumed());
        assertEquals(0, hive.tips());
        assertEquals(0, hive.tipsStarted());
        assertEquals("the belief is held for the drawing", RIGHT_CELL_UP, hive.position());
    }

    /**
     * Q-1 at Belding (20245, 7 Nov 2026), Auto: volley, away to a FLOWER, back, volley, away, back.
     * The HIVE TIPped once, after the first volley. The old tracker started a TIP at each turn
     * away and counted 3.
     */
    @Test
    public void turningAwayAndBackCountsOnlyWhatTheCameraSeesChanged() {
        HiveTracker.Tuning.tipSeconds = 3.5;
        hive.assumeMatchStart();
        for (long t = 0; t <= 3100; t += 50) see(RIGHT_CELL_UP, t);          // volley 1
        for (long t = 3900; t < 12_600; t += 50) {                            // to the wall FLOWER
            assertEquals(UNSEEN, lose(RIGHT_CELL_UP, t - 3100, t));
        }
        assertEquals(0, hive.tips());
        for (long t = 12_600; t <= 16_900; t += 50) see(LEFT_CELL_UP, t);     // it had TIPped: volley 2
        assertEquals(1, hive.tips());
        for (long t = 17_300; t < 23_000; t += 50) {                          // to the scoring FLOWER
            assertEquals(UNSEEN, lose(LEFT_CELL_UP, t - 16_900, t));
        }
        assertEquals(LEFT_CELL_UP, see(LEFT_CELL_UP, 23_000));                // still LEFT up: no TIP
        assertFalse(hive.assumed());
        assertEquals(1, hive.tips());
        assertEquals(1, hive.tipsStarted());
    }

    @Test
    public void aCellSeenMidTipForLongEnoughIsATipToo() {
        see(RIGHT_CELL_UP, 0);
        assertEquals(RIGHT_CELL_UP, see(TRANSITION, 100)); // one frame proves nothing
        assertEquals(TRANSITION, see(TRANSITION, 350));
        assertTrue(hive.rightCellDown());
        assertFalse(hive.leftCellDown());
        assertEquals(0, hive.tips()); // not counted until the camera sees it settle
        assertEquals(LEFT_CELL_UP, see(LEFT_CELL_UP, 2500));
        assertEquals(1, hive.tips());
        assertEquals(1, hive.tipsStarted());
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
    public void whileTheMatchStartIsAssumedAMidTipSightingStartsNothing() {
        hive.assumeMatchStart();
        see(TRANSITION, 100);
        see(TRANSITION, 400);
        assertEquals(RIGHT_CELL_UP, see(TRANSITION, 800));
        assertEquals(0, hive.tipsStarted());
        // Seeing it settled puts everything right, and a real TIP after that counts.
        assertEquals(RIGHT_CELL_UP, see(RIGHT_CELL_UP, 900));
        assertFalse(hive.assumed());
        seeTipping(1000);
        assertEquals(1, hive.tipsStarted());
    }

    @Test
    public void theTipBackIsAwayFromTheLeft() {
        see(LEFT_CELL_UP, 0);
        seeTipping(100);
        assertTrue(hive.leftCellDown());
        assertFalse(hive.rightCellDown());
        see(RIGHT_CELL_UP, 2000);
        assertEquals(2, hive.tips());
    }

    @Test
    public void withoutAMeasuredTipTimeATipLastsUntilSeen() {
        see(RIGHT_CELL_UP, 0);
        seeTipping(100);
        assertEquals(TRANSITION, lose(UNSEEN, 60_000, 60_000));
        assertFalse(hive.assumed());
        assertEquals(0, hive.tips());
    }

    /** A TIP seen to start but never seen to finish is dropped: unknown, not "the other CELL up". */
    @Test
    public void aTipNotSeenToFinishInTimeIsUnseenAndNotCounted() {
        HiveTracker.Tuning.tipSeconds = 1.5;
        HiveTracker.Tuning.confirmSeconds = 1.0;
        see(RIGHT_CELL_UP, 0);
        seeTipping(1000);                                             // started at 1000
        assertEquals(TRANSITION, lose(UNSEEN, 1000, 3499));
        assertEquals(UNSEEN, lose(UNSEEN, 1001, 3500));
        assertFalse(hive.tipping());
        assertFalse(hive.assumed());
        assertFalse(hive.leftCellUp());
        assertFalse(hive.rightCellDown());
        assertEquals(0, hive.tips());
        assertEquals(1, hive.tipsStarted());
        assertEquals(RIGHT_CELL_UP, hive.position());
        // Looking again settles it either way.
        assertEquals(LEFT_CELL_UP, see(LEFT_CELL_UP, 9000));
        assertEquals(1, hive.tips());
    }

    @Test
    public void aTipSeenToStartThatSettlesBackCountsNothing() {
        HiveTracker.Tuning.tipSeconds = 3.5;
        see(RIGHT_CELL_UP, 0);
        seeTipping(100);
        assertEquals(RIGHT_CELL_UP, see(RIGHT_CELL_UP, 1500));
        assertFalse(hive.tipping());
        assertEquals(0, hive.tips());
    }

    @Test
    public void aTipCountsAsStartedTheMomentItBegins() {
        see(RIGHT_CELL_UP, 0);
        assertEquals(0, hive.tipsStarted());
        seeTipping(100);
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
        lose(RIGHT_CELL_UP, 300, 300); // looked away
        see(LEFT_CELL_UP, 5000); // it had tipped meanwhile
        assertEquals(1, hive.tipsStarted());
        assertEquals(1, hive.tips());
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
        assertEquals(UNSEEN, lose(LEFT_CELL_UP, 300, 1300));
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

    /**
     * R-Solo, 5 Nov 2026 00:32: the robot drove off its first launch spot at about 30 in/s, turning,
     * and the camera read the CELL mid-tip for 250 ms. Moving, that is not a TIP.
     */
    @Test
    public void aMidTipSightingWhileTheRobotDrivesIsNotATip() {
        see(RIGHT_CELL_UP, 0);
        for (long t = 100; t <= 600; t += 50) {
            assertEquals(RIGHT_CELL_UP, hive.observe(TRANSITION, 3, 0, false, t));
        }
        assertFalse(hive.tipping());
        assertEquals(0, hive.tipsStarted());
        // Stopped, the same sighting for 250 ms is a TIP, timed from when the robot was steady.
        hive.observe(TRANSITION, 3, 0, true, 700);
        assertEquals(TRANSITION, hive.observe(TRANSITION, 3, 0, true, 950));
        assertEquals(1, hive.tipsStarted());
    }

    @Test
    public void slowingDownMidSightingRestartsTheCount() {
        see(RIGHT_CELL_UP, 0);
        hive.observe(TRANSITION, 3, 0, true, 100);
        hive.observe(TRANSITION, 3, 0, false, 200);   // a fast loop breaks the run
        assertEquals(RIGHT_CELL_UP, hive.observe(TRANSITION, 3, 0, true, 400));
        assertEquals(TRANSITION, hive.observe(TRANSITION, 3, 0, true, 650));
    }

    @Test
    public void steadyIsSlowAndHardlyTurning() {
        HiveSubsystem.Tuning.midTipMaxSpeedInPerSec = 10;
        HiveSubsystem.Tuning.midTipMaxTurnDegPerSec = 30;
        Pose at = new Pose(50, 50, 0);
        assertTrue("no earlier pose", HiveSubsystem.steady(null, at, 0.02));
        assertTrue("no time passed", HiveSubsystem.steady(at, at, 0));
        assertTrue("still", HiveSubsystem.steady(at, at, 0.02));
        assertTrue("8 in/s", HiveSubsystem.steady(at, new Pose(50.16, 50, 0), 0.02));
        assertFalse("30 in/s", HiveSubsystem.steady(at, new Pose(50.6, 50, 0), 0.02));
        assertTrue("20 deg/s", HiveSubsystem.steady(at, new Pose(50, 50, Math.toRadians(0.4)), 0.02));
        assertFalse("60 deg/s", HiveSubsystem.steady(at, new Pose(50, 50, Math.toRadians(1.2)), 0.02));
        assertFalse("60 deg/s across the wrap",
                HiveSubsystem.steady(new Pose(50, 50, Math.toRadians(359.4)), new Pose(50, 50, Math.toRadians(0.6)), 0.02));
    }
}
