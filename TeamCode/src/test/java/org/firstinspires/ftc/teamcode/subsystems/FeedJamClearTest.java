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

    /**
     * With the throat beam, {@code IntakeSubsystem} passes "a ball held in the throat" as no entry
     * piece: that jam is the throat's (LaunchCards backs the feeder out). No burst while it is held,
     * and the quiet spell starts afresh once it moves: one jam, one handler.
     */
    @Test
    public void aBallHeldInTheThroatStandsItDown() {
        FeedJamClear j = new FeedJamClear();
        for (long t = 0; t < 500; t += 20) assertFalse(step(j, true, true, 0, t));
        // Held in the throat from 0.5 s to 3.0 s, far past jamMs.
        for (long t = 500; t < 3000; t += 20) assertFalse("the throat's jam", step(j, true, false, 0, t));
        // Free again, the entry piece still: quiet counts from 3.0 s, so no burst before 4.2 s.
        for (long t = 3000; t < 4180; t += 20) assertFalse(step(j, true, true, 0, t));
        boolean burst = false;
        for (long t = 4180; t < 4300; t += 20) burst |= step(j, true, true, 0, t);
        assertTrue("then a lane jam is this one's again", burst);
    }

    /** Each ball the throat beam counts is movement ({@code launcher.launched()}): a firing feed is left alone. */
    @Test
    public void beamBallsAreMovement() {
        FeedJamClear j = new FeedJamClear();
        int balls = 0;
        for (long t = 0; t < 6000; t += 20) {
            if (t % 1000 == 0) balls++;
            assertFalse(step(j, true, true, balls, t));
        }
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
