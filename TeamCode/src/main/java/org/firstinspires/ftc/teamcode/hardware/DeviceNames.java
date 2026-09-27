package org.firstinspires.ftc.teamcode.hardware;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Every hardware device name this codebase uses, in one place.
 *
 * <p>This is the Java half of the robot configuration. The other half is the
 * bundled XML in {@code TeamCode/src/main/res/xml/robot_*.xml}. {@code
 * RobotConfigXmlTest} fails the build if the two ever disagree, so a rename in
 * one place without the other is a red CI check rather than a {@code null}
 * device three weeks later.
 *
 * <p><b>Never write a device name as a string literal anywhere else.</b> Last
 * season's project had a five-name registry that covered a quarter of the
 * robot; every other name lived as a mutable {@code public String motorName}
 * field on a dashboard-editable config object, and two OpModes bypassed the
 * registry entirely with raw literals. A device name is not a tunable.
 *
 * <p>Adding a device is three edits: a constant here, an entry in the device
 * list of every robot that has it ({@link #COMPETITION_ROBOT} for both
 * competition robots), and the element in each of those robots'
 * {@code robot_*.xml}. Miss the third and the test tells you which file.
 *
 * <p><b>History:</b> until 26 Sep 2026 there was one list, {@link #ALL}, and
 * every {@code robot_*.xml} had to declare every device in it. That was fine
 * while every configuration was a whole competition robot. It stopped being
 * fine with the first bench rig — the two-wheel launcher on hub FTC-EoM3,
 * which has one motor and nothing else — so each {@link RobotIdentity} now
 * names its own list. {@code ALL} survives as the union, meaning "every name
 * the code knows about", which is what the literal scan needs.
 */
public final class DeviceNames {

    private DeviceNames() {}

    // Drivetrain (mecanum).
    public static final String FRONT_LEFT = "frontLeft";
    public static final String FRONT_RIGHT = "frontRight";
    public static final String BACK_LEFT = "backLeft";
    public static final String BACK_RIGHT = "backRight";

    // Odometry.
    public static final String PINPOINT = "pinpoint";

    // Launcher flywheels, left to right as seen from behind the robot. Carried
    // over from the DECODE robots, which wired all three on the Expansion Hub.
    public static final String LAUNCHER_LEFT = "launcher_left";
    public static final String LAUNCHER_CENTER = "launcher_center";
    public static final String LAUNCHER_RIGHT = "launcher_right";

    // Two-wheel pinch launcher test rig (hub FTC-EoM3). Both motors hang off
    // one Y-cable on a single port, with one encoder, so software sees one
    // motor. See robot_launcher_rig.xml.
    public static final String LAUNCHER2 = "launcher2";

    /**
     * What kind of port a device occupies. Determines which XML element tags
     * are legal for it, and which Java class {@code ValidateHardware} asks the
     * {@code HardwareMap} for.
     */
    public enum Kind {
        MOTOR,
        SERVO,
        I2C
    }

    /** One required device: the name the code asks for, and what kind it is. */
    public static final class Device {
        public final String name;
        public final Kind kind;

        Device(String name, Kind kind) {
            this.name = name;
            this.kind = kind;
        }

        @Override
        public String toString() {
            return name + " (" + kind + ")";
        }
    }

    /**
     * Every device a competition robot carries. Both {@code robot_19429.xml}
     * and {@code robot_20245.xml} must declare exactly these, and they are
     * what {@code ValidateHardware} looks for on those robots.
     */
    public static final List<Device> COMPETITION_ROBOT = Collections.unmodifiableList(Arrays.asList(
            new Device(FRONT_LEFT, Kind.MOTOR),
            new Device(FRONT_RIGHT, Kind.MOTOR),
            new Device(BACK_LEFT, Kind.MOTOR),
            new Device(BACK_RIGHT, Kind.MOTOR),
            new Device(LAUNCHER_LEFT, Kind.MOTOR),
            new Device(LAUNCHER_CENTER, Kind.MOTOR),
            new Device(LAUNCHER_RIGHT, Kind.MOTOR),
            new Device(PINPOINT, Kind.I2C)));

    /** The two-wheel launcher bench rig: one Y-cabled motor pair, nothing else. */
    public static final List<Device> LAUNCHER_RIG = Collections.unmodifiableList(Arrays.asList(
            new Device(LAUNCHER2, Kind.MOTOR)));

    /**
     * Every device name the code knows about, on any robot — the union of the
     * per-robot lists above. Not a requirement on any one configuration; each
     * {@link RobotIdentity} carries its own list for that.
     */
    public static final List<Device> ALL = union(COMPETITION_ROBOT, LAUNCHER_RIG);

    @SafeVarargs
    private static List<Device> union(List<Device>... lists) {
        Map<String, Device> byName = new LinkedHashMap<>();
        for (List<Device> list : lists) {
            for (Device device : list) {
                Device previous = byName.put(device.name, device);
                if (previous != null && previous.kind != device.kind) {
                    throw new IllegalStateException("\"" + device.name + "\" is a " + previous.kind
                            + " on one robot and a " + device.kind + " on another.");
                }
            }
        }
        return Collections.unmodifiableList(new ArrayList<>(byName.values()));
    }
}
