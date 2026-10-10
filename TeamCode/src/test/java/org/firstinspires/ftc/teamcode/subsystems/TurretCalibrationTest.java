package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.hardware.RobotIdentity;
import org.junit.Test;

public class TurretCalibrationTest {

    private static final double EPS = 1e-9;

    @Test
    public void aPulseBecomesTheEncodersAngle() {
        assertEquals(360.0 / 1024.0, TurretCalibration.rawDegrees(1), EPS);
        assertEquals(180.0, TurretCalibration.rawDegrees(512), EPS);
        // The widest pulse is a full turn, which is zero again.
        assertEquals(0.0, TurretCalibration.rawDegrees(1024), EPS);
    }

    @Test
    public void aPulseNoThruBoreMakesIsNoReading() {
        // An unplugged encoder reads as no pulse at all.
        assertTrue(Double.isNaN(TurretCalibration.rawDegrees(0)));
        assertTrue(Double.isNaN(TurretCalibration.rawDegrees(1025)));
        assertTrue(Double.isNaN(TurretCalibration.rawDegrees(-5)));
    }

    @Test
    public void theAngleIsMeasuredFromHome() {
        TurretCalibration cal = new TurretCalibration(100.0, 1);
        assertEquals(0.0, cal.turretDegrees(100.0), EPS);
        assertEquals(30.0, cal.turretDegrees(130.0), EPS);
        assertEquals(-30.0, cal.turretDegrees(70.0), EPS);
    }

    @Test
    public void aReversedEncoderStillReadsCcwPositive() {
        TurretCalibration cal = new TurretCalibration(100.0, -1);
        assertEquals(-30.0, cal.turretDegrees(130.0), EPS);
        assertEquals(30.0, cal.turretDegrees(70.0), EPS);
    }

    @Test
    public void theAngleWrapsAcrossTheEncodersZero() {
        // Home just below 360: a reading just past zero is a few degrees CCW, not 358 degrees.
        TurretCalibration cal = new TurretCalibration(358.0, 1);
        assertEquals(4.0, cal.turretDegrees(2.0), EPS);
        assertEquals(-8.0, cal.turretDegrees(350.0), EPS);
    }

    @Test
    public void theTurretPointingStraightBackIsPlus180() {
        TurretCalibration cal = new TurretCalibration(0.0, 1);
        assertEquals(180.0, cal.turretDegrees(180.0), EPS);
        assertEquals(179.0, cal.turretDegrees(179.0), EPS);
        assertEquals(-179.0, cal.turretDegrees(181.0), EPS);
    }

    @Test
    public void wrapKeepsEveryAngleInMinus180To180() {
        assertEquals(180.0, TurretCalibration.wrap180(180.0), EPS);
        assertEquals(180.0, TurretCalibration.wrap180(-180.0), EPS);
        assertEquals(-90.0, TurretCalibration.wrap180(270.0), EPS);
        assertEquals(10.0, TurretCalibration.wrap180(730.0), EPS);
        assertEquals(-10.0, TurretCalibration.wrap180(-730.0), EPS);
    }

    @Test
    public void anUnmeasuredRobotHasNoAngle() {
        assertFalse(new TurretCalibration(Double.NaN, 1).calibrated());
        assertFalse(new TurretCalibration(100.0, 0).calibrated());
        assertTrue(Double.isNaN(new TurretCalibration(Double.NaN, 1).turretDegrees(50.0)));
        assertTrue(Double.isNaN(new TurretCalibration(100.0, 0).turretDegrees(50.0)));
    }

    @Test
    public void noReadingMeansNoAngle() {
        assertTrue(Double.isNaN(new TurretCalibration(100.0, 1).turretDegrees(Double.NaN)));
    }

    /**
     * Each competition robot gets its own values, and the rig (no turret) never borrows a robot's.
     * Measured values must be NaN/0 or real — a direction of anything but -1, 0 or +1 is a typo.
     */
    @Test
    public void everyRobotHasItsOwnWellFormedValues() {
        for (RobotIdentity robot : RobotIdentity.values()) {
            TurretCalibration cal = TurretCalibration.forRobot(robot);
            assertTrue(robot + " direction must be -1, 0 (not measured) or +1",
                    cal.direction == -1 || cal.direction == 0 || cal.direction == 1);
            assertTrue(robot + " home offset must be NaN (not measured) or in [0, 360)",
                    Double.isNaN(cal.homeRawDeg) || (cal.homeRawDeg >= 0 && cal.homeRawDeg < 360));
        }
        assertFalse(TurretCalibration.forRobot(RobotIdentity.LAUNCHER_RIG).calibrated());
    }

    @Test
    public void theHomeToleranceIsUnmeasuredOrSensible() {
        double tol = TurretCalibration.HOME_TOLERANCE_DEG;
        assertTrue("HOME_TOLERANCE_DEG must be NaN (not measured) or between 0 and 45 degrees",
                Double.isNaN(tol) || (tol > 0 && tol <= 45));
    }
}
