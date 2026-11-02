package org.firstinspires.ftc.teamcode.opmodes;

import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.localization.StartCheck;
import org.firstinspires.ftc.teamcode.util.Alliance;

import java.util.Locale;

/**
 * What the drive team must fix before PLAY in an Autonomous's INIT: each warning is what is wrong
 * and the one thing to do about it. While there is any, the robot's light flashes white ("read the
 * Driver Station") and the Match page shows them at the top, big, most serious first. The light
 * and the page read this one list, so a flashing light always has its reason on the screen.
 *
 * <p>The mentor's list and wording (2 Nov 2026). Only what a person can fix before PLAY is here:
 * "the pose is not on the field yet", say, is not, since nobody can act on it. A TeleOp's INIT shows
 * none of this; its light is the alliance colour.
 *
 * <p>Pure: {@link #evaluate} takes plain values, so a test can check every row.
 */
public final class InitWarnings {

    /** The warnings, most serious first: the order they are listed in. */
    public enum Kind {
        /** The Auto drives by it: without it, no path is followed. */
        PINPOINT,
        /** The whole Auto runs turned about, or (none) does not move. */
        ALLIANCE,
        OFF_MARK,
        CAMERA_NOT_ANSWERING,
        CAMERA_NO_FRAMES,
        BATTERY_LOW
    }

    /** The camera may take this long after INIT to send its first frame before that is a warning. */
    public static final long FIRST_FRAME_GRACE_MS = 15_000;

    private static final Kind[] KINDS = Kind.values();
    private static final String REPLUG = "unplug and replug the Limelight, wait 30 s";

    private final String[] what = new String[KINDS.length];
    private final String[] action = new String[KINDS.length];
    private int count;

    /**
     * This loop's warnings, replacing the last loop's.
     *
     * @param pinpointProblem what went wrong with the Pinpoint this INIT (missing, or a fault it
     *                        reported, kept once seen), or null
     * @param alliance        the alliance in use ({@code MatchSetup.alliance()})
     * @param cameraSees      the alliance whose start the camera puts the robot on, or {@code UNKNOWN}
     * @param start           the start check, or null without a declared start
     * @param cameraProblem   why the camera is missing or not answering, or null when it answers
     * @param cameraFrames    frames the camera has sent since INIT
     * @param msSinceInit     time since INIT was pressed
     * @param batteryVolts    the battery at INIT
     * @param lowBatteryVolts below this it is a warning
     */
    public void evaluate(String pinpointProblem, Alliance alliance, Alliance cameraSees,
                         StartCheck.Result start, String cameraProblem, long cameraFrames,
                         long msSinceInit, double batteryVolts, double lowBatteryVolts) {
        for (int i = 0; i < KINDS.length; i++) {
            what[i] = null;
            action[i] = null;
        }
        count = 0;
        if (pinpointProblem != null) {
            set(Kind.PINPOINT, "Pinpoint: " + pinpointProblem, "reseat the Pinpoint cables, restart the robot");
        }
        if (!known(alliance)) {
            set(Kind.ALLIANCE, "No alliance", "press X for blue, B for red");
        } else if (known(cameraSees) && cameraSees != alliance) {
            set(Kind.ALLIANCE, alliance + " chosen, but the camera sees the " + cameraSees + " start",
                    "press " + (cameraSees == Alliance.BLUE ? "X for blue" : "B for red")
                            + " (or move the robot to the " + alliance + " start)");
        }
        if (start != null && start.status == StartCheck.Status.OUT_OF_POSITION) {
            set(Kind.OFF_MARK, String.format(Locale.US, "%.0f in off the start mark", start.errorIn),
                    "move it onto the mark: " + start.start.placement());
        }
        if (cameraProblem != null) {
            set(Kind.CAMERA_NOT_ANSWERING, "Camera: " + cameraProblem, REPLUG);
        } else if (cameraFrames == 0 && msSinceInit >= FIRST_FRAME_GRACE_MS) {
            set(Kind.CAMERA_NO_FRAMES, "Camera: no picture after " + (FIRST_FRAME_GRACE_MS / 1000) + " s", REPLUG);
        }
        if (batteryVolts < lowBatteryVolts) {
            set(Kind.BATTERY_LOW, String.format(Locale.US, "Battery %.1f V", batteryVolts),
                    "swap to a charged battery");
        }
    }

    private void set(Kind kind, String whatText, String actionText) {
        what[kind.ordinal()] = whatText;
        action[kind.ordinal()] = actionText;
        count++;
    }

    /** Whether there is anything to fix. */
    public boolean any() {
        return count > 0;
    }

    public int count() {
        return count;
    }

    /** Whether {@code kind} is among them. */
    public boolean has(Kind kind) {
        return what[kind.ordinal()] != null;
    }

    /** What is wrong, or null when {@code kind} is fine. */
    public String what(Kind kind) {
        return what[kind.ordinal()];
    }

    /** What to do about it, or null when {@code kind} is fine. */
    public String action(Kind kind) {
        return action[kind.ordinal()];
    }

    /**
     * The first line of the INIT page, big: the alliance in its colour, and in an Auto the verdict,
     * READY or how many things to fix, with the warnings under it ({@link #describe}). A TeleOp's
     * INIT has no warnings (mentor, 2 Nov 2026): the alliance only.
     */
    public void describeTop(Display display, Alliance alliance, boolean autonomous) {
        String who = alliance == Alliance.RED ? "<font color=\"#E53935\">RED ALLIANCE</font>"
                : alliance == Alliance.BLUE ? "<font color=\"#1E88E5\">BLUE ALLIANCE</font>" : "NO ALLIANCE";
        if (!autonomous) {
            display.line("<big><b>" + who + "</b></big>");
            return;
        }
        String verdict = count > 0
                ? "<font color=\"#FFB300\">" + count + " TO FIX</font>"
                : "<font color=\"#4CAF50\">READY</font>";
        display.line("<big><b>" + who + " · " + verdict + "</b></big>");
        describe(display);
    }

    /**
     * Under the INIT page's top line: every warning with its action, most serious first. Nothing
     * with none: the top line already says READY.
     */
    public void describe(Display display) {
        for (int i = 0; i < KINDS.length; i++) {
            if (what[i] != null) display.alert(what[i], action[i]);
        }
    }

    /** One line for the match log: every warning, most serious first; "none" with none. */
    public String summary() {
        if (count == 0) return "none";
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < KINDS.length; i++) {
            if (what[i] == null) continue;
            if (text.length() > 0) text.append("; ");
            text.append(what[i]);
        }
        return text.toString();
    }

    private static boolean known(Alliance alliance) {
        return alliance == Alliance.RED || alliance == Alliance.BLUE;
    }
}
