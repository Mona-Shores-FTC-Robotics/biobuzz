package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;

import org.firstinspires.ftc.teamcode.subsystems.PrefeedSubsystem.State;
import org.junit.Test;

/** The pure part of {@link PrefeedSubsystem}: when it runs, and what power each state writes. */
public class PrefeedSubsystemTest {

    private static final double EPS = 1e-9;

    private static final IntakeSubsystem.State[] INTAKE = IntakeSubsystem.State.values();
    private static final FeederSubsystem.State[] FEEDER = FeederSubsystem.State.values();

    @Test
    public void runsWheneverTheIntakeTakesPiecesIn() {
        for (FeederSubsystem.State feeder : new FeederSubsystem.State[] {
                FeederSubsystem.State.IDLE, FeederSubsystem.State.FEED}) {
            assertEquals(State.IN, PrefeedSubsystem.requestFor(IntakeSubsystem.State.IN, feeder));
        }
    }

    @Test
    public void runsWheneverTheFeederFeeds() {
        assertEquals(State.IN, PrefeedSubsystem.requestFor(IntakeSubsystem.State.IDLE, FeederSubsystem.State.FEED));
        assertEquals(State.IN, PrefeedSubsystem.requestFor(IntakeSubsystem.State.IN, FeederSubsystem.State.FEED));
    }

    @Test
    public void staysStillWhenNothingIsMovingPieces() {
        assertEquals(State.IDLE, PrefeedSubsystem.requestFor(IntakeSubsystem.State.IDLE, FeederSubsystem.State.IDLE));
    }

    @Test
    public void backsOutWithAnEjectOrAnUnjam() {
        assertEquals(State.OUT, PrefeedSubsystem.requestFor(IntakeSubsystem.State.OUT, FeederSubsystem.State.IDLE));
        assertEquals(State.OUT, PrefeedSubsystem.requestFor(IntakeSubsystem.State.IDLE, FeederSubsystem.State.REVERSE));
    }

    @Test
    public void backingOutWinsOverCarryingIn() {
        assertEquals(State.OUT, PrefeedSubsystem.requestFor(IntakeSubsystem.State.IN, FeederSubsystem.State.REVERSE));
        assertEquals(State.OUT, PrefeedSubsystem.requestFor(IntakeSubsystem.State.OUT, FeederSubsystem.State.FEED));
    }

    @Test
    public void everyCombinationIsDecided() {
        for (IntakeSubsystem.State intake : INTAKE) {
            for (FeederSubsystem.State feeder : FEEDER) {
                boolean moving = intake != IntakeSubsystem.State.IDLE || feeder != FeederSubsystem.State.IDLE;
                assertEquals(intake + "/" + feeder, moving, PrefeedSubsystem.requestFor(intake, feeder) != State.IDLE);
            }
        }
    }

    @Test
    public void idleIsZeroWhateverThePowers() {
        assertEquals(0.0, PrefeedSubsystem.powerFor(State.IDLE, 0.8, 0.6, false), EPS);
        assertEquals(0.0, PrefeedSubsystem.powerFor(State.IDLE, 0.8, 0.6, true), EPS);
    }

    @Test
    public void inIsPositiveAndOutIsNegative() {
        assertEquals(0.8, PrefeedSubsystem.powerFor(State.IN, 0.8, 0.6, false), EPS);
        assertEquals(-0.6, PrefeedSubsystem.powerFor(State.OUT, 0.8, 0.6, false), EPS);
    }

    @Test
    public void reversedFlipsBothDirections() {
        assertEquals(-0.8, PrefeedSubsystem.powerFor(State.IN, 0.8, 0.6, true), EPS);
        assertEquals(0.6, PrefeedSubsystem.powerFor(State.OUT, 0.8, 0.6, true), EPS);
    }

    @Test
    public void aPanelsTypoIsClampedNeverOverdriven() {
        assertEquals(1.0, PrefeedSubsystem.powerFor(State.IN, 4.0, 0.6, false), EPS);
        assertEquals(-1.0, PrefeedSubsystem.powerFor(State.OUT, 0.8, 9.0, false), EPS);
        assertEquals(0.0, PrefeedSubsystem.powerFor(State.IN, -1.0, 0.6, false), EPS);
        assertEquals(0.0, PrefeedSubsystem.powerFor(State.IN, Double.NaN, 0.6, false), EPS);
    }

    @Test
    public void anOutPowerOfZeroLeavesItStillWhileBackingOut() {
        assertEquals(0.0, PrefeedSubsystem.powerFor(State.OUT, 0.8, 0.0, false), EPS);
    }

    @Test
    public void gateClearingIsOffAtZeroAndLastsItsTime() {
        assertEquals(false, PrefeedSubsystem.clearingGate(0.0, 0.0));
        assertEquals(false, PrefeedSubsystem.clearingGate(0.0, Double.NaN));
        assertEquals(true, PrefeedSubsystem.clearingGate(0.0, 0.15));
        assertEquals(true, PrefeedSubsystem.clearingGate(0.14, 0.15));
        assertEquals(false, PrefeedSubsystem.clearingGate(0.15, 0.15));
    }

    @Test
    public void gateClearingHoldsAtZeroPowerAndBacksOffAboveIt() {
        assertEquals(0.0, PrefeedSubsystem.powerFor(State.OUT, 0.0, 0.0, false), EPS);
        assertEquals(-0.4, PrefeedSubsystem.powerFor(State.OUT, 0.0, 0.4, false), EPS);
        assertEquals(0.4, PrefeedSubsystem.powerFor(State.OUT, 0.0, 0.4, true), EPS);
    }
}
