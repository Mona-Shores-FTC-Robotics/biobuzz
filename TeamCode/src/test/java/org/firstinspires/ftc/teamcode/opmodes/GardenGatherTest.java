package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertEquals;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.localization.GardenFieldPoints;
import org.junit.Test;

/** {@link GardenGather#strafePose}: an Auto's gather strafes onto the side wall's line first. */
public class GardenGatherTest {

    private static final double EPS = 1e-9;

    /** From R-NoCross's GARDEN_STAGING (11.5, 22.1): sideways onto x 8.5, the same depth, facing in. */
    @Test
    public void fromRedStagingItStrafesOntoTheWallLineAtTheSameDepth() {
        Pose p = GardenGather.strafePose(GardenFieldPoints.RED, new Pose(11.5, 22.1, Math.toRadians(265)), 8, 18);
        assertEquals(GardenFieldPoints.RED.gather.x(), p.x(), EPS);
        assertEquals(22.1, p.y(), EPS);
        assertEquals(GardenFieldPoints.RED.gather.heading(), p.heading(), EPS);
    }

    /** Blue's GARDEN is red's turned half a turn: the same move, turned. */
    @Test
    public void blueIsRedTurnedHalfATurn() {
        double c = 141.5;
        Pose p = GardenGather.strafePose(GardenFieldPoints.BLUE, new Pose(c - 11.5, c - 22.1, Math.toRadians(85)), 8, 18);
        assertEquals(GardenFieldPoints.BLUE.gather.x(), p.x(), EPS);
        assertEquals(c - 22.1, p.y(), EPS);
    }

    /** Too close to the audience wall, it strafes from at least minIn back; too far, from at most maxIn. */
    @Test
    public void theDepthIsHeldBetweenItsLimits() {
        double gy = GardenFieldPoints.RED.gather.y();
        assertEquals(gy + 8, GardenGather.strafePose(GardenFieldPoints.RED, new Pose(11.5, gy + 2, 0), 8, 18).y(), EPS);
        assertEquals(gy + 18, GardenGather.strafePose(GardenFieldPoints.RED, new Pose(11.5, gy + 30, 0), 8, 18).y(), EPS);
    }
}
