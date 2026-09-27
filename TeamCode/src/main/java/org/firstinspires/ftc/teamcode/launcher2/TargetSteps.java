package org.firstinspires.ftc.teamcode.launcher2;

/**
 * The d-pad step rule for a target RPM, kept free of SDK types so it can be
 * unit tested.
 *
 * <p>Same rule as the three-wheel Flywheel Speed Test's {@code nudgeTarget}, on
 * purpose: stepping by the same increments on both rigs is what makes numbers
 * read off one comparable with the other. Each press moves the target by
 * exactly the step, then clamps to {@code [0, maxRpm]}. Nothing snaps to a
 * grid, so a target typed into Panels as 1510 steps to 1535, not 1525 — the
 * step is what stays constant, not the set of reachable values.
 */
public final class TargetSteps {

    private TargetSteps() {}

    /**
     * @param currentRpm the target now
     * @param deltaRpm   the signed step, e.g. {@code +fineStepRpm}
     * @param maxRpm     the ceiling; a negative ceiling is treated as 0
     * @return the new target, never below 0 nor above {@code maxRpm}
     */
    public static double step(double currentRpm, double deltaRpm, double maxRpm) {
        double ceiling = Math.max(0.0, maxRpm);
        double updated = currentRpm + deltaRpm;
        if (updated < 0.0) {
            return 0.0;
        }
        if (updated > ceiling) {
            return ceiling;
        }
        return updated;
    }
}
