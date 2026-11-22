package org.firstinspires.ftc.teamcode.subsystems;

/**
 * Turns the turret encoder's quadrature count into the turret's angle. Plain Java on purpose, so
 * all of it is unit tested at a laptop; the hardware read is the caller's.
 *
 * <h2>The encoder</h2>
 *
 * <p>A REV Through Bore Encoder (REV-11-1271) on the turret's drive gear, its quadrature output
 * plugged into a hub motor encoder socket ({@code TurretSubsystem.encoderHostName()}). The hub counts
 * both edges of both channels: {@value #REV_THROUGH_BORE_COUNTS} per turn of that encoder (the
 * Studica Cypher Max it replaced counts {@value #CYPHER_MAX_COUNTS}). It is a <b>relative</b>
 * count: zero wherever the encoder was when the hub powered up, and it keeps counting across
 * OpModes while the hub stays on. So the turret's angle is the count since the turret was last
 * known to be at home, which a person puts it at before INIT.
 *
 * <p>The encoder also has an absolute pulse output. Not used (the build team chose relative plus
 * a home mark, Oct 2026); nothing here stops adding it later.
 *
 * <h2>The two measured values</h2>
 *
 * <ul>
 *   <li>{@link #turnsPerTurretTurn}: encoder turns for one turret turn, from the gear train. 1 if
 *       the encoder is on the turret's own shaft; 2.75 on the goBILDA turret kit's 64T drive gear
 *       against its 176T ring. Measure it: turn the turret one full turn by hand and read the
 *       count.</li>
 *   <li>{@link #direction}: +1 if the count rises as the turret turns counter-clockwise seen from
 *       above, -1 if it falls. Every angle in this codebase is counter-clockwise positive (Pedro's
 *       frame), and this is where the encoder is made to agree.</li>
 * </ul>
 *
 * <p>Angles are unwrapped: {@link #degrees} grows past 180 and below -180 as the turret winds, so
 * a turret with stops can be kept inside its travel. {@link #wrap180} is for display and for a
 * continuous turret's shortest way round.
 */
public final class TurretEncoder {

    /** What a REV hub counts per turn of a REV Through Bore Encoder (2048 cycles, four edges each). */
    public static final int REV_THROUGH_BORE_COUNTS = 8192;

    /** What a REV hub counts per turn of a Studica Cypher Max, as read on the 24 Oct 2026 bench. */
    public static final int CYPHER_MAX_COUNTS = 2048;

    /** Hub counts per encoder turn: the encoder's own number. */
    public final double countsPerEncoderTurn;

    /** Encoder turns per turret turn, from the gearing. Measured, never guessed. */
    public final double turnsPerTurretTurn;

    /** +1 or -1: which way the count moves as the turret turns counter-clockwise seen from above. */
    public final int direction;

    /**
     * @param countsPerEncoderTurn hub counts per encoder turn; must be positive and finite
     * @param turnsPerTurretTurn   encoder turns per turret turn; must be positive and finite
     * @param direction            +1 or -1
     * @throws IllegalArgumentException for anything else, because a wrong ratio or a 0 direction
     *                                  would turn every angle into nonsense while looking valid
     */
    public TurretEncoder(double countsPerEncoderTurn, double turnsPerTurretTurn, int direction) {
        if (!(countsPerEncoderTurn > 0.0) || Double.isInfinite(countsPerEncoderTurn)) {
            throw new IllegalArgumentException(
                    "countsPerEncoderTurn must be positive and finite, not " + countsPerEncoderTurn);
        }
        if (!(turnsPerTurretTurn > 0.0) || Double.isInfinite(turnsPerTurretTurn)) {
            throw new IllegalArgumentException(
                    "turnsPerTurretTurn must be positive and finite, not " + turnsPerTurretTurn);
        }
        if (direction != 1 && direction != -1) {
            throw new IllegalArgumentException("direction must be +1 or -1, not " + direction);
        }
        this.countsPerEncoderTurn = countsPerEncoderTurn;
        this.turnsPerTurretTurn = turnsPerTurretTurn;
        this.direction = direction;
    }

    /** Hub counts for one turret turn. */
    public double countsPerTurretTurn() {
        return countsPerEncoderTurn * turnsPerTurretTurn;
    }

    /** Turret degrees per hub count: the finest angle this setup can tell apart. */
    public double degreesPerCount() {
        return 360.0 / countsPerTurretTurn();
    }

    /**
     * The turret's angle from home, degrees, counter-clockwise positive, <em>not</em> wrapped.
     *
     * @param counts     the hub's current count
     * @param zeroCounts the hub's count when the turret was last at home
     */
    public double degrees(long counts, long zeroCounts) {
        return direction * (counts - zeroCounts) * degreesPerCount();
    }

    /**
     * Hub counts per second into turret degrees per second, counter-clockwise positive. The hub's
     * velocity is a 16-bit value that wraps above 32767 counts/s; at 8192 counts per encoder turn
     * that is 4 encoder turns per second, about 520 degrees per second of turret through 2.75:1,
     * well above the servo's top speed.
     */
    public double degreesPerSecond(double countsPerSecond) {
        return direction * countsPerSecond * degreesPerCount();
    }

    /** Encoder turns since {@code zeroCounts}, signed the way the hub counts, for reading a ratio off. */
    public double encoderTurns(long counts, long zeroCounts) {
        return (counts - zeroCounts) / countsPerEncoderTurn;
    }

    /** {@code deg} wrapped into (-180, 180]. NaN stays NaN. */
    public static double wrap180(double deg) {
        if (Double.isNaN(deg)) {
            return Double.NaN;
        }
        double wrapped = deg % 360.0;
        if (wrapped <= -180.0) {
            wrapped += 360.0;
        } else if (wrapped > 180.0) {
            wrapped -= 360.0;
        }
        return wrapped;
    }
}
