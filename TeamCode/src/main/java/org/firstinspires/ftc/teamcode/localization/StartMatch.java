package org.firstinspires.ftc.teamcode.localization;

import com.pedropathing.math.Pose;

/**
 * During an Autonomous's INIT: which known start the camera puts the robot on. The Auto's own two
 * (as drawn, or rotated for the other alliance) give the alliance; any other known start
 * ({@link StartPositions#known}) means the wrong Auto was picked or the robot is on the wrong spot,
 * and INIT says which.
 *
 * <p>{@link StartCheck} cannot tell: it compares the camera with the one start declared, using the
 * Pinpoint heading that start set, so a robot on another start reads only as out of position. This
 * tries every candidate: each frame is solved position-only with each start's own heading
 * ({@code DriveSubsystem.setStartCandidates}, {@code TagGate.solve}), and a start matches when its
 * solve lands within {@link #MATCH_RADIUS_IN} of it. With a heading that is not the robot's the
 * tags put it somewhere else entirely (half a turn out: about two camera ranges away), so one
 * HIVE's tags are enough. Until 2 Nov 2026 this used the camera's whole-pose fit instead, which
 * needs tags on both HIVEs ({@link LocalizationTuning#minFitLeverIn}) and no start sees both.
 *
 * <p>The radius is wide for placement and a two-tag position (an inch or two) and narrow for the
 * nearest pair of starts that face the same way: the red audience start and blue's audience-wall
 * start are 23.6 in apart, so 10 in cannot match both. Whether the robot sits on its start to the
 * inch is the start check's job, which follows with {@link LocalizationTuning#startMarginIn}.
 * {@link #FRAMES_TO_AGREE} frames in a row on one start settle it; as many in a row on none clear
 * it. It never guesses: no match, no start.
 *
 * <p>Plain Java on Pedro's {@link Pose}, so it is unit tested.
 */
public final class StartMatch {

    /** Camera frames in a row that must agree before the answer changes. */
    public static final int FRAMES_TO_AGREE = 5;
    /** How far from a start the robot may be solved and still count as on it, inches. */
    public static final double MATCH_RADIUS_IN = 10.0;
    /** {@link #settled()} when the robot is on none of the candidates. */
    public static final int NONE = -1;

    private int candidate = NONE;
    private int agreeing;
    private int settled = NONE;

    /**
     * One camera frame: {@code fixes[i]} is where it puts the robot with {@code candidates[i]}'s
     * heading (null when that solve failed). Returns the settled candidate's index, or
     * {@link #NONE} until one is.
     */
    public int update(Pose[] fixes, StartPosition[] candidates) {
        int which = NONE;
        for (int i = 0; i < candidates.length; i++) {
            if (matches(i < fixes.length ? fixes[i] : null, candidates[i])) {
                which = i;
                break;
            }
        }
        if (which == candidate) {
            agreeing++;
        } else {
            candidate = which;
            agreeing = 1;
        }
        if (agreeing >= FRAMES_TO_AGREE) settled = candidate;
        return settled;
    }

    /** The candidate the frames so far have settled on, or {@link #NONE}. */
    public int settled() {
        return settled;
    }

    /** Whether {@code fix}, solved with {@code start}'s heading, lies within the radius of it. */
    static boolean matches(Pose fix, StartPosition start) {
        if (fix == null || start == null) return false;
        return Math.hypot(fix.x() - start.pose.x(), fix.y() - start.pose.y()) <= MATCH_RADIUS_IN;
    }
}
