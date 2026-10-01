package org.firstinspires.ftc.teamcode.logging;

import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The evidence for choosing a robot: every candidate design, in each Auto it would run, with a
 * partner as good as us and with a typical one, 20 seeds each. Writes one JSON line per run to
 * {@code build/sim-logs/decision/runs.jsonl} for charts, and prints a summary. Opt in:
 *
 * <pre>
 * BIOBUZZ_DECISION=1 ./gradlew :TeamCode:testDebugUnitTest --tests '*DecisionStudyTest*' -i
 * </pre>
 *
 * Optional: BIOBUZZ_DECISION_SEEDS (default 20), BIOBUZZ_DECISION_ONLY (a substring of a design).
 */
public class DecisionStudyTest {

    static Map<String, RobotDesign> candidates() {
        Map<String, RobotDesign> all = AutoStudyTest.designs(), m = new LinkedHashMap<>();
        m.put("one spring hood", all.get("spring hood"));
        m.put("one spring hood + catcher", all.get("spring hood, 24 in catcher"));
        m.put("two spring hoods + catcher", all.get("two spring hoods, 24 in catcher"));
        RobotDesign cat = all.get("clump catapult 72 deg, 24 in catcher");
        for (double residual : new double[] {0.3, 0.6, 1.0}) {
            RobotDesign c = cat.copy("catapult + catcher, clump " + residual);
            c.catapultResidual = residual;
            m.put(c.name, c);
        }
        return m;
    }

    /** Auto name, spec, and whether it has a partner. */
    static final String[][] AUTOS = {
            {"duo-lz", "DuoLzRightAuto,DuoLzLeftAuto@50"},
            {"lean-opp", "LeanOppRightAuto,LeanOppLeftAuto@50"},
            {"three-tip-adaptive (alone)", "ThreeTipAdaptiveAuto@50"},
    };

    @Test
    public void decide() throws Exception {
        if (System.getenv("BIOBUZZ_DECISION") == null) return;
        String seedsEnv = System.getenv("BIOBUZZ_DECISION_SEEDS"), only = System.getenv("BIOBUZZ_DECISION_ONLY");
        int seeds = seedsEnv == null ? 20 : Integer.parseInt(seedsEnv);
        RobotDesign typical = AutoStudyTest.designs().get("spring hood");
        File dir = new File(TeamCodeDir.simLogs(), "decision");
        dir.mkdirs();
        File scratch = new File(dir, "scratch.wpilog");
        try (Writer out = new FileWriter(new File(dir, only == null ? "runs.jsonl" : "runs-" + only.replaceAll("[^A-Za-z0-9]+", "-") + ".jsonl"))) {
            for (Map.Entry<String, RobotDesign> c : candidates().entrySet()) {
                if (only != null && !c.getKey().contains(only)) continue;
                for (String[] auto : AUTOS) {
                    boolean duo = auto[1].contains(",");
                    for (String partner : duo ? new String[] {"same as ours", "typical"} : new String[] {"none"}) {
                        List<Double> pts = new ArrayList<>();
                        int fourTips = 0, parked = 0;
                        for (long seed = 1; seed <= seeds; seed++) {
                            AutoSim.Result r = partner.equals("typical")
                                    ? AutoStudyTest.run(auto[1], c.getValue(), typical, 40, seed, scratch)
                                    : AutoStudyTest.run(auto[1], c.getValue(), null, Double.NaN, seed, scratch);
                            boolean allParked = r.robots.stream().allMatch(x -> x.leave && x.park);
                            pts.add((double) r.autoPoints());
                            if (r.autoTips() >= 4) fourTips++;
                            if (allParked) parked++;
                            StringBuilder tips = new StringBuilder();
                            for (double t : r.tipsAt) tips.append(tips.length() == 0 ? "" : ",").append(String.format(Locale.ROOT, "%.2f", t));
                            out.write(String.format(Locale.ROOT,
                                    "{\"design\":\"%s\",\"auto\":\"%s\",\"partner\":\"%s\",\"seed\":%d,\"points\":%d,\"autoTips\":%d,"
                                            + "\"tipsAt\":[%s],\"parked\":%b,\"cellLoad\":%.3f,\"held\":%d}%n",
                                    c.getKey(), auto[0], partner, seed, r.autoPoints(), r.autoTips(), tips, allParked, r.cellLoad, r.held));
                        }
                        out.flush();
                        java.util.Collections.sort(pts);
                        double mean = pts.stream().mapToDouble(Double::doubleValue).average().orElse(0);
                        double sd = Math.sqrt(pts.stream().mapToDouble(p -> (p - mean) * (p - mean)).sum() / Math.max(1, pts.size() - 1));
                        System.out.printf(Locale.ROOT, "DECIDE %-32s %-28s %-13s mean %5.1f ± %4.1f (95%% CI) | p10 %3.0f p50 %3.0f p90 %3.0f | 4+ TIPs %2d/%d | parked %2d/%d%n",
                                c.getKey(), auto[0], partner, mean, 1.96 * sd / Math.sqrt(pts.size()),
                                pts.get(pts.size() / 10), pts.get(pts.size() / 2), pts.get(Math.min(pts.size() - 1, 9 * pts.size() / 10)),
                                fourTips, seeds, parked, seeds);
                    }
                }
            }
        }
        scratch.delete();
    }
}
