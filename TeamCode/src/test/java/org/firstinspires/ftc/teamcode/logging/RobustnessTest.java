package org.firstinspires.ftc.teamcode.logging;

import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Which conclusions survive the simulation's guesses. Runs each candidate (Autos on a robot
 * design) under a spread of physics: friction, bounce, shot spread and how fast the HIVE swings,
 * each from half to one and a half times the placeholder or more. A conclusion such as "a catcher
 * beats no catcher" is only worth building on if it holds in every one of them. Opt in, slow:
 *
 * <pre>
 * BIOBUZZ_ROBUSTNESS=1 ./gradlew :TeamCode:testDebugUnitTest --tests '*RobustnessTest*' -i
 * </pre>
 * Optional: BIOBUZZ_ROBUSTNESS_SEEDS (default 4), BIOBUZZ_ROBUSTNESS_ONLY (a substring of the
 * candidates to run).
 */
public class RobustnessTest {

    /** {friction, bounce, spread, swing}: the placeholders, then each pushed both ways. */
    static final double[][] PHYSICS = {
            {1, 1, 1, 1},
            {0.5, 1, 1, 1}, {2, 1, 1, 1},
            {1, 0.6, 1, 1}, {1, 1.5, 1, 1},
            {1, 1, 1.5, 1}, {1, 1, 2, 1},
            {1, 1, 1, 0.7}, {1, 1, 1, 1.4},
            {0.5, 1.5, 1.5, 1.4}, {2, 0.6, 1.5, 0.7}, {2, 1.5, 2, 1},
    };

    static Map<String, String[]> candidates() {
        Map<String, String[]> m = new LinkedHashMap<>();
        for (String design : new String[] {"spring hood", "two spring hoods", "spring hood, 24 in catcher",
                "two spring hoods, 24 in catcher"}) {
            m.put("duo-lz | " + design, new String[] {"DuoLzRightAuto,DuoLzLeftAuto@50", design});
            m.put("lean | " + design, new String[] {"LeanRightAuto,LeanLeftAuto@50", design});
            m.put("lean-opp | " + design, new String[] {"LeanOppRightAuto,LeanOppLeftAuto@50", design});
        }
        for (String design : new String[] {"clump catapult 72 deg, 24 in catcher"}) {
            m.put("duo-lz | " + design, new String[] {"DuoLzRightAuto,DuoLzLeftAuto@50", design});
            m.put("lean-opp | " + design, new String[] {"LeanOppRightAuto,LeanOppLeftAuto@50", design});
        }
        m.put("three-tip-adaptive | spring hood", new String[] {"ThreeTipAdaptiveAuto@50", "spring hood"});
        m.put("three-tip-adaptive | two spring hoods, 24 in catcher", new String[] {"ThreeTipAdaptiveAuto@50", "two spring hoods, 24 in catcher"});
        return m;
    }

    @Test
    public void robustness() throws Exception {
        if (System.getenv("BIOBUZZ_ROBUSTNESS") == null) return;
        String seedsEnv = System.getenv("BIOBUZZ_ROBUSTNESS_SEEDS");
        int seeds = seedsEnv == null ? 4 : Integer.parseInt(seedsEnv);
        String only = System.getenv("BIOBUZZ_ROBUSTNESS_ONLY");
        Map<String, String[]> cands = candidates();
        Map<String, double[]> points = new LinkedHashMap<>();
        Map<String, double[]> tips = new LinkedHashMap<>();
        File file = new File(TeamCodeDir.simLogs(), "robustness.wpilog");
        try {
            for (Map.Entry<String, String[]> c : cands.entrySet()) {
                if (only != null && !c.getKey().contains(only)) continue;
                double[] pts = new double[PHYSICS.length], tp = new double[PHYSICS.length];
                for (int v = 0; v < PHYSICS.length; v++) {
                    FieldSim.frictionScale = PHYSICS[v][0];
                    FieldSim.bounceScale = PHYSICS[v][1];
                    FieldSim.spreadScale = PHYSICS[v][2];
                    FieldSim.swingScale = PHYSICS[v][3];
                    for (long seed = 1; seed <= seeds; seed++) {
                        AutoSim.Result r = AutoStudyTest.run(c.getValue()[0], AutoStudyTest.designs().get(c.getValue()[1]), seed, file);
                        pts[v] += r.autoPoints() / (double) seeds;
                        tp[v] += r.autoTips() / (double) seeds;
                    }
                }
                points.put(c.getKey(), pts);
                tips.put(c.getKey(), tp);
                StringBuilder line = new StringBuilder(String.format(Locale.ROOT, "ROBUST %-52s", c.getKey()));
                double min = Double.MAX_VALUE, sum = 0;
                for (double p : pts) {
                    line.append(String.format(Locale.ROOT, "%5.0f", p));
                    min = Math.min(min, p);
                    sum += p;
                }
                line.append(String.format(Locale.ROOT, " | mean %.0f, worst %.0f", sum / pts.length, min));
                System.out.println(line);
            }
        } finally {
            FieldSim.frictionScale = 1;
            FieldSim.bounceScale = 1;
            FieldSim.spreadScale = 1;
            FieldSim.swingScale = 1;
        }
        System.out.println("ROBUST columns: physics {friction, bounce, spread, swing}:");
        for (double[] p : PHYSICS) System.out.printf(Locale.ROOT, "ROBUST   %s%n", java.util.Arrays.toString(p));
        // Pairwise: in how many physics does A beat B?
        List<String> names = new ArrayList<>(points.keySet());
        for (String a : names) {
            for (String b : names) {
                if (a.compareTo(b) >= 0) continue;
                int wins = 0, losses = 0;
                for (int v = 0; v < PHYSICS.length; v++) {
                    if (points.get(a)[v] > points.get(b)[v] + 0.5) wins++;
                    else if (points.get(b)[v] > points.get(a)[v] + 0.5) losses++;
                }
                System.out.printf(Locale.ROOT, "PAIR %-52s vs %-52s %2d-%2d%n", a, b, wins, losses);
            }
        }
    }
}
