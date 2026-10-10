package org.firstinspires.ftc.teamcode.localization;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.HiveCellState;
import org.firstinspires.ftc.teamcode.vision.Vec3;

/**
 * Turns one CELL sighting into a robot position for Pedro's {@code FusionLocalizer} — the only
 * BIOBUZZ-specific step in localization. Pure math.
 *
 * <p>The robot is at the CELL's field point for its current state, minus the sighting rotated into
 * the field by the robot's heading. That needs the row-centre position the vision code already
 * measures and the Pinpoint's heading — no tag orientations, no Limelight field map. Heading is
 * returned as NaN so the filter leaves the Pinpoint's heading alone.
 */
public final class CellFix {

    private CellFix() {
    }

    /**
     * The robot's field position implied by seeing {@code cell} (in {@code state}) at
     * {@code seenRobotFrame}, with the robot at {@code headingRad}; or null if that CELL/state has
     * no measured field point.
     */
    public static Pose position(HiveCell cell, HiveCellState state, Vec3 seenRobotFrame, double headingRad) {
        Vec3 field = HiveFieldPoints.rowCentre(cell, state);
        return field == null ? null : position(field, seenRobotFrame, headingRad);
    }

    /**
     * The robot's whole pose from two or more tags, on one CELL or several: each tag's field position
     * ({@link HiveFieldPoints#tagPosition}) against where the robot sees it, fitted by least squares
     * (the 2D rigid fit: the turn that best lines up the two sets, then the shift). Only x and y of
     * each are used. Null with fewer than two tags or tags too close together to give a heading
     * (spread under {@code minSpreadIn}, RMS distance from their middle).
     *
     * <p>More tags and wider spread give a better answer; tags on two CELLs, even one each, are
     * better than four on one.
     *
     * @param fieldX field x of each tag, inches; {@code fieldY} likewise
     * @param robotX where the robot sees each tag, robot frame x (forward), inches; {@code robotY}
     *               likewise (left)
     * @param count  how many of the arrays' entries to use
     */
    public static Pose fit(double[] fieldX, double[] fieldY, double[] robotX, double[] robotY,
                           int count, double minSpreadIn) {
        if (count < 2) return null;
        double fx = 0, fy = 0, rx = 0, ry = 0;
        for (int i = 0; i < count; i++) {
            fx += fieldX[i];
            fy += fieldY[i];
            rx += robotX[i];
            ry += robotY[i];
        }
        fx /= count;
        fy /= count;
        rx /= count;
        ry /= count;
        double dot = 0, cross = 0, spread = 0;
        for (int i = 0; i < count; i++) {
            double ax = robotX[i] - rx, ay = robotY[i] - ry;
            double bx = fieldX[i] - fx, by = fieldY[i] - fy;
            dot += ax * bx + ay * by;
            cross += ax * by - ay * bx;
            spread += ax * ax + ay * ay;
        }
        if (Math.sqrt(spread / count) < minSpreadIn) return null;
        double heading = Math.atan2(cross, dot);
        double c = Math.cos(heading), s = Math.sin(heading);
        return new Pose(fx - (c * rx - s * ry), fy - (s * rx + c * ry), heading);
    }

    /**
     * The robot's position from one or more tags with the heading given (the Pinpoint's): each tag
     * gives a position exactly, its own offset along the row included, and they are averaged.
     * Heading is returned as NaN so a filter leaves it alone. Null with no tags.
     */
    public static Pose position(double[] fieldX, double[] fieldY, double[] robotX, double[] robotY,
                                int count, double headingRad) {
        if (count < 1) return null;
        double c = Math.cos(headingRad), s = Math.sin(headingRad);
        double x = 0, y = 0;
        for (int i = 0; i < count; i++) {
            x += fieldX[i] - (c * robotX[i] - s * robotY[i]);
            y += fieldY[i] - (s * robotX[i] + c * robotY[i]);
        }
        return new Pose(x / count, y / count, Double.NaN);
    }

    static Pose position(Vec3 field, Vec3 seen, double headingRad) {
        double c = Math.cos(headingRad);
        double s = Math.sin(headingRad);
        return new Pose(field.x() - (c * seen.x() - s * seen.y()),
                field.y() - (s * seen.x() + c * seen.y()),
                Double.NaN);
    }

    /**
     * Measurement variance for Pedro, (x, y, heading): larger far away and from a single tag.
     * Heading is not measured (the fix's heading is NaN), but its variance must still be finite:
     * Pedro inverts a matrix built from it.
     */
    public static Pose variance(double rangeIn, int tagCount) {
        double v = LocalizationTuning.fixVariance(rangeIn, tagCount);
        return new Pose(v, v, LocalizationTuning.headingMeasurementVariance);
    }
}
