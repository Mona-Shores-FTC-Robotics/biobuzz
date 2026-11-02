package org.firstinspires.ftc.teamcode.localization;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.util.Alliance;

/**
 * During an Autonomous's INIT: which of the Auto's two starts (as drawn, or rotated for the other
 * alliance) the camera puts the robot on, and so which alliance it is.
 *
 * <p>{@link StartCheck} cannot tell: it compares the camera with the one start declared, using the
 * Pinpoint heading that start set, so a robot on the other alliance's start reads only as out of
 * position. This takes the camera's whole pose instead, fitted from two or more tags with no
 * heading assumed ({@code DriveSubsystem.wholeCameraPose}), and compares it with both starts:
 * position within {@link #MATCH_RADIUS_IN}, heading within {@link #HEADING_TOLERANCE_DEG}.
 *
 * <p>Those are loose on purpose. The two starts are about 127 in apart, so telling them apart needs
 * no precision, and a fit from only two neighbouring tags (3.75 in apart, about 57 in away; all a
 * robot on blue's left start sees on 31 Oct) has a heading good to a few degrees, which moves its
 * position by several inches. Whether the robot sits on its start to the inch is the start check's
 * job, which follows with the Pinpoint's heading and {@link LocalizationTuning#startMarginIn}. {@link #FRAMES_TO_AGREE} frames in a row on one start settle it;
 * as many in a row on neither clear it. It never guesses: no match, no alliance.
 *
 * <p>Plain Java on Pedro's {@link Pose}, so it is unit tested.
 */
public final class StartMatch {

    /** Frames in a row that must agree before a start is believed, or disbelieved. */
    public static final int FRAMES_TO_AGREE = 5;
    /** A whole-pose fit's position must be this close to a start, inches. */
    public static final double MATCH_RADIUS_IN = 18.0;
    /** A whole-pose fit's heading must be this close to the start's, degrees. */
    public static final double HEADING_TOLERANCE_DEG = 30.0;

    private Alliance candidate = Alliance.UNKNOWN;
    private int agreeing;
    private Alliance settled = Alliance.UNKNOWN;

    /**
     * One new camera fit. Returns the settled alliance: the one whose start the camera has agreed
     * with for {@link #FRAMES_TO_AGREE} frames in a row, else {@code UNKNOWN}.
     *
     * @param fit     the camera's whole pose for one frame (field inches, radians)
     * @param drawn   the Auto's start as drawn
     * @param rotated the same start for the other alliance
     */
    public Alliance update(Pose fit, StartPosition drawn, StartPosition rotated) {
        Alliance which = matches(fit, drawn) ? drawn.alliance
                : matches(fit, rotated) ? rotated.alliance : Alliance.UNKNOWN;
        if (which == candidate) {
            agreeing++;
        } else {
            candidate = which;
            agreeing = 1;
        }
        if (agreeing >= FRAMES_TO_AGREE) settled = candidate;
        return settled;
    }

    /** The settled alliance, as {@link #update} last returned it. */
    public Alliance settled() {
        return settled;
    }

    static boolean matches(Pose fit, StartPosition start) {
        if (fit == null || start == null) return false;
        double off = Math.hypot(fit.x() - start.pose.x(), fit.y() - start.pose.y());
        double turned = Math.toDegrees(Math.abs(
                Math.IEEEremainder(fit.heading() - start.pose.heading(), 2 * Math.PI)));
        return off <= MATCH_RADIUS_IN && turned <= HEADING_TOLERANCE_DEG;
    }
}
