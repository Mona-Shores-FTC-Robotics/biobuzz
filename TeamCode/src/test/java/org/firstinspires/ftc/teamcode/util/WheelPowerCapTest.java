package org.firstinspires.ftc.teamcode.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Covers {@link WheelPowerCap}: a speed mode is the same fraction of full power in every direction,
 * and a bad cap gives a robot that stops, never one past full power.
 */
public class WheelPowerCapTest {

    private static final double EPS = 1e-9;

    /** Pedro's own mix ({@code Mecanum.computeWheelPowersUnnormalized}), then its normalization. */
    private static double largestAfterPedro(double f, double s, double t) {
        double[] w = {f - s - t, f + s + t, f + s - t, f - s + t};
        double max = 1.0;
        for (double x : w) max = Math.max(max, Math.abs(x));
        double largest = 0.0;
        for (double x : w) largest = Math.max(largest, Math.abs(x / max));
        return largest;
    }

    private static double cappedLargest(double f, double s, double t, double cap) {
        double k = WheelPowerCap.factor(f, s, t, cap);
        return largestAfterPedro(k * f, k * s, k * t);
    }

    @Test
    public void largestWheelIsTheSumOfTheMagnitudes() {
        assertEquals(1.2, WheelPowerCap.largestWheel(0.5, -0.4, 0.3), EPS);
        assertEquals(0.0, WheelPowerCap.largestWheel(0.0, 0.0, 0.0), EPS);
    }

    @Test
    public void underTheCapNothingChanges() {
        assertEquals(1.0, WheelPowerCap.factor(0.5, 0.0, 0.0, 0.7), EPS);
        assertEquals(1.0, WheelPowerCap.factor(0.3, 0.2, 0.1, 0.7), EPS);
        assertEquals(1.0, WheelPowerCap.factor(0.0, 0.0, 0.0, 0.0), EPS);
    }

    /** The bug this fixes: 0.8 on a diagonal used to be normalized to full power, the same as turbo. */
    @Test
    public void aDiagonalAtNormalNoLongerReachesFullPower() {
        double d = 0.8 / Math.sqrt(2.0);   // the stick at its rim at 45°, times 0.8
        assertEquals(1.0, largestAfterPedro(d, d, 0.0), EPS);          // before: full power
        assertEquals(0.8, cappedLargest(d, d, 0.0, 0.8), EPS);          // now: the mode's share
    }

    @Test
    public void turningWhileDrivingStaysUnderTheCap() {
        assertEquals(0.7, cappedLargest(0.7, 0.0, 0.7, 0.7), EPS);
        assertEquals(0.25, cappedLargest(0.25, 0.25, 0.25, 0.25), EPS);
    }

    /** Capping at k gives exactly turbo's command times k, for any direction and turn. */
    @Test
    public void aModeIsTurboTimesItsScaleInEveryDirection() {
        double k = 0.7;
        for (int deg = 0; deg < 360; deg += 15) {
            for (double turn = -1.0; turn <= 1.0; turn += 0.5) {
                double f = Math.cos(Math.toRadians(deg));
                double s = Math.sin(Math.toRadians(deg));
                double turbo = largestAfterPedro(f, s, turn);
                double mode = cappedLargest(k * f, k * s, k * turn, k);
                assertEquals("at " + deg + "°, turn " + turn, k * turbo, mode, EPS);
            }
        }
    }

    @Test
    public void aCapOfOneIsPedrosOwnNormalization() {
        assertEquals(1.0, cappedLargest(1.0, 1.0, 1.0, 1.0), EPS);
        assertEquals(1.0 / 3.0, WheelPowerCap.factor(1.0, 1.0, 1.0, 1.0), EPS);
    }

    @Test
    public void aBadCapStopsTheRobotRatherThanRunningAway() {
        assertEquals(0.0, WheelPowerCap.factor(0.5, 0.0, 0.0, Double.NaN), EPS);
        assertEquals(0.0, WheelPowerCap.factor(0.5, 0.0, 0.0, -1.0), EPS);
        assertEquals(1.0, cappedLargest(1.0, 1.0, 0.0, 5.0), EPS);   // clamped to 1
    }
}
