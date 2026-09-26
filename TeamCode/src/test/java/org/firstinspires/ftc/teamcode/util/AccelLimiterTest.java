package org.firstinspires.ftc.teamcode.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Covers {@link AccelLimiter}. The point of the class is the asymmetry — speeding up is limited,
 * slowing down is not — so most of these check that the robot stops when the stick says stop.
 */
public class AccelLimiterTest {

    private static final double EPS = 1e-9;

    @Test
    public void rampsUpAtTheGivenRate() {
        AccelLimiter limiter = new AccelLimiter();
        assertEquals(0.1, limiter.step(1.0, 2.0, 0.05), EPS);
        assertEquals(0.2, limiter.step(1.0, 2.0, 0.05), EPS);
    }

    @Test
    public void doesNotOvershootTheTarget() {
        AccelLimiter limiter = new AccelLimiter();
        assertEquals(0.3, limiter.step(0.3, 100.0, 1.0), EPS);
    }

    @Test
    public void releasingTheStickStopsImmediately() {
        AccelLimiter limiter = new AccelLimiter();
        limiter.reset(0.8);
        assertEquals(0.0, limiter.step(0.0, 2.0, 0.02), EPS);
    }

    @Test
    public void easingOffFollowsImmediately() {
        AccelLimiter limiter = new AccelLimiter();
        limiter.reset(0.8);
        assertEquals(0.4, limiter.step(0.4, 2.0, 0.02), EPS);
    }

    @Test
    public void reversingBrakesToZeroThenRampsTheOtherWay() {
        AccelLimiter limiter = new AccelLimiter();
        limiter.reset(0.8);
        assertEquals(-0.1, limiter.step(-1.0, 2.0, 0.05), EPS);
    }

    @Test
    public void negativeDirectionRampsToo() {
        AccelLimiter limiter = new AccelLimiter();
        assertEquals(-0.1, limiter.step(-1.0, 2.0, 0.05), EPS);
    }

    @Test
    public void aNaNRateCannotMoveTheOutput() {
        AccelLimiter limiter = new AccelLimiter();
        assertEquals(0.0, limiter.step(1.0, Double.NaN, 0.05), EPS);
    }

    @Test
    public void aNegativeTimeStepCannotMoveTheOutput() {
        AccelLimiter limiter = new AccelLimiter();
        assertEquals(0.0, limiter.step(1.0, 2.0, -0.5), EPS);
    }
}
