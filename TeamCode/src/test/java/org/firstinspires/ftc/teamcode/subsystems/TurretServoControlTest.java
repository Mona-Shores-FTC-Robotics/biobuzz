package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TurretServoControlTest {

    private static final double EPS = 1e-9;
    private static final long MS = 1_000_000L;

    private static TurretServoControl.Gains bench() {
        // kP, kD, creepMaxRateDps, minPower, creepPower, maxPower, creepBelowDeg, toleranceDeg, creepOnMs, creepOffMs
        return new TurretServoControl.Gains(0.007, 0.0, 30.0, 0.12, 0.0, 1.0, 5.0, 1.0, 30.0, 100.0);
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
        gentle.set(0.007, 0.0, 30.0, 0.12, 0.0, 0.3, 5.0, 1.0, 30.0, 100.0);
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
    public void dampingEasesTheDriveOffButNeverTurnsItAround() {
        TurretServoControl c = new TurretServoControl();
        TurretServoControl.Gains g = bench();
        g.set(0.007, 0.002, 30.0, 0.12, 0.0, 1.0, 5.0, 1.0, 30.0, 100.0);
        double still = c.power(40.0, 0.0, g, 0);
        assertEquals(0.12 + 0.007 * 40, still, EPS);
        // 100 deg/s toward the target: eased off.
        assertEquals(still - 0.002 * 100, c.power(40.0, 100.0, g, 0), EPS);
        // Very fast toward the target: eased to zero, never reversed.
        assertEquals(0.0, c.power(40.0, 1000.0, g, 0), EPS);
        // Moving away from the target: the full drive, damping does not add to it.
        assertEquals(still, c.power(40.0, -100.0, g, 0), EPS);
        // Same on the negative side.
        assertEquals(-(still - 0.002 * 100), c.power(-40.0, -100.0, g, 0), EPS);
    }

    @Test
    public void arrivingFastBrakesGentlyAndOnlyAgainstMotionTowardTheTarget() {
        TurretServoControl c = new TurretServoControl();
        TurretServoControl.Gains g = bench();
        g.set(0.007, 0.002, 30.0, 0.12, 0.0, 1.0, 5.0, 1.0, 30.0, 100.0);
        // Coming in at 200 deg/s: a brake, capped at minPower, never a reversal at speed.
        assertEquals(-0.12, c.power(3.0, 200.0, g, 0), EPS);
        assertEquals(-0.12, c.power(0.5, 200.0, g, 0), EPS);
        // A slow brake inside the cap.
        assertEquals(-0.002 * 40, c.power(3.0, 40.0, g, 0), EPS);
        // Slow enough: a creep pulse toward the target.
        assertEquals(0.12, c.power(3.0, 10.0, g, 0), EPS);
        // Moving away fast in the creep zone: not braked (that would push it further away); creep toward.
        assertEquals(0.12, c.power(3.0, -200.0, g, 0), EPS);
        // Inside the band and moving away: nothing.
        assertEquals(0.0, c.power(0.5, -200.0, g, 0), EPS);
    }

    @Test
    public void brakingOffWhenCreepMaxRateIsZero() {
        TurretServoControl c = new TurretServoControl();
        TurretServoControl.Gains g = bench();
        g.set(0.007, 0.002, 0.0, 0.12, 0.0, 1.0, 5.0, 1.0, 30.0, 100.0);
        assertEquals(0.12, c.power(3.0, 500.0, g, 0), EPS);
        assertEquals(0.0, c.power(0.5, 500.0, g, 0), EPS);
    }

    @Test
    public void aCreepPulseMayBeStrongerThanTheFloor() {
        TurretServoControl c = new TurretServoControl();
        TurretServoControl.Gains g = bench();
        g.set(0.004, 0.0, 0.0, 0.07, 0.12, 1.0, 10.0, 1.0, 25.0, 100.0);
        assertEquals(0.12, c.power(3.0, 0.0, g, 0), EPS);              // the pulse
        assertEquals(0.07 + 0.004 * 20, c.power(20.0, 0.0, g, 0), EPS); // the drive floor
        // 0 means the pulse is the floor.
        g.set(0.004, 0.0, 0.0, 0.07, 0.0, 1.0, 10.0, 1.0, 25.0, 100.0);
        assertEquals(0.07, c.power(3.0, 0.0, g, 0), EPS);
    }

    @Test
    public void noOffTimeMeansTheFloorIsContinuous() {
        TurretServoControl c = new TurretServoControl();
        TurretServoControl.Gains g = bench();
        g.set(0.007, 0.0, 30.0, 0.12, 0.0, 1.0, 5.0, 1.0, 30.0, 0.0);
        for (int i = 0; i < 10; i++) {
            assertEquals(0.12, c.power(3.0, 0.0, g, i * 50 * MS), EPS);
        }
    }

    @Test
    public void badGainsBecomeTheHarmlessEnd() {
        TurretServoControl.Gains g = new TurretServoControl.Gains(
                Double.NaN, -0.5, Double.NaN, -1.0, Double.NaN, 7.0, Double.POSITIVE_INFINITY, -2.0, Double.NaN, -5.0);
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
