package org.firstinspires.ftc.teamcode.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Scaling by the battery: nominal over battery, exactly 1 for anything that is not a real reading. */
public class VoltageCompensationTest {

    private static final double EPS = 1e-9;

    @Test
    public void theScaleIsNominalOverBattery() {
        assertEquals(12.0 / 10.6, VoltageCompensation.scale(true, 12.0, 10.6), EPS);
        assertEquals(12.0 / 12.5, VoltageCompensation.scale(true, 12.0, 12.5), EPS);
        assertEquals(1.0, VoltageCompensation.scale(true, 12.0, 12.0), EPS);
    }

    @Test
    public void offOrABadReadingIsExactlyOne() {
        assertEquals(1.0, VoltageCompensation.scale(false, 12.0, 10.6), 0.0);
        assertEquals(1.0, VoltageCompensation.scale(true, 12.0, Double.NaN), 0.0);
        assertEquals(1.0, VoltageCompensation.scale(true, 12.0, 0.0), 0.0);
        assertEquals(1.0, VoltageCompensation.scale(true, 12.0, -3.0), 0.0);
        assertEquals(1.0, VoltageCompensation.scale(true, Double.NaN, 11.0), 0.0);
        assertEquals(1.0, VoltageCompensation.scale(true, -12.0, 11.0), 0.0);
        assertEquals(1.0, VoltageCompensation.scale(true, Double.POSITIVE_INFINITY, 11.0), 0.0);
    }

    @Test
    public void aWildReadingIsLimited() {
        assertEquals(VoltageCompensation.MAX_SCALE, VoltageCompensation.scale(true, 12.0, 2.0), EPS);
        assertEquals(VoltageCompensation.MIN_SCALE, VoltageCompensation.scale(true, 12.0, 60.0), EPS);
    }

    @Test
    public void aPowerIsScaledAndKeptInRangeWithItsSign() {
        assertEquals(0.85 * 12.0 / 10.6, VoltageCompensation.power(0.85, 12.0 / 10.6), EPS);
        assertEquals(-0.85 * 12.0 / 10.6, VoltageCompensation.power(-0.85, 12.0 / 10.6), EPS);
        assertEquals(1.0, VoltageCompensation.power(1.0, 1.2), EPS);
        assertEquals(-1.0, VoltageCompensation.power(-1.0, 1.2), EPS);
        assertEquals(0.96, VoltageCompensation.power(1.0, 0.96), EPS);
        assertEquals(0.0, VoltageCompensation.power(0.0, 1.2), 0.0);
    }

    @Test
    public void aBadPowerIsZeroAndABadScaleChangesNothing() {
        assertEquals(0.0, VoltageCompensation.power(Double.NaN, 1.1), 0.0);
        assertEquals(0.5, VoltageCompensation.power(0.5, Double.NaN), 0.0);
        assertEquals(0.5, VoltageCompensation.power(0.5, 0.0), 0.0);
    }

    @Test
    public void onlyAChangeBiggerThanTheStepIsWorthAWrite() {
        assertTrue("nothing applied yet", VoltageCompensation.worthApplying(Double.NaN, 1.05, 0.01));
        assertFalse(VoltageCompensation.worthApplying(1.05, 1.05, 0.01));
        assertFalse("half a percent", VoltageCompensation.worthApplying(1.05, 1.055, 0.01));
        assertTrue("two percent", VoltageCompensation.worthApplying(1.05, 1.071, 0.01));
        assertTrue("back to 1: switched off", VoltageCompensation.worthApplying(1.005, 1.0, 0.01));
    }

    /** 20245, Belding 14:39 R-Solo: 12.94 V before PLAY, 9.57 V caught at PLAY, 11.66 V a second later. */
    @Test
    public void aLoneSagIsNotTakenAndTheNextReadingIs() {
        VoltageCompensation.Steady steady = new VoltageCompensation.Steady();
        assertTrue(Double.isNaN(steady.volts()));
        assertEquals(12.94, steady.update(12.94), EPS);
        assertEquals("the sag at PLAY", 12.94, steady.update(9.57), EPS);
        assertEquals("a real drop is taken one reading later", 11.66, steady.update(11.66), EPS);
    }

    @Test
    public void aDropThatLastsIsTakenOneReadingLate() {
        VoltageCompensation.Steady steady = new VoltageCompensation.Steady();
        steady.update(12.5);
        assertEquals(12.5, steady.update(11.0), EPS);
        assertEquals(11.0, steady.update(11.0), EPS);
        assertEquals("a rise is taken at once", 12.0, steady.update(12.0), EPS);
    }

    @Test
    public void aMissingReadingChangesNothing() {
        VoltageCompensation.Steady steady = new VoltageCompensation.Steady();
        assertTrue(Double.isNaN(steady.update(Double.NaN)));
        steady.update(12.0);
        assertEquals(12.0, steady.update(Double.NaN), EPS);
        assertEquals("the reading before the gap still counts", 12.0, steady.update(10.0), EPS);
    }
}
