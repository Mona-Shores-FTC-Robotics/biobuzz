package org.firstinspires.ftc.teamcode.logging;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * A log that exists to check one thing: that a Pedro pose is drawn where Pedro means it on
 * AdvantageScope's BIOBUZZ field.
 *
 * <p>The robot steps through labelled Pedro poses, 3 s each, and each step writes an event saying
 * which pose it is. Two fixed arrays mark Pedro's axes from the field centre: {@code /Field/PedroXAxis}
 * is a row of poses along +X, {@code /Field/PedroYAxis} along +Y, every 12 in, each pointing the
 * way its axis runs. Open it in AdvantageScope, then open the Pedro Visualizer (format 1.5.0) on the BIOBUZZ
 * field: the corners and axes should agree. If they do not, {@link AdvantageScopeFrame} is wrong,
 * and this log shows how.
 */
public final class KnownPointsLog {

    static final double HOLD_S = 3.0;

    /** {x in, y in, heading deg} and what it is. */
    static final Object[][] POINTS = {
            {70.75, 70.75, 0.0, "field centre, facing Pedro +X"},
            {70.75, 70.75, 90.0, "field centre, facing Pedro +Y"},
            {9.0, 9.0, 0.0, "near the Pedro origin corner (0, 0), facing +X"},
            {132.5, 9.0, 0.0, "near corner (141.5, 0), facing +X"},
            {132.5, 132.5, 90.0, "near corner (141.5, 141.5), facing +Y"},
            {9.0, 132.5, 180.0, "near corner (0, 141.5), facing -X"},
            {70.75, 9.0, 90.0, "middle of the y = 0 wall, facing +Y (into the field)"},
            {132.5, 70.75, 180.0, "middle of the x = 141.5 wall, facing -X (into the field)"},
    };

    public void write(File file) throws IOException {
        file.getParentFile().mkdirs();
        try (WpiLog log = new WpiLog(new WpiLogWriter(
                new BufferedOutputStream(new FileOutputStream(file)), "BIOBUZZ known points"))) {
            write(log);
        }
    }

    void write(WpiLog log) throws IOException {
        log.putMetadata("Generator", "KnownPointsLog (TeamCode test sources)");
        log.putMetadata("PoseFrame", AdvantageScopeFrame.DESCRIPTION);
        log.putMetadata("HowToCheck",
                "Compare each labelled pose, and the PedroXAxis/PedroYAxis rows, with the Pedro Visualizer");

        log.putPose2dArray("/Field/PedroXAxis", axis(1, 0), 0);
        log.putPose2dArray("/Field/PedroYAxis", axis(0, 1), 0);
        double[] all = new double[3 * POINTS.length];
        for (int i = 0; i < POINTS.length; i++) {
            double x = (Double) POINTS[i][0];
            double y = (Double) POINTS[i][1];
            double h = Math.toRadians((Double) POINTS[i][2]);
            all[3 * i] = AdvantageScopeFrame.xMeters(x, y);
            all[3 * i + 1] = AdvantageScopeFrame.yMeters(x, y);
            all[3 * i + 2] = AdvantageScopeFrame.headingRad(h);
        }
        log.putPose2dArray("/Field/AllKnownPoints", all, 0);

        for (int i = 0; i < POINTS.length; i++) {
            long us = Math.round(i * HOLD_S * 1e6);
            double x = (Double) POINTS[i][0];
            double y = (Double) POINTS[i][1];
            double hDeg = (Double) POINTS[i][2];
            double[] pedro = {x, y, Math.toRadians(hDeg)};
            // Written every 20 ms so scrubbing anywhere in the hold shows the robot.
            for (long t = us; t < us + Math.round(HOLD_S * 1e6); t += 20_000) {
                SimulatedMatch.putPedroPose(log, "/Odometry/Robot", pedro, t);
                log.putPose3dFlat("/Odometry/Robot3d", AdvantageScopeFrame.xMeters(x, y),
                        AdvantageScopeFrame.yMeters(x, y), 0.0, AdvantageScopeFrame.headingRad(pedro[2]), t);
            }
            log.putEvent(String.format("Pedro (%.0f, %.0f) %.0f deg: %s",
                    x, y, hDeg, POINTS[i][3]), us);
            log.put("/Odometry/PedroInches/X", x, us);
            log.put("/Odometry/PedroInches/Y", y, us);
            log.put("/Odometry/PedroInches/HeadingDeg", hDeg, us);
        }
    }

    /** Poses from the centre to the wall along a Pedro axis, every 12 in, pointing along it. */
    private static double[] axis(double dx, double dy) {
        int n = 6;
        double[] out = new double[3 * n];
        double heading = Math.atan2(dy, dx);
        for (int i = 0; i < n; i++) {
            double x = AdvantageScopeFrame.PEDRO_FIELD_CENTER_IN + dx * 12 * (i + 1);
            double y = AdvantageScopeFrame.PEDRO_FIELD_CENTER_IN + dy * 12 * (i + 1);
            out[3 * i] = AdvantageScopeFrame.xMeters(x, y);
            out[3 * i + 1] = AdvantageScopeFrame.yMeters(x, y);
            out[3 * i + 2] = AdvantageScopeFrame.headingRad(heading);
        }
        return out;
    }
}
