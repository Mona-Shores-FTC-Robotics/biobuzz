package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Turns every Pedro Visualizer {@code .pp} file under {@code src/test/resources/pedro-paths/} into a
 * {@code .wpilog} in {@code build/sim-logs/pedro-paths/}, and checks each one follows its file.
 *
 * <p>To try your own Auto: save it from the Visualizer into that folder and run
 * {@code ./gradlew :TeamCode:testDebugUnitTest --tests '*VisualizerPathLogTest*'}.
 *
 * <p>The {@code decode/} files are DECODE Autos the team ran on a robot in 2025–26, kept as a known
 * reference: where that robot went on the DECODE field is not in question, so they test the field
 * mapping rather than a path.
 */
public class VisualizerPathLogTest {

    private static final Path PATHS = new File(TeamCodeDir.get(), "src/test/resources/pedro-paths").toPath();

    private static List<Path> ppFiles() throws IOException {
        try (Stream<Path> s = Files.walk(PATHS)) {
            return s.filter(p -> p.toString().endsWith(".pp")).sorted().collect(Collectors.toList());
        }
    }

    @Test
    public void everyVisualizerFileBecomesALogThatFollowsIt() throws IOException {
        List<Path> files = ppFiles();
        assertFalse("no .pp files found under " + PATHS.toAbsolutePath(), files.isEmpty());
        File outDir = new File(TeamCodeDir.simLogs(), "pedro-paths");
        List<String> written = new ArrayList<>();
        for (Path pp : files) {
            VisualizerPath path = VisualizerPath.read(pp);
            String name = PATHS.relativize(pp).toString().replace(File.separatorChar, '-').replace(".pp", "");
            File out = new File(outDir, name + ".wpilog");
            new VisualizerPathLog(path).write(out);
            written.add(out.getName());

            WpiLogReader r = new WpiLogReader(Files.readAllBytes(out.toPath()));
            List<WpiLogReader.Record> poses = r.entry("/Odometry/Robot").records;

            // Starts at the file's start point.
            double[] first = poses.get(0).asDoubles();
            assertEquals(name, AdvantageScopeFrame.xMeters(path.start[0], path.start[1]), first[0], 1e-9);
            assertEquals(name, AdvantageScopeFrame.yMeters(path.start[0], path.start[1]), first[1], 1e-9);

            // Ends at the last line's end point.
            VisualizerPath.Line last = path.lines.get(path.lines.size() - 1);
            double[] end = last.points[last.points.length - 1];
            double[] finalPose = poses.get(poses.size() - 1).asDoubles();
            assertEquals(name, AdvantageScopeFrame.xMeters(end[0], end[1]), finalPose[0], 1e-6);
            assertEquals(name, AdvantageScopeFrame.yMeters(end[0], end[1]), finalPose[1], 1e-6);

            // Passes through every line's end point, and never jumps.
            for (VisualizerPath.Line l : path.lines) {
                double[] e = l.points[l.points.length - 1];
                double best = Double.MAX_VALUE;
                for (WpiLogReader.Record p : poses) {
                    double[] v = p.asDoubles();
                    best = Math.min(best, Math.hypot(v[0] - AdvantageScopeFrame.xMeters(e[0], e[1]),
                            v[1] - AdvantageScopeFrame.yMeters(e[0], e[1])));
                }
                assertTrue(name + " never reached " + l.name, best < 0.5 * AdvantageScopeFrame.METERS_PER_INCH);
            }
            double maxStep = (path.maxVelocity * VisualizerPathLog.LOOP_S + 0.5) * AdvantageScopeFrame.METERS_PER_INCH;
            for (int k = 1; k < poses.size(); k++) {
                double[] a = poses.get(k - 1).asDoubles();
                double[] b = poses.get(k).asDoubles();
                assertTrue(name + " jumps at record " + k, Math.hypot(b[0] - a[0], b[1] - a[1]) < maxStep);
            }
        }
        System.out.println("Wrote to " + outDir.getAbsolutePath() + ": " + written);
    }

    @Test
    public void readsTheVisualizerFormat() throws IOException {
        VisualizerPath p = VisualizerPath.read(PATHS.resolve("decode/MichianaSimple.pp"));
        assertEquals("1.2.1", p.version);
        assertEquals("decode.webp", p.fieldMap);
        assertEquals(26.5, p.start[0], 0);
        assertEquals(130.0, p.start[1], 0);
        assertEquals(10, p.lines.size());
        assertEquals("PreloadLaunch", p.lines.get(0).name);
        assertEquals(Math.toRadians(134), p.lines.get(0).endRad, 1e-12);
        assertEquals("2025-2026 Field (DECODE)", VisualizerPathLog.fieldHint(p.fieldMap));
    }

    @Test
    public void miniJsonReadsWhatTheVisualizerWrites() {
        Object v = MiniJson.parse("{\"a\": [1, -2.5e1, true, null, \"x\\\"y\"], \"b\": {}}");
        assertEquals("{a=[1.0, -25.0, true, null, x\"y], b={}}", v.toString());
    }
}
