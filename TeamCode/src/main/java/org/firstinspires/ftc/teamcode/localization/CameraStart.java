package org.firstinspires.ftc.teamcode.localization;

import com.pedropathing.math.Pose;

/**
 * A TeleOp with no handoff assumes its declared start, wherever the robot is. During its INIT this
 * watches the camera's whole-pose fits ({@code DriveSubsystem.wholeCameraPose}, two or more tags, no
 * heading assumed) and, once {@link LocalizationTuning#seedFrames} in a row agree
 * ({@link LocalizationTuning#seedAgreementIn}, {@link LocalizationTuning#seedAgreementDeg}) and sit
 * further from the start than {@link LocalizationTuning#startMarginIn} or
 * {@link LocalizationTuning#startOverrideDeg}, says to take the camera's pose instead. On the mark, or
 * with no steady fit, it says nothing and the declared start stands: the marks win when the robot is
 * on them.
 *
 * <p>1 Nov 2026, 20245: five practice TeleOps ran a whole session with a pose 17 to 41 in off, because
 * the robot was not on the start mark and nothing moved the pose to where the camera could see it was.
 * An Autonomous never takes this path: its start is declared and the drive team places by the marks.
 * Plain Java on Pedro's {@link Pose}, so it is unit tested.
 */
public final class CameraStart {

    private Pose candidate;
    private int agreeing;
    private double offIn = Double.NaN;
    private double offDeg = Double.NaN;

    /**
     * One new camera fit against the declared {@code start}. Returns the pose to take instead of the
     * start, or null to leave the start alone (this frame).
     */
    public Pose update(Pose fit, StartPosition start) {
        if (fit == null || start == null) return null;
        if (candidate != null && agrees(fit, candidate)) {
            agreeing++;
        } else {
            agreeing = 1;
        }
        candidate = fit;
        if (agreeing < Math.max(1, LocalizationTuning.seedFrames)) return null;
        offIn = Math.hypot(fit.x() - start.pose.x(), fit.y() - start.pose.y());
        offDeg = Math.toDegrees(Math.IEEEremainder(fit.heading() - start.pose.heading(), 2 * Math.PI));
        boolean onTheMark = offIn <= LocalizationTuning.startMarginIn
                && Math.abs(offDeg) <= LocalizationTuning.startOverrideDeg;
        return onTheMark ? null : fit;
    }

    public void reset() {
        candidate = null;
        agreeing = 0;
        offIn = Double.NaN;
        offDeg = Double.NaN;
    }

    /** How far the last decided fit sat from the start, inches and degrees; NaN before one. */
    public double offIn() { return offIn; }
    public double offDeg() { return offDeg; }

    static boolean agrees(Pose a, Pose b) {
        return Math.hypot(a.x() - b.x(), a.y() - b.y()) <= LocalizationTuning.seedAgreementIn
                && Math.toDegrees(Math.abs(Math.IEEEremainder(a.heading() - b.heading(), 2 * Math.PI)))
                <= LocalizationTuning.seedAgreementDeg;
    }
}
