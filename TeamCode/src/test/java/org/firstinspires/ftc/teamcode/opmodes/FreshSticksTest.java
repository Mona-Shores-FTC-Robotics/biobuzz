package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** {@link FreshSticks}: only a stick moved after the press takes the robot back (#495). */
public class FreshSticksTest {

    private static final double AFTER_S = 0.6;

    @Test
    public void stickAlreadyMovingAtThePressDoesNotCancel() {
        FreshSticks s = new FreshSticks();
        assertFalse(s.moved(true, true, 0.00, AFTER_S));
        assertFalse(s.moved(true, true, 0.02, AFTER_S));
        assertTrue(s.ignoring());
        assertFalse(s.moved(true, true, 0.30, AFTER_S));
    }

    @Test
    public void stickMovedAfterThePressCancelsAtOnce() {
        FreshSticks s = new FreshSticks();
        assertFalse(s.moved(true, false, 0.00, AFTER_S));
        assertTrue(s.moved(true, true, 0.02, AFTER_S));
    }

    @Test
    public void staleStickCancelsOnceItHasCentredAndMovedAgain() {
        FreshSticks s = new FreshSticks();
        assertFalse(s.moved(true, true, 0.00, AFTER_S));
        assertFalse(s.moved(true, false, 0.10, AFTER_S));
        assertFalse(s.ignoring());
        assertTrue(s.moved(true, true, 0.20, AFTER_S));
    }

    @Test
    public void stickStillPushedAfterTheGraceCancels() {
        FreshSticks s = new FreshSticks();
        assertFalse(s.moved(true, true, 0.00, AFTER_S));
        assertFalse(s.moved(true, true, 0.59, AFTER_S));
        assertTrue(s.moved(true, true, 0.60, AFTER_S));
        assertTrue(s.moved(true, true, 0.62, AFTER_S));
    }

    @Test
    public void eachPressStartsAgain() {
        FreshSticks s = new FreshSticks();
        assertFalse(s.moved(true, true, 0.00, AFTER_S));
        assertTrue(s.moved(true, true, 0.70, AFTER_S));
        assertTrue(s.moved(false, true, 0.80, AFTER_S)); // not held: the raw stick
        assertFalse(s.moved(true, true, 0.90, AFTER_S)); // a new press, the stick still moving
    }

    @Test
    public void zeroOrNanGraceIsTheOldRule() {
        assertTrue(new FreshSticks().moved(true, true, 0.0, 0.0));
        assertTrue(new FreshSticks().moved(true, true, 0.0, Double.NaN));
        assertTrue(new FreshSticks().moved(true, true, 0.0, -1.0));
    }
}
