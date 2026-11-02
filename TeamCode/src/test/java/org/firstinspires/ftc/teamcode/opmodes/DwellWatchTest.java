package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** The dwell ends once the pieces have stopped coming, or when none came at all. */
public class DwellWatchTest {

    private static final double SPIKE = 1.0;
    private static final double QUIET = 400;
    private static final double EMPTY = 700;

    private static DwellWatch freeRunning() {
        DwellWatch w = new DwellWatch();
        for (double t = -500; t < 0; t += 20) w.observe(t, 0.35, SPIKE);
        return w;
    }

    @Test
    public void anEmptyFlowerEndsTheDwellEarly() {
        DwellWatch w = freeRunning();
        for (double t = 0; t < EMPTY; t += 20) {
            w.observe(t, 0.37, SPIKE);
            assertEquals("still waiting at " + t, false, w.done(t, 0, QUIET, EMPTY));
        }
        assertEquals(true, w.done(EMPTY, 0, QUIET, EMPTY));
        assertEquals(false, w.sawSpike());
    }

    @Test
    public void piecesKeepItGoingUntilTheyStop() {
        DwellWatch w = freeRunning();
        w.observe(100, 6.0, SPIKE);
        w.observe(500, 5.5, SPIKE); // the last piece
        assertEquals("past emptyMs, but pieces came", false, w.done(800, 0, QUIET, EMPTY));
        assertEquals(true, w.done(900, 0, QUIET, EMPTY));
        assertEquals(true, w.sawSpike());
    }

    @Test
    public void aSpikeDuringTheInCounts() {
        DwellWatch w = freeRunning();
        w.observe(-100, 6.0, SPIKE);
        assertEquals(false, w.done(250, 0, QUIET, EMPTY));
        assertEquals(true, w.done(300, 0, QUIET, EMPTY));
    }

    @Test
    public void theBaselineIsTheFreeRunningCurrent() {
        DwellWatch w = new DwellWatch();
        w.observe(-300, 3.8, SPIKE); // the start-up surge
        w.observe(-200, 0.35, SPIKE);
        w.observe(0, 0.9, SPIKE); // a little load, not a piece
        assertEquals(false, w.sawSpike());
        w.observe(20, 1.5, SPIKE);
        assertEquals(true, w.sawSpike());
    }

    @Test
    public void zeroTurnsItOff() {
        DwellWatch w = freeRunning();
        assertEquals(false, w.done(5000, 0, 0, EMPTY));
        assertEquals(false, w.done(5000, 0, QUIET, 0));
    }

    @Test
    public void skippedReadsAreIgnored() {
        DwellWatch w = freeRunning();
        w.observe(100, Double.NaN, SPIKE);
        assertEquals(false, w.sawSpike());
    }
}
