package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;

import org.firstinspires.ftc.teamcode.subsystems.FeederSubsystem.State;
import org.junit.Test;

/** The pure part of {@link FeederSubsystem}: the TeleOp gate and what each request does to the motor. */
public class FeederSubsystemTest {

    private static final double EPS = 1e-9;

    @Test
    public void nothingHeldIsIdle() {
        assertEquals(State.IDLE, FeederSubsystem.requestFor(false, false, true, true, false));
        assertEquals(State.IDLE, FeederSubsystem.requestFor(false, false, false, false, false));
        assertEquals("releasing the button stops a feed", State.IDLE,
                FeederSubsystem.requestFor(false, false, true, true, true));
    }

    @Test
    public void aFeedStartsOnlyOnceTheLauncherIsAtSpeed() {
        assertEquals(State.IDLE, FeederSubsystem.requestFor(true, false, false, true, false));
        assertEquals(State.FEED, FeederSubsystem.requestFor(true, false, true, true, false));
    }

    @Test
    public void aFeedAlreadyRunningKeepsRunningThroughASpeedDip() {
        // The wheels slow as a piece meets them; the feeder must not pulse.
        assertEquals(State.FEED, FeederSubsystem.requestFor(true, false, false, true, true));
    }

    @Test
    public void benchPolicyFeedsRegardless() {
        assertEquals(State.FEED, FeederSubsystem.requestFor(true, false, false, false, false));
    }

    @Test
    public void reverseAlwaysWins() {
        assertEquals(State.REVERSE, FeederSubsystem.requestFor(false, true, false, true, false));
        assertEquals(State.REVERSE, FeederSubsystem.requestFor(true, true, true, true, true));
    }

    @Test
    public void feedIsPositiveReverseIsNegativeIdleIsZero() {
        assertEquals(0.8, FeederSubsystem.powerFor(State.FEED, 0.8, 0.5, false), EPS);
        assertEquals(-0.5, FeederSubsystem.powerFor(State.REVERSE, 0.8, 0.5, false), EPS);
        assertEquals(0.0, FeederSubsystem.powerFor(State.IDLE, 0.8, 0.5, false), EPS);
    }

    @Test
    public void reversedFlipsBothDirections() {
        assertEquals(-0.8, FeederSubsystem.powerFor(State.FEED, 0.8, 0.5, true), EPS);
        assertEquals(0.5, FeederSubsystem.powerFor(State.REVERSE, 0.8, 0.5, true), EPS);
    }

    @Test
    public void powersAreClampedAndNanStopsIt() {
        assertEquals(1.0, FeederSubsystem.powerFor(State.FEED, 3.0, 0.5, false), EPS);
        assertEquals(0.0, FeederSubsystem.powerFor(State.FEED, -0.2, 0.5, false), EPS);
        assertEquals(0.0, FeederSubsystem.powerFor(State.REVERSE, 0.8, Double.NaN, false), EPS);
    }
}
