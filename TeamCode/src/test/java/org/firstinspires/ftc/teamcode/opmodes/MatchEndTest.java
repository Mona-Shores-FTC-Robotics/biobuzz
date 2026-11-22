package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** {@link MatchEnd}: the TeleOp launches nothing from 2:00 less its margin. */
public class MatchEndTest {

    @Test
    public void cutoffIsTwoMinutesLessTheMargin() {
        assertEquals(119.0, MatchEnd.cutoffS(1.0), 1e-9);
        assertEquals(120.0, MatchEnd.cutoffS(0.0), 1e-9);
    }

    @Test
    public void aBadMarginNeverStopsLaterThanTwoMinutes() {
        assertEquals(120.0, MatchEnd.cutoffS(Double.NaN), 1e-9);
        assertEquals(120.0, MatchEnd.cutoffS(-3.0), 1e-9);
        assertEquals(120.0 - MatchEnd.MAX_MARGIN_S, MatchEnd.cutoffS(500.0), 1e-9);
    }

    @Test
    public void launchesUntilTheCutoffThenNeverAgain() {
        MatchEnd end = new MatchEnd();
        assertFalse(end.update(0.0, 1.0));
        assertFalse(end.update(118.99, 1.0));
        assertFalse(end.over());
        assertTrue("the loop it ends says so, once", end.update(119.0, 1.0));
        assertTrue(end.over());
        assertFalse(end.update(119.1, 1.0));
        assertTrue(end.over());
        assertFalse("a margin changed after the end does not reopen it", end.update(119.2, 0.0));
        assertTrue(end.over());
        assertFalse(end.update(Double.NaN, 1.0));
        assertTrue(end.over());
    }

    /** Fruitport's four late feeds, at their robot times after TeleOp PLAY: none would feed. */
    @Test
    public void fruitportsLateFeedsAreAllPastTheCutoff() {
        double[] lateFeedStarts = {121.6, 121.9, 121.2, 120.3, 127.5, 121.4}; // Q-20 x2, Q-42, M-3 x2, M-7
        for (double t : lateFeedStarts) {
            MatchEnd end = new MatchEnd();
            end.update(t, 1.0);
            assertTrue("a feed at " + t + " s", end.over());
        }
    }

    @Test
    public void aMatchThatHasNotStartedIsNotOver() {
        MatchEnd end = new MatchEnd();
        assertFalse(end.update(Double.NaN, 1.0));
        assertFalse(end.over());
    }
}
