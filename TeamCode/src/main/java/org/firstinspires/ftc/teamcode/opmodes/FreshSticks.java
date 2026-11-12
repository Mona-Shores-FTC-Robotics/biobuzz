package org.firstinspires.ftc.teamcode.opmodes;

/**
 * Whether the drive sticks take the robot back from a held routine (the launch-spot drive, a
 * FLOWER pick, a GARDEN gather). Sticks always win, but only a stick movement made after the
 * press: a stick that was already off centre when the button went down is the driver still
 * driving up to it, not an answer to it. Pure, one per routine, so a test drives every case.
 *
 * <p>Why: at the Belding Qualifier (7 Nov 2026) 50 of the 51 launch-spot presses cancelled within
 * 0.1 s had a stick already past the threshold when the button went down, and 21 FLOWER pick
 * presses ended the same way (#495). Those presses fired nothing.
 *
 * <p>The rule: a stick off centre at the press is <b>stale</b>. It becomes fresh, and from then on
 * cancels, when it comes back to centre once, or when it is still off centre
 * {@code staleCancelsAfterS} after the press (a driver still pushing by then means it). A stick
 * that was centred at the press cancels at once, as before.
 */
final class FreshSticks {

    private boolean wasHeld;
    private boolean stale;
    private double pressS;

    /**
     * One step, every loop.
     *
     * @param held               the routine's button is held
     * @param offCentre          a drive stick is past the cancel threshold
     * @param nowS               a clock, seconds
     * @param staleCancelsAfterS how long a stick held since before the press is ignored; 0, negative
     *                           or NaN cancels at once (the rule before #495)
     * @return the sticks take the robot back this loop
     */
    boolean moved(boolean held, boolean offCentre, double nowS, double staleCancelsAfterS) {
        if (!held) {
            wasHeld = false;
            stale = false;
            return offCentre;
        }
        if (!wasHeld) {
            wasHeld = true;
            stale = offCentre;
            pressS = nowS;
        }
        if (stale && (!offCentre || !(nowS - pressS < staleCancelsAfterS))) stale = false;
        return offCentre && !stale;
    }

    /** A stick is being ignored because it was off centre at the press. */
    boolean ignoring() {
        return stale;
    }
}
