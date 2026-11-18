package org.firstinspires.ftc.teamcode.opmodes;

/**
 * What one press of the driver's Circle does, decided as it goes down from the operator's turret
 * mode (Triangle), not from the robot's config (mentor, 18 Nov 2026). Pure, so a test drives it.
 *
 * <ul>
 *   <li><b>{@link Kind#FIRE}</b>, the turret AIMing: Circle fires as the operator's FIRE does (same
 *       spin-up, gates and feed). The turret points.</li>
 *   <li><b>{@link Kind#AIM_AND_FIRE}</b>, the turret LOCKED (or a robot whose turret is locked
 *       forward): Circle turns the robot onto the CELL with the chassis aim ({@link ChassisAimTarget},
 *       camera first) and fires from wherever it is. The launcher spins up as the turn starts, and a
 *       feed starts only once the robot is on the CELL ({@link #mayFeed}). Square's volley without
 *       the drive to a spot.</li>
 * </ul>
 * Letting go stops the turn and the feed. A stick moved after the press ({@link FreshSticks})
 * hands the turn back to the driver and ends this press's fire too, until Circle is let go.
 */
final class CirclePress {

    enum Kind { FIRE, AIM_AND_FIRE }

    private CirclePress() {}

    /** The press's kind, from the turret as Circle goes down. */
    static Kind kindFor(boolean turretLockedForward, boolean turretAiming) {
        return !turretLockedForward && turretAiming ? Kind.FIRE : Kind.AIM_AND_FIRE;
    }

    /** Whether the press asks for a shot this loop: held, and for an aim-and-fire press not handed back. */
    static boolean wantsFire(boolean held, Kind kind, boolean handedBack) {
        return held && (kind == Kind.FIRE || !handedBack);
    }

    /**
     * Whether an aim-and-fire press may start a feed: the aim gate passes (on the CELL within the
     * tolerance; an unknown aim is not on it) and the launcher is at speed, whatever the gates the
     * operator's FIRE enforces. A plain fire press is the operator's FIRE: {@code gatesOpen}.
     */
    static boolean mayFeed(Kind kind, boolean gatesOpen, FireGates.Verdict aim, boolean atSpeed) {
        if (kind == Kind.FIRE) return gatesOpen;
        return gatesOpen && aim == FireGates.Verdict.PASS && atSpeed;
    }
}
