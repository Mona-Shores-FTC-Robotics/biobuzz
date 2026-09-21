package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.hardware.ActiveConfig;
import org.firstinspires.ftc.teamcode.hardware.DeviceNames;
import org.firstinspires.ftc.teamcode.hardware.RobotIdentity;

import java.util.ArrayList;
import java.util.List;
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
 */
@TeleOp(name = "Validate Hardware", group = "Diagnostics")
public class ValidateHardware extends LinearOpMode {

    @Override
    public void runOpMode() {
        telemetry.setAutoClear(false);

        List<String> problems = new ArrayList<>();

        reportActiveConfig(problems);
        telemetry.addLine();
        reportExpectedDevices(problems);
        telemetry.addLine();
        reportUnexpectedDevices();
        telemetry.addLine();

        if (problems.isEmpty()) {
            telemetry.addLine("RESULT: all " + DeviceNames.ALL.size() + " expected devices present.");
        } else {
            telemetry.addLine("RESULT: " + problems.size() + " problem(s):");
            for (String problem : problems) {
                telemetry.addLine("  - " + problem);
            }
        }
        telemetry.update();

        waitForStart();
        while (opModeIsActive()) {
            idle();
        }
    }

    private void reportActiveConfig(List<String> problems) {
        telemetry.addLine("=== Active configuration ===");

        String activeName = ActiveConfig.name();
        if (activeName == null) {
            telemetry.addData("Config", "NONE SELECTED");
            problems.add("No configuration is active. Configure Robot -> select one -> Activate.");
            return;
        }

        telemetry.addData("Config", activeName);

        if (ActiveConfig.isBundled()) {
            telemetry.addData("Source", "bundled in APK (read-only, cannot drift)");
        } else {
            telemetry.addData("Source", "ROBOT STORAGE - editable, can drift from the repo");
            problems.add("Active config \"" + activeName + "\" is not one of the bundled ones. "
                    + "Someone made it by hand on the Driver Station, so it is not under "
                    + "version control and may not match this code.");
        }

        RobotIdentity identity = RobotIdentity.fromConfigName(activeName);
        if (identity == null) {
            telemetry.addData("Robot", "UNRECOGNISED");
            problems.add("Config \"" + activeName + "\" maps to no RobotIdentity. Known: "
                    + knownConfigNames() + ".");
        } else {
            telemetry.addData("Robot", identity.name());
        }
    }

    private void reportExpectedDevices(List<String> problems) {
        telemetry.addLine("=== Expected devices ===");

        for (DeviceNames.Device expected : DeviceNames.ALL) {
            HardwareDevice found = hardwareMap.tryGet(HardwareDevice.class, expected.name);

            if (found == null) {
                telemetry.addData(expected.name, "MISSING (expected " + expected.kind + ")");
                problems.add(expected.name + " is missing from the active configuration.");
                continue;
            }

            Class<?> required = requiredClassFor(expected.kind);
            String actualType = found.getClass().getSimpleName();

            if (required != null && !required.isInstance(found)) {
                telemetry.addData(expected.name,
                        "WRONG TYPE - is " + actualType + ", needs " + required.getSimpleName());
                problems.add(expected.name + " is configured as " + actualType
                        + " but the code uses it as a " + required.getSimpleName() + ".");
            } else {
                telemetry.addData(expected.name, "ok - " + actualType);
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

    private static Class<?> requiredClassFor(DeviceNames.Kind kind) {
        switch (kind) {
            case MOTOR:
                return DcMotorEx.class;
            case SERVO:
                return Servo.class;
            default:
                // I2C covers many unrelated driver classes; presence plus the
                // reported concrete type is the useful signal, not an
                // interface check.
                return null;
        }
    }

    private static String knownConfigNames() {
        StringBuilder names = new StringBuilder();
        for (RobotIdentity identity : RobotIdentity.values()) {
            if (names.length() > 0) {
                names.append(", ");
            }
            names.append(identity.configName);
        }
        return names.toString();
    }
}
