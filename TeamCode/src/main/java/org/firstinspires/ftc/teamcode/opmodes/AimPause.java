package org.firstinspires.ftc.teamcode.opmodes;

/**
 * Whether a feed that the driver's button started should hold back because the robot has been
 * knocked off its aim ({@code TeleOpConfig.pauseFeedOffAim}): a shove from a defender swings the
 * robot off the CELL faster than a person can let go. Paused once the aim is more than the
 * tolerance off, and fed again once it is back within {@link #RESUME_FRACTION} of it, so an aim
 * sitting on the edge does not open and shut the gate every loop. Each pause costs the feed's
 * lead again (0.7 to 0.9 s from gate open to first piece at Belding), which is why it is a switch.
 *
 * <p>Pure, one per button, so a test drives every case.
 */
final class AimPause {

    /** A paused feed resumes within this fraction of the tolerance. A starting point, <b>not measured</b>. */
    static final double RESUME_FRACTION = 0.5;

    private boolean paused;

    /**
     * One step, every loop the button is feeding or wants to.
     *
     * @param enabled       the switch; off never pauses
     * @param errorDeg      how far off the aim is, degrees, signed; NaN when unknown
     * @param toleranceDeg  the aim tolerance, degrees; NaN, zero or negative never pauses
     * @param unknownPauses an unknown aim pauses (Circle, which fires only on a known aim)
     * @return the feed holds back this loop
     */
    boolean update(boolean enabled, double errorDeg, double toleranceDeg, boolean unknownPauses) {
        if (!enabled || !(toleranceDeg > 0.0)) {
            paused = false;
        } else if (Double.isNaN(errorDeg)) {
            paused = unknownPauses;
        } else if (Math.abs(errorDeg) > toleranceDeg) {
            paused = true;
        } else if (Math.abs(errorDeg) <= toleranceDeg * RESUME_FRACTION) {
            paused = false;
        }
        return paused;
    }

    /** The button was let go: the next press starts unpaused. */
    void reset() {
        paused = false;
    }

    boolean paused() {
        return paused;
    }
}
