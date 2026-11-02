package org.firstinspires.ftc.teamcode.subsystems;

import java.util.Locale;

/**
 * What is in the transfer lane, slot by slot, from one colour sensor at its entrance. Data only:
 * nothing decides anything from it yet. Its guess goes to the match log next to the raw readings,
 * so a session with known loads shows whether it can be trusted.
 *
 * <p>The lane holds {@link #SLOTS} pieces behind the closed gate. The sensor looks down at the
 * entrance, just past the star wheels, which is also the fourth slot. So the tracker never sees a
 * slot; it keeps a queue:
 *
 * <ul>
 *   <li>a piece <b>arriving</b> under the sensor (brightness rises above {@code presentAbove})
 *       while the lane runs inward joins the back, as the colour it shows;</li>
 *   <li>while it stays there, the brightest reading names its colour (a piece at the edge of the
 *       spot reads dimmer than one under it);</li>
 *   <li>a <b>shot</b> takes the front piece;</li>
 *   <li>a piece <b>leaving</b> under the sensor while the lane runs outward (eject, unjam) takes
 *       the back piece.</li>
 * </ul>
 *
 * <p>Presence has hysteresis: it starts above {@code presentAbove} and ends below
 * {@code leaveBelow}, so a piece wobbling at the threshold counts once. The first reading only
 * sets whether something is already there, so a preload sitting under the sensor at INIT is not
 * counted again. A count that goes past full or below empty is a counting error; it is kept at the
 * limit and counted ({@link #overflows()}, {@link #underflows()}) so a log shows how often.
 *
 * <p>Pure: no SDK, no allocation per loop, so a test can drive it.
 */
public final class LaneTracker {

    /** What a slot holds. */
    public enum Piece { POLLEN, NECTAR, UNKNOWN }

    /** Pieces the lane holds behind the closed gate. */
    public static final int SLOTS = 4;

    /** An Autonomous starts with this many POLLEN loaded (the drive team always loads four). */
    public static final int AUTO_PRELOAD = 4;

    /** slots[0] is the front, the next piece to fire. */
    private final Piece[] slots = new Piece[SLOTS];
    private int count;
    private boolean baselineSet;
    private boolean present;
    /** The back slot is the piece under the sensor now, so its colour may still improve. */
    private boolean namingBack;
    private double brightest = Double.NEGATIVE_INFINITY;
    private int overflows;
    private int underflows;
    private String event;

    /** Declare what is loaded, front to back all {@code piece}, before the first reading. */
    public void fill(Piece piece, int n) {
        count = Math.max(0, Math.min(SLOTS, n));
        for (int i = 0; i < SLOTS; i++) slots[i] = i < count ? piece : null;
        namingBack = false;
    }

    /**
     * One loop.
     *
     * @param fresh        whether the sensor was read this loop; brightness and piece are ignored if not
     * @param brightness   the reading's brightness (the clear channel)
     * @param piece        the reading's colour, from {@link #classify}
     * @param outward      the lane runs toward the intake this loop (eject, unjam)
     * @param shot         the launcher counted a shot this loop
     * @param presentAbove a piece is there above this brightness; NaN turns arrival and leaving off
     * @param leaveBelow   and gone below this; NaN means the same as {@code presentAbove}
     */
    public void step(boolean fresh, double brightness, Piece piece, boolean outward, boolean shot,
                     double presentAbove, double leaveBelow) {
        event = null;
        if (shot) {
            if (count > 0) {
                removeFront();
                event = String.format(Locale.US, "lane: shot, %d left", count);
            } else {
                underflows++;
                event = "lane: shot with the lane counted empty (a count was missed)";
            }
        }
        if (!fresh || Double.isNaN(presentAbove) || Double.isNaN(brightness)) return;
        double leave = Double.isNaN(leaveBelow) ? presentAbove : Math.min(leaveBelow, presentAbove);
        boolean now = present ? brightness > leave : brightness > presentAbove;
        if (!baselineSet) {
            baselineSet = true;
            present = now;
            return;
        }
        if (now && !present) {
            if (!outward) {
                if (count < SLOTS) {
                    slots[count++] = piece;
                    namingBack = true;
                    brightest = brightness;
                    event = String.format(Locale.US, "lane: %s in, %d in the lane", piece, count);
                } else {
                    overflows++;
                    namingBack = false;
                    event = "lane: a piece arrived with the lane counted full (a count was wrong)";
                }
            }
        } else if (now) {
            if (namingBack && count > 0 && brightness > brightest) {
                slots[count - 1] = piece;
                brightest = brightness;
            }
        } else if (present) {
            namingBack = false;
            brightest = Double.NEGATIVE_INFINITY;
            if (outward) {
                if (count > 0) {
                    slots[--count] = null;
                    event = String.format(Locale.US, "lane: a piece out past the sensor, %d left", count);
                } else {
                    underflows++;
                    event = "lane: a piece out past the sensor with the lane counted empty";
                }
            }
        }
        present = now;
    }

    private void removeFront() {
        for (int i = 1; i < count; i++) slots[i - 1] = slots[i];
        slots[--count] = null;
        if (count == 0) namingBack = false;
    }

    /**
     * Which piece a hue is: inside POLLEN's range, inside NECTAR's, or neither. A range whose min is
     * above its max wraps through 0° (a red). NaN bounds or a NaN hue give UNKNOWN.
     */
    public static Piece classify(double hueDeg, double pollenMin, double pollenMax,
                                 double nectarMin, double nectarMax) {
        if (inRange(hueDeg, pollenMin, pollenMax)) return Piece.POLLEN;
        if (inRange(hueDeg, nectarMin, nectarMax)) return Piece.NECTAR;
        return Piece.UNKNOWN;
    }

    static boolean inRange(double h, double min, double max) {
        if (Double.isNaN(h) || Double.isNaN(min) || Double.isNaN(max)) return false;
        return min <= max ? h >= min && h <= max : h >= min || h <= max;
    }

    /** Pieces counted in the lane. */
    public int count() {
        return count;
    }

    public boolean full() {
        return count >= SLOTS;
    }

    /** Slot {@code i}, 0 the front; null when empty. */
    public Piece slot(int i) {
        return i >= 0 && i < count ? slots[i] : null;
    }

    /** Whether a piece is under the sensor, as of the last fresh reading. */
    public boolean present() {
        return present;
    }

    /** Pieces that arrived with the lane already counted full. */
    public int overflows() {
        return overflows;
    }

    /** Shots or outward pieces with the lane already counted empty. */
    public int underflows() {
        return underflows;
    }

    /** This loop's change, for the match log's events; null when nothing changed. */
    public String event() {
        return event;
    }

    /** Front to back, one letter a slot: P, N, ? and _ for empty. */
    public String lettersFrontToBack() {
        StringBuilder b = new StringBuilder(SLOTS);
        for (int i = 0; i < SLOTS; i++) {
            Piece p = slot(i);
            b.append(p == null ? '_' : p == Piece.POLLEN ? 'P' : p == Piece.NECTAR ? 'N' : '?');
        }
        return b.toString();
    }
}
