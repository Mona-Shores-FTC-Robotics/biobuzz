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

    // AprilTag camera: a Limelight 3A on the Control Hub's USB-Ethernet link.
    public static final String LIMELIGHT = "limelight";

    // Launcher: two flywheels, left and right as seen from behind the robot,
    // and the feeder that pushes a piece into them. Not built yet (Oct 2026);
    // the names and the ports in the XML are the plan, Expansion Hub motor
    // ports 1, 2 and 3 after the intake on 0, so the build team wires to them.
    public static final String LAUNCHER_LEFT = "launcher_left";
    public static final String LAUNCHER_RIGHT = "launcher_right";
    public static final String FEEDER = "feeder";

    /**
     * The turret's angle encoder is read through this motor's encoder socket, not a device of its
     * own. A Studica Cypher Max rides the turret's drive gear; its quadrature (A/B/I) output plugs
     * into the encoder socket beside Expansion Hub motor port 3, whose motor is {@link #FEEDER}
     * (the build team's choice, 24 Oct 2026: the drive motors' sockets hold their own encoders).
     * The SDK reads whatever is plugged into a socket as that motor's position, so the feeder's
     * {@code getVelocity()} on a competition robot is the turret encoder moving, not the feeder;
     * the feeder runs without its encoder and shows that number only on the Robot page. Code
     * that wants the turret angle asks for this motor and reads only its encoder; nothing may set
     * its power except the feeder code, and nothing may reset its count except the turret code.
     * The socket is in each robot's XML.
     */
    public static final String TURRET_ENCODER_HOST = FEEDER;
    // The launcher bench's Y-cabled flywheel pair: both motors on one port, so
    // software sees one motor. Bench only; the robots have LAUNCHER_LEFT/RIGHT.
    public static final String LAUNCHER = "launcher";

    /**
     * The FLOWER extractor's first edition: one servo swinging a stick (#177).
     * Control Hub servo port 1; the port itself is in each robot's XML.
     */
    public static final String FLOWER_STICK = "flowerStick";
    // The intake's star wheels (the intake-assist wheels: SWYFT wheels cut
    // into stars on a continuous-rotation servo with one-way clutches, #160).
    // Control Hub servo port 5 on both robots, wired 24 Oct 2026.
    public static final String INTAKE_STARS = "intakeStars";

    /**
     * The prefeed wheel: the big grey wheel at the start of the transfer, on a goBILDA Torque servo
     * in continuous-rotation mode. Control Hub servo port 2; the port itself is in each robot's XML.
     */
    public static final String PREFEED = "prefeed";

    /**
     * The feed gate: a goBILDA Speed servo (positional) turning a plate between BLOCKING and
     * FEEDING. Control Hub servo port 3; the port itself is in each robot's XML.
     */
    public static final String FEED_GATE = "feedGate";

    /**
     * The indicator light: a goBILDA RGB Indicator Light, a servo whose position is its colour
     * ({@code subsystems/IndicatorSubsystem}). Expansion Hub servo port 0 on both robots.
     */
    public static final String INDICATOR = "indicator";

    /**
     * The turret's drive: a goBILDA servo in continuous-rotation mode, so it occupies a servo port
     * and a {@code <ContinuousRotationServo>} element, not a motor port (all eight are taken).
     * Control Hub servo port 0; the port itself is in each robot's XML. Its angle comes from
     * {@link #TURRET_ENCODER_HOST}.
     */
    public static final String TURRET = "turret";

    // Turret angle sensing: a Digital Chicken Labs OctoQuad (FTC Edition) on
    // I2C, reading two REV Thru-Bore absolute outputs on its pulse-width
    // channels (#169). On the OctoQuad bench rig today; on the competition
    // robots once a turret exists (#174 adds it to COMPETITION_ROBOT).
    public static final String OCTOQUAD = "octoquad";

    /**
     * The intake roller: one goBILDA 5203 motor (#160's floating roller, #181 counts pieces from
     * its current). Expansion Hub motor port 0; the port itself is in each robot's XML.
     */
    public static final String INTAKE = "intake";

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
        /** A positional servo: {@code <Servo>}, asked for as {@code Servo}. */
        SERVO,
        /**
         * A continuous-rotation servo: {@code <ContinuousRotationServo>}, asked for as
         * {@code CRServo}. A different kind from {@link #SERVO} because the SDK builds a different
         * class for it, so {@code ValidateHardware} has to ask for a different one; it shares the
         * servo ports.
         */
        CR_SERVO,
        I2C,

        /**
         * A device on the Control Hub's USB-Ethernet link — in SDK 12, a
         * Limelight 3A. It occupies no hub port at all: the SDK declares it as
         * an {@code <EthernetDevice>} directly under {@code <Robot>}, beside
         * the {@code <LynxUsbDevice>}, addressed by {@code ipAddress}. Its
         * equivalent of a port range check is that address.
         *
         * <p>Named after the SDK's own element rather than something broader
         * like {@code USB} or {@code PORTLESS}, because the webcam is also
         * portless and is a different element ({@code <Webcam>}) with
         * different attributes (a per-unit serial number). Lumping the two
         * would make the kind mean "not a hub port" rather than "checked
         * like this".
         */
        ETHERNET
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
            new Device(INTAKE, Kind.MOTOR),
            new Device(LAUNCHER_LEFT, Kind.MOTOR),
            new Device(LAUNCHER_RIGHT, Kind.MOTOR),
            new Device(FEEDER, Kind.MOTOR),
            new Device(FLOWER_STICK, Kind.SERVO),
            new Device(INTAKE_STARS, Kind.CR_SERVO),
            new Device(PREFEED, Kind.CR_SERVO),
            new Device(FEED_GATE, Kind.SERVO),
            new Device(INDICATOR, Kind.SERVO),
            new Device(TURRET, Kind.CR_SERVO),
            new Device(PINPOINT, Kind.I2C),
            new Device(LIMELIGHT, Kind.ETHERNET)));

    /** The two-wheel launcher bench rig: one Y-cabled motor pair, nothing else. */
    public static final List<Device> LAUNCHER_RIG = Collections.unmodifiableList(Arrays.asList(
            new Device(LAUNCHER2, Kind.MOTOR)));

    /**
     * Every device name the code knows about, on any robot — the union of the
     * per-robot lists above. Not a requirement on any one configuration; each
     * {@link RobotIdentity} carries its own list for that.
     */
    /**
     * The launcher bench (robot_launcher_bench.xml): a spare Control Hub with the two flywheel motors
     * Y-cabled to one port (one motor to software, {@link #LAUNCHER}) and the two feeder motors
     * Y-cabled to another ({@link #FEEDER}).
     */
    public static final List<Device> LAUNCHER_BENCH = Collections.unmodifiableList(Arrays.asList(
            new Device(FEEDER, Kind.MOTOR),
            new Device(LAUNCHER, Kind.MOTOR)));

    /** The OctoQuad turret-encoder bench rig (robot_octoquad_rig.xml): a spare Control Hub and the OctoQuad. */
    public static final List<Device> OCTOQUAD_RIG = Collections.unmodifiableList(Arrays.asList(
            new Device(OCTOQUAD, Kind.I2C)));

    public static final List<Device> ALL = union(COMPETITION_ROBOT, LAUNCHER_RIG, LAUNCHER_BENCH, OCTOQUAD_RIG);

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
