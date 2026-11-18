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

    // ---------------------------------------------------------------- centred on the aim trim
    // 18 Nov 2026: a turret zeroed 60° off, the operator trimmed +60°. The robot's real forward
    // reads +60 on the encoder, so the arc (±90 here) is +60 ± 90: encoder -30 to +150.
    private static final double TRIM = 60.0, L90 = 90.0, R90 = 90.0;

    @Test
    public void withTrimAnAimAtRealPlus80FeedsAtEncoderPlus140() {
        assertTrue(TurretArc.contains(140.0, TRIM, L90, R90));
        assertTrue(TurretArc.allowsFeed(140.0, 140.0, TRIM, L90, R90));
        // Without the centre, +140 is behind a ±90 arc: what refused Circle 40 times tonight.
        assertFalse(TurretArc.allowsFeed(140.0, 140.0, 0.0, L90, R90));
        // The real right edge moved as well: encoder -40 is real -100, behind.
        assertFalse(TurretArc.contains(-40.0, TRIM, L90, R90));
        assertTrue(TurretArc.contains(-30.0, TRIM, L90, R90));
    }

    @Test
    public void withTrimAnAimAtRealPlus100IsHeldAtEncoderPlus150() {
        // Real +100 is encoder +160: held at the left edge, encoder +150, real +90.
        assertEquals(150.0, TurretArc.hold(160.0, Double.NaN, TRIM, L90, R90), 1e-9);
        // Inside, the aim itself, exactly.
        assertEquals(140.0, TurretArc.hold(140.0, Double.NaN, TRIM, L90, R90), 0.0);
        // Straight behind the real robot (encoder -120) and wandering: it keeps the edge it had.
        double edge = TurretArc.hold(-125.0, Double.NaN, TRIM, L90, R90);
        assertEquals(150.0, edge, 1e-9);
        edge = TurretArc.hold(-115.0, edge, TRIM, L90, R90);
        assertEquals(150.0, edge, 1e-9);
        // Entering on the right of the real back it takes the right edge, encoder -30.
        assertEquals(-30.0, TurretArc.hold(-100.0, Double.NaN, TRIM, L90, R90), 1e-9);
    }

    @Test
    public void withTrimEdgeToEdgeGoesRoundTheRealFront() {
        // From the left edge (encoder +150) to the right (encoder -30): 180° clockwise through the
        // real front (+60), not 180° the other way through the real back (-120).
        assertEquals(-180.0, TurretArc.errorDeg(-30.0, 150.0, TRIM, L90, R90), 1e-9);
        // Back to the real front (encoder +60) from the left edge: 90° clockwise.
        assertEquals(-90.0, TurretArc.errorDeg(60.0, 150.0, TRIM, L90, R90), 1e-9);
        // Pushed round past the left edge to encoder +170 (real +110), it comes back through the
        // real front, never on through encoder 180 (real +120) into the real back.
        assertEquals(-200.0, TurretArc.errorDeg(-30.0, 170.0, TRIM, L90, R90), 1e-9);
        // With no centre the same move would take the short way through encoder 180.
        assertEquals(160.0, TurretArc.errorDeg(-30.0, 170.0, Double.NaN, Double.NaN), 1e-9);
    }

    @Test
    public void aZeroCentreIsTheOldArc() {
        assertEquals(TurretArc.hold(130.0, Double.NaN, L, R), TurretArc.hold(130.0, Double.NaN, 0.0, L, R), 0.0);
        assertEquals(TurretArc.errorDeg(-100.0, 100.0, L, R), TurretArc.errorDeg(-100.0, 100.0, 0.0, L, R), 0.0);
        assertTrue(TurretArc.contains(95.0, Double.NaN, L, R));
    }
}
