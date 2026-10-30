package org.firstinspires.ftc.teamcode.localization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.localization.ImpactWatch.Event;
import org.junit.Test;

/** A push is the drive told to go and not going, long enough; a jolt is a speed change no drive makes. */
public class ImpactWatchTest {

    private static final double EPS = 1e-9;

    @Test
    public void aLongPushIsReportedWhenItEnds() {
        ImpactWatch w = new ImpactWatch();
        assertEquals(Event.NONE, w.update(0, 0.8, 0.5));
        assertTrue(w.pushing());
        assertEquals(Event.NONE, w.update(200, 0.9, 0.5));
        assertEquals(Event.NONE, w.update(400, 0.7, 0.5));
        assertEquals(Event.PUSH_ENDED, w.update(420, 0.0, 0.5));
        assertEquals(420, w.lastPushMs(), EPS);
        assertEquals(0.9, w.lastPushPeakCommand(), EPS);
    }

    @Test
    public void aShortStallOrMovingRobotIsNoPush() {
        ImpactWatch w = new ImpactWatch();
        w.update(0, 0.8, 0.5);
        assertEquals("too short", Event.NONE, w.update(200, 0.0, 0.5));
        ImpactWatch moving = new ImpactWatch();
        for (long t = 0; t <= 1000; t += 20) {
            assertEquals(Event.NONE, moving.update(t, 0.8, 40.0 + t * 0.01));
        }
    }

    @Test
    public void aPathIsNeverAPush() {
        ImpactWatch w = new ImpactWatch();
        for (long t = 0; t <= 1000; t += 20) w.update(t, Double.NaN, 0.0);
        assertEquals(false, w.pushing());
    }

    @Test
    public void aSuddenStopIsOneJolt() {
        ImpactWatch w = new ImpactWatch();
        w.update(0, 0.8, 40.0);
        assertEquals(Event.JOLT, w.update(20, 0.8, 30.0)); // 500 in/s²
        assertEquals(500, w.lastJoltInPerSec2(), 1e-6);
        assertEquals("held off", Event.NONE, w.update(40, 0.8, 20.0));
        assertEquals("steady again", Event.NONE, w.update(600, 0.8, 20.0));
        assertEquals("after the hold-off", Event.JOLT, w.update(620, 0.8, 40.0)); // 1000 in/s²
    }

    @Test
    public void ordinaryAccelerationIsNoJolt() {
        ImpactWatch w = new ImpactWatch();
        double speed = 0.0;
        for (long t = 0; t <= 1000; t += 20) {
            assertEquals(Event.NONE, w.update(t, 0.8, speed));
            speed += 2.0; // 100 in/s²
        }
    }
}
