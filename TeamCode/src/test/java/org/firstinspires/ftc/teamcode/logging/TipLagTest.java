package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import org.firstinspires.ftc.teamcode.localization.StartPositions;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * The simulator's TIP lag against the measured one ({@code TeamCode/autos/timing-terms.md}): the real robot's volley
 * from RIGHT_START into a raised CELL, timed from the last ball leaving to the CELL settled (the rocker on its other
 * stop), with the simulator's own dwell ({@link FieldSim#tipDwellRange}) and hits. Belding's films, last ball to CELL
 * settled: four POLLEN on the three NECTAR a raised CELL starts with, and eight in an empty CELL, are at the CELL's
 * tipping weight, 3.4 to 6.5 s, median 3.9 (13 films); nine or more pieces are well past it, 2.2 to 2.5 s (3 films).
 * The robot's camera reads the CELL up 0.14 to 0.23 s after it settles, which is where "about 4.1 s" and "about
 * 2.5 s" come from.
 *
 * <p>Opt-in, like every simulator test but the smoke test: {@code BIOBUZZ_SIM_CHECKS=1}. About 20 s.
 */
public class TipLagTest {

    static final int SEEDS = 30;

    @Before
    public void optIn() {
        assumeTrue("set BIOBUZZ_SIM_CHECKS=1 to run the simulator's HIVE checks", "1".equals(System.getenv("BIOBUZZ_SIM_CHECKS")));
    }

    /** Seconds from the last ball leaving to the CELL settled, one per seed that tipped, sorted. */
    static List<Double> lags(int nectar, int pollen, int shots) {
        FieldSim.Physics physics = HiveCalibration.current().fit();
        List<Double> out = new ArrayList<>();
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
            if (sim.red.tips > 0) continue;
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
            double next = 0, last = Double.NaN;
            for (int k = 0; k < Math.round(15.0 / HiveCalibration.LOOP_S); k++) {
                sim.setRobot(x, y, heading, 0, 0, 0, false);
                double t = k * HiveCalibration.LOOP_S;
                if (!sim.main.stored.isEmpty() && t >= next && sim.red.raisedEnd() == end) {
                    sim.launch(aim);
                    last = t;
                    next = t + design.shotIntervalS;
                }
                sim.step(HiveCalibration.LOOP_S);
                if (sim.red.tips > 0) {
                    out.add(t + HiveCalibration.LOOP_S - last);
                    break;
                }
            }
        }
        Collections.sort(out);
        return out;
    }

    static double median(List<Double> xs) {
        int n = xs.size();
        return n % 2 == 1 ? xs.get(n / 2) : (xs.get(n / 2 - 1) + xs.get(n / 2)) / 2;
    }

    static String describe(String what, List<Double> xs) {
        return String.format(Locale.ROOT, "TipLagTest %s: %d of %d tipped; last ball to settled %.2f to %.2f s, median %.2f",
                what, xs.size(), SEEDS, xs.get(0), xs.get(xs.size() - 1), median(xs));
    }

    /** Four POLLEN on the three starting NECTAR: the preloads' TIP, at the CELL's weight. */
    @Test
    public void preloadsOnTheStartingNectarAreAtWeight() {
        List<Double> xs = lags(3, 0, 4);
        System.out.println(describe("3 NECTAR + 4 POLLEN", xs));
        assertTrue(describe("3 NECTAR + 4 POLLEN", xs), xs.size() >= SEEDS * 3 / 4);
        assertTrue(describe("3 NECTAR + 4 POLLEN", xs), median(xs) >= 3.4 && median(xs) <= 4.4);
        assertTrue(describe("3 NECTAR + 4 POLLEN", xs), xs.get(0) >= 2.8 && xs.get(xs.size() - 1) <= 7.0);
    }

    /** Eight POLLEN in an empty CELL (four placed, then a volley of four): at its weight too. */
    @Test
    public void eightInAnEmptyCellAreAtWeight() {
        List<Double> xs = lags(0, 4, 4);
        System.out.println(describe("4 POLLEN + 4 POLLEN", xs));
        assertTrue(describe("4 POLLEN + 4 POLLEN", xs), xs.size() >= SEEDS / 3);  // launched POLLEN rest nearer the axle: the 8th often holds
        assertTrue(describe("4 POLLEN + 4 POLLEN", xs), median(xs) >= 3.4 && median(xs) <= 4.8);
    }

    /** Six POLLEN on the three starting NECTAR, about three POLLEN past the weight: well past it. */
    @Test
    public void ninePiecesAreWellPast() {
        List<Double> xs = lags(3, 0, 6);
        System.out.println(describe("3 NECTAR + 6 POLLEN", xs));
        assertTrue(describe("3 NECTAR + 6 POLLEN", xs), xs.size() >= SEEDS * 3 / 4);
        assertTrue(describe("3 NECTAR + 6 POLLEN", xs), median(xs) >= 1.9 && median(xs) <= 2.9);
    }
}
