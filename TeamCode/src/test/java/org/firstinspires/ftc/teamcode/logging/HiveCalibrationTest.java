package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.vision.HiveTracker;
import org.junit.Test;

/**
 * The simulated HIVE passes the calibration FIRST's field staff give every real HIVE (Event Field
 * Setup Guide V1.0 §12.3), and tips at the measured speed.
 */
public class HiveCalibrationTest {

    /** One row of §12.3: a CELL holding these pieces, then one more POLLEN placed or tossed in. */
    private static void row(int nectar, int pollen, boolean tossed, boolean tips) {
        FieldSim.Physics physics = HiveCalibration.current().fit();
        FieldSim sim = HiveCalibration.upwardCell(physics, nectar, pollen, false);
        String what = nectar + " NECTAR + " + pollen + " POLLEN, then one more " + (tossed ? "tossed in" : "placed");
        assertEquals(what + ": tipped before the test piece", 0, sim.red.tips);
        FieldSim.Piece p = tossed
                ? sim.tossIntoRaisedCell(sim.red, FieldSim.Kind.POLLEN, HiveCalibration.TOSS_IN_PER_S)
                : sim.placeInRaisedCell(sim.red, FieldSim.Kind.POLLEN);
        assertNotNull(p);
        HiveCalibration.settleRocker(sim);
        assertEquals(what, tips ? 1 : 0, sim.red.tips);
    }

    @Test
    public void nectarRows() {
        row(3, 1, true, false);  // 2nd POLLEN tossed in: no tip (necessary)
        row(3, 2, false, true);  // 3rd POLLEN placed: tip (preferred)
        row(3, 2, true, true);   // 3rd POLLEN tossed in: tip (necessary)
    }

    @Test
    public void pollenRows() {
        row(0, 6, true, false);  // 7th POLLEN tossed in: no tip (necessary)
        row(0, 7, false, true);  // 8th POLLEN placed: tip (preferred)
        row(0, 7, true, true);   // 8th POLLEN tossed in: tip (necessary)
    }

    @Test
    public void fromTheStartOfAMatchTheThirdPollenTips() {
        assertEquals(3, HiveCalibration.current().pollenToTipFromMatchStart());
        assertEquals(0.091 / 0.055, HiveCalibration.current().fit().nectarWeight, 1e-12);
    }

    /** Slow tiles are a what-if for the pieces, not a different HIVE: the fit ignores frictionScale. */
    @Test
    public void theFitIsTheSameOnSlowTiles() {
        HiveCalibration.forgetFits();
        FieldSim.Physics normal = HiveCalibration.current().fit();
        HiveCalibration.forgetFits();
        FieldSim.frictionScale = 3;
        try {
            FieldSim.Physics slow = HiveCalibration.current().fit();
            assertEquals(normal.holdTorque, slow.holdTorque, 1e-12);
            assertEquals(normal.swingRadPerS, slow.swingRadPerS, 1e-12);
            assertEquals(3, FieldSim.frictionScale, 0);
        } finally {
            FieldSim.frictionScale = 1;
        }
    }

    /**
     * Each TIP's time is drawn from the filmed range (FieldSim.tipSecondsRange): the calibration load,
     * tipped in 20 matches, takes from about the range's low end to its high end, never the same.
     */
    @Test
    public void eachTipTakesATimeFromTheFilmedRange() {
        FieldSim.Physics physics = HiveCalibration.current().fit();
        double lo = Double.POSITIVE_INFINITY, hi = 0;
        for (long seed = 1; seed <= 20; seed++) {
            FieldSim sim = new FieldSim(new java.util.ArrayList<>(), seed, physics);
            sim.red.locked = true;
            for (int i = 0; i < HiveCalibration.NECTAR_AT_MATCH_START; i++) {
                sim.placeInRaisedCell(sim.red, FieldSim.Kind.RED_NECTAR);
                HiveCalibration.settle(sim);
            }
            sim.red.locked = false;
            for (int k = 0; k < 6 && sim.red.tips == 0; k++) {
                sim.placeInRaisedCell(sim.red, FieldSim.Kind.POLLEN);
                HiveCalibration.settleRocker(sim);
            }
            assertEquals("seed " + seed + " tipped", 1, sim.red.tips);
            lo = Math.min(lo, sim.red.lastTipSeconds);
            hi = Math.max(hi, sim.red.lastTipSeconds);
        }
        double[] range = FieldSim.FILMED_TIP_SECONDS;
        assertTrue("fastest " + lo, lo > range[0] - 0.1 && lo < range[0] + 0.2);
        assertTrue("slowest " + hi, hi < range[1] + 0.1 && hi > range[1] - 0.2);
    }

    @Test
    public void aTipTakesTheCalibratedTime() {
        for (double seconds : new double[] {0.6, 1.0, 2.0}) {
            HiveCalibration calibration = HiveCalibration.of(seconds, 0.35);
            double took = calibration.timedTip(calibration.fit());
            assertEquals("calibrated to " + seconds + " s", seconds, took, 0.05 * seconds);
        }
    }

    @Test
    public void aDroppedPollenReboundsToTheCalibratedHeight() {
        double drop = 40, rebound = 10;
        FieldSim.Physics physics = HiveCalibration.of(1.0, Math.sqrt(rebound / drop)).fit();
        FieldSim sim = new FieldSim(new java.util.ArrayList<>(), 1, physics);
        FieldSim.Piece p = new FieldSim.Piece(FieldSim.Kind.POLLEN, FieldSim.Where.FIELD, 30, 30,
                drop + FieldSim.POLLEN_RADIUS_IN);
        sim.pieces.add(p);
        boolean bounced = false;
        double apex = 0;
        for (int i = 0; i < 200; i++) {
            sim.step(0.005);
            if (p.vz > 0) bounced = true;
            if (bounced) apex = Math.max(apex, p.z - FieldSim.POLLEN_RADIUS_IN);
            if (bounced && p.vz < 0) break;
        }
        assertEquals(rebound, apex, 0.1 * rebound);
    }

    /** The tip time is the robot's own tuning, so the robot and the simulation agree. */
    @Test
    public void tipTimeComesFromTheRobotsHiveTrackerTuning() {
        double before = HiveTracker.Tuning.tipSeconds;
        try {
            HiveTracker.Tuning.tipSeconds = Double.NaN;
            HiveCalibration unmeasured = HiveCalibration.current();
            assertEquals(HiveCalibration.ASSUMED_TIP_SECONDS, unmeasured.tipSeconds, 0);
            assertTrue(unmeasured.assumed.contains("tip time"));
            assertTrue(unmeasured.describe().contains("Event Field Setup Guide"));
            assertTrue(unmeasured.describe().contains("Assumed, not measured"));

            HiveTracker.Tuning.tipSeconds = 1.7;
            HiveCalibration measured = HiveCalibration.current();
            assertEquals(1.7, measured.tipSeconds, 0);
            assertFalse(measured.assumed.contains("tip time"));
        } finally {
            HiveTracker.Tuning.tipSeconds = before;
        }
    }
}
