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
