package org.firstinspires.ftc.teamcode.logging;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Writes an Autonomous drawn in the Pedro Visualizer as a {@code .wpilog}: the robot drives the
 * {@code .pp} file's paths in order, inside a match timeline, so it opens in AdvantageScope like a
 * real Auto would.
 *
 * <p>This is also the field-frame check. A path drawn in the Visualizer lands where it was drawn
 * only if {@link AdvantageScopeFrame} is right, so open the log and the {@code .pp} side by side:
 * same field, same shape, same place, same headings. Paths the team already ran on a robot (DECODE's)
 * are the best test, because where the robot went is known.
 *
 * <p>Logged:
 * <ul>
 *   <li>{@code /Odometry/Robot} and {@code /Odometry/Robot3d}: the robot following the path;</li>
 *   <li>{@code /Path/Full}: every line of the Auto, written once;</li>
 *   <li>{@code /Path/Active}: the line being driven now; {@code /Path/Target}: its end pose;</li>
 *   <li>{@code /Odometry/PedroInches/*}: the same pose in Visualizer numbers, to read off directly;</li>
 *   <li>events for each line and wait, match state, and metadata naming the source file.</li>
 * </ul>
 */
public final class VisualizerPathLog {

    static final double LOOP_S = 0.020;
    static final double PRE_S = 2.0;
    static final double POST_S = 2.0;

    private final VisualizerPath path;

    VisualizerPathLog(VisualizerPath path) {
        this.path = path;
    }

    public void write(File file) throws IOException {
        file.getParentFile().mkdirs();
        try (WpiLog log = new WpiLog(new WpiLogWriter(
                new BufferedOutputStream(new FileOutputStream(file), 1 << 16),
                "Pedro Visualizer path: " + path.sourceName))) {
            write(log);
        }
    }

    void write(WpiLog log) throws IOException {
        log.putMetadata("Generator", "VisualizerPathLog (TeamCode test sources)");
        log.putMetadata("Source", path.sourceName);
        log.putMetadata("VisualizerVersion", path.version);
        log.putMetadata("VisualizerField", path.fieldMap);
        log.putMetadata("OpenWithField", fieldHint(path.fieldMap));
        log.putMetadata("PoseFrame", "Pedro -> AdvantageScope Center/Rotated (PsiKit mapping, traced against Visualizer and AdvantageScope source)");
        log.putMetadata("Timing", String.format(java.util.Locale.US,
                "trapezoid per line: %.0f in/s, %.0f in/s^2 accel, %.0f in/s^2 decel",
                path.maxVelocity, path.maxAcceleration, path.maxDeceleration));

        // The whole Auto, drawn once.
        int perLine = 30;
        double[] full = new double[3 * (perLine + 1) * path.lines.size()];
        int at = 0;
        for (VisualizerPath.Line l : path.lines) {
            for (double[] p : VisualizerPath.sample(l, perLine)) at = putAs(full, at, p);
        }
        log.putPose2dArray("/Path/Full", full, 0);
        log.put(AdvantageScopeKeys.ALLIANCE_STATION, 0L, 0);

        double autoStart = PRE_S;
        double autoEnd = autoStart + path.durationS;
        double end = autoEnd + POST_S;
        String lastMode = "";
        VisualizerPath.Step lastStep = null;
        log.putEvent("Path file: " + path.sourceName + " (" + path.lines.size() + " lines, "
                + String.format(java.util.Locale.US, "%.1f", path.durationS) + " s)", 0);

        for (long k = 0; k * LOOP_S <= end; k++) {
            double t = k * LOOP_S;
            long us = Math.round(t * 1e6);
            boolean auto = t >= autoStart && t < autoEnd;
            String mode = auto ? "autonomous" : "disabled";
            if (!mode.equals(lastMode)) {
                log.put(AdvantageScopeKeys.ENABLED, auto, us);
                log.put(AdvantageScopeKeys.AUTONOMOUS, auto, us);
                log.put(AdvantageScopeKeys.ROBOT_MODE, mode, us);
                log.putEvent("Match: " + mode, us);
                lastMode = mode;
            }

            double pathT = Math.max(0, Math.min(path.durationS, t - autoStart));
            VisualizerPath.Step step = auto ? path.stepAt(pathT) : null;
            if (step != lastStep) {
                if (step == null) {
                    log.putPose2dArray("/Path/Active", new double[0], us);
                } else if (step.line == null) {
                    log.putEvent(String.format(java.util.Locale.US, "wait %.0f ms (%s)",
                            (step.endS - step.startS) * 1000, step.name), us);
                    log.putPose2dArray("/Path/Active", new double[0], us);
                } else {
                    VisualizerPath.Line l = step.line;
                    double[] e = l.points[l.points.length - 1];
                    log.putEvent(String.format(java.util.Locale.US,
                            "path %s: to (%.1f, %.1f) %s heading, %.0f in",
                            l.name, e[0], e[1], l.headingMode, l.lengthIn), us);
                    double[] active = new double[3 * (perLine + 1)];
                    int a = 0;
                    for (double[] p : VisualizerPath.sample(l, perLine)) a = putAs(active, a, p);
                    log.putPose2dArray("/Path/Active", active, us);
                    SimulatedMatch.putPedroPose(log, "/Path/Target", new double[] {e[0], e[1], l.heading(1)}, us);
                }
                lastStep = step;
            }

            double[] pose = path.poseAt(pathT);
            SimulatedMatch.putPedroPose(log, "/Odometry/Robot", pose, us);
            log.putPose3dFlat("/Odometry/Robot3d", AdvantageScopeFrame.xMeters(pose[0], pose[1]),
                    AdvantageScopeFrame.yMeters(pose[0], pose[1]), 0.0,
                    AdvantageScopeFrame.headingRad(pose[2]), us);
            log.put("/Odometry/PedroInches/X", pose[0], us);
            log.put("/Odometry/PedroInches/Y", pose[1], us);
            log.put("/Odometry/PedroInches/HeadingDeg", Math.toDegrees(pose[2]), us);
        }
        log.putEvent("OpMode stopped", Math.round(end * 1e6));
    }

    private static int putAs(double[] dst, int at, double[] pedro) {
        dst[at] = AdvantageScopeFrame.xMeters(pedro[0], pedro[1]);
        dst[at + 1] = AdvantageScopeFrame.yMeters(pedro[0], pedro[1]);
        dst[at + 2] = AdvantageScopeFrame.headingRad(pedro[2]);
        return at + 3;
    }

    /** Which AdvantageScope field image matches the Visualizer's. */
    static String fieldHint(String fieldMap) {
        String f = fieldMap == null ? "" : fieldMap.toLowerCase(java.util.Locale.US);
        if (f.contains("decode")) return "2025-2026 Field (DECODE)";
        if (f.contains("biobuzz")) return "2026-2027 Field (BIOBUZZ)";
        return "the field matching " + fieldMap;
    }
}
