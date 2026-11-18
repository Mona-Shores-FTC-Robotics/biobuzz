package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** The at-speed rumble goes once per spin-up, however often at speed flaps (18 Nov 2026). */
public class SpinUpRumbleTest {

    @Test
    public void rumblesOnceWhenASpinUpFirstReachesSpeed() {
        SpinUpRumble r = new SpinUpRumble();
        assertFalse(r.update(false, false)); // idle
        assertFalse(r.update(true, false));  // spinning up
        assertTrue(r.update(true, true));    // first at speed
        assertFalse(r.update(true, true));   // still at speed
    }

    @Test
    public void shotDipsAndAMovingTargetDoNotRumbleAgain() {
        SpinUpRumble r = new SpinUpRumble();
        r.update(true, false);
        assertTrue(r.update(true, true));
        for (int i = 0; i < 50; i++) {
            assertFalse(r.update(true, false)); // a shot dips it out of the tolerance
            assertFalse(r.update(true, true));  // and it comes back
        }
    }

    @Test
    public void backToIdleArmsTheNextSpinUp() {
        SpinUpRumble r = new SpinUpRumble();
        r.update(true, false);
        assertTrue(r.update(true, true));
        assertFalse(r.update(false, false)); // idle
        assertFalse(r.update(true, false));
        assertTrue(r.update(true, true));
    }

    @Test
    public void atSpeedWhileIdleNeverRumbles() {
        // The idle speed can read "at speed" against its own setpoint: only a spin-up counts.
        SpinUpRumble r = new SpinUpRumble();
        assertFalse(r.update(false, true));
        assertFalse(r.update(false, true));
    }
}
