package org.firstinspires.ftc.teamcode.localization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.localization.PinpointTrust.State;
import org.junit.Test;

/**
 * {@link PinpointTrust}: a blip is not a loss; a fault that holds is; once the Pinpoint reads again
 * a reset or the camera brings the pose back, and nothing else does.
 */
public class PinpointTrustTest {

    private static final String BAD = "FAULT_BAD_READ";
    private static final String READY = "READY";

    /** A fault held from {@code fromMs}, then reading again at {@code readyMs}. */
    private static PinpointTrust faultedThenReady(long fromMs, long readyMs) {
        PinpointTrust t = new PinpointTrust();
        for (long ms = fromMs; ms < readyMs; ms += 20) t.observe(true, BAD, ms);
        t.observe(true, READY, readyMs);
        return t;
    }

    /** Agreeing camera frames every {@code everyMs} from {@code fromMs}. */
    private static long agree(PinpointTrust t, int frames, long fromMs, long everyMs, double gapIn) {
        long ms = fromMs;
        for (int i = 0; i < frames; i++, ms += everyMs) {
            t.observe(true, READY, ms);
            t.cameraGap(gapIn, ms);
        }
        return ms;
    }

    /** The logs' blips: 52 cleared in one loop, the longest 220 ms. None is a loss. */
    @Test
    public void blipsAreNotALoss() {
        PinpointTrust t = new PinpointTrust();
        t.observe(true, BAD, 1000);
        t.observe(true, BAD, 1220);
        t.observe(true, READY, 1240);
        t.observe(true, BAD, 1300);
        t.observe(true, BAD, 1700);
        t.observe(true, READY, 1720);
        assertTrue(t.trusted());
        assertNull(t.faultStatus());
        assertEquals(0, t.faults());
    }

    /** 4 Nov, R-Solo 21:26: the fault held the whole run. Not believed after 500 ms, and never again. */
    @Test
    public void aFaultThatNeverClearsIsNeverBelieved() {
        PinpointTrust t = new PinpointTrust();
        t.observe(true, BAD, 1000);
        t.observe(true, BAD, 1499);
        assertTrue(t.trusted());
        t.observe(true, BAD, 1500);
        assertEquals(State.FAULTED, t.state());
        assertEquals(BAD, t.faultStatus());
        // Resets and agreeing frames while it still faults change nothing.
        t.poseSet(2000);
        for (long ms = 2000; ms < 150_000; ms += 20) {
            t.observe(true, BAD, ms);
            t.cameraGap(0.5, ms);
        }
        assertEquals(State.FAULTED, t.state());
        assertFalse(t.trusted());
    }

    /** Reading again is not enough on its own: the first good read is not a confirmation. */
    @Test
    public void readingAgainWaitsForAConfirmation() {
        PinpointTrust t = faultedThenReady(226_300, 233_400);
        assertEquals(State.CONFIRMING, t.state());
        for (long ms = 233_420; ms < 300_000; ms += 20) t.observe(true, READY, ms);
        assertFalse("reads alone never bring the pose back", t.trusted());
    }

    /** Q13, 7 Nov: a corner reset after the Pinpoint reads again brings Square back at once. */
    @Test
    public void aResetAfterRecoveryConfirms() {
        PinpointTrust t = faultedThenReady(226_300, 233_400);
        t.poseSet(234_000);
        assertTrue(t.trusted());
        assertEquals(PinpointTrust.BY_RESET, t.confirmedBy());
    }

    /** A reset that clears the jump rejection: the Pinpoint reads again just after it. */
    @Test
    public void aResetThatClearsTheFaultConfirms() {
        PinpointTrust t = new PinpointTrust();
        for (long ms = 0; ms <= 5000; ms += 20) t.observe(true, BAD, ms);
        t.poseSet(5000);
        t.observe(true, BAD, 5020);
        t.observe(true, READY, 5040);
        assertTrue(t.trusted());
    }

    /** A reset long before the Pinpoint reads again says nothing about the pose it comes back with. */
    @Test
    public void aStaleResetDuringTheFaultDoesNot() {
        PinpointTrust t = new PinpointTrust();
        for (long ms = 0; ms <= 5000; ms += 20) t.observe(true, BAD, ms);
        t.poseSet(5000);
        for (long ms = 5000; ms <= 9000; ms += 20) t.observe(true, BAD, ms);
        t.observe(true, READY, 9020);
        assertEquals(State.CONFIRMING, t.state());
    }

    /** The camera agreeing for long enough confirms; the count and the span both matter. */
    @Test
    public void theCameraAgreeingConfirms() {
        PinpointTrust t = faultedThenReady(42_500, 105_900);
        // Ten frames in 90 ms: enough frames, not enough time.
        long ms = agree(t, PinpointTrust.AGREE_FRAMES, 106_000, 10, 1.7);
        assertEquals(State.CONFIRMING, t.state());
        agree(t, 50, ms, 10, 1.7);
        assertTrue(t.trusted());
        assertEquals(PinpointTrust.BY_CAMERA, t.confirmedBy());
    }

    /** One frame past the limit starts the count again; a pose that is off never confirms. */
    @Test
    public void aDisagreeingFrameStartsAgain() {
        PinpointTrust t = faultedThenReady(0, 1000);
        long ms = agree(t, PinpointTrust.AGREE_FRAMES - 1, 1100, 100, 2.0);
        t.cameraGap(PinpointTrust.AGREE_IN + 0.1, ms);
        assertEquals(0, t.agreeingFrames());
        agree(t, PinpointTrust.AGREE_FRAMES - 1, ms + 100, 100, 2.0);
        assertEquals(State.CONFIRMING, t.state());
        t.cameraGap(Double.NaN, ms + 2000);
        assertEquals(0, t.agreeingFrames());

        PinpointTrust off = faultedThenReady(0, 1000);
        agree(off, 200, 1100, 50, 9.0);
        assertFalse("a pose 9 in off is never confirmed by the camera", off.trusted());
    }

    /** A blip while confirming breaks the run; a fault that holds again goes back to FAULTED. */
    @Test
    public void faultsWhileConfirming() {
        PinpointTrust t = faultedThenReady(0, 1000);
        long ms = agree(t, PinpointTrust.AGREE_FRAMES - 1, 1100, 100, 1.0);
        t.observe(true, BAD, ms);
        assertEquals(0, t.agreeingFrames());
        assertEquals(State.CONFIRMING, t.state());
        for (long m = ms; m <= ms + 600; m += 20) t.observe(true, BAD, m);
        assertEquals(State.FAULTED, t.state());
        assertEquals(2, t.faults());
    }

    /** Trusted again, a second fault that holds is a second loss, and recovers the same way. */
    @Test
    public void aSecondFault() {
        PinpointTrust t = faultedThenReady(0, 1000);
        t.poseSet(1100);
        assertTrue(t.trusted());
        for (long ms = 2000; ms <= 2600; ms += 20) t.observe(true, BAD, ms);
        assertEquals(State.FAULTED, t.state());
        assertNull("the last confirmation is for the last fault only", t.confirmedBy());
        t.observe(true, READY, 3000);
        assertEquals(State.CONFIRMING, t.state());
    }

    /** Before PLAY the Auto's INIT warns and a restart is the fix: nothing is decided here. */
    @Test
    public void nothingBeforePlay() {
        PinpointTrust t = new PinpointTrust();
        t.observe(false, BAD, 0);
        t.observe(false, BAD, 60_000);
        assertTrue(t.trusted());
        // A fault still on at PLAY counts from PLAY, not from INIT.
        t.observe(true, BAD, 60_020);
        assertTrue(t.trusted());
        t.observe(true, BAD, 60_520);
        assertFalse(t.trusted());
    }

    /** A pod that is not detected is a fault too; no status (no Pinpoint) is not one. */
    @Test
    public void anyFaultCounts() {
        PinpointTrust t = new PinpointTrust();
        t.observe(true, "FAULT_Y_POD_NOT_DETECTED", 0);
        t.observe(true, "FAULT_Y_POD_NOT_DETECTED", 600);
        assertFalse(t.trusted());
        PinpointTrust none = new PinpointTrust();
        none.observe(true, null, 0);
        none.observe(true, null, 600);
        assertTrue(none.trusted());
    }

    /** The camera and a reset do nothing while trusted. */
    @Test
    public void confirmationsWhileTrustedChangeNothing() {
        PinpointTrust t = new PinpointTrust();
        t.poseSet(0);
        t.cameraGap(50.0, 0);
        assertTrue(t.trusted());
        assertNull(t.confirmedBy());
    }
}
