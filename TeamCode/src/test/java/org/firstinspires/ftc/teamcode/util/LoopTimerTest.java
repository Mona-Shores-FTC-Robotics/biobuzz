package org.firstinspires.ftc.teamcode.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Pins down the arithmetic in {@link LoopTimer} with a fake clock.
 *
 * <p>The behaviour most worth protecting is the first-lap rule: {@code lap()} called once records
 * nothing. Without it the interval spanning init lands in the statistics, and since it is usually
 * the largest number of the whole run it takes over {@code maxMs()} permanently — which makes the
 * one statistic that exists to catch stalls useless at catching them.
 */
public class LoopTimerTest {

    private static final double EPS = 1e-9;
    private static final long MS = 1_000_000L;

    @Test
    public void firstLapOnlyMarksTheOrigin() {
        LoopTimer timer = new LoopTimer();
        timer.lap(0L);

        assertEquals("no loop has been measured", 0, timer.count());
        assertEquals("max must not include the pre-loop interval", 0.0, timer.maxMs(), EPS);
        assertTrue("mean is undefined with no samples", Double.isNaN(timer.meanMs()));
    }

    @Test
    public void measuresIntervalsNotTimestamps() {
        LoopTimer timer = new LoopTimer();
        timer.lap(1000 * MS);
        timer.lap(1005 * MS);
        timer.lap(1010 * MS);

        assertEquals(2, timer.count());
        assertEquals(5.0, timer.meanMs(), EPS);
        assertEquals(5.0, timer.lastMs(), EPS);
        assertEquals(200.0, timer.hz(), EPS);
    }

    @Test
    public void maxCatchesAStallThatBarelyMovesTheMean() {
        LoopTimer timer = new LoopTimer();
        long t = 0L;
        timer.lap(t);
        for (int i = 0; i < 100; i++) {
            t += 4 * MS;
            timer.lap(t);
        }
        t += 180 * MS;
        timer.lap(t);

        assertEquals("one stall in 101 loops", 101, timer.count());
        assertEquals("the stall is visible", 180.0, timer.maxMs(), EPS);
        assertTrue("but the mean barely moved", timer.meanMs() < 6.0);
    }

    @Test
    public void slowLoopsCountAgainstTheThreshold() {
        LoopTimer timer = new LoopTimer(10.0);
        long t = 0L;
        timer.lap(t);
        for (int i = 0; i < 3; i++) {
            t += 4 * MS;
            timer.lap(t);
        }
        t += 25 * MS;
        timer.lap(t);

        assertEquals(1, timer.slowLoops());
        assertEquals(4, timer.count());
        assertEquals(0.25, timer.slowFraction(), EPS);
    }

    /** Exactly at the threshold is not slow; the check is strictly greater. */
    @Test
    public void thresholdIsExclusive() {
        LoopTimer timer = new LoopTimer(10.0);
        timer.lap(0L);
        timer.lap(10 * MS);

        assertEquals(0, timer.slowLoops());
    }

    @Test
    public void resetForgetsTheOriginToo() {
        LoopTimer timer = new LoopTimer();
        timer.lap(0L);
        timer.lap(5 * MS);
        timer.reset();

        // If reset kept the origin, this lap would record a 995 ms interval.
        timer.lap(1000 * MS);
        timer.lap(1004 * MS);

        assertEquals(1, timer.count());
        assertEquals(4.0, timer.meanMs(), EPS);
        assertEquals(4.0, timer.maxMs(), EPS);
    }

    @Test
    public void summaryIsSafeBeforeAnySamples() {
        assertEquals("no loops measured yet", new LoopTimer().summary());
    }
}
