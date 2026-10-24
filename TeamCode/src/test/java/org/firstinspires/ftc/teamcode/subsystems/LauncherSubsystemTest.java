package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.subsystems.LauncherSubsystem.AtSpeedTimer;
import org.junit.Test;

/**
 * The pure part of {@link LauncherSubsystem}: what a target and a Panels edit do to each wheel's
 * command, when "at speed" is allowed to say so, and when a silent encoder is called a fault.
 */
public class LauncherSubsystemTest {

    private static final double EPS = 1e-9;
    private static final long MS = 1_000_000L;

    @Test
    public void commandIsTheTargetNegatedForAReversedWheel() {
        assertEquals(1000.0, LauncherSubsystem.commandFor(1000.0, false), EPS);
        assertEquals(-1000.0, LauncherSubsystem.commandFor(1000.0, true), EPS);
    }

    @Test
    public void negativeOrNanTargetCommandsNothing() {
        assertEquals(0.0, LauncherSubsystem.commandFor(-300.0, false), EPS);
        assertEquals(0.0, LauncherSubsystem.commandFor(-300.0, true), EPS);
        assertEquals(0.0, LauncherSubsystem.commandFor(Double.NaN, false), EPS);
        assertEquals(0.0, LauncherSubsystem.nonNegative(Double.NaN), EPS);
    }

    @Test
    public void atSpeedWaitsForTheHoldTime() {
        AtSpeedTimer timer = new AtSpeedTimer();
        assertFalse(timer.update(true, 0, 150));
        assertFalse(timer.update(true, 100 * MS, 150));
        assertTrue(timer.update(true, 150 * MS, 150));
        assertTrue(timer.update(true, 900 * MS, 150));
    }

    @Test
    public void leavingToleranceRestartsTheHold() {
        AtSpeedTimer timer = new AtSpeedTimer();
        timer.update(true, 0, 150);
        assertTrue(timer.update(true, 200 * MS, 150));
        assertFalse(timer.update(false, 210 * MS, 150));
        assertFalse(timer.update(true, 220 * MS, 150));
        assertFalse(timer.update(true, 360 * MS, 150));
        assertTrue(timer.update(true, 370 * MS, 150));
    }

    @Test
    public void resetForgetsTheHold() {
        AtSpeedTimer timer = new AtSpeedTimer();
        timer.update(true, 0, 150);
        assertTrue(timer.update(true, 200 * MS, 150));
        timer.reset();
        assertFalse(timer.update(true, 210 * MS, 150));
    }

    @Test
    public void aZeroHoldIsAtSpeedAtOnce() {
        AtSpeedTimer timer = new AtSpeedTimer();
        assertTrue(timer.update(true, 5 * MS, 0));
    }

    @Test
    public void encoderSilentNeedsACommandTimeAndNoMotion() {
        // Commanded long enough and reading nothing: a fault.
        assertTrue(LauncherSubsystem.encoderSilent(1000.0, 0.0, 600.0, 500.0, 20.0));
        assertTrue(LauncherSubsystem.encoderSilent(1000.0, 5.0, 500.0, 500.0, 20.0));
        // Too soon to tell.
        assertFalse(LauncherSubsystem.encoderSilent(1000.0, 0.0, 100.0, 500.0, 20.0));
        // Moving at all clears it.
        assertFalse(LauncherSubsystem.encoderSilent(1000.0, 20.0, 600.0, 500.0, 20.0));
        assertFalse(LauncherSubsystem.encoderSilent(1000.0, -40.0, 600.0, 500.0, 20.0));
        // Not commanded: nothing to fault.
        assertFalse(LauncherSubsystem.encoderSilent(0.0, 0.0, 600.0, 500.0, 20.0));
        assertFalse(LauncherSubsystem.encoderSilent(Double.NaN, 0.0, 600.0, 500.0, 20.0));
    }

    @Test
    public void aWheelsTargetIsTheCommonTargetPlusItsTrim() {
        assertEquals(1000.0, LauncherSubsystem.wheelTarget(1000.0, 0.0), EPS);
        assertEquals(1200.0, LauncherSubsystem.wheelTarget(1000.0, 200.0), EPS);
        assertEquals(800.0, LauncherSubsystem.wheelTarget(1000.0, -200.0), EPS);
    }

    @Test
    public void aTrimCannotTakeAWheelBelowZeroOrPoisonIt() {
        assertEquals(0.0, LauncherSubsystem.wheelTarget(1000.0, -5000.0), EPS);
        assertEquals(1000.0, LauncherSubsystem.wheelTarget(1000.0, Double.NaN), EPS);
        assertEquals(200.0, LauncherSubsystem.wheelTarget(Double.NaN, 200.0), EPS);
    }

    @Test
    public void rpmReusesTheIntakesRule() {
        assertEquals(60.0, IntakeSubsystem.rpm(28.0, 28.0), EPS);
        assertEquals(Double.NaN, IntakeSubsystem.rpm(28.0, Double.NaN), EPS);
    }
}
