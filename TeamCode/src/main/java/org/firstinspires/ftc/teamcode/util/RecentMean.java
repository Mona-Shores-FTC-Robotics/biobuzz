package org.firstinspires.ftc.teamcode.util;

/**
 * The mean of the last few values, for a bench's "running mean of the last 5": one test's result
 * is noisy, a few in a row say whether a change helped. {@link WelfordVariance} is the mean of
 * everything; this forgets the oldest. A NaN is not a value and is skipped. Allocated once.
 */
public final class RecentMean {

    private final double[] values;
    private int next;
    private int count;

    /** Keeps the last {@code size} values, at least one. */
    public RecentMean(int size) {
        values = new double[Math.max(1, size)];
    }

    /** Adds a value, forgetting the oldest once full. NaN is skipped. */
    public void add(double value) {
        if (Double.isNaN(value)) return;
        values[next] = value;
        next = (next + 1) % values.length;
        if (count < values.length) count++;
    }

    /** How many values the mean is over, up to the size. */
    public int count() {
        return count;
    }

    /** The mean of the values kept; NaN when there are none. */
    public double mean() {
        if (count == 0) return Double.NaN;
        double sum = 0.0;
        for (int i = 0; i < count; i++) sum += values[i];
        return sum / count;
    }

    /** Forget everything. */
    public void reset() {
        next = 0;
        count = 0;
    }
}
