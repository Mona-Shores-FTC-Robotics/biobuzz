package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Circle in AIM with the CELL behind the arc turns the robot just far enough to bring it in (20 Nov 2026). */
public class ArcTurnTest {

    private static final double EPS = 1e-9;

    @Test
    public void insideTheArcNoTurn() {
        assertEquals(0.0, ArcTurn.errorDeg(0.0, 0.0, 90.0, 90.0, 10.0), EPS);
        assertEquals(0.0, ArcTurn.errorDeg(79.0, 0.0, 90.0, 90.0, 10.0), EPS);
        assertEquals(0.0, ArcTurn.errorDeg(-79.0, 0.0, 90.0, 90.0, 10.0), EPS);
    }

    @Test
    public void behindOnTheLeftTurnsLeftToTheMargin() {
        // Aim 120° left, arc 90° left, 10° margin: turn 40° left so the aim sits at 80°.
        assertEquals(40.0, ArcTurn.errorDeg(120.0, 0.0, 90.0, 90.0, 10.0), EPS);
    }

    @Test
    public void behindOnTheRightTurnsRight() {
        assertEquals(-40.0, ArcTurn.errorDeg(-120.0, 0.0, 90.0, 90.0, 10.0), EPS);
    }

    @Test
    public void theCentreIsTheAimTrim() {
        // Trim +30°: the left edge is at 120°, so 125° needs 15° to reach 110°.
        assertEquals(15.0, ArcTurn.errorDeg(125.0, 30.0, 90.0, 90.0, 10.0), EPS);
        // -150° is 180° from a +30° centre, which wraps to +180: the left side, 100° to come in.
        assertEquals(100.0, ArcTurn.errorDeg(-150.0, 30.0, 90.0, 90.0, 10.0), EPS);
        // -100° is 130° right of the centre: 50° right to the margin, at -50°.
        assertEquals(-50.0, ArcTurn.errorDeg(-100.0, 30.0, 90.0, 90.0, 10.0), EPS);
    }

    @Test
    public void straightBehindTakesTheWrappedSide() {
        // 180° wraps to 180 (> 0): the left side, 100° to come in to 80°.
        assertEquals(100.0, ArcTurn.errorDeg(180.0, 0.0, 90.0, 90.0, 10.0), EPS);
    }

    @Test
    public void aMarginTooBigIsHalfTheNarrowerSide() {
        // Margin 100 on a 60° side is held to 30: aim 70° left of a 60° edge turns to 30°.
        assertEquals(40.0, ArcTurn.errorDeg(70.0, 0.0, 60.0, 90.0, 100.0), EPS);
    }

    @Test
    public void noAimOrNoArcIsNaN() {
        assertTrue(Double.isNaN(ArcTurn.errorDeg(Double.NaN, 0.0, 90.0, 90.0, 10.0)));
        assertTrue(Double.isNaN(ArcTurn.errorDeg(120.0, 0.0, Double.NaN, 90.0, 10.0)));
        assertTrue(Double.isNaN(ArcTurn.errorDeg(120.0, 0.0, 180.0, 180.0, 10.0)));
    }

    @Test
    public void aNaNMarginIsTheEdge() {
        assertEquals(30.0, ArcTurn.errorDeg(120.0, 0.0, 90.0, 90.0, Double.NaN), EPS);
    }

    @Test
    public void startsOnlyOutsideAndRunsToTheMargin() {
        // Inside the arc but short of the margin: a press there does not turn.
        assertFalse(ArcTurn.turning(false, 5.0, false, 1.5));
        // Outside: it starts.
        assertTrue(ArcTurn.turning(false, 40.0, true, 1.5));
        // Back inside the edge but not yet at the margin: it keeps going.
        assertTrue(ArcTurn.turning(true, 5.0, false, 1.5));
        // Within the done band: it stops.
        assertFalse(ArcTurn.turning(true, 1.0, false, 1.5));
        // No aim stops it.
        assertFalse(ArcTurn.turning(true, Double.NaN, true, 1.5));
    }

    @Test
    public void onByDefault() {
        assertTrue(TeleOpConfig.circleTurnsIntoArc);
    }
}
