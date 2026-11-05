package org.firstinspires.ftc.teamcode.subsystems;

import org.firstinspires.ftc.teamcode.util.Alliance;

/**
 * Whether to spit out the piece at the lane entrance because it is the other alliance's NECTAR.
 * Holding one is a big penalty, so the intake backs it out in a short burst the moment the colour
 * sensor sees it (the mentor's call, 5 Nov 2026).
 *
 * <p>The sensor looks at the entrance, just past the star wheels, so the piece it sees is the
 * newest, still at the roller and the stars: reversing those two alone, with the prefeed still,
 * pushes that one piece out and leaves the rest of the lane where it is
 * ({@code IntakeSubsystem}, {@code PrefeedSubsystem}).
 *
 * <p>A burst starts after {@code afterReadings} fresh readings in a row show a piece (brightness
 * above {@code presentAbove}) in the other alliance's NECTAR colour, so one odd reading cannot
 * throw out our own piece. It lasts {@code burstMs}, whatever the sensor sees meanwhile; if the
 * piece is still there when it ends (or the roller takes it straight back in), the next readings
 * start another. With no alliance chosen it never fires.
 *
 * <p>Pure: no SDK, no allocation, so a test can drive it.
 */
public final class NectarReject {

    private int matching;
    private boolean rejecting;
    private long startedMs;
    private long bursts;
    private boolean justStarted;

    /** The NECTAR colour that belongs to the other alliance; null for UNKNOWN. */
    public static LaneTracker.Piece enemyNectar(Alliance ours) {
        if (ours == Alliance.RED) return LaneTracker.Piece.BLUE_NECTAR;
        if (ours == Alliance.BLUE) return LaneTracker.Piece.RED_NECTAR;
        return null;
    }

    /**
     * One loop.
     *
     * @param nowMs         a monotonic clock, ms
     * @param fresh         whether the sensor was read this loop; brightness and piece are ignored if not
     * @param brightness    the reading's brightness (the clear channel)
     * @param piece         the reading's colour, from {@link LaneTracker#classify}
     * @param ours          our alliance; UNKNOWN never rejects
     * @param enabled       the switch; off ends a burst at once and never starts one
     * @param presentAbove  a piece is there above this brightness; NaN never rejects
     * @param afterReadings readings in a row that start a burst; below 1 counts as 1
     * @param burstMs       how long a burst runs, ms; NaN or not positive never rejects
     */
    public void step(long nowMs, boolean fresh, double brightness, LaneTracker.Piece piece, Alliance ours,
                     boolean enabled, double presentAbove, int afterReadings, double burstMs) {
        justStarted = false;
        LaneTracker.Piece enemy = enemyNectar(ours);
        if (!enabled || enemy == null || !(burstMs > 0) || Double.isNaN(presentAbove)) {
            rejecting = false;
            matching = 0;
            return;
        }
        if (rejecting && nowMs - startedMs >= burstMs) {
            rejecting = false;
        }
        if (rejecting || !fresh) return;
        boolean seen = piece == enemy && brightness > presentAbove;
        matching = seen ? matching + 1 : 0;
        if (matching >= Math.max(1, afterReadings)) {
            rejecting = true;
            startedMs = nowMs;
            matching = 0;
            bursts++;
            justStarted = true;
        }
    }

    /** Whether a burst is running as of the last {@link #step}. */
    public boolean rejecting() {
        return rejecting;
    }

    /** Whether the last {@link #step} started a burst, for one match-log event each. */
    public boolean justStarted() {
        return justStarted;
    }

    /** Bursts started since this was built. */
    public long bursts() {
        return bursts;
    }
}
