package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.vision.HiveTracker;
import org.junit.Test;

/**
 * The simulated HIVE does what the calibration says the real one does: it tips on the measured
 * POLLEN and not one before, and the TIP takes the measured time.
 */
public class HiveCalibrationTest {

    @Test
    public void theHiveTipsOnTheCalibratedPollenAndNotBefore() {
        for (int count : new int[] {1, 3, 6}) {
            FieldSim.Physics physics = HiveCalibration.of(count, 1.5, 1.0, 0.35).fit();
            assertEquals("calibrated to tip on POLLEN " + count, count,
                    HiveCalibration.pollenThatTip(physics, count + 3));
        }
    }

    @Test
    public void heavierNectarMeansFewerPollen() {
        // The same holding torque, with heavier NECTAR already in the CELL, needs less POLLEN.
        FieldSim.Physics light = HiveCalibration.of(5, 1.0, 1.0, 0.35).fit();
        FieldSim.Physics heavy = new FieldSim.Physics(3.0, light.holdTorque, light.swingRadPerS, 0.35);
        assertTrue(HiveCalibration.pollenThatTip(heavy, 8) < 5);
        assertEquals(1.0, light.nectarWeight, 0);
    }

    @Test
    public void aTipTakesTheCalibratedTime() {
        for (double seconds : new double[] {0.6, 1.0, 2.0}) {
            HiveCalibration calibration = HiveCalibration.of(3, 1.5, seconds, 0.35);
            double took = calibration.timedTip(calibration.fit());
            assertEquals("calibrated to " + seconds + " s", seconds, took, 0.05 * seconds);
        }
    }

    @Test
    public void aDroppedPollenReboundsToTheCalibratedHeight() {
        double drop = 40, rebound = 10;
        FieldSim.Physics physics = HiveCalibration.of(3, 1.5, 1.0, Math.sqrt(rebound / drop)).fit();
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
            assertTrue(unmeasured.describe().contains("Assumed, not measured"));

            HiveTracker.Tuning.tipSeconds = 1.7;
            HiveCalibration measured = HiveCalibration.current();
            assertEquals(1.7, measured.tipSeconds, 0);
            assertTrue(!measured.assumed.contains("tip time"));
        } finally {
            HiveTracker.Tuning.tipSeconds = before;
        }
    }
}
