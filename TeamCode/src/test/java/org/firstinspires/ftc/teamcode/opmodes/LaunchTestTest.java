package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** The pure parts of {@link LaunchTest}: the launch-speed trim and the gate-position nudge. */
public class LaunchTestTest {

    private static final double EPS = 1e-9;

    @Test
    public void aStepMovesTheTrimEitherWay() {
        assertEquals(50.0, LaunchTest.trimmed(0.0, 50.0, 1525.0), EPS);
        assertEquals(-50.0, LaunchTest.trimmed(0.0, -50.0, 1525.0), EPS);
        assertEquals(0.0, LaunchTest.trimmed(50.0, -50.0, 1525.0), EPS);
    }

    @Test
    public void theTrimNeverTakesTheLaunchSpeedBelowZero() {
        assertEquals(-1525.0, LaunchTest.trimmed(-1500.0, -50.0, 1525.0), EPS);
        assertEquals(0.0, LaunchTest.launchSpeedFor(1525.0, -1525.0), EPS);
        assertEquals(0.0, LaunchTest.trimmed(0.0, -50.0, Double.NaN), EPS);
        assertEquals(0.0, LaunchTest.trimmed(0.0, -50.0, -10.0), EPS);
    }

    @Test
    public void theLaunchSpeedIsTheConfiguredSpeedPlusTheTrim() {
        assertEquals(1575.0, LaunchTest.launchSpeedFor(1525.0, 50.0), EPS);
        assertEquals(1475.0, LaunchTest.launchSpeedFor(1525.0, -50.0), EPS);
        assertEquals(50.0, LaunchTest.launchSpeedFor(Double.NaN, 50.0), EPS);
    }

    @Test
    public void anUnmeasuredPositionStartsAtMidRangeWhateverTheDirection() {
        assertEquals(0.5, LaunchTest.nudged(Double.NaN, 0.02), EPS);
        assertEquals(0.5, LaunchTest.nudged(Double.NaN, -0.02), EPS);
    }

    @Test
    public void aMeasuredPositionMovesByTheStep() {
        assertEquals(0.52, LaunchTest.nudged(0.5, 0.02), EPS);
        assertEquals(0.495, LaunchTest.nudged(0.5, -0.005), EPS);
    }

    @Test
    public void itStaysInsideTheServosRange() {
        assertEquals(1.0, LaunchTest.nudged(0.99, 0.02), EPS);
        assertEquals(0.0, LaunchTest.nudged(0.01, -0.02), EPS);
    }
}
