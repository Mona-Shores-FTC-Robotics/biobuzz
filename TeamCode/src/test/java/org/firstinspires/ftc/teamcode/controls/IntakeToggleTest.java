package org.firstinspires.ftc.teamcode.controls;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** {@link IntakeToggle}: a press turns the intake on, the next turns it off, holding does nothing more. */
public class IntakeToggleTest {

    @Test
    public void aPressTurnsItOnAndTheNextOff() {
        IntakeToggle toggle = new IntakeToggle();
        assertFalse(toggle.update(true, false));
        assertTrue("press", toggle.update(true, true));
        assertTrue("still held: no second toggle", toggle.update(true, true));
        assertTrue("released: stays on", toggle.update(true, false));
        assertFalse("second press: off", toggle.update(true, true));
        assertFalse(toggle.update(true, false));
    }

    @Test
    public void disabledItNeverRunsAndForgetsTheLatch() {
        IntakeToggle toggle = new IntakeToggle();
        assertFalse("hold-to-run is the OpMode's when disabled", toggle.update(false, true));
        assertFalse(toggle.update(true, false));
        assertTrue("a press once enabled turns it on", toggle.update(true, true));
        assertFalse("switching it off drops the latch", toggle.update(false, false));
        assertFalse("and switching it back on starts off", toggle.update(true, false));
    }
}
