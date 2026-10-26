package org.firstinspires.ftc.teamcode.opmodes.calibration;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.vision.LimelightVisionSubsystem;

/**
 * The two telemetry blocks every Vision calibration OpMode shows.
 *
 * <p>Both were copied by hand into each OpMode before this existed — the init banner three times,
 * the status header twice — and they had already drifted apart. A calibration run that reads
 * differently depending on which OpMode you happened to open is a bad way to find out your camera
 * is unplugged.
 */
final class VisionTelemetry {

    private VisionTelemetry() {
    }

    /**
     * The init banner: a title, then either why the camera is missing or what to do next.
     *
     * @param whenAvailable lines shown only when the camera was found — the OpMode's instructions
     */
    static void addBanner(Telemetry telemetry,
                          LimelightVisionSubsystem vision,
                          String title,
                          String... whenAvailable) {
        telemetry.addLine("=== " + title + " ===");
        if (!vision.isAvailable()) {
            telemetry.addLine("LIMELIGHT NOT FOUND");
            telemetry.addData("Reason", vision.unavailableReason());
        } else {
            for (String line : whenAvailable) {
                telemetry.addLine(line);
            }
        }
        telemetry.update();
    }

    /**
     * The per-loop camera status header.
     *
     * <p>"Link" is the first line to read: with the bundled configs the camera is always in the
     * hardware map, so a dead USB cable shows up here and nowhere else.
     *
     * <p>"3D poses missing" climbing while tags are plainly visible is the tell that the Limelight
     * pipeline is not emitting full 3D pose — no sighting can be built from a frame like that, so
     * every downstream reading stays empty and nothing else says why.
     */
    static void addStatusHeader(Telemetry telemetry, LimelightVisionSubsystem vision) {
        telemetry.addData("Camera", vision.isAvailable() ? vision.state() : "UNAVAILABLE");
        if (vision.isAvailable()) {
            // The config creates the camera whether or not one is plugged in, so "Camera" alone
            // cannot tell an unplugged Limelight from a working one. This can.
            telemetry.addData("Link", vision.isConnected()
                    ? "ok"
                    : "NO RESPONSE (" + vision.connectionInfo() + ") — plugged in? powered?"
                            + " IP matches robot_*.xml?");
        }
        telemetry.addData("Fresh frames", vision.freshResultCount());
        telemetry.addData("3D poses missing", vision.framesMissing3dPose());
        telemetry.addData("Update", "%.2f ms", vision.lastPeriodicMs());
        telemetry.addLine();
    }
}
