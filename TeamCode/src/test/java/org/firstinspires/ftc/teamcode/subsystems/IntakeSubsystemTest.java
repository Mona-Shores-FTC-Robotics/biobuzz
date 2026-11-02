package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;

import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.teamcode.subsystems.IntakeSubsystem.State;
import org.junit.Test;

/** The pure part of {@link IntakeSubsystem}: what each request, and a Panels edit, does to the motor. */
public class IntakeSubsystemTest {

    private static final double EPS = 1e-9;

    @Test
    public void idleIsZeroWhateverThePowers() {
        assertEquals(0.0, IntakeSubsystem.powerFor(State.IDLE, 1.0, 0.6), EPS);
    }

    @Test
    public void inIsPositiveAndOutIsNegative() {
        assertEquals(1.0, IntakeSubsystem.powerFor(State.IN, 1.0, 0.6), EPS);
        assertEquals(-0.6, IntakeSubsystem.powerFor(State.OUT, 1.0, 0.6), EPS);
    }

    /**
     * Reversed flips the motor's direction, not the power, so the encoder flips with it: taking
     * pieces in reads positive on either robot.
     */
    @Test
    public void reversedSetsTheMotorDirectionNotThePower() {
        assertEquals(DcMotorSimple.Direction.REVERSE, IntakeSubsystem.directionFor(true));
        assertEquals(DcMotorSimple.Direction.FORWARD, IntakeSubsystem.directionFor(false));
        assertEquals(1.0, IntakeSubsystem.powerFor(State.IN, 1.0, 0.6), EPS);
    }

    @Test
    public void powersPastFullAreClamped() {
        assertEquals(1.0, IntakeSubsystem.powerFor(State.IN, 1.7, 0.6), EPS);
        assertEquals(-1.0, IntakeSubsystem.powerFor(State.OUT, 1.0, 2.5), EPS);
        assertEquals(0.0, IntakeSubsystem.powerFor(State.IN, -0.3, 0.6), EPS);
    }

    @Test
    public void rpmNeedsTicksPerRev() {
        assertEquals(60.0, IntakeSubsystem.rpm(537.7, 537.7), EPS);
        assertEquals(Double.NaN, IntakeSubsystem.rpm(537.7, Double.NaN), EPS);
        assertEquals(Double.NaN, IntakeSubsystem.rpm(537.7, 0.0), EPS);
        assertEquals(Double.NaN, IntakeSubsystem.rpm(537.7, -1.0), EPS);
    }

    @Test
    public void starsTurnOnlyWhileTakingIn() {
        assertEquals(1.0, IntakeSubsystem.starsPowerFor(State.IN, 1.0, false), EPS);
        assertEquals(-1.0, IntakeSubsystem.starsPowerFor(State.OUT, 1.0, false), EPS);
        assertEquals(0.0, IntakeSubsystem.starsPowerFor(State.IDLE, 1.0, false), EPS);
    }

    @Test
    public void starsReversedFlipsThemAndTheirPowerIsClamped() {
        assertEquals(-1.0, IntakeSubsystem.starsPowerFor(State.IN, 1.0, true), EPS);
        assertEquals(1.0, IntakeSubsystem.starsPowerFor(State.IN, 3.0, false), EPS);
        assertEquals(0.0, IntakeSubsystem.starsPowerFor(State.IN, Double.NaN, false), EPS);
        assertEquals(1.0, IntakeSubsystem.starsPowerFor(State.OUT, 1.0, true), EPS);
    }

    @Test
    public void aFeedTurnsTheStarsAndByDefaultTheRoller() {
        assertEquals(State.IN, IntakeSubsystem.rollerFor(State.IDLE, FeederSubsystem.State.FEED, true));
        assertEquals(State.IN, IntakeSubsystem.starsFor(State.IDLE, FeederSubsystem.State.FEED));
    }

    @Test
    public void theRollerCanSitOutAFeedButTheStarsCannot() {
        assertEquals(State.IDLE, IntakeSubsystem.rollerFor(State.IDLE, FeederSubsystem.State.FEED, false));
        assertEquals(State.IN, IntakeSubsystem.starsFor(State.IDLE, FeederSubsystem.State.FEED));
    }

    @Test
    public void theOpModesRequestStillWins() {
        // Intake held while firing: unchanged, whatever the switch says.
        assertEquals(State.IN, IntakeSubsystem.rollerFor(State.IN, FeederSubsystem.State.FEED, false));
        // Eject (unjam) backs everything out whatever the feeder does.
        assertEquals(State.OUT, IntakeSubsystem.rollerFor(State.OUT, FeederSubsystem.State.FEED, true));
        assertEquals(State.OUT, IntakeSubsystem.starsFor(State.OUT, FeederSubsystem.State.FEED));
    }

    @Test
    public void aReversingFeederBacksTheIntakeOut() {
        assertEquals(State.OUT, IntakeSubsystem.rollerFor(State.IDLE, FeederSubsystem.State.REVERSE, false));
        assertEquals(State.OUT, IntakeSubsystem.rollerFor(State.IN, FeederSubsystem.State.REVERSE, true));
        assertEquals(State.OUT, IntakeSubsystem.starsFor(State.IN, FeederSubsystem.State.REVERSE));
    }

    @Test
    public void nothingAskedNothingMoves() {
        assertEquals(State.IDLE, IntakeSubsystem.rollerFor(State.IDLE, FeederSubsystem.State.IDLE, true));
        assertEquals(State.IDLE, IntakeSubsystem.starsFor(State.IDLE, FeederSubsystem.State.IDLE));
    }

    @Test
    public void nanStopsTheRoller() {
        assertEquals(0.0, IntakeSubsystem.powerFor(State.IN, Double.NaN, 0.6), EPS);
        assertEquals(0.0, IntakeSubsystem.powerFor(State.OUT, 1.0, Double.NaN), EPS);
    }
}
