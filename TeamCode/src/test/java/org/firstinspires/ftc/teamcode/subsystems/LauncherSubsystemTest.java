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
    public void reverseOnlyWhileTheFeederReversesAndAfterPlay() {
        assertTrue(LauncherSubsystem.reverseFor(FeederSubsystem.State.REVERSE, true));
        assertFalse(LauncherSubsystem.reverseFor(FeederSubsystem.State.REVERSE, false));
        assertFalse(LauncherSubsystem.reverseFor(FeederSubsystem.State.FEED, true));
        assertFalse(LauncherSubsystem.reverseFor(FeederSubsystem.State.IDLE, true));
    }

    @Test
    public void reverseCommandIsBackwardsInEachWheelsOwnSense() {
        assertEquals(-300.0, LauncherSubsystem.reverseCommandFor(300.0, false), EPS);
        assertEquals(300.0, LauncherSubsystem.reverseCommandFor(300.0, true), EPS);
        assertEquals(0.0, LauncherSubsystem.reverseCommandFor(0.0, true), EPS);
        assertEquals(0.0, LauncherSubsystem.reverseCommandFor(-50.0, false), EPS);
        assertEquals(0.0, LauncherSubsystem.reverseCommandFor(Double.NaN, false), EPS);
    }

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
        return d.update(deficit, within, true, TARGET, DIP);
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
        assertFalse(d.update(LauncherSubsystem.deficit(TARGET, 1500, TARGET, 1250), false, true, TARGET, DIP));
        assertTrue(d.update(LauncherSubsystem.deficit(TARGET, 1500, TARGET, 1480), true, true, TARGET, DIP));
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

    // The dip shapes of the match log of 4 Nov 2026 (20245, 00:02 Basic Drive), target 1595,
    // threshold 70 and tolerance 60 as on the robot, one sample per ~40 ms loop as logged.
    private static final double LOG_TARGET = 1595.0;
    private static final double LOG_DIP = 70.0;

    /** One loop at the log's settings: the two wheels' speeds and whether the gate is open. */
    private static boolean logged(ShotDetector d, double left, double right, boolean gateOpen) {
        double deficit = LauncherSubsystem.deficit(LOG_TARGET, left, LOG_TARGET, right);
        boolean within = Math.abs(left - LOG_TARGET) <= 60.0 && Math.abs(right - LOG_TARGET) <= 60.0;
        return d.update(deficit, within, gateOpen, LOG_TARGET, LOG_DIP);
    }

    /** Drives a shape through the detector, both wheels the same speed; returns the shots counted. */
    private static int sameOnBoth(ShotDetector d, boolean gateOpen, double... speeds) {
        int before = d.shots();
        for (double v : speeds) logged(d, v, v, gateOpen);
        return d.shots() - before;
    }

    @Test
    public void theIntakesSlowSagWithTheGateShutIsNotAShot() {
        // 62.95 s, 1660.15 s, 1695.55 s: a NECTAR stalls the intake, gate shut, feeder unloaded, and
        // both wheels sag 80–100 together over about 0.3 s and come back.
        ShotDetector d = new ShotDetector();
        assertEquals(0, sameOnBoth(d, false, 1600, 1595, 1580, 1560, 1540, 1520, 1500, 1495, 1510, 1540, 1570, 1590));
        assertFalse(d.inDip());
        // The 1509 s shape: the gate shut 0.2 s before, the same slow sag.
        assertEquals(0, sameOnBoth(d, false, 1600, 1580, 1560, 1540, 1520, 1540, 1560, 1580, 1600));
        assertEquals(0, d.shots());
    }

    @Test
    public void oneWheelDippingWithTheGateShutIsNotAShot() {
        // 207.38 s and 207.91 s: the left wheel alone fell about 200, the right unchanged, gate shut.
        ShotDetector d = new ShotDetector();
        logged(d, 1595, 1595, false);
        for (double left : new double[] {1520, 1430, 1395, 1440, 1520, 1570, 1600}) logged(d, left, 1595, false);
        logged(d, 1595, 1595, false);
        for (double left : new double[] {1500, 1375, 1420, 1500, 1580}) logged(d, left, 1595, false);
        assertEquals(0, d.shots());
        assertFalse(d.inDip());
    }

    @Test
    public void aFedShotsSharpDipIsStillOneShot() {
        // A real shot: gate open, both wheels fall 150–200 within a loop or two and recover.
        ShotDetector d = new ShotDetector();
        assertEquals(1, sameOnBoth(d, true, 1600, 1595, 1440, 1400, 1470, 1530, 1580, 1600));
        assertEquals(195.0, d.lastDipTicksPerSecond(), EPS);
        // A fed shot that pulls mostly one wheel (134.95 s: left 35, right 115) counts too.
        logged(d, 1560, 1480, true);
        logged(d, 1590, 1600, true);
        assertEquals(2, d.shots());
    }

    @Test
    public void aDipThatStartsWithTheGateOpenCountsEvenIfItShutsBeforeRecovery() {
        ShotDetector d = new ShotDetector();
        logged(d, 1595, 1595, true);
        logged(d, 1420, 1410, true);              // the piece hits with the gate open
        logged(d, 1450, 1450, false);             // the driver lets go: the gate shuts mid-dip
        assertTrue(logged(d, 1580, 1590, false)); // recovered: still the fed piece's shot
        assertEquals(1, d.shots());
    }

    @Test
    public void aGateShutDipDoesNotSpoilTheNextFedShot() {
        ShotDetector d = new ShotDetector();
        logged(d, 1595, 1595, false);
        sameOnBoth(d, false, 1540, 1500, 1510, 1590);   // the intake's sag: ignored
        assertEquals(1, sameOnBoth(d, true, 1600, 1420, 1470, 1590));
    }

    @Test
    public void deficitIsTheSlowerWheelsShortfall() {
        assertEquals(300.0, LauncherSubsystem.deficit(1500, 1400, 1500, 1200), EPS);
        assertEquals(-20.0, LauncherSubsystem.deficit(1500, 1520, 1500, 1530), EPS);
        assertEquals(250.0, LauncherSubsystem.deficit(1500, 1250, 1600, 1600), EPS);
    }
}
