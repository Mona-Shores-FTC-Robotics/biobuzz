package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.subsystems.LaneTracker.Piece;
import org.junit.Test;

/** The lane queue from one entrance sensor: arrivals, shots, ejects, and the counting limits. */
public class LaneTrackerTest {

    private static final double IN = 20;      // presentBelowMm
    private static final double OUT = 30;     // leaveAboveMm
    private static final double NEAR = 10;
    private static final double FAR = 80;

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
        pass(Piece.NECTAR);
        pass(Piece.POLLEN);
        assertEquals(3, lane.count());
        assertEquals("PNP_", lane.lettersFrontToBack());
        shot();
        assertEquals("NP__", lane.lettersFrontToBack());
        assertEquals("lane: shot, 2 left", lane.event());
    }

    @Test
    public void theFourthPieceStaysUnderTheSensorAndTheLaneIsFull() {
        read(FAR, Piece.UNKNOWN);
        pass(Piece.POLLEN);
        pass(Piece.POLLEN);
        pass(Piece.POLLEN);
        read(NEAR, Piece.NECTAR);
        read(NEAR, Piece.NECTAR);
        assertTrue(lane.full());
        assertEquals("PPPN", lane.lettersFrontToBack());
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
        read(25, Piece.POLLEN); // between the thresholds: still there
        read(NEAR, Piece.POLLEN);
        read(FAR, Piece.UNKNOWN);
        assertEquals(1, lane.count());
    }

    @Test
    public void theClosestReadingNamesThePiece() {
        read(FAR, Piece.UNKNOWN);
        read(18, Piece.UNKNOWN); // at the edge of the spot
        read(8, Piece.NECTAR);   // right under it
        read(15, Piece.POLLEN);  // farther again: ignored
        assertEquals("N___", lane.lettersFrontToBack());
    }

    @Test
    public void anEjectTakesTheBackPiece() {
        read(FAR, Piece.UNKNOWN);
        pass(Piece.POLLEN);
        pass(Piece.NECTAR);
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
        pass(Piece.NECTAR);
        assertEquals(4, lane.count());
        assertEquals(1, lane.overflows());
    }

    @Test
    public void uncalibratedPresenceCountsNothingButShots() {
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

    @Test
    public void classifyUsesRangesAndWrapsThroughZero() {
        assertEquals(Piece.POLLEN, LaneTracker.classify(60, 40, 80, 250, 300));
        assertEquals(Piece.NECTAR, LaneTracker.classify(270, 40, 80, 250, 300));
        assertEquals(Piece.UNKNOWN, LaneTracker.classify(150, 40, 80, 250, 300));
        assertEquals(Piece.NECTAR, LaneTracker.classify(5, 40, 80, 340, 20));
        assertEquals(Piece.NECTAR, LaneTracker.classify(350, 40, 80, 340, 20));
        assertEquals(Piece.UNKNOWN, LaneTracker.classify(60, Double.NaN, 80, 250, 300));
        assertEquals(Piece.UNKNOWN, LaneTracker.classify(Double.NaN, 40, 80, 250, 300));
    }
}
