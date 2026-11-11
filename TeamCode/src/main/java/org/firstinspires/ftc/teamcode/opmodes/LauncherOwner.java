package org.firstinspires.ftc.teamcode.opmodes;

/**
 * Who has Basic Drive's launcher, and so when it spins down. Pure: the OpMode asks it what a press
 * does and spins the launcher up or down as told, so a test drives every case.
 *
 * <p>The rules, in the words a drive team uses:
 * <ul>
 *   <li><b>Letting go of Square means "done shooting".</b> A launch-spot press that fires
 *       ({@code TeleOpConfig.goToSpotAlsoFires}) spins the launcher up if it is idle, and when the
 *       press ends the launcher idles, whoever spun it up (mentor, 7 Nov 2026: the release is the
 *       driver saying the volley is over, and a launcher left spinning costs battery).</li>
 *   <li><b>The operator spins up early.</b> Spin up (or Fire from idle) before Square gives a press
 *       that finds the launcher already at speed, so the volley does not wait for spin-up.</li>
 *   <li><b>The operator cannot hurt a Square volley.</b> While a Square press is firing, the
 *       operator's toggle never idles the launcher. (Until 7 Nov 2026 it idled it mid-drive, and the
 *       volley then fed below launch speed once its spin-up wait ran out.) The OpMode also holds an
 *       operator Fire's feed back during the volley, so it still waits for the spot, aim and speed.</li>
 *   <li><b>Without Square, the toggle idles it.</b> A launcher the operator spun up and shot with
 *       Fire alone stays on until their toggle.</li>
 * </ul>
 */
final class LauncherOwner {

    enum Owner { NONE, SPOT, OPERATOR }

    /** What the OpMode does to the launcher after a press. */
    enum Effect { SPIN_UP, SPIN_DOWN, KEEP }

    private Owner owner = Owner.NONE;

    Owner owner() {
        return owner;
    }

    /** The launcher is asked for launch speed. */
    boolean spinning() {
        return owner != Owner.NONE;
    }

    /** The operator's spin up / idle toggle. {@code spotFiring}: a Square press that fires is under way. */
    Effect operatorToggle(boolean spotFiring) {
        if (owner == Owner.NONE) {
            owner = Owner.OPERATOR;
            return Effect.SPIN_UP;
        }
        if (spotFiring) return Effect.KEEP; // never idle a volley; the press's end idles it
        owner = Owner.NONE;
        return Effect.SPIN_DOWN;
    }

    /**
     * The operator's Fire, at the press. From idle it spins up for the operator, unless unjam is
     * held (that press feeds nothing); a spinning launcher is left as it is.
     */
    Effect operatorFire(boolean unjamHeld) {
        if (owner != Owner.NONE || unjamHeld) return Effect.KEEP;
        owner = Owner.OPERATOR;
        return Effect.SPIN_UP;
    }

    /** A Square press that fires has started: the launcher is the press's until it ends. */
    Effect spotPressStarted() {
        boolean wasIdle = owner == Owner.NONE;
        owner = Owner.SPOT;
        return wasIdle ? Effect.SPIN_UP : Effect.KEEP;
    }

    /** That press has ended (let go, sticks moved, timed out, pose lost): done shooting, idle. */
    Effect spotPressEnded() {
        if (owner == Owner.NONE) return Effect.KEEP;
        owner = Owner.NONE;
        return Effect.SPIN_DOWN;
    }
}
