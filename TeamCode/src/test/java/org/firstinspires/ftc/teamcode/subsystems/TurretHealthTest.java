package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TurretHealthTest {

    private static final TurretCalibration MEASURED = new TurretCalibration(200.0, 1);
    private static final double TOLERANCE = 3.0;

    @Test
    public void noReadingIsNoSignal() {
        assertEquals(TurretHealth.NO_SIGNAL,
                TurretHealth.classify(Double.NaN, MEASURED, TOLERANCE));
        assertFalse(TurretHealth.NO_SIGNAL.hasSignal());
    }

    @Test
    public void atHomeWithinTheToleranceIsHome() {
        assertEquals(TurretHealth.HOME, TurretHealth.classify(200.0, MEASURED, TOLERANCE));
        assertEquals(TurretHealth.HOME, TurretHealth.classify(202.5, MEASURED, TOLERANCE));
        assertEquals(TurretHealth.HOME, TurretHealth.classify(197.0, MEASURED, TOLERANCE));
    }

    @Test
    public void offHomeIsNotHome() {
        assertEquals(TurretHealth.NOT_HOME, TurretHealth.classify(203.5, MEASURED, TOLERANCE));
        assertEquals(TurretHealth.NOT_HOME, TurretHealth.classify(20.0, MEASURED, TOLERANCE));
        assertTrue(TurretHealth.NOT_HOME.hasSignal());
    }

    @Test
    public void homeIsJudgedAcrossTheEncodersZero() {
        TurretCalibration nearZero = new TurretCalibration(359.0, 1);
        assertEquals(TurretHealth.HOME, TurretHealth.classify(1.0, nearZero, TOLERANCE));
    }

    @Test
    public void unmeasuredValuesAreUncalibratedNeverAGuess() {
        assertEquals(TurretHealth.UNCALIBRATED,
                TurretHealth.classify(200.0, new TurretCalibration(Double.NaN, 1), TOLERANCE));
        assertEquals(TurretHealth.UNCALIBRATED,
                TurretHealth.classify(200.0, new TurretCalibration(200.0, 0), TOLERANCE));
        assertEquals(TurretHealth.UNCALIBRATED,
                TurretHealth.classify(200.0, MEASURED, Double.NaN));
        assertTrue(TurretHealth.UNCALIBRATED.hasSignal());
    }
}
