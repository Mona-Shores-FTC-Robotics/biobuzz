package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.subsystems.LaneTracker.Piece;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

/** {@link NectarReject}: when a burst starts, how long it runs, and when it never does. */
public class NectarRejectTest {

    private static final double PRESENT = 0.10;
    private static final double BURST_MS = 500;
    private static final double BRIGHT = 0.6;

    private final NectarReject reject = new NectarReject();

    private void read(long ms, Piece piece, Alliance ours) {
        reject.step(ms, true, BRIGHT, piece, ours, true, PRESENT, 2, BURST_MS);
    }

    @Test
    public void theEnemyNectarIsTheOtherAlliancesColour() {
        assertEquals(Piece.BLUE_NECTAR, NectarReject.enemyNectar(Alliance.RED));
        assertEquals(Piece.RED_NECTAR, NectarReject.enemyNectar(Alliance.BLUE));
        assertNull(NectarReject.enemyNectar(Alliance.UNKNOWN));
    }

    @Test
    public void twoReadingsInARowStartABurst() {
        read(0, Piece.BLUE_NECTAR, Alliance.RED);
        assertFalse("one reading is not enough", reject.rejecting());
        read(50, Piece.BLUE_NECTAR, Alliance.RED);
        assertTrue(reject.rejecting());
        assertTrue(reject.justStarted());
        assertEquals(1, reject.bursts());
    }

    @Test
    public void anotherReadingBetweenStartsTheCountAgain() {
        read(0, Piece.BLUE_NECTAR, Alliance.RED);
        read(50, Piece.POLLEN, Alliance.RED);
        read(100, Piece.BLUE_NECTAR, Alliance.RED);
        assertFalse(reject.rejecting());
    }

    @Test
    public void ourOwnNectarAndPollenAreKept() {
        for (int i = 0; i < 10; i++) read(i * 50L, Piece.RED_NECTAR, Alliance.RED);
        assertFalse(reject.rejecting());
        for (int i = 0; i < 10; i++) read(i * 50L, Piece.POLLEN, Alliance.BLUE);
        assertFalse(reject.rejecting());
        for (int i = 0; i < 10; i++) read(i * 50L, Piece.BLUE_NECTAR, Alliance.BLUE);
        assertFalse(reject.rejecting());
    }

    @Test
    public void noAllianceNeverRejects() {
        for (int i = 0; i < 10; i++) read(i * 50L, Piece.BLUE_NECTAR, Alliance.UNKNOWN);
        for (int i = 0; i < 10; i++) read(i * 50L, Piece.RED_NECTAR, Alliance.UNKNOWN);
        assertFalse(reject.rejecting());
    }

    @Test
    public void aDimReadingIsNoPiece() {
        for (int i = 0; i < 10; i++) {
            reject.step(i * 50L, true, 0.02, Piece.BLUE_NECTAR, Alliance.RED, true, PRESENT, 2, BURST_MS);
        }
        assertFalse(reject.rejecting());
    }

    @Test
    public void aBurstRunsItsTimeWhateverTheSensorSees() {
        read(0, Piece.BLUE_NECTAR, Alliance.RED);
        read(50, Piece.BLUE_NECTAR, Alliance.RED);
        reject.step(300, true, 0.01, Piece.UNKNOWN, Alliance.RED, true, PRESENT, 2, BURST_MS);
        assertTrue(reject.rejecting());
        assertFalse(reject.justStarted());
        reject.step(549, false, Double.NaN, Piece.UNKNOWN, Alliance.RED, true, PRESENT, 2, BURST_MS);
        assertTrue(reject.rejecting());
        reject.step(550, false, Double.NaN, Piece.UNKNOWN, Alliance.RED, true, PRESENT, 2, BURST_MS);
        assertFalse(reject.rejecting());
    }

    @Test
    public void aPieceStillThereAfterABurstStartsAnother() {
        read(0, Piece.BLUE_NECTAR, Alliance.RED);
        read(50, Piece.BLUE_NECTAR, Alliance.RED);
        read(600, Piece.BLUE_NECTAR, Alliance.RED);
        assertFalse("readings during and at the end of a burst do not count", reject.rejecting());
        read(650, Piece.BLUE_NECTAR, Alliance.RED);
        assertTrue(reject.rejecting());
        assertEquals(2, reject.bursts());
    }

    @Test
    public void theSwitchOffEndsABurstAtOnce() {
        read(0, Piece.BLUE_NECTAR, Alliance.RED);
        read(50, Piece.BLUE_NECTAR, Alliance.RED);
        reject.step(100, true, BRIGHT, Piece.BLUE_NECTAR, Alliance.RED, false, PRESENT, 2, BURST_MS);
        assertFalse(reject.rejecting());
    }

    @Test
    public void unsetNumbersNeverReject() {
        for (int i = 0; i < 5; i++) {
            reject.step(i * 50L, true, BRIGHT, Piece.BLUE_NECTAR, Alliance.RED, true, Double.NaN, 2, BURST_MS);
            reject.step(i * 50L, true, BRIGHT, Piece.BLUE_NECTAR, Alliance.RED, true, PRESENT, 2, Double.NaN);
            reject.step(i * 50L, true, BRIGHT, Piece.BLUE_NECTAR, Alliance.RED, true, PRESENT, 2, 0);
        }
        assertFalse(reject.rejecting());
    }

    @Test
    public void readingsCountOnlyWhenFresh() {
        read(0, Piece.BLUE_NECTAR, Alliance.RED);
        reject.step(20, false, BRIGHT, Piece.BLUE_NECTAR, Alliance.RED, true, PRESENT, 2, BURST_MS);
        assertFalse(reject.rejecting());
    }
}
