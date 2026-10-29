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
 * <p>It also says which way to move the robot, in the robot's own terms (forward/back,
 * left/right as the robot faces at its start), since "off by 2 in" alone does not say where.
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

        /** Which way to move the robot, in words. */
        public String move() {
            return StartCheck.move(moveForwardIn, moveLeftIn);
        }

        public void describe(Display display) {
            switch (status) {
                case NOT_DECLARED:
                    return;
                case UNCONFIRMED:
                    display.status("Start", Display.Level.WARN, start + " (" + start.where()
                            + "): camera can't confirm — using the declared pose");
                    return;
                case IN_POSITION:
                    display.status("Start", Display.Level.OK, String.format(java.util.Locale.US,
                            "%s (%s): in position (%.1f in off: %s)", start, start.where(), errorIn, move()));
                    return;
                default:
                    display.status("Start", Display.Level.WARN, String.format(java.util.Locale.US,
                            "%s (%s): OFF BY %.1f in — %s", start, start.where(), errorIn, move()));
            }
        }
    }

    /** "move it 1.8 in back, 0.6 in right", leaving out a part under a quarter inch. */
    static String move(double forwardIn, double leftIn) {
        StringBuilder words = new StringBuilder("move it");
        if (Math.abs(forwardIn) >= 0.25) {
            words.append(String.format(java.util.Locale.US, " %.1f in %s", Math.abs(forwardIn),
                    forwardIn > 0 ? "forward" : "back"));
        }
        if (Math.abs(leftIn) >= 0.25) {
            if (words.length() > "move it".length()) words.append(',');
            words.append(String.format(java.util.Locale.US, " %.1f in %s", Math.abs(leftIn),
                    leftIn > 0 ? "left" : "right"));
        }
        return words.length() == "move it".length() ? "leave it" : words.toString();
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
