package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** The colour sensor's hue and saturation, the two numbers that do not depend on gain or distance. */
public class ColorSensorSubsystemTest {

    @Test
    public void primariesHaveTheirHues() {
        assertEquals(0.0, ColorSensorSubsystem.hueDegrees(1, 0, 0), 1e-9);
        assertEquals(120.0, ColorSensorSubsystem.hueDegrees(0, 1, 0), 1e-9);
        assertEquals(240.0, ColorSensorSubsystem.hueDegrees(0, 0, 1), 1e-9);
    }

    @Test
    public void inBetweenHues() {
        assertEquals(60.0, ColorSensorSubsystem.hueDegrees(1, 1, 0), 1e-9);   // yellow
        assertEquals(180.0, ColorSensorSubsystem.hueDegrees(0, 1, 1), 1e-9);  // cyan
        assertEquals(300.0, ColorSensorSubsystem.hueDegrees(1, 0, 1), 1e-9);  // magenta
        assertEquals(330.0, ColorSensorSubsystem.hueDegrees(1, 0, 0.5), 1e-9); // wraps, never negative
    }

    @Test
    public void hueIgnoresBrightness() {
        assertEquals(ColorSensorSubsystem.hueDegrees(0.8, 0.4, 0.1),
                ColorSensorSubsystem.hueDegrees(0.08, 0.04, 0.01), 1e-9);
    }

    @Test
    public void greyHasNoHue() {
        assertTrue(Double.isNaN(ColorSensorSubsystem.hueDegrees(0.3, 0.3, 0.3)));
        assertTrue(Double.isNaN(ColorSensorSubsystem.hueDegrees(0, 0, 0)));
    }

    @Test
    public void saturationRunsFromGreyToPure() {
        assertEquals(0.0, ColorSensorSubsystem.saturation(0.3, 0.3, 0.3), 1e-9);
        assertEquals(1.0, ColorSensorSubsystem.saturation(0.5, 0, 0), 1e-9);
        assertEquals(0.5, ColorSensorSubsystem.saturation(0.4, 0.2, 0.3), 1e-9);
        assertEquals(0.0, ColorSensorSubsystem.saturation(0, 0, 0), 1e-9);
    }

    @Test
    public void readsFastOnlyWhileAPieceCanMove() {
        assertEquals(50, ColorSensorSubsystem.readIntervalMs(true, 50, 250));
        assertEquals(250, ColorSensorSubsystem.readIntervalMs(false, 50, 250));
    }

    @Test
    public void idleIsNeverFasterThanMoving() {
        assertEquals(100, ColorSensorSubsystem.readIntervalMs(false, 100, 20));
        assertEquals(0, ColorSensorSubsystem.readIntervalMs(true, -5, 250));
        assertEquals(250, ColorSensorSubsystem.readIntervalMs(false, -5, 250));
    }

    // ------------------------------------------------------------ when it reads

    private static final IntakeSubsystem.State I_IDLE = IntakeSubsystem.State.IDLE;
    private static final IntakeSubsystem.State I_IN = IntakeSubsystem.State.IN;
    private static final IntakeSubsystem.State I_OUT = IntakeSubsystem.State.OUT;
    private static final PrefeedSubsystem.State P_IDLE = PrefeedSubsystem.State.IDLE;
    private static final FeederSubsystem.State F_IDLE = FeederSubsystem.State.IDLE;

    @Test
    public void takingInWatchesClosely() {
        assertTrue(ColorSensorSubsystem.pieceCanMove(I_IN, I_IN, false, PrefeedSubsystem.State.IN, F_IDLE, false));
        assertTrue("stars alone move a piece",
                ColorSensorSubsystem.pieceCanMove(I_IDLE, I_IN, false, P_IDLE, F_IDLE, false));
        assertTrue("an eject", ColorSensorSubsystem.pieceCanMove(I_OUT, I_OUT, false, PrefeedSubsystem.State.OUT, F_IDLE, false));
    }

    @Test
    public void holdingAFullLaneReadsSlowly() {
        // The intake's hold: roller and stars inward, prefeed stopped (stopWhenFull), feeder idle or standing by.
        assertFalse(ColorSensorSubsystem.pieceCanMove(I_IN, I_IN, true, P_IDLE, F_IDLE, false));
        assertFalse("nothing running", ColorSensorSubsystem.pieceCanMove(I_IDLE, I_IDLE, false, P_IDLE, F_IDLE, false));
    }

    @Test
    public void aFeedAShotOrAPrefeedWatchesClosedEvenWhileHolding() {
        assertTrue("a feed opens the gate",
                ColorSensorSubsystem.pieceCanMove(I_IN, I_IN, true, P_IDLE, FeederSubsystem.State.FEED, false));
        assertTrue("an unjam backs the lane out",
                ColorSensorSubsystem.pieceCanMove(I_IN, I_IN, true, P_IDLE, FeederSubsystem.State.REVERSE, false));
        assertTrue("a shot just counted", ColorSensorSubsystem.pieceCanMove(I_IN, I_IN, true, P_IDLE, F_IDLE, true));
        assertTrue("stopWhenFull off: the prefeed still pushes",
                ColorSensorSubsystem.pieceCanMove(I_IN, I_IN, true, PrefeedSubsystem.State.IN, F_IDLE, false));
    }

    @Test
    public void theFirstReadIsAlwaysDue() {
        assertTrue(ColorSensorSubsystem.readDue(false, 0, false, false, 50, 250));
    }

    @Test
    public void slowReadsWaitTheIdleInterval() {
        assertFalse(ColorSensorSubsystem.readDue(true, 249, false, false, 50, 250));
        assertTrue(ColorSensorSubsystem.readDue(true, 250, false, false, 50, 250));
    }

    @Test
    public void fastReadsWaitTheFastInterval() {
        assertFalse(ColorSensorSubsystem.readDue(true, 49, true, true, 50, 250));
        assertTrue(ColorSensorSubsystem.readDue(true, 50, true, true, 50, 250));
    }

    @Test
    public void somethingStartingToMoveReadsAtOnce() {
        // 10 ms after a slow read, a feed starts: read now, not 240 ms from now.
        assertTrue(ColorSensorSubsystem.readDue(true, 10, true, false, 50, 250));
        assertTrue("even just after a read", ColorSensorSubsystem.readDue(true, 0, true, false, 50, 250));
    }

    @Test
    public void goingQuietWaitsFromTheLastRead() {
        // The feed ends 60 ms after the last fast read: the next read is the slow one, from that read.
        assertFalse(ColorSensorSubsystem.readDue(true, 60, false, true, 50, 250));
        assertTrue(ColorSensorSubsystem.readDue(true, 250, false, true, 50, 250));
    }

    /**
     * A whole hold, loop by loop at 20 ms: the lane goes full while taking in, the hold reads four a
     * second, and the feed that empties it is read in its first loop, so full clears as the piece
     * leaves. Brightness 0.3 is a piece under the sensor, 0.01 an empty lane.
     */
    @Test
    public void aHoldReadsSlowlyAndAFeedClearsFullAtOnce() {
        LaneFull full = new LaneFull();
        boolean watching = false;
        boolean everRead = false;
        long lastReadMs = 0;
        int readsWhileHolding = 0;
        long feedAtMs = 3000;
        long clearedAtMs = -1;
        boolean wentFull = false;
        for (long now = 0; now <= 4000; now += 20) {
            boolean feeding = now >= feedAtMs;
            boolean holding = full.full() && !feeding;
            boolean canMove = ColorSensorSubsystem.pieceCanMove(I_IN, I_IN, holding,
                    holding ? P_IDLE : PrefeedSubsystem.State.IN,
                    feeding ? FeederSubsystem.State.FEED : F_IDLE, false);
            boolean due = ColorSensorSubsystem.readDue(everRead, now - lastReadMs, canMove, watching, 50, 250);
            watching = canMove;
            if (!due) continue;
            everRead = true;
            lastReadMs = now;
            double brightness = feeding ? 0.01 : 0.3; // the feed carries the piece away at once
            full.step(now, brightness, !feeding, 0.10, 0.05, 1000);
            if (holding) readsWhileHolding++;
            wentFull |= full.full();
            if (feeding && clearedAtMs < 0 && !full.full()) clearedAtMs = now;
        }
        assertTrue("full after the hold time", wentFull);
        // Full from about 1.0 s to 3.0 s: four reads a second, not twenty.
        assertTrue("held slowly: " + readsWhileHolding, readsWhileHolding >= 7 && readsWhileHolding <= 9);
        assertEquals("the feed is read in its first loop", feedAtMs, clearedAtMs);
    }
}
