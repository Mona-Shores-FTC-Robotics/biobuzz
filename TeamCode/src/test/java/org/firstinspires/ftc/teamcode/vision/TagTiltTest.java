package org.firstinspires.ftc.teamcode.vision;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Pins the arithmetic {@code Vision: Tag Tilt} shows. It cannot say which {@link TagTilt.Order} the
 * Limelight uses; only the robot can. It proves each candidate is computed as described.
 */
public class TagTiltTest {

    private static final double EPS = 1e-9;
    private static final Vec3 AHEAD = new Vec3(0, 0, 1);

    private double pitchDeg;
    private double yawDeg;

    @Before
    public void levelCamera() {
        pitchDeg = CameraMount.pitchDeg;
        yawDeg = CameraMount.yawDeg;
        CameraMount.pitchDeg = 0;
        CameraMount.yawDeg = 0;
    }

    @After
    public void restoreCamera() {
        CameraMount.pitchDeg = pitchDeg;
        CameraMount.yawDeg = yawDeg;
    }

    @Test
    public void anUnturnedTagFacesTheCamera() {
        for (TagTilt.Order order : TagTilt.Order.values()) {
            Vec3 face = TagTilt.faceCamera(0, 0, 0, order, AHEAD);
            assertEquals(order + " x", 0, face.x(), EPS);
            assertEquals(order + " y", 0, face.y(), EPS);
            assertEquals(order + " z", -1, face.z(), EPS);
        }
    }

    @Test
    public void oneTurnAloneMeansTheSameInEveryOrder() {
        Vec3 expected = TagTilt.faceCamera(12, 0, 0, TagTilt.Order.XYZ, AHEAD);
        for (TagTilt.Order order : TagTilt.Order.values()) {
            Vec3 face = TagTilt.faceCamera(12, 0, 0, order, AHEAD);
            assertEquals(order + " x", expected.x(), face.x(), EPS);
            assertEquals(order + " y", expected.y(), face.y(), EPS);
            assertEquals(order + " z", expected.z(), face.z(), EPS);
        }
    }

    @Test
    public void ordersDisagreeOnceTwoTurnsCombine() {
        Vec3 xyz = TagTilt.faceCamera(20, 30, 40, TagTilt.Order.XYZ, AHEAD);
        Vec3 zyx = TagTilt.faceCamera(20, 30, 40, TagTilt.Order.ZYX, AHEAD);
        assertTrue("XYZ and ZYX should differ", xyz.minus(zyx).norm() > 0.05);
    }

    @Test
    public void aTurnAboutCameraXTiltsTheFaceDownByThatMuch() {
        // Level camera: a tag facing it points straight back along the floor (0°); turning it
        // +10° about camera X (right) swings its face 10° toward the floor.
        assertEquals(0, TagTilt.elevationDeg(TagTilt.faceRobot(0, 0, 0, TagTilt.Order.XYZ, AHEAD)), EPS);
        assertEquals(-10, TagTilt.elevationDeg(TagTilt.faceRobot(10, 0, 0, TagTilt.Order.XYZ, AHEAD)), 1e-6);
    }

    @Test
    public void theCameraPitchCarriesIntoRobotAxes() {
        CameraMount.pitchDeg = 45;
        // Camera looking 45° up; a tag square to it faces 45° down, back at the robot.
        assertEquals(-45, TagTilt.elevationDeg(TagTilt.faceRobot(0, 0, 0, TagTilt.Order.XYZ, AHEAD)), 1e-6);
    }

    @Test
    public void heightGivesTheRockerAngle() {
        // 19429, 3 Oct 2026: UP 50.2 in, DOWN 35 in; mid-tip readings 45.5 and 43.0 in.
        assertEquals(30, TagTilt.rockerAngleFromHeightDeg(50.2, 50.2, 35, 30), 1e-9);
        assertEquals(-30, TagTilt.rockerAngleFromHeightDeg(35, 50.2, 35, 30), 1e-9);
        assertEquals(0, TagTilt.rockerAngleFromHeightDeg(42.6, 50.2, 35, 30), 1e-9);
        assertEquals(11.0, TagTilt.rockerAngleFromHeightDeg(45.5, 50.2, 35, 30), 0.1);
        assertEquals(1.5, TagTilt.rockerAngleFromHeightDeg(43.0, 50.2, 35, 30), 0.1);
        assertEquals(30, TagTilt.rockerAngleFromHeightDeg(60, 50.2, 35, 30), 1e-9);
    }

    @Test
    public void heightGivesNoAngleWhileTheRestingHeightsAreUnmeasured() {
        assertTrue(Double.isNaN(TagTilt.rockerAngleFromHeightDeg(45, Double.NaN, 35, 30)));
        assertTrue(Double.isNaN(TagTilt.rockerAngleFromHeightDeg(45, 50, Double.NaN, 30)));
        assertTrue(Double.isNaN(TagTilt.rockerAngleFromHeightDeg(Double.NaN, 50, 35, 30)));
    }
}
