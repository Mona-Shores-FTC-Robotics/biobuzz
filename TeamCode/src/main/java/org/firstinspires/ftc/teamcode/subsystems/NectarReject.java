package org.firstinspires.ftc.teamcode.subsystems;

import org.firstinspires.ftc.teamcode.util.Alliance;

/**
 * Whether to spit out the piece at the lane entrance because it is the other alliance's NECTAR.
 * Holding one is a big penalty, so the intake backs it out in a short burst the moment the colour
 * sensor sees it (the mentor's call, 5 Nov 2026).
 *
 * <p>A burst backs the whole lane out: the roller, the stars and the prefeed, as an eject does
 * ({@code IntakeSubsystem}, {@code PrefeedSubsystem}). Reversing only the roller and the stars, the
 * prefeed still, kept the rest of the lane, but a NECTAR already past the stars stayed in (20245,
 * 5 Nov 2026); losing a POLLEN costs far less than keeping the NECTAR (mentor, 5 Nov 2026).
 *
 * <h2>One reject</h2>
 *
 * <ol>
 *   <li><b>Armed:</b> {@code afterReadings} fresh readings in a row show a piece (brightness above
 *       {@code seenAbove}) in the other alliance's NECTAR colour, so one odd reading cannot
 *       throw out our own piece. Then:</li>
 *   <li><b>Burst</b> ({@link Phase#BURST}), {@code burstMs}: the roller and the stars back out,
 *       whatever the sensor sees meanwhile.</li>
 *   <li><b>Hold-off</b> ({@link Phase#HOLD_OFF}), {@code holdOffMs}: the roller and the stars stop,
 *       so the piece rolls clear instead of being taken straight back in. The intake is asked to
 *       take in almost all the time (biobuzz#456), so without this a burst would hand the piece
 *       back to the roller. A piece that rolls back under the sensor meanwhile is burst again at
 *       once, without waiting for the hold-off to end (5 Nov 2026: a NECTAR the burst left at the
 *       mouth rolled back during the hold-off, and the roller took it in as the hold-off ended).</li>
 *   <li><b>Re-arm only after a different reading:</b> a fresh reading that is not the other
 *       alliance's NECTAR (nothing there, POLLEN, ours). A wrong piece still under the sensor
 *       after the hold-off is not rejected again until the sensor has seen it go: one burst per
 *       arrival, never a chain while the robot sits at a FLOWER.</li>
 * </ol>
 *
 * <p>A backstop: more than {@code maxBursts} bursts within {@code windowMs} and it stops
 * ({@link #gaveUp()}) until a different reading re-arms it, so a piece wedged where the burst
 * cannot clear it does not run the intake backwards for the rest of the match. With no alliance
 * chosen, or the switch off, it never fires.
 *
 * <p>With {@code allNectar} it rejects both NECTAR colours, ours too, and keeps only POLLEN: a
 * robot locked out of NECTAR ({@code ColorSensorSubsystem.rejectAllNectar}). Then the alliance does
 * not matter, so it fires even with none chosen.
 *
 * <p>Pure: no SDK, no allocation, so a test can drive it.
 */
public final class NectarReject {

    /** What the reject is doing. */
    public enum Phase { IDLE, BURST, HOLD_OFF }

    /** The most bursts kept for the cap; {@code maxBursts} above this counts as this. */
    static final int MAX_TRACKED = 8;

    private Phase phase = Phase.IDLE;
    private long phaseStartMs;
    private int matching;
    /** Whether a new arrival may start a burst: false from a burst until a different reading. */
    private boolean armed = true;
    private boolean gaveUp;
    private boolean justGaveUp;
    private long bursts;
    private boolean justStarted;
    /** Start times of the latest bursts, a ring, for the cap. */
    private final long[] recent = new long[MAX_TRACKED];
    private int recentCount;
    private int recentNext;

    /** The NECTAR colour that belongs to the other alliance; null for UNKNOWN. */
    public static LaneTracker.Piece enemyNectar(Alliance ours) {
        if (ours == Alliance.RED) return LaneTracker.Piece.BLUE_NECTAR;
        if (ours == Alliance.BLUE) return LaneTracker.Piece.RED_NECTAR;
        return null;
    }

    /**
     * Whether {@code piece} is one to spit out: the other alliance's NECTAR, or with
     * {@code allNectar} either NECTAR (a robot locked to POLLEN only, whatever the alliance).
     */
    public static boolean rejects(LaneTracker.Piece piece, Alliance ours, boolean allNectar) {
        if (allNectar && (piece == LaneTracker.Piece.RED_NECTAR || piece == LaneTracker.Piece.BLUE_NECTAR)) {
            return true;
        }
        LaneTracker.Piece enemy = enemyNectar(ours);
        return enemy != null && piece == enemy;
    }

    /**
     * One loop.
     *
     * @param nowMs         a monotonic clock, ms
     * @param fresh         whether the sensor was read this loop; brightness and piece are ignored if not
     * @param brightness    the reading's brightness (the clear channel)
     * @param piece         the reading's colour, from {@link LaneTracker#classify}
     * @param ours          our alliance; UNKNOWN never rejects, unless {@code allNectar}
     * @param allNectar     reject both NECTAR colours, ours too; the alliance no longer matters
     * @param enabled       the switch; off ends a reject at once and never starts one
     * @param seenAbove     a piece is there above this brightness; NaN never rejects. The colour
     *                      sensor passes its {@code leaveBelow}, not its {@code presentAbove}: a
     *                      NECTAR at the edge of the spot reads 0.07 to 0.09 against an empty
     *                      lane's 0.02 (5 Nov 2026), and it is still the wrong piece
     * @param afterReadings readings in a row that start a burst; below 1 counts as 1
     * @param burstMs       how long a burst runs, ms; NaN or not positive never rejects
     * @param holdOffMs     how long the intake stays stopped after a burst, ms; NaN or not positive: none
     * @param maxBursts     bursts allowed within {@code windowMs} before it gives up; below 1: no cap
     * @param windowMs      the cap's window, ms; NaN or not positive: no cap
     */
    public void step(long nowMs, boolean fresh, double brightness, LaneTracker.Piece piece, Alliance ours,
                     boolean allNectar, boolean enabled, double seenAbove, int afterReadings, double burstMs,
                     double holdOffMs, int maxBursts, double windowMs) {
        justStarted = false;
        justGaveUp = false;
        if (!enabled || (enemyNectar(ours) == null && !allNectar) || !(burstMs > 0) || Double.isNaN(seenAbove)) {
            phase = Phase.IDLE;
            matching = 0;
            armed = true;
            gaveUp = false;
            return;
        }
        if (phase == Phase.BURST && nowMs - phaseStartMs >= burstMs) {
            // The hold-off starts when the burst ended, not when a slow loop noticed.
            phase = Phase.HOLD_OFF;
            phaseStartMs += (long) burstMs;
        }
        if (phase == Phase.HOLD_OFF && !(nowMs - phaseStartMs < holdOffMs)) {
            phase = Phase.IDLE;
        }
        if (!fresh) return;
        boolean seen = rejects(piece, ours, allNectar) && brightness > seenAbove;
        if (phase == Phase.BURST) {
            // A different reading during the burst means the piece has gone.
            if (!seen) armed = true;
            return;
        }
        if (!seen) {
            armed = true;
            gaveUp = false;
            matching = 0;
            return;
        }
        // Idle, or in the hold-off: the piece is back under the sensor, so it bursts again now.
        if (!armed) return;
        matching++;
        if (matching < Math.max(1, afterReadings)) return;
        matching = 0;
        armed = false;
        if (capReached(nowMs, maxBursts, windowMs)) {
            gaveUp = true;
            justGaveUp = true;
            return;
        }
        phase = Phase.BURST;
        phaseStartMs = nowMs;
        bursts++;
        justStarted = true;
        recent[recentNext] = nowMs;
        recentNext = (recentNext + 1) % MAX_TRACKED;
        if (recentCount < MAX_TRACKED) recentCount++;
    }

    /** Whether {@code maxBursts} bursts have already started within {@code windowMs} of now. */
    private boolean capReached(long nowMs, int maxBursts, double windowMs) {
        if (maxBursts < 1 || !(windowMs > 0)) return false;
        int cap = Math.min(maxBursts, MAX_TRACKED);
        int within = 0;
        for (int i = 0; i < recentCount; i++) {
            if (nowMs - recent[i] < windowMs) within++;
        }
        return within >= cap;
    }

    /** What the reject is doing as of the last {@link #step}. */
    public Phase phase() {
        return phase;
    }

    /** Whether a burst is running: the roller and the stars back out. */
    public boolean rejecting() {
        return phase == Phase.BURST;
    }

    /** Whether the hold-off after a burst is running: the roller and the stars stay stopped. */
    public boolean holdingOff() {
        return phase == Phase.HOLD_OFF;
    }

    /** Whether the last {@link #step} started a burst, for one match-log event each. */
    public boolean justStarted() {
        return justStarted;
    }

    /** Whether it has stopped rejecting after too many bursts, until a different reading re-arms it. */
    public boolean gaveUp() {
        return gaveUp;
    }

    /** Whether the last {@link #step} gave up, for one match-log event. */
    public boolean justGaveUp() {
        return justGaveUp;
    }

    /** Bursts started since this was built. */
    public long bursts() {
        return bursts;
    }
}
