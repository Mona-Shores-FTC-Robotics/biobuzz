package org.firstinspires.ftc.teamcode.controls;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LongPressTest {

    private static final long MS = 1_000_000L;
    private final LongPress press = new LongPress(1000);

    @Test
    public void aTapDoesNothing() {
        assertFalse(press.update(true, 0));
        assertFalse(press.update(true, 500 * MS));
        assertFalse(press.update(false, 600 * MS));
        assertFalse(press.update(false, 2000 * MS));
    }

    @Test
    public void firesOnceWhenTheHoldReachesItsLength() {
        assertFalse(press.update(true, 0));
        assertTrue(press.pending());
        assertFalse(press.update(true, 999 * MS));
        assertTrue(press.update(true, 1000 * MS));
        assertFalse(press.update(true, 3000 * MS));
        assertFalse(press.pending());
    }

    @Test
    public void letGoAndHoldAgainStartsOver() {
        press.update(true, 0);
        press.update(true, 1000 * MS);
        press.update(false, 1100 * MS);
        assertFalse(press.update(true, 1200 * MS));
        assertFalse(press.update(true, 2100 * MS));
        assertTrue(press.update(true, 2200 * MS));
    }
}
