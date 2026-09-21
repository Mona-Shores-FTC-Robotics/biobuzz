package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.HardwareDevice;

import org.firstinspires.ftc.teamcode.hardware.DeviceNames;
import org.firstinspires.ftc.teamcode.hardware.HardwareCheck;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Compares the live {@code hardwareMap} against what {@link DeviceNames}
 * expects, and reports every mismatch by name.
 *
 * <p>This is the run-time half of the drift check. {@code RobotConfigXmlTest}
 * proves at build time that the bundled XML and the Java agree; this proves
 * that what is actually plugged into the robot agrees with both. They catch
 * different things: the wrong configuration selected, an unplugged or dead
 * device, or somebody running a hand-made configuration.
 *
 * <p>It exists because "could not find device" mid-match is a bad way to learn
 * about a rename, and because last season's silent-{@code null} lookups
 * (a {@code try*} helper that swallowed {@code IllegalArgumentException}) meant
 * three missing sensors went unnoticed for weeks.
 *
 * <p>Reports during init, so you do not have to press play.
 *
 * <p>The checks themselves live in {@link HardwareCheck}, which every match
 * OpMode also runs at init; this OpMode is the detailed view of the same
 * result. It uses {@link HardwareCheck#inspect}, so it changes nothing — but
 * it does recognise stand-ins a match OpMode left behind, and reports those
 * devices as missing rather than "ok".
 */
@TeleOp(name = "Validate Hardware", group = "Diagnostics")
public class ValidateHardware extends LinearOpMode {

    @Override
    public void runOpMode() {
        telemetry.setAutoClear(false);

        HardwareCheck check = HardwareCheck.inspect(hardwareMap);

        reportConfiguration(check);
        telemetry.addLine();
        reportExpectedDevices(check);
        telemetry.addLine();
        reportUnexpectedDevices();
        telemetry.addLine();

        if (check.isOk()) {
            telemetry.addLine("RESULT: all " + DeviceNames.ALL.size() + " expected devices present.");
        } else {
            telemetry.addLine("RESULT: " + check.problems().size() + " problem(s):");
            for (String problem : check.problems()) {
                telemetry.addLine("  - " + problem);
            }
        }
        telemetry.update();

        waitForStart();
        while (opModeIsActive()) {
            idle();
        }
    }

    private void reportConfiguration(HardwareCheck check) {
        telemetry.addLine("=== Active configuration ===");

        String configName = check.configName();
        telemetry.addData("Config", configName == null ? "NONE SELECTED" : configName);
        if (configName != null) {
            telemetry.addData("Source", check.isConfigBundled()
                    ? "bundled in APK (read-only, cannot drift)"
                    : "ROBOT STORAGE - editable, can drift from the repo");
        }
        telemetry.addData("Control Hub", check.controlHubName() == null
                ? "(name unavailable)" : check.controlHubName());
        telemetry.addData("Robot", check.robot() == null ? "UNRECOGNISED" : check.robot().name());
    }

    private void reportExpectedDevices(HardwareCheck check) {
        telemetry.addLine("=== Expected devices ===");

        for (Map.Entry<String, HardwareCheck.Status> entry : check.statuses().entrySet()) {
            String name = entry.getKey();
            switch (entry.getValue()) {
                case OK:
                    HardwareDevice device = hardwareMap.tryGet(HardwareDevice.class, name);
                    telemetry.addData(name, "ok - "
                            + (device == null ? "?" : device.getClass().getSimpleName()));
                    break;
                case WRONG_TYPE:
                    telemetry.addData(name, "WRONG TYPE");
                    break;
                default:
                    telemetry.addData(name, "MISSING");
                    break;
            }
        }
    }

    /**
     * Devices the robot has that the code knows nothing about. Usually
     * harmless, but it is how you spot a device that was renamed on one side
     * only — the old name shows up here while the new one shows up as MISSING.
     */
    private void reportUnexpectedDevices() {
        telemetry.addLine("=== Configured but unused ===");

        SortedSet<String> expectedNames = new TreeSet<>();
        for (DeviceNames.Device expected : DeviceNames.ALL) {
            expectedNames.add(expected.name);
        }

        SortedSet<String> unused = new TreeSet<>(hardwareMap.getAllNames(HardwareDevice.class));
        unused.removeAll(expectedNames);

        if (unused.isEmpty()) {
            telemetry.addLine("  (none)");
            return;
        }
        for (String name : unused) {
            telemetry.addLine("  " + name);
        }
    }
}
