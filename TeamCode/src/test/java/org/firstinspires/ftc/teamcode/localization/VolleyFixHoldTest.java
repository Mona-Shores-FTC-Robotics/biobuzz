package org.firstinspires.ftc.teamcode.localization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.pedropathing.math.Pose;

import org.junit.After;
import org.junit.Test;

/** A still fix that passes during a volley waits for its end, and is dropped if the robot moved. */
public class VolleyFixHoldTest {

    private static final Pose VARIANCE = new Pose(1, 1, 0.01);
    /** Fruitport Q-42's first LEFT volley: still at LEFT_AUTO_HIVE, a fix 2.5 in away. */
    private static final Pose AT_SPOT = new Pose(61.7, 120.4, Math.toRadians(269));

    @After
    public void restore() {
        LocalizationTuning.holdFixesWhileFeeding = true;
    }

    @Test
    public void aFixDuringAFeedIsHeldAndAppliedWhenTheFeedEnds() {
        VolleyFixHold hold = new VolleyFixHold();
        assertNull(hold.observe(false, AT_SPOT, 1_000));
        assertFalse("no volley: a fix goes straight in", hold.holding());

        assertNull(hold.observe(true, AT_SPOT, 2_000));
        assertTrue("the gate is open: a passing fix is held", hold.holding());
        hold.hold(62.7, 118.7, VARIANCE, 1_900, AT_SPOT);
        assertNull("nothing released while the feed goes on", hold.observe(true, AT_SPOT, 3_000));

        VolleyFixHold.Release release = hold.observe(false, AT_SPOT, 4_000);
        assertNotNull(release);
        assertTrue(release.apply);
        assertEquals(62.7, release.fix.x(), 1e-9);
        assertEquals(118.7, release.fix.y(), 1e-9);
        assertTrue("position only", Double.isNaN(release.fix.heading()));
        assertEquals("stamped with the last feeding loop, when the robot was still where its frames saw it",
                3_000, release.stampNs);
        assertNull("released once", hold.observe(false, AT_SPOT, 5_000));
        assertEquals(1, hold.held());
        assertEquals(0, hold.dropped());
    }

    @Test
    public void theNewestFixDuringAFeedWins() {
        VolleyFixHold hold = new VolleyFixHold();
        hold.observe(true, AT_SPOT, 1_000);
        hold.hold(62.7, 118.7, VARIANCE, 900, AT_SPOT);
        hold.hold(61.8, 120.3, VARIANCE, 1_500, AT_SPOT);
        VolleyFixHold.Release release = hold.observe(false, AT_SPOT, 2_000);
        assertEquals(61.8, release.fix.x(), 1e-9);
        assertEquals(120.3, release.fix.y(), 1e-9);
    }

    @Test
    public void aFixIsDroppedIfTheRobotMovedDuringTheFeed() {
        VolleyFixHold hold = new VolleyFixHold();
        hold.observe(true, AT_SPOT, 1_000);
        hold.hold(62.7, 118.7, VARIANCE, 900, AT_SPOT);
        Pose pushed = new Pose(AT_SPOT.x() + 2.0, AT_SPOT.y(), AT_SPOT.heading());
        hold.observe(true, pushed, 2_000);
        VolleyFixHold.Release release = hold.observe(false, pushed, 3_000);
        assertNotNull(release);
        assertFalse("the frames saw the robot where it no longer is", release.apply);
        assertEquals(1, hold.dropped());
        assertTrue(release.note, release.note.contains("dropped"));
    }

    @Test
    public void aFixIsDroppedIfTheRobotTurnedDuringTheFeed() {
        VolleyFixHold hold = new VolleyFixHold();
        hold.observe(true, AT_SPOT, 1_000);
        hold.hold(62.7, 118.7, VARIANCE, 900, AT_SPOT);
        Pose turned = new Pose(AT_SPOT.x(), AT_SPOT.y(), AT_SPOT.heading() + Math.toRadians(5));
        hold.observe(true, turned, 2_000);
        assertFalse(hold.observe(false, turned, 3_000).apply);
    }

    @Test
    public void aFeedWithNoFixReleasesNothing() {
        VolleyFixHold hold = new VolleyFixHold();
        hold.observe(true, AT_SPOT, 1_000);
        assertNull(hold.observe(false, AT_SPOT, 2_000));
    }

    @Test
    public void theSwitchOffMeansNothingIsHeld() {
        LocalizationTuning.holdFixesWhileFeeding = false;
        VolleyFixHold hold = new VolleyFixHold();
        hold.observe(true, AT_SPOT, 1_000);
        assertFalse(hold.holding());
    }
}
