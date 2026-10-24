package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TurretServoControlTest {

    private static final double EPS = 1e-9;
    private static final long MS = 1_000_000L;

    private static TurretServoControl.Gains bench() {
        // kP, minPower, maxPower, creepBelowDeg, toleranceDeg, creepOnMs, creepOffMs
        return new TurretServoControl.Gains(0.007, 0.12, 1.0, 5.0, 1.0, 30.0, 100.0);
    }

    @Test
    public void insideTheToleranceNothingIsSent() {
        TurretServoControl c = new TurretServoControl();
        assertEquals(0.0, c.power(0.0, bench(), 0), EPS);
        assertEquals(0.0, c.power(0.9, bench(), 0), EPS);
        assertEquals(0.0, c.power(-1.0, bench(), 0), EPS);
        assertEquals(0.0, c.power(Double.NaN, bench(), 0), EPS);
    }

    @Test
    public void drivingIsTheFloorPlusTheGainTowardTheTarget() {
        TurretServoControl c = new TurretServoControl();
        assertEquals(0.12 + 0.007 * 20, c.power(20.0, bench(), 0), EPS);
        assertEquals(-(0.12 + 0.007 * 20), c.power(-20.0, bench(), 0), EPS);
    }

    @Test
    public void drivingNeverExceedsMaxPower() {
        TurretServoControl c = new TurretServoControl();
        assertEquals(1.0, c.power(500.0, bench(), 0), EPS);
        TurretServoControl.Gains gentle = bench();
        gentle.set(0.007, 0.12, 0.3, 5.0, 1.0, 30.0, 100.0);
        assertEquals(0.3, c.power(100.0, gentle, 0), EPS);
    }

    @Test
    public void creepingPulsesTheFloorOnThenOff() {
        TurretServoControl c = new TurretServoControl();
        long t0 = 5_000 * MS;
        assertEquals(0.12, c.power(3.0, bench(), t0), EPS);            // pulse starts
        assertEquals(0.12, c.power(3.0, bench(), t0 + 20 * MS), EPS);  // still inside 30 ms on
        assertEquals(0.0, c.power(3.0, bench(), t0 + 40 * MS), EPS);   // off for 100 ms
        assertEquals(0.0, c.power(3.0, bench(), t0 + 120 * MS), EPS);
        assertEquals(0.12, c.power(3.0, bench(), t0 + 135 * MS), EPS); // next cycle
        assertEquals(-0.12, c.power(-3.0, bench(), t0 + 140 * MS), EPS); // signed toward the target
    }

    @Test
    public void noOffTimeMeansTheFloorIsContinuous() {
        TurretServoControl c = new TurretServoControl();
        TurretServoControl.Gains g = bench();
        g.set(0.007, 0.12, 1.0, 5.0, 1.0, 30.0, 0.0);
        for (int i = 0; i < 10; i++) {
            assertEquals(0.12, c.power(3.0, g, i * 50 * MS), EPS);
        }
    }

    @Test
    public void badGainsBecomeTheHarmlessEnd() {
        TurretServoControl.Gains g = new TurretServoControl.Gains(
                Double.NaN, -1.0, 7.0, Double.POSITIVE_INFINITY, -2.0, Double.NaN, -5.0);
        assertEquals(0.0, g.kP, EPS);
        assertEquals(0.0, g.minPower, EPS);
        assertEquals(1.0, g.maxPower, EPS);
        assertEquals(0.0, g.creepBelowDeg, EPS);
        assertEquals(0.0, g.toleranceDeg, EPS);
        assertEquals(0.0, g.creepOnMs, EPS);
        assertEquals(0.0, g.creepOffMs, EPS);
        TurretServoControl c = new TurretServoControl();
        assertTrue(Math.abs(c.power(50.0, g, 0)) <= 1.0);
    }
}
