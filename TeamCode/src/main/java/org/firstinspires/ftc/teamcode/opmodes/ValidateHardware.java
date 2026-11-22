package org.firstinspires.ftc.teamcode.opmodes;

import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.Utility;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.RobotDifferences;
import org.firstinspires.ftc.teamcode.hardware.ActiveConfig;
import org.firstinspires.ftc.teamcode.hardware.DeviceNames;
import org.firstinspires.ftc.teamcode.hardware.RobotIdentity;
import org.firstinspires.ftc.teamcode.subsystems.BeamBreakSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.TurretSubsystem;

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
@Utility(name = "Check: Hardware", description = "Every device the active config expects, and whether it answered")
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
        // The optional devices' switches are RobotDifferences lines: apply them, so the optional
        // devices below are judged against this robot's switches.
        if (identity != null) {
            try {
                RobotDifferences.applyForActiveRobot();
            } catch (RuntimeException e) {
                problems.add("robot differences not applied: " + e.getMessage());
            }
        }
        // An unrecognised config is already a problem; checking it against every
        // name the code knows still says which devices are there.
        List<DeviceNames.Device> expected = identity == null ? requiredSomewhere() : identity.devices;
        List<DeviceNames.Device> optional = identity == null ? DeviceNames.COMPETITION_OPTIONAL : identity.optional;
        line("");
        reportExpectedDevices(expected, problems);
        line("");
        reportOptionalDevices(optional, problems);
        line("");
        List<DeviceNames.Device> known = new ArrayList<>(expected);
        known.addAll(optional);
        reportUnexpectedDevices(known);
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
            } else if (expected.kind == DeviceNames.Kind.ETHERNET) {
                // The SDK builds an Ethernet device from the config alone, so being here
                // proves the config, not the cable.
                data(expected.name, "configured - " + actualType + " (whether it answers: the"
                        + " Link line in any Vision OpMode)");
            } else {
                data(expected.name, "ok - " + actualType);
            }
        }
    }

    /** Every name the code knows, less the ones no robot is required to have. */
    private static List<DeviceNames.Device> requiredSomewhere() {
        List<DeviceNames.Device> list = new ArrayList<>(DeviceNames.ALL);
        list.removeAll(DeviceNames.COMPETITION_OPTIONAL);
        return list;
    }

    /**
     * Devices the robot may have (the beam breaks and the turret's home switch,
     * {@link RobotIdentity#optional}): each one's
     * configured state beside its switch. A switch on with no device is a problem (the OpModes
     * will refuse to start); a device configured with its switch off is only a reminder.
     */
    private void reportOptionalDevices(List<DeviceNames.Device> devices, List<String> problems) {
        line("=== Optional devices ===");
        if (devices.isEmpty()) {
            line("  (none)");
            return;
        }
        for (DeviceNames.Device optional : devices) {
            HardwareDevice found = hardwareMap.tryGet(HardwareDevice.class, optional.name);
            boolean on = fitted(optional.name);
            String switchLine = switchName(optional.name) + (on ? " on" : " off");
            if (found == null) {
                data(optional.name, "not configured (" + switchLine + ")");
                if (on) problems.add(optional.name + ": " + switchLine + " but the active configuration has no "
                        + optional.name + ". Add it to the robot's XML, or turn the switch off.");
                continue;
            }
            Class<?> required = requiredClassFor(optional.kind);
            if (required != null && !required.isInstance(found)) {
                data(optional.name, "WRONG TYPE - is " + found.getClass().getSimpleName() + ", needs "
                        + required.getSimpleName());
                problems.add(optional.name + " is configured as " + found.getClass().getSimpleName()
                        + " but the code uses it as a " + required.getSimpleName() + ".");
            } else {
                String state = found instanceof DigitalChannel
                        ? digitalState(optional.name, ((DigitalChannel) found).getState())
                        : "";
                data(optional.name, "ok - " + found.getClass().getSimpleName() + state + ", " + switchLine
                        + (on ? "" : ": add its RobotDifferences line to use it"));
            }
        }
    }

    /** Whether the optional device {@code name} has its switch on. */
    private static boolean fitted(String name) {
        if (DeviceNames.TURRET_HOME.equals(name)) return TurretSubsystem.homeSwitch;
        return BeamBreakSubsystem.fitted(name);
    }

    /** The switch that turns the optional device {@code name} on. */
    private static String switchName(String name) {
        if (DeviceNames.TURRET_HOME.equals(name)) return "TurretSubsystem.homeSwitch";
        return BeamBreakSubsystem.switchName(name);
    }

    /** What an optional digital device's reading means. Both read low when they see something. */
    private static String digitalState(String name, boolean high) {
        if (DeviceNames.TURRET_HOME.equals(name)) {
            return high ? " (reads HIGH: no magnet)" : " (reads LOW: magnet, the turret on its mark)";
        }
        return high ? " (reads HIGH: beam clear)" : " (reads LOW: beam broken)";
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
            case CR_SERVO:
                return CRServo.class;
            case DIGITAL:
                return DigitalChannel.class;
            case ETHERNET:
                // The only Ethernet device SDK 12 builds (HardwareFactory.mapEthernetOverUsb).
                return Limelight3A.class;
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
