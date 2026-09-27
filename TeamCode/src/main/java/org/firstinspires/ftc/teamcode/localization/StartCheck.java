package org.firstinspires.ftc.teamcode.localization;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.util.Alliance;

/**
 * During an Autonomous's INIT: is the robot where its declared start says, and on that alliance?
 *
 * <p>The OpMode puts the pose at the declared start with a loose uncertainty. Camera fixes then pull
 * the fused pose to where the robot really is. So the distance between the fused pose and the
 * declared one <em>is</em> the placement error — once enough fixes have been accepted to believe
 * it. No fixes means the check cannot confirm anything, and it says so rather than passing.
 *
 * <p>Heading is not checked: fixes are computed from the Pinpoint's heading, which is taken from the
 * declaration. A robot placed facing the wrong way produces fixes that disagree with each other and
 * are rejected, so it shows up as "can't confirm", not as a false pass.
 */
public final class StartCheck {

    public enum Status { NOT_DECLARED, UNCONFIRMED, IN_POSITION, OUT_OF_POSITION }

    /** Fixes needed before a placement is believed. */
    public static final int FIXES_TO_CONFIRM = 5;

    private StartCheck() {
    }

    public static final class Result {
        public final Status status;
        public final StartPosition start;
        public final double errorIn;

        Result(Status status, StartPosition start, double errorIn) {
            this.status = status;
            this.start = start;
            this.errorIn = errorIn;
        }

        /** The alliance the camera confirms, or {@code UNKNOWN} — only a confirmed placement counts. */
        public Alliance confirmedAlliance() {
            return status == Status.IN_POSITION ? start.alliance : Alliance.UNKNOWN;
        }

        public void describe(Display display) {
            switch (status) {
                case NOT_DECLARED:
                    return;
                case UNCONFIRMED:
                    display.status("Start", Display.Level.WARN, start + ": camera can't confirm — "
                            + "using the declared pose");
                    return;
                case IN_POSITION:
                    display.status("Start", Display.Level.OK, String.format(java.util.Locale.US,
                            "%s: in position (%.1f in off)", start, errorIn));
                    return;
                default:
                    display.status("Start", Display.Level.WARN, String.format(java.util.Locale.US,
                            "%s: OFF BY %.1f in — reposition the robot", start, errorIn));
            }
        }
    }

    /**
     * @param start          the declared start, or null if this OpMode declares none
     * @param fused          the current fused pose
     * @param acceptedFixes  fixes accepted since the pose was set to the declared start
     */
    public static Result evaluate(StartPosition start, Pose fused, int acceptedFixes) {
        if (start == null) {
            return new Result(Status.NOT_DECLARED, null, Double.NaN);
        }
        if (fused == null || acceptedFixes < FIXES_TO_CONFIRM) {
            return new Result(Status.UNCONFIRMED, start, Double.NaN);
        }
        double error = Math.hypot(fused.x() - start.pose.x(), fused.y() - start.pose.y());
        Status status = error <= LocalizationTuning.startMarginIn
                ? Status.IN_POSITION : Status.OUT_OF_POSITION;
        return new Result(status, start, error);
    }
}
