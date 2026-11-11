package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** {@link EncoderWatch}: a silent wheel is cut, and tried again (#496). */
public class EncoderWatchTest {

    private static final long MS = 1_000_000L;
    private static final double FAULT_MS = 500.0;
    private static final double FLOOR = 20.0;
    private static final double RETRY_MS = 2000.0;

    private static void step(EncoderWatch w, double measured, long ms) {
        w.update(1500.0, measured, ms * MS, FAULT_MS, FLOOR, RETRY_MS);
    }

    @Test
    public void aWheelThatNeverReadsIsCutAfterTheFaultTime() {
        EncoderWatch w = new EncoderWatch();
        w.restart(0);
        step(w, 0.0, 100);
        assertFalse(w.cut());
        step(w, 0.0, 499);
        assertFalse(w.fault());
        step(w, 0.0, 500);
        assertTrue(w.cut());
        assertTrue(w.fault());
    }

    @Test
    public void oneZeroReadingInARunningWheelIsNotAFault() {
        EncoderWatch w = new EncoderWatch();
        w.restart(0);
        step(w, 1500.0, 3000);
        step(w, 0.0, 3020); // long after spin-up, one bad read
        assertFalse(w.fault());
        step(w, 1490.0, 3040);
        step(w, 0.0, 3400);
        assertFalse(w.fault()); // 360 ms after it last moved
    }

    @Test
    public void aRunningWheelThatGoesSilentIsCutAfterTheFaultTime() {
        EncoderWatch w = new EncoderWatch();
        w.restart(0);
        step(w, 1500.0, 3000);
        step(w, 0.0, 3020);
        step(w, 0.0, 3499); // the silence is timed from the last reading that moved
        assertFalse(w.cut());
        step(w, 0.0, 3500);
        assertTrue(w.cut());
    }

    @Test
    public void aCableThatComesBackRestoresTheWheelWithoutASpinDown() {
        EncoderWatch w = new EncoderWatch();
        w.restart(0);
        step(w, 0.0, 500);
        assertTrue(w.cut());
        step(w, 0.0, 2499); // coasting, cut: reads nothing either way
        assertTrue(w.cut());
        step(w, 0.0, 2500); // the retry: commanded again
        assertFalse(w.cut());
        assertTrue("still reported until it reads speed", w.fault());
        assertEquals(1, w.retries());
        step(w, 300.0, 2600); // the encoder is back
        assertFalse(w.cut());
        assertFalse(w.fault());
    }

    @Test
    public void aCableStillOutIsCutAgainAndTriedAgain() {
        EncoderWatch w = new EncoderWatch();
        w.restart(0);
        step(w, 0.0, 500);
        step(w, 0.0, 2500);
        assertFalse(w.cut());
        step(w, 0.0, 2999);
        assertFalse(w.cut());
        step(w, 0.0, 3000);
        assertTrue(w.cut());
        step(w, 0.0, 5000);
        assertFalse(w.cut());
        assertEquals(2, w.retries());
        assertTrue(w.fault());
    }

    @Test
    public void noRetryTimeKeepsItCutUntilARestart() {
        EncoderWatch w = new EncoderWatch();
        w.restart(0);
        w.update(1500.0, 0.0, 500 * MS, FAULT_MS, FLOOR, 0.0);
        w.update(1500.0, 0.0, 60_000 * MS, FAULT_MS, FLOOR, Double.NaN);
        assertTrue(w.cut());
        w.restart(60_001 * MS);
        assertFalse(w.fault());
    }

    @Test
    public void aZeroTargetIsNeverAFault() {
        EncoderWatch w = new EncoderWatch();
        w.restart(0);
        w.update(0.0, 0.0, 5000 * MS, FAULT_MS, FLOOR, RETRY_MS);
        assertFalse(w.fault());
        // and the clock starts when the target does
        w.update(1500.0, 0.0, 5400 * MS, FAULT_MS, FLOOR, RETRY_MS);
        assertFalse(w.fault());
    }
}
