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
    public void theReceiveChecksTheMeasuredDirectionAgainstTheCellAndTheLiveDemand() {
        assertTrue(AimedVolley.receiveAligned(83, 84, 84, 5));
        assertFalse(AimedVolley.receiveAligned(0, 84, 84, 5));
        assertFalse(AimedVolley.receiveAligned(0, 0, 84, 5));
        assertFalse(AimedVolley.receiveAligned(86, 84, 84, 5));
        assertFalse(AimedVolley.receiveAligned(84, 86, 84, 5));
        assertFalse(AimedVolley.receiveAligned(84, Double.NaN, 84, 5));
    }

    @Test
    public void theTurretAimsOnlyWithAPoseAnAllianceAndAWorkingTurret() {
        assertTrue(AimedVolley.canAim(true, false, null, Alliance.RED));
        assertFalse("no pose: home", AimedVolley.canAim(false, false, null, Alliance.RED));
        assertFalse("locked forward: home", AimedVolley.canAim(true, true, null, Alliance.RED));
        assertFalse("a dropped hold: home", AimedVolley.canAim(true, false, "DROPPED: x", Alliance.RED));
        assertFalse(AimedVolley.canAim(true, false, null, Alliance.UNKNOWN));
        assertFalse(AimedVolley.canAim(true, false, null, null));
    }

    /** A turret that cannot aim fires from home only where home points at the CELL: never from a heading that faces elsewhere. */
    @Test
    public void homeIsAShotOnlyWhereItPointsAtTheCell() {
        assertNull("a spot that aims a locked turret", AimedVolley.homeRefusal(0.4));
        assertNull(AimedVolley.homeRefusal(-AimedVolley.HOME_AIM_TOLERANCE_DEG));
        assertTrue("L-RightUnderHive's NECTAR spot, 84 deg off", AimedVolley.homeRefusal(84).contains("84 deg off the CELL"));
        assertTrue(AimedVolley.homeRefusal(-20) != null);
        assertTrue("not known is not a shot", AimedVolley.homeRefusal(Double.NaN).contains("not known"));
    }

    /** The one-ball aimed shot asks for the aim inside the arc by its margin; a volley from the park asks for none. */
    @Test
    public void theAimIsInsideTheArcByTheMargin() {
        assertTrue(AimedVolley.insideArcBy(84, 90, 90, 5));
        assertFalse(AimedVolley.insideArcBy(88.7, 90, 90, 5));
        assertTrue(AimedVolley.insideArcBy(-85, 90, 90, 5));
        assertFalse(AimedVolley.insideArcBy(-86, 90, 90, 5));
        assertTrue("no margin asks nothing more", AimedVolley.insideArcBy(89.9, 90, 90, 0));
        assertFalse(AimedVolley.insideArcBy(Double.NaN, 90, 90, 5));
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
