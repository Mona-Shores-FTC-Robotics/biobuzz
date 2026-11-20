package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** The driver's firing-arc light: orange whenever the shot is behind the arc, flashing with a fire held, steady at the edge. */
public class ArcLightTest {

    @Test
    public void orangeWithoutAFireHeldFlashingWithOne() {
        ArcLight light = new ArcLight();
        assertEquals(ArcLight.State.NONE, light.update(false, false, 0));
        assertEquals("the driver sees it before pressing", ArcLight.State.ORANGE, light.update(true, false, 20));
        assertEquals(ArcLight.State.FLASH, light.update(true, true, 40));
        assertEquals(ArcLight.State.ORANGE, light.update(true, false, 60));
    }

    @Test
    public void anAimFlappingAtTheEdgeKeepsTheLightOn() {
        ArcLight light = new ArcLight();
        // 20245, 19 Nov 2026: an aim on the 90° edge, in and out of the arc every few loops.
        long t = 0;
        for (int i = 0; i < 50; i++, t += 20) {
            assertEquals("loop " + i, ArcLight.State.FLASH, light.update(i % 3 == 0, true, t));
        }
    }

    @Test
    public void clearsOnlyOnceBackInsideForAWhile() {
        ArcLight light = new ArcLight();
        light.update(true, false, 1000);
        assertEquals(ArcLight.State.ORANGE, light.update(false, false, 1000 + ArcLight.CLEAR_MS - 1));
        assertEquals(ArcLight.State.NONE, light.update(false, false, 1000 + ArcLight.CLEAR_MS));
        assertEquals("a fire inside the arc is not orange", ArcLight.State.NONE, light.update(false, true, 2000));
    }

    @Test
    public void theArcTurnPulseLastsItsLengthThenClears() {
        ArcLight light = new ArcLight();
        light.pulse(1000, 400);
        // Circle held, the turn sorting it out: the TeleOp says "not outside" while it turns.
        assertEquals(ArcLight.State.PULSE, light.update(false, true, 1000));
        assertEquals(ArcLight.State.PULSE, light.update(false, true, 1399));
        assertEquals(ArcLight.State.NONE, light.update(false, true, 1400));
    }

    @Test
    public void aRefusedFireFlashesOverThePulse() {
        ArcLight light = new ArcLight();
        light.pulse(0, 400);
        assertEquals(ArcLight.State.FLASH, light.update(true, true, 100));
    }

    @Test
    public void aNewTurnsPulseIsNotHiddenByTheLastPressesFlash() {
        ArcLight light = new ArcLight();
        // A press cancelled by the right stick: flashing behind the arc.
        assertEquals(ArcLight.State.FLASH, light.update(true, true, 1000));
        // Circle let go and pressed again within CLEAR_MS: a new turn starts its pulse.
        light.pulse(1100, 400);
        assertEquals(ArcLight.State.PULSE, light.update(false, true, 1100));
        // Cancelled again mid-pulse: the flash takes over at once.
        assertEquals(ArcLight.State.FLASH, light.update(true, true, 1200));
    }

    @Test
    public void noPulseUntilOneStarts() {
        ArcLight light = new ArcLight();
        assertEquals(ArcLight.State.NONE, light.update(false, true, 0));
        light.pulse(500, 0);
        assertEquals("a zero-length pulse shows nothing", ArcLight.State.NONE, light.update(false, true, 500));
    }
}
