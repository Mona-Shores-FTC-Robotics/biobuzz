package org.firstinspires.ftc.teamcode.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Covers {@link WelfordVariance}, including the numerical claim that justifies it.
 *
 * <p>The class exists instead of a two-line {@code sum}/{@code sumOfSquares}
 * accumulator because the naive form loses precision once the values cluster far
 * from zero. That claim was written down and never checked. {@link
 * #holdsPrecisionWhereTheNaiveFormulaCollapses} checks it, by computing both and
 * showing the naive one is not merely less accurate but wrong.
 *
 * <p>{@code VisionNoiseTuner} is the consumer, and it reports these numbers as the
 * measurement noise a downstream filter should trust a sighting with. A quietly
 * wrong standard deviation there does not look wrong — it looks like a confident
 * camera.
 */
public class WelfordVarianceTest {

    private static final double EPS = 1e-9;

    private static WelfordVariance of(double... values) {
        WelfordVariance stats = new WelfordVariance();
        for (double value : values) {
            stats.update(value);
        }
        return stats;
    }

    /**
     * Mean, sample variance and sample deviation on a dataset chosen so the
     * Bessel-corrected answer differs visibly from the population one: the
     * population variance here is exactly 4, the sample variance 32/7.
     */
    @Test
    public void meanAndSampleVarianceMatchHandComputedValues() {
        WelfordVariance stats = of(2, 4, 4, 4, 5, 5, 7, 9);

        assertEquals(8, stats.n());
        assertEquals(5.0, stats.mean(), EPS);
        assertEquals(32.0 / 7.0, stats.variance(), EPS);
        assertEquals(Math.sqrt(32.0 / 7.0), stats.stdDev(), EPS);
    }

    /**
     * Guards the Bessel correction specifically. Dividing by {@code n} instead of
     * {@code n - 1} is the single easiest edit to make here and would understate
     * every reported deviation — the direction that makes a camera look better than
     * it is.
     */
    @Test
    public void varianceIsTheSampleVarianceNotThePopulationVariance() {
        WelfordVariance stats = of(2, 4, 4, 4, 5, 5, 7, 9);

        double population = 4.0;
        assertTrue("variance() returned the population variance (" + population + "),"
                        + " not the sample variance",
                Math.abs(stats.variance() - population) > 0.5);
    }

    /** A deviation needs two points. One is a mean with no spread, not a spread of zero. */
    @Test
    public void varianceIsNaNUntilTwoSamples() {
        WelfordVariance empty = new WelfordVariance();
        assertEquals(0, empty.n());
        assertTrue("no samples", Double.isNaN(empty.variance()));
        assertTrue("no samples", Double.isNaN(empty.stdDev()));

        WelfordVariance one = of(42.0);
        assertEquals(1, one.n());
        assertEquals(42.0, one.mean(), EPS);
        assertTrue("one sample", Double.isNaN(one.variance()));
        assertTrue("one sample", Double.isNaN(one.stdDev()));
    }

    /** A stationary camera reporting the same number forever has no spread at all. */
    @Test
    public void identicalSamplesHaveZeroVariance() {
        WelfordVariance stats = of(7.5, 7.5, 7.5, 7.5, 7.5);

        assertEquals(7.5, stats.mean(), EPS);
        assertEquals(0.0, stats.variance(), EPS);
        assertEquals(0.0, stats.stdDev(), EPS);
        assertTrue("variance must never go negative", stats.variance() >= 0.0);
    }

    /**
     * The reason this class exists rather than a running sum of squares.
     *
     * <p>Four values one inch apart, offset to a billion. The true sample variance is
     * 5/3 either way, but {@code sumOfSquares - sum*sum/n} has to recover it as the
     * difference of two numbers near 4e18 — where a double's spacing is already 512.
     * The answer does not degrade; it is destroyed.
     *
     * <p>Realistic offsets in this codebase are smaller, so the everyday benefit is
     * modest. The point is that the accumulator never has to be audited against the
     * magnitude of the data, which is the kind of caveat nobody remembers at a
     * meeting.
     */
    @Test
    public void holdsPrecisionWhereTheNaiveFormulaCollapses() {
        double offset = 1e9;
        double[] values = {offset + 1, offset + 2, offset + 3, offset + 4};
        double trueSampleVariance = 5.0 / 3.0;

        WelfordVariance stats = of(values);
        assertEquals(trueSampleVariance, stats.variance(), 1e-6);
        assertEquals(offset + 2.5, stats.mean(), 1e-6);

        double sum = 0.0;
        double sumOfSquares = 0.0;
        for (double value : values) {
            sum += value;
            sumOfSquares += value * value;
        }
        double naive = (sumOfSquares - sum * sum / values.length) / (values.length - 1);

        assertTrue("The naive accumulator returned " + naive + ", which happens to be"
                        + " close to the true " + trueSampleVariance + ". If that is now"
                        + " genuinely true, the rationale in WelfordVariance's javadoc no"
                        + " longer holds and both should be revisited.",
                Math.abs(naive - trueSampleVariance) > 0.5);
    }

    @Test
    public void resetReturnsItToItsInitialState() {
        WelfordVariance stats = of(3, 1, 4, 1, 5, 9, 2, 6);
        stats.reset();

        assertEquals(0, stats.n());
        assertEquals(0.0, stats.mean(), EPS);
        assertTrue(Double.isNaN(stats.variance()));

        // And it accumulates cleanly afterwards rather than carrying anything over.
        stats.update(10.0);
        stats.update(20.0);
        assertEquals(2, stats.n());
        assertEquals(15.0, stats.mean(), EPS);
        assertEquals(50.0, stats.variance(), EPS);
    }

    /**
     * Negative and mixed-sign values are ordinary input — a bearing in degrees
     * straddling zero is the obvious case, and {@code VisionNoiseTuner} feeds
     * exactly that.
     */
    @Test
    public void handlesNegativeAndMixedSignValues() {
        WelfordVariance stats = of(-2, -1, 0, 1, 2);

        assertEquals(0.0, stats.mean(), EPS);
        assertEquals(2.5, stats.variance(), EPS);
        assertEquals(Math.sqrt(2.5), stats.stdDev(), EPS);
    }
}
