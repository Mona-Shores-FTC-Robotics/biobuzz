package org.firstinspires.ftc.teamcode.subsystems;

import org.firstinspires.ftc.teamcode.util.Alliance;

/**
 * The NECTAR gate at the lane entrance: what the colour sensor's reading says about the piece in
 * front of it ({@link #judge}), the burst that spits a wrong one out, and whether a wrong one may
 * still be in the lane ({@link #suspect()}), which holds the feed gate shut.
 *
 * <p><b>The rule.</b> We may launch only our own alliance's NECTAR; launching the other alliance's
 * is a penalty (the manual's G408, as {@code AutoSim} reads it: a robot takes only POLLEN and its
 * alliance's own NECTAR). So the gate keeps a piece only when the reading is sure of it, and
 * <b>treats every uncertain piece as wrong</b>: losing a POLLEN costs far less than launching the
 * other alliance's NECTAR (mentor, 5 and 22 Nov 2026).
 *
 * <h2>The reading ({@link #judge})</h2>
 *
 * <ul>
 *   <li>A reading the sensor cannot have made of a real piece is dropped before any of this
 *       ({@link #plausible}): 20245 read brightness exactly 1.00 with red at 1.0 and green and blue at
 *       0 or 1, 92 times in its 8 Fruitport matches (21 Nov 2026), each one reading out of an empty
 *       lane. Red 1, green 0, blue 0 is hue 0°, red NECTAR, and twice in Q-20, on BLUE, it fired a
 *       reject at nothing. No real piece read any channel above 0.19 (8030 bright readings).</li>
 *   <li>A piece (brightness above {@code seenAbove}) is {@link Sighting#WRONG} if its hue is in the
 *       other alliance's NECTAR range or within {@code doubtDeg} of it, if it has no hue, or if it
 *       is no nearer a colour we keep (POLLEN, and our own NECTAR unless {@code allNectar}) than the
 *       wrong one: a hue between colours goes to the nearest, and a tie is wrong. Otherwise
 *       {@link Sighting#KEEP}. Not every out-of-range hue is wrong: 19429 read the lane's own
 *       grey-green at 132° to 136°, saturation 0.4, brightness up to 0.16 (Fruitport), 2° past
 *       POLLEN's top and 45° from blue.</li>
 *   <li>A dim reading ({@code faintAbove} up to {@code seenAbove}) in or near the other
 *       alliance's colour, with a real piece's saturation ({@code faintMinSat}), is
 *       {@link Sighting#WRONG_FAINT}: a wrong NECTAR just off the sensor's spot. Q-11 (20245 on
 *       BLUE, Fruitport): after two bursts the red NECTAR sat inside the lane reading 0.018 to
 *       0.022 at 31° to 34° (saturation 0.50 to 0.55), against the empty lane's 0.007 to 0.012 at
 *       100° to 165° (0.33 to 0.42), never above the 0.05 that counted as a piece, and it was
 *       launched 6 s later. A faint sighting only counts while a bright one is recent
 *       ({@code faintWindowMs}) or the lane is already suspect: an empty lane on 20245 once read
 *       0.02 to 0.03 at a blue hue (5 Nov 2026), and that alone must never shut the gate.</li>
 * </ul>
 *
 * <h2>One reject</h2>
 *
 * <ol>
 *   <li><b>Armed:</b> {@code afterReadings} fresh readings in a row are {@link Sighting#WRONG} (or
 *       a counted {@link Sighting#WRONG_FAINT}). One, now: the sensor makes a new reading about every
 *       100 ms and the subsystem reads every 50, so two in a row were most often one reading twice
 *       (Fruitport), and a NECTAR passing the entrance is under the sensor for little more than
 *       that (20245, Q-28, 0.13 s).</li>
 *   <li><b>Burst</b> ({@link Phase#BURST}), {@code burstMs}: the roller, the stars and the prefeed
 *       back out, whatever the sensor sees meanwhile.</li>
 *   <li><b>Hold-off</b> ({@link Phase#HOLD_OFF}), {@code holdOffMs}: they stop, so the piece rolls
 *       clear instead of being taken straight back in. A piece back under the sensor meanwhile is
 *       burst again at once (5 Nov 2026).</li>
 *   <li><b>Re-arm only after a different reading</b> (nothing, or a piece to keep): one burst per
 *       arrival, never a chain while the robot sits at a FLOWER.</li>
 * </ol>
 *
 * <p>A backstop: more than {@code maxBursts} bursts within {@code windowMs} and it stops bursting
 * ({@link #gaveUp()}) until a different reading re-arms it. The lane stays suspect meanwhile.
 *
 * <h2>Suspect: a wrong NECTAR may be in the lane</h2>
 *
 * <p>One sensor at the entrance cannot see which way a piece went when it leaves the spot. A burst
 * runs the lane outward, yet in Q-11 the NECTAR ended up inside. So from the first wrong sighting
 * the lane is {@link #suspect()}, and the feed gate stays shut ({@code FeedGateSubsystem}), until
 * one of:
 *
 * <ul>
 *   <li><b>Seen leaving outward:</b> the last wrong reading and the first clean one after it were
 *       both taken in the same outward run (a burst or an eject), and the sensor then reads clean
 *       for {@code clearMs}. Q-28 (20245 on RED): a BLUE NECTAR read 0.92 to 0.13 through the
 *       burst and was gone before it ended.</li>
 *   <li><b>An eject:</b> the lane runs outward for {@code ejectClearMs} with no bright wrong
 *       reading, which carries anything in it out past the sensor. Faint readings do not stop
 *       this one, so the driver's eject always clears the lane.</li>
 * </ul>
 *
 * <p>With {@code allNectar} it treats both NECTAR colours as wrong, ours too, and keeps only POLLEN:
 * a robot locked out of NECTAR ({@code ColorSensorSubsystem.rejectAllNectar}). Then the alliance does
 * not matter. With no alliance and not {@code allNectar}, nothing is wrong.
 *
 * <p>Pure: no SDK, no allocation, so a test can drive it.
 */
public final class NectarReject {

    /** What the reject is doing. */
    public enum Phase { IDLE, BURST, HOLD_OFF }

    /** What one reading says is at the entrance ({@link #judge}). */
    public enum Sighting {
        /** No piece: dark, or dim and not the wrong colour. */
        NOTHING,
        /** A piece the reading is sure is POLLEN or our own NECTAR. */
        KEEP,
        /** A piece in the other alliance's colour, near it, or of no sure colour. */
        WRONG,
        /** Dim, in or near the other alliance's colour, with a piece's saturation: a wrong NECTAR off the spot. */
        WRONG_FAINT
    }

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

    private boolean suspect;
    private boolean justSuspected;
    private boolean justCleared;
    /** How the latest clear came: true an eject, false seen leaving outward. */
    private boolean clearedByEject;
    private long lastBrightWrongMs = Long.MIN_VALUE / 2;
    /** Outward runs so far; a new one starts each time the lane starts running outward. */
    private long outwardRun;
    private boolean wasOutward;
    /** The outward run the last wrong reading was taken in; -1 when the lane was not running outward. */
    private long lastWrongRun = -1;
    /** Since the first clean reading after the last wrong one, ms; MIN while none yet. */
    private long cleanSinceMs = Long.MIN_VALUE;
    private boolean leftOutward;
    /** Since when the lane has run outward with no bright wrong reading, ms; MIN while not. */
    private long ejectSinceMs = Long.MIN_VALUE;
    private Sighting lastSighting = Sighting.NOTHING;

    /** The NECTAR colour that belongs to the other alliance; null for UNKNOWN. */
    public static LaneTracker.Piece enemyNectar(Alliance ours) {
        if (ours == Alliance.RED) return LaneTracker.Piece.BLUE_NECTAR;
        if (ours == Alliance.BLUE) return LaneTracker.Piece.RED_NECTAR;
        return null;
    }

    /**
     * Whether a reading can be of a real piece: brightness under 0.999 and every colour channel
     * under {@code maxChannel}. NaN anywhere is not. A real piece reads at most 0.19 on a channel at
     * the sensor's default gain while its brightness reaches 0.99; 20245's glitches read 1.0
     * (Fruitport, 21 Nov 2026). {@code maxChannel} NaN: only the brightness test.
     */
    public static boolean plausible(double red, double green, double blue, double brightness, double maxChannel) {
        if (Double.isNaN(red) || Double.isNaN(green) || Double.isNaN(blue) || Double.isNaN(brightness)) return false;
        if (!(brightness < 0.999)) return false;
        if (Double.isNaN(maxChannel)) return true;
        return Math.max(red, Math.max(green, blue)) < maxChannel;
    }

    /**
     * What one plausible reading says is at the entrance (class comment, "The reading"). Pure.
     *
     * @param hueDeg      the reading's hue, degrees; NaN for none
     * @param saturation  0 to 1
     * @param brightness  the clear channel
     * @param ours        our alliance; UNKNOWN with not {@code allNectar}: nothing is ever wrong
     * @param allNectar   both NECTAR colours are wrong
     * @param seenAbove   a piece above this brightness; NaN: nothing is ever wrong
     * @param faintAbove  a faint sighting from this brightness up; NaN: none
     * @param faintMinSat a faint sighting needs at least this saturation
     * @param doubtDeg    a hue this close to the wrong NECTAR's range is wrong too; NaN or negative as 0
     */
    public static Sighting judge(double hueDeg, double saturation, double brightness, Alliance ours, boolean allNectar,
                                 double seenAbove, double faintAbove, double faintMinSat, double doubtDeg,
                                 double pollenMin, double pollenMax, double redMin, double redMax,
                                 double blueMin, double blueMax) {
        if (Double.isNaN(seenAbove) || Double.isNaN(brightness)) return Sighting.NOTHING;
        boolean redWrong = allNectar || ours == Alliance.BLUE;
        boolean blueWrong = allNectar || ours == Alliance.RED;
        boolean present = brightness > seenAbove;
        if (!redWrong && !blueWrong) return present ? Sighting.KEEP : Sighting.NOTHING;
        double doubt = doubtDeg > 0 ? doubtDeg : 0.0;
        boolean nearWrong = !Double.isNaN(hueDeg)
                && ((redWrong && distanceToRange(hueDeg, redMin, redMax) <= doubt)
                || (blueWrong && distanceToRange(hueDeg, blueMin, blueMax) <= doubt));
        if (present) {
            if (Double.isNaN(hueDeg) || nearWrong) return Sighting.WRONG;
            // Outside every range: the nearest colour decides, and a tie is wrong.
            double kept = distanceToRange(hueDeg, pollenMin, pollenMax);
            if (!allNectar) {
                kept = Math.min(kept, ours == Alliance.RED ? distanceToRange(hueDeg, redMin, redMax)
                        : distanceToRange(hueDeg, blueMin, blueMax));
            }
            double wrong = Math.min(redWrong ? distanceToRange(hueDeg, redMin, redMax) : Double.POSITIVE_INFINITY,
                    blueWrong ? distanceToRange(hueDeg, blueMin, blueMax) : Double.POSITIVE_INFINITY);
            return kept < wrong ? Sighting.KEEP : Sighting.WRONG;
        }
        if (nearWrong && brightness >= faintAbove && saturation >= faintMinSat) return Sighting.WRONG_FAINT;
        return Sighting.NOTHING;
    }

    /**
     * How far a hue is from a range, degrees round the circle: 0 inside it (min above max wraps
     * through 0). Infinite if the range is unset (NaN). Pure.
     */
    static double distanceToRange(double h, double min, double max) {
        if (Double.isNaN(min) || Double.isNaN(max)) return Double.POSITIVE_INFINITY;
        if (LaneTracker.inRange(h, min, max)) return 0.0;
        return Math.min(circular(h, min), circular(h, max));
    }

    private static double circular(double a, double b) {
        double d = Math.abs(a - b) % 360.0;
        return d > 180.0 ? 360.0 - d : d;
    }

    /**
     * One loop.
     *
     * @param nowMs         a monotonic clock, ms
     * @param fresh         whether a plausible reading was taken this loop; {@code seen} is ignored if not
     * @param seen          that reading, from {@link #judge}
     * @param outward       whether the lane runs outward this loop (a burst, or an eject)
     * @param enabled       the switch; off ends a reject at once, never starts one, and clears suspect
     * @param afterReadings readings in a row that start a burst; below 1 counts as 1
     * @param burstMs       how long a burst runs, ms; NaN or not positive: off, as {@code enabled}
     * @param holdOffMs     how long the intake stays stopped after a burst, ms; NaN or not positive: none
     * @param maxBursts     bursts allowed within {@code windowMs} before it gives up; below 1: no cap
     * @param windowMs      the cap's window, ms; NaN or not positive: no cap
     * @param faintWindowMs how long after a bright wrong reading a faint one counts, ms
     * @param clearMs       clean readings for this long after the piece was seen leaving outward clear suspect, ms
     * @param ejectClearMs  the lane outward this long with no bright wrong reading clears suspect, ms
     */
    public void step(long nowMs, boolean fresh, Sighting seen, boolean outward, boolean enabled,
                     int afterReadings, double burstMs, double holdOffMs, int maxBursts, double windowMs,
                     double faintWindowMs, double clearMs, double ejectClearMs) {
        justStarted = false;
        justGaveUp = false;
        justSuspected = false;
        justCleared = false;
        if (!enabled || !(burstMs > 0)) {
            phase = Phase.IDLE;
            matching = 0;
            armed = true;
            gaveUp = false;
            suspect = false;
            lastSighting = Sighting.NOTHING;
            return;
        }
        if (outward && !wasOutward) outwardRun++;
        wasOutward = outward;
        if (phase == Phase.BURST && nowMs - phaseStartMs >= burstMs) {
            // The hold-off starts when the burst ended, not when a slow loop noticed.
            phase = Phase.HOLD_OFF;
            phaseStartMs += (long) burstMs;
        }
        if (phase == Phase.HOLD_OFF && !(nowMs - phaseStartMs < holdOffMs)) {
            phase = Phase.IDLE;
        }
        if (!outward) {
            ejectSinceMs = Long.MIN_VALUE;
        } else if (ejectSinceMs == Long.MIN_VALUE) {
            ejectSinceMs = nowMs;
        }
        if (fresh) {
            lastSighting = seen == null ? Sighting.NOTHING : seen;
            track(nowMs, outward, faintWindowMs);
        }
        if (suspect && clears(nowMs, outward, clearMs, ejectClearMs)) {
            suspect = false;
            justCleared = true;
        }
        if (!fresh) return;
        boolean wrong = counts(lastSighting);
        if (phase == Phase.BURST) {
            // A different reading during the burst means the piece has left the spot.
            if (!wrong) armed = true;
            return;
        }
        if (!wrong) {
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

    /** Suspect's bookkeeping for one fresh reading: a wrong one sets it, a clean one may start its clearing. */
    private void track(long nowMs, boolean outward, double faintWindowMs) {
        if (lastSighting == Sighting.WRONG) lastBrightWrongMs = nowMs;
        if (lastSighting == Sighting.WRONG_FAINT
                && !(suspect || nowMs - lastBrightWrongMs <= faintWindowMs)) {
            // A faint sighting with no wrong piece recently seen: the lane's own light, not a piece.
            lastSighting = Sighting.NOTHING;
        }
        if (lastSighting == Sighting.WRONG) ejectSinceMs = outward ? nowMs : Long.MIN_VALUE;
        if (counts(lastSighting)) {
            if (!suspect) justSuspected = true;
            suspect = true;
            lastWrongRun = outward ? outwardRun : -1;
            cleanSinceMs = Long.MIN_VALUE;
            leftOutward = false;
        } else if (suspect && cleanSinceMs == Long.MIN_VALUE) {
            cleanSinceMs = nowMs;
            leftOutward = outward && lastWrongRun == outwardRun;
        }
    }

    private boolean clears(long nowMs, boolean outward, double clearMs, double ejectClearMs) {
        if (leftOutward && cleanSinceMs != Long.MIN_VALUE && nowMs - cleanSinceMs >= Math.max(0.0, clearMs)) {
            clearedByEject = false;
            return true;
        }
        if (outward && ejectSinceMs != Long.MIN_VALUE && ejectClearMs > 0 && nowMs - ejectSinceMs >= ejectClearMs) {
            clearedByEject = true;
            return true;
        }
        return false;
    }

    private static boolean counts(Sighting s) {
        return s == Sighting.WRONG || s == Sighting.WRONG_FAINT;
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

    /** Whether it has stopped bursting after too many bursts, until a different reading re-arms it. */
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

    /**
     * Whether a wrong NECTAR may be in the lane (class comment, "Suspect"): from a wrong sighting
     * until it was seen leaving outward or an eject ran. The feed gate stays shut meanwhile.
     */
    public boolean suspect() {
        return suspect;
    }

    /** Whether the last {@link #step} made the lane suspect, for one match-log event. */
    public boolean justSuspected() {
        return justSuspected;
    }

    /** Whether the last {@link #step} cleared suspect, for one match-log event. */
    public boolean justCleared() {
        return justCleared;
    }

    /** Whether the latest clear came from an eject (else the piece was seen leaving outward). */
    public boolean clearedByEject() {
        return clearedByEject;
    }

    /** The latest fresh reading's verdict, a faint one not counted already turned to NOTHING. */
    public Sighting lastSighting() {
        return lastSighting;
    }
}
