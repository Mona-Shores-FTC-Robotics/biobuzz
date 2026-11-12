package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Random;

public class TurretAbsoluteTest {

    /** The turret's 176T ring: encoder A on a 64T gear (2.75), encoder B on the 44T gear (4.0). */
    private static final double RATIO_A = 176.0 / 64.0;
    private static final double RATIO_B = 176.0 / 44.0;
    private static final double HOME_A = 0.37;
    private static final double HOME_B = 0.81;
    private static final double SEPARATION = 360.0 / 11.0;
    private static final double MAX_RESIDUAL = SEPARATION / 3.0;
    private static final double MIN_MARGIN = SEPARATION / 2.0;

    /** What the two encoders read with the turret {@code turretDeg} from home, any number of turns. */
    private static double[] readings(double turretDeg, int dirA, int dirB) {
        double turns = turretDeg / 360.0;
        double a = HOME_A + dirA * turns * RATIO_A;
        double b = HOME_B + dirB * turns * RATIO_B;
        return new double[] {a - Math.floor(a), b - Math.floor(b)};
    }

    private static TurretAbsolute.Solution solve(double[] r, int dirA, int dirB) {
        return TurretAbsolute.solve(r[0], r[1], HOME_A, HOME_B, RATIO_A, RATIO_B, dirA, dirB,
                MAX_RESIDUAL, MIN_MARGIN);
    }

    @Test
    public void the64And44RepeatAfterFourWholeTurretTurns() {
        assertEquals(4, TurretAbsolute.periodTurretTurns(RATIO_A, RATIO_B));
        assertEquals(SEPARATION, TurretAbsolute.separationDeg(RATIO_A, RATIO_B), 1e-6);
    }

    @Test
    public void everyPointingDirectionComesBackHoweverManyTurnsFromHome() {
        // No stops: the turret may be any number of turns from home at power-on.
        for (double deg = -2000.0; deg <= 2000.0; deg += 0.5) {
            TurretAbsolute.Solution s = solve(readings(deg, 1, 1), 1, 1);
            assertTrue(deg + ": " + s.problem, s.ok());
            assertEquals(deg + "", 0.0, TurretAbsolute.wrap180(deg - s.turretDeg), 1e-6);
            assertTrue(s.turretDeg > -180.0 && s.turretDeg <= 180.0);
        }
    }

    @Test
    public void bothDirectionsWork() {
        int[][] dirs = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        for (int[] d : dirs) {
            for (double deg : new double[] {-170, -45, 0, 30, 179, 541}) {
                TurretAbsolute.Solution s = solve(readings(deg, d[0], d[1]), d[0], d[1]);
                assertTrue(s.ok());
                assertEquals(0.0, TurretAbsolute.wrap180(deg - s.turretDeg), 1e-6);
            }
        }
    }

    @Test
    public void readingErrorsWithinTheBudgetStillPickTheRightTurn() {
        Random random = new Random(169);
        double noiseTurns = 3.0 / 360.0; // ±3° on each encoder: worse than the stated ±1.2°
        for (int i = 0; i < 5000; i++) {
            double deg = (random.nextDouble() * 2 - 1) * 1440.0;
            double[] r = readings(deg, 1, -1);
            r[0] = wrap01(r[0] + (random.nextDouble() * 2 - 1) * noiseTurns);
            r[1] = wrap01(r[1] + (random.nextDouble() * 2 - 1) * noiseTurns);
            TurretAbsolute.Solution s = solve(r, 1, -1);
            assertTrue(deg + ": " + s.problem, s.ok());
            // A's error divided by its ratio is the turret's error.
            assertEquals(0.0, TurretAbsolute.wrap180(deg - s.turretDeg), 3.0 / RATIO_A + 1e-9);
        }
    }

    @Test
    public void aSlippedEncoderIsCaughtNotBelieved() {
        double[] r = readings(40.0, 1, 1);
        r[1] = wrap01(r[1] + 16.0 / 360.0); // B jumped 16°: halfway between two candidates
        TurretAbsolute.Solution s = solve(r, 1, 1);
        assertFalse(s.ok());
        assertTrue(s.problem, s.problem.startsWith("encoders disagree"));
        assertTrue(Double.isNaN(s.turretDeg));
    }

    @Test
    public void gearsOneToothApartAreTooClose() {
        double ratioB = 176.0 / 63.0;
        // 64T and 63T never repeat on a whole turret turn within the search, so no direction.
        assertEquals(0, TurretAbsolute.periodTurretTurns(RATIO_A, ratioB));
        assertProblem(TurretAbsolute.solve(0.1, 0.2, 0.1, 0.2, RATIO_A, ratioB, 1, 1,
                MAX_RESIDUAL, MIN_MARGIN), "whole turret turn");
    }

    @Test
    public void aRatioThatRepeatsOffAWholeTurnIsRefused() {
        // 64T and 56T repeat after 2.55 turret turns: two repeats point different ways.
        assertEquals(0, TurretAbsolute.periodTurretTurns(176.0 / 64.0, 176.0 / 56.0));
    }

    @Test
    public void missingSettingsSayWhichOne() {
        assertProblem(TurretAbsolute.solve(0.1, 0.2, Double.NaN, 0.2, RATIO_A, RATIO_B, 1, 1,
                MAX_RESIDUAL, MIN_MARGIN), "home");
        assertProblem(TurretAbsolute.solve(0.1, 0.2, 0.1, 0.2, Double.NaN, RATIO_B, 1, 1,
                MAX_RESIDUAL, MIN_MARGIN), "ratio");
        assertProblem(TurretAbsolute.solve(0.1, 0.2, 0.1, 0.2, RATIO_A, RATIO_A, 1, 1,
                MAX_RESIDUAL, MIN_MARGIN), "equal");
        assertProblem(TurretAbsolute.solve(0.1, 0.2, 0.1, 0.2, RATIO_A, RATIO_B, 0, 1,
                MAX_RESIDUAL, MIN_MARGIN), "direction");
        assertProblem(TurretAbsolute.solve(0.1, Double.NaN, 0.1, 0.2, RATIO_A, RATIO_B, 1, 1,
                MAX_RESIDUAL, MIN_MARGIN), "encoder B");
    }

    @Test
    public void aReadingBecomesAFractionOfTheEncodersOwnRange() {
        assertEquals(0.0, TurretAbsolute.fractionOfRange(0.01, 0.01, 3.21, 0.05), 1e-12);
        assertEquals(0.5, TurretAbsolute.fractionOfRange(1.61, 0.01, 3.21, 0.05), 1e-12);
        assertEquals(0.0, TurretAbsolute.fractionOfRange(3.21, 0.01, 3.21, 0.05), 1e-12);
        // Just outside the range is noise at an end, and still a reading; far outside is not.
        assertEquals(0.0, TurretAbsolute.fractionOfRange(3.23, 0.01, 3.21, 0.05) , 0.01);
        assertTrue(Double.isNaN(TurretAbsolute.fractionOfRange(3.3, 0.01, 3.21, 0.05)));
        assertTrue(Double.isNaN(TurretAbsolute.fractionOfRange(1.0, Double.NaN, 3.21, 0.05)));
        assertTrue(Double.isNaN(TurretAbsolute.fractionOfRange(Double.NaN, 0.01, 3.21, 0.05)));
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
