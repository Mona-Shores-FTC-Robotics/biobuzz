package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.opmodes.auto.LaunchCards;
import org.firstinspires.ftc.teamcode.opmodes.auto.PathEndSettle;
import org.junit.Test;

/** {@link SpotVolley}: the launch-spot button's volley, waited for as an Auto's LaunchAll is, fed while held. */
public class SpotVolleyTest {

    private static final double FEED = LaunchCards.launchAllFeedS;
    private static final double SPIN = LaunchCards.maxSpinUpWaitS;
    private static final double SETTLE = PathEndSettle.maxSettleWaitS;

    private static SpotVolley started() {
        SpotVolley v = new SpotVolley();
        v.start();
        return v;
    }

    @Test
    public void nothingFeedsWhileDriving() {
        SpotVolley v = started();
        assertFalse(v.update(0.5, false, true, true, false));
        assertFalse(v.update(3.0, false, true, true, false));
        assertEquals(SpotVolley.Phase.DRIVING, v.phase());
        assertFalse(v.fired());
    }

    @Test
    public void atSpeedAndSettledOnArrivalFeedsForAsLongAsItIsHeld() {
        SpotVolley v = started();
        assertTrue(v.update(2.0, true, true, true, false));
        assertEquals(2.0, v.feedStartS(), 1e-9);
        assertEquals("at speed, settled", v.why());
        // No set time: an Auto's LaunchAll would have stopped at FEED; held, this keeps feeding.
        assertTrue(v.update(2.0 + FEED, true, true, true, false));
        assertTrue(v.update(2.0 + FEED + 5, true, true, true, false));
        assertEquals(SpotVolley.Phase.FEEDING, v.phase());
        assertTrue(Double.isNaN(v.feedEndS()));
        v.stop(2.0 + FEED + 6);
        assertEquals(2.0 + FEED + 6, v.feedEndS(), 1e-9);
    }

    @Test
    public void itWaitsForSettleThenFeedsAnywayAfterTheAutosSettleWait() {
        SpotVolley v = started();
        assertFalse(v.update(1.0, true, true, false, false));
        assertFalse(v.update(1.0 + SETTLE - 0.01, true, true, false, false));
        assertTrue(v.update(1.0 + SETTLE, true, true, false, false));
        assertTrue(v.why(), v.why().contains("before settled"));
    }

    @Test
    public void itWaitsForSpeedThenFeedsAnywayAfterTheAutosSpinUpWait() {
        SpotVolley v = started();
        assertFalse(v.update(1.0, true, false, true, false));
        assertFalse(v.update(1.0 + SPIN - 0.01, true, false, true, false));
        assertTrue(v.update(1.0 + SPIN, true, false, true, false));
        assertTrue(v.why(), v.why().contains("below launch speed"));
    }

    @Test
    public void theOperatorsFireWinsForTheRestOfThePress() {
        SpotVolley v = started();
        assertTrue(v.update(1.0, true, true, true, false));
        assertFalse(v.update(1.2, true, true, true, true));
        assertEquals(SpotVolley.Phase.YIELDED, v.phase());
        assertEquals(1.2, v.feedEndS(), 1e-9);
        assertFalse("not taken back when the operator lets go", v.update(1.4, true, true, true, false));
        SpotVolley before = started();
        assertFalse(before.update(0.5, false, true, true, true));
        assertFalse(before.update(1.0, true, true, true, false));
        assertFalse(before.fired());
    }

    @Test
    public void lettingGoStopsTheFeedAtOnce() {
        SpotVolley v = started();
        assertTrue(v.update(1.0, true, true, true, false));
        v.stop(1.5);
        assertEquals(1.5, v.feedEndS(), 1e-9);
        assertFalse(v.update(1.6, true, true, true, false));
        assertEquals(SpotVolley.Phase.OFF, v.phase());
    }

    @Test
    public void aNewPressIsAFreshVolley() {
        SpotVolley v = started();
        assertTrue(v.update(1.0, true, true, true, false));
        v.stop(1.5);
        v.start();
        assertFalse(v.fired());
        assertTrue(v.update(0.8, true, true, true, false));
    }

    @Test
    public void aPanelsTypoMakesAShortWaitNeverAStuckOne() {
        assertEquals(0.0, SpotVolley.nonNegative(Double.NaN), 0);
        assertEquals(0.0, SpotVolley.nonNegative(-1), 0);
        assertEquals(2.5, SpotVolley.nonNegative(2.5), 0);
    }

    @Test
    public void onTheApproachItFeedsBeforeArrivingOnceAtSpeed() {
        SpotVolley v = started();
        assertFalse(v.update(0.3, false, false, false, false, true)); // close and aimed, not at speed
        assertFalse(v.update(0.4, false, true, false, false, false)); // at speed, not close yet
        assertTrue(v.update(0.5, false, true, false, false, true));
        assertEquals(0.5, v.feedStartS(), 1e-9);
        assertEquals("on the approach, at speed", v.why());
        // It keeps feeding through the arrival and the settle, with no second wait.
        assertTrue(v.update(0.8, true, true, false, false, false));
        assertTrue(v.update(1.0, true, false, false, false, false));
        assertEquals(SpotVolley.Phase.FEEDING, v.phase());
    }

    @Test
    public void withoutTheApproachItStillWaitsForArrival() {
        SpotVolley v = started();
        assertFalse(v.update(0.5, false, true, true, false, false));
        assertTrue(v.update(0.9, true, true, true, false, false));
        assertEquals("at speed, settled", v.why());
    }

    @Test
    public void aShoveOffTheSpotCountsAsTheAngleItMovesTheShot() {
        // 2.62 in off at 50 in is 3 degrees; a turn larger than that wins.
        assertEquals(3.0, LaunchSpotDrive.shotOffDeg(0.5, 2.62, 50.0), 0.01);
        assertEquals(7.0, LaunchSpotDrive.shotOffDeg(-7.0, 2.62, 50.0), 1e-9);
        // Still driving in (no distance), or no range to the CELL: the turn alone.
        assertEquals(1.0, LaunchSpotDrive.shotOffDeg(1.0, Double.NaN, 50.0), 1e-9);
        assertEquals(1.0, LaunchSpotDrive.shotOffDeg(-1.0, 4.0, Double.NaN), 1e-9);
        // No pose for the heading, but a distance: the shove alone.
        assertEquals(3.0, LaunchSpotDrive.shotOffDeg(Double.NaN, 2.62, 50.0), 0.01);
    }
}
