package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.subsystems.FeedGateSubsystem.State;
import org.junit.Test;

/** The pure part of {@link FeedGateSubsystem}: when it opens, and where each state puts the servo. */
public class FeedGateSubsystemTest {

    private static final double EPS = 1e-9;

    @Test
    public void blocksUnlessTheFeederIsMovingAPiece() {
        assertEquals(State.BLOCKING, FeedGateSubsystem.requestFor(FeederSubsystem.State.IDLE));
    }

    @Test
    public void opensToFeedAndToClearAJam() {
        assertEquals(State.FEEDING, FeedGateSubsystem.requestFor(FeederSubsystem.State.FEED));
        assertEquals(State.FEEDING, FeedGateSubsystem.requestFor(FeederSubsystem.State.REVERSE));
    }

    @Test
    public void eachStateGoesToItsOwnPosition() {
        assertEquals(0.2, FeedGateSubsystem.positionFor(State.BLOCKING, 0.2, 0.7), EPS);
        assertEquals(0.7, FeedGateSubsystem.positionFor(State.FEEDING, 0.2, 0.7), EPS);
    }

    @Test
    public void aPositionNotMeasuredYetIsNotWritten() {
        assertTrue(Double.isNaN(FeedGateSubsystem.positionFor(State.BLOCKING, Double.NaN, 0.7)));
        assertTrue(Double.isNaN(FeedGateSubsystem.positionFor(State.FEEDING, 0.2, Double.NaN)));
        assertTrue("one end set is not enough: the gate could swing into the other",
                Double.isNaN(FeedGateSubsystem.positionFor(State.BLOCKING, 0.2, Double.NaN)));
    }

    @Test
    public void aPanelsTypoIsClampedToTheServosRange() {
        assertEquals(1.0, FeedGateSubsystem.positionFor(State.FEEDING, 0.2, 3.0), EPS);
        assertEquals(0.0, FeedGateSubsystem.positionFor(State.BLOCKING, -2.0, 0.7), EPS);
    }

    @Test
    public void bothCommittedPositionsAreSetTogetherOrNotAtAll() {
        // Measured on a robot, never guessed (CLAUDE.md): NaN until then. One end committed and not
        // the other is a half-finished measurement, and the gate would never move anyway.
        assertTrue("commit blockingPosition and feedingPosition together, with a comment saying which robot",
                Double.isNaN(FeedGateSubsystem.blockingPosition) == Double.isNaN(FeedGateSubsystem.feedingPosition));
    }
}
