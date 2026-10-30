package org.firstinspires.ftc.teamcode.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** A throttle is due at once, then once per period; an offset shifts the first one. */
public class ThrottleTest {

    private static final long MS = 1_000_000L;

    @Test
    public void dueAtOnceThenOncePerPeriod() {
        Throttle t = new Throttle(100, 0);
        assertTrue(t.due(0));
        assertFalse(t.due(20 * MS));
        assertFalse(t.due(99 * MS));
        assertTrue(t.due(100 * MS));
        assertFalse(t.due(150 * MS));
        assertTrue(t.due(210 * MS));
    }

    @Test
    public void resetMakesItDueAgain() {
        Throttle t = new Throttle(100, 0);
        assertTrue(t.due(0));
        t.reset();
        assertTrue(t.due(10 * MS));
        assertFalse(t.due(50 * MS));
    }

    @Test
    public void anOffsetDelaysTheFirstOne() {
        Throttle t = new Throttle(100, 40);
        assertFalse(t.due(0));
        assertFalse(t.due(39 * MS));
        assertTrue(t.due(40 * MS));
        assertFalse(t.due(120 * MS));
        assertTrue(t.due(140 * MS));
    }
}
