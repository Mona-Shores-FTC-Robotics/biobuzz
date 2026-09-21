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

    TEAM_19429("robot_19429"),
    TEAM_20245("robot_20245");

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

    RobotIdentity(String configName) {
        this.configName = configName;
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
