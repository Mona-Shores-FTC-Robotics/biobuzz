package org.firstinspires.ftc.teamcode.opmodes.auto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

/** When {@code LaunchAllAimed} may open its gate from the park spot: {@link AimedVolley#refusal}. */
public class AimedVolleyTest {

    @Test
    public void aReferencedPoseAFreeTurretAndTimeLeftMayFire() {
        // 19429, 18 Nov 2026: parked at LEFT_PARK by 26.2 s with 2 POLLEN.
        assertNull(AimedVolley.refusal(true, false, null, Alliance.RED, 26.2, 2));
    }

    @Test
    public void noPoseNoTurretOrNoAllianceDoesNotFire() {
        assertTrue(AimedVolley.refusal(false, false, null, Alliance.RED, 26.2, 2).contains("not referenced"));
        assertTrue(AimedVolley.refusal(true, true, null, Alliance.RED, 26.2, 2).contains("locked forward"));
        assertTrue(AimedVolley.refusal(true, false, "DROPPED: x", Alliance.RED, 26.2, 2).contains("dropped its hold"));
        assertTrue(AimedVolley.refusal(true, false, null, Alliance.UNKNOWN, 26.2, 2).contains("no alliance"));
        assertTrue(AimedVolley.refusal(true, false, null, null, 26.2, 2).contains("no alliance"));
    }

    @Test
    public void theGateMustOpenInTimeForTheLastPieceHeld() {
        // First ball 0.75 s after the gate, then one each 0.45 s: 2 held, the gate by 28.8 s; 4 held, 27.9 s.
        assertEquals(28.8, AimedVolley.gateDeadlineS(2), 1e-9);
        assertEquals(27.9, AimedVolley.gateDeadlineS(4), 1e-9);
        assertEquals("none counted is still one", 29.25, AimedVolley.gateDeadlineS(0), 1e-9);
        assertNull(AimedVolley.refusal(true, false, null, Alliance.RED, 28.8, 2));
        assertTrue(AimedVolley.refusal(true, false, null, Alliance.RED, 28.81, 2).contains("after AUTO"));
        assertTrue(AimedVolley.refusal(true, false, null, Alliance.RED, 28.0, 4).contains("after AUTO"));
        assertNull("no clock is never too late", AimedVolley.refusal(true, false, null, Alliance.RED, Double.NaN, 4));
    }
}
