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
 * Prints a map per design and per raised CELL, then how often both kinds score by angle off the
 * CELL's axis (rows) and distance (columns), and writes build/sim-logs/shot-map.csv.
 * BIOBUZZ_SHOT_SCATTER multiplies the shot-to-shot spread (default 1; try 2 for a worse launcher).
 */
public class ShotMapTest {

    static final int SHOTS = 6;
    static final double STEP = 4;

    @Test
    public void map() throws Exception {
        if (System.getenv("BIOBUZZ_SHOT_MAP") == null) return;
        RobotDesign[] designs = {RobotDesign.standard(), RobotDesign.springHood()};
        String scatter = System.getenv("BIOBUZZ_SHOT_SCATTER");
        FieldSim.spreadScale = scatter == null ? 1 : Double.parseDouble(scatter);
        try {
            map(designs);
        } finally {
            FieldSim.spreadScale = 1;
        }
    }

    void map(RobotDesign[] designs) throws Exception {
        List<double[]> spots = new ArrayList<>();
        for (double y = 9; y <= 135; y += STEP) {
            for (double x = 9; x <= 62; x += STEP) spots.add(new double[] {x, y});
        }
        File csv = new File(TeamCodeDir.simLogs(), FieldSim.spreadScale == 1 ? "shot-map.csv"
                : String.format(Locale.ROOT, "shot-map-scatter-%.1f.csv", FieldSim.spreadScale));
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
                    byAngle(spots, rate, left);
                    for (int i = 0; i < spots.size(); i++) {
                        if (rate[i] == null) continue;
                        out.printf(Locale.ROOT, "%s,%s,%.0f,%.0f,%.2f,%.2f%n", design.name, left ? "left" : "right",
                                spots.get(i)[0], spots.get(i)[1], rate[i][0], rate[i][1]);
                    }
                }
            }
        }
    }

    static final double[] ANGLES = {0, 10, 20, 30, 40, 50, 90}, DISTANCES = {20, 32, 44, 56, 80};

    /** Mean rate at which both kinds score, by degrees off the CELL's axis and distance to its centre. */
    static void byAngle(List<double[]> spots, double[][] rate, boolean left) {
        System.out.printf(Locale.ROOT, "ANGLE scatter x%.1f: both-score rate by angle off axis (rows) and distance in (columns)%n", FieldSim.spreadScale);
        StringBuilder head = new StringBuilder("ANGLE   deg   ");
        for (int d = 0; d + 1 < DISTANCES.length; d++) head.append(String.format(Locale.ROOT, "  %2.0f-%-2.0f", DISTANCES[d], DISTANCES[d + 1]));
        System.out.println(head);
        double cy = FieldSim.CENTRE_IN + (left ? 20 : -20);
        for (int a = 0; a + 1 < ANGLES.length; a++) {
            StringBuilder line = new StringBuilder(String.format(Locale.ROOT, "ANGLE %3.0f-%-3.0f ", ANGLES[a], ANGLES[a + 1]));
            for (int d = 0; d + 1 < DISTANCES.length; d++) {
                double sum = 0;
                int n = 0;
                for (int i = 0; i < spots.size(); i++) {
                    if (rate[i] == null) continue;
                    double dx = FieldSim.RED_HIVE_X_IN - spots.get(i)[0], dy = cy - spots.get(i)[1];
                    double off = Math.toDegrees(Math.atan2(Math.abs(dy), dx)), dist = Math.hypot(dx, dy);
                    if (off < ANGLES[a] || off >= ANGLES[a + 1] || dist < DISTANCES[d] || dist >= DISTANCES[d + 1]) continue;
                    sum += Math.min(rate[i][0], rate[i][1]);
                    n++;
                }
                line.append(n == 0 ? "      - " : String.format(Locale.ROOT, "  %3.0f%%  ", 100 * sum / n));
            }
            System.out.println(line);
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
