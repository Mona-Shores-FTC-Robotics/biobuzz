package org.firstinspires.ftc.teamcode.opmodes;

/**
 * Who has Basic Drive's launcher, and so when it spins down. Pure: the OpMode asks it what a press
 * does and spins the launcher up or down as told, so a test drives every case.
 *
 * <p>The rules, in the words a drive team uses:
 * <ul>
 *   <li><b>Square spun it up, Square spins it down.</b> A launch-spot press that fires
 *       ({@code TeleOpConfig.goToSpotAlsoFires}) and finds the launcher idle spins it up, and
 *       idles it again when the press ends.</li>
 *   <li><b>The operator spun it up, the operator spins it down.</b> Spin up (or Fire from idle)
 *       gives the launcher to the operator; it stays at launch speed until the operator's toggle
 *       idles it, so a launcher spun up early stays spinning through a Square press and after it.</li>
 *   <li><b>The operator cannot hurt a Square volley.</b> While a Square press is firing, the
 *       operator's toggle never idles the launcher: it keeps it spinning and gives it to the
 *       operator, so it stays on after the press. (Until 7 Nov 2026 the toggle idled it mid-drive,
 *       and the volley then fed below launch speed once its spin-up wait ran out.) A Fire press
 *       during the volley also gives the launcher to the operator; the OpMode holds that press's
 *       feed back so the volley still waits for the spot, aim and speed.</li>
 * </ul>
 * Nothing else idles it: an idle launcher is one nobody has.
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
        if (spotFiring) {
            owner = Owner.OPERATOR; // keep it, and keep it after the press
            return Effect.KEEP;
        }
        owner = Owner.NONE;
        return Effect.SPIN_DOWN;
    }

    /**
     * The operator's Fire, at the press. From idle it spins up for the operator, unless unjam is
     * held (that press feeds nothing); on Square's launcher it takes it over.
     */
    Effect operatorFire(boolean unjamHeld) {
        if (owner == Owner.NONE) {
            if (unjamHeld) return Effect.KEEP;
            owner = Owner.OPERATOR;
            return Effect.SPIN_UP;
        }
        owner = Owner.OPERATOR;
        return Effect.KEEP;
    }

    /** A Square press that fires has started. */
    Effect spotPressStarted() {
        if (owner != Owner.NONE) return Effect.KEEP;
        owner = Owner.SPOT;
        return Effect.SPIN_UP;
    }

    /** That press has ended (let go, sticks moved, timed out, pose lost). */
    Effect spotPressEnded() {
        if (owner != Owner.SPOT) return Effect.KEEP;
        owner = Owner.NONE;
        return Effect.SPIN_DOWN;
    }
}
