package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.follower.Follower;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.hardware.ActiveConfig;
import org.firstinspires.ftc.teamcode.hardware.DeviceNames;
import org.firstinspires.ftc.teamcode.hardware.RobotIdentity;
import org.firstinspires.ftc.teamcode.pedro.robots.Robot19429;
import org.firstinspires.ftc.teamcode.pedro.robots.Robot20245;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Robot configuration for Pedro Pathing: picks the running robot's tuned values and builds Pedro's
 * objects from them.
 *
 * <p>This and {@link Tuning} are the two files in the {@code pedro} package that are ours rather
 * than upstream's — the rest is the Quickstart copied verbatim and should be re-copied, not
 * patched, on the next Pedro release. ({@link RobotConstants} and {@code pedro/robots/} are ours
 * too, and are new; upstream has neither.)
 *
 * <h2>Where the numbers live: one file per robot</h2>
 *
 * <p>The tuned values are <b>not in this file</b>. Each robot has its own, in
 * {@code pedro/robots/}: {@link Robot19429} and {@link Robot20245}. The two robots have identical
 * hardware but measure different numbers, and when there was one set here, tuning the second robot
 * overwrote the first's. Which file is used is decided by the active Driver Station configuration,
 * through {@link ActiveConfig#requireIdentity()} — the same pick that already selects the robot's
 * wiring, so wiring and tuning cannot disagree about which robot this is.
 *
 * <p>Run the tuners <b>on each robot</b>, in this order; each produces the values the next needs:
 *
 * <ol>
 *   <li><b>Mecanum Tuner</b> → {@code drivetrainConfig} in that robot's file.
 *   <li><b>Pinpoint Tuner</b> → {@code localizerConfig} in that robot's file.
 *   <li><b>Foresight Tuner</b> → {@code foresightConfig} in that robot's file, empty until then.
 *   <li><b>Tests</b> → verifies the result.
 * </ol>
 *
 * <p>Each tuner ends on a page of generated Java whose first line is
 * {@code public static <Type> <field> = ...}. Paste it over the field of that name in
 * <b>the file of the robot you ran it on</b> — {@code Robot19429.java} for 19429,
 * {@code Robot20245.java} for 20245 — exactly as the tuner shows it, name lines included.
 * Device names are shared, and {@link RobotConstants} sets them from {@link DeviceNames} regardless;
 * {@code PedroRobotsTest} fails the build only if a pasted name differs from {@link DeviceNames}.
 *
 * <h2>Adding a robot: one file plus one line</h2>
 *
 * <p>Once the robot has its {@link RobotIdentity} and {@code res/xml} config (see that enum):
 * copy {@code Robot19429.java} to {@code Robot<name>.java} in {@code pedro/robots/} (Android
 * Studio renames the class references for you), and add one line to {@link #ROBOTS}. Forgetting
 * the line fails {@code ConstantsTest} for any identity whose device list includes the
 * drivetrain — at build time, not at a meeting.
 *
 * <h2>Rejected alternatives</h2>
 *
 * <ul>
 *   <li><b>Both robots' blocks side by side in this file</b> ({@code drivetrainConfig19429},
 *       {@code drivetrainConfig20245}, …). The tuners emit {@code drivetrainConfig}; every paste
 *       would need a hand rename, and pasting into the wrong-numbered block is a one-character
 *       mistake nobody sees. One file per robot means the field names match the tuner's output
 *       exactly and the file name says which robot it is.
 *   <li><b>Only the differences per robot, applied as overrides to a shared base</b>. Smaller
 *       files, but a tuner's block can no longer be pasted as a whole, and "which value is this
 *       robot actually running?" needs two files to answer.
 *   <li><b>A method on {@link RobotIdentity} returning the configs</b>. Puts Pedro types in the
 *       {@code hardware} package, which is deliberately free of them.
 *   <li><b>A default robot when the identity has no values</b>. That is last season's bug: a
 *       silent 19429/20245 fallback ran one robot on the other's tuning. {@link #forRobot} throws
 *       instead, naming the identity.
 * </ul>
 */
public class Constants {

    /**
     * Every robot with Pedro values. <b>Adding a robot is one line here</b> plus its file in
     * {@code pedro/robots/}.
     *
     * <p>A {@link Supplier}, so a robot's config objects are only built when that robot is the one
     * asked for, and a robot file's static fields are read at call time rather than captured once.
     */
    private static final Map<RobotIdentity, Supplier<RobotConstants>> ROBOTS =
            new EnumMap<>(RobotIdentity.class);

    static {
        ROBOTS.put(RobotIdentity.TEAM_19429, Robot19429::constants);
        ROBOTS.put(RobotIdentity.TEAM_20245, Robot20245::constants);
    }

    /** The devices Pedro drives. An identity carrying any of them needs values in {@link #ROBOTS}. */
    static final List<String> PEDRO_DEVICES = Collections.unmodifiableList(Arrays.asList(
            DeviceNames.FRONT_LEFT, DeviceNames.FRONT_RIGHT,
            DeviceNames.BACK_LEFT, DeviceNames.BACK_RIGHT,
            DeviceNames.PINPOINT));

    /** True if this robot's device list includes any part of Pedro's drivetrain or localizer. */
    public static boolean hasDrivetrain(RobotIdentity identity) {
        for (DeviceNames.Device device : identity.devices) {
            if (PEDRO_DEVICES.contains(device.name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * The tuned values for one robot. Pure — no Android, no active config — so it can be unit
     * tested; the {@link HardwareMap} factories below call it with the active robot.
     *
     * @throws IllegalStateException naming the identity if it has no drivetrain, or has one but no
     *     values registered. Never falls back to another robot's values.
     */
    public static RobotConstants forRobot(RobotIdentity identity) {
        Supplier<RobotConstants> robot = ROBOTS.get(identity);
        if (robot != null) {
            return robot.get();
        }
        if (!hasDrivetrain(identity)) {
            throw new IllegalStateException(
                    "Robot " + identity + " (config \"" + identity.configName + "\") has no "
                            + "drivetrain, so it has no Pedro values and cannot run Pedro, AutoTune "
                            + "or a drive OpMode. If this is the wrong robot, activate the right "
                            + "configuration on the Driver Station.");
        }
        throw new IllegalStateException(
                "Robot " + identity + " (config \"" + identity.configName + "\") has a drivetrain "
                        + "but no Pedro values. Copy pedro/robots/Robot19429.java to a new file for "
                        + "this robot, and register it in Constants.ROBOTS.");
    }

    /** The values for the robot whose configuration is active on the Driver Station. */
    public static RobotConstants forActiveRobot() {
        return forRobot(ActiveConfig.requireIdentity());
    }

    public static Mecanum createDrivetrain(HardwareMap hardwareMap) {
        return new Mecanum(hardwareMap, forActiveRobot().drivetrainConfig);
    }

    public static PinpointLocalizer createLocalizer(HardwareMap hardwareMap) {
        return new PinpointLocalizer(hardwareMap, forActiveRobot().localizerConfig);
    }

    /** Builds the Foresight algorithm for the active robot. See {@link #createAlgorithm(RobotIdentity)}. */
    public static Foresight createAlgorithm() {
        return createAlgorithm(ActiveConfig.requireIdentity());
    }

    /**
     * Builds the Foresight algorithm for one robot, checking up front that it has been tuned.
     *
     * <p>Twelve of {@code ForesightConfig}'s variables are declared {@code ConfigVar.required(…)}
     * with no default: {@code headingFeedback}, {@code forwardTranslational},
     * {@code strafeTranslational}, {@code brake}, {@code coast}, the linear, quadratic and heading
     * brake coefficients, both max achievable velocities, and both natural decelerations. Those
     * twelve are exactly what the Foresight Tuner measures — properties of the robot's mass, wheels
     * and battery — so there is no sensible default and nothing should be guessed.
     *
     * <p>Without this check the first unset variable surfaces as a bare
     * {@code IllegalStateException("Config variable has not been set")} partway through following a
     * path — no field name, no robot, no hint. The tuner writes all twelve required values in one
     * block, so probing one of them is enough to tell "never tuned" from "tuned".
     */
    public static Foresight createAlgorithm(RobotIdentity identity) {
        RobotConstants robot = forRobot(identity);
        try {
            robot.foresightConfig.maxAchievableForwardVelocity.get();
        } catch (IllegalStateException e) {
            throw new IllegalStateException(
                    "Foresight has not been tuned for " + identity + ". Run the Foresight Tuner in "
                            + "AutoTune on that robot and paste its generated block over "
                            + "foresightConfig in " + robot.file + ".", e);
        }
        return new Foresight(robot.foresightConfig);
    }

    public static Follower create(HardwareMap hardwareMap) {
        RobotIdentity identity = ActiveConfig.requireIdentity();
        RobotConstants robot = forRobot(identity);
        return new Follower(
                new PinpointLocalizer(hardwareMap, robot.localizerConfig),
                new Mecanum(hardwareMap, robot.drivetrainConfig),
                createAlgorithm(identity));
    }
}
