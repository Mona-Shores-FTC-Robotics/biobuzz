package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.subsystems.LaneTracker.Piece;
import org.firstinspires.ftc.teamcode.subsystems.NectarReject.Phase;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

/** {@link NectarReject}: when a burst starts, its hold-off, when it re-arms, and its cap. */
public class NectarRejectTest {

    private static final double PRESENT = 0.10;
    private static final double BURST_MS = 500;
    private static final double HOLD_OFF_MS = 300;
    private static final int MAX_BURSTS = 3;
    private static final double WINDOW_MS = 3000;
    private static final double BRIGHT = 0.6;
    private static final double DARK = 0.01;

    private final NectarReject reject = new NectarReject();

    private void step(long ms, boolean fresh, double brightness, Piece piece, Alliance ours, boolean enabled,
                      double presentAbove, double burstMs) {
        reject.step(ms, fresh, brightness, piece, ours, enabled, presentAbove, 2, burstMs,
                HOLD_OFF_MS, MAX_BURSTS, WINDOW_MS);
    }

    private void read(long ms, Piece piece, Alliance ours) {
        step(ms, true, BRIGHT, piece, ours, true, PRESENT, BURST_MS);
    }

    private void blueOnRed(long ms) {
        read(ms, Piece.BLUE_NECTAR, Alliance.RED);
    }

    private void clear(long ms) {
        step(ms, true, DARK, Piece.UNKNOWN, Alliance.RED, true, PRESENT, BURST_MS);
    }

    private void tick(long ms) {
        step(ms, false, Double.NaN, Piece.UNKNOWN, Alliance.RED, true, PRESENT, BURST_MS);
    }

    @Test
    public void theEnemyNectarIsTheOtherAlliancesColour() {
        assertEquals(Piece.BLUE_NECTAR, NectarReject.enemyNectar(Alliance.RED));
        assertEquals(Piece.RED_NECTAR, NectarReject.enemyNectar(Alliance.BLUE));
        assertNull(NectarReject.enemyNectar(Alliance.UNKNOWN));
    }

    @Test
    public void twoReadingsInARowStartABurst() {
        blueOnRed(0);
        assertFalse("one reading is not enough", reject.rejecting());
        blueOnRed(50);
        assertTrue(reject.rejecting());
        assertTrue(reject.justStarted());
        assertEquals(1, reject.bursts());
    }

    @Test
    public void anotherReadingBetweenStartsTheCountAgain() {
        blueOnRed(0);
        read(50, Piece.POLLEN, Alliance.RED);
        blueOnRed(100);
        assertFalse(reject.rejecting());
    }

    @Test
    public void ourOwnNectarAndPollenAreKept() {
        for (int i = 0; i < 10; i++) read(i * 50L, Piece.RED_NECTAR, Alliance.RED);
        for (int i = 0; i < 10; i++) read(i * 50L, Piece.POLLEN, Alliance.BLUE);
        for (int i = 0; i < 10; i++) read(i * 50L, Piece.BLUE_NECTAR, Alliance.BLUE);
        assertEquals(0, reject.bursts());
    }

    @Test
    public void noAllianceNeverRejects() {
        for (int i = 0; i < 10; i++) read(i * 50L, Piece.BLUE_NECTAR, Alliance.UNKNOWN);
        for (int i = 0; i < 10; i++) read(i * 50L, Piece.RED_NECTAR, Alliance.UNKNOWN);
        assertEquals(0, reject.bursts());
    }

    @Test
    public void aDimReadingIsNoPiece() {
        for (int i = 0; i < 10; i++) {
            step(i * 50L, true, 0.02, Piece.BLUE_NECTAR, Alliance.RED, true, PRESENT, BURST_MS);
        }
        assertEquals(0, reject.bursts());
    }

    @Test
    public void aBurstThenAHoldOffThenIdle() {
        blueOnRed(0);
        blueOnRed(50);
        clear(300);
        assertEquals("the burst runs its time whatever the sensor sees", Phase.BURST, reject.phase());
        tick(549);
        assertEquals(Phase.BURST, reject.phase());
        tick(550);
        assertEquals(Phase.HOLD_OFF, reject.phase());
        assertTrue(reject.holdingOff());
        assertFalse(reject.rejecting());
        tick(849);
        assertEquals(Phase.HOLD_OFF, reject.phase());
        tick(850);
        assertEquals(Phase.IDLE, reject.phase());
    }

    @Test
    public void noHoldOffGoesStraightToIdle() {
        reject.step(0, true, BRIGHT, Piece.BLUE_NECTAR, Alliance.RED, true, PRESENT, 2, BURST_MS, 0, MAX_BURSTS, WINDOW_MS);
        reject.step(50, true, BRIGHT, Piece.BLUE_NECTAR, Alliance.RED, true, PRESENT, 2, BURST_MS, 0, MAX_BURSTS, WINDOW_MS);
        reject.step(550, false, Double.NaN, Piece.UNKNOWN, Alliance.RED, true, PRESENT, 2, BURST_MS, 0, MAX_BURSTS, WINDOW_MS);
        assertEquals(Phase.IDLE, reject.phase());
    }

    @Test
    public void aPieceThatNeverLeftIsNotRejectedAgain() {
        blueOnRed(0);
        blueOnRed(50);
        // The sensor sees the same NECTAR all through the burst, the hold-off and after.
        for (long t = 100; t < 3000; t += 50) blueOnRed(t);
        assertEquals("one burst per arrival, never a chain", 1, reject.bursts());
        assertEquals(Phase.IDLE, reject.phase());
    }

    @Test
    public void aPieceBackAfterAClearReadingGetsAnotherBurst() {
        blueOnRed(0);
        blueOnRed(50);
        clear(400); // gone, during the burst
        tick(850);  // hold-off over
        blueOnRed(900);
        blueOnRed(950);
        assertTrue(reject.rejecting());
        assertEquals(2, reject.bursts());
    }

    @Test
    public void theCapStopsItUntilADifferentReading() {
        long t = 0;
        for (int i = 0; i < MAX_BURSTS; i++) {
            blueOnRed(t);
            blueOnRed(t + 50);
            assertTrue(reject.rejecting());
            clear(t + 100);
            tick(t + 850);
            t += 900;
        }
        assertEquals(MAX_BURSTS, reject.bursts());
        blueOnRed(t);
        blueOnRed(t + 50);
        assertFalse("the fourth within the window gives up", reject.rejecting());
        assertTrue(reject.justGaveUp());
        assertTrue(reject.gaveUp());
        assertEquals(MAX_BURSTS, reject.bursts());
        clear(t + 100);
        assertFalse("a different reading re-arms it", reject.gaveUp());
    }

    @Test
    public void theCapLetsGoOnceTheWindowPasses() {
        long t = 0;
        for (int i = 0; i < MAX_BURSTS; i++) {
            blueOnRed(t);
            blueOnRed(t + 50);
            clear(t + 100);
            tick(t + 850);
            t += 900;
        }
        t += 3000;
        blueOnRed(t);
        blueOnRed(t + 50);
        assertTrue(reject.rejecting());
    }

    @Test
    public void theSwitchOffEndsItAtOnce() {
        blueOnRed(0);
        blueOnRed(50);
        step(100, true, BRIGHT, Piece.BLUE_NECTAR, Alliance.RED, false, PRESENT, BURST_MS);
        assertEquals(Phase.IDLE, reject.phase());
    }

    @Test
    public void unsetNumbersNeverReject() {
        for (int i = 0; i < 5; i++) {
            step(i * 50L, true, BRIGHT, Piece.BLUE_NECTAR, Alliance.RED, true, Double.NaN, BURST_MS);
            step(i * 50L, true, BRIGHT, Piece.BLUE_NECTAR, Alliance.RED, true, PRESENT, Double.NaN);
            step(i * 50L, true, BRIGHT, Piece.BLUE_NECTAR, Alliance.RED, true, PRESENT, 0);
        }
        assertEquals(0, reject.bursts());
    }

    @Test
    public void readingsCountOnlyWhenFresh() {
        blueOnRed(0);
        step(20, false, BRIGHT, Piece.BLUE_NECTAR, Alliance.RED, true, PRESENT, BURST_MS);
        assertFalse(reject.rejecting());
    }

    /**
     * 20245, 5 Nov 2026 (23:55 Basic Drive, 25.4 to 26.5 s): a burst left the NECTAR at the mouth,
     * it rolled back under the sensor during the hold-off, then sat dim at the edge (0.07 to 0.08)
     * until the roller took it in. It must burst again as it rolls back, dim or not.
     */
    @Test
    public void aPieceRollingBackDuringTheHoldOffIsBurstAgainAtOnce() {
        double seenAbove = 0.05; // the colour sensor's leaveBelow
        long[] t = {25380, 25430};
        for (long ms : t) step(ms, true, 0.81, Piece.BLUE_NECTAR, Alliance.RED, true, seenAbove, BURST_MS);
        assertTrue(reject.rejecting());
        step(25681, true, 0.029, Piece.BLUE_NECTAR, Alliance.RED, true, seenAbove, BURST_MS); // gone: hue noise in the dark
        step(25950, false, Double.NaN, Piece.UNKNOWN, Alliance.RED, true, seenAbove, BURST_MS);
        assertTrue(reject.holdingOff());
        step(26165, true, 0.129, Piece.BLUE_NECTAR, Alliance.RED, true, seenAbove, BURST_MS);
        step(26215, true, 0.084, Piece.BLUE_NECTAR, Alliance.RED, true, seenAbove, BURST_MS);
        assertTrue("burst again during the hold-off", reject.rejecting());
        assertEquals(2, reject.bursts());
    }

    @Test
    public void anEmptyLaneIsNotAPieceWhateverItsHue() {
        // An empty lane reads about 0.02, and its hue wanders into blue's range (200° logged).
        for (int i = 0; i < 10; i++) {
            step(i * 50L, true, 0.023, Piece.BLUE_NECTAR, Alliance.RED, true, 0.05, BURST_MS);
        }
        assertEquals(0, reject.bursts());
    }
}
