package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.subsystems.LaneTracker.Piece;
import org.junit.Test;

/** The lane queue from one entrance sensor: arrivals, shots, ejects, and the counting limits. */
public class LaneTrackerTest {

    // Brightness: higher means a piece is there. The bench readings of 2 Nov 2026 as the fixtures.
    private static final double IN = 0.15;    // presentAbove
    private static final double OUT = 0.08;   // leaveBelow
    private static final double NEAR = 0.903; // NECTAR under the sensor
    private static final double FAR = 0.020;  // empty lane

    private final LaneTracker lane = new LaneTracker();

    private void read(double mm, Piece piece) {
        lane.step(true, mm, piece, false, false, IN, OUT);
    }

    private void readOutward(double mm) {
        lane.step(true, mm, Piece.UNKNOWN, true, false, IN, OUT);
    }

    private void shot() {
        lane.step(false, Double.NaN, Piece.UNKNOWN, false, true, IN, OUT);
    }

    /** A piece passes under the sensor: arrives, then moves on into the lane. */
    private void pass(Piece piece) {
        read(NEAR, piece);
        read(FAR, Piece.UNKNOWN);
    }

    @Test
    public void piecesJoinTheBackAndShotsTakeTheFront() {
        read(FAR, Piece.UNKNOWN); // baseline: nothing there
        pass(Piece.POLLEN);
        pass(Piece.BLUE_NECTAR);
        pass(Piece.POLLEN);
        assertEquals(3, lane.count());
        assertEquals("PBP_", lane.lettersFrontToBack());
        shot();
        assertEquals("BP__", lane.lettersFrontToBack());
        assertEquals("lane: shot, 2 left", lane.event());
    }

    @Test
    public void theFourthPieceStaysUnderTheSensorAndTheLaneIsFull() {
        read(FAR, Piece.UNKNOWN);
        pass(Piece.POLLEN);
        pass(Piece.POLLEN);
        pass(Piece.POLLEN);
        read(NEAR, Piece.BLUE_NECTAR);
        read(NEAR, Piece.BLUE_NECTAR);
        assertTrue(lane.full());
        assertEquals("PPPB", lane.lettersFrontToBack());
    }

    @Test
    public void aPreloadUnderTheSensorAtInitIsNotCountedAgain() {
        lane.fill(Piece.POLLEN, LaneTracker.AUTO_PRELOAD);
        read(NEAR, Piece.POLLEN); // baseline: the fourth preload is there
        read(NEAR, Piece.POLLEN);
        assertEquals(4, lane.count());
        assertEquals(0, lane.overflows());
    }

    @Test
    public void hysteresisCountsAWobbleOnce() {
        read(FAR, Piece.UNKNOWN);
        read(NEAR, Piece.POLLEN);
        read(0.10, Piece.POLLEN); // between the thresholds: still there
        read(NEAR, Piece.POLLEN);
        read(FAR, Piece.UNKNOWN);
        assertEquals(1, lane.count());
    }

    @Test
    public void theBrightestReadingNamesThePiece() {
        read(FAR, Piece.UNKNOWN);
        read(0.2, Piece.UNKNOWN); // at the edge of the spot
        read(0.9, Piece.BLUE_NECTAR);  // right under it
        read(0.5, Piece.POLLEN);  // dimmer again: ignored
        assertEquals("B___", lane.lettersFrontToBack());
    }

    @Test
    public void anEjectTakesTheBackPiece() {
        read(FAR, Piece.UNKNOWN);
        pass(Piece.POLLEN);
        pass(Piece.BLUE_NECTAR);
        readOutward(NEAR); // the NECTAR comes back under the sensor: no change yet
        assertEquals(2, lane.count());
        readOutward(FAR);  // and out
        assertEquals("P___", lane.lettersFrontToBack());
    }

    @Test
    public void countingErrorsStayAtTheLimitAndAreCounted() {
        read(FAR, Piece.UNKNOWN);
        shot();
        assertEquals(0, lane.count());
        assertEquals(1, lane.underflows());
        lane.fill(Piece.POLLEN, 4);
        pass(Piece.BLUE_NECTAR);
        assertEquals(4, lane.count());
        assertEquals(1, lane.overflows());
    }

    @Test
    public void unsetPresenceCountsNothingButShots() {
        lane.fill(Piece.POLLEN, 2);
        lane.step(true, NEAR, Piece.POLLEN, false, false, Double.NaN, Double.NaN);
        lane.step(true, FAR, Piece.UNKNOWN, false, false, Double.NaN, Double.NaN);
        lane.step(true, NEAR, Piece.POLLEN, false, false, Double.NaN, Double.NaN);
        assertEquals(2, lane.count());
        lane.step(false, Double.NaN, Piece.UNKNOWN, false, true, Double.NaN, Double.NaN);
        assertEquals(1, lane.count());
    }

    @Test
    public void nothingChangingLeavesNoEvent() {
        read(FAR, Piece.UNKNOWN);
        read(FAR, Piece.UNKNOWN);
        assertNull(lane.event());
        assertFalse(lane.present());
    }

    private static Piece shipped(double hue) {
        return LaneTracker.classify(hue, ColorSensorSubsystem.pollenHueMin, ColorSensorSubsystem.pollenHueMax,
                ColorSensorSubsystem.redNectarHueMin, ColorSensorSubsystem.redNectarHueMax,
                ColorSensorSubsystem.blueNectarHueMin, ColorSensorSubsystem.blueNectarHueMax);
    }

    @Test
    public void theBenchReadingsClassifyWithTheShippedRanges() {
        for (double h : new double[] {73, 77, 94}) assertEquals("POLLEN at " + h, Piece.POLLEN, shipped(h));
        for (double h : new double[] {9, 11, 22}) assertEquals("red NECTAR at " + h, Piece.RED_NECTAR, shipped(h));
        for (double h : new double[] {212, 229, 232}) assertEquals("blue NECTAR at " + h, Piece.BLUE_NECTAR, shipped(h));
        assertTrue("an empty lane reads below presentAbove", 0.020 < ColorSensorSubsystem.presentAbove);
        assertTrue("the dimmest POLLEN at rest reads above it", 0.155 > ColorSensorSubsystem.presentAbove);
        assertTrue("leaveBelow sits under presentAbove", ColorSensorSubsystem.leaveBelow < ColorSensorSubsystem.presentAbove);
    }

    @Test
    public void classifyUsesRangesAndWrapsThroughZero() {
        assertEquals(Piece.POLLEN, LaneTracker.classify(60, 40, 80, 340, 20, 250, 300));
        assertEquals(Piece.BLUE_NECTAR, LaneTracker.classify(270, 40, 80, 340, 20, 250, 300));
        assertEquals(Piece.RED_NECTAR, LaneTracker.classify(5, 40, 80, 340, 20, 250, 300));
        assertEquals(Piece.RED_NECTAR, LaneTracker.classify(350, 40, 80, 340, 20, 250, 300));
        assertEquals(Piece.UNKNOWN, LaneTracker.classify(150, 40, 80, 340, 20, 250, 300));
        assertEquals(Piece.UNKNOWN, LaneTracker.classify(60, Double.NaN, 80, 340, 20, 250, 300));
        assertEquals(Piece.UNKNOWN, LaneTracker.classify(Double.NaN, 40, 80, 340, 20, 250, 300));
    }
}
