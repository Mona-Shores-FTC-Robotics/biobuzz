package org.firstinspires.ftc.teamcode.vision;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Pins down the camera-to-robot transform.
 *
 * <p>These assert our documented convention — camera {@code +X} right, {@code +Y}
 * down, {@code +Z} forward, mapping to robot {@code +X} forward, {@code +Y} left,
 * {@code +Z} up. They do not prove the camera actually uses that convention; only
 * putting a tag in front of the robot does that. What they do prove is that the
 * rotation and translation chain is self-consistent, so once the axis mapping is
 * confirmed the rest is known good.
 */
public class CameraMountTest {

    private static final double EPS = 1e-9;
    private static final double LOOSE = 1e-9;

    private static void assertVec(String what, Vec3 actual, double x, double y, double z) {
        assertEquals(what + " x", x, actual.x(), LOOSE);
        assertEquals(what + " y", y, actual.y(), LOOSE);
        assertEquals(what + " z", z, actual.z(), LOOSE);
    }

    /** Camera axes with no mounting offset: forward stays forward, right becomes -left. */
    @Test
    public void axisReinterpretation() {
        assertVec("straight ahead",
                CameraMount.cameraAxesToRobotAxes(new Vec3(0, 0, 100)), 100, 0, 0);
        assertVec("10in to the camera's right",
                CameraMount.cameraAxesToRobotAxes(new Vec3(10, 0, 100)), 100, -10, 0);
        assertVec("10in above centre (camera +Y is down)",
                CameraMount.cameraAxesToRobotAxes(new Vec3(0, -10, 100)), 100, 0, 10);
    }

    @Test
    public void identityMountIsJustTheAxisSwap() {
        assertVec("identity mount",
                CameraMount.toRobotFrame(new Vec3(10, -20, 100), 0, 0, 0, 0, 0),
                100, -10, 20);
    }

    /** Pitched straight up, the camera's forward axis points at the robot's ceiling. */
    @Test
    public void pitchNinetyTurnsForwardIntoUp() {
        assertVec("pitched 90 up",
                CameraMount.toRobotFrame(new Vec3(0, 0, 100), 90, 0, 0, 0, 0),
                0, 0, 100);
    }

    /** A realistic upward tilt: 30 degrees splits forward between X and Z. */
    @Test
    public void pitchThirtySplitsForwardAndUp() {
        Vec3 robot = CameraMount.toRobotFrame(new Vec3(0, 0, 100), 30, 0, 0, 0, 0);
        assertEquals(100 * Math.cos(Math.toRadians(30)), robot.x(), 1e-9);
        assertEquals(0.0, robot.y(), EPS);
        assertEquals(100 * Math.sin(Math.toRadians(30)), robot.z(), 1e-9);
    }

    /** Positive yaw is counter-clockwise, so a camera yawed left sees targets to the left. */
    @Test
    public void yawNinetyTurnsForwardIntoLeft() {
        assertVec("yawed 90 left",
                CameraMount.toRobotFrame(new Vec3(0, 0, 100), 0, 90, 0, 0, 0),
                0, 100, 0);
    }

    @Test
    public void yawIsAppliedAfterPitch() {
        // Pitched 90 up then yawed: the point is on the robot's Z axis, which yaw
        // rotates about, so yaw must leave it alone. If the order were reversed
        // this would come out somewhere else entirely.
        assertVec("pitch then yaw",
                CameraMount.toRobotFrame(new Vec3(0, 0, 100), 90, 45, 0, 0, 0),
                0, 0, 100);
    }

    @Test
    public void mountTranslationShiftsTheResult() {
        assertVec("offset mount",
                CameraMount.toRobotFrame(new Vec3(0, 0, 100), 0, 0, 7, 3, 11),
                107, 3, 11);
    }

    /** Pitch and translation compose: a raised, tilted camera looking at a high target. */
    @Test
    public void raisedAndTiltedCamera() {
        Vec3 robot = CameraMount.toRobotFrame(new Vec3(0, 0, 60), 45, 0, 5, 0, 12);
        double leg = 60 * Math.sqrt(0.5);
        assertEquals(leg + 5, robot.x(), 1e-9);
        assertEquals(0.0, robot.y(), EPS);
        assertEquals(leg + 12, robot.z(), 1e-9);
    }

    /** Rotation preserves length; only the translation moves the origin. */
    @Test
    public void rotationIsLengthPreserving() {
        Vec3 camera = new Vec3(13, -7, 91);
        double before = camera.norm();
        for (double pitch = -60; pitch <= 60; pitch += 15) {
            for (double yaw = -180; yaw <= 180; yaw += 45) {
                Vec3 robot = CameraMount.toRobotFrame(camera, pitch, yaw, 0, 0, 0);
                assertEquals("pitch=" + pitch + " yaw=" + yaw, before, robot.norm(), 1e-9);
            }
        }
    }
}
