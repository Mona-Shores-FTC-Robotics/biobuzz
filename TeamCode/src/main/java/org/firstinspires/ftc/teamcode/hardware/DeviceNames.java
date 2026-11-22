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
 * fine with the first bench rig — a two-wheel launcher on its own hub (since
 * retired), which had one motor and nothing else — so each {@link RobotIdentity} now
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
     * The motor whose encoder socket holds the turret's angle encoder on a robot not yet moved to a
     * drive socket ({@code TurretSubsystem.encoderOnDriveMotor} off). A REV Through Bore Encoder
     * rides the turret's drive gear; its quadrature output plugs into the encoder socket beside
     * Expansion Hub motor port 0, whose motor is {@link #INTAKE} (the mentor's call at the robot,
     * 17 Nov 2026; until then a Studica Cypher Max in the feeder's socket). The SDK reads whatever
     * is plugged into a socket as that motor's position, so there the intake's
     * {@code getCurrentPosition()} and {@code getVelocity()} are the turret turning, not the
     * roller, and everything of the intake's that needs its own encoder is off
     * ({@code IntakeSubsystem.ownEncoder()}). Code that wants the turret's host asks
     * {@code TurretSubsystem.encoderHostName()}, never this constant, which is right only while the
     * switch is off.
     */
    public static final String TURRET_ENCODER_HOST = DeviceNames.INTAKE;

    /**
     * The motor whose encoder socket holds the turret's encoder on a robot moved off the intake's
     * ({@code TurretSubsystem.encoderOnDriveMotor}, a {@code RobotDifferences} line per robot as it
     * is rewired): the back-right drive wheel, Control Hub motor port 2 in both robots' XML. Its own
     * encoder is only logged ({@code DriveSubsystem.wheelTicksPerSec}, NaN for this wheel then):
     * odometry is the Pinpoint, and neither Pedro's drivetrain nor ours sets a drive motor's mode
     * or resets its count, so the drive leaves the turret's count alone. Back-right, not another:
     * 20245's back-left socket reads 0 (Belding, Fruitport), and taking a right wheel leaves both of
     * 20245's working diagonals and both sides with a logged wheel. Nothing may set this motor's
     * mode or reset its count except the turret code; it must never run {@code RUN_USING_ENCODER},
     * or the hub's velocity loop would drive the wheel from the turret's count.
     */
    public static final String TURRET_ENCODER_DRIVE_HOST = DeviceNames.BACK_RIGHT;
    // The launcher bench's Y-cabled flywheel pair: both motors on one port, so
    // software sees one motor. Bench only; the robots have LAUNCHER_LEFT/RIGHT.
    public static final String LAUNCHER = "launcher";

    /**
     * The FLOWER extractor's first edition: one servo swinging a stick (#177).
     * A servo port; which one is in each robot's XML (the two robots differ: 19429's new stick's
     * pivot is on its Expansion Hub, 17 Nov 2026).
     */
    public static final String FLOWER_STICK = "flowerStick";

    /**
     * The roller on the end of 19429's new FLOWER stick (17 Nov 2026): a continuous-rotation servo
     * that spins while the stick is down. 19429 only, so far. Its port is in the robot's XML.
     */
    public static final String FLOWER_ROLLER = "flowerRoller";
    // The intake's star wheels (the intake-assist wheels: SWYFT wheels cut
    // into stars on a continuous-rotation servo with one-way clutches, #160).
    // On 20245 one servo drives them all; on 19429 this is the LEFT star wheel's
    // servo and INTAKE_STARS_RIGHT drives the right one. Ports in each robot's XML.
    public static final String INTAKE_STARS = "intakeStars";
    // 19429's right intake star wheel, its own continuous-rotation servo since
    // 13 Nov 2026 (IntakeSubsystem.rightStarWheel). 19429 only, so far.
    public static final String INTAKE_STARS_RIGHT = "intakeStarsRight";

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
     * The indicator lights: goBILDA RGB Indicator Lights, a servo whose position is its colour
     * ({@code subsystems/IndicatorSubsystem}). More than one light, Y-cabled to one port, so they
     * are one device here and all show the same colour. A servo port; which one is in each robot's
     * XML (the two robots differ).
     */
    public static final String INDICATOR = "indicator";

    /**
     * The turret's drive: a goBILDA 25-3 Speed servo in continuous mode, so a
     * {@code <ContinuousRotationServo>} element on a servo port, not a motor port (all eight are
     * taken). It is sent a speed and has no feedback of its own: the turret's angle comes from
     * {@code TurretSubsystem.encoderHostName()}'s socket, and {@code TurretSubsystem} closes the loop on it. An
     * Expansion Hub servo port; which one is in each robot's XML.
     */
    public static final String TURRET = "turret";

    /**
     * The turret's home switch: a REV Magnetic Limit Switch on the chassis, with a magnet on the back
     * of the turret over it when the turret is on its mark. A Control Hub digital channel (connector
     * 0-1; the channel is in the robot's XML); it reads low with the magnet there.
     * {@code TurretSubsystem} zeroes the turret from it. <b>Optional</b> ({@link #TURRET_HOME_SWITCH}):
     * a robot that has it declares it in its XML and turns it on with one {@code RobotDifferences}
     * line ({@code TurretSubsystem.homeSwitch = true}).
     */
    public static final String TURRET_HOME = "turretHome";

    /**
     * The intake roller: one goBILDA 5203 motor (#160's floating roller, #181 counts pieces from
     * its current). Expansion Hub motor port 0; the port itself is in each robot's XML.
     */
    public static final String INTAKE = "intake";

    /**
     * A REV Color Sensor V3 in the transfer, on I2C: what colour is in front of it and how close
     * ({@code subsystems/ColorSensorSubsystem}). Data only for now. Control Hub I2C bus 2; the bus
     * itself is in each robot's XML.
     */
    public static final String COLOR_SENSOR = "colorSensor";

    /**
     * An IR break-beam across the launcher's throat, between the feed gate and the flywheels' nip
     * (#505): it sees each ball leave ({@code subsystems/BeamBreakSubsystem}). A Control Hub digital
     * channel. <b>Optional</b> ({@link #BEAM_BREAKS}): not fitted on either robot yet (22 Nov 2026);
     * a robot that has it declares it in its XML and turns it on with one {@code RobotDifferences}
     * line ({@code BeamBreakSubsystem.throatFitted = true}).
     */
    public static final String THROAT_BEAM = "throatBeam";

    /**
     * An IR break-beam across the transfer lane where the third piece sits when the lane is packed
     * behind the gate (#213): with the entrance colour sensor it decides "full"
     * ({@code ColorSensorSubsystem.full()}). A Control Hub digital channel. <b>Optional</b>, as
     * {@link #THROAT_BEAM}: {@code BeamBreakSubsystem.laneFitted = true} turns it on.
     */
    public static final String LANE_BEAM = "laneBeam";

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
         * A digital input channel: {@code <DigitalDevice>}, asked for as {@code DigitalChannel}.
         * A REV hub has channels 0-7 (four JST PH ports of two channels each); it is part of the
         * bulk read, so reading one every loop costs nothing.
         */
        DIGITAL,

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
     * Every device both competition robots carry. {@code robot_20245.xml} must
     * declare exactly these; {@code robot_19429.xml} these and
     * {@link #ROBOT_19429}'s additions. They are what {@code ValidateHardware}
     * looks for on those robots.
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
            new Device(COLOR_SENSOR, Kind.I2C),
            new Device(LIMELIGHT, Kind.ETHERNET)));

    /**
     * 19429: {@link #COMPETITION_ROBOT}, a second intake star-wheel servo, the right one
     * ({@link #INTAKE_STARS_RIGHT}, 13 Nov 2026) and the new FLOWER stick's roller
     * ({@link #FLOWER_ROLLER}, 17 Nov 2026). 20245 still drives its stars from one servo and has the
     * old stick. The turret's home switch is optional on both ({@link #TURRET_HOME_SWITCH}).
     */
    public static final List<Device> ROBOT_19429 = with(COMPETITION_ROBOT,
            new Device(INTAKE_STARS_RIGHT, Kind.CR_SERVO),
            new Device(FLOWER_ROLLER, Kind.CR_SERVO));

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

    /**
     * Devices a competition robot <em>may</em> carry: the two beam breaks, bought for the Michigan
     * State Championship and not fitted yet (22 Nov 2026). A robot's XML may declare each or not
     * ({@code RobotConfigXmlTest}); the code looks one up only once its switch is on
     * ({@code BeamBreakSubsystem.throatFitted}, {@code laneFitted}, flipped by a
     * {@code RobotDifferences} line), and then fails loudly if the XML lacks it. That switch is
     * the explicit, named decision CLAUDE.md asks of an optional device. Once both robots have
     * one for good, move it into {@link #COMPETITION_ROBOT}.
     */
    public static final List<Device> BEAM_BREAKS = Collections.unmodifiableList(Arrays.asList(
            new Device(THROAT_BEAM, Kind.DIGITAL),
            new Device(LANE_BEAM, Kind.DIGITAL)));

    /**
     * The turret's home switch ({@link #TURRET_HOME}), optional as the {@link #BEAM_BREAKS} are: being
     * fitted to both robots for the Michigan State Championship (22 Nov 2026). The code looks it up
     * only with {@code TurretSubsystem.homeSwitch} on (a {@code RobotDifferences} line), and then
     * fails loudly if the XML lacks it. Once both robots have it for good, move it into
     * {@link #COMPETITION_ROBOT}.
     */
    public static final List<Device> TURRET_HOME_SWITCH = Collections.singletonList(
            new Device(TURRET_HOME, Kind.DIGITAL));

    /** Every optional device a competition robot may carry: {@link #BEAM_BREAKS} and {@link #TURRET_HOME_SWITCH}. */
    public static final List<Device> COMPETITION_OPTIONAL = union(BEAM_BREAKS, TURRET_HOME_SWITCH);

    public static final List<Device> ALL = union(ROBOT_19429, COMPETITION_ROBOT, LAUNCHER_BENCH, COMPETITION_OPTIONAL);

    private static List<Device> with(List<Device> base, Device... more) {
        List<Device> list = new ArrayList<>(base);
        list.addAll(Arrays.asList(more));
        return Collections.unmodifiableList(list);
    }

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
