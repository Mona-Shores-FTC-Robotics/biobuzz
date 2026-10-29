package org.firstinspires.ftc.teamcode.opmodes;

import java.util.Locale;

/**
 * The checks a shot could wait for, run in <b>shadow mode</b>: computed every loop and logged
 * when one would have stopped a shot, but enforced only where its {@code TeleOpConfig} flag says
 * so. All flags start off. Fire does what the operator asks at once, and the log gathers the
 * evidence for whether a gate would have helped before any gate is allowed to get in the way.
 *
 * <ul>
 *   <li>{@link Gate#LAUNCHER_AT_SPEED}: the launcher is at its launch speed (not idle, not dipping
 *       from the last shot).</li>
 *   <li>{@link Gate#AIM_ON_TARGET}: where the shot would go is within
 *       {@code TeleOpConfig.aimToleranceDeg} of our CELL. Unknown, not closed, when there is no
 *       aim to compare against (no pose on the field, no CELL in view).</li>
 * </ul>
 *
 * <p>An enforced gate gates the <b>start</b> of a feed only, as {@code FeederSubsystem.requestFor}
 * already does: a feed in progress is never cut. An unknown gate never blocks.
 */
public final class FireGates {

    public enum Gate { LAUNCHER_AT_SPEED, AIM_ON_TARGET }

    public enum Verdict { PASS, BLOCK, UNKNOWN }

    private Verdict atSpeed = Verdict.UNKNOWN;
    private Verdict aim = Verdict.UNKNOWN;
    private double aimErrorDeg = Double.NaN;

    /**
     * One loop's evaluation.
     *
     * @param launchMode  the launcher is set to launch speed, not idle
     * @param atLaunchSpeed the launcher reports at speed
     * @param aimErrorDeg how far the shot would miss our CELL, degrees, signed; NaN when unknown
     * @param toleranceDeg the aim gate's tolerance, degrees
     */
    public void evaluate(boolean launchMode, boolean atLaunchSpeed, double aimErrorDeg, double toleranceDeg) {
        atSpeed = launchMode && atLaunchSpeed ? Verdict.PASS : Verdict.BLOCK;
        this.aimErrorDeg = aimErrorDeg;
        if (Double.isNaN(aimErrorDeg) || Double.isNaN(toleranceDeg)) {
            aim = Verdict.UNKNOWN;
        } else {
            aim = Math.abs(aimErrorDeg) <= Math.abs(toleranceDeg) ? Verdict.PASS : Verdict.BLOCK;
        }
    }

    public Verdict verdict(Gate gate) {
        return gate == Gate.LAUNCHER_AT_SPEED ? atSpeed : aim;
    }

    /** Whether any gate, enforced or not, would block a shot now. */
    public boolean anyWouldBlock() {
        return atSpeed == Verdict.BLOCK || aim == Verdict.BLOCK;
    }

    /** Whether the enforced gates let a feed start now. With nothing enforced, always true. */
    public boolean enforcedOpen(boolean enforceAtSpeed, boolean enforceAim) {
        return !(enforceAtSpeed && atSpeed == Verdict.BLOCK) && !(enforceAim && aim == Verdict.BLOCK);
    }

    /** "speed BLOCK, aim PASS (+1.2°)": the gates' state, for a log line. */
    public String summary() {
        return "speed " + atSpeed + ", aim " + aim
                + (Double.isNaN(aimErrorDeg) ? "" : String.format(Locale.US, " (%+.1f°)", aimErrorDeg));
    }
}
