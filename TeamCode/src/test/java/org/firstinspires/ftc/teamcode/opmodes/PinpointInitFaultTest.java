package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/** 2 Nov 2026: "Pinpoint: bad read" on every Auto's INIT, with the Auto working fine. */
public class PinpointInitFaultTest {

    private final PinpointInitFault f = new PinpointInitFault();

    @Test
    public void aSingleDroppedReadDoesNotWarn() {
        f.observe(true, "READY", 0);
        f.observe(true, "FAULT_BAD_READ", 20);
        f.observe(true, "READY", 40);
        assertNull(f.problem());
    }

    @Test
    public void aBadReadThatHoldsWarnsThenClearsWhenTheReadsRecover() {
        f.observe(true, "FAULT_BAD_READ", 0);
        f.observe(true, "FAULT_BAD_READ", PinpointInitFault.BAD_READ_HOLD_MS - 1);
        assertNull(f.problem());
        f.observe(true, "FAULT_BAD_READ", PinpointInitFault.BAD_READ_HOLD_MS);
        assertEquals("bad read", f.problem());
        f.observe(true, "READY", PinpointInitFault.BAD_READ_HOLD_MS + 20);
        assertNull(f.problem());
    }

    @Test
    public void repeatedShortBadReadsNeverAddUpToAWarning() {
        for (long t = 0; t < 5_000; t += 40) {
            f.observe(true, t % 80 == 0 ? "FAULT_BAD_READ" : "READY", t);
        }
        assertNull(f.problem());
    }

    @Test
    public void aHardFaultIsKeptEvenAfterItDropsOut() {
        f.observe(true, "FAULT_Y_POD_NOT_DETECTED", 0);
        f.observe(true, "READY", 20);
        assertEquals("y pod not detected", f.problem());
    }

    @Test
    public void aMissingPinpointIsReported() {
        f.observe(false, null, 0);
        assertEquals("not found", f.problem());
    }

    @Test
    public void allWellIsNoProblem() {
        f.observe(true, "READY", 0);
        f.observe(true, null, 20);
        assertNull(f.problem());
    }
}
