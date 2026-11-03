package org.firstinspires.ftc.teamcode.localization;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.FieldFrame;

/**
 * The marked spots the camera is calibrated from ({@code CameraCalibration}, Vision: HIVE &amp;
 * Pose's R1/R2): places a drive team can put the robot by a wall and a seam, to a fraction of an
 * inch, that face a HIVE so the camera has tags. The only list; the OpMode cycles it.
 *
 * <p>Why these four. Red audience and red far wall see the <b>same</b> HIVE from opposite sides,
 * so a mount error gives the same answer at both and a HIVE point that is off on this field gives
 * opposite ones ({@code CameraCalibration.agree}). The two blue-side spots are the same pair turned
 * half a turn (the field is rotationally symmetric, manual Figure 9-2), for a check from the other
 * end and for a blue practice field.
 *
 * <p><b>Not here yet: diagonal corner spots.</b> A robot slid into a field corner at 45 deg, both
 * frame corners on the walls, would be repeatable and would look at the HIVE from a third heading.
 * Its pose needs this robot's frame width and length (the centre sits half the diagonal from the
 * corner), and whether the camera sees the CELL's underside tags from 45 deg off the row at about
 * 85 in is untested. Measure both on the robot and add it; never guess it (CLAUDE.md).
 */
public final class CalibrationSpots {

    private CalibrationSpots() {
    }

    /**
     * Tile D1, facing away from the audience, back flat on the audience wall, the frame's left side
     * on the C-D seam: {@link StartPositions#RED_FAR} turned half a turn. From here the camera sees
     * three or four of the red HIVE's tags (noted when RED_FAR was measured).
     */
    public static final StartPosition BLUE_AUDIENCE = new StartPosition("Blue audience wall",
            Alliance.BLUE, halfTurn(StartPositions.RED_FAR.pose));

    /** In the order R2 cycles them: the red pair first, then the blue pair. */
    public static final StartPosition[] ALL = {
            StartPositions.RED_AUDIENCE,
            StartPositions.RED_FAR,
            BLUE_AUDIENCE,
            StartPositions.BLUE_FROM_RED_AUDIENCE,
    };

    private static Pose halfTurn(Pose pose) {
        return new Pose(2 * FieldFrame.FIELD_CENTRE_INCHES - pose.x(),
                2 * FieldFrame.FIELD_CENTRE_INCHES - pose.y(), pose.heading() + Math.PI);
    }
}
