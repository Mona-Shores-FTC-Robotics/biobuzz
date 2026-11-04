package org.firstinspires.ftc.teamcode.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Surface speed from encoder ticks: revolutions × ratio × circumference, unknown when unmeasured. */
public class WheelSpeedTest {

    private static final double EPS = 1e-9;

    @Test
    public void feederAtTwentyTwoHundredTicks() {
        double d = 48.0 / 25.4;
        assertEquals(2200.0 / 384.5 * Math.PI * d, WheelSpeed.surfaceInchesPerSecond(2200, 384.5, 1.0, d), EPS);
        assertEquals("direction removed", WheelSpeed.surfaceInchesPerSecond(2200, 384.5, 1.0, d),
                WheelSpeed.surfaceInchesPerSecond(-2200, 384.5, 1.0, d), EPS);
    }

    @Test
    public void aRatioScalesTheWheel() {
        assertEquals(1595.0 / 28 * 0.5 * Math.PI * 4.0, WheelSpeed.surfaceInchesPerSecond(1595, 28, 0.5, 4.0), EPS);
    }

    @Test
    public void unmeasuredIsUnknown() {
        assertTrue(Double.isNaN(WheelSpeed.surfaceInchesPerSecond(1595, 28, Double.NaN, 4.0)));
        assertTrue(Double.isNaN(WheelSpeed.surfaceInchesPerSecond(1595, 28, 1.0, Double.NaN)));
        assertTrue(Double.isNaN(WheelSpeed.surfaceInchesPerSecond(1595, 0, 1.0, 4.0)));
        assertTrue(Double.isNaN(WheelSpeed.surfaceInchesPerSecond(1595, 28, -1.0, 4.0)));
        assertTrue(Double.isNaN(WheelSpeed.surfaceInchesPerSecond(Double.NaN, 28, 1.0, 4.0)));
    }
}
