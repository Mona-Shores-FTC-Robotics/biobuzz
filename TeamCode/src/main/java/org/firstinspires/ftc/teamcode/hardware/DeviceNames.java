package org.firstinspires.ftc.teamcode.hardware;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
 * <p>Adding a device is three edits: a constant here, an entry in {@link #ALL},
 * and the element in every {@code robot_*.xml}. Miss the third and the test
 * tells you which file.
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
     * Every device the code requires. Each one must be declared by every
     * {@code robot_*.xml}, and must be present in the live {@code HardwareMap}
     * at run time.
     */
    public static final List<Device> ALL = Collections.unmodifiableList(Arrays.asList(
            new Device(FRONT_LEFT, Kind.MOTOR),
            new Device(FRONT_RIGHT, Kind.MOTOR),
            new Device(BACK_LEFT, Kind.MOTOR),
            new Device(BACK_RIGHT, Kind.MOTOR),
            new Device(PINPOINT, Kind.I2C)));
}
