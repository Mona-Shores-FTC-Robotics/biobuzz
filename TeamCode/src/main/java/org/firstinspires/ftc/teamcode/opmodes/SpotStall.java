package org.firstinspires.ftc.teamcode.opmodes;

/**
 * The launch-spot button's stall (mentor, 23 Nov 2026): a Square press that cannot reach its spot,
 * blocked by a robot or a piece, fires from where it stands instead of giving up
 * ({@link LaunchSpotDrive#fireOnStall}). Two pure parts, so a test drives them:
 * <ul>
 *   <li>{@link #update}: the stall itself. Still driving, more than {@code toleranceIn} from the
 *       spot, slower than {@code speedInPerSec} and turning slower than {@code turnDegPerSec}, and
 *       closing on the spot by less than {@code progressIn}, all for {@code stallS} on end. Any loop
 *       that breaks one of them starts the wait again, so a robot creeping in slowly but getting
 *       there never stalls.</li>
 *   <li>{@link #refusal}: whether a stalled press may fire, and if not, the reason for the Match
 *       page and the log. The shot itself is Circle's in AIM, handed over (BasicDriveTeleOp), so
 *       the aim, the speed table, the arc turn, the at-speed and aim gates, the NECTAR gate and the
 *       shot count are Circle's own; this only decides whether to hand over.</li>
 * </ul>
 */
final class SpotStall {

    private double sinceS = Double.NaN;
    private double remainingAtSinceIn = Double.NaN;

    /** A press starts: no stall yet. */
    void reset() {
        sinceS = Double.NaN;
        remainingAtSinceIn = Double.NaN;
    }

    /**
     * One step while the press is driving; true once the robot has been stalled for {@code stallS}.
     * A NaN speed, turn or distance is not a stall (no Follower, no pose: nothing to judge by).
     */
    boolean update(double nowS, double remainingIn, double speedInPerSec, double turnDegPerSec,
                   double stallS, double toleranceIn, double slowInPerSec, double slowTurnDegPerSec,
                   double progressIn) {
        boolean slow = remainingIn > toleranceIn
                && Math.abs(speedInPerSec) < slowInPerSec
                && Math.abs(turnDegPerSec) < slowTurnDegPerSec;
        if (!slow) {
            reset();
            return false;
        }
        if (Double.isNaN(sinceS) || remainingAtSinceIn - remainingIn > progressIn) {
            // The first slow loop, or still getting closer: the wait starts here.
            sinceS = nowS;
            remainingAtSinceIn = remainingIn;
            return false;
        }
        return nowS - sinceS >= stallS;
    }

    /** How long the current stall has lasted, seconds; 0 when not stalled. */
    double stalledForS(double nowS) {
        return Double.isNaN(sinceS) ? 0.0 : nowS - sinceS;
    }

    /**
     * Whether a stalled press may fire from where it is: null if it may, else why not, a few words
     * ("out of range"). In order: the turret aims (the shot is Circle's in AIM), the pose is
     * referenced, the robot is on our side of the centre line (a CELL is scored only from its own
     * end), the range to the near CELL is inside the speed table's measured rows, and the aim is
     * inside the firing arc or the arc turn will bring it in.
     *
     * @param turretAims       the turret is in AIM (not LOCKED, not locked forward)
     * @param referenced       {@code robot.drive.poseReferenced()}
     * @param pastLineIn       the robot's centre past the centre line onto the other side, inches; negative on ours
     * @param rangeIn          the range to the near CELL the shot would use, inches; NaN unknown
     * @param rangeMeasured    {@code rangeIn} is inside the speed table's measured rows
     * @param aimDeg           the turret's aim at the CELL; NaN with none
     * @param aimInsideArc     that aim is inside the firing arc
     * @param turnsIntoArc     {@code TeleOpConfig.circleTurnsIntoArc}: Circle turns an aim behind the arc into it
     */
    static String refusal(boolean turretAims, boolean referenced, double pastLineIn, double rangeIn,
                          boolean rangeMeasured, double aimDeg, boolean aimInsideArc, boolean turnsIntoArc) {
        if (!turretAims) return "turret not aiming (AIM needed)";
        if (!referenced) return "pose not referenced";
        if (!(pastLineIn < 0.0)) return "past the centre line";
        if (Double.isNaN(rangeIn)) return "range unknown";
        if (!rangeMeasured) return String.format(java.util.Locale.US, "out of range (%.0f in)", rangeIn);
        if (Double.isNaN(aimDeg)) return "no aim at the CELL";
        if (!aimInsideArc && !turnsIntoArc) return "aim outside the firing arc";
        return null;
    }

    /**
     * A Panels number guarded: NaN or infinite is {@code fallback}, and it stays in [{@code lo},
     * {@code hi}], so a typo makes a slightly odd stall, never one at the first slow loop or never.
     */
    static double guarded(double value, double fallback, double lo, double hi) {
        if (Double.isNaN(value) || Double.isInfinite(value)) return fallback;
        return Math.max(lo, Math.min(hi, value));
    }
}
