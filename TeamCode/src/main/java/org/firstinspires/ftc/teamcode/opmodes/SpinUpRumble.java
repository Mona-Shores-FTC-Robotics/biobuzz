package org.firstinspires.ftc.teamcode.opmodes;

/**
 * When the operator's pad rumbles for launch speed: once per spin-up, the first loop the launcher
 * is at speed after it started spinning from idle or stopped, and not again until it has gone back
 * to idle (mentor, 18 Nov 2026). At speed flaps in a volley (every shot dips the wheel out of the
 * tolerance) and as the range-based target moves while the robot drives, and a rumble on every
 * re-entry buzzed the pad all match. The light bar still follows at speed every loop. Pure, so a
 * test drives it.
 */
final class SpinUpRumble {

    /** This spin-up has had its rumble. Cleared when the launcher goes back to idle. */
    private boolean rumbled;

    /**
     * One loop: {@code spinning} is the launcher at launch speed's setpoint (not idle),
     * {@code atSpeed} whether it reads at that speed. True on the one loop to rumble.
     */
    boolean update(boolean spinning, boolean atSpeed) {
        if (!spinning) {
            rumbled = false;
            return false;
        }
        if (!atSpeed || rumbled) return false;
        rumbled = true;
        return true;
    }
}
