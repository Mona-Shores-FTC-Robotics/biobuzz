package org.firstinspires.ftc.teamcode.util;

/**
 * Loop-time statistics for an OpMode's main loop.
 *
 * <p>Call {@link #lap()} once per iteration, at the same point every time. The first call only
 * marks the origin — it records no sample, because the interval between "OpMode started" and
 * "first loop" includes init and would drag the mean down for the rest of the match.
 *
 * <p><b>Why four numbers and not one.</b> Mean alone hides everything that matters. A loop that
 * runs at 4 ms mean with one 180 ms stall per match will drop a whole path segment and the mean
 * will barely move; {@link #maxMs()} shows it immediately. {@link #stdDevMs()} separates "steady
 * but slow" (usually a blocking read you can cache) from "fast but spiky" (usually garbage
 * collection or a bulk-read fallback). {@link #slowFraction()} is the one to watch over a whole
 * match, because a loop that is over budget 2% of the time is a different problem from one that
 * is over budget 40% of the time.
 *
 * <p>Uses {@link WelfordVariance} for the running mean and variance, so the numbers stay accurate
 * across a two-and-a-half minute match at 200 Hz (~30,000 samples) without accumulating error.
 *
 * <p>Deliberately has no dependency on the FTC SDK: it is a plain clock reader, which is what lets
 * {@code LoopTimerTest} drive it with a fake clock and assert on exact numbers.
 */
public final class LoopTimer {

    /**
     * Loops slower than this count as slow.
     *
     * <p>10 ms (100 Hz) is the working threshold for this robot rather than a rule from anywhere.
     * Pedro's follower wants to correct faster than the drivetrain can visibly drift, and a
     * bulk-read mecanum loop with odometry sits near 3–5 ms when nothing is wrong, so 10 ms means
     * "something took three times longer than it should have" rather than "slightly busy".
     */
    public static final double DEFAULT_SLOW_LOOP_MS = 10.0;

    private static final double NANOS_PER_MS = 1_000_000.0;

    private final double slowLoopMs;
    private final WelfordVariance stats = new WelfordVariance();

    private boolean started = false;
    private long lastNanos = 0L;
    private double lastMs = Double.NaN;
    private double maxMs = 0.0;
    private int slowLoops = 0;

    public LoopTimer() {
        this(DEFAULT_SLOW_LOOP_MS);
    }

    public LoopTimer(double slowLoopMs) {
        this.slowLoopMs = slowLoopMs;
    }

    /** Records one loop, timed against the system clock. */
    public void lap() {
        lap(System.nanoTime());
    }

    /**
     * Records one loop at an explicit timestamp.
     *
     * <p>Exists so tests can supply a clock. Production code calls {@link #lap()}.
     *
     * @param nowNanos a monotonic timestamp in nanoseconds
     */
    public void lap(long nowNanos) {
        if (!started) {
            started = true;
            lastNanos = nowNanos;
            return;
        }

        double elapsedMs = (nowNanos - lastNanos) / NANOS_PER_MS;
        lastNanos = nowNanos;
        lastMs = elapsedMs;

        stats.update(elapsedMs);
        if (elapsedMs > maxMs) {
            maxMs = elapsedMs;
        }
        if (elapsedMs > slowLoopMs) {
            slowLoops++;
        }
    }

    /**
     * Forgets every sample and the origin.
     *
     * <p>Worth calling right after {@code waitForStart()}: init does a lot of one-off work, and a
     * few slow loops recorded during it sit in the max for the rest of the match.
     */
    public void reset() {
        stats.reset();
        started = false;
        lastNanos = 0L;
        lastMs = Double.NaN;
        maxMs = 0.0;
        slowLoops = 0;
    }

    /** The most recent loop, in milliseconds. NaN until two laps have been recorded. */
    public double lastMs() {
        return lastMs;
    }

    /** Mean loop time in milliseconds. NaN until two laps have been recorded. */
    public double meanMs() {
        return stats.n() == 0 ? Double.NaN : stats.mean();
    }

    /** Standard deviation of loop time in milliseconds. NaN until three laps. */
    public double stdDevMs() {
        return stats.stdDev();
    }

    /** The slowest loop seen since the last {@link #reset()}, in milliseconds. */
    public double maxMs() {
        return maxMs;
    }

    /** Mean loop rate in hertz. NaN until two laps have been recorded. */
    public double hz() {
        double mean = meanMs();
        return mean > 0.0 ? 1000.0 / mean : Double.NaN;
    }

    /** How many loops have been measured. One fewer than the number of {@link #lap()} calls. */
    public int count() {
        return stats.n();
    }

    /** How many loops exceeded the slow-loop threshold. */
    public int slowLoops() {
        return slowLoops;
    }

    /** The slow-loop threshold this timer was built with, in milliseconds. */
    public double slowLoopMs() {
        return slowLoopMs;
    }

    /** Fraction of measured loops that exceeded the threshold, 0..1. NaN before any sample. */
    public double slowFraction() {
        return stats.n() == 0 ? Double.NaN : (double) slowLoops / stats.n();
    }

    /**
     * One line fit for telemetry: rate, mean, spread, worst case and how often it was over budget.
     */
    public String summary() {
        if (stats.n() == 0) {
            return "no loops measured yet";
        }
        return String.format(
                java.util.Locale.US,
                "%.0f Hz  mean %.2f ms  sd %.2f  max %.1f  slow %d/%d (%.1f%%)",
                hz(), meanMs(), stdDevMs(), maxMs(), slowLoops, stats.n(), slowFraction() * 100.0);
    }
}
