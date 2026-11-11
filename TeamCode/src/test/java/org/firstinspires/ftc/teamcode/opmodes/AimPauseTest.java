package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** {@link AimPause}: a driver's feed holds back while the robot is knocked off its aim. */
public class AimPauseTest {

    @Test
    public void offNeverPauses() {
        AimPause p = new AimPause();
        assertFalse(p.update(false, 20.0, 3.0, true));
        assertFalse(p.update(false, Double.NaN, 3.0, true));
    }

    @Test
    public void pausesPastTheToleranceAndResumesWellInsideIt() {
        AimPause p = new AimPause();
        assertFalse(p.update(true, 2.9, 3.0, false));
        assertTrue(p.update(true, 3.1, 3.0, false));
        // Back inside the tolerance but not yet well inside: still paused, so the gate does not chatter.
        assertTrue(p.update(true, -2.0, 3.0, false));
        assertFalse(p.update(true, -1.5, 3.0, false));
        // And inside the band it stays feeding.
        assertFalse(p.update(true, 2.5, 3.0, false));
    }

    @Test
    public void anUnknownAimPausesOnlyWhereItIsAskedTo() {
        AimPause circle = new AimPause();
        assertTrue(circle.update(true, Double.NaN, 3.0, true));
        AimPause square = new AimPause();
        assertFalse(square.update(true, Double.NaN, 3.0, false));
    }

    @Test
    public void aBadToleranceNeverPauses() {
        AimPause p = new AimPause();
        assertFalse(p.update(true, 20.0, Double.NaN, true));
        assertFalse(p.update(true, 20.0, 0.0, true));
    }

    @Test
    public void resetClearsAPause() {
        AimPause p = new AimPause();
        assertTrue(p.update(true, 10.0, 3.0, false));
        p.reset();
        assertFalse(p.paused());
    }
}
