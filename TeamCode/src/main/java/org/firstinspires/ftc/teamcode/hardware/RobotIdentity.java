package org.firstinspires.ftc.teamcode.hardware;

/**
 * Which physical robot the code is running on.
 *
 * <p>Identity comes from the name of the <em>active Driver Station
 * configuration</em> — see {@link ActiveConfig}. Somebody already has to pick a
 * configuration once per robot (an APK cannot set the active one; it is a
 * SharedPreferences value on the Robot Controller). Deriving identity from that
 * pick makes the one irreducible manual step do double duty, and means the
 * wiring and the per-robot constants can never disagree about which robot this
 * is.
 *
 * <p>Rejected alternatives, and why:
 * <ul>
 *   <li><b>Control Hub WiFi SSID</b> — what last season used. It needs a hidden
 *       Android API via reflection, and it resolves <em>after</em> the class
 *       load that needs it, which is what forced that project's
 *       {@code RobotProfile.invalidate()} plus a hand-maintained list of
 *       {@code reloadProfileConfigs()} calls. Forget to extend that list and
 *       you silently get stale values.</li>
 *   <li><b>Hub serial number</b> — stable, but opaque. Swapping a hub turns
 *       into hunting for a hex string.</li>
 * </ul>
 *
 * <p>This enum deliberately has no Android dependencies so it stays unit
 * testable. Everything that touches the SDK lives in {@link ActiveConfig}.
 *
 * <p><b>Adding a robot is two edits:</b> a constant here, and a matching
 * {@code res/xml/robot_<name>.xml}. {@code RobotConfigXmlTest} asserts the two
 * sets match exactly, so a file with no constant (or a constant with no file)
 * fails the build. At that price there is no reason to cap the number of
 * robots — a spare chassis or last season's bot used as a test mule costs one
 * file each.
 */
public enum RobotIdentity {

    TEAM_19429("robot_19429", "19429"),
    TEAM_20245("robot_20245", "20245");

    /**
     * The bundled configuration's name, which is also its resource entry name,
     * which is also its filename without {@code .xml}.
     *
     * <p>Exactly one string identifies a robot, and it is visible in the repo.
     * The {@code <Robot>} elements deliberately carry no {@code name}
     * attribute: the SDK would prefer that attribute over the resource entry
     * name ({@code RobotConfigFileManager.getXMLFiles} passes
     * {@code getResourceEntryName(id)} as the fallback to
     * {@code RobotConfigResFilter.getRootAttribute}), which would create a
     * second identity string free to disagree with the filename — a new drift
     * surface in a change whose whole point is closing them.
     */
    public final String configName;

    /**
     * The team number, which each Control Hub's device name (and Wi-Fi network
     * name) contains — e.g. {@code 19429-RC}. Used only to cross-check the
     * active configuration; see {@link #fromDeviceName(String)}.
     */
    public final String teamNumber;

    RobotIdentity(String configName, String teamNumber) {
        this.configName = configName;
        this.teamNumber = teamNumber;
    }

    /**
     * Resolves a configuration name to a robot, or returns {@code null} if the
     * name is not one of ours.
     *
     * <p>Matches by <b>prefix</b>, so {@code "robot_19429 fix"} is still 19429.
     * That is the event workflow: a bundled config is read-only on the Driver
     * Station, and the SDK's answer is "edit it, then save it under a new
     * name". The copy must keep meaning the same robot. {@link HardwareCheck}
     * separately warns that such a copy is not the bundled file.
     *
     * <p>Returns {@code null} rather than falling back to a default on purpose.
     * Last season had four disagreeing fallbacks — an initial {@code "UNKNOWN"},
     * a {@code setRobotName(null)} that became 19429, a log line announcing
     * 19429, and a profile builder that actually used 20245 — so a robot could
     * run one robot's tuning while telling you it was using the other's. A
     * {@code null} here is reported by {@link HardwareCheck}; it never guesses.
     */
    public static RobotIdentity fromConfigName(String configName) {
        if (configName == null) {
            return null;
        }
        for (RobotIdentity identity : values()) {
            if (configName.startsWith(identity.configName)) {
                return identity;
            }
        }
        return null;
    }

    /**
     * Resolves a Control Hub device name (e.g. {@code "19429-RC"}) to a robot,
     * or returns {@code null} if it contains no known team number, or more than
     * one.
     *
     * <p>This is a cross-check, not the identity. It exists to catch the one
     * mistake the active config cannot see about itself: 20245's configuration
     * selected on 19429's Control Hub.
     */
    public static RobotIdentity fromDeviceName(String deviceName) {
        if (deviceName == null) {
            return null;
        }
        RobotIdentity match = null;
        for (RobotIdentity identity : values()) {
            if (deviceName.contains(identity.teamNumber)) {
                if (match != null) {
                    return null;
                }
                match = identity;
            }
        }
        return match;
    }
}
