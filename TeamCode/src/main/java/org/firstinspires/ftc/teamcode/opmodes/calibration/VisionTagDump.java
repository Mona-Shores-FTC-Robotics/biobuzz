package org.firstinspires.ftc.teamcode.opmodes.calibration;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.teamcode.opmodes.RobotOpMode;
import org.firstinspires.ftc.teamcode.vision.BiobuzzTags;
import org.firstinspires.ftc.teamcode.vision.CameraMount;
import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.Vec3;

import java.util.List;

/**
 * Raw per-fiducial dump straight from the Limelight, before any of our maths.
 *
 * <p><b>Run this first.</b> Its job is to settle the one thing the unit tests
 * cannot: which axis of the camera's reported pose is which. Everything
 * downstream — range, bearing, elevation — is built on that assumption.
 *
 * <h2>What the three numbers mean</h2>
 *
 * The camera reports the tag in <em>its own</em> frame: {@code +X} to the lens's
 * right, {@code +Y} down the image, {@code +Z} straight out of the lens. So
 * {@code Z} is the range <b>along the lens axis</b> — not "forward" in robot terms
 * and not height. Our Limelight is pitched steeply upward at the HIVE, so a tag
 * held level with the robot a metre ahead is nowhere near the lens axis; to check
 * {@code Z} against a tape measure, hold the tag square to the lens, directly
 * along the direction it points.
 *
 * <h2>Procedure</h2>
 * <ol>
 *   <li>Put a single HIVE CELL tag square to the lens, along where it points, at a
 *       tape-measured 24 in (lens to tag centre). Confirm <b>Z</b> and
 *       <b>straight-line</b> both read about 24 and X and Y are near zero. Repeat
 *       at 48 in. A constant ratio at both distances means the tag size is wrong
 *       (a printout that is not 3.25 in across the black square, or the pipeline's
 *       marker size) or a bad custom calibration in the Limelight web UI, not a
 *       code bug.</li>
 *   <li>Move the tag to the camera's <b>right</b>. Confirm <b>X</b> goes positive.</li>
 *   <li>Move the tag <b>above</b> centre. Confirm <b>Y</b> goes negative.</li>
 * </ol>
 *
 * <p>If any of those is different, fix it in {@code CameraMount.cameraAxesToRobotAxes}
 * — one method, and {@code CameraMountTest} covers the rest of the chain.
 *
 * <p>The <b>robot fwd/left/up</b> line is the same tag after {@link CameraMount}
 * — the vector every sighting is built from. Once the mount is measured and
 * entered in Panels, a tag at a tape-measured position relative to the robot's
 * origin should read there. While the mount is still all zeros the line is just
 * the axis swap, and the header says so.
 *
 * <p>"3D poses missing" counting up while tags are visible means the Limelight
 * pipeline is not emitting full 3D pose, and no sighting can be built at all.
 */
@TeleOp(name = "Vision: Raw Tag Dump", group = "Vision")
public class VisionTagDump extends RobotOpMode {

    @Override
    protected void onInit() {
        VisionTelemetry.addBanner(telemetry, robot.vision, "Raw Tag Dump",
                "Hold a HIVE CELL tag square to the lens, along where it points.",
                "Expect: Z = range along the lens axis, +X = right, -Y = up.");
    }

    @Override
    protected void onLoop() {
        VisionTelemetry.addStatusHeader(telemetry, robot.vision);

        telemetry.addLine("--- CAMERA MOUNT (CameraMount, tune in Panels) ---");
        telemetry.addData("pitch / yaw", "%.1f° / %.1f°", CameraMount.pitchDeg, CameraMount.yawDeg);
        telemetry.addData("fwd / left / up", "%.2f / %.2f / %.2f in",
                CameraMount.mountForwardIn, CameraMount.mountLeftIn, CameraMount.mountUpIn);
        if (mountIsUnset()) {
            telemetry.addLine("  NOT MEASURED - robot-frame lines below are just the axis swap.");
        }
        telemetry.addLine();

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
        telemetry.addLine("--- FIDUCIALS (raw camera space) ---");
        for (LLResultTypes.FiducialResult fiducial : fiducials) {
            int tagId = fiducial.getFiducialId();
            HiveCell cell = BiobuzzTags.cellForTag(tagId);

            telemetry.addLine(String.format("id %d  %s",
                    tagId, cell == null ? "(not a BIOBUZZ tag)" : cell.toString()));
            telemetry.addData("  tx / ty", "%.2f° / %.2f°",
                    fiducial.getTargetXDegrees(), fiducial.getTargetYDegrees());
            telemetry.addData("  area", "%.3f%%", fiducial.getTargetArea());

            Position raw = rawPosition(fiducial.getTargetPoseCameraSpace());
            if (raw == null) {
                telemetry.addLine("  XYZ: none (pipeline not emitting 3D pose)");
                continue;
            }

            // What came off the wire, before Position.toUnit. If this is not in
            // METER the inch conversion below is scaling the wrong thing.
            telemetry.addData("  raw", "%.3f, %.3f, %.3f %s", raw.x, raw.y, raw.z, raw.unit);

            Position inches = raw.toUnit(DistanceUnit.INCH);
            Vec3 camera = new Vec3(inches.x, inches.y, inches.z);
            if (camera.norm() < 1e-6) {
                // The camera's "no 3D solution" sentinel; the subsystem drops it too.
                telemetry.addLine("  XYZ: all zero (no 3D solution for this tag)");
                continue;
            }

            telemetry.addData("  XYZ", "%.2f, %.2f, %.2f in", camera.x(), camera.y(), camera.z());
            // Lens to tag centre regardless of which axis it lands in: this is the
            // number to hold a tape measure against.
            telemetry.addData("  straight-line", "%.2f in", camera.norm());

            Vec3 robotFrame = CameraMount.toRobotFrame(camera);
            telemetry.addData("  robot fwd/left/up", "%.2f / %.2f / %.2f in",
                    robotFrame.x(), robotFrame.y(), robotFrame.z());
        }

        telemetry.update();
    }

    private static boolean mountIsUnset() {
        return CameraMount.pitchDeg == 0.0
                && CameraMount.yawDeg == 0.0
                && CameraMount.mountForwardIn == 0.0
                && CameraMount.mountLeftIn == 0.0
                && CameraMount.mountUpIn == 0.0;
    }

    private static Position rawPosition(Pose3D pose) {
        if (pose == null) return null;
        Position position = pose.getPosition();
        if (position == null || position.unit == null) return null;
        return position;
    }
}
