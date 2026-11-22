package org.firstinspires.ftc.teamcode.localization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pedropathing.math.Pose;

import org.junit.Test;

/**
 * {@link FusionShadow} against hand-worked Kalman steps. Defaults: initialVarianceXY 0.25 in²,
 * processVarianceXY 1 in² per inch driven (Pedro's FusionLocalizer.updateCovariance).
 */
public class FusionShadowTest {

    private static final double EPS = 1e-9;

    @Test
    public void drivingGrowsTheVarianceAlongTheRobotAndAFrameMovesItByTheGain() {
        FusionShadow f = new FusionShadow();
        f.odometry(new Pose(10, 10, 0), 1000);
        f.odometry(new Pose(20, 10, 0), 2000);      // 10 in forward, facing +x
        assertEquals(10.25, f.varianceX(), EPS);
        assertEquals(0.25, f.varianceY(), EPS);

        assertTrue(f.continuousFix(21, 10, 2000, 10.0, 6.0));   // the camera 1 in ahead
        double k = 10.25 / 20.25;
        assertEquals(k, f.offsetX(), EPS);
        assertEquals(0.0, f.offsetY(), EPS);
        assertEquals(10.25 * 10.0 / 20.25, f.varianceX(), 1e-9);   // Joseph form, optimal gain
        assertEquals(20 + k, f.of(new Pose(20, 10, 0)).x(), EPS);
        assertEquals(1, f.framesUsed());
    }

    @Test
    public void turnedRobotGrowsTheVarianceAlongItsOwnForward() {
        FusionShadow f = new FusionShadow();
        f.odometry(new Pose(0, 0, Math.toRadians(90)), 1000);
        f.odometry(new Pose(0, 8, Math.toRadians(90)), 2000);   // 8 in forward, facing +y
        assertEquals(0.25, f.varianceX(), 1e-9);
        assertEquals(8.25, f.varianceY(), 1e-9);
    }

    @Test
    public void aFrameTooFarFromTheShadowIsNotUsed() {
        FusionShadow f = new FusionShadow();
        f.odometry(new Pose(20, 10, 0), 1000);
        assertFalse(f.continuousFix(27, 10, 1000, 10.0, 6.0));
        assertEquals(0.0, f.offsetIn(), EPS);
        assertEquals(1, f.framesGated());
        assertEquals(0, f.framesUsed());
    }

    @Test
    public void aFrameIsComparedWithThePoseAtItsCaptureTime() {
        FusionShadow f = new FusionShadow();
        f.odometry(new Pose(0, 0, 0), 1000);
        f.odometry(new Pose(10, 0, 0), 2000);
        // Captured halfway: the real pose was at x 5 then, so the camera's 5.5 is 0.5 ahead, not 4.5 behind.
        assertTrue(f.continuousFix(5.5, 0, 1500, 10.0, 6.0));
        assertEquals(0.5 * 10.25 / 20.25, f.offsetX(), EPS);
    }

    @Test
    public void aStillFixAfterADriveIsScoredAndTheShadowStaysPutWhenTheRealPoseJumps() {
        FusionShadow f = new FusionShadow();
        f.odometry(new Pose(0, 0, 0), 1);
        f.stillFix(0, 0, 1.0, new Pose(0, 0, 0), new Pose(0, 0, 0));   // no drive before it: not scored
        assertEquals(0, f.arrivals());
        assertEquals(0.2, f.varianceX(), EPS);                          // 0.25 * 1 / 1.25

        f.odometry(new Pose(10, 0, 0), 2);                               // 10 in of driving
        // The camera says 12; the real pose (still fixes on) jumps from 10 onto it.
        f.stillFix(12, 0, 1.0, new Pose(10, 0, 0), new Pose(12, 0, 0));
        assertEquals(1, f.arrivals());
        assertEquals(2.0, f.lastArrivalRealIn(), EPS);
        assertEquals(2.0, f.lastArrivalShadowIn(), EPS);
        assertEquals(10.0, f.lastArrivalDrivenIn(), EPS);
        double shadowAfter = 10 + 2 * 10.2 / 11.2;                       // its own Kalman step from 10
        assertEquals(shadowAfter, f.of(new Pose(12, 0, 0)).x(), EPS);
        assertTrue(f.summary(), f.summary().startsWith("fusion shadow: 1 stops"));
    }

    @Test
    public void aHardSetClearsTheOffsetAndMatchRealKeepsTheScores() {
        FusionShadow f = new FusionShadow();
        f.odometry(new Pose(0, 0, 0), 1000);
        f.odometry(new Pose(10, 0, 0), 2000);
        f.continuousFix(11, 0, 2000, 10.0, 6.0);
        f.matchReal();
        assertEquals(0.0, f.offsetIn(), EPS);
        assertEquals(1, f.framesUsed());
        f.follow();
        assertEquals(0.25, f.varianceX(), EPS);
        assertFalse(f.continuousFix(11, 0, 3000, 10.0, 6.0));   // no pose since the hard set
        assertTrue(f.summary(), f.summary().startsWith("fusion shadow: no stops scored"));
    }
}
