package org.firstinspires.ftc.teamcode.localization;

/**
 * Averages the gated camera positions of consecutive frames while the robot holds still, so one
 * frame's jitter never moves the pose. Pure math, no allocation.
 *
 * <p>The caller resets it whenever the robot moves; a frame that fails {@link TagGate} is simply
 * not added. Once {@link LocalizationTuning#stillFixFrames} frames are in, {@link #ready} is true
 * and {@link #spreadIn} says how well they agree: the furthest any one sat from their mean.
 */
public final class FixAverager {

    private static final int CAPACITY = 32;

    private final double[] xs = new double[CAPACITY];
    private final double[] ys = new double[CAPACITY];
    private int n;
    private int tags;
    private double nearestRangeIn = Double.POSITIVE_INFINITY;
    private long newestCaptureNs;

    public void reset() {
        n = 0;
        tags = 0;
        nearestRangeIn = Double.POSITIVE_INFINITY;
        newestCaptureNs = 0;
    }

    /** One frame's gated position, from {@code tagCount} tags, the nearest {@code rangeIn} away. */
    public void add(double x, double y, int tagCount, double rangeIn, long captureNs) {
        if (n >= CAPACITY) return;
        xs[n] = x;
        ys[n] = y;
        n++;
        tags = n == 1 ? tagCount : Math.min(tags, tagCount);
        nearestRangeIn = Math.min(nearestRangeIn, rangeIn);
        newestCaptureNs = Math.max(newestCaptureNs, captureNs);
    }

    public int frames() { return n; }

    /** Enough frames to commit. */
    public boolean ready() {
        return n >= Math.max(1, LocalizationTuning.stillFixFrames);
    }

    public double x() {
        double sum = 0;
        for (int i = 0; i < n; i++) sum += xs[i];
        return n == 0 ? Double.NaN : sum / n;
    }

    public double y() {
        double sum = 0;
        for (int i = 0; i < n; i++) sum += ys[i];
        return n == 0 ? Double.NaN : sum / n;
    }

    /** The furthest any frame sat from the mean, inches; NaN with no frames. */
    public double spreadIn() {
        if (n == 0) return Double.NaN;
        double mx = x(), my = y(), worst = 0;
        for (int i = 0; i < n; i++) worst = Math.max(worst, Math.hypot(xs[i] - mx, ys[i] - my));
        return worst;
    }

    /** The fewest tags any frame had, for the fix's variance. */
    public int minTags() { return tags; }

    public double nearestRangeIn() { return nearestRangeIn; }

    public long newestCaptureNs() { return newestCaptureNs; }
}
