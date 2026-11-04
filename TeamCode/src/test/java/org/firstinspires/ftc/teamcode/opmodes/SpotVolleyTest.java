package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.opmodes.auto.LaunchCards;
import org.firstinspires.ftc.teamcode.opmodes.auto.PathEndSettle;
import org.junit.Test;

/** {@link SpotVolley}: the launch-spot button's one volley, waited for as an Auto's LaunchAll is. */
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
    public void atSpeedAndSettledOnArrivalFeedsForTheLaunchAllTimeThenStops() {
        SpotVolley v = started();
        assertTrue(v.update(2.0, true, true, true, false));
        assertEquals(2.0, v.feedStartS(), 1e-9);
        assertEquals("at speed, settled", v.why());
        assertTrue(v.update(2.0 + FEED - 0.01, true, true, true, false));
        assertFalse(v.update(2.0 + FEED, true, true, true, false));
        assertEquals(SpotVolley.Phase.DONE, v.phase());
        // One volley a press: nothing more while still held.
        assertFalse(v.update(2.0 + FEED + 5, true, true, true, false));
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
}
