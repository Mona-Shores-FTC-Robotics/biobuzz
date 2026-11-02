package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Full is a piece held under the sensor while taking in; a passing piece is not. */
public class LaneFullTest {

    private static final double PRESENT = 0.10;
    private static final double LEAVE = 0.05;
    private static final double FULL_AFTER = 500;
    private static final double EMPTY = 0.02;
    private static final double BALL = 0.3;

    private final LaneFull lane = new LaneFull();
    private long now;

    /** Readings every 50 ms, as the colour sensor reads, for {@code ms}. */
    private void hold(double brightness, boolean intaking, long ms) {
        for (long end = now + ms; now < end; now += 50) {
            lane.step(now, brightness, intaking, PRESENT, LEAVE, FULL_AFTER);
        }
    }

    @Test
    public void aPassingPieceIsNotFullAndItsDwellIsReported() {
        hold(EMPTY, true, 200);
        hold(BALL, true, 150);
        assertTrue(lane.present());
        assertFalse(lane.full());
        lane.step(now, EMPTY, true, PRESENT, LEAVE, FULL_AFTER);
        assertEquals(150, lane.endedDwellMs(), 1e-9);
        assertFalse(lane.endedFull());
        now += 50;
        lane.step(now, EMPTY, true, PRESENT, LEAVE, FULL_AFTER);
        assertTrue("reported once", Double.isNaN(lane.endedDwellMs()));
    }

    @Test
    public void aPieceHeldWhileTakingInIsFull() {
        hold(BALL, true, 500);
        assertFalse("not yet: 450 ms of intaking", lane.full());
        hold(BALL, true, 100);
        assertTrue(lane.full());
    }

    @Test
    public void heldWithTheIntakeIdleIsNotFull() {
        hold(BALL, false, 2000);
        assertFalse(lane.full());
        assertEquals(0, lane.heldWhileIntakingMs(), 1e-9);
    }

    @Test
    public void fullStaysWhileThePieceStaysAndClearsWhenItLeaves() {
        hold(BALL, true, 700);
        assertTrue(lane.full());
        hold(BALL, false, 1000);
        assertTrue("the roller stopping for it does not undo it", lane.full());
        hold(0.07, false, 100);
        assertTrue("hysteresis: still above leaveBelow", lane.full());
        lane.step(now, EMPTY, false, PRESENT, LEAVE, FULL_AFTER);
        assertFalse(lane.full());
        assertTrue(lane.endedFull());
    }

    @Test
    public void aNewPieceStartsFromZero() {
        hold(BALL, true, 400);
        hold(EMPTY, true, 100);
        hold(BALL, true, 400);
        assertFalse(lane.full());
    }

    @Test
    public void nanTurnsItOff() {
        for (int i = 0; i < 40; i++, now += 50) {
            lane.step(now, BALL, true, PRESENT, LEAVE, Double.NaN);
        }
        assertFalse(lane.full());
        for (int i = 0; i < 40; i++, now += 50) {
            lane.step(now, BALL, true, Double.NaN, LEAVE, FULL_AFTER);
        }
        assertFalse(lane.present());
        assertFalse(lane.full());
    }
}
