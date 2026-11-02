package org.firstinspires.ftc.teamcode.localization;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.util.Alliance;

/**
 * During an Autonomous's INIT: which of the Auto's two starts (as drawn, or rotated for the other
 * alliance) the camera puts the robot on, and so which alliance it is.
 *
 * <p>{@link StartCheck} cannot tell: it compares the camera with the one start declared, using the
 * Pinpoint heading that start set, so a robot on the other alliance's start reads only as out of
 * position. This tries both: each frame is solved position-only with each start's own heading
 * ({@code DriveSubsystem.setStartCandidates}, {@code TagGate.solve}), and a start matches when its
 * solve lands within {@link #MATCH_RADIUS_IN} of it. The wrong start's heading is half a turn out,
 * which puts the robot about two camera ranges away, so one HIVE's tags are enough and the two
 * cannot both match. Until 2 Nov 2026 this used the camera's whole-pose fit instead, which needs
 * tags on both HIVEs ({@link LocalizationTuning#minFitLeverIn}) and no start sees both.
 *
 * <p>The radius is loose on purpose: the two starts are about 127 in apart, and whether the robot
 * sits on its start to the inch is the start check's job, which follows with
 * {@link LocalizationTuning#startMarginIn}. {@link #FRAMES_TO_AGREE} frames in a row on one start
 * settle it; as many in a row on neither clear it. It never guesses: no match, no alliance.
 *
 * <p>Plain Java on Pedro's {@link Pose}, so it is unit tested.
 */
public final class StartMatch {

    /** Camera frames in a row that must agree before the alliance changes. */
    public static final int FRAMES_TO_AGREE = 5;
    /** How far from a start the robot may be solved and still count as on it, inches. */
    public static final double MATCH_RADIUS_IN = 18.0;

    private Alliance candidate = Alliance.UNKNOWN;
    private int agreeing;
    private Alliance settled = Alliance.UNKNOWN;

    /**
     * One camera frame: where it puts the robot with each start's heading (null when that solve
     * failed), against the starts. Returns the settled alliance, {@code UNKNOWN} until one is.
     */
    public Alliance update(Pose fixAtDrawn, Pose fixAtRotated, StartPosition drawn, StartPosition rotated) {
        Alliance which = matches(fixAtDrawn, drawn) ? drawn.alliance
                : matches(fixAtRotated, rotated) ? rotated.alliance : Alliance.UNKNOWN;
        if (which == candidate) {
            agreeing++;
        } else {
            candidate = which;
            agreeing = 1;
        }
        if (agreeing >= FRAMES_TO_AGREE) settled = candidate;
        return settled;
    }

    /** The alliance the frames so far have settled on, {@code UNKNOWN} until they do. */
    public Alliance settled() {
        return settled;
    }

    /** Whether {@code fix}, solved with {@code start}'s heading, lies within the radius of it. */
    static boolean matches(Pose fix, StartPosition start) {
        if (fix == null || start == null) return false;
        return Math.hypot(fix.x() - start.pose.x(), fix.y() - start.pose.y()) <= MATCH_RADIUS_IN;
    }
}
