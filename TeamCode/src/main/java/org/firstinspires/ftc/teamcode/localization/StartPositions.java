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

    /*
     * The rule both starts follow, so a drive team places the robot the same way at either: <b>back
     * flat against the wall, the side of the frame on the robot's left on the near tips of a tile
     * seam's teeth</b>, on the seam's right-hand side as the robot faces. The frame is 16 in wide,
     * centred, 1.1 in outside the wheels on each side, so it is the frame, not the wheels, that
     * meets the seam; and at the left start, whose seam is the centre line, nothing crosses it.
     * Tile and seam names are the manual's (§9.4 Figures 9-4 and 9-5). The centre is the middle of
     * the wheelbase, the point the Pinpoint's pod offsets are measured from: 7.7 in from the back
     * (the rear wheels are flush with the back; 15.43 in over the wheels, CAD 29 Oct 2026).
     * Set on 31 Oct 2026 with the robot on the field and in the Visualizer (the team spots
     * {@code START_RIGHT} and {@code START_LEFT}, rounded to 0.1 in): x is the seam (B-C 47.17,
     * C-D 70.75) plus or minus about 0.4 in of tooth and 8 in of half frame.
     */

    /**
     * Red, against the audience wall, under the red HIVE: <b>tile C1, facing away from the
     * audience, back flat against the audience wall, the frame's left side on the B-C seam</b>.
     * x 55.6 = 47.17 + 0.4 + 8. Before 31 Oct the left wheels went on the seam, at 54.06, which
     * the camera confirmed four times (54.0 to 54.5); the frame then overhung the seam.
     */
    public static final StartPosition RED_AUDIENCE =
            new StartPosition("Red audience", Alliance.RED, new Pose(55.6, 7.7, Math.toRadians(90)));

    /**
     * Red, against the far wall, the left-side Autos' start (L-Simple, L-Quals, L-Launch-Park,
     * Sister-Left): <b>tile C6, facing the audience, back flat against the far wall, the frame's
     * left side on the C-D seam</b>, the centre line, so the frame reaches 70.75 and no further.
     * x 62.3 = 70.75 - 0.4 - 8. Turned half a turn for blue, (79.2, 7.7, 90°): from there the
     * camera sees three or four of the red HIVE's tags.
     */
    public static final StartPosition RED_FAR =
            new StartPosition("Red far wall", Alliance.RED, new Pose(62.3, 133.8, Math.toRadians(270)));

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
    static final StartPosition[] ALL = {RED_AUDIENCE, RED_FAR};

    /**
     * The team spot each start in {@link #ALL} is, in the same order: the Visualizer's start
     * buttons are the team's {@code START_} spots ({@code TeamCode/autos/spots.json}), and
     * {@code TeamSpotsTest} fails while one differs from the start measured here. Measure a start,
     * then change both: this file, and the spot (in the Visualizer, Save team spots).
     */
    static final String[] TEAM_SPOTS = {"START_RIGHT", "START_LEFT"};

    /** The measured start a team spot is, or null for a spot that is not a measured start. */
    static StartPosition forTeamSpot(String name) {
        for (int i = 0; i < ALL.length; i++) {
            if (TEAM_SPOTS[i].equals(name)) return ALL[i];
        }
        return null;
    }

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
