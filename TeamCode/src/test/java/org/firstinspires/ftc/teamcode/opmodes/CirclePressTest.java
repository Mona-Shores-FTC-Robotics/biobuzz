package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** The driver's Circle follows the operator's turret mode: AIM fires, LOCKED turns onto the CELL and then fires. */
public class CirclePressTest {

    @Test
    public void circleFollowsTheTurretModeNotTheRobotsConfig() {
        assertEquals(CirclePress.Kind.FIRE, CirclePress.kindFor(false, true));
        assertEquals(CirclePress.Kind.AIM_AND_FIRE, CirclePress.kindFor(false, false));
        // A robot whose turret is locked forward never aims it, whatever the mode says.
        assertEquals(CirclePress.Kind.AIM_AND_FIRE, CirclePress.kindFor(true, true));
        assertEquals(CirclePress.Kind.AIM_AND_FIRE, CirclePress.kindFor(true, false));
    }

    @Test
    public void lettingGoOrHandingBackStopsTheFire() {
        assertTrue(CirclePress.wantsFire(true, CirclePress.Kind.AIM_AND_FIRE, false));
        assertFalse("let go", CirclePress.wantsFire(false, CirclePress.Kind.AIM_AND_FIRE, false));
        assertFalse("a stick took the turn back", CirclePress.wantsFire(true, CirclePress.Kind.AIM_AND_FIRE, true));
        // A plain fire press has no turn to hand back.
        assertTrue(CirclePress.wantsFire(true, CirclePress.Kind.FIRE, true));
        assertFalse(CirclePress.wantsFire(false, CirclePress.Kind.FIRE, false));
    }

    @Test
    public void aimThenFire() {
        FireGates gates = new FireGates();
        double tol = 3.0;
        // Pressed: the launcher spins up as the robot starts to turn, 40° off the CELL. No feed.
        gates.evaluate(true, false, 40.0, tol);
        assertFalse(feed(gates));
        // Turning, at speed now, still 12° off. No feed.
        gates.evaluate(true, true, 12.0, tol);
        assertFalse(feed(gates));
        // On the CELL but the launcher dipped: no feed.
        gates.evaluate(true, false, 1.0, tol);
        assertFalse(feed(gates));
        // On the CELL and at speed: feed.
        gates.evaluate(true, true, -2.5, tol);
        assertTrue(feed(gates));
        // No aim at all (no CELL, no pose): it does not fire blind.
        gates.evaluate(true, true, Double.NaN, tol);
        assertFalse(feed(gates));
    }

    @Test
    public void anotherGateStillHoldsAnAimAndFire() {
        FireGates gates = new FireGates();
        gates.evaluate(true, true, 0.0, 3.0);
        assertFalse("the firing arc, for one", CirclePress.mayFeed(CirclePress.Kind.AIM_AND_FIRE, false,
                gates.verdict(FireGates.Gate.AIM_ON_TARGET), true));
    }

    @Test
    public void aFirePressIsTheOperatorsFire() {
        assertTrue(CirclePress.mayFeed(CirclePress.Kind.FIRE, true, FireGates.Verdict.BLOCK, false));
        assertFalse(CirclePress.mayFeed(CirclePress.Kind.FIRE, false, FireGates.Verdict.PASS, true));
    }

    private static boolean feed(FireGates gates) {
        return CirclePress.mayFeed(CirclePress.Kind.AIM_AND_FIRE, gates.enforcedOpen(false, true),
                gates.verdict(FireGates.Gate.AIM_ON_TARGET), gates.verdict(FireGates.Gate.LAUNCHER_AT_SPEED) == FireGates.Verdict.PASS);
    }
}
