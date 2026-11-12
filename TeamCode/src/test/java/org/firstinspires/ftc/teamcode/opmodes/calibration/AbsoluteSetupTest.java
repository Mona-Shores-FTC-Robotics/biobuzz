package org.firstinspires.ftc.teamcode.opmodes.calibration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.subsystems.TurretAbsolute;
import org.junit.Test;

import java.util.List;

/** The guided setup, driven by two simulated Melonbotics encoders' analog outputs on a turret turned by hand. */
public class AbsoluteSetupTest {

    private static final double RATIO_A = 176.0 / 64.0;
    private static final double RATIO_B = 176.0 / 44.0;

    /** Two encoders with their own voltage range, direction and home, and the turret's angle. */
    private static final class Rig {
        double turretDeg;
        double minA = 0.004, maxA = 3.214, minB = 0.009, maxB = 3.187;
        int dirA = -1, dirB = -1;
        double homeA = 0.62, homeB = 0.17;
        boolean swapped;

        /** The hub reads about a millivolt per step. */
        double pulse(double ratio, int dir, double home, double min, double max) {
            double f = home + dir * turretDeg / 360.0 * ratio;
            f -= Math.floor(f);
            return Math.round((min + f * (max - min)) * 1000.0) / 1000.0;
        }

        double pulseA() {
            return swapped ? pulse(RATIO_B, dirB, homeB, minB, maxB) : pulse(RATIO_A, dirA, homeA, minA, maxA);
        }

        double pulseB() {
            return swapped ? pulse(RATIO_A, dirA, homeA, minA, maxA) : pulse(RATIO_B, dirB, homeB, minB, maxB);
        }
    }

    private static void loop(AbsoluteSetup setup, Rig rig, boolean cross) {
        setup.update(true, null, rig.pulseA(), rig.pulseB(), cross);
    }

    /** Turn the turret by {@code deg} in steps of {@code stepDeg}, one loop each. */
    private static void turn(AbsoluteSetup setup, Rig rig, double deg, double stepDeg) {
        int n = (int) Math.ceil(Math.abs(deg) / stepDeg);
        for (int i = 0; i < n; i++) {
            rig.turretDeg += deg / n;
            loop(setup, rig, false);
        }
    }

    private static void still(AbsoluteSetup setup, Rig rig, int loops) {
        for (int i = 0; i < loops; i++) loop(setup, rig, false);
    }

    /** A wiggle: both readings move, so both cables are in. */
    private static void wiggle(AbsoluteSetup setup, Rig rig) {
        turn(setup, rig, 30, 0.5);
        turn(setup, rig, -30, 0.5);
    }

    /** Through BOARD, RANGES and DIRECTIONS, ending at HOME. */
    private static void toHome(AbsoluteSetup setup, Rig rig) {
        wiggle(setup, rig);
        assertEquals(AbsoluteSetup.Step.RANGES, setup.step());
        turn(setup, rig, -400, 0.5);
        assertEquals(AbsoluteSetup.Step.DIRECTIONS, setup.step());
        still(setup, rig, AbsoluteSetup.STILL_LOOPS + 1);
        turn(setup, rig, 45, 0.5);
        still(setup, rig, AbsoluteSetup.STILL_LOOPS + 1);
    }

    @Test
    public void aWholeSetupFindsWhatTheEncodersAre() {
        Rig rig = new Rig();
        AbsoluteSetup setup = new AbsoluteSetup(RATIO_A, RATIO_B);
        toHome(setup, rig);
        assertEquals(AbsoluteSetup.Step.HOME, setup.step());
        assertEquals(rig.dirA, setup.dirA());
        assertEquals(rig.dirB, setup.dirB());
        // Sampled once a loop while turning, an end can be one loop's step short (0.5° of turret here).
        assertEquals(rig.minA, setup.minA(), AbsoluteSetup.SLOW_STEP_VOLTS);
        assertEquals(rig.maxA, setup.maxA(), AbsoluteSetup.SLOW_STEP_VOLTS);
        assertEquals(rig.minB, setup.minB(), AbsoluteSetup.SLOW_STEP_VOLTS);
        assertEquals(rig.maxB, setup.maxB(), AbsoluteSetup.SLOW_STEP_VOLTS);

        // Back on the mark, then Cross: the home readings.
        rig.turretDeg = 0;
        loop(setup, rig, true);
        still(setup, rig, AbsoluteSetup.HOME_LOOPS);
        assertEquals(AbsoluteSetup.Step.CHECK, setup.step());
        assertEquals(rig.homeA, setup.homeA(), 0.003);
        assertEquals(rig.homeB, setup.homeB(), 0.003);

        // The solve with what it found gives the turret's angle anywhere, several turns out.
        for (double deg = -900; deg <= 900; deg += 7.5) {
            rig.turretDeg = deg;
            double fa = TurretAbsolute.fractionOfRange(rig.pulseA(), setup.minA(), setup.maxA(), AbsoluteSetup.SLACK_VOLTS);
            double fb = TurretAbsolute.fractionOfRange(rig.pulseB(), setup.minB(), setup.maxB(), AbsoluteSetup.SLACK_VOLTS);
            TurretAbsolute.Solution s = TurretAbsolute.solve(fa, fb, setup.homeA(), setup.homeB(),
                    RATIO_A, RATIO_B, setup.dirA(), setup.dirB(), 10.9, 16.4);
            assertTrue(deg + ": " + s.problem, s.ok());
            assertEquals(0.0, TurretAbsolute.wrap180(deg - s.turretDeg), 1.0);
            setup.checkSolution(s);
        }
        loop(setup, rig, true);
        assertEquals(AbsoluteSetup.Step.DONE, setup.step());
        List<String> paste = setup.pasteLines("TEAM_19429", "2026-11-12");
        assertTrue(paste.get(0).contains("RobotIdentity.TEAM_19429"));
        assertTrue(String.join("\n", paste).contains("TurretSubsystem.useAbsoluteZero = true;"));
    }

    @Test
    public void anEncoderThatNeverMovesSaysWhich() {
        AbsoluteSetup setup = new AbsoluteSetup(RATIO_A, RATIO_B);
        setup.update(true, null, 1.0, 0.0, false);
        setup.update(true, null, 1.5, 0.0, false);
        assertEquals(AbsoluteSetup.Step.BOARD, setup.step());
        assertTrue(setup.problem(), setup.problem().contains("B (44T, analog input 1)"));
        setup.update(false, "no reading", 1.5, 0.0, false);
        assertEquals("no reading", setup.problem());
    }

    @Test
    public void swappedChannelsAreCaughtByTheRatio() {
        Rig rig = new Rig();
        rig.swapped = true;
        AbsoluteSetup setup = new AbsoluteSetup(RATIO_A, RATIO_B);
        toHome(setup, rig);
        assertEquals(AbsoluteSetup.Step.DIRECTIONS, setup.step());
        assertNotNull(setup.problem());
        assertTrue(setup.problem(), setup.problem().contains("swapped"));
    }

    @Test
    public void movingWhileHomingIsRefused() {
        Rig rig = new Rig();
        AbsoluteSetup setup = new AbsoluteSetup(RATIO_A, RATIO_B);
        toHome(setup, rig);
        loop(setup, rig, true);
        turn(setup, rig, 10, 0.5);
        still(setup, rig, AbsoluteSetup.HOME_LOOPS);
        assertEquals(AbsoluteSetup.Step.HOME, setup.step());
        assertTrue(setup.problem(), setup.problem().contains("moved"));
    }

    @Test
    public void aRangeNoMelonboticsEncoderGivesIsRefused() {
        Rig rig = new Rig();
        rig.maxA = 2.0; // a span of 2 V
        AbsoluteSetup setup = new AbsoluteSetup(RATIO_A, RATIO_B);
        wiggle(setup, rig);
        turn(setup, rig, -400, 0.5);
        assertEquals(AbsoluteSetup.Step.RANGES, setup.step());
        assertTrue(setup.problem(), setup.problem().contains("A's range"));
    }

    @Test
    public void backAndRedoStartAStepAgain() {
        Rig rig = new Rig();
        AbsoluteSetup setup = new AbsoluteSetup(RATIO_A, RATIO_B);
        wiggle(setup, rig);
        turn(setup, rig, -100, 0.5);
        setup.redo();
        assertEquals(AbsoluteSetup.Step.RANGES, setup.step());
        loop(setup, rig, false);
        assertTrue(setup.lines().get(1), setup.lines().get(1).contains("wraps 0"));
        setup.back();
        assertEquals(AbsoluteSetup.Step.BOARD, setup.step());
        assertNull(setup.problem());
    }
}
