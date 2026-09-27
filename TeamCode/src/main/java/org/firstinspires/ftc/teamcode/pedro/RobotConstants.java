package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.PinpointConfig;

import org.firstinspires.ftc.teamcode.hardware.DeviceNames;

/**
 * One robot's three Pedro config blocks, plus the file they live in.
 *
 * <p>Each file in {@code pedro/robots/} builds one of these from its own static fields. It exists
 * so {@link Constants} can pick a robot's values without the robot files having to share a base
 * class or interface — they stay three plain {@code public static} fields in exactly the shape the
 * tuners emit, which is what makes "paste the tuner's block over the matching block" work.
 *
 * <p><b>Device names are set here, not in the robot files.</b> The Mecanum and Pinpoint Tuners'
 * generated blocks include {@code c.frontLeftName.set("frontLeft")}-style lines with string
 * literals. Both robots have the same names, so a per-robot copy of those lines is a place for one
 * robot's names to drift from the other's and from the {@code res/xml} configs. The constructor
 * overwrites every name from {@link DeviceNames}, so whatever a robot file says, the names are the
 * shared ones — and {@code PedroRobotsTest} fails the build if a pasted name line is left in a
 * robot file, so the file doesn't claim something that isn't true.
 */
public final class RobotConstants {

    /** Repo-relative path of the robot file, for error messages that say where to paste. */
    public final String file;

    public final MecanumConfig drivetrainConfig;
    public final PinpointConfig localizerConfig;
    public final ForesightConfig foresightConfig;

    /**
     * @param robotFile the class holding the values, e.g. {@code Robot19429.class}. Taking the
     *     class rather than a string means copying a robot file in Android Studio renames it along
     *     with everything else, so the "paste into" message cannot point at the file it was copied
     *     from.
     */
    public RobotConstants(Class<?> robotFile, MecanumConfig drivetrainConfig,
                          PinpointConfig localizerConfig, ForesightConfig foresightConfig) {
        this.file = "TeamCode/src/main/java/org/firstinspires/ftc/teamcode/pedro/robots/"
                + robotFile.getSimpleName() + ".java";
        this.drivetrainConfig = drivetrainConfig;
        this.localizerConfig = localizerConfig;
        this.foresightConfig = foresightConfig;

        drivetrainConfig.frontLeftName.set(DeviceNames.FRONT_LEFT);
        drivetrainConfig.frontRightName.set(DeviceNames.FRONT_RIGHT);
        drivetrainConfig.backLeftName.set(DeviceNames.BACK_LEFT);
        drivetrainConfig.backRightName.set(DeviceNames.BACK_RIGHT);
        localizerConfig.name.set(DeviceNames.PINPOINT);
    }

    /** The robot file's name without its directory, e.g. {@code Robot19429.java}. */
    public String fileName() {
        return file.substring(file.lastIndexOf('/') + 1);
    }
}
