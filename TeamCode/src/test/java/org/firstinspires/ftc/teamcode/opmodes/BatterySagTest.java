package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BatterySagTest {

    @Test
    public void aRunThatHeldUpIsNotSagging() {
        BatterySag s = new BatterySag();
        for (int i = 0; i < 30; i++) s.add(11.5);
        assertFalse(s.sagged());
        assertEquals(11.5, s.meanVolts(), 1e-9);
    }

    /** 21 Nov 2026, 20245's 12:56 R-Solo: steady 9.4 V on average under load. */
    @Test
    public void aRunAveragingUnderTenVoltsSags() {
        BatterySag s = new BatterySag();
        for (int i = 0; i < 30; i++) s.add(9.4);
        assertTrue(s.sagged());
        assertTrue(s.event(), s.event().startsWith("battery sagging: 9.40 V"));
    }

    @Test
    public void aFewSecondsAreNotEnoughToCall() {
        BatterySag s = new BatterySag();
        for (int i = 0; i < BatterySag.MIN_READINGS - 1; i++) s.add(8.0);
        assertFalse(s.sagged());
    }

    @Test
    public void badReadingsAreIgnored() {
        BatterySag s = new BatterySag();
        s.add(Double.NaN);
        s.add(0.0);
        s.add(Double.POSITIVE_INFINITY);
        assertEquals(0, s.readings());
        assertTrue(Double.isNaN(s.meanVolts()));
    }

    @Test
    public void theNextInitWarnsWhileTheSameBatteryIsIn() {
        String note = BatterySag.initNote(12.5, 12.41, 9.4);
        assertTrue(note, note != null && note.contains("9.4 V"));
    }

    @Test
    public void aSwappedBatteryOrAGoodRunOrNoRecordDoesNotWarn() {
        assertNull("INIT 0.5 V higher: swapped", BatterySag.initNote(12.9, 12.41, 9.4));
        assertNull("the last run held up", BatterySag.initNote(12.5, 12.6, 11.2));
        assertNull("nothing recorded", BatterySag.initNote(12.5, Double.NaN, Double.NaN));
    }
}
