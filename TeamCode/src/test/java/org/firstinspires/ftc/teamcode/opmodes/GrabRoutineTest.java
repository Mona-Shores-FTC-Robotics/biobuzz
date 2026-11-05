package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/** Why a grab's dwell ends: a full lane first, then the cap, then the intake current's answer. */
public class GrabRoutineTest {

    @Test
    public void aFullLaneEndsTheDwellWhateverElseSays() {
        assertEquals("full", GrabRoutine.dwellEnd(false, true, false, false));
        assertEquals("full", GrabRoutine.dwellEnd(true, true, true, true));
    }

    @Test
    public void theCapEndsItWhenTheLaneIsNotFull() {
        assertEquals("dwellMs", GrabRoutine.dwellEnd(true, false, false, false));
        assertEquals("dwellMs", GrabRoutine.dwellEnd(true, false, true, true));
    }

    @Test
    public void theCurrentEndsItWhenItSaysSo() {
        assertEquals("pieces in", GrabRoutine.dwellEnd(false, false, true, true));
        assertEquals("nothing came", GrabRoutine.dwellEnd(false, false, true, false));
    }

    @Test
    public void otherwiseItGoesOn() {
        assertNull(GrabRoutine.dwellEnd(false, false, false, true));
    }
}
