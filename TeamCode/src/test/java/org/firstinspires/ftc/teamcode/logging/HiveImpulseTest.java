package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import org.firstinspires.ftc.teamcode.localization.StartPositions;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Locale;

/**
 * The simulated HIVE with its hits ({@link FieldSim#kick}): it still passes the calibration FIRST's field staff give
 * every real HIVE (Event Field Setup Guide V1.0 §12.3: tips on 3 NECTAR + 3 POLLEN and on 8 POLLEN, holds on one
 * fewer, placed or gently tossed in), and a fast volley of launched pieces onto a CELL just under its tipping weight
 * sometimes knocks it over, as the mentor has seen on video (4 Nov 2026), but never onto 6.5 POLLEN weights or less.
 * The impulse's strength is a placeholder ({@link FieldSim#PLACEHOLDER_IMPULSE_SCALE}); these say what it was set to.
 *
 * <p>Opt-in, like every simulator test but the smoke test: {@code BIOBUZZ_SIM_CHECKS=1}. About 20 s.
 */
public class HiveImpulseTest {

    static final int SEEDS = 20;

    @Before
    public void optIn() {
        assumeTrue("set BIOBUZZ_SIM_CHECKS=1 to run the simulator's HIVE checks", "1".equals(System.getenv("BIOBUZZ_SIM_CHECKS")));
    }

    // ---- §12.3, with the hits on ----------------------------------------------------------------

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
    }

    // ---- Volleys ----------------------------------------------------------------------------------

    /** What a volley did over the seeds: how many tipped, and the load (POLLEN weights) the CELL ended with. */
    static final class Volleys {
        int tipped, knocked, runs;
        double minLoad = Double.POSITIVE_INFINITY, maxLoad;

        @Override
        public String toString() {
            return String.format(Locale.ROOT, "tipped %d of %d (%d knocked over under the tipping weight); load %.2f to %.2f",
                    tipped, runs, knocked, minLoad, maxLoad);
        }
    }

    /**
     * {@code nectar} and {@code pollen} placed in the red raised CELL (the RIGHT one, at the match's start: no
     * starting NECTAR here), then the real robot's volley of {@code shots} POLLEN from RIGHT_START, one every
     * {@link RobotDesign#shotIntervalS}, aimed at the CELL as the robot aims; then settled.
     */
    static Volleys volley(int nectar, int pollen, int shots) {
        FieldSim.Physics physics = HiveCalibration.current().fit();
        Volleys out = new Volleys();
        for (long seed = 1; seed <= SEEDS; seed++) {
            FieldSim sim = new FieldSim(new ArrayList<>(), seed, physics);
            sim.red.locked = true;
            for (int i = 0; i < nectar; i++) {
                sim.placeInRaisedCell(sim.red, FieldSim.Kind.RED_NECTAR);
                HiveCalibration.settle(sim);
            }
            for (int i = 0; i < pollen; i++) {
                sim.placeInRaisedCell(sim.red, FieldSim.Kind.POLLEN);
                HiveCalibration.settle(sim);
            }
            sim.red.locked = false;
            HiveCalibration.settleRocker(sim);
            assertEquals("tipped before the volley", 0, sim.red.tips);
            int end = sim.red.raisedEnd();
            double[] aim = sim.red.aimPointFor(end);
            RobotDesign design = RobotDesign.real();
            sim.main.design = design;
            double x = StartPositions.RED_AUDIENCE.pose.x(), y = StartPositions.RED_AUDIENCE.pose.y();
            double heading = Math.atan2(aim[1] - y, aim[0] - x);
            for (int i = 0; i < shots; i++) {
                FieldSim.Piece held = new FieldSim.Piece(FieldSim.Kind.POLLEN, FieldSim.Where.ROBOT, x, y, 10);
                sim.pieces.add(held);
                sim.main.stored.add(held);
            }
            double next = 0;
            for (int k = 0; k < Math.round(6.0 / HiveCalibration.LOOP_S); k++) {
                sim.setRobot(x, y, heading, 0, 0, 0, false);
                double t = k * HiveCalibration.LOOP_S;
                if (!sim.main.stored.isEmpty() && t >= next && sim.red.raisedEnd() == end) {
                    sim.launch(aim);
                    next = t + design.shotIntervalS;
                }
                sim.step(HiveCalibration.LOOP_S);
            }
            HiveCalibration.settleRocker(sim);
            out.runs++;
            if (sim.red.tips > 0) out.tipped++;
            out.knocked += sim.red.knockedTips;
            double load = 0;
            for (FieldSim.Piece p : sim.pieces) {
                if (p.where == FieldSim.Where.FIELD && p.cell != null && p.cell.alliance() == sim.red.alliance) load += sim.weight(p.kind);
            }
            if (sim.red.tips == 0) {
                out.minLoad = Math.min(out.minLoad, load);
                out.maxLoad = Math.max(out.maxLoad, load);
            }
        }
        return out;
    }

    /** 7 POLLEN after a 4-ball volley, and 4 POLLEN + 2 NECTAR (7.3 weights): it tips sometimes, not always. */
    @Test
    public void aFastVolleyOntoAboutSevenWeightsTipsSometimes() {
        Volleys seven = volley(0, 3, 4);
        Volleys sevenPointThree = volley(2, 0, 4);
        System.out.println("HiveImpulseTest 3 POLLEN + a 4-ball volley (7.0): " + seven);
        System.out.println("HiveImpulseTest 2 NECTAR + a 4-ball volley (7.3): " + sevenPointThree);
        int tipped = seven.tipped + sevenPointThree.tipped, runs = seven.runs + sevenPointThree.runs;
        assertTrue("never tipped: " + seven + "; " + sevenPointThree, tipped > 0);
        assertTrue("always tipped: " + seven + "; " + sevenPointThree, tipped < runs);
    }

    /** 6 POLLEN after a 4-ball volley, and 2 NECTAR + 3 POLLEN (6.3 weights): it never tips. */
    @Test
    public void aVolleyOntoUnderSixAndAHalfWeightsNeverTips() {
        Volleys six = volley(0, 2, 4);
        Volleys sixPointThree = volley(2, 0, 3);
        System.out.println("HiveImpulseTest 2 POLLEN + a 4-ball volley (6.0): " + six);
        System.out.println("HiveImpulseTest 2 NECTAR + a 3-ball volley (6.3): " + sixPointThree);
        System.out.println("HiveImpulseTest 1 NECTAR + 1 POLLEN + a 4-ball volley (6.65): " + volley(1, 1, 4));
        assertEquals(six.toString(), 0, six.tipped);
        assertEquals(sixPointThree.toString(), 0, sixPointThree.tipped);
    }

    /** With the hits off, the volleys' TIPs are the static threshold's alone: none under it. */
    @Test
    public void withTheHitsOffNothingUnderTheThresholdTips() {
        double before = FieldSim.impulseScale;
        FieldSim.impulseScale = 0;
        try {
            Volleys seven = volley(2, 0, 4);
            assertEquals(seven.toString(), 0, seven.tipped);
        } finally {
            FieldSim.impulseScale = before;
        }
    }

    /**
     * For re-fitting {@link FieldSim#PLACEHOLDER_IMPULSE_SCALE} once video counts exist: {@code BIOBUZZ_SIM_SWEEP=0.5,1,2}
     * prints every case's tip rate at each strength. 4 Nov 2026, 20 seeds, the launcher at its setpoint: at 0.5, 7.3
     * weights tipped 6 of 20, 7.0 and 6.65 1 each; at 0.6, 7.3 tipped 13; at 0.8, 20; 6.3 and 6.0 never. (At the
     * ideal speed per shot, a flatter arc: at 1, 7.3 tipped 15, 7.0 none; at 2, 20 and 7; at 4, 6.3 tipped 6.)
     */
    @Test
    public void sweep() {
        String sw = System.getenv("BIOBUZZ_SIM_SWEEP");
        if (sw == null) return;
        double before = FieldSim.impulseScale;
        for (String v : sw.split(",")) {
            FieldSim.impulseScale = Double.parseDouble(v);
            System.out.println("SWEEP " + v + " 6.0: " + volley(0, 2, 4) + " | 6.3: " + volley(2, 0, 3) + " | 6.65: " + volley(1, 1, 4)
                    + " | 7.0: " + volley(0, 3, 4) + " | 7.3: " + volley(2, 0, 4));
        }
        FieldSim.impulseScale = before;
    }
}
