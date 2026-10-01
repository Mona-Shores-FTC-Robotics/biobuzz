package org.firstinspires.ftc.teamcode.logging;

import com.pedropathing.math.Pose;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Writes Autonomouses drawn in the Pedro Visualizer as a {@code .wpilog}: each robot drives its
 * file's Pedro 3 paths ({@link VisualizerPath}) in order, inside a match timeline, so it opens in
 * AdvantageScope like a real Auto would.
 *
 * <p>One file is one robot. Several files are several robots on the field at once, all starting
 * when AUTO starts: a duo Auto and its partner's, or both alliances. The first file is our robot,
 * then our partner, then the other alliance's two ({@link FieldRobot}). Each file is drawn where the
 * Visualizer draws it, so draw each robot's Auto for the alliance it plays.
 *
 * <p>Logged, per robot under its {@link FieldRobot} keys ({@code Robot} shown):
 * <ul>
 *   <li>{@code /Odometry/Robot} and {@code /Odometry/Robot3d}: the robot following the paths;</li>
 *   <li>{@code /Path/Full}: every path of the Auto, written once;</li>
 *   <li>{@code /Path/Active}: the path being driven now; {@code /Path/Target}: where it ends;</li>
 *   <li>{@code /Odometry/PedroInches/*}: the same pose in Visualizer numbers, to read off directly;</li>
 *   <li>an event per path and per pause, match state, and metadata naming the source file.</li>
 * </ul>
 * With more than one robot, {@link FieldRobot#ALL_3D} holds them all, and events say which robot.
 *
 * <p>Open it next to the same {@code .pp} in the Visualizer: same field, same shapes, same places.
 */
public final class VisualizerPathLog {

    static final double LOOP_S = 0.020;
    static final double PRE_S = 2.0;
    static final double POST_S = 2.0;
    private static final int SAMPLES = 30;

    private final List<VisualizerPath> autos;

    VisualizerPathLog(VisualizerPath auto) {
        this(Collections.singletonList(auto));
    }

    /** One robot per file, in {@link FieldRobot} slot order: ours, our partner, then the opponents. */
    VisualizerPathLog(List<VisualizerPath> autos) {
        if (autos.isEmpty()) throw new IllegalArgumentException("no Visualizer files to log");
        FieldRobot.slot(autos.size() - 1); // a field holds four robots
        this.autos = new ArrayList<>(autos);
    }

    /**
     * The robots a "together" file names: one {@code .pp} path per line, relative to
     * {@code resources}, in {@link FieldRobot} slot order. Blank lines and lines starting with
     * {@code #} are skipped.
     */
    static List<VisualizerPath> readTogether(File together, File resources) throws IOException {
        List<VisualizerPath> autos = new ArrayList<>();
        for (String line : Files.readAllLines(together.toPath(), StandardCharsets.UTF_8)) {
            String l = line.trim();
            if (l.isEmpty() || l.startsWith("#")) continue;
            File pp = new File(resources, l);
            if (!pp.isFile()) throw new IOException(together.getName() + " names " + l + ", which is not there");
            autos.add(VisualizerPath.read(pp.toPath()));
        }
        return autos;
    }

    public void write(File file) throws IOException {
        file.getParentFile().mkdirs();
        StringBuilder title = new StringBuilder();
        for (VisualizerPath a : autos) title.append(title.length() == 0 ? "" : " + ").append(a.sourceName);
        try (WpiLog log = new WpiLog(new WpiLogWriter(
                new BufferedOutputStream(new FileOutputStream(file), 1 << 16),
                "Pedro Visualizer Auto: " + title))) {
            write(log);
        }
    }

    void write(WpiLog log) throws IOException {
        VisualizerPath first = autos.get(0);
        int count = autos.size();
        log.putMetadata("Generator", "VisualizerPathLog (TeamCode test sources)");
        log.putMetadata("Source", first.sourceName);
        log.putMetadata("VisualizerFormat", first.version);
        log.putMetadata("VisualizerField", first.fieldMap);
        log.putMetadata("OpenWithField", fieldHint(first.fieldMap));
        log.putMetadata("Paths", "Built and sampled with Pedro Pathing core 3.0.1");
        log.putMetadata("PoseFrame", AdvantageScopeFrame.DESCRIPTION);
        String[] what = new String[count];
        for (int i = 0; i < count; i++) {
            VisualizerPath auto = autos.get(i);
            FieldRobot robot = FieldRobot.slot(i);
            String key = i == 0 ? "" : robot.label;
            if (i > 0) log.putMetadata(key + "Source", auto.sourceName);
            log.putMetadata(key + "Timing", String.format(Locale.US,
                    "trapezoid per path, rest to rest: %.0f in/s, %.0f in/s^2 accel, %.0f in/s^2 decel",
                    auto.maxVelocity, auto.maxAcceleration, auto.maxDeceleration));
            what[i] = auto.sourceName;

            int drivingSteps = 0;
            for (VisualizerPath.Step s : auto.steps) if (s.path != null) drivingSteps++;
            double[] full = new double[3 * (SAMPLES + 1) * drivingSteps];
            int at = 0;
            for (VisualizerPath.Step s : auto.steps) {
                if (s.path == null) continue;
                for (Pose p : VisualizerPath.sample(s.path, SAMPLES)) at = putAs(full, at, p);
            }
            log.putPose2dArray(robot.fullPath, full, 0);
            log.putEvent(String.format(Locale.US, "%sVisualizer file %s: %d paths, %.1f s",
                    tag(i), auto.sourceName, drivingSteps, auto.durationS), 0);
        }
        FieldRobot.putViewingHint(log, count, what);
        log.put(AdvantageScopeKeys.ALLIANCE_STATION, 0L, 0);

        double longest = 0;
        for (VisualizerPath a : autos) longest = Math.max(longest, a.durationS);
        double autoStart = PRE_S;
        double end = autoStart + longest + POST_S;
        String lastMode = "";
        VisualizerPath.Step[] lastStep = new VisualizerPath.Step[count];
        double[] active = new double[3 * (SAMPLES + 1)];
        double[] all = new double[3 * count];

        for (long k = 0; k * LOOP_S <= end; k++) {
            double t = k * LOOP_S;
            long us = Math.round(t * 1e6);
            boolean anyRunning = t >= autoStart && t < autoStart + longest;
            String mode = anyRunning ? "autonomous" : "disabled";
            if (!mode.equals(lastMode)) {
                log.put(AdvantageScopeKeys.ENABLED, anyRunning, us);
                log.put(AdvantageScopeKeys.AUTONOMOUS, anyRunning, us);
                log.put(AdvantageScopeKeys.ROBOT_MODE, mode, us);
                log.putEvent("Match: " + mode, us);
                lastMode = mode;
            }

            for (int i = 0; i < count; i++) {
                VisualizerPath auto = autos.get(i);
                FieldRobot robot = FieldRobot.slot(i);
                boolean running = t >= autoStart && t < autoStart + auto.durationS;
                double autoT = Math.max(0, Math.min(auto.durationS, t - autoStart));
                VisualizerPath.Step step = running ? auto.stepAt(autoT) : null;
                if (step != lastStep[i]) {
                    if (step == null || step.path == null) {
                        if (step != null) {
                            log.putEvent(String.format(Locale.US, "%spause %.0f ms (%s)",
                                    tag(i), (step.endS - step.startS) * 1000, step.name), us);
                        }
                        log.putPose2dArray(robot.activePath, new double[0], us);
                    } else {
                        Pose e = step.path.endPose();
                        log.putEvent(String.format(Locale.US, "%spath %s: to (%.1f, %.1f) at %.0f deg, %.1f in",
                                tag(i), step.name, e.x(), e.y(), Math.toDegrees(e.heading()),
                                step.path.curve.length()), us);
                        int a = 0;
                        for (Pose p : VisualizerPath.sample(step.path, SAMPLES)) a = putAs(active, a, p);
                        log.putPose2dArray(robot.activePath, active, us);
                        putPose(log, robot.target, e, us);
                    }
                    lastStep[i] = step;
                }

                Pose pose = auto.poseAt(autoT);
                robot.putPose(log, pose.x(), pose.y(), pose.heading(), us);
                all[3 * i] = pose.x();
                all[3 * i + 1] = pose.y();
                all[3 * i + 2] = pose.heading();
            }
            if (count > 1) FieldRobot.putAll(log, all, us);
        }
        log.putEvent("OpMode stopped", Math.round(end * 1e6));
    }

    /** Which robot an event is about, when there is more than one. */
    private String tag(int index) {
        return autos.size() == 1 ? "" : FieldRobot.slot(index).label + ": ";
    }

    private static void putPose(WpiLog log, String key, Pose p, long us) throws IOException {
        SimulatedMatch.putPedroPose(log, key, new double[] {p.x(), p.y(), p.heading()}, us);
    }

    private static int putAs(double[] dst, int at, Pose p) {
        dst[at] = AdvantageScopeFrame.xMeters(p.x(), p.y());
        dst[at + 1] = AdvantageScopeFrame.yMeters(p.x(), p.y());
        dst[at + 2] = AdvantageScopeFrame.headingRad(p.heading());
        return at + 3;
    }

    /** Which AdvantageScope field matches the Visualizer's. */
    static String fieldHint(String fieldMap) {
        String f = fieldMap == null ? "" : fieldMap.toLowerCase(Locale.US);
        if (f.contains("biobuzz")) return "2026-2027 Field (BIOBUZZ)";
        return "the AdvantageScope field matching " + fieldMap;
    }
}
