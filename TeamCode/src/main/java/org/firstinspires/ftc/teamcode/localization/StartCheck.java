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
 * reads as out of position, not as a false pass. A robot turned a little gives a sideways error
 * instead: 2° at 50 in from the CELL is about 1.7 in. So a small sideways error can be the robot
 * not being square, not just not being in the right place.
 *
 * <p><b>The marks win.</b> The drive team places the robot by the wall and the seam, and the
 * camera is not yet trusted to an inch, so a small gap is shown as information and recorded
 * ({@code RobotOpMode} logs it at PLAY as a calibration sample), never as an instruction. Only a
 * gap past {@link LocalizationTuning#startMarginIn} (a wrong tile, side or facing) warns, and then
 * it says to check the marks, not to follow the camera. Which way the camera has the robot,
 * {@link Result#moveForwardIn} / {@link Result#moveLeftIn}, goes to the log only.
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
        /**
         * Which way to move the robot to put it on the start, inches, in the start's own frame:
         * + forward / + left as the robot faces there. NaN until confirmed or out of position.
         */
        public final double moveForwardIn;
        public final double moveLeftIn;

        Result(Status status, StartPosition start, double errorIn, double moveForwardIn, double moveLeftIn) {
            this.status = status;
            this.start = start;
            this.errorIn = errorIn;
            this.moveForwardIn = moveForwardIn;
            this.moveLeftIn = moveLeftIn;
        }

        /** The alliance the camera confirms, or {@code UNKNOWN} — only a confirmed placement counts. */
        public Alliance confirmedAlliance() {
            return status == Status.IN_POSITION ? start.alliance : Alliance.UNKNOWN;
        }

        public void describe(Display display) {
            if (status == Status.NOT_DECLARED) return;
            String[] map = start.map();
            if (map != null) {
                for (String line : map) display.line(line);
            }
            switch (status) {
                case UNCONFIRMED:
                    display.status("Start", Display.Level.OK, start + " (the " + start.arrow()
                            + ") — place: " + start.howToPlace());
                    display.line("<small>Camera: not seeing it yet (nothing to do)</small>");
                    return;
                case IN_POSITION:
                    display.status("Start", Display.Level.OK, start + " (the " + start.arrow()
                            + ") — place: " + start.howToPlace());
                    display.line(String.format(java.util.Locale.US,
                            "<small>Camera: %.1f in from the start, recorded (don't adjust for it)</small>",
                            errorIn));
                    return;
                default:
                    display.status("Start", Display.Level.WARN, String.format(java.util.Locale.US,
                            "%s: the camera puts the robot %.0f in away — CHECK THE MARKS", start, errorIn));
                    display.line("Place (the " + start.arrow() + "): " + start.howToPlace());
            }
        }
    }

    /**
     * @param start     the declared start, or null if this OpMode declares none
     * @param fixCount  camera fixes since the pose was set to the declared start
     * @param meanFixX  the average x of the latest of them, inches (ignored when {@code fixCount} is 0)
     * @param meanFixY  their average y, inches
     */
    public static Result evaluate(StartPosition start, int fixCount, double meanFixX, double meanFixY) {
        if (start == null) {
            return new Result(Status.NOT_DECLARED, null, Double.NaN, Double.NaN, Double.NaN);
        }
        if (fixCount < FIXES_TO_CONFIRM) {
            return new Result(Status.UNCONFIRMED, start, Double.NaN, Double.NaN, Double.NaN);
        }
        // Where the robot is, minus where it should be; the move is the opposite, turned into the
        // start's own frame so it reads as the robot faces.
        double dx = meanFixX - start.pose.x();
        double dy = meanFixY - start.pose.y();
        double c = Math.cos(start.pose.heading());
        double s = Math.sin(start.pose.heading());
        double moveForward = -(c * dx + s * dy);
        double moveLeft = -(-s * dx + c * dy);
        double error = Math.hypot(dx, dy);
        Status status = error <= LocalizationTuning.startMarginIn
                ? Status.IN_POSITION : Status.OUT_OF_POSITION;
        return new Result(status, start, error, moveForward, moveLeft);
    }
}
