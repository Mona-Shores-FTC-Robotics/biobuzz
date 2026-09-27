package org.firstinspires.ftc.teamcode.logging;

import com.pedropathing.math.Pose;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

/**
 * Writes an Autonomous drawn in the Pedro Visualizer as a {@code .wpilog}: the robot drives the
 * file's Pedro 3 paths ({@link VisualizerPath}) in order, inside a match timeline, so it opens in
 * AdvantageScope like a real Auto would.
 *
 * <p>Logged:
 * <ul>
 *   <li>{@code /Odometry/Robot} and {@code /Odometry/Robot3d}: the robot following the paths;</li>
 *   <li>{@code /Path/Full}: every path of the Auto, written once;</li>
 *   <li>{@code /Path/Active}: the path being driven now; {@code /Path/Target}: where it ends;</li>
 *   <li>{@code /Odometry/PedroInches/*}: the same pose in Visualizer numbers, to read off directly;</li>
 *   <li>an event per path and per pause, match state, and metadata naming the source file.</li>
 * </ul>
 *
 * <p>Open it next to the same {@code .pp} in the Visualizer: same field, same shapes, same places.
 */
public final class VisualizerPathLog {

    static final double LOOP_S = 0.020;
    static final double PRE_S = 2.0;
    static final double POST_S = 2.0;
    private static final int SAMPLES = 30;

    private final VisualizerPath auto;

    VisualizerPathLog(VisualizerPath auto) {
        this.auto = auto;
    }

    public void write(File file) throws IOException {
        file.getParentFile().mkdirs();
        try (WpiLog log = new WpiLog(new WpiLogWriter(
                new BufferedOutputStream(new FileOutputStream(file), 1 << 16),
                "Pedro Visualizer Auto: " + auto.sourceName))) {
            write(log);
        }
    }

    void write(WpiLog log) throws IOException {
        log.putMetadata("Generator", "VisualizerPathLog (TeamCode test sources)");
        log.putMetadata("Source", auto.sourceName);
        log.putMetadata("VisualizerFormat", auto.version);
        log.putMetadata("VisualizerField", auto.fieldMap);
        log.putMetadata("OpenWithField", fieldHint(auto.fieldMap));
        log.putMetadata("Paths", "Built and sampled with Pedro Pathing core 3.0.1");
        log.putMetadata("PoseFrame", AdvantageScopeFrame.DESCRIPTION);
        log.putMetadata("Timing", String.format(Locale.US,
                "trapezoid per path, rest to rest: %.0f in/s, %.0f in/s^2 accel, %.0f in/s^2 decel",
                auto.maxVelocity, auto.maxAcceleration, auto.maxDeceleration));

        int drivingSteps = 0;
        for (VisualizerPath.Step s : auto.steps) if (s.path != null) drivingSteps++;
        double[] full = new double[3 * (SAMPLES + 1) * drivingSteps];
        int at = 0;
        for (VisualizerPath.Step s : auto.steps) {
            if (s.path == null) continue;
            for (Pose p : VisualizerPath.sample(s.path, SAMPLES)) at = putAs(full, at, p);
        }
        log.putPose2dArray("/Path/Full", full, 0);
        log.put(AdvantageScopeKeys.ALLIANCE_STATION, 0L, 0);
        log.putEvent(String.format(Locale.US, "Visualizer file %s: %d paths, %.1f s",
                auto.sourceName, drivingSteps, auto.durationS), 0);

        double autoStart = PRE_S;
        double autoEnd = autoStart + auto.durationS;
        double end = autoEnd + POST_S;
        String lastMode = "";
        VisualizerPath.Step lastStep = null;
        double[] active = new double[3 * (SAMPLES + 1)];

        for (long k = 0; k * LOOP_S <= end; k++) {
            double t = k * LOOP_S;
            long us = Math.round(t * 1e6);
            boolean running = t >= autoStart && t < autoEnd;
            String mode = running ? "autonomous" : "disabled";
            if (!mode.equals(lastMode)) {
                log.put(AdvantageScopeKeys.ENABLED, running, us);
                log.put(AdvantageScopeKeys.AUTONOMOUS, running, us);
                log.put(AdvantageScopeKeys.ROBOT_MODE, mode, us);
                log.putEvent("Match: " + mode, us);
                lastMode = mode;
            }

            double autoT = Math.max(0, Math.min(auto.durationS, t - autoStart));
            VisualizerPath.Step step = running ? auto.stepAt(autoT) : null;
            if (step != lastStep) {
                if (step == null || step.path == null) {
                    if (step != null) {
                        log.putEvent(String.format(Locale.US, "pause %.0f ms (%s)",
                                (step.endS - step.startS) * 1000, step.name), us);
                    }
                    log.putPose2dArray("/Path/Active", new double[0], us);
                } else {
                    Pose e = step.path.endPose();
                    log.putEvent(String.format(Locale.US, "path %s: to (%.1f, %.1f) at %.0f deg, %.1f in",
                            step.name, e.x(), e.y(), Math.toDegrees(e.heading()), step.path.curve.length()), us);
                    int a = 0;
                    for (Pose p : VisualizerPath.sample(step.path, SAMPLES)) a = putAs(active, a, p);
                    log.putPose2dArray("/Path/Active", active, us);
                    putPose(log, "/Path/Target", e, us);
                }
                lastStep = step;
            }

            Pose pose = auto.poseAt(autoT);
            putPose(log, "/Odometry/Robot", pose, us);
            log.putPose3dFlat("/Odometry/Robot3d", AdvantageScopeFrame.xMeters(pose.x(), pose.y()),
                    AdvantageScopeFrame.yMeters(pose.x(), pose.y()), 0.0,
                    AdvantageScopeFrame.headingRad(pose.heading()), us);
            log.put("/Odometry/PedroInches/X", pose.x(), us);
            log.put("/Odometry/PedroInches/Y", pose.y(), us);
            log.put("/Odometry/PedroInches/HeadingDeg", Math.toDegrees(pose.heading()), us);
        }
        log.putEvent("OpMode stopped", Math.round(end * 1e6));
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
