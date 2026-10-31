package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pedropathing.math.Pose;

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

    @Test
    public void goesLimpOnlyAfterTheSettleTimeAndOnlyWhereAsked() {
        assertFalse(FlowerStickSubsystem.shouldGoLimp(0, 1500, true));
        assertFalse(FlowerStickSubsystem.shouldGoLimp(1499, 1500, true));
        assertTrue(FlowerStickSubsystem.shouldGoLimp(1500, 1500, true));
        assertTrue(FlowerStickSubsystem.shouldGoLimp(60_000, 1500, true));
        assertFalse(FlowerStickSubsystem.shouldGoLimp(60_000, 1500, false));
    }

    @Test
    public void staysDownUntilTheRobotIsClearOfTheFlower() {
        // A FLOWER at (30, 40); clear at 18 in (contact 12 + backed 6).
        assertFalse(FlowerStickSubsystem.clearOf(new Pose(30, 52, 0), 30, 40, 18));  // in contact
        assertFalse(FlowerStickSubsystem.clearOf(new Pose(30, 57.9, 0), 30, 40, 18));
        assertTrue(FlowerStickSubsystem.clearOf(new Pose(30, 58, 0), 30, 40, 18));
        assertTrue(FlowerStickSubsystem.clearOf(new Pose(50, 40, 0), 30, 40, 18));    // driven sideways
    }

    @Test
    public void noPoseCountsAsClearSoTheStickCanComeIn() {
        assertTrue(FlowerStickSubsystem.clearOf(null, 30, 40, 18));
    }
}
