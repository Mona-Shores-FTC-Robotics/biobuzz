package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * {@link LauncherResponse}: the spin-up and dip timings, driven with made-up readings shaped like
 * 3 and 4 Nov 2026's logs (a 1595 target, ±60 tolerance, a 95 dip, an overshoot to about 1680).
 */
public class LauncherResponseTest {

    private static final double EPS = 1e-6;
    private static final long MS = 1_000_000L;

    private final LauncherResponse r = new LauncherResponse();

    /** One loop: {@code excess} over target, within if |excess| ≤ 60 (the at-speed tolerance). */
    private void loop(long ms, double excess, boolean atSpeed) {
        loop(ms, excess, atSpeed, false, false, Double.NaN);
    }

    private void loop(long ms, double excess, boolean atSpeed, boolean inDip, boolean shot, double depth) {
        r.update(ms * MS, Math.abs(excess) <= 60.0, atSpeed, excess, inDip, shot, depth);
    }

    /** A spin-up from rest at 0 ms: in tolerance at 600, 40 over at 650, at speed at 800. */
    private void spinUp() {
        r.command(0, 0.0);
        loop(100, -1200, false);
        loop(400, -300, false);
        loop(600, -55, false);
        loop(650, 40, false);
        loop(700, 10, false);
        loop(800, 5, true);
    }

    @Test
    public void aSpinUpGivesRiseOvershootAndSettle() {
        spinUp();
        assertEquals(1, r.spinUps());
        assertEquals(600.0, r.riseMs(), EPS);
        assertEquals(40.0, r.spinOvershootTps(), EPS);
        assertEquals(800.0, r.settleMs(), EPS);
        assertEquals(0.0, r.spinFromTps(), EPS);
        assertFalse(r.spinningUp());
    }

    @Test
    public void aSpinUpThatNeverGoesOverHasNoOvershoot() {
        r.command(0, 0.0);
        loop(500, -50, false);
        loop(700, -20, true);
        assertEquals(0.0, r.spinOvershootTps(), EPS);
        assertEquals(500.0, r.riseMs(), EPS);
    }

    @Test
    public void aSpinUpInProgressIsNotAResult() {
        r.command(0, 0.0);
        loop(300, -800, false);
        assertTrue(r.spinningUp());
        assertEquals(300.0, r.spinUpElapsedMs(300 * MS), EPS);
        assertEquals(0, r.spinUps());
        assertTrue(Double.isNaN(r.riseMs()));
        r.cancel();
        assertFalse(r.spinningUp());
        loop(900, 0, true);
        assertEquals("a spin-down dropped it", 0, r.spinUps());
    }

    @Test
    public void aDipIsTimedFromLeavingTheBand() {
        spinUp();
        loop(1000, 0, true);
        loop(1010, -70, false);                        // out of the band
        loop(1020, -95, false, true, false, Double.NaN); // the detector's dip
        loop(1100, -40, false, true, false, Double.NaN); // (within, but the detector counts next loop)
        loop(1210, -30, false, false, true, 95.0);      // counted: back in tolerance
        loop(1300, 85, false);                          // overshoots past the tolerance
        loop(1500, 40, false);
        loop(1710, 10, true);                           // at speed again
        assertEquals(1, r.dips());
        assertEquals(95.0, r.dipDepthTps(), EPS);
        assertEquals(200.0, r.recoverMs(), EPS);
        assertEquals(85.0, r.dipOvershootTps(), EPS);
        assertEquals(700.0, r.resettleMs(), EPS);
    }

    @Test
    public void aSecondDipBeforeSettlingClosesTheFirstWithNoResettle() {
        spinUp();
        loop(1000, 0, true);
        loop(1010, -80, false, true, false, Double.NaN);
        loop(1200, 0, false, false, true, 90.0);
        loop(1300, 70, false);
        loop(1400, -80, false, true, false, Double.NaN);
        assertEquals(1, r.dips());
        assertTrue(Double.isNaN(r.resettleMs()));
        assertEquals(70.0, r.dipOvershootTps(), EPS);
    }

    @Test
    public void aDipTheDetectorDropsIsNotADip() {
        spinUp();
        loop(1000, 0, true);
        loop(1010, -80, false, true, false, Double.NaN);
        loop(1100, -80, false, false, false, Double.NaN); // reset: a target change or a jam
        loop(1500, 0, true);
        assertEquals(0, r.dips());
    }

    @Test
    public void aDipStillHuntingIsClosedAfterTheGiveUpTime() {
        spinUp();
        loop(1000, 0, true);
        loop(1010, -80, false, true, false, Double.NaN);
        loop(1200, 0, false, false, true, 90.0);
        loop(1300, 80, false);
        loop(1000 + (long) LauncherResponse.DIP_GIVE_UP_MS + 20, 70, false);
        assertEquals(1, r.dips());
        assertTrue(Double.isNaN(r.resettleMs()));
    }

    @Test
    public void aCancelWhileSettlingKeepsTheDipWithoutResettle() {
        spinUp();
        loop(1000, 0, true);
        loop(1010, -80, false, true, false, Double.NaN);
        loop(1200, 0, false, false, true, 90.0);
        r.cancel();
        assertEquals(1, r.dips());
        assertEquals(190.0, r.recoverMs(), EPS);
        assertTrue(Double.isNaN(r.resettleMs()));
    }

    @Test
    public void excessIsTheFasterWheelsMirrorOfTheDeficit() {
        assertEquals(85.0, LauncherSubsystem.excess(1595, 1680, 1595, 1650), EPS);
        assertEquals(-5.0, LauncherSubsystem.excess(1595, 1500, 1595, 1590), EPS);
    }
}
