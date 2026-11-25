package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;

public class HeadingLockTest {

    private static final double EPS = 1e-9;

    private final double cone = TeleOpConfig.headingLockConeDeg;
    private final double step = TeleOpConfig.headingLockStepDeg;
    private final double kd = TeleOpConfig.headingLockKd;

    @After
    public void restore() {
        TeleOpConfig.headingLockConeDeg = cone;
        TeleOpConfig.headingLockStepDeg = step;
        TeleOpConfig.headingLockKd = kd;
    }

    private static double rad(double deg) {
        return Math.toRadians(deg);
    }

    private static double targetDegAfterPressAt(double headingDeg) {
        HeadingLock lock = new HeadingLock();
        lock.update(true, true, rad(headingDeg), 0.0, 0.0, 0.0, 0.0);
        return Math.toDegrees(lock.targetRad());
    }

    @Test
    public void thePressPicksTheNearestCardinal() {
        assertEquals(0.0, targetDegAfterPressAt(44.0), EPS);
        assertEquals(90.0, targetDegAfterPressAt(46.0), EPS);
        assertEquals(90.0, targetDegAfterPressAt(120.0), EPS);
        assertEquals(-90.0, targetDegAfterPressAt(-100.0), EPS);
        assertEquals(0.0, targetDegAfterPressAt(-10.0), EPS);
        // Across the wrap: 170° and -170° both go to 180, written -180.
        assertEquals(180.0, Math.abs(targetDegAfterPressAt(170.0)), EPS);
        assertEquals(180.0, Math.abs(targetDegAfterPressAt(-170.0)), EPS);
        // A heading that has wound past a turn still finds its cardinal.
        assertEquals(90.0, targetDegAfterPressAt(360.0 + 80.0), EPS);
    }

    @Test
    public void theTargetIsFixedForTheWholeHold() {
        HeadingLock lock = new HeadingLock();
        lock.update(true, true, rad(30.0), 0.0, 0.0, 0.0, 0.0);
        assertTrue(lock.pressed());
        lock.update(true, true, rad(60.0), 0.0, 0.0, 0.0, 0.0);
        assertFalse(lock.pressed());
        assertEquals("not re-picked as the robot turns", 0.0, Math.toDegrees(lock.targetRad()), EPS);
    }

    @Test
    public void itTurnsTowardTheTargetAndIgnoresTheRightStick() {
        HeadingLock lock = new HeadingLock();
        // At 30°, the target is 0: turn clockwise (negative), whatever the stick asks.
        lock.update(true, true, rad(30.0), 0.0, 0.0, 0.0, 1.0);
        assertTrue(lock.turn() < 0.0);
        assertTrue(Math.abs(lock.turn()) <= TeleOpConfig.headingLockMaxTurn + EPS);
        // At -20°, counter-clockwise.
        lock.update(true, true, rad(-20.0), 0.0, 0.0, 0.0, -1.0);
        assertTrue(lock.turn() > 0.0);
        // On the heading, nothing.
        lock.update(true, true, rad(0.5), 0.0, 0.0, 0.0, 1.0);
        assertEquals(0.0, lock.turn(), EPS);
    }

    @Test
    public void theFloorKeepsASmallErrorTurningAndTheDTermDamps() {
        HeadingLock lock = new HeadingLock();
        lock.update(true, true, rad(4.0), 0.0, 0.0, 0.0, 0.0);
        assertEquals(-TeleOpConfig.headingLockMinTurn, lock.turn(), EPS);
        TeleOpConfig.headingLockKd = 0.2;
        // Swinging clockwise onto 0 at 1 rad/s from 20°: the D term takes power off the turn.
        double p = HeadingLock.holdTurn(rad(-20.0), 0.0);
        double pd = HeadingLock.holdTurn(rad(-20.0), -1.0);
        assertTrue(pd > p);
    }

    @Test
    public void theLeftStickSnapsInsideTheConeAtFullMagnitude() {
        HeadingLock lock = new HeadingLock();
        // 15° off forward: straight forward, the stick's whole magnitude.
        double f = Math.cos(rad(15.0));
        double s = Math.sin(rad(15.0));
        lock.update(true, true, 0.0, 0.0, 0.8 * f, 0.8 * s, 0.0);
        assertEquals(0.8, lock.forward(), EPS);
        assertEquals(0.0, lock.strafe(), EPS);
        // 10° off a left strafe: straight left.
        lock.update(true, true, 0.0, 0.0, Math.sin(rad(10.0)), Math.cos(rad(10.0)), 0.0);
        assertEquals(0.0, lock.forward(), EPS);
        assertEquals(1.0, lock.strafe(), EPS);
        // Back and right.
        lock.update(true, true, 0.0, 0.0, -0.5, -0.05, 0.0);
        assertEquals(-Math.hypot(0.5, 0.05), lock.forward(), EPS);
        assertEquals(0.0, lock.strafe(), EPS);
        lock.update(true, true, 0.0, 0.0, 0.05, -0.5, 0.0);
        assertEquals(0.0, lock.forward(), EPS);
        assertEquals(-Math.hypot(0.5, 0.05), lock.strafe(), EPS);
    }

    @Test
    public void outsideTheConeTheStickPassesThrough() {
        HeadingLock lock = new HeadingLock();
        // A diagonal, 45° off both axes.
        lock.update(true, true, 0.0, 0.0, 0.6, 0.6, 0.0);
        assertEquals(0.6, lock.forward(), EPS);
        assertEquals(0.6, lock.strafe(), EPS);
        // 30° off, outside the 20° cone.
        lock.update(true, true, 0.0, 0.0, Math.cos(rad(30.0)), Math.sin(rad(30.0)), 0.0);
        assertEquals(Math.cos(rad(30.0)), lock.forward(), EPS);
        assertEquals(Math.sin(rad(30.0)), lock.strafe(), EPS);
        // A centred stick stays centred.
        lock.update(true, true, 0.0, 0.0, 0.0, 0.0, 0.0);
        assertEquals(0.0, lock.forward(), EPS);
        assertEquals(0.0, lock.strafe(), EPS);
    }

    @Test
    public void releaseHandsTheSticksBackExactlyTheNextLoop() {
        HeadingLock lock = new HeadingLock();
        lock.update(true, true, rad(30.0), 0.0, 0.9, 0.1, 0.7);
        assertTrue(lock.holding());
        lock.update(false, true, rad(25.0), 0.0, 0.9, 0.1, 0.7);
        assertTrue(lock.released());
        assertFalse(lock.holding());
        assertFalse(lock.held());
        assertTrue(Double.isNaN(lock.targetRad()));
        assertEquals(0.9, lock.forward(), EPS);
        assertEquals(0.1, lock.strafe(), EPS);
        assertEquals(0.7, lock.turn(), EPS);
        lock.update(false, true, rad(25.0), 0.0, 0.2, -0.3, -0.4);
        assertFalse(lock.released());
        assertEquals(-0.4, lock.turn(), EPS);
        // The next press picks afresh from where the robot is.
        lock.update(true, true, rad(100.0), 0.0, 0.0, 0.0, 0.0);
        assertEquals(90.0, Math.toDegrees(lock.targetRad()), EPS);
    }

    @Test
    public void withNoHeadingItOnlySnapsTheStick() {
        HeadingLock lock = new HeadingLock();
        lock.update(true, false, rad(30.0), 0.0, 1.0, 0.1, 0.5);
        assertFalse(lock.holding());
        assertTrue(lock.held());
        assertTrue(Double.isNaN(lock.targetRad()));
        assertEquals(0.5, lock.turn(), EPS);
        assertEquals(Math.hypot(1.0, 0.1), lock.forward(), EPS);
        assertEquals(0.0, lock.strafe(), EPS);
        // The heading comes back mid-hold: it picks the cardinal from there.
        lock.update(true, true, rad(80.0), 0.0, 0.0, 0.0, 0.0);
        assertTrue(lock.holding());
        assertEquals(90.0, Math.toDegrees(lock.targetRad()), EPS);
        // And a fault mid-hold gives the right stick back.
        lock.update(true, false, rad(80.0), 0.0, 0.0, 0.0, 0.3);
        assertEquals(0.3, lock.turn(), EPS);
    }

    @Test
    public void badTunablesAreGuarded() {
        TeleOpConfig.headingLockConeDeg = Double.NaN;
        assertEquals(0.0, HeadingLock.coneDeg(), EPS);
        TeleOpConfig.headingLockConeDeg = 90.0;
        assertEquals(45.0, HeadingLock.coneDeg(), EPS);
        TeleOpConfig.headingLockConeDeg = -5.0;
        assertEquals(0.0, HeadingLock.coneDeg(), EPS);
        HeadingLock lock = new HeadingLock();
        lock.update(true, true, 0.0, 0.0, 1.0, 0.1, 0.0);
        assertEquals("no cone, no snap", 0.1, lock.strafe(), EPS);

        TeleOpConfig.headingLockStepDeg = Double.NaN;
        assertEquals(90.0, HeadingLock.stepDeg(), EPS);
        TeleOpConfig.headingLockStepDeg = 0.0;
        assertEquals(90.0, HeadingLock.stepDeg(), EPS);
        TeleOpConfig.headingLockStepDeg = 1.0;
        assertEquals(15.0, HeadingLock.stepDeg(), EPS);
        TeleOpConfig.headingLockStepDeg = 1000.0;
        assertEquals(180.0, HeadingLock.stepDeg(), EPS);
        TeleOpConfig.headingLockStepDeg = 44.0;
        assertEquals(45.0, HeadingLock.stepDeg(), EPS);
        assertEquals(45.0, targetDegAfterPressAt(50.0), EPS);
    }
}
