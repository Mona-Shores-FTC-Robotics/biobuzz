package org.firstinspires.ftc.teamcode.vision;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Cross-checks the transcribed manual constants against each other.
 *
 * <p>These are not tests of our code so much as tests of our <em>reading</em> of the
 * manual. The published numbers over-determine the geometry, so if the tilt has been
 * misinterpreted or a figure misread, the redundancy catches it here rather than on
 * the field.
 */
public class HiveGeometryTest {

    /**
     * The load-bearing check on our interpretation of the 30°.
     *
     * <p>Figure 9-10 gives the raised CELL's opening spanning 53.5" to 65.6" above the
     * tiles — 12.1" of vertical extent — and §9.6.2 gives the opening as 14" tall in
     * its own plane. 14 * cos(30°) = 12.12, which matches. Had the 30° been measured
     * from vertical instead, we would expect 14 * sin(30°) = 7.0, which does not.
     */
    @Test
    public void publishedOpeningHeightsConfirmTheTiltIsFromHorizontal() {
        double publishedExtent =
                HiveGeometry.OPENING_TOP_HEIGHT_IN - HiveGeometry.OPENING_BOTTOM_HEIGHT_IN;

        assertEquals(12.1, publishedExtent, 1e-9);
        assertEquals("14in opening tilted 30deg from horizontal should span ~12.1in",
                publishedExtent, HiveGeometry.OPENING_VERTICAL_EXTENT_IN, 0.05);
    }

    /** The derived swing, which is what makes height usable as a state discriminator. */
    @Test
    public void stateHeightDeltaIsAboutNineInches() {
        assertEquals(9.42, HiveGeometry.STATE_HEIGHT_DELTA_IN, 0.01);
    }

    /**
     * The separation has to exceed the classifier's two acceptance bands, or UP and
     * DOWN overlap and {@code CellStateTracker} correctly refuses to distinguish them.
     * This is the constraint that sets the default tolerance.
     */
    @Test
    public void stateSeparationExceedsTheClassifierTolerance() {
        double bandsWidth = 2.0 * CellStateTracker.Geometry.heightToleranceIn;
        assertTrue(
                "Tolerance " + CellStateTracker.Geometry.heightToleranceIn
                        + "in leaves no dead band across a "
                        + HiveGeometry.STATE_HEIGHT_DELTA_IN + "in swing",
                HiveGeometry.STATE_HEIGHT_DELTA_IN > bandsWidth);
    }

    /** The raised opening must sit above the HIVE's lowest point, or a figure is misread. */
    @Test
    public void publishedHeightsAreOrdered() {
        assertTrue(HiveGeometry.HIVE_BOTTOM_HEIGHT_IN < HiveGeometry.OPENING_BOTTOM_HEIGHT_IN);
        assertTrue(HiveGeometry.OPENING_BOTTOM_HEIGHT_IN < HiveGeometry.OPENING_TOP_HEIGHT_IN);
        assertTrue(HiveGeometry.PIVOT_AXIS_HEIGHT_IN < HiveGeometry.OPENING_BOTTOM_HEIGHT_IN);
        assertTrue(HiveGeometry.HIVE_BOTTOM_HEIGHT_IN < HiveGeometry.PIVOT_AXIS_HEIGHT_IN);
    }

    /** Two CELLS plus the connecting assembly have to fit inside the HIVE's overall width. */
    @Test
    public void cellSpacingFitsWithinTheHiveWidth() {
        assertTrue(HiveGeometry.CELL_SPACING_IN < HiveGeometry.HIVE_OVERALL_WIDTH_IN);
    }

    /** Both HIVES sit on one frame, so their centres cannot be further apart than it is. */
    @Test
    public void hiveSpacingFitsWithinTheFrame() {
        assertTrue(HiveGeometry.HIVE_CENTER_TO_CENTER_IN < HiveGeometry.FRAME_WIDTH_IN);
    }
}
