package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TurretServoControlTest {

    private static final double EPS = 1e-9;
    private static final long MS = 1_000_000L;

    private static TurretServoControl.Gains bench() {
        // kP, kD, creepMaxRateDps, minPower, maxPower, creepBelowDeg, toleranceDeg, creepOnMs, creepOffMs
        return new TurretServoControl.Gains(0.007, 0.0, 30.0, 0.12, 1.0, 5.0, 1.0, 30.0, 100.0);
    }

    @Test
    public void insideTheToleranceNothingIsSent() {
        TurretServoControl c = new TurretServoControl();
        assertEquals(0.0, c.power(0.0, 0.0, bench(), 0), EPS);
        assertEquals(0.0, c.power(0.9, 0.0, bench(), 0), EPS);
        assertEquals(0.0, c.power(-1.0, 0.0, bench(), 0), EPS);
        assertEquals(0.0, c.power(Double.NaN, 0.0, bench(), 0), EPS);
    }

    @Test
    public void drivingIsTheFloorPlusTheGainTowardTheTarget() {
        TurretServoControl c = new TurretServoControl();
        assertEquals(0.12 + 0.007 * 20, c.power(20.0, 0.0, bench(), 0), EPS);
        assertEquals(-(0.12 + 0.007 * 20), c.power(-20.0, 0.0, bench(), 0), EPS);
    }

    @Test
    public void drivingNeverExceedsMaxPower() {
        TurretServoControl c = new TurretServoControl();
        assertEquals(1.0, c.power(500.0, 0.0, bench(), 0), EPS);
        TurretServoControl.Gains gentle = bench();
        gentle.set(0.007, 0.0, 30.0, 0.12, 0.3, 5.0, 1.0, 30.0, 100.0);
        assertEquals(0.3, c.power(100.0, 0.0, gentle, 0), EPS);
    }

    @Test
    public void creepingPulsesTheFloorOnThenOff() {
        TurretServoControl c = new TurretServoControl();
        long t0 = 5_000 * MS;
        assertEquals(0.12, c.power(3.0, 0.0, bench(), t0), EPS);            // pulse starts
        assertEquals(0.12, c.power(3.0, 0.0, bench(), t0 + 20 * MS), EPS);  // still inside 30 ms on
        assertEquals(0.0, c.power(3.0, 0.0, bench(), t0 + 40 * MS), EPS);   // off for 100 ms
        assertEquals(0.0, c.power(3.0, 0.0, bench(), t0 + 120 * MS), EPS);
        assertEquals(0.12, c.power(3.0, 0.0, bench(), t0 + 135 * MS), EPS); // next cycle
        assertEquals(-0.12, c.power(-3.0, 0.0, bench(), t0 + 140 * MS), EPS); // signed toward the target
    }

    @Test
    public void dampingTakesPowerOffAsTheTurretSpeedsUp() {
        TurretServoControl c = new TurretServoControl();
        TurretServoControl.Gains g = bench();
        g.set(0.007, 0.002, 30.0, 0.12, 1.0, 5.0, 1.0, 30.0, 100.0);
        double still = c.power(40.0, 0.0, g, 0);
        double moving = c.power(40.0, 100.0, g, 0);          // 100 deg/s toward the target
        assertEquals(0.12 + 0.007 * 40, still, EPS);
        assertEquals(still - 0.002 * 100, moving, EPS);
        // Moving the wrong way adds power.
        assertEquals(still + 0.002 * 100, c.power(40.0, -100.0, g, 0), EPS);
    }

    @Test
    public void arrivingFastInTheCreepZoneBrakesInsteadOfCreeping() {
        TurretServoControl c = new TurretServoControl();
        TurretServoControl.Gains g = bench();
        g.set(0.007, 0.002, 30.0, 0.12, 1.0, 5.0, 1.0, 30.0, 100.0);
        assertEquals(-0.002 * 200, c.power(3.0, 200.0, g, 0), EPS);   // brake
        assertEquals(0.12, c.power(3.0, 10.0, g, 0), EPS);            // slow enough: creep pulse
        assertEquals(-0.002 * 200, c.power(0.5, 200.0, g, 0), EPS);   // even inside the band
    }

    @Test
    public void noOffTimeMeansTheFloorIsContinuous() {
        TurretServoControl c = new TurretServoControl();
        TurretServoControl.Gains g = bench();
        g.set(0.007, 0.0, 30.0, 0.12, 1.0, 5.0, 1.0, 30.0, 0.0);
        for (int i = 0; i < 10; i++) {
            assertEquals(0.12, c.power(3.0, 0.0, g, i * 50 * MS), EPS);
        }
    }

    @Test
    public void badGainsBecomeTheHarmlessEnd() {
        TurretServoControl.Gains g = new TurretServoControl.Gains(
                Double.NaN, -0.5, Double.NaN, -1.0, 7.0, Double.POSITIVE_INFINITY, -2.0, Double.NaN, -5.0);
        assertEquals(0.0, g.kP, EPS);
        assertEquals(0.0, g.kD, EPS);
        assertEquals(0.0, g.creepMaxRateDps, EPS);
        assertEquals(0.0, g.minPower, EPS);
        assertEquals(1.0, g.maxPower, EPS);
        assertEquals(0.0, g.creepBelowDeg, EPS);
        assertEquals(0.0, g.toleranceDeg, EPS);
        assertEquals(0.0, g.creepOnMs, EPS);
        assertEquals(0.0, g.creepOffMs, EPS);
        TurretServoControl c = new TurretServoControl();
        assertTrue(Math.abs(c.power(50.0, 0.0, g, 0)) <= 1.0);
    }
}
