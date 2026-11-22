package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** {@link FeedHold}: a rule holds a feed's start until it passes or the press has been held its give-up time. */
public class FeedHoldTest {

    @Test
    public void offNeverHolds() {
        FeedHold h = new FeedHold();
        assertEquals(FeedHold.Verdict.OFF, h.update(false, true, false, 0.0, 1.0));
        assertFalse(h.holds());
        assertEquals(FeedHold.Verdict.OFF, h.update(false, true, false, 5.0, 1.0));
        assertFalse(h.holds());
    }

    @Test
    public void waitsForTheRuleThenLetsThePressThrough() {
        FeedHold h = new FeedHold();
        assertEquals(FeedHold.Verdict.WAITING, h.update(true, true, false, 10.0, 1.0));
        assertTrue(h.holds());
        assertEquals(FeedHold.Verdict.WAITING, h.update(true, true, false, 10.5, 1.0));
        assertEquals(FeedHold.Verdict.PASS, h.update(true, true, true, 10.6, 1.0));
        assertFalse(h.holds());
        assertTrue(h.waited());
        assertEquals(0.6, h.heldS(10.6), 1e-9);
    }

    @Test
    public void aFeedUnderWayIsNeverCutWhenTheRuleStopsPassing() {
        FeedHold h = new FeedHold();
        h.update(true, true, true, 0.0, 1.0);
        // The first ball leaves: the lane no longer reads full.
        assertEquals(FeedHold.Verdict.PASS, h.update(true, true, false, 0.2, 1.0));
        assertFalse(h.holds());
        assertFalse(h.holds());
        assertFalse(h.waited());
    }

    @Test
    public void givesUpAfterTheGiveUpTimeAndStaysGivenUp() {
        FeedHold h = new FeedHold();
        h.update(true, true, false, 0.0, 1.0);
        assertEquals(FeedHold.Verdict.WAITING, h.update(true, true, false, 0.99, 1.0));
        assertEquals(FeedHold.Verdict.GAVE_UP, h.update(true, true, false, 1.0, 1.0));
        assertFalse(h.holds());
        assertEquals(FeedHold.Verdict.GAVE_UP, h.update(true, true, false, 1.5, 1.0));
    }

    @Test
    public void aNewPressWaitsAgain() {
        FeedHold h = new FeedHold();
        h.update(true, true, true, 0.0, 1.0);
        assertEquals(FeedHold.Verdict.PASS, h.update(true, false, false, 0.5, 1.0));
        assertFalse(h.holds());
        assertEquals(FeedHold.Verdict.WAITING, h.update(true, true, false, 0.6, 1.0));
        assertTrue(h.holds());
    }

    @Test
    public void aBadGiveUpTimeWaitsAsLongAsHeld() {
        FeedHold h = new FeedHold();
        h.update(true, true, false, 0.0, Double.NaN);
        assertTrue(h.holds());
        h.update(true, true, false, 100.0, Double.NaN);
        assertTrue(h.holds());
        h.update(true, true, false, 0.0, -1.0);
        assertTrue(h.holds());
    }

    @Test
    public void switchedOffMidPressLetsItThroughForTheRestOfIt() {
        FeedHold h = new FeedHold();
        h.update(true, true, false, 0.0, 1.0);
        assertTrue(h.holds());
        h.update(false, true, false, 0.1, 1.0);
        assertFalse(h.holds());
        h.update(true, true, false, 0.2, 1.0);
        assertFalse("switched back on, the press is still let through", h.holds());
    }
}
