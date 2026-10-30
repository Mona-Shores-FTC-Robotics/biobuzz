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
 *   <li><b>Default</b> — {@link #DEFAULT_ALLIANCE}, red, but only where {@link #defaultTo} was
 *       called: a TeleOp or test OpMode with no Auto before it.</li>
 * </ol>
 *
 * <p>The strongest source present wins, and a weaker one that disagrees is shown in amber rather
 * than hidden — so a wrong button press is visible to the drive coach before PLAY.
 *
 * <p>An Autonomous never guesses: with no source it is {@link Alliance#UNKNOWN}, the Driver Station
 * says so in red, and the Auto does not move (DECODE's silent fallback to BLUE is the bug this
 * replaced). Anything else defaults to red, because a TeleOp with no side is never what anyone
 * wants and our practice field is laid out for red; the default is shown in amber, "RED (default)",
 * until a button or the camera says.
 *
 * <p>{@link #lock()} at PLAY freezes the choice for the match.
 */
public final class MatchSetup {

    /** Where the current alliance came from. Ordered weakest to strongest. */
    public enum Source { NONE, DEFAULT, VISION, AUTO, MANUAL }

    /** The alliance when nothing else has said: our practice field is laid out for red. */
    public static final Alliance DEFAULT_ALLIANCE = Alliance.RED;

    private Alliance manual = Alliance.UNKNOWN;
    private Alliance fromAuto = Alliance.UNKNOWN;
    private Alliance fromVision = Alliance.UNKNOWN;
    private Alliance fallback = Alliance.UNKNOWN;
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

    /** The drive team discarded the handoff ("start fresh"): forget the Auto's alliance. Ignored once locked. */
    public void forgetAuto() {
        if (!locked) {
            fromAuto = Alliance.UNKNOWN;
        }
    }

    /** Vision's current proposal, with what it saw. Called every INIT loop; ignored once locked. */
    public void offerVision(Alliance alliance, String evidence) {
        if (!locked) {
            fromVision = alliance == null ? Alliance.UNKNOWN : alliance;
            visionEvidence = evidence == null ? "" : evidence;
        }
    }

    /**
     * Use {@code alliance} when nothing else says: for a TeleOp or test OpMode, never an Auto.
     * Ignored once locked.
     */
    public void defaultTo(Alliance alliance) {
        if (!locked) {
            fallback = alliance == null ? Alliance.UNKNOWN : alliance;
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
            case DEFAULT: return fallback;
            default:     return Alliance.UNKNOWN;
        }
    }

    public Source source() {
        if (known(manual)) return Source.MANUAL;
        if (known(fromAuto)) return Source.AUTO;
        if (known(fromVision)) return Source.VISION;
        if (known(fallback)) return Source.DEFAULT;
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
                    locked ? "NONE — was not chosen before PLAY" : "NONE — press Square (X) for blue, Circle (B) for red");
        } else if (source() == Source.DEFAULT) {
            display.status("Alliance", Display.Level.WARN, chosen + " (default"
                    + (locked ? ")" + lock : " — press Square (X) if blue, Circle (B) to confirm red)"));
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
            case DEFAULT: return "default";
            default:     return "none";
        }
    }

    private static boolean known(Alliance alliance) {
        return alliance == Alliance.RED || alliance == Alliance.BLUE;
    }
}
