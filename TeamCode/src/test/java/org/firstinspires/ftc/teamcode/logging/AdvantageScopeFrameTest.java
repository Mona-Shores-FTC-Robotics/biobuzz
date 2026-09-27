package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Pins the Pedro → Center/Rotated arithmetic. Whether the mapping is <i>right</i> for the BIOBUZZ
 * field is a visual check against the known-points log; these tests only stop it changing by
 * accident.
 */
public class AdvantageScopeFrameTest {

    private static final double EPS = 1e-12;
    private static final double IN = AdvantageScopeFrame.METERS_PER_INCH;
    private static final double C = AdvantageScopeFrame.PEDRO_FIELD_CENTER_IN;

    /** The Visualizer's field is 141.5 in, and its Pedro 3 export mirrors about half of that. */
    @Test
    public void centreIsHalfTheVisualizersField() {
        assertEquals(141.5 / 2, C, 0.0);
    }

    @Test
    public void pedroCenterIsTheOrigin() {
        assertEquals(0.0, AdvantageScopeFrame.xMeters(C, C), EPS);
        assertEquals(0.0, AdvantageScopeFrame.yMeters(C, C), EPS);
    }

    @Test
    public void pedroPlusXBecomesCenterRotatedPlusY() {
        assertEquals(0.0, AdvantageScopeFrame.xMeters(C + 60, C), EPS);
        assertEquals(60 * IN, AdvantageScopeFrame.yMeters(C + 60, C), EPS);
    }

    @Test
    public void pedroPlusYBecomesCenterRotatedMinusX() {
        assertEquals(-60 * IN, AdvantageScopeFrame.xMeters(C, C + 60), EPS);
        assertEquals(0.0, AdvantageScopeFrame.yMeters(C, C + 60), EPS);
    }

    @Test
    public void headingTurnsAQuarterAndWraps() {
        assertEquals(Math.PI / 2, AdvantageScopeFrame.headingRad(0), EPS);
        assertEquals(-Math.PI, AdvantageScopeFrame.headingRad(Math.PI / 2), EPS);
        assertEquals(0.0, AdvantageScopeFrame.headingRad(-Math.PI / 2), EPS);
    }

    /**
     * AdvantageScope turns a Center/Rotated pose into its internal frame as (x, y, θ) → (y, −x,
     * θ − π/2) ({@code geometry.ts}). Composed with ours, a Pedro pose should arrive centred and
     * otherwise unchanged. That is the claim the class javadoc makes; this checks it.
     */
    @Test
    public void composedWithAdvantageScopeItIsPedroCentred() {
        double[][] poses = {{10, 20, 0.3}, {140, 5, -2.0}, {C, C, 3.0}};
        for (double[] p : poses) {
            double xcr = AdvantageScopeFrame.xMeters(p[0], p[1]);
            double ycr = AdvantageScopeFrame.yMeters(p[0], p[1]);
            double tcr = AdvantageScopeFrame.headingRad(p[2]);
            assertEquals((p[0] - C) * IN, ycr, EPS);
            assertEquals((p[1] - C) * IN, -xcr, EPS);
            assertEquals(AdvantageScopeFrame.wrap(p[2]), AdvantageScopeFrame.wrap(tcr - Math.PI / 2), EPS);
        }
    }
}
