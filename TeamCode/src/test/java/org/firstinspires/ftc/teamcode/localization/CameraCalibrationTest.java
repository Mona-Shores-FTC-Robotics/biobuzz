package org.firstinspires.ftc.teamcode.localization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.vision.CameraMount;
import org.firstinspires.ftc.teamcode.vision.Vec3;
import org.junit.Test;

/**
 * The sign of the correction, closed through the real chain: a camera-space sighting goes through
 * {@code CameraMount.toRobotFrame} with a wrong mount, {@code CellFix} turns it into a robot
 * position, the residual against the true robot goes into {@link CameraCalibration}, and its
 * suggestion must be the true mount, at any heading.
 */
public class CameraCalibrationTest {

    private static final double TRUE_FWD = 3.55, TRUE_LEFT = 1.05, UP = 14.0, PITCH = 45.0, YAW = 0.0;
    private static final double WRONG_FWD = 4.0, WRONG_LEFT = 0.0;

    /** Residual the code would show at {@code headingRad}, with the mount set wrong, for one tag. */
    private static double[] residual(double headingRad, Vec3 tagField, Pose truth) {
        // What the camera really sees: the tag relative to the lens, in robot axes, then back into camera axes.
        double c = Math.cos(headingRad), s = Math.sin(headingRad);
        double relX = tagField.x() - truth.x(), relY = tagField.y() - truth.y();
        double robotX = c * relX + s * relY, robotY = -s * relX + c * relY; // tag in true robot axes
        // Invert the mount transform with the TRUE mount to get the camera-space vector...
        Vec3 inRobot = new Vec3(robotX, robotY, tagField.z());
        Vec3 cameraSpace = invertMount(inRobot, TRUE_FWD, TRUE_LEFT);
        // ...then run it through the code's WRONG mount, as the robot would.
        Vec3 seen = CameraMount.toRobotFrame(cameraSpace, PITCH, YAW, WRONG_FWD, WRONG_LEFT, UP);
        Pose est = CellFix.position(tagField, seen, headingRad);
        return new double[] {est.x() - truth.x(), est.y() - truth.y()};
    }

    /** Inverse of {@code toRobotFrame} for the pitch/yaw used here, so the test does not trust its own algebra twice. */
    private static Vec3 invertMount(Vec3 robot, double fwd, double left) {
        double x = robot.x() - fwd, y = robot.y() - left, z = robot.z() - UP;
        double w = Math.toRadians(YAW), cw = Math.cos(w), sw = Math.sin(w);
        double px = x * cw + y * sw, py = -x * sw + y * cw, pz = z;
        double p = Math.toRadians(PITCH), cp = Math.cos(p), sp = Math.sin(p);
        double ax = px * cp + pz * sp, az = -px * sp + pz * cp, ay = py;
        // aligned (fwd, left, up) -> camera (right, down, forward)
        return new Vec3(-ay, -az, ax);
    }

    @Test
    public void suggestsTheTrueMountFromAnyHeading() {
        Vec3 tag = new Vec3(60.0, 70.0, 49.8);
        for (double deg : new double[] {0, 45, 90, 137, 270}) {
            double h = Math.toRadians(deg);
            Pose truth = new Pose(55.6, 7.7 + (deg == 270 ? 100 : 0), h);
            CameraCalibration cal = new CameraCalibration();
            for (int i = 0; i < CameraCalibration.FRAMES_WANTED; i++) {
                double[] r = residual(h, tag, truth);
                cal.add(r[0], r[1], h, Double.NaN);
            }
            assertTrue(cal.ready());
            assertEquals("fwd at " + deg, TRUE_FWD, cal.suggestedForwardIn(WRONG_FWD), 1e-6);
            assertEquals("left at " + deg, TRUE_LEFT, cal.suggestedLeftIn(WRONG_LEFT), 1e-6);
            assertFalse(cal.tooFar());
        }
    }

    @Test
    public void matchesTheNumbersSeenOn20245() {
        // 31 Oct 2026, red start facing 90 deg: camera minus odometry (-1.05, -0.6) in field axes.
        CameraCalibration cal = new CameraCalibration();
        cal.add(-1.05, -0.6, Math.toRadians(90), 1.9);
        assertEquals(-0.6, cal.forwardIn(), 1e-9);  // behind
        assertEquals(1.05, cal.leftIn(), 1e-9);     // to the left
        assertEquals(3.4, cal.suggestedForwardIn(4.0), 1e-9);
        assertEquals(1.05, cal.suggestedLeftIn(0.0), 1e-9);
        assertEquals(12.1, cal.heightZeroingUpIn(14.0), 1e-9);
    }

    @Test
    public void twoSpotsAgreeOnlyWhenTheyGiveTheSameMount() {
        assertTrue(CameraCalibration.agree(3.40, 1.05, 3.55, 1.20));
        assertFalse(CameraCalibration.agree(3.40, 1.05, 4.60, 1.05));
    }

    @Test
    public void aBigMeanIsNotAMountError() {
        CameraCalibration cal = new CameraCalibration();
        cal.add(4.0, 0.0, 0.0, Double.NaN);
        assertTrue(cal.tooFar());
        assertTrue(Double.isNaN(cal.spreadIn()));
        assertEquals("no still frames yet", new CameraCalibration().describe(4, 0, 14));
    }
}
