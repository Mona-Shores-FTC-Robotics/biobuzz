package org.firstinspires.ftc.teamcode.logging;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Every Auto pair, on every robot asked for, measured for the qualifier goal (mentor, 6 Oct 2026): 3 TIPs, both
 * robots LEAVE and PARK, no G409, and after each TIP our robot picks up 4 of its spill to fire at the other CELL.
 * {@link DeepDiveTest} runs it (opt in); each case is one line,
 * {@code label|our Auto|partner Auto|our design|partner design ("same": ours)|partner speed (NaN: ours)}.
 *
 * <p>Writes, under {@code build/sim-logs/deep-dive/}: {@code cases.csv} (one row a case and tiles),
 * {@code runs.csv} (one row a run, every TIP's numbers) and, normal tiles only, {@code logs/<case>-best.wpilog}
 * and {@code -typical.wpilog}.
 */
final class DeepDive {

    static final int RUNS = Integer.getInteger("deepdive.runs", 20);

    private DeepDive() {
    }

    /** Where it writes: {@code build/sim-logs/deep-dive}, or {@code deep-dive-<part>} (-Ddeepdive.part) for one of several runs at once. */
    static File dir() {
        String part = System.getProperty("deepdive.part", "");
        File d = new File(TeamCodeDir.simLogs(), part.isEmpty() ? "deep-dive" : "deep-dive-" + part);
        new File(d, "logs").mkdirs();
        return d;
    }

    static synchronized void append(File f, String header, String line) throws IOException {
        if (!f.exists()) Files.write(f.toPath(), (header + "\n").getBytes(StandardCharsets.UTF_8));
        Files.write(f.toPath(), (line + "\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
    }

    static String q(String s) {
        return "\"" + s.replace("\"", "'") + "\"";
    }

    /** Runs one case on the given tiles, 20 seeds, and writes its rows (and, on normal tiles, its logs). */
    static void runCase(String spec, double friction) throws Exception {
        String[] c = spec.split("\\|");
        String label = c[0], ours = c[1], partner = c[2];
        RobotDesign design = AutoStudyTest.designs().get(c[3]);
        if (design == null) throw new IllegalArgumentException("no design " + c[3]);
        RobotDesign partnerDesign = c[4].equals("same") ? design : AutoStudyTest.designs().get(c[4]);
        double partnerSpeed = Double.parseDouble(c[5]);
        String slug = (label + "-" + c[3]).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("-+$", "");
        File dir = dir(), scratch = new File(dir, "scratch-" + slug);
        scratch.mkdirs();
        FieldSim.frictionScale = friction;
        double points = 0, tips = 0, held = 0;
        int three = 0, perfect = 0, g409Runs = 0, problemRuns = 0, usLeavePark = 0, partnerLeavePark = 0, bothLeavePark = 0, finished = 0;
        int[] tipRuns = new int[4], kept4 = new int[4];
        double[] keptUs = new double[4], keptPartner = new double[4], toFourth = new double[4], tipAt = new double[4];
        int[] fourthN = new int[4];
        double[][] bySeed = new double[RUNS][];
        try {
            for (int i = 0; i < RUNS; i++) {
                long seed = i + 1;
                AutoSim.Result r = AutoStudyTest.run(ours + "," + partner + "@50", design, partnerDesign, partnerSpeed, seed,
                        new File(scratch, seed + ".wpilog"));
                int pts = r.autoPoints();
                points += pts;
                tips += r.autoTips();
                held += r.held;
                if (r.autoTips() >= 3) three++;
                if (pts >= 76) perfect++;
                if (r.g409 > 0) g409Runs++;
                AutoSim.RobotResult a = r.robots.get(0), b = r.robots.get(1);
                boolean aOk = a.leave && a.park, bOk = b.leave && b.park;
                if (aOk) usLeavePark++;
                if (bOk) partnerLeavePark++;
                if (aOk && bOk) bothLeavePark++;
                if (a.finished) finished++;
                // A problem disqualifies a route (claude/simulator, 6 Oct 2026): an illegal start, the centre line
                // crossed, the HIVE or a FLOWER hit, or the robots colliding. As the study line's PROBLEMS.
                String problem = problem(r);
                if (problem != null) problemRuns++;
                StringBuilder tipCols = new StringBuilder();
                for (int t = 0; t < 4; t++) {
                    if (t < r.tipKeeps.size()) {
                        double[] k = r.tipKeeps.get(t);
                        tipRuns[t]++;
                        tipAt[t] += k[0];
                        keptUs[t] += k[2];
                        keptPartner[t] += k[3];
                        if (k[2] >= 4) kept4[t]++;
                        if (!Double.isNaN(k[4])) {
                            toFourth[t] += k[4];
                            fourthN[t]++;
                        }
                        tipCols.append(String.format(Locale.ROOT, ",%.2f,%.0f,%.0f,%.0f,%s", k[0], k[1], k[2], k[3],
                                Double.isNaN(k[4]) ? "" : String.format(Locale.ROOT, "%.2f", k[4])));
                    } else {
                        tipCols.append(",,,,,");
                    }
                }
                bySeed[i] = new double[] {pts, seed};
                append(new File(dir, "runs.csv"), "label,design,friction,seed,points,tips,g409,usLeave,usPark,partnerLeave,partnerPark,held"
                                + ",tip1At,tip1InCell,tip1KeptUs,tip1KeptPartner,tip1FourthAfter,tip2At,tip2InCell,tip2KeptUs,tip2KeptPartner,tip2FourthAfter"
                                + ",tip3At,tip3InCell,tip3KeptUs,tip3KeptPartner,tip3FourthAfter,tip4At,tip4InCell,tip4KeptUs,tip4KeptPartner,tip4FourthAfter,problem",
                        String.format(Locale.ROOT, "%s,%s,%.0f,%d,%d,%d,%d,%b,%b,%b,%b,%d%s,%s", q(label), q(c[3]), friction, seed, pts,
                                r.autoTips(), r.g409, a.leave, a.park, b.leave, b.park, r.held, tipCols, problem == null ? "" : q(problem)));
            }
        } finally {
            FieldSim.frictionScale = 1;
        }
        double[][] sorted = bySeed.clone();
        Arrays.sort(sorted, (x, y) -> x[0] != y[0] ? Double.compare(y[0], x[0]) : Double.compare(x[1], y[1]));
        long best = (long) sorted[0][1], typical = (long) sorted[RUNS / 2][1];
        if (friction == 1) {
            for (Object[] keep : new Object[][] {{best, "best"}, {typical, "typical"}}) {
                Files.copy(new File(scratch, keep[0] + ".wpilog").toPath(),
                        new File(dir, "logs/" + slug + "-" + keep[1] + ".wpilog").toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
        for (File f : scratch.listFiles()) f.delete();
        scratch.delete();
        StringBuilder tipCols = new StringBuilder();
        for (int t = 0; t < 3; t++) {
            int n = tipRuns[t];
            tipCols.append(String.format(Locale.ROOT, ",%d,%s,%s,%s,%d,%s", n,
                    n == 0 ? "" : String.format(Locale.ROOT, "%.1f", tipAt[t] / n),
                    n == 0 ? "" : String.format(Locale.ROOT, "%.2f", keptUs[t] / n),
                    n == 0 ? "" : String.format(Locale.ROOT, "%.2f", keptPartner[t] / n), kept4[t],
                    fourthN[t] == 0 ? "" : String.format(Locale.ROOT, "%.1f", toFourth[t] / fourthN[t])));
        }
        String row = String.format(Locale.ROOT, "%s,%s,%s,%s,%.0f,%.1f,%.2f,%d,%d,%d,%d,%d,%d,%.2f,%d,%d,%d%s,%d",
                q(label), ours, partner, q(c[3]), friction, points / RUNS, tips / RUNS, three, perfect, usLeavePark,
                partnerLeavePark, bothLeavePark, g409Runs, held / RUNS, finished, best, typical, tipCols, problemRuns);
        append(new File(dir, "cases.csv"), "label,auto,partner,design,friction,points,tips,threeTipRuns,perfectRuns,usLeavePark,"
                + "partnerLeavePark,bothLeavePark,g409Runs,heldAtTeleop,usFinished,bestSeed,typicalSeed"
                + ",tip1Runs,tip1At,tip1KeptUs,tip1KeptPartner,tip1Kept4Runs,tip1FourthAfter"
                + ",tip2Runs,tip2At,tip2KeptUs,tip2KeptPartner,tip2Kept4Runs,tip2FourthAfter"
                + ",tip3Runs,tip3At,tip3KeptUs,tip3KeptPartner,tip3Kept4Runs,tip3FourthAfter,problemRuns", row);
        System.out.println("DEEPDIVE " + row);
    }

    /** The run's first problem (AutoStudyTest's PROBLEMS), or null. */
    static String problem(AutoSim.Result r) {
        for (AutoSim.RobotResult robot : r.robots) {
            if (robot.illegalStart != null || !Double.isNaN(robot.crossedAt) || !Double.isNaN(robot.hitHiveAt)
                    || !Double.isNaN(robot.hitFlowerAt)) {
                return robot.toString();
            }
        }
        return Double.isNaN(r.robotsCollidedAt) ? null : String.format(Locale.ROOT, "robots collide at %.1f s", r.robotsCollidedAt);
    }

    /** {@code args}: frictions ("1,3"), then case specs. */
    public static void main(String[] args) throws Exception {
        // As AutoStudyTest: BIOBUZZ_AUTO_SPREAD scales the launcher's shot-to-shot spread (1 = the placeholder, 0 = none).
        String spread = System.getenv("BIOBUZZ_AUTO_SPREAD");
        FieldSim.spreadScale = spread == null ? 1 : Double.parseDouble(spread);
        String spin = System.getenv("BIOBUZZ_AUTO_SPIN");
        if (spin != null) FieldSim.launchSpin = Double.parseDouble(spin);
        List<Double> frictions = new ArrayList<>();
        for (String f : args[0].split(",")) frictions.add(Double.parseDouble(f));
        for (int i = 1; i < args.length; i++) {
            for (double f : frictions) runCase(args[i], f);
        }
    }
}
