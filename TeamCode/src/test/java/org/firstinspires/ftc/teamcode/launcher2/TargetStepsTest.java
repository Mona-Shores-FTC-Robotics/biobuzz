package org.firstinspires.ftc.teamcode.launcher2;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * The team's one hard requirement for the two-wheel rig: the target RPM moves
 * by the same increments as on the three-wheel Flywheel Speed Test.
 */
public class TargetStepsTest {

    private static final double EPS = 1e-9;

    @Test
    public void eachPressMovesByExactlyTheStep() {
        double rpm = 1500;
        rpm = TargetSteps.step(rpm, 25, 6000);
        assertEquals(1525, rpm, EPS);
        rpm = TargetSteps.step(rpm, 100, 6000);
        assertEquals(1625, rpm, EPS);
        rpm = TargetSteps.step(rpm, -25, 6000);
        assertEquals(1600, rpm, EPS);
        rpm = TargetSteps.step(rpm, -100, 6000);
        assertEquals(1500, rpm, EPS);
    }

    @Test
    public void manyStepsAccumulateWithoutDrift() {
        double rpm = 0;
        for (int i = 0; i < 200; i++) {
            rpm = TargetSteps.step(rpm, 25, 6000);
        }
        assertEquals(5000, rpm, EPS);
    }

    /** No grid: a hand-typed 1510 steps to 1535, keeping the step size. */
    @Test
    public void anOffGridTargetKeepsTheStepSize() {
        assertEquals(1535, TargetSteps.step(1510, 25, 6000), EPS);
    }

    @Test
    public void neverGoesBelowZero() {
        assertEquals(0, TargetSteps.step(10, -25, 6000), EPS);
        assertEquals(0, TargetSteps.step(0, -100, 6000), EPS);
    }

    @Test
    public void neverGoesAboveTheCeiling() {
        assertEquals(6000, TargetSteps.step(5990, 25, 6000), EPS);
        assertEquals(6000, TargetSteps.step(6000, 100, 6000), EPS);
    }

    @Test
    public void aNegativeCeilingIsTreatedAsZero() {
        assertEquals(0, TargetSteps.step(100, 25, -1), EPS);
    }
}
