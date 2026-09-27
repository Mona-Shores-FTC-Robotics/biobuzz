package org.firstinspires.ftc.teamcode.logging;

/**
 * Log keys AdvantageScope gives special meaning to. Spelled exactly as AdvantageScope looks for
 * them ({@code src/shared/log/LogUtil.ts}, v27.0.0-alpha-6), so its timeline, Joysticks and
 * Metadata tabs work without configuration.
 *
 * <p>Match state is written in both the pre-2027 form (booleans) and the 2027 form (a
 * {@code RobotMode} string); v27 reads either, v26 reads only the first.
 */
public final class AdvantageScopeKeys {

    private AdvantageScopeKeys() {
    }

    /** {@code boolean}. The timeline shades enabled time and snaps the cursor to its edges. */
    public static final String ENABLED = "/DriverStation/Enabled";
    /** {@code boolean}. Pre-2027 form of the robot mode. */
    public static final String AUTONOMOUS = "/DriverStation/Autonomous";
    /** {@code string}: {@code "autonomous"}, {@code "teleop"} or {@code "disabled"}. 2027 form. */
    public static final String ROBOT_MODE = "/DriverStation/RobotMode";
    /** {@code int64}: 1–3 red, 4–6 blue, 0 unknown (AdvantageKit's encoding). */
    public static final String ALLIANCE_STATION = "/DriverStation/AllianceStation";
    public static final String MATCH_NUMBER = "/DriverStation/MatchNumber";

    /** Gamepad {@code n} is logged under this prefix followed by {@code n}; see {@link GamepadLog}. */
    public static final String JOYSTICK_PREFIX = "/DriverStation/Joystick";

    /** Strings under here appear on the Metadata tab. */
    public static final String METADATA_PREFIX = "/RealMetadata/";

    /** Any {@code string} entry can be shown on the Console tab; this is ours. */
    public static final String EVENTS = "/Events";

    public static long allianceStation(boolean red, int station) {
        return red ? station : 3 + station;
    }
}
