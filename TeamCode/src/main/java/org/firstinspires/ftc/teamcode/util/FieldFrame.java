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
     * 141.5 in matches the real field, and it is what our Visualizer fork draws and rotates on.
     * (A tape measure on our practice field is the check, if it is ever in doubt.) A second
     * number here is how last season's frame bugs started, so nothing else in the code states the
     * field's size: {@code FieldFrameTest} fails the build if one appears.
     */
    public static final double FIELD_SIZE_INCHES = 141.5;

    /**
     * Field centre on either axis, in inches — also the point the two alliances turn about.
     *
     * <p>BIOBUZZ is rotationally symmetric, not mirrored: blue's side is red's turned half a turn
     * about the field centre (manual §9.3, Figure 9-2: red's LOADING ZONE and blue's sit in
     * diagonally opposite corners, as do the GARDENs). A pose on the other alliance is
     * {@code (2c - x, 2c - y, heading + pi)}, c being this. The Visualizer's exported Autos do it
     * with {@code PoseFactory.mirrorAroundPoint(70.75, 70.75)} (a reflection through a point is a
     * half turn); robot code writes {@code FieldFrame.FIELD_CENTRE_INCHES}, never {@code 144 - x}.
     */
    public static final double FIELD_CENTRE_INCHES = FIELD_SIZE_INCHES / 2.0;

    /** One tile's side, inches: the field is six tiles across, wall face to wall face. */
    public static final double TILE_INCHES = FIELD_SIZE_INCHES / 6.0;

    private FieldFrame() {
    }

    /**
     * The manual's name for the tile under {@code (x, y)} (§9.4, Figure 9-5), so a pose can be read
     * the way a drive team places a robot: column A–F across from the red wall (left to right as the
     * audience sees the field), row 1–6 up from the audience wall. The red audience start is "C1".
     * Null off the field. The seams between columns are V (A|B), W (B|C), X, Y, Z (Figure 9-4).
     */
    public static String tileName(double x, double y) {
        int column = (int) Math.floor(x / TILE_INCHES);
        int row = (int) Math.floor(y / TILE_INCHES);
        if (x < 0 || y < 0 || column > 5 || row > 5) return null;
        return String.valueOf((char) ('A' + column)) + (row + 1);
    }

    /**
     * Which way {@code headingRad} faces, in field words, to the nearest quarter turn: +x is toward
     * the blue wall, +y away from the audience.
     */
    public static String facing(double headingRad) {
        int quarter = (int) Math.floorMod(Math.round(headingRad / (Math.PI / 2)), 4L);
        switch (quarter) {
            case 0:  return "toward the blue wall";
            case 1:  return "away from the audience";
            case 2:  return "toward the red wall";
            default: return "toward the audience";
        }
    }

    /**
     * The field heading that means "away from me" for a driver standing at {@code alliance}'s
     * station, in radians, or NaN if not known.
     *
     * <p><b>Red measured, blue not yet.</b> Red and blue drivers stand on opposite sides, so this is
     * what makes field-centric "push away, robot goes away" true for both — but which Pedro heading
     * that is depends on where the stations sit in Pedro's frame for BIOBUZZ, and that is checked on
     * a field, not guessed. To measure: at each station, square the robot to face directly away from
     * the drivers and read its handed-over heading. While NaN, TeleOp falls back to "forward is
     * the way the robot faces at PLAY", and says so.
     *
     * <p>Red: 0 (field +x). Measured on 20245, 24 Oct 2026: the red station is on the red-side
     * wall, and a robot squared to face away from it read a heading of 0 on the Pose section.
     * Blue is expected to be the half turn of red, pi, but it stays NaN until someone stands at the blue
     * station and reads it.
     */
    public static double driverForwardHeading(Alliance alliance) {
        switch (alliance) {
            case RED:  return 0.0;
            case BLUE: return Double.NaN;
            default:   return Double.NaN;
        }
    }
}
