package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MotionWatchTest {

    private static final long S = 1_000_000_000L;

    @Test
    public void aCommandedMotorThatNeverMovesIsReportedOnceAfterTheGrace() {
        MotionWatch watch = new MotionWatch("feeder");
        assertNull(watch.check(true, 0.0, 0));
        assertNull(watch.check(true, 0.0, S / 2));
        String line = watch.check(true, 0.0, S);
        assertNotNull(line);
        assertTrue(line, line.startsWith("feeder: commanded for 1.0 s"));
        assertNull("said once", watch.check(true, 0.0, 2 * S));
        assertNull(watch.check(true, 0.0, 10 * S));
    }

    @Test
    public void aMotorThatMovesIsNeverReported() {
        MotionWatch watch = new MotionWatch("intake");
        assertNull(watch.check(true, 0.0, 0));
        assertNull(watch.check(true, 300.0, S / 2));   // spun up
        assertNull(watch.check(true, 300.0, 5 * S));
        assertNull(watch.check(true, -300.0, 6 * S));  // direction does not matter
    }

    @Test
    public void idleIsNotWatched() {
        MotionWatch watch = new MotionWatch("intake");
        assertNull(watch.check(false, 0.0, 0));
        assertNull(watch.check(false, 0.0, 10 * S));
    }

    @Test
    public void reArmsWhenTheCommandStopsOrItMoves() {
        MotionWatch watch = new MotionWatch("feeder");
        assertNull(watch.check(true, 0.0, 0));
        assertNotNull(watch.check(true, 0.0, S));
        assertNull(watch.check(false, 0.0, 2 * S));        // command off: forget
        assertNull(watch.check(true, 0.0, 3 * S));         // on again: the grace restarts
        assertNull(watch.check(true, 0.0, 3 * S + S / 2));
        assertNotNull(watch.check(true, 0.0, 4 * S));      // reported again
        assertNull(watch.check(true, 200.0, 5 * S));       // moved: forget
        assertNull(watch.check(true, 0.0, 6 * S));         // jammed: grace restarts
        assertNotNull(watch.check(true, 0.0, 7 * S));
    }

    @Test
    public void slowCreepCountsAsNotMoving() {
        MotionWatch watch = new MotionWatch("feeder");
        assertNull(watch.check(true, MotionWatch.MOVING_TICKS_PER_SECOND - 1, 0));
        assertNotNull(watch.check(true, MotionWatch.MOVING_TICKS_PER_SECOND - 1, S));
        assertEquals(20.0, MotionWatch.MOVING_TICKS_PER_SECOND, 0.0);
    }
}
