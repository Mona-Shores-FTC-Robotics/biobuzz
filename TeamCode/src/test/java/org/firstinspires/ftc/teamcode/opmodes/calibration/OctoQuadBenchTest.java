package org.firstinspires.ftc.teamcode.opmodes.calibration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** The pure part of {@link OctoQuadBench}: a pulse width to degrees, and the wrap the difference uses. */
public class OctoQuadBenchTest {

    private static final double EPS = 1e-9;

    @Test
    public void thePulseRangeMapsToAFullTurn() {
        assertEquals(0.0, OctoQuadBench.degreesFromPulse(1), EPS);
        assertEquals(180.0, OctoQuadBench.degreesFromPulse(513), EPS);
        assertEquals(1023 * 360.0 / 1024.0, OctoQuadBench.degreesFromPulse(1024), EPS);
    }

    @Test
    public void outsideTheRangeIsNoPulse() {
        assertTrue(Double.isNaN(OctoQuadBench.degreesFromPulse(0)));
        assertTrue(Double.isNaN(OctoQuadBench.degreesFromPulse(1025)));
        assertTrue(Double.isNaN(OctoQuadBench.degreesFromPulse(-5)));
    }

    @Test
    public void wrapKeepsDegreesInATurn() {
        assertEquals(10.0, OctoQuadBench.wrap360(370.0), EPS);
        assertEquals(350.0, OctoQuadBench.wrap360(-10.0), EPS);
        assertEquals(0.0, OctoQuadBench.wrap360(720.0), EPS);
        assertTrue(Double.isNaN(OctoQuadBench.wrap360(Double.NaN)));
    }

    @Test
    public void theDifferenceOfTwoAnglesWrapsTheSameWay() {
        double a = OctoQuadBench.degreesFromPulse(20);
        double b = OctoQuadBench.degreesFromPulse(1000);
        double diff = OctoQuadBench.wrap360(a - b);
        assertTrue(diff >= 0.0 && diff < 360.0);
        assertEquals(OctoQuadBench.wrap360(a - b + 360.0), diff, EPS);
    }
}
