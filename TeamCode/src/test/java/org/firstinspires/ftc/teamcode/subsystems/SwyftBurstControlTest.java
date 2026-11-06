package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class SwyftBurstControlTest {
    private static final long MS = 1_000_000L;

    @Test
    public void burstsAreFullDirectionFollowedByOffNotProportionalPower() {
        SwyftBurstControl c = new SwyftBurstControl();
        assertEquals(1, c.power(.12, 25, 250, 0), 0);
        assertEquals(1, c.power(.12, 25, 250, 24 * MS), 0);
        assertEquals(0, c.power(.12, 25, 250, 25 * MS), 0);
        assertEquals(0, c.power(.12, 25, 250, 274 * MS), 0);
        assertEquals(1, c.power(.12, 25, 250, 275 * MS), 0);
    }

    @Test
    public void stopInterruptsABurstAndRearmingStartsANewOne() {
        SwyftBurstControl c = new SwyftBurstControl();
        assertEquals(-1, c.power(-1, 25, 250, 0), 0);
        assertEquals(0, c.power(0, 25, 250, 10 * MS), 0);
        assertEquals(-1, c.power(-1, 25, 250, 12 * MS), 0);
        assertEquals(-1, c.power(-1, 25, 250, 36 * MS), 0);
        assertEquals(0, c.power(-1, 25, 250, 37 * MS), 0);
    }

    @Test
    public void reversingWaitsForAnOffInterval() {
        SwyftBurstControl c = new SwyftBurstControl();
        assertEquals(1, c.power(1, 25, 250, 0), 0);
        assertEquals(0, c.power(-1, 25, 250, 10 * MS), 0);
        assertEquals(0, c.power(-1, 25, 250, 259 * MS), 0);
        assertEquals(-1, c.power(-1, 25, 250, 260 * MS), 0);
    }

    @Test
    public void badInputsStopAndTimingBoundsPreventLongContinuousCommands() {
        SwyftBurstControl c = new SwyftBurstControl();
        for (double bad : new double[] {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            assertEquals(0, c.power(bad, 25, 250, 0), 0);
            assertEquals(0, c.power(1, bad, 250, 0), 0);
            assertEquals(0, c.power(1, 25, bad, 0), 0);
        }
        assertEquals(0, c.power(1, 0, 250, 0), 0);
        assertEquals(0, c.power(1, 25, -1, 0), 0);
        assertEquals(1, c.power(1, 99999, 0, 0), 0);
        assertEquals(0, c.power(1, 99999, 0, 100 * MS), 0);
        assertEquals(0, c.power(1, 99999, 0, 199 * MS), 0);
        assertEquals(1, c.power(1, 99999, 0, 200 * MS), 0);
    }
}
