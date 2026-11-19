package org.firstinspires.ftc.teamcode.opmodes.auto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

/** When an aimed volley aims, and when the park one may open its gate ({@link AimedVolley}). */
public class AimedVolleyTest {

    @Test
    public void theTurretAimsOnlyWithAPoseAnAllianceAndAWorkingTurret() {
        assertTrue(AimedVolley.canAim(true, false, null, Alliance.RED));
        assertFalse("no pose: home", AimedVolley.canAim(false, false, null, Alliance.RED));
        assertFalse("locked forward: home", AimedVolley.canAim(true, true, null, Alliance.RED));
        assertFalse("a dropped hold: home", AimedVolley.canAim(true, false, "DROPPED: x", Alliance.RED));
        assertFalse(AimedVolley.canAim(true, false, null, Alliance.UNKNOWN));
        assertFalse(AimedVolley.canAim(true, false, null, null));
    }

    @Test
    public void theParkVolleyNeedsAPoseAndAnAlliance() {
        // 19429, 18 Nov 2026: parked by 26.2 s with 2 POLLEN.
        assertNull(AimedVolley.parkRefusal(true, Alliance.RED, 26.2, 2));
        assertTrue(AimedVolley.parkRefusal(false, Alliance.RED, 26.2, 2).contains("not referenced"));
        assertTrue(AimedVolley.parkRefusal(true, Alliance.UNKNOWN, 26.2, 2).contains("no alliance"));
        assertTrue(AimedVolley.parkRefusal(true, null, 26.2, 2).contains("no alliance"));
    }

    @Test
    public void theParkVolleysGateMustOpenInTimeForTheLastPieceHeld() {
        // First ball 0.75 s after the gate, then one each 0.45 s: 2 held, the gate by 28.8 s; 4 held, 27.9 s.
        assertEquals(28.8, AimedVolley.gateDeadlineS(2), 1e-9);
        assertEquals(27.9, AimedVolley.gateDeadlineS(4), 1e-9);
        assertEquals("none counted is still one", 29.25, AimedVolley.gateDeadlineS(0), 1e-9);
        assertNull(AimedVolley.parkRefusal(true, Alliance.RED, 28.8, 2));
        assertTrue(AimedVolley.parkRefusal(true, Alliance.RED, 28.81, 2).contains("after AUTO"));
        assertTrue(AimedVolley.parkRefusal(true, Alliance.RED, 28.0, 4).contains("after AUTO"));
        assertNull("no clock is never too late", AimedVolley.parkRefusal(true, Alliance.RED, Double.NaN, 4));
    }
}
