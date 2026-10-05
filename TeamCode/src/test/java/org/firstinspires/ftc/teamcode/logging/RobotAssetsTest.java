package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.vision.CameraMount;
import org.firstinspires.ftc.teamcode.vision.Vec3;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

/**
 * The AdvantageScope robot model and its Limelight camera.
 *
 * <p><b>To build it</b> (once per laptop, and again whenever {@link CameraMount} changes):
 * <pre>
 * ./gradlew :TeamCode:testDebugUnitTest --tests '*RobotAssetsTest*'
 * </pre>
 * It writes {@code TeamCode/build/advantagescope/Robot_BIOBUZZ}. Copy that folder into
 * AdvantageScope's {@code userAssets} folder (Show Assets Folder, in the app menu) and restart
 * AdvantageScope.
 */
public class RobotAssetsTest {

    private static final double EPS = 1e-9;

    @Test
    public void buildsTheRobotFolder() throws IOException {
        File dir = RobotAssets.build(new File(TeamCodeDir.get(), "build/advantagescope"));
        assertTrue(new File(dir, "model.glb").isFile());
        assertTrue(new File(dir, "config.json").isFile());

        // The model reads back as glTF with every part present.
        Glb model = Glb.read(Files.readAllBytes(new File(dir, "model.glb").toPath()));
        int root = model.sceneRoots().get(0);
        for (String part : new String[] {"Chassis", "Intake", "Limelight", "View ray"}) {
            model.childNamed(root, part);
        }
        System.out.println("Wrote " + dir.getAbsolutePath() + ": copy it into AdvantageScope's userAssets folder.");
    }

    /**
     * AdvantageScope's view through the camera points where {@link CameraMount} says the lens
     * points. Replays {@code rotationSequenceToQuaternion} ({@code geometry.ts}): each rotation in
     * the list is premultiplied, so the first one listed is applied first.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void configCameraLooksWhereTheMountLooks() {
        double[][] mounts = {{45, 0}, {45, 20}, {-10, -90}, {0, 0}};
        for (double[] m : mounts) {
            Map<String, Object> config = (Map<String, Object>) MiniJson.parse(
                    RobotAssets.config(4, 1, 14, m[0], m[1]));
            Map<String, Object> camera = (Map<String, Object>) ((List<Object>) config.get("cameras")).get(0);

            double[] r = {1, 0, 0, 0, 1, 0, 0, 0, 1};
            for (Object o : (List<Object>) camera.get("rotations")) {
                Map<String, Object> rot = (Map<String, Object>) o;
                r = mul3(axisAngle((String) rot.get("axis"), (Double) rot.get("degrees")), r);
            }
            assertArrayEquals(RobotAssets.mountRotation(m[0], m[1]), r, EPS);

            // A point 10 in straight out of the lens, through CameraMount's own transform.
            Vec3 out = CameraMount.toRobotFrame(new Vec3(0, 0, 10), m[0], m[1], 4, 1, 14);
            double[] axis = RobotAssets.mul(r, new double[] {1, 0, 0});
            assertEquals(4 + 10 * axis[0], out.x(), EPS);
            assertEquals(1 + 10 * axis[1], out.y(), EPS);
            assertEquals(14 + 10 * axis[2], out.z(), EPS);

            double[] position = Glb.doubles(camera.get("position"));
            assertArrayEquals(new double[] {4 * 0.0254, 1 * 0.0254, 14 * 0.0254}, position, EPS);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void pitchedUpCameraLooksUp() {
        double[] r = RobotAssets.mountRotation(45, 0);
        double[] axis = RobotAssets.mul(r, new double[] {1, 0, 0});
        assertTrue("forward", axis[0] > 0);
        assertTrue("up", axis[2] > 0);
        Map<String, Object> config = (Map<String, Object>) MiniJson.parse(RobotAssets.config(0, 0, 0, 45, 0));
        Map<String, Object> camera = (Map<String, Object>) ((List<Object>) config.get("cameras")).get(0);
        Map<String, Object> pitch = (Map<String, Object>) ((List<Object>) camera.get("rotations")).get(0);
        assertEquals("y", pitch.get("axis"));
        assertEquals(-45.0, (Double) pitch.get("degrees"), EPS);
    }

    private static double[] axisAngle(String axis, double degrees) {
        double a = Math.toRadians(degrees), c = Math.cos(a), s = Math.sin(a);
        switch (axis) {
            case "x": return new double[] {1, 0, 0, 0, c, -s, 0, s, c};
            case "y": return new double[] {c, 0, s, 0, 1, 0, -s, 0, c};
            case "z": return new double[] {c, -s, 0, s, c, 0, 0, 0, 1};
            default: throw new IllegalArgumentException(axis);
        }
    }

    private static double[] mul3(double[] a, double[] b) {
        double[] o = new double[9];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) o[3 * i + j] += a[3 * i + k] * b[3 * k + j];
            }
        }
        return o;
    }
}
