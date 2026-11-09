package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Random;

public class TurretAbsoluteTest {

    /** The goBILDA turret's 176T ring: 64T on encoder A (2.75), a 56T example gear on B. */
    private static final double RATIO_A = 176.0 / 64.0;
    private static final double RATIO_B = 176.0 / 56.0;
    private static final double HOME_A = 0.37;
    private static final double HOME_B = 0.81;
    private static final double EACH_WAY = 270.0;
    private static final double MAX_RESIDUAL = 8.0;
    private static final double MIN_MARGIN = 16.0;

    /** What the two encoders read with the turret at {@code turretDeg}. */
    private static double[] readings(double turretDeg, int dirA, int dirB) {
        double turns = turretDeg / 360.0;
        double a = HOME_A + dirA * turns * RATIO_A;
        double b = HOME_B + dirB * turns * RATIO_B;
        return new double[] {a - Math.floor(a), b - Math.floor(b)};
    }

    private static TurretAbsolute.Solution solve(double[] r, int dirA, int dirB) {
        return TurretAbsolute.solve(r[0], r[1], HOME_A, HOME_B, RATIO_A, RATIO_B, dirA, dirB,
                EACH_WAY, MAX_RESIDUAL, MIN_MARGIN);
    }

    @Test
    public void everyAngleInTheTravelComesBack() {
        for (double deg = -EACH_WAY; deg <= EACH_WAY; deg += 0.5) {
            TurretAbsolute.Solution s = solve(readings(deg, 1, -1), 1, -1);
            assertTrue(deg + ": " + s.problem, s.ok());
            assertEquals(deg, s.turretDeg, 1e-6);
        }
    }

    @Test
    public void bothDirectionsWork() {
        int[][] dirs = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        for (int[] d : dirs) {
            for (double deg : new double[] {-200, -45, 0, 30, 181, 260}) {
                TurretAbsolute.Solution s = solve(readings(deg, d[0], d[1]), d[0], d[1]);
                assertTrue(s.ok());
                assertEquals(deg, s.turretDeg, 1e-6);
            }
        }
    }

    @Test
    public void readingErrorsWithinTheBudgetStillPickTheRightTurn() {
        Random random = new Random(169);
        double noiseTurns = 2.0 / 360.0; // ±2° on each encoder: worse than the stated ±1.2°
        for (int i = 0; i < 5000; i++) {
            double deg = (random.nextDouble() * 2 - 1) * EACH_WAY;
            double[] r = readings(deg, 1, 1);
            r[0] = wrap01(r[0] + (random.nextDouble() * 2 - 1) * noiseTurns);
            r[1] = wrap01(r[1] + (random.nextDouble() * 2 - 1) * noiseTurns);
            TurretAbsolute.Solution s = solve(r, 1, 1);
            assertTrue(deg + ": " + s.problem, s.ok());
            // A's error divided by its ratio is the turret's error.
            assertEquals(deg, s.turretDeg, 2.0 / RATIO_A + 1e-9);
        }
    }

    @Test
    public void aSlippedEncoderIsCaughtNotBelieved() {
        double[] r = readings(40.0, 1, 1);
        r[1] = wrap01(r[1] + 25.0 / 360.0); // B jumped 25°: no turn of A explains it
        TurretAbsolute.Solution s = solve(r, 1, 1);
        assertFalse(s.ok());
        assertTrue(s.problem, s.problem.startsWith("encoders disagree"));
        assertTrue(Double.isNaN(s.turretDeg));
    }

    @Test
    public void gearsOneToothApartAreTooCloseForThisTravel() {
        double ratioB = 176.0 / 63.0;
        // 64T and 63T: candidates only 5.7° of B apart.
        assertEquals(360.0 * (64.0 / 63.0 - 1.0), TurretAbsolute.separationDeg(RATIO_A, ratioB, EACH_WAY), 1e-6);
        TurretAbsolute.Solution s = TurretAbsolute.solve(0.1, 0.2, 0.1, 0.2, RATIO_A, ratioB, 1, 1,
                EACH_WAY, MAX_RESIDUAL, MIN_MARGIN);
        assertFalse(s.ok());
        assertTrue(s.problem, s.problem.startsWith("ambiguous"));
    }

    @Test
    public void theExamplePairIsWellSeparated() {
        // 64T and 56T on a 176T ring: candidates at least 51° of B apart over ±270°.
        assertEquals(360.0 / 7.0, TurretAbsolute.separationDeg(RATIO_A, RATIO_B, EACH_WAY), 1e-6);
        assertEquals(448.0 / 176.0, TurretAbsolute.unambiguousTurretTurns(176, 64, 56), 1e-12);
    }

    @Test
    public void theMentors44ToothGearAgainstThe64IsWellSeparated() {
        double ratioB = 176.0 / 44.0;
        // Under 262° of total travel only one neighbouring turn of A fits: 163.6° apart.
        assertEquals(360.0 * 5.0 / 11.0, TurretAbsolute.separationDeg(RATIO_A, ratioB, 120.0), 1e-6);
        // Over any longer travel, up to 4 turret turns, never closer than 32.7°.
        assertEquals(360.0 / 11.0, TurretAbsolute.separationDeg(RATIO_A, ratioB, 270.0), 1e-6);
        assertEquals(360.0 / 11.0, TurretAbsolute.separationDeg(RATIO_A, ratioB, 700.0), 1e-6);
        assertEquals(4.0, TurretAbsolute.unambiguousTurretTurns(176, 64, 44), 1e-12);
    }

    @Test
    public void missingSettingsSayWhichOne() {
        assertProblem(TurretAbsolute.solve(0.1, 0.2, Double.NaN, 0.2, RATIO_A, RATIO_B, 1, 1,
                EACH_WAY, MAX_RESIDUAL, MIN_MARGIN), "home");
        assertProblem(TurretAbsolute.solve(0.1, 0.2, 0.1, 0.2, Double.NaN, RATIO_B, 1, 1,
                EACH_WAY, MAX_RESIDUAL, MIN_MARGIN), "ratio");
        assertProblem(TurretAbsolute.solve(0.1, 0.2, 0.1, 0.2, RATIO_A, RATIO_A, 1, 1,
                EACH_WAY, MAX_RESIDUAL, MIN_MARGIN), "equal");
        assertProblem(TurretAbsolute.solve(0.1, 0.2, 0.1, 0.2, RATIO_A, RATIO_B, 0, 1,
                EACH_WAY, MAX_RESIDUAL, MIN_MARGIN), "direction");
        assertProblem(TurretAbsolute.solve(0.1, Double.NaN, 0.1, 0.2, RATIO_A, RATIO_B, 1, 1,
                EACH_WAY, MAX_RESIDUAL, MIN_MARGIN), "encoder B");
    }

    @Test
    public void aPulseBecomesAFractionOfTheEncodersOwnRange() {
        assertEquals(0.0, TurretAbsolute.fractionFromPulse(10, 10, 1810), 1e-12);
        assertEquals(0.5, TurretAbsolute.fractionFromPulse(910, 10, 1810), 1e-12);
        assertEquals(0.0, TurretAbsolute.fractionFromPulse(1810, 10, 1810), 1e-12);
        assertTrue(Double.isNaN(TurretAbsolute.fractionFromPulse(0, 10, 1810)));
        assertTrue(Double.isNaN(TurretAbsolute.fractionFromPulse(3000, 10, 1810)));
        assertTrue(Double.isNaN(TurretAbsolute.fractionFromPulse(500, Double.NaN, 1810)));
    }

    private static void assertProblem(TurretAbsolute.Solution s, String word) {
        assertFalse(s.ok());
        assertNotNull(s.problem);
        assertTrue(s.problem, s.problem.contains(word));
    }

    private static double wrap01(double f) {
        return f - Math.floor(f);
    }
}
