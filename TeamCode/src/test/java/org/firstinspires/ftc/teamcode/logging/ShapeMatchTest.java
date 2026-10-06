package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * The spill shapes in whole simulated Autos (mentor, 5 Oct 2026): qual-right-v3 with its partner, on the
 * plain robot and on the shortlist's rigid V and large and small right hooks
 * ({@code tools/auto-routes/qual_shapes.py} draws each one's route). 20 runs each on normal tiles and on
 * tiles with 3x the friction, as the README's Qualifier table. Opt in:
 *
 * <pre>
 * BIOBUZZ_SHAPE_MATCHES=1 ./gradlew :TeamCode:testDebugUnitTest --tests '*ShapeMatchTest*' -i
 * </pre>
 * Prints a row a design and tiles, writes {@code build/sim-logs/shape-matches.csv}, and the best and the
 * median run on normal tiles of each design as {@code shape-match-<design>-best.wpilog} / {@code -typical}.
 * "TIP 2's spill on the blue half": of the pieces our CELL spills at TIP 2 (the one a hook waits for), those
 * on the other alliance's half 3 s after it starts ({@link AutoSim.Result#tipSpills}).
 */
public class ShapeMatchTest {

    static final String PARTNER = "PartnerPreloadsRightAuto";
    /** {card, Auto, robot design, file name}: sim-review/body-shapes-shortlist.png's numbers. */
    static final String[][] CASES = {
            {"1 · Plain", "QualRightV3Auto", "spring hood, full-width intake", "plain"},
            {"15 · Rigid V", "QualRightV3RigidVAuto", "spring hood, rigid V", "rigid-v"},
            {"13 · Large right hook", "QualRightV3LargeHookAuto", "spring hood, large right hook", "large-hook"},
            {"14 · Small right hook", "QualRightV3SmallHookAuto", "spring hood, small right hook", "small-hook"},
            // The same on the Flat Intake, the baseline robot since 5 Oct 2026 (was "option 3"), and its Auto,
            // qual-right-o3. The hooks are folded into the Ramp Hook (6 Oct 2026), simulated as the 8 in hook.
            {"1 · Flat Intake", "QualRightO3Auto", "flat intake", "o3-plain"},
            {"15 · Rigid V, Flat Intake", "QualRightO3RigidVAuto", "flat intake, rigid V", "o3-rigid-v"},
            {"14 · Ramp Hook, Flat Intake", "QualRightO3SmallHookAuto", "flat intake, ramp hook", "o3-small-hook"},
    };
    static final int RUNS = 20;

    @Test
    public void shapesInTheQualifierAuto() throws Exception {
        if (System.getenv("BIOBUZZ_SHAPE_MATCHES") == null) return;
        File dir = TeamCodeDir.simLogs();
        File scratch = new File(dir, "shape-match-runs");
        StringBuilder csv = new StringBuilder("# card,auto,design,friction,points,tip3Runs,parkedRuns,g409Runs,g409Pieces,"
                + "tip2Spilled,tip2BlueHalfPercent,heldAtTeleop,bestSeed,typicalSeed; ShapeMatchTest, " + RUNS + " runs each\n");
        for (double friction : new double[] {1, 3}) {
            FieldSim.frictionScale = friction;
            try {
                for (String[] c : CASES) {
                    RobotDesign design = AutoStudyTest.designs().get(c[2]);
                    RobotDesign partner = AutoStudyTest.designs().get("spring hood");
                    double points = 0, held = 0;
                    int tip3 = 0, parked = 0, g409Runs = 0, g409 = 0, spilled = 0, blue = 0;
                    double[][] bySeed = new double[RUNS][2];  // {points, seed}
                    for (int i = 0; i < RUNS; i++) {
                        long seed = i + 1;
                        AutoSim.Result r = AutoStudyTest.run(c[1] + "," + PARTNER + "@50", design, partner, 40, seed,
                                new File(scratch, c[3] + "-" + seed + ".wpilog"));
                        points += r.autoPoints();
                        bySeed[i] = new double[] {r.autoPoints(), seed};
                        if (r.autoTips() >= 3) tip3++;
                        if (r.robots.get(0).leave && r.robots.get(0).park) parked++;
                        if (r.g409 > 0) g409Runs++;
                        g409 += r.g409;
                        held += r.held;
                        if (r.tipSpills.size() >= 2) {
                            spilled += r.tipSpills.get(1)[0];
                            blue += r.tipSpills.get(1)[1];
                        }
                    }
                    // Best: the highest score; typical: the median, ties to the lower seed.
                    double[][] sorted = bySeed.clone();
                    Arrays.sort(sorted, (a, b) -> a[0] != b[0] ? Double.compare(b[0], a[0]) : Double.compare(a[1], b[1]));
                    long best = (long) sorted[0][1], typical = (long) sorted[RUNS / 2][1];
                    if (friction == 1) {
                        for (Object[] keep : new Object[][] {{best, "best"}, {typical, "typical"}}) {
                            Files.copy(new File(scratch, c[3] + "-" + keep[0] + ".wpilog").toPath(),
                                    new File(dir, "shape-match-" + c[3] + "-" + keep[1] + ".wpilog").toPath(),
                                    StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                    String row = String.format(Locale.ROOT, "\"%s\",%s,\"%s\",%.0f,%.1f,%d,%d,%d,%d,%d,%.0f,%.2f,%d,%d",
                            c[0], c[1], c[2], friction, points / RUNS, tip3, parked, g409Runs, g409, spilled,
                            spilled == 0 ? 0 : 100.0 * blue / spilled, held / RUNS, best, typical);
                    csv.append(row).append('\n');
                    System.out.printf(Locale.ROOT, "SHAPEMATCH %-22s tiles x%.0f: %.1f pts, TIP 3 %d/%d, PARK %d/%d, G409 in %d runs (%d pieces),"
                                    + " TIP 2's spill on the blue half %.0f%% of %d, held %.2f; best seed %d, typical %d%n",
                            c[0], friction, points / RUNS, tip3, RUNS, parked, RUNS, g409Runs, g409,
                            spilled == 0 ? 0 : 100.0 * blue / spilled, spilled, held / RUNS, best, typical);
                    assertTrue(c[0] + ": no TIP 2 spill counted", spilled > 0);
                }
            } finally {
                FieldSim.frictionScale = 1;
            }
        }
        Files.write(new File(dir, "shape-matches.csv").toPath(), csv.toString().getBytes(StandardCharsets.UTF_8));
        for (File f : scratch.listFiles()) f.delete();
        scratch.delete();
    }

    /** Every case names an Auto that exists and a design the simulator knows. */
    @Test
    public void everyCaseExists() throws Exception {
        List<String> missing = new ArrayList<>();
        for (String[] c : CASES) {
            Class.forName("org.firstinspires.ftc.teamcode.opmodes.auto.generated." + c[1]);
            if (!AutoStudyTest.designs().containsKey(c[2])) missing.add(c[2]);
        }
        assertTrue("unknown designs: " + missing, missing.isEmpty());
    }
}
