package org.firstinspires.ftc.teamcode.hardware;

import com.qualcomm.ftccommon.configuration.RobotConfigFile;
import com.qualcomm.ftccommon.configuration.RobotConfigFileManager;

import org.firstinspires.ftc.robotcore.internal.network.DeviceNameManagerFactory;

/**
 * Reads which Driver Station configuration is currently active, and turns that
 * into a {@link RobotIdentity}.
 *
 * <p>Split out from {@link RobotIdentity} so that enum stays free of Android
 * types and can be unit tested.
 *
 * <p>{@code new RobotConfigFileManager()} passes a {@code null} Activity to the
 * real constructor, and {@code getActiveConfig()} reads only {@code context}
 * and {@code SharedPreferences} — verified against the FtcCommon 12.0.0
 * bytecode — so this is safe to call from an OpMode with no Activity in hand.
 */
public final class ActiveConfig {

    private ActiveConfig() {}

    /** The active configuration, or {@code null} if none is selected. */
    public static RobotConfigFile file() {
        RobotConfigFile active = new RobotConfigFileManager().getActiveConfig();
        return active == null || active.isNoConfig() ? null : active;
    }

    /** Name of the active configuration, or {@code null} if none is selected. */
    public static String name() {
        RobotConfigFile active = file();
        return active == null ? null : active.getName();
    }

    /**
     * True when the active configuration is one bundled in the APK rather than
     * a file on the Robot Controller's storage.
     *
     * <p>Worth surfacing: a {@code LOCAL_STORAGE} configuration is editable in
     * the DS editor, so it can silently drift from this repository. A bundled
     * one cannot. If someone hand-made a configuration, this is how you find
     * out.
     */
    public static boolean isBundled() {
        RobotConfigFile active = file();
        return active != null && active.getLocation() == RobotConfigFile.FileLocation.RESOURCE;
    }

    /**
     * The robot this code is running on, according to the active
     * configuration — or {@code null} if no configuration is active or it is
     * not one of ours.
     *
     * <p>Never throws, and never guesses. {@link HardwareCheck} reports a
     * {@code null} here by name at init. An earlier version of this class
     * offered {@code requireIdentity()}, which threw instead; it was removed
     * before anything called it, because "the robot refuses to start" is the
     * outcome the team most wants to avoid at a competition, and because it
     * would have thrown on a Driver Station "Save As" copy made to work
     * around a dead port.
     */
    public static RobotIdentity identity() {
        return RobotIdentity.fromConfigName(name());
    }

    /**
     * The Control Hub's device name (also its Wi-Fi network name), e.g.
     * {@code "19429-RC"}, or {@code null} if it cannot be read.
     *
     * <p>Read through the SDK's own {@code DeviceNameManager}, which the Robot
     * Controller app starts before any OpMode can run. This is <em>not</em>
     * last season's approach: DECODE reached the Wi-Fi access-point
     * configuration through a hidden Android API by reflection, which was not
     * ready on a cold boot and needed a retry loop.
     */
    public static String deviceName() {
        try {
            return DeviceNameManagerFactory.getInstance().getDeviceName();
        } catch (RuntimeException | LinkageError e) {
            return null;
        }
    }

    static String knownConfigNames() {
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
