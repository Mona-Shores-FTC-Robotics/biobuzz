package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;

/**
 * The launcher's speed-vs-distance table (#80): measured rows of (range to the UP CELL, launch
 * speed), and the speed for a range between them. Pure math, unit tested at a laptop.
 *
 * <h2>Why a measured table and not a formula</h2>
 *
 * <p>A fixed-angle throw to a target {@code h} above the exit point at range {@code d} needs an
 * exit speed {@code v² = g·d² / (2·cos²θ·(d·tanθ − h))}, and the wheel speed is that times an
 * efficiency nobody can calculate (slip, the ball squashing, backspin from the pinch, drag, wheel
 * wear). So the formula's shape is only a check on the rows and a guide to where to measure; the
 * numbers come off the robot. Only an UP CELL scores, so {@code h} is always the raised row and one
 * table serves every shot.
 *
 * <h2>How it answers</h2>
 *
 * <p>Straight-line interpolation between the two rows either side of the range, and the end row's
 * speed beyond either end: never an extrapolation, which past the last row would be a guess. A row
 * counts only with a finite range of zero or more and a finite speed above zero, so a NaN row (the
 * default) is no row and a Panels typo drops one row rather than throwing a wild speed. With no
 * usable row, or a NaN range, the answer is NaN, and the caller keeps
 * {@link LauncherSubsystem#targetTicksPerSecond}. Rows need not be in order. Two rows at the same
 * range: the first one counts.
 *
 * <h2>Filling it in</h2>
 *
 * <p>With {@link #enabled} off, in Basic Drive: Square drives to the nearest launch spot and fires;
 * the operator trims the speed up or down and Square fires again, until it scores. The Match page's
 * "Last launch spot" line then shows the spot's range and the speed to write down. One row per
 * spot, measured on the right side; the left spots are the same distances on the other side:
 * LAUNCH_CATCH about 43 in, LAUNCH_OFFSET about 45 in, START about 50 in, and the corner shot near
 * the GARDEN about 64 in (driven to by hand and fired with FIRE until it is a team spot; its range
 * is on the "Last shot" line). Take the middle of the speeds that score, so a tired battery or a
 * worn wheel has room either way, and the range the robot showed rather than these model numbers.
 * Then switch {@link #enabled} on. Both robots share the table (identical hardware); one seen to
 * differ gets a line in {@code RobotDifferences}, never a second table.
 */
@Configurable
public final class LaunchSpeedTable {

    private LaunchSpeedTable() {
    }

    /**
     * The one switch. Off (the default): every launch, TeleOp and Auto, uses
     * {@link LauncherSubsystem#targetTicksPerSecond} plus any trim, exactly as before the table, and
     * the rows are ignored; that is also how the rows are measured (Square, trim until it scores,
     * write the speed down). On: every launch uses the table for its range, falling back to the
     * configured speed only where the table has no answer. Flip it in Panels to try the table and
     * back if it makes things worse; write it here once the rows are trusted.
     */
    public static boolean enabled = false;

    /** Row 1: horizontal range from the turret axis to the UP CELL's raised row, inches. NaN: no row. */
    public static double row1RangeIn = Double.NaN;
    /** Row 1: the launch speed that scores from {@link #row1RangeIn}, ticks per second. NaN: no row. */
    public static double row1TicksPerSecond = Double.NaN;
    /** As row 1. */
    public static double row2RangeIn = Double.NaN;
    /** As row 1. */
    public static double row2TicksPerSecond = Double.NaN;
    /** As row 1. */
    public static double row3RangeIn = Double.NaN;
    /** As row 1. */
    public static double row3TicksPerSecond = Double.NaN;
    /** As row 1. */
    public static double row4RangeIn = Double.NaN;
    /** As row 1. */
    public static double row4TicksPerSecond = Double.NaN;
    /** As row 1. */
    public static double row5RangeIn = Double.NaN;
    /** As row 1. */
    public static double row5TicksPerSecond = Double.NaN;
    /** As row 1. */
    public static double row6RangeIn = Double.NaN;
    /** As row 1. */
    public static double row6TicksPerSecond = Double.NaN;

    /** The rows copied out of the fields, so a lookup allocates nothing. Loop thread only. */
    private static final double[] RANGES = new double[6];
    private static final double[] SPEEDS = new double[6];

    /**
     * The launch speed for {@code rangeIn} from the configured rows, ticks per second; NaN when the
     * range is NaN or no row is usable, so the caller falls back to the configured speed.
     */
    public static double ticksPerSecondFor(double rangeIn) {
        loadRows();
        return interpolate(RANGES, SPEEDS, rangeIn);
    }

    /**
     * The speed a launch from {@code rangeIn} uses, ticks per second: the table's when
     * {@link #enabled} and it has an answer, else {@code configured} (the caller passes
     * {@link LauncherSubsystem#targetTicksPerSecond}). Trim is the caller's, added on top.
     */
    public static double launchSpeed(double rangeIn, double configured) {
        if (!enabled) return configured;
        double tps = ticksPerSecondFor(rangeIn);
        return Double.isNaN(tps) ? configured : tps;
    }

    /** How many configured rows count (class comment), for the Robot page and the match log. */
    public static int usableRows() {
        loadRows();
        int n = 0;
        for (int i = 0; i < RANGES.length; i++) {
            if (usable(RANGES[i], SPEEDS[i])) n++;
        }
        return n;
    }

    private static void loadRows() {
        RANGES[0] = row1RangeIn;
        SPEEDS[0] = row1TicksPerSecond;
        RANGES[1] = row2RangeIn;
        SPEEDS[1] = row2TicksPerSecond;
        RANGES[2] = row3RangeIn;
        SPEEDS[2] = row3TicksPerSecond;
        RANGES[3] = row4RangeIn;
        SPEEDS[3] = row4TicksPerSecond;
        RANGES[4] = row5RangeIn;
        SPEEDS[4] = row5TicksPerSecond;
        RANGES[5] = row6RangeIn;
        SPEEDS[5] = row6TicksPerSecond;
    }

    /**
     * The speed for {@code rangeIn} from rows {@code (ranges[i], speeds[i])}: interpolated between
     * the nearest usable rows below and above, the end row's speed past either end, NaN with no
     * usable row or a NaN range. One pass, no sorting, nothing allocated. Pure, so a test can drive it.
     */
    static double interpolate(double[] ranges, double[] speeds, double rangeIn) {
        if (Double.isNaN(rangeIn)) return Double.NaN;
        int below = -1;
        int above = -1;
        int lowest = -1;
        int highest = -1;
        int n = Math.min(ranges.length, speeds.length);
        for (int i = 0; i < n; i++) {
            double r = ranges[i];
            if (!usable(r, speeds[i])) continue;
            if (r == rangeIn) return speeds[i];
            if (lowest < 0 || r < ranges[lowest]) lowest = i;
            if (highest < 0 || r > ranges[highest]) highest = i;
            if (r < rangeIn && (below < 0 || r > ranges[below])) below = i;
            if (r > rangeIn && (above < 0 || r < ranges[above])) above = i;
        }
        if (lowest < 0) return Double.NaN;
        if (below < 0) return speeds[lowest];
        if (above < 0) return speeds[highest];
        double f = (rangeIn - ranges[below]) / (ranges[above] - ranges[below]);
        return speeds[below] + f * (speeds[above] - speeds[below]);
    }

    /**
     * The speed to keep commanding, ticks per second, given the one in force and the table's latest
     * answer: the answer once it differs by more than {@code bandTps}, else the speed in force. A
     * new target re-arms the launcher's at-speed timer and shot detector, so a range that wobbles
     * by an inch must not move it every loop. A NaN answer (no range this loop) keeps the speed in
     * force; with none in force, the answer is taken as it is. A NaN or negative band is no band.
     * Pure, so a test can drive it.
     */
    public static double held(double inForce, double answer, double bandTps) {
        if (Double.isNaN(answer)) return inForce;
        if (Double.isNaN(inForce)) return answer;
        double band = bandTps > 0.0 ? bandTps : 0.0;
        return Math.abs(answer - inForce) > band ? answer : inForce;
    }

    /** A row counts with a finite range of zero or more and a finite speed above zero. */
    static boolean usable(double rangeIn, double ticksPerSecond) {
        return rangeIn >= 0.0 && !Double.isInfinite(rangeIn)
                && ticksPerSecond > 0.0 && !Double.isInfinite(ticksPerSecond);
    }
}
