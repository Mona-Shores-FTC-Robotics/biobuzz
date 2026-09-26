package org.firstinspires.ftc.teamcode.hardware;

import java.util.List;

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
 * <p><b>Adding a robot is two edits:</b> a constant here, naming the
 * {@link DeviceNames} list it carries, and a matching
 * {@code res/xml/robot_<name>.xml}. {@code RobotConfigXmlTest} asserts the two
 * sets match exactly, so a file with no constant (or a constant with no file)
 * fails the build. At that price there is no reason to cap the number of
 * robots — a spare chassis or last season's bot used as a test mule costs one
 * file each.
 */
public enum RobotIdentity {

    TEAM_19429("robot_19429", DeviceNames.COMPETITION_ROBOT),
    TEAM_20245("robot_20245", DeviceNames.COMPETITION_ROBOT),

    /**
     * Not a robot: the two-wheel pinch launcher bench rig, on its own Control
     * Hub, FTC-EoM3. It has an identity so its configuration gets the same
     * build-time checks as the robots' do.
     */
    LAUNCHER_RIG("robot_launcher_rig", DeviceNames.LAUNCHER_RIG);

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
     * Every device this configuration must declare, and may declare nothing
     * beyond. {@code RobotConfigXmlTest} holds the XML to it; {@code
     * ValidateHardware} holds the live hardware to it.
     */
    public final List<DeviceNames.Device> devices;

    RobotIdentity(String configName, List<DeviceNames.Device> devices) {
        this.configName = configName;
        this.devices = devices;
    }

    /**
     * Resolves a configuration name to a robot, or returns {@code null} if the
     * name is not one of ours.
     *
     * <p>Returns {@code null} rather than falling back to a default on purpose.
     * Last season had four disagreeing fallbacks — an initial {@code "UNKNOWN"},
     * a {@code setRobotName(null)} that became 19429, a log line announcing
     * 19429, and a profile builder that actually used 20245 — so a robot could
     * run one robot's tuning while telling you it was using the other's.
     * Callers that need a robot should use {@link ActiveConfig#requireIdentity()},
     * which fails loudly instead.
     */
    public static RobotIdentity fromConfigName(String configName) {
        for (RobotIdentity identity : values()) {
            if (identity.configName.equals(configName)) {
                return identity;
            }
        }
        return null;
    }
}
