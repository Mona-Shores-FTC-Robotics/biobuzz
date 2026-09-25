package org.firstinspires.ftc.teamcode.opmodes.calibration;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.opmodes.RobotOpMode;
import org.firstinspires.ftc.teamcode.vision.CameraMount;
import org.firstinspires.ftc.teamcode.vision.CellSighting;
import org.firstinspires.ftc.teamcode.vision.CellStateTracker;
import org.firstinspires.ftc.teamcode.vision.HiveCell;

/**
 * Live view of every HIVE CELL the camera can see, in robot-frame terms.
 *
 * <p>This is the tape-measure check. Park the robot a known distance from a cell
 * and confirm the reported range matches; swing the robot and confirm bearing
 * goes positive as the cell moves to the robot's left.
 *
 * <p>Replaces DECODE's {@code DiagnoseMegaTag2}, which compared MegaTag2's field
 * pose against odometry. That comparison cannot be made under BIOBUZZ — there is
 * no field map for MegaTag to solve against — so this checks what can actually be
 * measured instead.
 *
 * <h2>Reading it</h2>
 * <ul>
 *   <li><b>tags</b> — how many of the cell's four members are contributing. Two or
 *       more and the row centre is exact.</li>
 *   <li><b>uncorrected</b> — only one tag visible, so the aim point can sit up to
 *       6.5" off along the row. Expect this at shallow angles or long range.</li>
 *   <li><b>range</b> is horizontal; <b>slant</b> is line-of-sight. They diverge as
 *       elevation grows, and the HIVE is overhead, so they should differ.</li>
 * </ul>
 *
 * <p>Camera mounting is live-tunable in Panels through {@link CameraMount} — this
 * OpMode is where you set it. Unlike last year's diagnostic, the values really are
 * exposed: {@code CameraMount} carries {@code @Configurable}, so the dashboard
 * shows it. (DECODE's {@code DiagnoseMegaTag2} told you five times to adjust its
 * offset in the dashboard, but the class was annotated with neither
 * {@code @Config} nor {@code @Configurable}, so the field never appeared.)
 */
@TeleOp(name = "Vision: Sighting Diagnostics", group = "Vision")
@Configurable
public class VisionSightingDiagnostics extends RobotOpMode {

    /** Optional ground-truth range, inches, for a quick accuracy read. Zero disables. */
    public static double expectedRangeIn = 0.0;

    @Override
    protected void onInit() {
        VisionTelemetry.addBanner(telemetry, robot.vision, "Sighting Diagnostics",
                "Set CameraMount.* in Panels to match the real mounting.",
                "Set expectedRangeIn to compare against a tape measure.");
    }

    @Override
    protected void onLoop() {
        VisionTelemetry.addStatusHeader(telemetry, robot.vision);

        telemetry.addLine("--- MOUNTING (tune in Panels) ---");
        telemetry.addData("fwd / left / up", "%.2f / %.2f / %.2f in",
                CameraMount.mountForwardIn, CameraMount.mountLeftIn, CameraMount.mountUpIn);
        telemetry.addData("pitch / yaw", "%.1f° / %.1f°",
                CameraMount.pitchDeg, CameraMount.yawDeg);
        telemetry.addLine();

        telemetry.addLine("--- CELL STATE GEOMETRY (tune in Panels) ---");
        if (Double.isNaN(CellStateTracker.Geometry.upRowHeightIn)
                || Double.isNaN(CellStateTracker.Geometry.downRowHeightIn)) {
            telemetry.addLine("  NOT MEASURED - every cell will report UNKNOWN.");
            telemetry.addLine("  Settle a cell, read ROW HEIGHT below, set up/down.");
        } else {
            telemetry.addData("  up / down / tol", "%.2f / %.2f / %.2f in",
                    CellStateTracker.Geometry.upRowHeightIn,
                    CellStateTracker.Geometry.downRowHeightIn,
                    CellStateTracker.Geometry.heightToleranceIn);
        }
        telemetry.addLine();

        boolean any = false;
        for (HiveCell cell : HiveCell.values()) {
            CellSighting sighting = robot.vision.sighting(cell);
            if (sighting == null) continue;
            any = true;

            telemetry.addLine("--- " + cell + " ---");
            telemetry.addData("  tags", "%d %s %s",
                    sighting.tagCount(),
                    sighting.tagIds(),
                    sighting.lateralCorrectionApplied() ? "" : "(uncorrected)");
            telemetry.addData("  range / slant", "%.1f / %.1f in",
                    sighting.groundRangeIn(), sighting.slantRangeIn());
            telemetry.addData("  bearing", "%.2f°  (+ = target is left)",
                    Math.toDegrees(sighting.bearingRad()));
            telemetry.addData("  elevation", "%.2f°", Math.toDegrees(sighting.elevationRad()));
            telemetry.addData("  robot-frame XYZ", sighting.rowCentreRobot().toString());
            telemetry.addData("  age", "%.0f ms", sighting.ageMs());

            // The number to write down. Park a settled cell, read this, and put it
            // into CellStateTracker.Geometry as the UP or DOWN nominal height.
            telemetry.addData("  ROW HEIGHT", "%.2f in  <- record for UP/DOWN geometry",
                    sighting.rowCentreRobot().z());
            telemetry.addData("  state", "%s (candidate %s x%d)",
                    robot.vision.state(cell),
                    robot.vision.stateTracker(cell).candidateState(),
                    robot.vision.stateTracker(cell).candidateSamples());

            if (expectedRangeIn > 0.0) {
                double error = sighting.groundRangeIn() - expectedRangeIn;
                telemetry.addData("  range error", "%+.2f in (%+.1f%%)",
                        error, 100.0 * error / expectedRangeIn);
            }
        }

        if (!any) {
            telemetry.addLine("No HIVE CELL in view.");
            telemetry.addLine("If tags are visible but nothing appears here, check");
            telemetry.addLine("'3D poses missing' above and run Vision: Raw Tag Dump.");
        }

        telemetry.update();
    }
}
