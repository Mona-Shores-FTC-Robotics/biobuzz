package org.firstinspires.ftc.teamcode.localization;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.util.Alliance;

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
     * audience, back flat against the audience wall, left wheels on seam W</b> (between columns B
     * and C; tile and seam names are the manual's, §9.4 Figures 9-4 and 9-5).
     * Measured on 20245, 24 Oct 2026: robot centre 55 in from the red-side wall (tape), 9.7 in
     * from the audience wall (the camera's fit, with the back on the wall), facing straight up the
     * field. From here the camera sees the red AUDIENCE CELL about 49 in away.
     */
    public static final StartPosition RED_AUDIENCE =
            new StartPosition("Red audience", Alliance.RED, new Pose(55.0, 9.7, Math.toRadians(90)));
}
