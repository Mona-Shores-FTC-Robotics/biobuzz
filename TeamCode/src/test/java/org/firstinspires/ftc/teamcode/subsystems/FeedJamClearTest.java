package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** {@link FeedJamClear}: a feed stuck with a piece at the entry backs out, and only then. */
public class FeedJamClearTest {

    private static final long MS = 1_000_000L;

    private static boolean step(FeedJamClear j, boolean feeding, boolean present, int shots, long ms) {
        return j.update(feeding, present, shots, ms * MS, 1200.0, 250.0, 2);
    }

    @Test
    public void aFeedThatFiresIsLeftAlone() {
        // Four balls about 0.45 s apart, the entry piece leaving after the first; a slow first ball.
        FeedJamClear j = new FeedJamClear();
        int shots = 0;
        for (long t = 0; t <= 3500; t += 20) {
            if (t == 1000 || t == 1450 || t == 1900 || t == 2350) shots++;
            assertFalse(step(j, true, t < 1100, shots, t));
        }
        assertEquals(0, j.bursts());
    }

    @Test
    public void notFeedingIsLeftAlone() {
        FeedJamClear j = new FeedJamClear();
        for (long t = 0; t < 10000; t += 20) assertFalse(step(j, false, true, 0, t));
        assertEquals(0, j.bursts());
    }

    @Test
    public void anEmptyEntryIsLeftAlone() {
        FeedJamClear j = new FeedJamClear();
        for (long t = 0; t < 5000; t += 20) assertFalse(step(j, true, false, 0, t));
        assertEquals(0, j.bursts());
    }

    @Test
    public void aStuckStackBacksOutThenFeedsAgain() {
        // M-1's preloads: gate open, the piece at the entry never moves, nothing fired.
        FeedJamClear j = new FeedJamClear();
        assertFalse(step(j, true, true, 0, 0));
        assertFalse(step(j, true, true, 0, 1180));
        assertTrue(step(j, true, true, 0, 1200));
        assertTrue(step(j, true, false, 0, 1300)); // backing out, the piece off the sensor
        assertFalse(step(j, true, true, 0, 1460)); // the burst is over: feeding again
        assertFalse(step(j, true, true, 1, 1900)); // a shot
        assertEquals(1, j.bursts());
    }

    @Test
    public void theQuietSpellCountsFromTheLastShot() {
        // M-6's second volley: three shots, then the last piece wedged at the entry.
        FeedJamClear j = new FeedJamClear();
        assertFalse(step(j, true, true, 0, 0));
        assertFalse(step(j, true, true, 1, 800));
        assertFalse(step(j, true, true, 2, 1250));
        assertFalse(step(j, true, true, 3, 1800));
        assertFalse(step(j, true, true, 3, 2980));
        assertTrue(step(j, true, true, 3, 3000));
    }

    @Test
    public void twoBurstsAFeedAtMost() {
        FeedJamClear j = new FeedJamClear();
        int bursts = 0;
        boolean was = false;
        for (long t = 0; t < 8000; t += 20) {
            boolean back = step(j, true, true, 0, t);
            if (back && !was) bursts++;
            was = back;
        }
        assertEquals(2, bursts);
        assertEquals(2, j.bursts());
        // a new feed may clear again
        assertFalse(step(j, false, true, 0, 8000));
        assertFalse(step(j, true, true, 0, 8020));
        assertTrue(step(j, true, true, 0, 9220));
        assertEquals(3, j.bursts());
    }

    @Test
    public void offWhenTheTimeIsNotPositive() {
        FeedJamClear j = new FeedJamClear();
        for (long t = 0; t < 5000; t += 20) assertFalse(j.update(true, true, 0, t * MS, Double.NaN, 250.0, 2));
        for (long t = 5000; t < 10000; t += 20) assertFalse(j.update(true, true, 0, t * MS, 0.0, 250.0, 2));
        assertEquals(0, j.bursts());
    }
}
