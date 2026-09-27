package org.firstinspires.ftc.teamcode.opmodes;

import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
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
 * <p>Reports during init, so you do not have to press play. The report goes
 * to Panels ({@code http://192.168.43.1:8001}) and the Driver Station together.
 */
@TeleOp(name = "Validate Hardware", group = "Diagnostics")
public class ValidateHardware extends LinearOpMode {

    /**
     * The finished report, one line per entry. Built once — the hardware map
     * does not change while the OpMode runs — and re-sent every loop.
     */
    private final List<String> report = new ArrayList<>();

    @Override
    public void runOpMode() {
        List<String> problems = new ArrayList<>();

        RobotIdentity identity = reportActiveConfig(problems);
        // An unrecognised config is already a problem; checking it against every
        // name the code knows still says which devices are there.
        List<DeviceNames.Device> expected = identity == null ? DeviceNames.ALL : identity.devices;
        line("");
        reportExpectedDevices(expected, problems);
        line("");
        reportUnexpectedDevices(expected);
        line("");

        if (problems.isEmpty()) {
            line("RESULT: all " + expected.size() + " expected devices present.");
        } else {
            line("RESULT: " + problems.size() + " problem(s):");
            for (String problem : problems) {
                line("  - " + problem);
            }
        }

        TelemetryManager panels = tryGetPanels();

        // Re-published every loop rather than once: Panels only shows what
        // arrives while a browser is connected, so a single update at init is
        // gone before anyone opens the page.
        while (opModeInInit()) {
            publish(panels);
            sleep(100);
        }
        while (opModeIsActive()) {
            publish(panels);
            sleep(100);
        }
    }

    /**
     * Sends the report to Panels and, through {@code update(telemetry)}, to the
     * Driver Station as well. If Panels is unavailable or throws, falls back to
     * the Driver Station alone: this is the OpMode you run when something is
     * wrong, so it must not depend on the dashboard being healthy.
     */
    private void publish(TelemetryManager panels) {
        if (panels != null) {
            try {
                for (String entry : report) {
                    panels.debug(entry);
                }
                panels.update(telemetry);
                return;
            } catch (RuntimeException ignored) {
                // Fall through to the Driver Station only.
            }
        }
        telemetry.clearAll();
        for (String entry : report) {
            telemetry.addLine(entry);
        }
        telemetry.update();
    }

    private static TelemetryManager tryGetPanels() {
        try {
            return PanelsTelemetry.INSTANCE.getTelemetry();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private void line(String text) {
        report.add(text);
    }

    private void data(String key, Object value) {
        report.add(key + ": " + value);
    }

    /** Reports the active configuration and returns its robot, or null if it has none. */
    private RobotIdentity reportActiveConfig(List<String> problems) {
        line("=== Active configuration ===");

        String activeName = ActiveConfig.name();
        if (activeName == null) {
            data("Config", "NONE SELECTED");
            problems.add("No configuration is active. Configure Robot -> select one -> Activate.");
            return null;
        }

        data("Config", activeName);

        if (ActiveConfig.isBundled()) {
            data("Source", "bundled in APK (read-only, cannot drift)");
        } else {
            data("Source", "ROBOT STORAGE - editable, can drift from the repo");
            problems.add("Active config \"" + activeName + "\" is not one of the bundled ones. "
                    + "Someone made it by hand on the Driver Station, so it is not under "
                    + "version control and may not match this code.");
        }

        RobotIdentity identity = RobotIdentity.fromConfigName(activeName);
        if (identity == null) {
            data("Robot", "UNRECOGNISED");
            problems.add("Config \"" + activeName + "\" maps to no RobotIdentity. Known: "
                    + knownConfigNames() + ".");
        } else {
            data("Robot", identity.name());
        }
        return identity;
    }

    private void reportExpectedDevices(List<DeviceNames.Device> devices, List<String> problems) {
        line("=== Expected devices ===");

        for (DeviceNames.Device expected : devices) {
            HardwareDevice found = hardwareMap.tryGet(HardwareDevice.class, expected.name);

            if (found == null) {
                data(expected.name, "MISSING (expected " + expected.kind + ")");
                problems.add(expected.name + " is missing from the active configuration.");
                continue;
            }

            Class<?> required = requiredClassFor(expected.kind);
            String actualType = found.getClass().getSimpleName();

            if (required != null && !required.isInstance(found)) {
                data(expected.name,
                        "WRONG TYPE - is " + actualType + ", needs " + required.getSimpleName());
                problems.add(expected.name + " is configured as " + actualType
                        + " but the code uses it as a " + required.getSimpleName() + ".");
            } else {
                data(expected.name, "ok - " + actualType);
            }
        }
    }

    /**
     * Devices the robot has that the code knows nothing about. Usually
     * harmless, but it is how you spot a device that was renamed on one side
     * only — the old name shows up here while the new one shows up as MISSING.
     */
    private void reportUnexpectedDevices(List<DeviceNames.Device> devices) {
        line("=== Configured but unused ===");

        SortedSet<String> expectedNames = new TreeSet<>();
        for (DeviceNames.Device expected : devices) {
            expectedNames.add(expected.name);
        }

        SortedSet<String> unused = new TreeSet<>(hardwareMap.getAllNames(HardwareDevice.class));
        unused.removeAll(expectedNames);

        if (unused.isEmpty()) {
            line("  (none)");
            return;
        }
        for (String name : unused) {
            line("  " + name);
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
