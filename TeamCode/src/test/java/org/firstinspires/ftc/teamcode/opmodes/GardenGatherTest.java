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
     * The two-step drive in's first leg ends on the gather pose's own line along the side wall (red's
     * x 8.5, the side 0.4 in off the wall), level with GARDEN_STAGING, not laneTowardWallIn into the
     * wall: the push that follows presses it there. Blue's is red's turned half a turn.
     */
    @Test
    public void theTwoStepFirstLegEndsOnTheGatherLineLevelWithTheStart() {
        Pose red = GardenGather.overTo(GardenFieldPoints.RED, new Pose(11.5, 22.1, Math.toRadians(270)));
        assertEquals(8.5, red.x(), EPS);
        assertEquals(22.1, red.y(), EPS);
        assertEquals(Math.toRadians(270), red.heading(), EPS);
        double s = 141.5;
        Pose blue = GardenGather.overTo(GardenFieldPoints.BLUE, new Pose(s - 11.5, s - 22.1, Math.toRadians(90)));
        assertEquals(s - 8.5, blue.x(), 1e-6);
        assertEquals(s - 22.1, blue.y(), 1e-6);
    }

    /**
     * The drive to GARDEN_STAGING lowers the stick over its last approachLowerWithinIn (24 in), and
     * only once the robot has faced the GARDEN for facingHoldMs (200 ms) without a break: the stick
     * came down on the field wall when lowered while the robot turned or faced another way.
     */
    @Test
    public void theDriveInLowersTheStickOnceCloseAndTheTurnIsOver() {
        assertEquals("30 in from the drive's end: not yet", false, GardenGather.lowerOnApproach(30.0, 1000));
        assertEquals("22 in from the end, facing for a second: now", true, GardenGather.lowerOnApproach(22.0, 1000));
        assertEquals("just turned to face it: not yet", false, GardenGather.lowerOnApproach(22.0, 50));
        // R-NoCross turns half a turn evenly on its way in: it faces the GARDEN only for its last 0.1 s.
        assertEquals("facing for 100 ms, 2 in out: the card lowers it, standing", false,
                GardenGather.lowerOnApproach(2.0, 100));
    }

    /** Facing is within lowerWithinDeg (10°) of the GARDEN's heading: red's 270°, blue's 90°. */
    @Test
    public void facingIsWithinTenDegreesOfTheGardensHeading() {
        assertEquals(true, GardenGather.facing(GardenFieldPoints.RED, Math.toRadians(262)));
        assertEquals(true, GardenGather.facing(GardenFieldPoints.RED, Math.toRadians(-90)));
        assertEquals("22° off, as R-NoCross is 6 in out", false, GardenGather.facing(GardenFieldPoints.RED, Math.toRadians(248)));
        assertEquals("facing the side wall", false, GardenGather.facing(GardenFieldPoints.RED, Math.toRadians(180)));
        assertEquals(true, GardenGather.facing(GardenFieldPoints.BLUE, Math.toRadians(92)));
        assertEquals(false, GardenGather.facing(GardenFieldPoints.BLUE, Math.toRadians(270)));
    }

    /** Both switches turn the early stick off: without the stick, and with lowerOnDriveIn off. */
    @Test
    public void theEarlyStickHasTwoOffSwitches() {
        boolean stick = GardenGather.useStick, early = GardenGather.lowerOnDriveIn;
        try {
            GardenGather.useStick = false;
            assertEquals("no stick in this gather", false, GardenGather.lowerOnApproach(8.0, 1000));
            GardenGather.useStick = true;
            GardenGather.lowerOnDriveIn = false;
            assertEquals("the card lowers it itself", false, GardenGather.lowerOnApproach(8.0, 1000));
        } finally {
            GardenGather.useStick = stick;
            GardenGather.lowerOnDriveIn = early;
        }
    }

    /** The approach goes for the GARDEN the gather will: the nearest in range, on the robot's half in an Auto. */
    @Test
    public void nearestGardenFollowsTheGathersOwnRule() {
        assertEquals(GardenFieldPoints.RED, GardenGather.nearest(11.5, 22.1, true));
        assertEquals(GardenFieldPoints.BLUE, GardenGather.nearest(130.0, 119.4, true));
        assertEquals("out of range (maxRangeIn 45)", null, GardenGather.nearest(60.0, 60.0, true));
    }

    /**
     * The drive back is done where it stands once it has stalled: 14 Nov's two slow back outs crept
     * an inch short for 1.5 and 2.8 s. Not in its first moments, and not while it still gets closer.
     */
    @Test
    public void theBackOutEndsWhenItStallsShort() {
        assertEquals("stalled for 0.5 s, 1.4 s in", true, GrabRoutine.backStalled(1400, 500, 400));
        assertEquals("still getting closer", false, GrabRoutine.backStalled(600, 50, 400));
        assertEquals("the path's first moments, ramping up", false, GrabRoutine.backStalled(500, 450, 400));
        assertEquals("off", false, GrabRoutine.backStalled(3000, 2500, 0));
    }

    /**
     * The stick comes down only while the robot turns slower than lowerMaxTurnDegPerSec (30°/s): on
     * 14 Nov it was lowered 12 times with the robot turning 57 to 148°/s.
     */
    @Test
    public void theStickWaitsWhileTheRobotIsStillTurning() {
        assertEquals("held still", true, GardenGather.steady(0.0));
        assertEquals("20°/s, the tail of a turn", true, GardenGather.steady(Math.toRadians(-20)));
        assertEquals("57°/s", false, GardenGather.steady(Math.toRadians(57)));
        assertEquals("148°/s the other way", false, GardenGather.steady(Math.toRadians(-148)));
        assertEquals("no Pinpoint: not held back", true, GardenGather.steady(Double.NaN));
    }
}
