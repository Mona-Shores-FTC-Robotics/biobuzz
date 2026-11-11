package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** {@link WedgeClear}: a roller wedged with an empty lane backs out, and only then. */
public class WedgeClearTest {

    private static final long MS = 1_000_000L;
    private static final double FREE_TPS = 2400.0;
    private static final double FREE_AMPS = 0.8;
    private static final double STALL_AMPS = 8.0;

    private static boolean step(WedgeClear w, boolean takingIn, double tps, double amps, long ms) {
        return w.update(takingIn, tps, amps, ms * MS, 100.0, 5.0, 1000.0, 250.0, 3);
    }

    @Test
    public void aRollerRunningFreeIsLeftAlone() {
        WedgeClear w = new WedgeClear();
        for (long t = 0; t < 5000; t += 20) assertFalse(step(w, true, FREE_TPS, FREE_AMPS, t));
        assertEquals(0, w.bursts());
    }

    @Test
    public void aShortStallThatLetsGoIsLeftAlone() {
        WedgeClear w = new WedgeClear();
        for (long t = 0; t < 800; t += 20) assertFalse(step(w, true, 0.0, STALL_AMPS, t));
        assertFalse(step(w, true, FREE_TPS, FREE_AMPS, 800));
        // a second short stall starts its own clock
        for (long t = 820; t < 1700; t += 20) assertFalse(step(w, true, 0.0, STALL_AMPS, t));
        assertEquals(0, w.bursts());
    }

    @Test
    public void aStallThatHoldsBacksOutThenTakesInAgain() {
        WedgeClear w = new WedgeClear();
        assertFalse(step(w, true, 0.0, STALL_AMPS, 0));
        assertFalse(step(w, true, 0.0, STALL_AMPS, 980));
        assertTrue(step(w, true, 0.0, STALL_AMPS, 1000));
        assertTrue(step(w, true, -1500.0, 2.0, 1200)); // backing out
        assertFalse(step(w, true, -200.0, 2.0, 1250)); // the burst is over
        assertFalse(step(w, true, FREE_TPS, FREE_AMPS, 1500));
        assertEquals(1, w.bursts());
    }

    @Test
    public void aLowCurrentAtNoSpeedIsNotAStall() {
        // An unplugged encoder reads no speed with the roller spinning free.
        WedgeClear w = new WedgeClear();
        for (long t = 0; t < 5000; t += 20) {
            assertFalse(step(w, true, 0.0, FREE_AMPS, t));
            assertFalse(step(w, true, 0.0, Double.NaN, t + 10));
        }
    }

    @Test
    public void itGivesUpAfterThreeBurstsThatDoNotFreeIt() {
        WedgeClear w = new WedgeClear();
        int starts = 0;
        boolean was = false;
        for (long t = 0; t < 20_000; t += 20) {
            boolean back = step(w, true, 0.0, STALL_AMPS, t);
            if (back && !was) starts++;
            was = back;
        }
        assertEquals(3, starts);
        assertTrue(w.gaveUp(3));
        // running free, then wedged again: it tries again
        assertFalse(step(w, true, FREE_TPS, FREE_AMPS, 20_000));
        assertFalse(w.gaveUp(3));
        assertFalse(step(w, true, 0.0, STALL_AMPS, 20_020));
        assertTrue(step(w, true, 0.0, STALL_AMPS, 21_020));
    }

    @Test
    public void notTakingInNeverBacksOutAndEndsABurst() {
        WedgeClear w = new WedgeClear();
        for (long t = 0; t < 3000; t += 20) assertFalse(step(w, false, 0.0, STALL_AMPS, t));
        step(w, true, 0.0, STALL_AMPS, 3000);
        assertTrue(step(w, true, 0.0, STALL_AMPS, 4000));
        // the lane fills, or the driver lets go, mid-burst: the burst ends at once
        assertFalse(step(w, false, -1000.0, 2.0, 4100));
        assertFalse(step(w, true, FREE_TPS, FREE_AMPS, 4120));
    }

    @Test
    public void zeroBurstsNeverClears() {
        WedgeClear w = new WedgeClear();
        for (long t = 0; t < 5000; t += 20) {
            assertFalse(w.update(true, 0.0, STALL_AMPS, t * MS, 100.0, 5.0, 1000.0, 250.0, 0));
        }
    }
}
