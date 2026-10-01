package org.firstinspires.ftc.teamcode.util;

/**
 * The field frame every pose in this codebase uses: Pedro's — origin at a field corner, x and y in
 * inches across the {@value #FIELD_SIZE_INCHES} in field, heading in radians counter-clockwise.
 * Degrees appear only on screens.
 *
 * <p><b>This is the only frame a person reads or types.</b> A library that works in another frame
 * (Panels' centre-origin canvas, the Limelight's camera space) is converted inside the one class
 * that talks to it, and nothing downstream ever sees the other frame.
 *
 * <p>Facts about the BIOBUZZ field that depend on that frame live here, in one place, so a wrong
 * one is fixed once.
 */
public final class FieldFrame {

    /**
     * The playing field's side, wall face to wall face, in inches.
     *
     * <p>Not 144. The nominal 12 ft ignores the thickness of the perimeter wall; last season showed
     * 141.5 in matches the real field, and it is what our Visualizer fork draws and mirrors with.
     * (A tape measure on our practice field is the check, if it is ever in doubt.) A second
     * number here is how last season's frame bugs started, so nothing else in the code states the
     * field's size: {@code FieldFrameTest} fails the build if one appears.
     */
    public static final double FIELD_SIZE_INCHES = 141.5;

    /**
     * Field centre on either axis, in inches — also the line the two alliances mirror across.
     *
     * <p>The Visualizer's exported Autos mirror with {@code PoseFactory.mirrorX(70.75)}; robot code
     * that mirrors writes {@code mirrorX(FieldFrame.FIELD_CENTRE_INCHES)}, never {@code 144 - x}.
     */
    public static final double FIELD_CENTRE_INCHES = FIELD_SIZE_INCHES / 2.0;

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
