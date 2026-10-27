package org.firstinspires.ftc.teamcode.subsystems;

/**
 * When the turret's zero gets set, so nobody has to think about it. Pure, so a test can check the
 * whole table.
 *
 * <p>The turret's angle is a software offset on a count that survives from one OpMode to the next.
 * Zero means "the count when the turret was at its home mark". The rules:
 *
 * <ul>
 *   <li><b>A person pressing the zero button always wins</b>, at any time.</li>
 *   <li><b>An Autonomous starts a match</b>, so the turret is assumed at home and zeroes where it
 *       stands at PLAY.</li>
 *   <li><b>A TeleOp that follows an Autonomous</b> (a fresh handoff, see {@code Handoff}) takes the
 *       Auto's zero, because the count is still good and the turret may be turned from wherever the
 *       Auto left it. Zeroing at PLAY there would call that spot home.</li>
 *   <li><b>A TeleOp with no Auto before it</b> is a fresh start: zero at PLAY.</li>
 * </ul>
 *
 * <p>The one case this gets wrong is a person re-homing the turret by hand between an Auto and the
 * TeleOp after it without pressing the zero button; the Robot page says where the zero came from,
 * and the button fixes it.
 */
public final class TurretZeroPolicy {

    public enum Action { NOTHING, RESTORE_FROM_AUTO, ZERO_HERE }

    private TurretZeroPolicy() {
    }

    /** At INIT, once the handoff is known. Only a TeleOp takes an Auto's zero. */
    public static Action atInit(boolean autonomous, boolean handoffCarriesZero) {
        return !autonomous && handoffCarriesZero ? Action.RESTORE_FROM_AUTO : Action.NOTHING;
    }

    /** At PLAY: zero here unless something (a person, or the handoff) already did. */
    public static Action atPlay(boolean alreadyZeroed) {
        return alreadyZeroed ? Action.NOTHING : Action.ZERO_HERE;
    }
}
