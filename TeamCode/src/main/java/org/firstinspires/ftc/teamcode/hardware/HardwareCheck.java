package org.firstinspires.ftc.teamcode.hardware;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Compares the live {@code HardwareMap} and the active configuration against what the code
 * expects, and reports every problem by name. <b>Never throws.</b>
 *
 * <p>Every match OpMode starts with:
 *
 * <pre>{@code
 * HardwareCheck.prepare(hardwareMap).addTo(telemetry);
 * }</pre>
 *
 * <p>The rule this implements is <b>never silent, never fatal</b>. A robot with a device missing
 * from its {@code HardwareMap} must still play the match, and the drive team must still find out
 * before it starts:
 *
 * <ul>
 *   <li><b>Not silent:</b> each missing or wrong-typed device, a missing or unrecognised
 *       configuration, and a configuration that belongs to the other robot all appear in init
 *       telemetry, by name.
 *   <li><b>Not fatal:</b> {@link #prepare} puts a {@link StandIn} in the {@code HardwareMap} for
 *       each missing motor or servo, so code that looks the device up gets a do-nothing device
 *       instead of an exception. Four drive motors minus one still drives.
 * </ul>
 *
 * <p>"Missing" means absent from the map, because the SDK builds the map from the configuration.
 * A motor cable unplugged from a connected hub is <em>not</em> detected here: the device still
 * exists and simply does nothing. See the README table under "Never silent, never fatal".
 *
 * <p>A missing I2C device (the Pinpoint) cannot be stood in for — see {@link StandIn}. Code that
 * depends on one must ask {@link #isMissing(String)} and decide its own fallback.
 *
 * <p><b>Stand-ins last until the robot restarts.</b> The SDK builds one {@code HardwareMap} and
 * keeps it across OpModes. Plugging the device back in is not enough; restart the robot (DS menu →
 * Restart Robot) so the real device is found again.
 */
public final class HardwareCheck {

    /** How one expected device looked. */
    public enum Status {
        OK,
        /** Not in the active configuration at all (or only a stand-in is). */
        MISSING,
        /** Present under this name, but as a different kind of device. */
        WRONG_TYPE
    }

    private final Map<String, Status> statuses = new LinkedHashMap<>();
    private final List<String> problems = new ArrayList<>();
    private final List<String> standInsInstalled = new ArrayList<>();
    private String configName;
    private boolean configBundled;
    private RobotIdentity robot;
    private String controlHubName;

    private HardwareCheck() {}

    /**
     * Checks everything and installs stand-ins for missing motors and servos. Call this at the
     * start of init in every match OpMode.
     *
     * <p>Not for tuning: tuning against a stand-in would measure a motor that is not there. The
     * tuners in {@code pedro/Tuning.java} refuse to run if they find one.
     */
    public static HardwareCheck prepare(HardwareMap hardwareMap) {
        return run(hardwareMap, true);
    }

    /** Checks everything and changes nothing. What the Validate Hardware OpMode uses. */
    public static HardwareCheck inspect(HardwareMap hardwareMap) {
        return run(hardwareMap, false);
    }

    private static HardwareCheck run(HardwareMap hardwareMap, boolean installStandIns) {
        HardwareCheck check = new HardwareCheck();
        try {
            check.checkConfiguration();
        } catch (RuntimeException | LinkageError e) {
            check.problems.add("Could not read the active configuration: " + e);
        }
        for (DeviceNames.Device device : DeviceNames.ALL) {
            try {
                check.checkDevice(hardwareMap, device, installStandIns);
            } catch (RuntimeException | LinkageError e) {
                check.statuses.put(device.name, Status.MISSING);
                check.problems.add(device.name + ": could not be checked (" + e + ").");
            }
        }
        return check;
    }

    private void checkConfiguration() {
        configName = ActiveConfig.name();
        controlHubName = ActiveConfig.deviceName();
        RobotIdentity fromHub = RobotIdentity.fromDeviceName(controlHubName);

        if (configName == null) {
            problems.add("No configuration is active. On the Driver Station: Configure Robot -> "
                    + "select " + ActiveConfig.knownConfigNames() + " -> Activate.");
            robot = fromHub;
            return;
        }

        configBundled = ActiveConfig.isBundled();
        robot = RobotIdentity.fromConfigName(configName);

        if (robot == null) {
            problems.add("Config \"" + configName + "\" is not one of this robot's ("
                    + ActiveConfig.knownConfigNames() + "). Select the right one on the "
                    + "Driver Station.");
            robot = fromHub;
        } else if (fromHub != null && fromHub != robot) {
            problems.add("WRONG ROBOT'S CONFIG: this Control Hub is \"" + controlHubName
                    + "\" (" + fromHub.teamNumber + ") but the active config is \"" + configName
                    + "\" (" + robot.teamNumber + "). Select " + fromHub.configName
                    + " on the Driver Station.");
        }

        if (!configBundled) {
            problems.add("Config \"" + configName + "\" was made or edited on the Driver "
                    + "Station, so it is not the one in the repo. Fine for a match; afterwards, "
                    + "copy the change into res/xml and go back to the bundled config.");
        }
    }

    private void checkDevice(HardwareMap hardwareMap, DeviceNames.Device device,
                             boolean installStandIns) {
        Class<? extends HardwareDevice> required = requiredClassFor(device.kind);
        HardwareDevice any = hardwareMap.tryGet(HardwareDevice.class, device.name);

        // tryGet returns the first device registered under a name. Real devices are registered
        // when the SDK builds the map, stand-ins only later, so "first is a stand-in" means no
        // real device exists under this name.
        Status status;
        String detail;
        if (any == null || StandIn.is(any)) {
            status = Status.MISSING;
            detail = device.name + " is MISSING (expected a " + device.kind + ").";
        } else if (required != null && !isRealInstance(hardwareMap, required, device.name)) {
            status = Status.WRONG_TYPE;
            detail = device.name + " is configured as " + any.getClass().getSimpleName()
                    + " but the code uses it as a " + required.getSimpleName() + ".";
        } else {
            statuses.put(device.name, Status.OK);
            return;
        }
        statuses.put(device.name, status);

        if (required == null) {
            problems.add(detail + " No stand-in is possible; whatever uses it will not work.");
            return;
        }
        boolean alreadyStoodIn = StandIn.is(hardwareMap.tryGet(required, device.name));
        if (installStandIns && !alreadyStoodIn) {
            hardwareMap.put(device.name, (HardwareDevice) StandIn.create(required, device.name));
            standInsInstalled.add(device.name);
        }
        problems.add(detail + (installStandIns || alreadyStoodIn
                ? " Running without it." : ""));
    }

    private static boolean isRealInstance(HardwareMap hardwareMap,
                                          Class<? extends HardwareDevice> type, String name) {
        Object found = hardwareMap.tryGet(type, name);
        return found != null && !StandIn.is(found);
    }

    /**
     * The interface a device of this kind must implement, which is also what its stand-in
     * implements — or {@code null} for kinds that cannot be stood in for.
     */
    private static Class<? extends HardwareDevice> requiredClassFor(DeviceNames.Kind kind) {
        switch (kind) {
            case MOTOR:
                return DcMotorEx.class;
            case SERVO:
                return Servo.class;
            default:
                // I2C covers many unrelated concrete driver classes; presence is the useful
                // check, and a Proxy cannot implement a class.
                return null;
        }
    }

    // ------------------------------------------------------------- results

    /** True if nothing is wrong. */
    public boolean isOk() {
        return problems.isEmpty();
    }

    /** Every problem, one sentence each, naming the device or config. */
    public List<String> problems() {
        return Collections.unmodifiableList(problems);
    }

    /**
     * True if the named device is not usable as real hardware — missing, wrong type, or only a
     * stand-in. Use this before building anything that needs an I2C device such as the Pinpoint.
     */
    public boolean isMissing(String deviceName) {
        Status status = statuses.get(deviceName);
        return status == null || status != Status.OK;
    }

    /** Status of each device in {@link DeviceNames#ALL}, in that order. */
    public Map<String, Status> statuses() {
        return Collections.unmodifiableMap(statuses);
    }

    /** Devices that got a stand-in during this call. */
    public List<String> standInsInstalled() {
        return Collections.unmodifiableList(standInsInstalled);
    }

    /**
     * Which robot this is: from the active config, or from the Control Hub's name if the config
     * is missing or unrecognised. {@code null} if neither says.
     */
    public RobotIdentity robot() {
        return robot;
    }

    /** Name of the active configuration, or {@code null}. */
    public String configName() {
        return configName;
    }

    /** True if the active configuration is bundled in the APK (not a Driver Station copy). */
    public boolean isConfigBundled() {
        return configBundled;
    }

    /** The Control Hub's device name, or {@code null}. */
    public String controlHubName() {
        return controlHubName;
    }

    /**
     * Writes a short summary to telemetry: one line if all is well, otherwise a headline and one
     * line per problem. Does not call {@code telemetry.update()}.
     */
    public void addTo(Telemetry telemetry) {
        String robotName = robot == null ? "UNKNOWN ROBOT" : robot.configName;
        if (isOk()) {
            telemetry.addLine("Hardware OK: " + robotName + ", all " + DeviceNames.ALL.size()
                    + " devices found.");
            return;
        }
        telemetry.addLine("!! HARDWARE: " + problems.size() + " problem(s) on " + robotName
                + ". The robot will still run.");
        for (String problem : problems) {
            telemetry.addLine("!! " + problem);
        }
        telemetry.addLine("Run \"Validate Hardware\" for details.");
    }
}
