package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** The turret's firing arc: what it lets fire, where it holds, and which way it turns. Limits ±100°. */
public class TurretArcTest {

    private static final double L = 100.0, R = 100.0;

    @Test
    public void insideTheArcIsAllowedAndBehindIsNot() {
        assertTrue(TurretArc.contains(0.0, L, R));
        assertTrue(TurretArc.contains(100.0, L, R));
        assertTrue(TurretArc.contains(-100.0, L, R));
        assertFalse(TurretArc.contains(101.0, L, R));
        assertFalse(TurretArc.contains(-101.0, L, R));
        assertFalse(TurretArc.contains(180.0, L, R));
        // A direction: one turn round is the same direction.
        assertTrue(TurretArc.contains(370.0, L, R));
        assertFalse(TurretArc.contains(-190.0, L, R));
    }

    @Test
    public void noArcLetsEverythingThrough() {
        assertFalse(TurretArc.active(Double.NaN, R));
        assertTrue(TurretArc.contains(180.0, Double.NaN, R));
        assertTrue(TurretArc.allowsFeed(180.0, 180.0, Double.NaN, Double.NaN));
        // An arc that covers the whole turn is no arc.
        assertFalse(TurretArc.active(180.0, 180.0));
    }

    @Test
    public void theFeedWaitsWhileTheTurretOrTheAimIsBehind() {
        assertTrue(TurretArc.allowsFeed(20.0, 25.0, L, R));
        assertFalse("the CELL is behind", TurretArc.allowsFeed(100.0, 160.0, L, R));
        assertFalse("the turret is still swinging through the back", TurretArc.allowsFeed(150.0, 30.0, L, R));
        assertTrue("no target: the turret decides", TurretArc.allowsFeed(30.0, Double.NaN, L, R));
        assertTrue("no turret reading: the aim decides", TurretArc.allowsFeed(Double.NaN, 30.0, L, R));
        assertFalse(TurretArc.allowsFeed(Double.NaN, -170.0, L, R));
    }

    @Test
    public void aTargetBehindIsHeldAtTheNearerEdge() {
        assertEquals(40.0, TurretArc.hold(40.0, Double.NaN, L, R), 0.0);
        assertEquals(L, TurretArc.hold(130.0, Double.NaN, L, R), 0.0);
        assertEquals(-R, TurretArc.hold(-130.0, Double.NaN, L, R), 0.0);
        assertEquals(-R, TurretArc.hold(230.0, Double.NaN, L, R), 0.0);
    }

    @Test
    public void aTargetStraightBehindDoesNotFlickBetweenEdges() {
        // Held on the left edge, the target wanders across the back: the turret stays on the left.
        double edge = TurretArc.hold(175.0, Double.NaN, L, R);
        assertEquals(L, edge, 0.0);
        edge = TurretArc.hold(-178.0, edge, L, R);
        assertEquals(L, edge, 0.0);
        edge = TurretArc.hold(-120.0, edge, L, R);
        assertEquals(L, edge, 0.0);
        // Back inside the arc, it follows the target again; behind once more, the nearer edge.
        assertEquals(-80.0, TurretArc.hold(-80.0, edge, L, R), 0.0);
        assertEquals(-R, TurretArc.hold(-120.0, Double.NaN, L, R), 0.0);
    }

    @Test
    public void theTurretNeverTurnsThroughTheBack() {
        // From the left edge to the right edge: the long way round the front, not the short way behind.
        assertEquals(-200.0, TurretArc.errorDeg(-100.0, 100.0, L, R), 1e-9);
        // Pushed round into the back, it comes back the way that stays clear of 180.
        assertEquals(-170.0, TurretArc.errorDeg(0.0, 170.0, L, R), 1e-9);
        assertEquals(270.0, TurretArc.errorDeg(100.0, -170.0, L, R), 1e-9);
        // With no arc it is the shortest way, as before.
        assertEquals(160.0, TurretArc.errorDeg(-100.0, 100.0, Double.NaN, Double.NaN), 1e-9);
    }
}
