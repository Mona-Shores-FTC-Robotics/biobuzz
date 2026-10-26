package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.subsystems.LauncherSubsystem.AtSpeedTimer;
import org.firstinspires.ftc.teamcode.subsystems.LauncherSubsystem.ShotDetector;
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

    // ---------------------------------------------------------------- shots from the speed dip

    private static final double TARGET = 1500.0;
    private static final double DIP = 150.0;

    /** One loop of the detector at a measured speed, both wheels the same, tolerance 60. */
    private static boolean at(ShotDetector d, double measured) {
        double deficit = LauncherSubsystem.deficit(TARGET, measured, TARGET, measured);
        boolean within = Math.abs(measured - TARGET) <= 60.0;
        return d.update(deficit, within, TARGET, DIP);
    }

    @Test
    public void aDipFromAtSpeedThatRecoversIsOneShot() {
        ShotDetector d = new ShotDetector();
        at(d, 1500); at(d, 1490);                 // at speed: armed
        assertFalse(at(d, 1300));                 // the piece hits: dip starts
        assertFalse(at(d, 1150));                 // bottom
        assertFalse(at(d, 1400));                 // recovering, not yet within
        assertTrue(d.inDip());
        assertTrue(at(d, 1460));                  // back within tolerance: counted now
        assertEquals(1, d.shots());
        assertEquals(350.0, d.lastDipTicksPerSecond(), EPS);
        assertEquals(TARGET, d.lastTargetTicksPerSecond(), EPS);
        assertTrue(d.justCounted());
        assertFalse(at(d, 1500));
        assertFalse(d.justCounted());
        assertEquals(1, d.shots());
    }

    @Test
    public void spinUpIsNotAShot() {
        ShotDetector d = new ShotDetector();
        for (double v : new double[] {0, 300, 800, 1200, 1400}) assertFalse(at(d, v));
        assertFalse(at(d, 1480));
        assertEquals(0, d.shots());
        assertFalse(d.inDip());
    }

    @Test
    public void aWobbleInsideTheThresholdIsNotAShot() {
        ShotDetector d = new ShotDetector();
        at(d, 1500);
        assertFalse(at(d, 1400));                 // 100 below: out of tolerance, under the dip threshold
        assertFalse(at(d, 1470));
        assertEquals(0, d.shots());
    }

    @Test
    public void twoDipsAreTwoShotsAndOnlyTheSlowerWheelNeedsToDip() {
        ShotDetector d = new ShotDetector();
        at(d, 1500);
        at(d, 1200); assertTrue(at(d, 1500));
        // Right wheel alone drops: still a dip.
        assertFalse(d.update(LauncherSubsystem.deficit(TARGET, 1500, TARGET, 1250), false, TARGET, DIP));
        assertTrue(d.update(LauncherSubsystem.deficit(TARGET, 1500, TARGET, 1480), true, TARGET, DIP));
        assertEquals(2, d.shots());
        assertEquals(250.0, d.lastDipTicksPerSecond(), EPS);
    }

    @Test
    public void aDipThatNeverRecoversIsNotCountedAndResetDropsIt() {
        ShotDetector d = new ShotDetector();
        at(d, 1500);
        at(d, 1000); at(d, 900);
        assertTrue(d.inDip());
        d.reset();                                // spin-down, or a target change
        assertFalse(d.inDip());
        assertEquals(0, d.shots());
        // Not armed after a reset: a return to speed is not a shot, and the next dip is.
        assertFalse(at(d, 1500));
        at(d, 1200); assertTrue(at(d, 1500));
        assertEquals(1, d.shots());
    }

    @Test
    public void deficitIsTheSlowerWheelsShortfall() {
        assertEquals(300.0, LauncherSubsystem.deficit(1500, 1400, 1500, 1200), EPS);
        assertEquals(-20.0, LauncherSubsystem.deficit(1500, 1520, 1500, 1530), EPS);
        assertEquals(250.0, LauncherSubsystem.deficit(1500, 1250, 1600, 1600), EPS);
    }
}
