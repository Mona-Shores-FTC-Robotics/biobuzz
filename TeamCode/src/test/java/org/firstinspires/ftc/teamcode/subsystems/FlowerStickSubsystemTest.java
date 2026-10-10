package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** The pure part of {@link FlowerStickSubsystem}: what a Panels edit is allowed to do to the servo. */
public class FlowerStickSubsystemTest {

    private static final double EPS = 1e-9;

    @Test
    public void positionsInsideTravelPassThrough() {
        assertEquals(0.0, FlowerStickSubsystem.unitRange(0.0), EPS);
        assertEquals(0.37, FlowerStickSubsystem.unitRange(0.37), EPS);
        assertEquals(1.0, FlowerStickSubsystem.unitRange(1.0), EPS);
    }

    @Test
    public void positionsPastTravelAreClamped() {
        assertEquals(1.0, FlowerStickSubsystem.unitRange(1.7), EPS);
        assertEquals(0.0, FlowerStickSubsystem.unitRange(-0.2), EPS);
    }

    @Test
    public void nanLeavesTheStickIn() {
        assertEquals(0.0, FlowerStickSubsystem.unitRange(Double.NaN), EPS);
    }
}
