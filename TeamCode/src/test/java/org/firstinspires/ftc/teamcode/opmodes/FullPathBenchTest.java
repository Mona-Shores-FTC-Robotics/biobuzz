package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** The one pure part of {@link FullPathBench}: how the gate's position is nudged. */
public class FullPathBenchTest {

    private static final double EPS = 1e-9;

    @Test
    public void anUnmeasuredPositionStartsAtMidRangeWhateverTheDirection() {
        assertEquals(0.5, FullPathBench.nudged(Double.NaN, 0.02), EPS);
        assertEquals(0.5, FullPathBench.nudged(Double.NaN, -0.02), EPS);
    }

    @Test
    public void aMeasuredPositionMovesByTheStep() {
        assertEquals(0.52, FullPathBench.nudged(0.5, 0.02), EPS);
        assertEquals(0.495, FullPathBench.nudged(0.5, -0.005), EPS);
    }

    @Test
    public void itStaysInsideTheServosRange() {
        assertEquals(1.0, FullPathBench.nudged(0.99, 0.02), EPS);
        assertEquals(0.0, FullPathBench.nudged(0.01, -0.02), EPS);
    }
}
