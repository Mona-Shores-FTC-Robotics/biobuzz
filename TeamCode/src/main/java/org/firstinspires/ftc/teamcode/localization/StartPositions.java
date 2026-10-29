package org.firstinspires.ftc.teamcode.localization;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.FieldFrame;

/**
 * Every legal BIOBUZZ starting position, in one place, so each Autonomous names one rather than
 * typing coordinates.
 *
 * <p>The game manual (G304) says only that a robot starts fully on its own alliance's side,
 * touching the field perimeter wall, out of the LOADING ZONE and off any FLOWER, in contact with
 * its four pre-loaded POLLEN. So a start position is a team choice, and what makes it worth
 * writing down is that the drive team can repeat it: a wall to push against and a tile seam to
 * line a wheel up with. Each pose comes from placing the robot that way and reading it off the
 * field, never from a drawing.
 *
 * <p>Each also needs its driver forward heading in {@code FieldFrame}, measured the same day.
 */
public final class StartPositions {

    private StartPositions() {
    }

    /**
     * Red, against the audience wall, under the red HIVE: <b>tile C1, facing away from the
     * audience, back flat against the audience wall, the robot's left side on seam W</b> (between
     * columns B and C; tile and seam names are the manual's, §9.4 Figures 9-4 and 9-5).
     * Derived from the robot's size, not from the camera:
     * <ul>
     *   <li>x 55.17 = seam W, 2 tiles from the red wall (47.17), + 8.0 (half the robot's 16 in
     *       width, side plate to side plate). The left wheels' outer face is 6.89 in from the centre
     *       (CAD, 29 Oct 2026: half of 12.277 in bearing to bearing, + 0.754), so they sit 1.1 in
     *       right of the seam. The side plates are not in the CAD on the left: 16 in is the build
     *       team's width, to be measured (doc/robot-session-3.md). The seam is the line through the
     *       middle of the tiles' interlocking teeth.</li>
     *   <li>y 7.72 = half of 15.433 in, back face to front face over the wheels; the back plate and
     *       the rear wheels are flush, so either one on the wall puts the centre here.</li>
     * </ul>
     * The centre is taken as the middle of the wheelbase, the point the Pinpoint's pod offsets are
     * measured from. It replaced (55, 9.7): 55 was taped, 9.7 was the camera's own fit, and the
     * camera had been reading about 2 in up the field from this. After that it was briefly (54.06, 7.72),
     * the left wheels on the seam rather than the left side. From here the camera sees the red
     * AUDIENCE CELL about 49 in away.
     */
    public static final StartPosition RED_AUDIENCE =
            new StartPosition("Red audience", Alliance.RED, new Pose(55.17, 7.72, Math.toRadians(90)));

    /**
     * {@link #RED_AUDIENCE} turned half a turn about the field centre, the way the field is (manual
     * Figure 9-2) and the way every Auto runs for blue: tile D6, back on the far wall, facing the
     * audience. Not measured on its own; the Autos already start blue here.
     */
    public static final StartPosition BLUE_FROM_RED_AUDIENCE = new StartPosition("Blue far wall",
            Alliance.BLUE, new Pose(2 * FieldFrame.FIELD_CENTRE_INCHES - RED_AUDIENCE.pose.x(),
            2 * FieldFrame.FIELD_CENTRE_INCHES - RED_AUDIENCE.pose.y(),
            RED_AUDIENCE.pose.heading() + Math.PI));

    /** {@code alliance}'s start, the Autos' one: red audience, or its half turn for blue. Red while unknown. */
    public static StartPosition forAlliance(Alliance alliance) {
        return alliance == Alliance.BLUE ? BLUE_FROM_RED_AUDIENCE : RED_AUDIENCE;
    }

    /** Every declared start, for finding one by its pose. */
    private static final StartPosition[] ALL = {RED_AUDIENCE};

    /**
     * The declared start at {@code pose} (within an inch and a degree), or null: lets an Auto drawn
     * from a known start show that start's name.
     */
    public static StartPosition at(Pose pose) {
        for (StartPosition start : ALL) {
            if (Math.hypot(start.pose.x() - pose.x(), start.pose.y() - pose.y()) <= 1.0
                    && Math.abs(Math.IEEEremainder(start.pose.heading() - pose.heading(), 2 * Math.PI))
                            <= Math.toRadians(1.0)) {
                return start;
            }
        }
        return null;
    }
}
