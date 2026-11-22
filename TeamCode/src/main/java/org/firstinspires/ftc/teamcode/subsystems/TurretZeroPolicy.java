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
 *       stands at INIT (mentor, 19 Nov 2026), then holds its home angle until PLAY, so the drive
 *       team sees at INIT whether it sat on its mark. INIT again zeroes again; PLAY keeps it, and the
 *       TeleOp after it takes it.</li>
 *   <li><b>A TeleOp that follows an Autonomous</b> (a fresh handoff, see {@code Handoff}) takes the
 *       Auto's zero, because the count is still good and the turret may be turned from wherever the
 *       Auto left it. Zeroing at PLAY there would call that spot home.</li>
 *   <li><b>A TeleOp with no Auto before it</b> is a fresh start: zero at PLAY.</li>
 *   <li><b>A robot with a home switch</b> ({@code TurretSubsystem.homeSwitch}, 19429) re-zeroes on
 *       every pass of the turret across it, at any time, in INIT too (a person turning the turret
 *       by hand across its mark zeroes it): the switch knows where the mark is, and every rule above
 *       only assumes it. A zero set by hand still wins, and a pass that disagrees with the zero by
 *       more than {@code TurretSubsystem.homeSwitchMaxCorrectionDeg} is logged, not taken.</li>
 * </ul>
 *
 * <p>Without a home switch, the one case this gets wrong is a person re-homing the turret by hand
 * between an Auto and the TeleOp after it without pressing the zero button; the Robot page says where
 * the zero came from, and the button fixes it. With one, the turret's next pass across the switch
 * fixes it.
 */
public final class TurretZeroPolicy {

    public enum Action {
        NOTHING,
        RESTORE_FROM_AUTO,
        ZERO_HERE,
        /** Move the zero to the mark the home switch found. */
        ZERO_AT_SWITCH,
        /** The home switch's mark is too far from the zero to trust: log it, keep the zero. */
        SWITCH_DISAGREES
    }

    private TurretZeroPolicy() {
    }

    /**
     * At INIT, once the handoff is known. An Autonomous zeroes where the turret stands (assumed on
     * its mark), whatever the last run left; only a TeleOp takes an Auto's zero.
     */
    public static Action atInit(boolean autonomous, boolean handoffCarriesZero) {
        if (autonomous) return Action.ZERO_HERE;
        return handoffCarriesZero ? Action.RESTORE_FROM_AUTO : Action.NOTHING;
    }

    /** At PLAY: zero here unless something (a person, or the handoff) already did. */
    public static Action atPlay(boolean alreadyZeroed) {
        return alreadyZeroed ? Action.NOTHING : Action.ZERO_HERE;
    }

    /**
     * A pass across the home switch ({@link TurretHomeSwitch}) put the mark {@code disagreeDeg} from
     * where the zero says it is (wrapped, so a turret wound a turn round still disagrees by little).
     *
     * @param zeroedByHand     a person pressed the zero button this OpMode: theirs wins
     * @param zeroed           any zero has been taken; with none, the switch's is the first
     * @param maxCorrectionDeg a bigger disagreement is not taken; NaN takes any
     */
    public static Action atSwitchPass(boolean zeroedByHand, boolean zeroed, double disagreeDeg,
                                      double maxCorrectionDeg) {
        if (zeroedByHand || Double.isNaN(disagreeDeg)) return Action.NOTHING;
        if (!zeroed) return Action.ZERO_AT_SWITCH;
        if (!Double.isNaN(maxCorrectionDeg) && Math.abs(disagreeDeg) > maxCorrectionDeg) {
            return Action.SWITCH_DISAGREES;
        }
        return Action.ZERO_AT_SWITCH;
    }
}
