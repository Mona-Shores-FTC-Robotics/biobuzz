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
 * <p>Straight-line interpolation between the two rows either side of the range. Past either end
 * the end segment's slope carries on for {@link #extendIn} (5 in), then the speed holds: a shot
 * slightly outside the measured spots gets a speed on the trend, a far one is not a guess. A row
 * counts only with a finite range of zero or more and a finite speed above zero, so a NaN row (the
 * default) is no row and a Panels typo drops one row rather than throwing a wild speed. With no
 * usable row, or a NaN range, the answer is NaN, and the caller keeps
 * {@link LauncherSubsystem#targetTicksPerSecond}. Rows need not be in order. Two rows at the same
 * range: the first one counts.
 *
 * <h2>Filling it in</h2>
 *
 * <p>With {@link #enabled} off, in Basic Drive: Square drives to the launch spot in line with the
 * robot and fires (the rows below were measured at the spots it drove to until 11 Nov 2026);
 * the operator trims the speed up or down and Square fires again, until it scores. The Match page's
 * "Last launch spot" line then shows the spot's range and the speed to write down. One row per
 * spot, measured on the right side; the left spots are the same distances on the other side:
 * LAUNCH_CATCH about 43 in, LAUNCH_OFFSET about 45 in, START about 50 in, and the corner shot near
 * the GARDEN about 64 in (driven to by hand and fired with FIRE until it is a team spot; its range
 * is on the "Last shot" line). Take the middle of the speeds that score, so a tired battery or a
 * worn wheel has room either way, and the range the robot showed rather than these model numbers.
 * Then switch {@link #enabled} on. Both robots share the table (identical hardware); one seen to
 * differ gets a line in {@code RobotDifferences}, never a second table.
 *
 * <p><b>Measured</b> on 20245 on 4 Nov 2026, with the launcher PIDF tuned that night, each range the
 * robot showed ({@code last_shot_range_in}) and the speed the drive team picked as scoring well:
 * RIGHT_LAUNCH_CATCH 39 in (38.6–39.2) at 1520, RIGHT_LAUNCH_OFFSET 42.3 in at 1560, RIGHT_START
 * 50.5 in at 1570 (raised to 1580 on 5 Nov: R-Solo's start volleys fell a little short; then to
 * 1600, after R-Solo's 23:41 run on video put three of the first volley's four in at 1580 and the
 * fourth, low, off the CELL's front lip), and the hand-driven shot near the GARDEN 55.4 in
 * (55.3–55.5) at 1635.
 *
 * <p><b>Those ranges were the camera's from the robot's tracked point</b> (the middle of the
 * wheels), while the pose's range has always been from the turret axis, 3.33 in behind and 1.87 in
 * right of it: facing the CELL, about 3.4 in apart. So on 5 Nov every launch range became the turret
 * axis's, camera and pose alike ({@code CellSighting.groundRangeFromIn}), and each row moved to the
 * same shot's range from the turret, {@code hypot(r + 3.33, 1.87)} with the CELL straight ahead:
 * 39.0 → 42.4, 42.3 → 45.7, 50.5 → 53.9, 55.4 → 58.8. The speeds are unchanged, and the
 * converted ranges agree with the pose's model of each spot to about an inch (CATCH 42.9, OFFSET
 * 44.6, RIGHT_START on the wall 53.3, the corner 58.1). With {@link #extendIn} at 5 in the table
 * answers from 37.4 in (about 1460) to 63.8 in (about 1670) and holds those beyond.
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
     * configured speed only where the table has no answer. On since 4 Nov 2026 (the rows below);
     * flip it off in Panels if a launch gets worse, and that is the single speed exactly.
     */
    public static boolean enabled = true;

    /**
     * How far past the first and last rows, inches, the table keeps their slope before it holds:
     * a shot a little closer or farther than any measured spot still gets a speed on the trend
     * (at 4 Nov's rows, about 12 ticks/s per inch at the close end and 13 at the far end) rather
     * than the end row's. Beyond it the speed holds, never a guess. 0 or NaN: hold at the end row.
     */
    public static double extendIn = 5.0;

    /**
     * How far past {@link #extendIn}, inches, a range may be and still get the table's speed (held
     * at the extension's end). Further out the range is not believed and every launch, TeleOp and
     * Auto, takes {@link LauncherSubsystem#targetTicksPerSecond}: with these rows, below 27.4 in or
     * above 73.8 in. NaN or negative: 0.
     */
    public static double trustBeyondIn = 10.0;

    /** Row 1: horizontal range from the turret axis to the UP CELL's raised row, inches. NaN: no row. */
    public static double row1RangeIn = 42.4;
    /** Row 1: the launch speed that scores from {@link #row1RangeIn}, ticks per second. NaN: no row. */
    public static double row1TicksPerSecond = 1520.0;
    /** As row 1. */
    public static double row2RangeIn = 45.7;
    /** As row 1. */
    public static double row2TicksPerSecond = 1560.0;
    /** As row 1. */
    public static double row3RangeIn = 53.9;
    /** As row 1. */
    public static double row3TicksPerSecond = 1600.0;
    /** As row 1. */
    public static double row4RangeIn = 58.8;
    /** As row 1. */
    public static double row4TicksPerSecond = 1635.0;
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
     * range is NaN, no row is usable, or the range is past {@link #trustBeyondIn} (a bad pose), so
     * the caller falls back to the configured speed.
     */
    public static double ticksPerSecondFor(double rangeIn) {
        loadRows();
        if (!trusted(RANGES, SPEEDS, rangeIn, extendIn, trustBeyondIn)) return Double.NaN;
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

    /**
     * Whether {@code rangeIn} is close enough to the rows to answer for: within {@code extend} plus
     * {@code beyond} inches of the first and last usable rows. A range further out is a bad pose
     * (a Pinpoint read fault on 4 Nov 2026 gave 136 in, and the table's top speed went long from a
     * launch spot), so the caller's configured speed is the safer shot. NaN or negative margins
     * count as 0; with no usable row, or a NaN range, false. Pure, so a test can drive it.
     */
    static boolean trusted(double[] ranges, double[] speeds, double rangeIn, double extend, double beyond) {
        if (Double.isNaN(rangeIn)) return false;
        double lo = Double.NaN;
        double hi = Double.NaN;
        int n = Math.min(ranges.length, speeds.length);
        for (int i = 0; i < n; i++) {
            if (!usable(ranges[i], speeds[i])) continue;
            if (Double.isNaN(lo) || ranges[i] < lo) lo = ranges[i];
            if (Double.isNaN(hi) || ranges[i] > hi) hi = ranges[i];
        }
        if (Double.isNaN(lo)) return false;
        double margin = (extend >= 0 && !Double.isInfinite(extend) ? extend : 0.0)
                + (beyond >= 0 && !Double.isInfinite(beyond) ? beyond : 0.0);
        return rangeIn >= lo - margin && rangeIn <= hi + margin;
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
        return interpolate(ranges, speeds, rangeIn, extendIn);
    }

    /**
     * As {@link #interpolate(double[], double[], double)}, extending the end segments by up to
     * {@code extend} inches: past an end row the speed keeps the slope of that end's two outermost
     * rows for at most that far, then holds there. A NaN, negative or infinite extension, or a
     * table of one row, is none: the end row's speed. Pure, so a test can drive it.
     */
    static double interpolate(double[] ranges, double[] speeds, double rangeIn, double extend) {
        if (Double.isNaN(rangeIn)) return Double.NaN;
        int below = -1;
        int above = -1;
        int lowest = -1;
        int nextLowest = -1;
        int highest = -1;
        int nextHighest = -1;
        int n = Math.min(ranges.length, speeds.length);
        for (int i = 0; i < n; i++) {
            double r = ranges[i];
            if (!usable(r, speeds[i])) continue;
            if (r == rangeIn) return speeds[i];
            if (lowest < 0 || r < ranges[lowest]) {
                if (lowest >= 0 && ranges[lowest] != r) nextLowest = lowest;
                lowest = i;
            } else if (r > ranges[lowest] && (nextLowest < 0 || r < ranges[nextLowest])) {
                nextLowest = i;
            }
            if (highest < 0 || r > ranges[highest]) {
                if (highest >= 0 && ranges[highest] != r) nextHighest = highest;
                highest = i;
            } else if (r < ranges[highest] && (nextHighest < 0 || r > ranges[nextHighest])) {
                nextHighest = i;
            }
            if (r < rangeIn && (below < 0 || r > ranges[below])) below = i;
            if (r > rangeIn && (above < 0 || r < ranges[above])) above = i;
        }
        if (lowest < 0) return Double.NaN;
        double reach = extend >= 0.0 && !Double.isInfinite(extend) ? extend : 0.0;
        if (below < 0) {
            if (nextLowest < 0 || reach == 0.0) return speeds[lowest];
            double at = Math.max(rangeIn, ranges[lowest] - reach);
            return line(ranges[lowest], speeds[lowest], ranges[nextLowest], speeds[nextLowest], at);
        }
        if (above < 0) {
            if (nextHighest < 0 || reach == 0.0) return speeds[highest];
            double at = Math.min(rangeIn, ranges[highest] + reach);
            return line(ranges[nextHighest], speeds[nextHighest], ranges[highest], speeds[highest], at);
        }
        return line(ranges[below], speeds[below], ranges[above], speeds[above], rangeIn);
    }

    /** The speed at {@code x} on the straight line through (r0, s0) and (r1, s1); r0 != r1. */
    private static double line(double r0, double s0, double r1, double s1, double x) {
        return s0 + (x - r0) / (r1 - r0) * (s1 - s0);
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
