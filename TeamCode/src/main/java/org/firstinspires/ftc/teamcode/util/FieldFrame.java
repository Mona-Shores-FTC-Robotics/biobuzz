package org.firstinspires.ftc.teamcode.util;

/**
 * The field frame every pose in this codebase uses: Pedro's — origin at a field corner, x and y in
 * inches across the 144 in field, heading in radians counter-clockwise. Degrees appear only on
 * screens.
 *
 * <p>Facts about the BIOBUZZ field that depend on that frame live here, in one place, so a wrong
 * one is fixed once.
 */
public final class FieldFrame {

    private FieldFrame() {
    }

    /**
     * The field heading that means "away from me" for a driver standing at {@code alliance}'s
     * station, in radians, or NaN if not known.
     *
     * <p><b>Both NaN until measured.</b> Red and blue drivers stand on opposite sides, so this is
     * what makes field-centric "push away, robot goes away" true for both — but which Pedro heading
     * that is depends on where the stations sit in Pedro's frame for BIOBUZZ, and that is checked on
     * a field, not guessed. To measure: at each station, square the robot to face directly away from
     * the drivers and read its handed-over heading. While NaN, TeleOp falls back to "forward is
     * the way the robot faces at PLAY", and says so.
     */
    public static double driverForwardHeading(Alliance alliance) {
        switch (alliance) {
            case RED:  return Double.NaN;
            case BLUE: return Double.NaN;
            default:   return Double.NaN;
        }
    }
}
