package org.firstinspires.ftc.teamcode.hardware;

import com.qualcomm.ftccommon.configuration.RobotConfigFile;
import com.qualcomm.ftccommon.configuration.RobotConfigFileManager;

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
     * The robot this code is running on.
     *
     * @throws IllegalStateException if no configuration is active, or the
     *     active one is not one of this project's. Deliberately loud: a silent
     *     default is how last season's project ran one robot's tuning while
     *     reporting the other's.
     */
    public static RobotIdentity requireIdentity() {
        String activeName = name();
        if (activeName == null) {
            throw new IllegalStateException(
                    "No Driver Station configuration is active. On the Driver Station: "
                            + "Configure Robot -> select " + knownConfigNames() + " -> Activate.");
        }
        RobotIdentity identity = RobotIdentity.fromConfigName(activeName);
        if (identity == null) {
            throw new IllegalStateException(
                    "Active Driver Station configuration is \"" + activeName + "\", which is not "
                            + "one of this project's robots (" + knownConfigNames() + "). Either "
                            + "activate one of those on the Driver Station, or add a "
                            + "res/xml/<name>.xml and a matching RobotIdentity constant. "
                            + "Run the \"Validate Hardware\" OpMode for details.");
        }
        return identity;
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
