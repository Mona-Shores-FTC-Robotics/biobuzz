package org.firstinspires.ftc.teamcode.opmodes.calibration;

import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.teamcode.opmodes.RobotOpMode;
import org.firstinspires.ftc.teamcode.vision.BiobuzzTags;
import org.firstinspires.ftc.teamcode.vision.CellSighting;
import org.firstinspires.ftc.teamcode.vision.CellStateTracker;
import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.HiveGeometry;
import org.firstinspires.ftc.teamcode.vision.TagTilt;
import org.firstinspires.ftc.teamcode.vision.Vec3;

import java.util.List;
import java.util.Locale;

/**
 * Settles how to read a CELL's angle from its tags' orientation: shows, for every CELL in view, the
 * rocker angle its <em>height</em> implies next to the way its tags <em>face</em> under each of the
 * six possible rotation orders ({@link TagTilt.Order}). The height needs no rotation convention, so
 * it is the referee.
 *
 * <h2>Procedure</h2>
 * <ol>
 *   <li><b>Still robot, CELL UP.</b> Note each order's face elevation. The right order gives about
 *       the same number for every tag of the CELL (small "spread").</li>
 *   <li><b>Turn the robot</b> 20–30° left and right on the spot, CELL untouched. The right order's
 *       elevation stays put: turning the robot does not tilt the CELL. Wrong orders drift.</li>
 *   <li><b>Tip the CELL slowly by hand</b> and watch Panels' graph: the right order's elevation
 *       moves one-for-one with {@code tilt_<cell>_height}, offset by a constant.</li>
 * </ol>
 * Write down the order that passes all three and the offset; that is what turns tag orientation
 * into a CELL angle.
 *
 * <p>The SDK's names are not Limelight's: {@code getRoll()} is the turn about camera X (Limelight's
 * "pitch"), {@code getPitch()} about camera Y, {@code getYaw()} about camera Z. See {@link TagTilt}.
 */
@TeleOp(name = "Vision: Tag Tilt", group = "Vision")
public class VisionTagTilt extends RobotOpMode {

    private static final TagTilt.Order[] ORDERS = TagTilt.Order.values();
    private static final HiveCell[] CELLS = HiveCell.values();

    private final double[][] sum = new double[CELLS.length][ORDERS.length];
    private final double[][] min = new double[CELLS.length][ORDERS.length];
    private final double[][] max = new double[CELLS.length][ORDERS.length];
    private final int[] tags = new int[CELLS.length];
    private final String[][] tiltKeys = new String[CELLS.length][ORDERS.length];
    private final String[] heightKeys = new String[CELLS.length];

    private TelemetryManager panels;

    @Override
    protected void onInit() {
        panels = PanelsTelemetry.INSTANCE.getTelemetry();
        for (int c = 0; c < CELLS.length; c++) {
            String cell = CELLS[c].name().toLowerCase(Locale.US);
            heightKeys[c] = "tilt_" + cell + "_height";
            for (int o = 0; o < ORDERS.length; o++) {
                tiltKeys[c][o] = "tilt_" + cell + "_" + ORDERS[o].name().toLowerCase(Locale.US);
            }
        }
        VisionTelemetry.addBanner(telemetry, robot.vision, "Tag Tilt",
                "1. CELL UP, robot still: which order has the smallest spread?",
                "2. Turn the robot: which order's elevation stays put?",
                "3. Tip slowly: which order follows the height angle (Panels graph)?");
    }

    @Override
    protected void onLoop() {
        VisionTelemetry.addStatusHeader(telemetry, robot.vision);
        try {
            LLResult result = robot.vision.lastResult();
            List<LLResultTypes.FiducialResult> fiducials =
                    result == null || !result.isValid() ? null : result.getFiducialResults();
            accumulate(fiducials);
            publish(fiducials);
        } catch (RuntimeException e) {
            panels.debug("Tag Tilt failed this loop: " + e);
        }
        panels.update(telemetry);
    }

    private void accumulate(List<LLResultTypes.FiducialResult> fiducials) {
        for (int c = 0; c < CELLS.length; c++) {
            tags[c] = 0;
            for (int o = 0; o < ORDERS.length; o++) {
                sum[c][o] = 0;
                min[c][o] = Double.POSITIVE_INFINITY;
                max[c][o] = Double.NEGATIVE_INFINITY;
            }
        }
        if (fiducials == null) return;
        panels.debug("--- per tag, degrees: rx = getRoll, ry = getPitch, rz = getYaw ---");
        for (LLResultTypes.FiducialResult fiducial : fiducials) {
            HiveCell cell = BiobuzzTags.cellForTag(fiducial.getFiducialId());
            Pose3D pose = fiducial.getTargetPoseCameraSpace();
            if (cell == null || pose == null || pose.getPosition() == null
                    || pose.getOrientation() == null) {
                continue;
            }
            Position p = pose.getPosition();
            Vec3 position = new Vec3(p.x, p.y, p.z);
            YawPitchRollAngles a = pose.getOrientation();
            double rx = a.getRoll(AngleUnit.DEGREES);
            double ry = a.getPitch(AngleUnit.DEGREES);
            double rz = a.getYaw(AngleUnit.DEGREES);
            int c = cell.ordinal();
            tags[c]++;
            for (int o = 0; o < ORDERS.length; o++) {
                double elevation = TagTilt.elevationDeg(
                        TagTilt.faceRobot(rx, ry, rz, ORDERS[o], position));
                sum[c][o] += elevation;
                min[c][o] = Math.min(min[c][o], elevation);
                max[c][o] = Math.max(max[c][o], elevation);
            }
            panels.debug(String.format(Locale.US, "  id %d %s  rx %.1f  ry %.1f  rz %.1f",
                    fiducial.getFiducialId(), cell, rx, ry, rz));
        }
    }

    private void publish(List<LLResultTypes.FiducialResult> fiducials) {
        boolean any = false;
        for (int c = 0; c < CELLS.length; c++) {
            if (tags[c] == 0) continue;
            any = true;
            panels.debug("--- " + CELLS[c] + " (" + tags[c] + " tags) ---");

            CellSighting sighting = robot.vision.sighting(CELLS[c]);
            double height = sighting == null ? Double.NaN : sighting.rowCentreRobot().z();
            double heightAngle = TagTilt.rockerAngleFromHeightDeg(height,
                    CellStateTracker.Geometry.upRowHeightIn,
                    CellStateTracker.Geometry.downRowHeightIn,
                    HiveGeometry.CELL_TILT_DEG);
            panels.debug(String.format(Locale.US, "  height angle %+.1f°  (row %.1f in)",
                    heightAngle, height));
            if (!Double.isNaN(heightAngle)) panels.addData(heightKeys[c], heightAngle);

            for (int o = 0; o < ORDERS.length; o++) {
                double mean = sum[c][o] / tags[c];
                panels.debug(String.format(Locale.US, "  %s face %+.1f°  spread %.1f°",
                        ORDERS[o], mean, max[c][o] - min[c][o]));
                panels.addData(tiltKeys[c][o], mean);
            }
        }
        if (!any) {
            panels.debug(fiducials == null
                    ? "No valid Limelight result."
                    : "No BIOBUZZ tag with a 3D pose in view.");
        }
    }
}
