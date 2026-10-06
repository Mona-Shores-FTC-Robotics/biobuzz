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

        // The one robot model: the Limelight in its base, each design a component (BodyShape.MATCH's order).
        Glb base = Glb.read(Files.readAllBytes(new File(dir, "model.glb").toPath()));
        base.childNamed(base.sceneRoots().get(0), "Limelight");
        base.childNamed(base.sceneRoots().get(0), "View ray");
        for (int i = 0; i < BodyShape.MATCH.length; i++) assertTrue(new File(dir, "model_" + i + ".glb").isFile());
        int flatAt = java.util.Arrays.asList(BodyShape.MATCH).indexOf(
                java.util.Arrays.stream(BodyShape.MATCH).filter(b -> b.name.equals("flat intake")).findFirst().get());
        Glb model = Glb.read(Files.readAllBytes(new File(dir, "model_" + flatAt + ".glb").toPath()));
        int root = model.sceneRoots().get(0);
        for (String part : new String[] {"Body", "Side plate left", "Chassis left", "Wheel front left", "Roller 1 (front left)", "Control Hub", "Intake roller", "Pickup volume", "Flywheel left"}) {
            model.childNamed(root, part);
        }
        Glb rigid = Glb.read(Files.readAllBytes(new File(dir, "model_" + (flatAt + 1) + ".glb").toPath()));
        rigid.childNamed(rigid.sceneRoots().get(0), "Left flap");
        File proto = new File(dir.getParentFile(), RobotAssets.PROTOTYPE_FOLDER);
        Glb prototype = Glb.read(Files.readAllBytes(new File(proto, "model.glb").toPath()));
        prototype.childNamed(prototype.sceneRoots().get(0), "Pinwheel");
        File wide = new File(dir.getParentFile(), RobotAssets.FULL_WIDTH_FOLDER);
        Glb fullWidth = Glb.read(Files.readAllBytes(new File(wide, "model.glb").toPath()));
        fullWidth.childNamed(fullWidth.sceneRoots().get(0), "Intake roller");
        model.childNamed(root, "Deflector");
        File walls = new File(dir.getParentFile(), RobotAssets.WALLS_FOLDER);
        for (String name : new String[] {"model.glb", "model_0.glb", "model_1.glb", "config.json"}) {
            assertTrue(name, new File(walls, name).isFile());
        }
        File shapes = new File(dir.getParentFile(), RobotAssets.SHAPES_FOLDER);
        for (int i = 0; i < BodyShape.SHOWN.length; i++) assertTrue(new File(shapes, "model_" + i + ".glb").isFile());
        File match = new File(dir.getParentFile(), RobotAssets.MATCH_FOLDER);
        for (int i = 0; i < BodyShape.MATCH.length; i++) assertTrue(new File(match, "model_" + i + ".glb").isFile());
        System.out.println("Wrote " + dir.getAbsolutePath() + " and the spill-study sketches: copy them into AdvantageScope's userAssets folder.");
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

    /** The side-wall sketch is the size it was asked to be, and its door sorts the pieces. */
    @Test
    public void sideWallsAre18StowedAnd24OutWithADoorOnlyPollenFits() {
        double[] stowed = size(RobotAssets.wallsRobot(0, 4, 0, 14, 45, 0));
        double[] out = size(RobotAssets.wallsRobot(RobotAssets.WALL_SLIDE_IN, 4, 0, 14, 45, 0));
        assertArrayEquals(new double[] {18, 18}, new double[] {stowed[0], stowed[1]}, 1e-4);
        assertArrayEquals(new double[] {24, 18}, new double[] {out[0], out[1]}, 1e-4);
        assertTrue("R102 height", stowed[2] <= 18);

        double pollen = 2 * FieldSim.POLLEN_RADIUS_IN, nectar = 2 * FieldSim.NECTAR_RADIUS_IN;
        assertTrue("POLLEN fits under the door", RobotAssets.DOOR_TOP_IN > pollen);
        assertTrue("NECTAR does not", RobotAssets.DOOR_TOP_IN < nectar);
    }

    /**
     * Overall size in inches: length (x), width (y), height above the floor (z). The view rod is left out: it
     * shows where the camera looks and is not part of the robot.
     */
    private static double[] size(Glb model) {
        double[] b = {Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE,
                -Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE};
        for (int part : model.children(model.sceneRoots().get(0))) {
            if (model.name(part).equals("View ray")) continue;
            double[] p = model.bounds(part, Glb.IDENTITY);
            for (int k = 0; k < 3; k++) {
                b[k] = Math.min(b[k], p[k]);
                b[k + 3] = Math.max(b[k + 3], p[k + 3]);
            }
        }
        return new double[] {(b[3] - b[0]) / 0.0254, (b[4] - b[1]) / 0.0254, b[5] / 0.0254};
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
