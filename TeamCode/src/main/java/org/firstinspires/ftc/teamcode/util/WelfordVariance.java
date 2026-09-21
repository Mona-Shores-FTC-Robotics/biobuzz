package org.firstinspires.ftc.teamcode.util;

/**
 * Welford's online algorithm for a numerically stable running mean and variance.
 *
 * <p>Update is O(1) and the intermediate sums never lose the precision that naive
 * {@code sumOfSquares - sum*sum/n} does once n grows or the values cluster far
 * from zero — which is exactly the case when accumulating, say, a range in inches.
 *
 * <p>Ported unchanged from the DECODE project, which took it from
 * BeepBot99/CodeBloodedDecodeV3.
 */
public final class WelfordVariance {

    private int n = 0;
    private double mean = 0.0;
    private double m2 = 0.0;

    public void update(double x) {
        n++;
        double delta = x - mean;
        mean += delta / n;
        m2 += delta * (x - mean);
    }

    public void reset() {
        n = 0;
        mean = 0.0;
        m2 = 0.0;
    }

    /** Sample variance (Bessel-corrected). NaN until at least two samples. */
    public double variance() {
        return n < 2 ? Double.NaN : m2 / (n - 1);
    }

    /** Sample standard deviation. NaN until at least two samples. */
    public double stdDev() {
        return Math.sqrt(variance());
    }

    public double mean() { return mean; }

    public int n() { return n; }
}
