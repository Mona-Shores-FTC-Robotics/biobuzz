package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertEquals;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.localization.GardenFieldPoints;
import org.junit.Test;

/**
 * {@link GardenGather#wallControl}: the drive in's control point, on the side wall's line, level with
 * the start. And {@link GardenGather#lowerOnApproach}: when the drive before an Auto's gather lowers
 * the stick.
 */
public class GardenGatherTest {

    private static final double EPS = 1e-9;

    /**
     * Red: from the line-up spot / GARDEN_STAGING (11.5, 22.1) to an In at (8.0, 9.5) facing the
     * audience wall (270°): the control point is (8.0, 22.1), on the wall's line, level with the start.
     */
    @Test
    public void redsControlIsOnTheWallLineLevelWithTheStart() {
        Pose c = GardenGather.wallControl(new Pose(11.5, 22.1, Math.toRadians(270)), new Pose(8.0, 9.5, Math.toRadians(270)));
        assertEquals(8.0, c.x(), EPS);
        assertEquals(22.1, c.y(), EPS);
    }

    /** Blue's GARDEN is red's turned half a turn: the same control point, turned. */
    @Test
    public void bluesIsRedsTurnedHalfATurn() {
        double s = 141.5;
        Pose c = GardenGather.wallControl(new Pose(s - 11.5, s - 22.1, Math.toRadians(90)), new Pose(s - 8.0, s - 9.5, Math.toRadians(90)));
        assertEquals(s - 8.0, c.x(), EPS);
        assertEquals(s - 22.1, c.y(), EPS);
    }

    /** A start already on the wall's line puts the control point on the straight line: the In stays straight. */
    @Test
    public void aStartOnTheWallLineStaysStraight() {
        Pose c = GardenGather.wallControl(new Pose(8.0, 20.0, Math.toRadians(270)), new Pose(8.0, 9.5, Math.toRadians(270)));
        assertEquals(8.0, c.x(), EPS);
        assertEquals(20.0, c.y(), EPS);
    }

    /**
     * The back out's control point is the drive in's mirror: from the corner (8.0, 9.5) back to the
     * start (11.5, 22.1), it is on the wall's line through the corner, level with the start.
     */
    @Test
    public void theBackOutsControlIsUpTheWallLevelWithTheStart() {
        Pose c = GardenGather.wallControl(new Pose(11.5, 22.1, Math.toRadians(270)), new Pose(8.0, 9.5, Math.toRadians(270)));
        assertEquals(8.0, c.x(), EPS);
        assertEquals(22.1, c.y(), EPS);
    }

    /**
     * The drive to GARDEN_STAGING (11.5, 22.1) lowers the stick over its last approachLowerWithinIn
     * (24 in), and only while the robot faces the GARDEN to within lowerWithinDeg (10°): a stick swung
     * down while the robot still turned in the corner came down on the side wall (30 Oct 2026).
     */
    @Test
    public void theDriveInLowersTheStickOnceCloseAndFacingTheGarden() {
        GardenFieldPoints.Garden red = GardenFieldPoints.RED; // faces the audience wall, 270°
        Pose facing = new Pose(11.5, 44.0, Math.toRadians(270));
        assertEquals("30 in from the drive's end: not yet", false, GardenGather.lowerOnApproach(red, facing, 30.0));
        assertEquals("22 in from the end, facing the GARDEN: now", true, GardenGather.lowerOnApproach(red, facing, 22.0));
        assertEquals("8° off: still facing it", true,
                GardenGather.lowerOnApproach(red, new Pose(11.5, 30.0, Math.toRadians(262)), 8.0));
        // R-NoCross turns half a turn on its way in: 2 in from the end it was still more than 10° off.
        assertEquals("still turning toward the side wall: not yet", false,
                GardenGather.lowerOnApproach(red, new Pose(13.0, 23.0, Math.toRadians(240)), 2.0));
        assertEquals("blue's GARDEN faces 90°", true,
                GardenGather.lowerOnApproach(GardenFieldPoints.BLUE, new Pose(130.0, 100.0, Math.toRadians(92)), 20.0));
    }

    /** Both switches turn the early stick off: without the stick, and with lowerOnDriveIn off. */
    @Test
    public void theEarlyStickHasTwoOffSwitches() {
        Pose facing = new Pose(11.5, 30.0, Math.toRadians(270));
        boolean stick = GardenGather.useStick, early = GardenGather.lowerOnDriveIn;
        try {
            GardenGather.useStick = false;
            assertEquals("no stick in this gather", false, GardenGather.lowerOnApproach(GardenFieldPoints.RED, facing, 8.0));
            GardenGather.useStick = true;
            GardenGather.lowerOnDriveIn = false;
            assertEquals("the card lowers it itself", false, GardenGather.lowerOnApproach(GardenFieldPoints.RED, facing, 8.0));
        } finally {
            GardenGather.useStick = stick;
            GardenGather.lowerOnDriveIn = early;
        }
    }

    /** The approach goes for the GARDEN the gather will: the nearest, on the robot's half in an Auto. */
    @Test
    public void nearestGardenFollowsTheGathersOwnRule() {
        assertEquals(GardenFieldPoints.RED, GardenGather.nearest(11.5, 22.1, true));
        assertEquals(GardenFieldPoints.BLUE, GardenGather.nearest(130.0, 119.4, true));
        // (72, 60) is on blue's half of the centre line and nearer red's GARDEN.
        assertEquals("an Auto never crosses the centre line", GardenFieldPoints.BLUE, GardenGather.nearest(72.0, 60.0, true));
        assertEquals("the button may", GardenFieldPoints.RED, GardenGather.nearest(72.0, 60.0, false));
    }
}
