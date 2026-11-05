package org.firstinspires.ftc.teamcode.localization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** {@link PinpointLoss}: a blip is not a loss, a fault that holds after PLAY is, and it stays. */
public class PinpointLossTest {

    private static final String BAD = "FAULT_BAD_READ";

    /** The logs' blips: 52 cleared in one loop, the longest 220 ms. None is a loss. */
    @Test
    public void blipsAreNotALoss() {
        PinpointLoss loss = new PinpointLoss();
        loss.observe(true, BAD, 1000);
        loss.observe(true, BAD, 1220);
        loss.observe(true, "READY", 1240);
        loss.observe(true, BAD, 1300);
        loss.observe(true, BAD, 1700);
        loss.observe(true, "READY", 1720);
        assertFalse(loss.lost());
        assertNull(loss.lostStatus());
    }

    /** 4 Nov, R-Solo 21:26: the fault held the whole run. Lost after 500 ms, and it stays lost. */
    @Test
    public void aFaultThatHoldsIsALossThatStays() {
        PinpointLoss loss = new PinpointLoss();
        loss.observe(true, BAD, 1000);
        loss.observe(true, BAD, 1499);
        assertFalse(loss.lost());
        loss.observe(true, BAD, 1500);
        assertTrue(loss.lost());
        assertEquals(BAD, loss.lostStatus());
        loss.observe(true, "READY", 1520);
        assertTrue("a read that looks well again does not bring the pose back", loss.lost());
    }

    /** Before PLAY the Auto's INIT warns and a restart is the fix: nothing is decided here. */
    @Test
    public void nothingBeforePlay() {
        PinpointLoss loss = new PinpointLoss();
        loss.observe(false, BAD, 0);
        loss.observe(false, BAD, 60_000);
        assertFalse(loss.lost());
        // A fault still on at PLAY counts from PLAY, not from INIT.
        loss.observe(true, BAD, 60_020);
        assertFalse(loss.lost());
        loss.observe(true, BAD, 60_520);
        assertTrue(loss.lost());
    }

    /** A pod that is not detected is a fault too. */
    @Test
    public void anyFaultCounts() {
        PinpointLoss loss = new PinpointLoss();
        loss.observe(true, "FAULT_Y_POD_NOT_DETECTED", 0);
        loss.observe(true, "FAULT_Y_POD_NOT_DETECTED", 600);
        assertTrue(loss.lost());
        PinpointLoss none = new PinpointLoss();
        none.observe(true, null, 0);
        none.observe(true, null, 600);
        assertFalse("no status (no Pinpoint) is not a fault here", none.lost());
    }
}
