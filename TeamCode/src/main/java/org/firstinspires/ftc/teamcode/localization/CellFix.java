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
     * The robot's whole field pose, heading included, from one sighting with two or more tags; null
     * if the state is unknown, the field point or the row's direction on the field is unmeasured, or
     * fewer than two tags were seen.
     *
     * <p>The row runs along the HIVE's axle, and a TIP turns the CELL about that line, so the row's
     * direction on the field is fixed: comparing it with the direction the robot sees it in gives
     * the heading, with no Pinpoint and no tag orientations. Tag order says which end is which, so
     * there is no 180° ambiguity. Coarser than the Pinpoint (a 13 in row at 50-odd inches is good
     * to a degree or two), so it is for setting the pose, not tracking it: see
     * {@code DriveSubsystem}.
     *
     * @param lateralAxisRobot the row's cluster +x direction as the robot sees it
     *                         ({@code CellSighting.lateralAxisRobot()})
     */
    public static Pose pose(HiveCell cell, HiveCellState state, Vec3 rowCentreRobot,
                            Vec3 lateralAxisRobot) {
        Vec3 field = HiveFieldPoints.rowCentre(cell, state);
        double alongX = HiveFieldPoints.clusterXAlongFieldX(cell);
        if (field == null || lateralAxisRobot == null || Double.isNaN(alongX)) {
            return null;
        }
        return pose(field, alongX, rowCentreRobot, lateralAxisRobot);
    }

    static Pose pose(Vec3 field, double alongX, Vec3 seen, Vec3 lateralAxisRobot) {
        if (Math.hypot(lateralAxisRobot.x(), lateralAxisRobot.y()) < 1e-9) {
            return null;
        }
        double fieldDirection = alongX > 0 ? 0.0 : Math.PI;
        double heading = Math.IEEEremainder(
                fieldDirection - Math.atan2(lateralAxisRobot.y(), lateralAxisRobot.x()), 2 * Math.PI);
        Pose position = position(field, seen, heading);
        return new Pose(position.x(), position.y(), heading);
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
