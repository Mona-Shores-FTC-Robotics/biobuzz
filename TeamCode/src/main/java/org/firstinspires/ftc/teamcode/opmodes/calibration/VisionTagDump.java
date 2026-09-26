package org.firstinspires.ftc.teamcode.opmodes.calibration;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.teamcode.opmodes.RobotOpMode;
import org.firstinspires.ftc.teamcode.vision.BiobuzzTags;
import org.firstinspires.ftc.teamcode.vision.HiveCell;

import java.util.List;

/**
 * Raw per-fiducial dump straight from the Limelight, before any of our maths.
 *
 * <p><b>Run this first.</b> Its job is to settle the one thing the unit tests
 * cannot: which axis of the camera's reported pose is which. Everything
 * downstream — range, bearing, elevation — is built on that assumption.
 *
 * <h2>Procedure</h2>
 * <ol>
 *   <li>Put a single HIVE CELL tag squarely in front of the camera, a metre or so
 *       out. Confirm <b>Z</b> holds roughly that distance in inches and X and Y
 *       are near zero.</li>
 *   <li>Move the tag to the camera's <b>right</b>. Confirm <b>X</b> goes positive.</li>
 *   <li>Move the tag <b>above</b> centre. Confirm <b>Y</b> goes negative.</li>
 * </ol>
 *
 * <p>If any of those is different, fix it in {@code CameraMount.cameraAxesToRobotAxes}
 * — one method, and {@code CameraMountTest} covers the rest of the chain.
 *
 * <p>"3D poses missing" counting up while tags are visible means the Limelight
 * pipeline is not emitting full 3D pose, and no sighting can be built at all.
 */
@TeleOp(name = "Vision: Raw Tag Dump", group = "Vision")
public class VisionTagDump extends RobotOpMode {

    @Override
    protected void onInit() {
        VisionTelemetry.addBanner(telemetry, robot.vision, "Raw Tag Dump",
                "Hold a HIVE CELL tag in front of the camera.",
                "Expect: Z = distance, +X = right, -Y = up.");
    }

    @Override
    protected void onLoop() {
        VisionTelemetry.addStatusHeader(telemetry, robot.vision);

        LLResult result = robot.vision.lastResult();
        if (result == null || !result.isValid()) {
            telemetry.addLine("No valid Limelight result.");
            telemetry.update();
            return;
        }

        telemetry.addData("Staleness", "%d ms", result.getStaleness());
        List<LLResultTypes.FiducialResult> fiducials = result.getFiducialResults();
        if (fiducials == null || fiducials.isEmpty()) {
            telemetry.addLine("No fiducials in frame.");
            telemetry.update();
            return;
        }

        telemetry.addLine();
        telemetry.addLine("--- FIDUCIALS (raw camera space, inches) ---");
        for (LLResultTypes.FiducialResult fiducial : fiducials) {
            int tagId = fiducial.getFiducialId();
            HiveCell cell = BiobuzzTags.cellForTag(tagId);

            telemetry.addLine(String.format("id %d  %s",
                    tagId, cell == null ? "(not a BIOBUZZ tag)" : cell.toString()));
            telemetry.addData("  tx / ty", "%.2f° / %.2f°",
                    fiducial.getTargetXDegrees(), fiducial.getTargetYDegrees());
            telemetry.addData("  area", "%.3f%%", fiducial.getTargetArea());

            Position position = positionInches(fiducial.getTargetPoseCameraSpace());
            if (position == null) {
                telemetry.addLine("  XYZ: none (pipeline not emitting 3D pose)");
            } else {
                telemetry.addData("  XYZ", "%.2f, %.2f, %.2f",
                        position.x, position.y, position.z);
            }
        }

        telemetry.update();
    }

    private static Position positionInches(Pose3D pose) {
        if (pose == null) return null;
        Position position = pose.getPosition();
        if (position == null || position.unit == null) return null;
        return position.toUnit(DistanceUnit.INCH);
    }
}
