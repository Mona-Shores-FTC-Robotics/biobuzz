package org.firstinspires.ftc.teamcode.subsystems;

/**
 * What the turret's angle sensor says before a match, read with nothing moving.
 *
 * <p>There is no homing: at 1:1 the absolute encoder reads the true angle wherever the turret is.
 * The one thing it cannot catch by itself is the encoder slipping on its shaft, which moves the zero
 * without a word. At the start of a match the starting configuration puts the turret at home, so
 * that is the moment to compare: a reading at home means the zero is still right.
 *
 * <p>Smart Auto and Backup use this to choose between "turret aims" and "turret locked forward, the
 * robot turns to aim". It is the INIT check, not an in-match limit; the turret rotates continuously
 * and has none.
 */
public enum TurretHealth {

    /** Signal present, and the angle is within {@link TurretCalibration#HOME_TOLERANCE_DEG} of home. */
    HOME("at home"),

    /**
     * Signal present, but off home: the turret was left turned, or the encoder slipped on its
     * shaft. Turn it home; if it still reads off, re-zero. The angle is
     * {@code TurretSubsystem.angleDegrees()}.
     */
    NOT_HOME("not at home — turn it home; if it still reads off, re-zero"),

    /**
     * Signal present, but this robot's home offset, direction or the home tolerance has not been
     * measured, so "at home" cannot be judged. The raw reading is still shown, for measuring.
     */
    UNCALIBRATED("not calibrated — measure home and direction (TurretCalibration)"),

    /** The OctoQuad or the encoder channel is missing, not answering, or giving no valid pulse. */
    NO_SIGNAL("no signal");

    /** Short text for the Driver Station. */
    public final String description;

    TurretHealth(String description) {
        this.description = description;
    }

    /** True for every state except {@link #NO_SIGNAL}: the encoder is giving a valid reading. */
    public boolean hasSignal() {
        return this != NO_SIGNAL;
    }

    /**
     * Classifies one reading.
     *
     * @param rawDeg the encoder's own angle, from {@link TurretCalibration#rawDegrees}; NaN for no
     *     valid reading
     * @param calibration this robot's measured values
     * @param toleranceDeg how far from home still counts as home; NaN until measured
     */
    public static TurretHealth classify(double rawDeg, TurretCalibration calibration,
                                        double toleranceDeg) {
        if (Double.isNaN(rawDeg)) {
            return NO_SIGNAL;
        }
        if (!calibration.calibrated() || Double.isNaN(toleranceDeg)) {
            return UNCALIBRATED;
        }
        return Math.abs(calibration.turretDegrees(rawDeg)) <= toleranceDeg ? HOME : NOT_HOME;
    }
}
