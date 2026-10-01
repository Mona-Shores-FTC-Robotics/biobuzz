package org.firstinspires.ftc.teamcode.logging;

import org.junit.Test;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

/**
 * Where on our half of the field a launcher scores from: for each spot, a few POLLEN and NECTAR
 * shots at the raised CELL with the usual shot-to-shot spread, robot facing the CELL. Spots where
 * an 18 in robot would touch a wall, the HIVE frame or the centre line are left out. Opt in:
 *
 * <pre>
 * BIOBUZZ_SHOT_MAP=1 ./gradlew :TeamCode:testDebugUnitTest --tests '*ShotMapTest*' -i
 * </pre>
 * Prints a map per design and per raised CELL, and writes build/sim-logs/shot-map.csv.
 */
public class ShotMapTest {

    static final int SHOTS = 6;
    static final double STEP = 4;

    @Test
    public void map() throws Exception {
        if (System.getenv("BIOBUZZ_SHOT_MAP") == null) return;
        RobotDesign[] designs = {RobotDesign.standard(), RobotDesign.springHood()};
        List<double[]> spots = new ArrayList<>();
        for (double y = 9; y <= 135; y += STEP) {
            for (double x = 9; x <= 62; x += STEP) spots.add(new double[] {x, y});
        }
        File csv = new File(TeamCodeDir.simLogs(), "shot-map.csv");
        csv.getParentFile().mkdirs();
        try (PrintWriter out = new PrintWriter(csv)) {
            out.println("design,raised,x,y,pollen,nectar");
            for (RobotDesign design : designs) {
                for (boolean left : new boolean[] {false, true}) {
                    double[][] rate = new double[spots.size()][];
                    IntStream.range(0, spots.size()).parallel().forEach(i -> rate[i] = rates(design, left, spots.get(i)));
                    System.out.printf(Locale.ROOT, "MAP %s, %s CELL raised (P = both score 5/6+, p = POLLEN only, n = NECTAR only, . = neither, blank = robot can't stand there)%n",
                            design.name, left ? "left" : "right");
                    int good = 0, all = 0;
                    for (int row = 0; ; row++) {
                        double y = 135 - (135 - 9) % STEP - row * STEP;
                        if (y < 9) break;
                        StringBuilder line = new StringBuilder(String.format(Locale.ROOT, "MAP %5.0f ", y));
                        for (int i = 0; i < spots.size(); i++) {
                            if (spots.get(i)[1] != y) continue;
                            double[] r = rate[i];
                            if (r == null) { line.append(' '); continue; }
                            all++;
                            boolean p = r[0] >= 5.0 / 6, n = r[1] >= 5.0 / 6;
                            if (p && n) good++;
                            line.append(p && n ? 'P' : p ? 'p' : n ? 'n' : '.');
                        }
                        System.out.println(line);
                    }
                    System.out.printf(Locale.ROOT, "MAP   x: 9 to 61 every %.0f in; both score from %d of %d spots%n", STEP, good, all);
                    for (int i = 0; i < spots.size(); i++) {
                        if (rate[i] == null) continue;
                        out.printf(Locale.ROOT, "%s,%s,%.0f,%.0f,%.2f,%.2f%n", design.name, left ? "left" : "right",
                                spots.get(i)[0], spots.get(i)[1], rate[i][0], rate[i][1]);
                    }
                }
            }
        }
    }

    /** Fraction of POLLEN and of NECTAR that score from {@code spot}, or null if a robot can't stand there. */
    static double[] rates(RobotDesign design, boolean left, double[] spot) {
        try {
            double heading = Math.atan2(FieldSim.CENTRE_IN + (left ? 20 : -20) - spot[1], FieldSim.RED_HIVE_X_IN - spot[0]);
            if (!standable(spot, heading)) return null;
            double[] out = new double[2];
            for (int k = 0; k < 2; k++) {
                int in = 0;
                for (int s = 0; s < SHOTS; s++) {
                    if (shot(design, left, spot, k == 0 ? FieldSim.Kind.POLLEN : FieldSim.Kind.RED_NECTAR, 1000L * s + k)) in++;
                }
                out[k] = in / (double) SHOTS;
            }
            return out;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static boolean standable(double[] c, double heading) {
        double h = 9, cs = Math.cos(heading), sn = Math.sin(heading);
        for (double[] k : new double[][] {{h, h}, {h, -h}, {-h, h}, {-h, -h}}) {
            double x = c[0] + k[0] * cs - k[1] * sn, y = c[1] + k[0] * sn + k[1] * cs;
            if (x < 0.5 || y < 0.5 || y > FieldSim.FIELD_SIZE_IN - 0.5 || x > FieldSim.CENTRE_IN) return false;
            if (FieldSim.inHiveFrame(x, y)) return false;
        }
        return !FieldSim.inHiveFrame(c[0], c[1]);
    }

    static boolean shot(RobotDesign design, boolean left, double[] spot, FieldSim.Kind kind, long seed) throws Exception {
        FieldSim sim = new FieldSim(HiveAssets.committedStagedPieces(), seed);
        FieldSim.Rocker r = sim.red;
        r.locked = true;
        if (left) r.angle = FieldSim.TILT_RAD;
        double[] aim = r.aimPoint();
        double heading = Math.atan2(aim[1] - spot[1], aim[0] - spot[0]);
        sim.main.design = design;
        sim.setRobot(spot[0], spot[1], heading, 0, 0, 0, false);
        FieldSim.Piece p = new FieldSim.Piece(kind, FieldSim.Where.ROBOT, spot[0], spot[1], 10);
        sim.pieces.add(p);
        sim.main.stored.clear();
        sim.main.stored.add(p);
        if (sim.launch(aim) == null) return false;
        for (int i = 0; i < 120; i++) sim.step(0.02);
        return p.cell != null;
    }
}
