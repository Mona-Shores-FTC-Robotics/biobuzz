package org.firstinspires.ftc.teamcode.localization;

import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.util.Alliance;

/**
 * During an Autonomous's INIT: is the robot where its declared start says, and on that alliance?
 *
 * <p>With the pose set to the declared start, every camera fix says where the robot really is. The
 * distance from the declaration to the average of those fixes is the placement error — once there
 * are enough fixes to believe it. No fixes means nothing is confirmed, and it says so rather than
 * passing.
 *
 * <p>Heading is not checked: fixes are computed from the Pinpoint's heading, which is taken from the
 * declaration. A robot placed facing the wrong way gives fixes scattered far from the start, so it
 * reads as out of position, not as a false pass.
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
     * @param start     the declared start, or null if this OpMode declares none
     * @param fixCount  camera fixes since the pose was set to the declared start
     * @param meanFixX  their average x, inches (ignored when {@code fixCount} is 0)
     * @param meanFixY  their average y, inches
     */
    public static Result evaluate(StartPosition start, int fixCount, double meanFixX, double meanFixY) {
        if (start == null) {
            return new Result(Status.NOT_DECLARED, null, Double.NaN);
        }
        if (fixCount < FIXES_TO_CONFIRM) {
            return new Result(Status.UNCONFIRMED, start, Double.NaN);
        }
        double error = Math.hypot(meanFixX - start.pose.x(), meanFixY - start.pose.y());
        Status status = error <= LocalizationTuning.startMarginIn
                ? Status.IN_POSITION : Status.OUT_OF_POSITION;
        return new Result(status, start, error);
    }
}
