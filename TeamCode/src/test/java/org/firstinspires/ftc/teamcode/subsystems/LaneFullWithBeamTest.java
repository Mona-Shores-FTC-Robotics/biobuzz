package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * {@link ColorSensorSubsystem#fullWithBeam}: with the lane beam fitted, full needs the colour
 * sensor and the beam to agree; without one, the colour sensor alone, as before.
 */
public class LaneFullWithBeamTest {

    private static BeamBreak beamBrokenFor(long ms) {
        BeamBreak beam = new BeamBreak();
        beam.step(0, false, 2);
        beam.step(1000, true, 2);
        beam.step(1000 + ms, true, 2);
        return beam;
    }

    @Test
    public void noBeamIsTheColourSensorAlone() {
        assertTrue(ColorSensorSubsystem.fullWithBeam(true, null, 0.3));
        assertFalse(ColorSensorSubsystem.fullWithBeam(false, null, 0.3));
    }

    @Test
    public void aPieceHeldAtTheEntranceWithTheBeamClearIsNotFull() {
        BeamBreak clear = new BeamBreak();
        clear.step(0, false, 2);
        clear.step(500, false, 2);
        assertFalse("the Fruitport false full: two or fewer ahead", ColorSensorSubsystem.fullWithBeam(true, clear, 0.3));
    }

    @Test
    public void bothAgreeingIsFullOnceTheBeamHasHeld() {
        assertFalse("a piece passing the beam", ColorSensorSubsystem.fullWithBeam(true, beamBrokenFor(200), 0.3));
        assertTrue(ColorSensorSubsystem.fullWithBeam(true, beamBrokenFor(300), 0.3));
        assertFalse("the beam alone is never full", ColorSensorSubsystem.fullWithBeam(false, beamBrokenFor(5000), 0.3));
        assertTrue("NaN hold: any break", ColorSensorSubsystem.fullWithBeam(true, beamBrokenFor(0), Double.NaN));
    }
}
