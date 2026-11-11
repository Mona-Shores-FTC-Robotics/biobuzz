package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class SwyftSingleBurstTest {
    private static final long MS = 1_000_000L;

    @Test
    public void heldBumperGetsOneBurstAndDoesNotRepeat() {
        SwyftSingleBurst c = new SwyftSingleBurst();
        assertEquals(1, c.power(1, 250, 0), 0);
        assertEquals(1, c.power(1, 250, 249 * MS), 0);
        assertEquals(0, c.power(1, 250, 250 * MS), 0);
        assertEquals(0, c.power(1, 250, 1000 * MS), 0);
        assertEquals(0, c.power(0, 250, 1001 * MS), 0);
        assertEquals(-1, c.power(-1, 250, 1002 * MS), 0);
    }

    @Test
    public void releaseInterruptsImmediatelyAndChangingDirectionCannotRestartTheClock() {
        SwyftSingleBurst c = new SwyftSingleBurst();
        assertEquals(1, c.power(1, 250, 0), 0);
        assertEquals(0, c.power(-1, 250, 10 * MS), 0);
        assertEquals(0, c.power(1, 250, 250 * MS), 0);
        assertEquals(0, c.power(0, 250, 251 * MS), 0);
        assertEquals(-1, c.power(-1, 250, 252 * MS), 0);
        assertEquals(0, c.power(0, 250, 253 * MS), 0);
    }

    @Test
    public void capsDurationAndRejectsBadTimings() {
        SwyftSingleBurst c = new SwyftSingleBurst();
        assertEquals(1, c.power(1, 99999, 0), 0);
        assertEquals(0, c.power(1, 99999, 500 * MS), 0);
        for (double bad : new double[] {0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            c.power(0, bad, 501 * MS);
            assertEquals(0, c.power(1, bad, 502 * MS), 0);
        }
    }
}
