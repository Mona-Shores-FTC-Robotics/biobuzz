package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.subsystems.FeederSubsystem.State;
import org.firstinspires.ftc.teamcode.util.VoltageCompensation;
import org.junit.Test;

/** The pure part of {@link FeederSubsystem}: the TeleOp gate and what each request does to the motor. */
public class FeederSubsystemTest {

    private static final double EPS = 1e-9;

    /** The power the feeder writes with {@code compensateVoltage} on, as update() computes it. */
    private static double compensated(double power, double battery) {
        return VoltageCompensation.power(power, VoltageCompensation.scale(true, 12.0, battery));
    }

    @Test
    public void compensationHoldsTheFeedFromAFreshToATiredBattery() {
        // 0.88 at 12.0 nominal: the same drive (power x battery) at 12.5 V and at 10.6 V.
        assertEquals(0.88 * 12.0, compensated(0.88, 12.5) * 12.5, 1e-6);
        assertEquals(0.88 * 12.0, compensated(0.88, 10.6) * 10.6, 0.02);
        assertTrue(compensated(0.88, 10.6) <= 1.0);
    }

    @Test
    public void atFullPowerCompensationHasNoHeadroom() {
        assertEquals("a tired battery gets nothing more", 1.0, compensated(1.0, 10.6), EPS);
        assertEquals("a fresh one is only slowed", 0.96, compensated(1.0, 12.5), EPS);
        assertEquals("off: unchanged", 1.0,
                VoltageCompensation.power(1.0, VoltageCompensation.scale(false, 12.0, 10.6)), EPS);
    }

    @Test
    public void aReverseIsScaledWithItsSign() {
        assertEquals(-0.8 * 12.0 / 11.0, compensated(-0.8, 11.0), EPS);
    }

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

    @Test
    public void theWheelsStandByOnlyAfterPlayAndWhenNothingElseIsAsked() {
        assertEquals(true, FeederSubsystem.standsBy(State.IDLE, true, true, 1.0));
        assertEquals("not before PLAY", false, FeederSubsystem.standsBy(State.IDLE, false, true, 1.0));
        assertEquals("not when neither the launcher nor the intake asks", false, FeederSubsystem.standsBy(State.IDLE, true, false, 1.0));
        assertEquals("0 turns it off", false, FeederSubsystem.standsBy(State.IDLE, true, true, 0.0));
        assertEquals("NaN is off", false, FeederSubsystem.standsBy(State.IDLE, true, true, Double.NaN));
        assertEquals("a feed is a feed", false, FeederSubsystem.standsBy(State.FEED, true, true, 1.0));
        assertEquals("an unjam wins", false, FeederSubsystem.standsBy(State.REVERSE, true, true, 1.0));
    }
}
