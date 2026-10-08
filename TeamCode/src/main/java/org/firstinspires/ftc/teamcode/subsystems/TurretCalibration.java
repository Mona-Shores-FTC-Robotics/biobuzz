package org.firstinspires.ftc.teamcode.subsystems;

import org.firstinspires.ftc.teamcode.hardware.RobotIdentity;

/**
 * Turns the turret encoder's raw reading into the turret's angle, using one robot's measured
 * values. Plain Java on purpose, so all of it is unit tested at a laptop.
 *
 * <h2>The encoder</h2>
 *
 * <p>A REV Thru-Bore encoder's absolute output is a pulse 1–1024 µs wide, once per revolution, read
 * by the OctoQuad as a width in microseconds: {@code 360 / 1024} degrees per µs, the constant the
 * SDK's {@code SensorOctoQuadAdv} sample uses. It is driven <b>exactly 1:1</b> with the turret
 * (#170), so one encoder revolution is one turret revolution, and the reading is the turret's true
 * angle on every turn, however many times it has gone round. That is why nothing here counts turns,
 * and why the OctoQuad's wrap tracking is off: it only counts turns while powered, and nothing
 * needs it.
 *
 * <h2>The measured values</h2>
 *
 * <p>Two per robot, both NaN or zero until measured at a meeting, never guessed (#169's checklist):
 *
 * <table>
 *   <caption>Per-robot values</caption>
 *   <tr><th>Value</th><th>How to measure it</th></tr>
 *   <tr><td>{@code HOME_RAW_DEG_*}</td><td>Turret at home (its angle in the starting
 *       configuration). Read "raw" on the Robot page and write it here. Re-zeroing after the encoder
 *       slips on its shaft is the same step.</td></tr>
 *   <tr><td>{@code DIRECTION_*}</td><td>Turn the turret counter-clockwise, seen from above. +1 if
 *       "raw" goes up, -1 if it goes down.</td></tr>
 * </table>
 *
 * <p>{@link #HOME_TOLERANCE_DEG} is shared: how far from home still counts as home. Set it from the
 * spread of readings when the turret is put at home by eye a few times.
 *
 * <p>Until a robot's values are in, its turret health is {@link TurretHealth#UNCALIBRATED}, which
 * says so on the Driver Station. Nothing falls back to a default.
 *
 * <p>These live here, not in {@code pedro/robots/}, because those files hold exactly what Pedro's
 * tuners emit, in the shape they emit it. Keyed by {@link RobotIdentity} like every per-robot number.
 */
public final class TurretCalibration {

    // ---------------------------------------------------------------- measured values

    /** 19429: raw encoder angle with the turret at home, degrees [0, 360). Not measured yet. */
    static final double HOME_RAW_DEG_19429 = Double.NaN;
    /** 19429: +1 if raw increases as the turret turns CCW seen from above, -1 if not. 0 = not measured. */
    static final int DIRECTION_19429 = 0;

    /** 20245: raw encoder angle with the turret at home, degrees [0, 360). Not measured yet. */
    static final double HOME_RAW_DEG_20245 = Double.NaN;
    /** 20245: +1 if raw increases as the turret turns CCW seen from above, -1 if not. 0 = not measured. */
    static final int DIRECTION_20245 = 0;

    /** Both robots: how far from home, in degrees, still reads as home. Not measured yet. */
    public static final double HOME_TOLERANCE_DEG = Double.NaN;

    // ---------------------------------------------------------------- encoder constants

    /** REV Thru-Bore absolute output: the narrowest and widest pulse, in µs. */
    public static final int PULSE_MIN_US = 1;
    public static final int PULSE_MAX_US = 1024;

    private static final double DEGREES_PER_US = 360.0 / 1024.0;

    // ---------------------------------------------------------------- one robot's values

    /** Raw encoder angle with the turret at home, degrees [0, 360); NaN until measured. */
    public final double homeRawDeg;

    /** +1 or -1: which way raw moves as the turret turns CCW. 0 until measured. */
    public final int direction;

    TurretCalibration(double homeRawDeg, int direction) {
        this.homeRawDeg = homeRawDeg;
        this.direction = direction;
    }

    /**
     * The measured values for {@code robot}. A configuration with no turret (the launcher rig) gets
     * unmeasured values, so it reads as uncalibrated rather than borrowing a robot's numbers.
     */
    public static TurretCalibration forRobot(RobotIdentity robot) {
        switch (robot) {
            case TEAM_19429:
                return new TurretCalibration(HOME_RAW_DEG_19429, DIRECTION_19429);
            case TEAM_20245:
                return new TurretCalibration(HOME_RAW_DEG_20245, DIRECTION_20245);
            default:
                return new TurretCalibration(Double.NaN, 0);
        }
    }

    /** True once both the home offset and the direction have been measured. */
    public boolean calibrated() {
        return !Double.isNaN(homeRawDeg) && (direction == 1 || direction == -1);
    }

    /**
     * The encoder's own angle from a pulse width, degrees [0, 360), or NaN if the width is not one a
     * REV Thru-Bore produces. A missing pulse reads as a width outside the range, which is how an
     * unplugged encoder shows up.
     */
    public static double rawDegrees(int pulseUs) {
        if (pulseUs < PULSE_MIN_US || pulseUs > PULSE_MAX_US) {
            return Double.NaN;
        }
        return (pulseUs * DEGREES_PER_US) % 360.0;
    }

    /**
     * The turret's angle from forward (home), degrees in (-180, 180], CCW positive (Pedro's frame).
     * NaN if {@code rawDeg} is NaN or this robot is not calibrated.
     */
    public double turretDegrees(double rawDeg) {
        if (Double.isNaN(rawDeg) || !calibrated()) {
            return Double.NaN;
        }
        return wrap180(direction * (rawDeg - homeRawDeg));
    }

    /** {@code deg} wrapped into (-180, 180]. The turret has no stops, so this is the only limit. */
    public static double wrap180(double deg) {
        double wrapped = deg % 360.0;
        if (wrapped <= -180.0) {
            wrapped += 360.0;
        } else if (wrapped > 180.0) {
            wrapped -= 360.0;
        }
        return wrapped;
    }
}
