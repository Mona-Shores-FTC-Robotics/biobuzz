package org.firstinspires.ftc.teamcode.opmodes.calibration;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.opmodes.RobotOpMode;
import org.firstinspires.ftc.teamcode.util.WelfordVariance;
import org.firstinspires.ftc.teamcode.vision.CellSighting;
import org.firstinspires.ftc.teamcode.vision.HiveCell;

/**
 * Measures how noisy a stationary sighting is: running mean and standard
 * deviation of range, bearing and elevation to one HIVE CELL.
 *
 * <p>Those numbers are what a downstream filter needs to know how far to trust a
 * sighting against odometry, and they are worth re-measuring at a few ranges —
 * AprilTag noise grows quickly with distance and with shallow viewing angles.
 *
 * <h2>Procedure</h2>
 * <ol>
 *   <li>Park the robot so one cell is visible. <b>Don't move it</b> — this measures
 *       measurement noise, not motion.</li>
 *   <li>Pick the cell with {@link #cellOrdinal} in Panels, or leave it and the
 *       first visible cell is used.</li>
 *   <li>Press PLAY and wait for the sample count to climb.</li>
 *   <li>Gamepad1 cross resets, so you can move to a new distance and resample.</li>
 * </ol>
 *
 * <h2>Why this counts samples differently to last year's tuner</h2>
 *
 * DECODE's {@code MeasurementStdevTuner} took a sample whenever a new snapshot
 * object had been constructed — which happened on every poll, whether or not the
 * camera had produced a new frame. Re-reading one frame ten times looks like ten
 * independent measurements that happen to agree perfectly, which drags the
 * reported deviation toward zero. This one keys off the frame's own capture
 * timestamp, so a repeated frame contributes nothing.
 *
 * <p>It also measures range and bearing rather than a field pose, because under
 * BIOBUZZ there is no field pose to measure.
 */
@TeleOp(name = "Vision: Noise Tuner", group = "Vision")
@Configurable
public class VisionNoiseTuner extends RobotOpMode {

    /** Which cell to sample, as an index into {@link HiveCell#values()}. -1 = first visible. */
    public static int cellOrdinal = -1;

    private final WelfordVariance rangeIn = new WelfordVariance();
    private final WelfordVariance bearingDeg = new WelfordVariance();
    private final WelfordVariance elevationDeg = new WelfordVariance();

    private HiveCell sampledCell;
    private long lastSampledCaptureNs = Long.MIN_VALUE;
    private int singleTagSamples = 0;
    private boolean prevCross = false;

    @Override
    protected void onInit() {
        VisionTelemetry.addBanner(telemetry, robot.vision, "Vision Noise Tuner",
                "Park the robot with a HIVE CELL in view and hold still.",
                "Gamepad1 cross resets the statistics.");
    }

    @Override
    protected void onLoop() {
        if (gamepad1.cross && !prevCross) resetStats();
        prevCross = gamepad1.cross;

        CellSighting sighting = pickSighting();
        if (sighting != null && sighting.captureTimeNs() != lastSampledCaptureNs) {
            lastSampledCaptureNs = sighting.captureTimeNs();

            if (sampledCell != sighting.cell()) {
                // Mixing two cells into one distribution would be meaningless.
                resetStats();
                sampledCell = sighting.cell();
                lastSampledCaptureNs = sighting.captureTimeNs();
            }

            rangeIn.update(sighting.groundRangeIn());
            bearingDeg.update(Math.toDegrees(sighting.bearingRad()));
            elevationDeg.update(Math.toDegrees(sighting.elevationRad()));
            if (!sighting.lateralCorrectionApplied()) singleTagSamples++;
        }

        telemetry.addData("Camera", robot.vision.isAvailable() ? robot.vision.state() : "UNAVAILABLE");
        telemetry.addData("Cell", sampledCell == null ? "none yet" : sampledCell.toString());
        telemetry.addData("Samples", rangeIn.n());
        if (rangeIn.n() > 0) {
            telemetry.addData("Single-tag samples", "%d of %d", singleTagSamples, rangeIn.n());
        }
        telemetry.addLine();

        if (rangeIn.n() < 2) {
            telemetry.addLine("Waiting for samples. Two or more are needed for a deviation.");
            telemetry.update();
            return;
        }

        telemetry.addLine("--- MEAN ---");
        telemetry.addData("range", "%.2f in", rangeIn.mean());
        telemetry.addData("bearing", "%.3f°", bearingDeg.mean());
        telemetry.addData("elevation", "%.3f°", elevationDeg.mean());
        telemetry.addLine();
        telemetry.addLine("--- STD DEV ---");
        telemetry.addData("range", "%.4f in", rangeIn.stdDev());
        telemetry.addData("bearing", "%.4f° (%.6f rad)",
                bearingDeg.stdDev(), Math.toRadians(bearingDeg.stdDev()));
        telemetry.addData("elevation", "%.4f° (%.6f rad)",
                elevationDeg.stdDev(), Math.toRadians(elevationDeg.stdDev()));
        telemetry.addLine();
        telemetry.addLine("Re-measure at a few distances; noise grows with range.");

        telemetry.update();
    }

    private CellSighting pickSighting() {
        HiveCell[] cells = HiveCell.values();
        if (cellOrdinal >= 0 && cellOrdinal < cells.length) {
            return robot.vision.sighting(cells[cellOrdinal]);
        }
        if (sampledCell != null) {
            CellSighting current = robot.vision.sighting(sampledCell);
            if (current != null) return current;
        }
        for (HiveCell cell : cells) {
            CellSighting candidate = robot.vision.sighting(cell);
            if (candidate != null) return candidate;
        }
        return null;
    }

    private void resetStats() {
        rangeIn.reset();
        bearingDeg.reset();
        elevationDeg.reset();
        singleTagSamples = 0;
        sampledCell = null;
        lastSampledCaptureNs = Long.MIN_VALUE;
    }
}
