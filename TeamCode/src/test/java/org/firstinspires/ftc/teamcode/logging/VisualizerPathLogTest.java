package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Turns every Pedro Visualizer file under {@code src/test/resources/pedro-paths/} into a
 * {@code .wpilog} in {@code build/sim-logs/pedro-paths/}, and checks each one follows its file.
 *
 * <p>To try your own Auto: save it from the current Visualizer (format 1.5.0) into that folder and
 * run {@code ./gradlew :TeamCode:testDebugUnitTest --tests '*VisualizerPathLogTest*'}.
 *
 * <p>{@code biobuzz-sample.pp} was written and reloaded by the Visualizer's own code
 * ({@code make-sample.ts}), with the Visualizer's own curve points recorded beside it; the geometry
 * test holds Pedro 3's paths to those points.
 */
public class VisualizerPathLogTest {

    private static final File PATHS = new File(TeamCodeDir.get(), "src/test/resources/pedro-paths");
    private static final File SAMPLE = new File(PATHS, "biobuzz-sample.pp");
    private static final double METERS = AdvantageScopeFrame.METERS_PER_INCH;

    private static List<File> ppFiles() throws IOException {
        try (Stream<java.nio.file.Path> s = Files.walk(PATHS.toPath())) {
            return s.filter(p -> p.toString().endsWith(".pp")).sorted().map(java.nio.file.Path::toFile)
                    .collect(Collectors.toList());
        }
    }

    @Test
    public void readsTheCurrentVisualizerFormat() throws IOException {
        VisualizerPath p = VisualizerPath.read(SAMPLE.toPath());
        assertEquals("1.5.0", p.version);
        assertEquals("biobuzz.webp", p.fieldMap);
        assertEquals(56.0, p.start.x(), 0);
        assertEquals(8.0, p.start.y(), 0);
        assertEquals(Math.toRadians(90), p.start.heading(), 1e-12);

        // Sequence: 7 segments and a wait, with the group's two segments merged into one path.
        List<String> names = new ArrayList<>();
        for (VisualizerPath.Step s : p.steps) names.add(s.path == null ? "wait:" + s.name : s.name);
        assertEquals("[Leave start, Arc to score, wait:Score, Face the HIVE, Sweep, Through pickups, Return]",
                names.toString());
        assertEquals(7, p.segments.size());
    }

    @Test
    public void rejectsAnOlderVisualizerFormat() {
        try {
            new VisualizerPath("old.pp", "{\"version\": \"1.2.1\", \"startPoint\": {}, \"lines\": []}");
            fail("a 1.2.1 file must be refused, not guessed at");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("save it again"));
        }
    }

    /**
     * Pedro 3 draws the curves the Visualizer draws. Lines and Bezier curves use the same parameter
     * in both, so their points and lengths must agree exactly.
     *
     * <p>"Through" curves do not quite agree, and that is a finding, not slack in the test: the
     * Visualizer draws its own spline through the points ({@code curveThroughPoints}), while the robot
     * follows Pedro's {@code BezierCurve.through}. On the sample they differ by up to 1.4 in and
     * Pedro's is about 4% longer. The log follows Pedro, since that is what the robot drives. The
     * Visualizer still ships "curve through" behind {@code experimentalFeatures.curveThrough}.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void pedroCurvesMatchTheVisualizersOwnPoints() throws IOException {
        VisualizerPath p = VisualizerPath.read(SAMPLE.toPath());
        Map<String, Object> expected = (Map<String, Object>) MiniJson.parse(new String(Files.readAllBytes(
                new File(PATHS, "biobuzz-sample.visualizer-points.json").toPath()), StandardCharsets.UTF_8));
        assertEquals("1.5.0", expected.get("visualizerVersion"));
        assertEquals(141.5, (Double) expected.get("fieldSize"), 0);

        Map<String, VisualizerPath.Segment> byId = new HashMap<>();
        for (VisualizerPath.Segment s : p.segments) byId.put(s.id, s);
        for (Object o : (List<Object>) expected.get("segments")) {
            Map<String, Object> seg = (Map<String, Object>) o;
            VisualizerPath.Segment ours = byId.get((String) seg.get("id"));
            Path path = ours.path;
            double visLength = (Double) seg.get("arcLength");
            double lengthTolerance = ours.throughPoints ? 0.05 : 0.01; // arc length is sampled in the Visualizer
            assertEquals(ours.id + " length", visLength, path.curve.length(), visLength * lengthTolerance);
            for (Object so : (List<Object>) seg.get("samples")) {
                Map<String, Object> sample = (Map<String, Object>) so;
                double vx = (Double) sample.get("x");
                double vy = (Double) sample.get("y");
                if (ours.throughPoints) {
                    double closest = path.curve.closestParameter(com.pedropathing.math.Vector2D.cartesian(vx, vy));
                    com.pedropathing.math.Vector2D c = path.curve.get(closest);
                    assertTrue(ours.id + " strays from the Visualizer's curve",
                            Math.hypot(c.x() - vx, c.y() - vy) < 1.5);
                } else {
                    com.pedropathing.math.Vector2D c = path.curve.get((Double) sample.get("t"));
                    assertEquals(ours.id + " x at t=" + sample.get("t"), vx, c.x(), 1e-9);
                    assertEquals(ours.id + " y at t=" + sample.get("t"), vy, c.y(), 1e-9);
                }
            }
        }
    }

    /** Headings at the ends of each path are what the file asks for, as Pedro 3 computes them. */
    @Test
    public void headingsFollowTheFile() throws IOException {
        VisualizerPath p = VisualizerPath.read(SAMPLE.toPath());
        Map<String, VisualizerPath.Step> byName = new HashMap<>();
        for (VisualizerPath.Step s : p.steps) byName.put(s.name, s);
        assertHeading(135, byName.get("Leave start").path.endPose());
        assertHeading(180, byName.get("Sweep").path.endPose()); // the group's heading, not the children's
        assertHeading(120, byName.get("Sweep").path.get(0));
        assertHeading(90, byName.get("Through pickups").path.get(0.5));
        // Face the HIVE ends facing the field centre.
        Pose end = byName.get("Face the HIVE").path.endPose();
        assertHeading(Math.toDegrees(Math.atan2(70.75 - end.y(), 70.75 - end.x())), end);
        // Return is reverse-tangential: the robot's back leads.
        Path ret = byName.get("Return").path;
        Pose a = ret.get(0.5);
        Pose b = ret.get(0.51);
        assertHeading(Math.toDegrees(Math.atan2(b.y() - a.y(), b.x() - a.x())) + 180, a, 1.0);
    }

    @Test
    public void everyVisualizerFileBecomesALogThatFollowsIt() throws IOException {
        List<File> files = ppFiles();
        assertFalse("no .pp files under " + PATHS, files.isEmpty());
        File outDir = new File(TeamCodeDir.simLogs(), "pedro-paths");
        for (File pp : files) {
            VisualizerPath auto = VisualizerPath.read(pp.toPath());
            String name = pp.getName().replace(".pp", "");
            File out = new File(outDir, name + ".wpilog");
            new VisualizerPathLog(auto).write(out);

            WpiLogReader r = new WpiLogReader(Files.readAllBytes(out.toPath()));
            List<WpiLogReader.Record> poses = r.entry("/Odometry/Robot").records;
            double[] first = poses.get(0).asDoubles();
            assertEquals(name, AdvantageScopeFrame.xMeters(auto.start.x(), auto.start.y()), first[0], 1e-9);
            assertEquals(name, AdvantageScopeFrame.yMeters(auto.start.x(), auto.start.y()), first[1], 1e-9);

            // Passes through the end of every path, and never jumps between loops.
            for (VisualizerPath.Step s : auto.steps) {
                if (s.path == null) continue;
                Pose e = s.path.endPose();
                double best = Double.MAX_VALUE;
                for (WpiLogReader.Record rec : poses) {
                    double[] v = rec.asDoubles();
                    best = Math.min(best, Math.hypot(v[0] - AdvantageScopeFrame.xMeters(e.x(), e.y()),
                            v[1] - AdvantageScopeFrame.yMeters(e.x(), e.y())));
                }
                assertTrue(name + " never reached the end of " + s.name, best < 0.5 * METERS);
            }
            double maxStep = (auto.maxVelocity * VisualizerPathLog.LOOP_S + 0.5) * METERS;
            for (int k = 1; k < poses.size(); k++) {
                double[] a = poses.get(k - 1).asDoubles();
                double[] b = poses.get(k).asDoubles();
                assertTrue(name + " jumps at record " + k, Math.hypot(b[0] - a[0], b[1] - a[1]) < maxStep);
            }
        }
    }

    private static void assertHeading(double expectedDeg, Pose pose) {
        assertHeading(expectedDeg, pose, 1e-6);
    }

    private static void assertHeading(double expectedDeg, Pose pose, double toleranceDeg) {
        double diff = Math.toDegrees(AdvantageScopeFrame.wrap(pose.heading() - Math.toRadians(expectedDeg)));
        assertEquals("heading", 0.0, diff, toleranceDeg);
    }
}
