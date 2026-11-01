package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** {@link PodOffsetCheck}'s geometry: from where the tracking point ends up, back to the pod offsets. */
public class PodOffsetCheckTest {

    private static final double EPS = 1e-9;

    /** Where a point at (forward, left) from the centre moves when the robot turns theta about it. */
    private static double[] moved(double forward, double left, double theta) {
        double c = Math.cos(theta), s = Math.sin(theta);
        return new double[] {(c - 1) * forward - s * left, s * forward + (c - 1) * left};
    }

    @Test
    public void atAHalfTurnItIsThePinpointTunersFormula() {
        double[] o = PodOffsetCheck.offsetsFrom(-2.0, 13.0, Math.PI);
        assertEquals("xPodOffset = -dy/2", -6.5, o[0], EPS);
        assertEquals("yPodOffset = -dx/2", 1.0, o[1], EPS);
    }

    @Test
    public void anOvershootOrAnUndershootStillGivesTheSameOffsets() {
        for (double deg : new double[] {160, 175, 190, 200, -165, -180, -195}) {
            double theta = Math.toRadians(deg);
            double[] d = moved(1.0, -6.5, theta);
            double[] o = PodOffsetCheck.offsetsFrom(d[0], d[1], theta);
            assertEquals(deg + "°: xPodOffset", -6.5, o[0], EPS);
            assertEquals(deg + "°: yPodOffset", 1.0, o[1], EPS);
        }
    }
}
