package org.firstinspires.ftc.teamcode.controls;

import org.firstinspires.ftc.teamcode.util.Alliance;

/**
 * The decisions made before PLAY — today, the alliance — worked out from the robot's sensors
 * wherever possible, because a person under match pressure will eventually press the wrong button.
 *
 * <p>Sources, strongest first:
 *
 * <ol>
 *   <li><b>Manual</b> — X (blue) or B (red) on either gamepad during INIT. Sticks: once someone has
 *       chosen, nothing overrides them.</li>
 *   <li><b>Autonomous</b> — inherited through {@link Handoff} when TeleOp follows an Auto.</li>
 *   <li><b>Vision</b> — proposed every INIT loop from the AprilTags in view.</li>
 * </ol>
 *
 * <p>The strongest source present wins, and a weaker one that disagrees is shown in amber rather
 * than hidden — so a wrong button press is visible to the drive coach before PLAY. With no source at
 * all the alliance is {@link Alliance#UNKNOWN} and the Driver Station says so in red. Nothing
 * guesses a colour: DECODE's silent fallback to BLUE is the bug this replaces.
 *
 * <p>{@link #lock()} at PLAY freezes the choice for the match.
 */
public final class MatchSetup {

    /** Where the current alliance came from. Ordered weakest to strongest. */
    public enum Source { NONE, VISION, AUTO, MANUAL }

    private Alliance manual = Alliance.UNKNOWN;
    private Alliance fromAuto = Alliance.UNKNOWN;
    private Alliance fromVision = Alliance.UNKNOWN;
    private String visionEvidence = "";
    private boolean locked;

    /** A person chose. Ignored once locked, and for {@code UNKNOWN}. */
    public void chooseManually(Alliance alliance) {
        if (!locked && known(alliance)) {
            manual = alliance;
        }
    }

    /** Autonomous handed this over. Ignored once locked. */
    public void inheritFromAuto(Alliance alliance) {
        if (!locked) {
            fromAuto = alliance == null ? Alliance.UNKNOWN : alliance;
        }
    }

    /** Vision's current proposal, with what it saw. Called every INIT loop; ignored once locked. */
    public void offerVision(Alliance alliance, String evidence) {
        if (!locked) {
            fromVision = alliance == null ? Alliance.UNKNOWN : alliance;
            visionEvidence = evidence == null ? "" : evidence;
        }
    }

    /** Freeze the choice. Called at PLAY. */
    public void lock() {
        locked = true;
    }

    public boolean isLocked() {
        return locked;
    }

    public Alliance alliance() {
        switch (source()) {
            case MANUAL: return manual;
            case AUTO:   return fromAuto;
            case VISION: return fromVision;
            default:     return Alliance.UNKNOWN;
        }
    }

    public Source source() {
        if (known(manual)) return Source.MANUAL;
        if (known(fromAuto)) return Source.AUTO;
        if (known(fromVision)) return Source.VISION;
        return Source.NONE;
    }

    /** A weaker source names a different alliance from the one in use. */
    public boolean hasDisagreement() {
        Alliance chosen = alliance();
        return known(chosen)
                && ((known(fromAuto) && fromAuto != chosen)
                || (known(fromVision) && fromVision != chosen));
    }

    /** One status line, plus the camera's evidence before PLAY. */
    public void describe(Display display) {
        Alliance chosen = alliance();
        String lock = locked ? " · locked" : "";
        if (!known(chosen)) {
            display.status("Alliance", Display.Level.FAULT,
                    locked ? "NONE — was not chosen before PLAY" : "NONE — press X for blue, B for red");
        } else if (hasDisagreement()) {
            display.status("Alliance", Display.Level.WARN,
                    chosen + " (" + label(source()) + ") — but " + dissent() + lock);
        } else {
            display.status("Alliance", Display.Level.OK, chosen + " (" + label(source()) + ")" + lock);
        }
        if (!locked && !visionEvidence.isEmpty()) {
            display.line("<small>Camera: " + visionEvidence + "</small>");
        }
    }

    private String dissent() {
        Alliance chosen = alliance();
        if (known(fromAuto) && fromAuto != chosen) return "Auto said " + fromAuto;
        return "vision sees " + fromVision;
    }

    private static String label(Source source) {
        switch (source) {
            case MANUAL: return "chosen";
            case AUTO:   return "from Auto";
            case VISION: return "vision";
            default:     return "none";
        }
    }

    private static boolean known(Alliance alliance) {
        return alliance == Alliance.RED || alliance == Alliance.BLUE;
    }
}
