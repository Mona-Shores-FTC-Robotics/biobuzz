package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link RobotComponents} says what {@code cad/advantagescope/Robot_BIOBUZZ/components.json} says
 * (the model's moving parts, written by {@code from_onshape.py} from the CAD), and turns them the
 * way {@code components.json}'s "about" spells out. A rebuild that moves a component's axis fails
 * here until the Java is updated, so the log never poses a part about an old axis.
 *
 * <p>A textual scan, like {@code TeamSpotsTest}: Android unit tests have no working JSON parser.
 */
public class RobotComponentsTest {

    private static final String NUM = "\\s*(-?[0-9.]+(?:[eE]-?[0-9]+)?)\\s*";
    private static final Pattern NAME = Pattern.compile("\"name\":\\s*\"([^\"]+)\"");
    private static final Pattern POINT = Pattern.compile("\"axis_point_m\":\\s*\\[" + NUM + "," + NUM + "," + NUM + "]");
    private static final Pattern DIRECTION = Pattern.compile("\"axis_direction\":\\s*\\[" + NUM + "," + NUM + "," + NUM + "]");
    private static final Pattern DRAWN = Pattern.compile("\"drawn_at_deg\":" + NUM);

    @Test
    public void theJavaSaysWhatComponentsJsonSays() {
        String json = read("components.json");
        List<String> names = all(NAME, json, 1);
        String hint = ": rebuilt the model? Copy components.json's numbers into RobotComponents";
        assertEquals("component count" + hint, names.size(), RobotComponents.COUNT);
        List<double[]> points = triples(POINT, json);
        List<double[]> directions = triples(DIRECTION, json);
        List<String> drawn = all(DRAWN, json, 1);
        for (int c = 0; c < RobotComponents.COUNT; c++) {
            assertEquals("component " + c + "'s name" + hint, names.get(c), RobotComponents.NAMES[c]);
            assertArrayEquals(names.get(c) + "'s axis point" + hint, points.get(c), RobotComponents.AXIS_POINT_M[c], 1e-6);
            assertArrayEquals(names.get(c) + "'s axis direction" + hint, directions.get(c), RobotComponents.AXIS_DIRECTION[c], 1e-6);
            assertEquals(names.get(c) + "'s drawn angle" + hint, Double.parseDouble(drawn.get(c)), RobotComponents.DRAWN_AT_DEG[c], 1e-9);
        }
        assertEquals("TURRET is the turret", "turret", RobotComponents.NAMES[RobotComponents.TURRET]);
    }

    @Test
    public void configJsonHasOneComponentEach() {
        Matcher m = Pattern.compile("\"zeroedRotations\"").matcher(read("config.json"));
        int n = 0;
        while (m.find()) n++;
        assertEquals("config.json's components: AdvantageScope draws model_0.glb .. as these", RobotComponents.COUNT, n);
    }

    @Test
    public void atItsDrawnAngleAComponentIsWhereTheCadDrewIt() {
        double[] out = new double[7 * RobotComponents.COUNT];
        for (int c = 0; c < RobotComponents.COUNT; c++) {
            RobotComponents.pose(c, RobotComponents.DRAWN_AT_DEG[c], out);
            assertArrayEquals(new double[] {0, 0, 0, 1, 0, 0, 0}, slice(out, c), 1e-12);
        }
    }

    @Test
    public void turningLeavesTheAxisWhereItIsAndTurnsCounterClockwise() {
        double[] out = new double[7];
        int c = RobotComponents.TURRET;
        double[] p = RobotComponents.AXIS_POINT_M[c];
        for (double deg : new double[] {0, 45, 90, -120, 270}) {
            RobotComponents.pose(c, deg, out);
            assertArrayEquals("a point on the axis stays put at " + deg, p, apply(out, p), 1e-9);
        }
        // From drawn to drawn + 90: a point 0.1 m ahead of the axis ends up 0.1 m to its left (+y).
        RobotComponents.pose(c, RobotComponents.DRAWN_AT_DEG[c] + 90, out);
        double[] ahead = {p[0] + 0.1, p[1], p[2]};
        double[] moved = apply(out, ahead);
        assertEquals(p[0], moved[0], 1e-3);
        assertEquals(p[1] + 0.1, moved[1], 1e-3);
    }

    @Test
    public void theFlowerStickSwungOutPutsItsTopShaftOnTheTile() {
        // The 312 mm shaft across the arms' tops, as the CAD draws it stowed (31 Oct 2026): X 1.87 in, Z 15.77 in.
        double in = 0.0254;
        double[] shaft = {1.87 * in, 0, 15.77 * in};
        double[] out = new double[7 * RobotComponents.COUNT];
        RobotComponents.pose(RobotComponents.FLOWER_STICK, 162, out);
        double[] moved = apply(slice(out, RobotComponents.FLOWER_STICK), shaft);
        assertEquals("out, it lies on the tile (the stick's wheels)", 0.5, moved[2] / in, 0.5);
        assertEquals("ahead of the robot's front", 12.7, moved[0] / in, 0.5);
    }

    /** The pose (translation t, quaternion q) applied to v: t + q v q*. */
    private static double[] apply(double[] pose, double[] v) {
        double w = pose[3], x = pose[4], y = pose[5], z = pose[6];
        double[][] r = {
                {1 - 2 * (y * y + z * z), 2 * (x * y - w * z), 2 * (x * z + w * y)},
                {2 * (x * y + w * z), 1 - 2 * (x * x + z * z), 2 * (y * z - w * x)},
                {2 * (x * z - w * y), 2 * (y * z + w * x), 1 - 2 * (x * x + y * y)}};
        double[] out = new double[3];
        for (int i = 0; i < 3; i++) out[i] = pose[i] + r[i][0] * v[0] + r[i][1] * v[1] + r[i][2] * v[2];
        return out;
    }

    private static double[] slice(double[] a, int c) {
        double[] out = new double[7];
        System.arraycopy(a, 7 * c, out, 0, 7);
        return out;
    }

    private static List<String> all(Pattern p, String text, int group) {
        List<String> out = new ArrayList<>();
        Matcher m = p.matcher(text);
        while (m.find()) out.add(m.group(group));
        return out;
    }

    private static List<double[]> triples(Pattern p, String text) {
        List<double[]> out = new ArrayList<>();
        Matcher m = p.matcher(text);
        while (m.find()) {
            out.add(new double[] {Double.parseDouble(m.group(1)), Double.parseDouble(m.group(2)), Double.parseDouble(m.group(3))});
        }
        return out;
    }

    /** A file in cad/advantagescope/Robot_BIOBUZZ, from the module dir under Gradle or the repo root in an IDE. */
    private static String read(String name) {
        File candidate = new File(System.getProperty("user.dir"));
        while (candidate != null) {
            File file = new File(candidate, "cad/advantagescope/Robot_BIOBUZZ/" + name);
            if (file.isFile()) {
                try {
                    return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new AssertionError(e);
                }
            }
            candidate = candidate.getParentFile();
        }
        throw new AssertionError("cad/advantagescope/Robot_BIOBUZZ/" + name + " not found from " + System.getProperty("user.dir"));
    }
}
