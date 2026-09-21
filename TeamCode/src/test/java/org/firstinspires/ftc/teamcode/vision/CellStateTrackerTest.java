package org.firstinspires.ftc.teamcode.vision;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Covers the up/down classification and the dwell filter that gates it.
 *
 * <p>The behaviour worth protecting is the asymmetry: confirming a state is slow,
 * losing one is instant. This is what stops a stale UP surviving into the middle of
 * a tip, where it would feed a badly wrong pose into localization.
 */
public class CellStateTrackerTest {

    private static final double UP = 40.0;
    private static final double DOWN = 20.0;
    private static final double TOL = 3.0;

    private static HiveCellState classify(double heightIn) {
        return CellStateTracker.classifyByHeight(heightIn, UP, DOWN, TOL);
    }

    private static long ms(long value) {
        return value * 1_000_000L;
    }

    @Test
    public void heightsNearANominalClassifyToThatState() {
        assertEquals(HiveCellState.UP, classify(40.0));
        assertEquals(HiveCellState.UP, classify(42.9));
        assertEquals(HiveCellState.UP, classify(37.1));
        assertEquals(HiveCellState.DOWN, classify(20.0));
        assertEquals(HiveCellState.DOWN, classify(22.9));
        assertEquals(HiveCellState.DOWN, classify(17.1));
    }

    /** Mid-tip heights match neither nominal, which is the transition signal. */
    @Test
    public void heightsBetweenTheStatesAreUnknown() {
        assertEquals(HiveCellState.UNKNOWN, classify(30.0));
        assertEquals(HiveCellState.UNKNOWN, classify(25.0));
        assertEquals(HiveCellState.UNKNOWN, classify(36.0));
        assertEquals(HiveCellState.UNKNOWN, classify(100.0));
    }

    /** Unmeasured geometry must not produce a guess. */
    @Test
    public void unmeasuredGeometryIsAlwaysUnknown() {
        assertEquals(HiveCellState.UNKNOWN,
                CellStateTracker.classifyByHeight(40.0, Double.NaN, DOWN, TOL));
        assertEquals(HiveCellState.UNKNOWN,
                CellStateTracker.classifyByHeight(40.0, UP, Double.NaN, TOL));
        assertEquals(HiveCellState.UNKNOWN,
                CellStateTracker.classifyByHeight(Double.NaN, UP, DOWN, TOL));
    }

    /** Bands that overlap cannot distinguish the states, so it declines to try. */
    @Test
    public void overlappingBandsAreUnknownRatherThanNearest() {
        assertEquals(HiveCellState.UNKNOWN,
                CellStateTracker.classifyByHeight(40.0, 40.0, 44.0, 3.0));
        assertEquals(HiveCellState.UNKNOWN,
                CellStateTracker.classifyByHeight(40.0, UP, DOWN, 0.0));
    }

    @Test
    public void confirmingAStateNeedsBothSamplesAndDwell() {
        CellStateTracker tracker = new CellStateTracker(3, 250L);

        assertEquals(HiveCellState.UNKNOWN, tracker.update(HiveCellState.UP, ms(0)));
        assertEquals(HiveCellState.UNKNOWN, tracker.update(HiveCellState.UP, ms(100)));
        // Third sample, but only 200ms of dwell.
        assertEquals(HiveCellState.UNKNOWN, tracker.update(HiveCellState.UP, ms(200)));
        // Dwell satisfied.
        assertEquals(HiveCellState.UP, tracker.update(HiveCellState.UP, ms(260)));
        assertTrue(tracker.isSettled());
    }

    @Test
    public void enoughDwellStillNeedsEnoughSamples() {
        CellStateTracker tracker = new CellStateTracker(3, 100L);
        assertEquals(HiveCellState.UNKNOWN, tracker.update(HiveCellState.DOWN, ms(0)));
        assertEquals(HiveCellState.UNKNOWN, tracker.update(HiveCellState.DOWN, ms(5000)));
        assertEquals(HiveCellState.DOWN, tracker.update(HiveCellState.DOWN, ms(5001)));
    }

    /** One unclassifiable frame drops the settled state immediately. */
    @Test
    public void asingleUnknownClearsASettledState() {
        CellStateTracker tracker = new CellStateTracker(2, 0L);
        tracker.update(HiveCellState.UP, ms(0));
        assertEquals(HiveCellState.UP, tracker.update(HiveCellState.UP, ms(50)));

        assertEquals(HiveCellState.UNKNOWN, tracker.update(HiveCellState.UNKNOWN, ms(60)));
        assertFalse(tracker.isSettled());
    }

    /** A tip: settled UP, transition, then settled DOWN — never a stale UP in between. */
    @Test
    public void aTipNeverReportsTheOldStateMidTransition() {
        CellStateTracker tracker = new CellStateTracker(2, 50L);

        tracker.update(HiveCellState.UP, ms(0));
        assertEquals(HiveCellState.UP, tracker.update(HiveCellState.UP, ms(100)));

        for (int i = 0; i < 5; i++) {
            assertEquals("mid-tip frame " + i,
                    HiveCellState.UNKNOWN, tracker.update(HiveCellState.UNKNOWN, ms(150 + i * 20L)));
        }

        assertEquals(HiveCellState.UNKNOWN, tracker.update(HiveCellState.DOWN, ms(300)));
        assertEquals(HiveCellState.DOWN, tracker.update(HiveCellState.DOWN, ms(400)));
    }

    /** Flapping between states never confirms either. */
    @Test
    public void alternatingObservationsNeverSettle() {
        CellStateTracker tracker = new CellStateTracker(3, 0L);
        for (int i = 0; i < 10; i++) {
            HiveCellState observed = (i % 2 == 0) ? HiveCellState.UP : HiveCellState.DOWN;
            assertEquals(HiveCellState.UNKNOWN, tracker.update(observed, ms(i * 20L)));
        }
    }

    @Test
    public void resetClearsEverything() {
        CellStateTracker tracker = new CellStateTracker(1, 0L);
        assertEquals(HiveCellState.UP, tracker.update(HiveCellState.UP, ms(0)));
        tracker.reset();
        assertEquals(HiveCellState.UNKNOWN, tracker.settledState());
        assertFalse(tracker.isSettled());
        assertEquals(0, tracker.candidateSamples());
    }

    @Test
    public void nullObservationIsTreatedAsUnknown() {
        CellStateTracker tracker = new CellStateTracker(1, 0L);
        assertEquals(HiveCellState.UP, tracker.update(HiveCellState.UP, ms(0)));
        assertEquals(HiveCellState.UNKNOWN, tracker.update(null, ms(10)));
    }
}
