package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Our preloads staged in the hook while we wait for TIP 1 (mentor idea, 5 Oct 2026): qual-right-v3 against the
 * same Auto with the hook lowered at N_FIRE, the preloads pushed out into it ({@code Outtake}), the far FLOWER
 * fetched and fired at TIP 1, then the preloads picked up again and fired for TIP 2
 * ({@code tools/auto-routes/qual_stage.py} draws the routes). 20 runs each on normal tiles and on tiles with 3x
 * the friction, as {@link ShapeMatchTest}. Opt in:
 *
 * <pre>
 * BIOBUZZ_STAGED_MATCHES=1 ./gradlew :TeamCode:testDebugUnitTest --tests '*StagedPreloadsTest*' -i
 * </pre>
 * Prints a row a case and tiles and writes {@code build/sim-logs/staged-preloads.csv}, and the best and the
 * median run on normal tiles of each case as {@code staged-<case>-best.wpilog} / {@code -typical}.
 *
 * <p>What it follows of the staged pieces (FieldSim#outtaken): how far past the chassis' face they stopped and
 * how many lie in the 10 in deep, chassis-wide pocket in front of it (the large hook's) as the robot leaves
 * them; how far they moved from then to 1 s later (the hook lifting, the robot backing off); how many lay loose
 * when TIP 2 started and how far TIP 2's spill moved those in the next 3 s; and how many the robot took back.
 */
public class StagedPreloadsTest {

    static final String PARTNER = "PartnerPreloadsRightAuto";
    /** {label, Auto, robot design, file name, outtake speed in/s}. */
    static final String[][] CASES = {
            {"qual-right-v3 (plain)", "QualRightV3Auto", "spring hood, full-width intake", "v3-plain", "20"},
            {"qual-right-v3-large-hook", "QualRightV3LargeHookAuto", "spring hood, large right hook", "v3-large-hook", "20"},
            {"Staged in the hook at N_FIRE (first drawn)", "QualStageLargeHookAuto", "spring hood, large right hook", "stage-at-n-fire", "20"},
            {"Staged in the hook, from y 120", "QualStageBackLargeHookAuto", "spring hood, large right hook", "stage-back-large-hook", "20"},
            {"Staged in the hook, from y 120, quick", "QualStageBackLargeHookQuickAuto", "spring hood, large right hook", "stage-back-large-hook-quick", "20"},
            {"Staged from y 120, plain robot (no hook)", "QualStageBackPlainAuto", "spring hood, full-width intake", "stage-back-plain", "20"},
            // How much the answer hangs on the outtake's speed, which nobody has measured.
            {"Staged in the hook, from y 120, outtake 10 in/s", "QualStageBackLargeHookAuto", "spring hood, large right hook", "stage-back-large-hook-10", "10"},
            {"Staged in the hook, from y 120, outtake 40 in/s", "QualStageBackLargeHookAuto", "spring hood, large right hook", "stage-back-large-hook-40", "40"},
    };
    static final int RUNS = 20;
    /** The pocket in front of the chassis' face: as deep as the large hook's arm, as wide as the chassis. */
    static final double POCKET_DEEP_IN = 10;

    /** What happened to the staged pieces in one run. */
    static final class Staging {
        int staged;
        /** When the robot first moved off the spot it staged from (NaN: never staged). */
        double leftAt = Double.NaN;
        int inPocket;
        double gapSum;
        /** Their poses as it left, and 1 s later. */
        double[][] atLeave;
        double maxMoveAfterLeave;
        boolean measuredMove;
        int looseAtTip2 = -1;
        double[][] atTip2;
        double maxMoveBySpill;
        boolean measuredSpill;
        final Set<FieldSim.Piece> retaken = new HashSet<>();
        int looseAtEnd = -1;
    }

    /** Watches a run's field each loop ({@link AutoSim#observer}). */
    static final class Watcher {
        final Staging s = new Staging();
        double[] stagedFrom;
        double tip2At = Double.NaN;

        void accept(FieldSim sim, double now) {
            List<FieldSim.Piece> out = sim.outtaken;
            FieldSim.Bot bot = sim.main;
            double[] pose = bot.pose();
            for (FieldSim.Piece p : out) if (bot.stored.contains(p)) s.retaken.add(p);
            if (out.size() < FieldSim.ROBOT_CAPACITY) return;
            s.staged = out.size();
            if (stagedFrom == null) stagedFrom = pose;
            if (Double.isNaN(s.leftAt) && Math.hypot(pose[0] - stagedFrom[0], pose[1] - stagedFrom[1]) > 0.5) {
                s.leftAt = now;
                s.atLeave = positions(out);
                double c = Math.cos(stagedFrom[2]), sn = Math.sin(stagedFrom[2]);
                double face = bot.design.frameIn / 2, half = bot.design.frameWidthIn / 2;
                for (FieldSim.Piece p : out) {
                    if (p.where != FieldSim.Where.FIELD) continue;
                    double lx = (p.x - stagedFrom[0]) * c + (p.y - stagedFrom[1]) * sn;
                    double ly = -(p.x - stagedFrom[0]) * sn + (p.y - stagedFrom[1]) * c;
                    s.gapSum += lx - face - p.kind.radius;
                    if (lx > face && lx < face + POCKET_DEEP_IN && Math.abs(ly) < half) s.inPocket++;
                }
            }
            if (!s.measuredMove && !Double.isNaN(s.leftAt) && now >= s.leftAt + 1.0) {
                s.measuredMove = true;
                s.maxMoveAfterLeave = maxMove(out, s.atLeave);
            }
            FieldSim.Rocker ours = sim.rocker(Alliance.RED);
            if (s.looseAtTip2 < 0 && ours.tipsStarted >= 2) {
                tip2At = now;
                s.atTip2 = positions(out);
                s.looseAtTip2 = 0;
                for (FieldSim.Piece p : out) if (p.where == FieldSim.Where.FIELD && p.cell == null) s.looseAtTip2++;
            }
            if (!s.measuredSpill && !Double.isNaN(tip2At) && now >= tip2At + AutoSim.SPILL_LOOK_S) {
                s.measuredSpill = true;
                s.maxMoveBySpill = maxMove(out, s.atTip2);
            }
        }

        static double[][] positions(List<FieldSim.Piece> pieces) {
            double[][] at = new double[pieces.size()][];
            for (int i = 0; i < at.length; i++) {
                FieldSim.Piece p = pieces.get(i);
                at[i] = p.where == FieldSim.Where.FIELD && p.cell == null ? new double[] {p.x, p.y} : null;
            }
            return at;
        }

        /** The furthest any piece still loose on the tiles moved since {@code before}. */
        static double maxMove(List<FieldSim.Piece> pieces, double[][] before) {
            double max = 0;
            for (int i = 0; i < before.length; i++) {
                FieldSim.Piece p = pieces.get(i);
                if (before[i] == null || p.where != FieldSim.Where.FIELD || p.cell != null) continue;
                max = Math.max(max, Math.hypot(p.x - before[i][0], p.y - before[i][1]));
            }
            return max;
        }
    }

    /** Loose pieces on our half when AUTO ends: on the tiles, not in a CELL or a FLOWER. */
    static int looseOnOurHalf(FieldSim sim) {
        int n = 0;
        for (FieldSim.Piece p : sim.pieces) {
            if (p.where == FieldSim.Where.FIELD && p.cell == null && p.flower < 0 && p.x < FieldSim.CENTRE_IN) n++;
        }
        return n;
    }

    @Test
    public void stagedPreloadsInTheQualifierAuto() throws Exception {
        if (System.getenv("BIOBUZZ_STAGED_MATCHES") == null) return;
        File dir = TeamCodeDir.simLogs();
        File scratch = new File(dir, "staged-runs");
        StringBuilder csv = new StringBuilder("# case,auto,design,outtakeInPerS,friction,points,tip3Runs,parkedRuns,g409Runs,"
                + "looseAtEnd,tip1At,tip2At,leftStagingAt,staged,inPocket,gapIn,movedAfterLeaveIn,looseAtTip2,movedBySpillIn,"
                + "retaken,bestSeed,typicalSeed; StagedPreloadsTest, " + RUNS + " runs each\n");
        double outtake = AutoSim.placeholderOuttakeInPerS;
        for (double friction : new double[] {1}) {  // one tile surface (6 Oct 2026); 3: pieces stop 3x sooner, a what-if
            FieldSim.frictionScale = friction;
            try {
                for (String[] c : CASES) {
                    AutoSim.placeholderOuttakeInPerS = Double.parseDouble(c[4]);
                    RobotDesign design = AutoStudyTest.designs().get(c[2]);
                    RobotDesign partner = AutoStudyTest.designs().get("spring hood");
                    double points = 0, loose = 0, tip1 = 0, tip2 = 0, left = 0, gap = 0, moved = 0, spillMoved = 0;
                    int tip3 = 0, parked = 0, g409Runs = 0, tip1n = 0, tip2n = 0, staged = 0, pocket = 0, retaken = 0;
                    int looseAtTip2 = 0, stagedRuns = 0;
                    double[][] bySeed = new double[RUNS][2];
                    for (int i = 0; i < RUNS; i++) {
                        long seed = i + 1;
                        Watcher w = new Watcher();
                        int[] endLoose = {-1};
                        AutoSim sim = new AutoSim(Class.forName(AutoStudyTest.PKG + c[1]), Alliance.RED, seed)
                                .speed(50, 45).design(design);
                        sim.alsoRun(Class.forName(AutoStudyTest.PKG + PARTNER)).speed(40, 36).design(partner);
                        sim.metadata("Case", c[0] + " (StagedPreloadsTest), tiles x" + (int) friction
                                + ", outtake " + c[4] + " in/s");
                        sim.observer = (f, now) -> {
                            w.accept(f, now);
                            if (endLoose[0] < 0 && now >= org.firstinspires.ftc.teamcode.autokit.AutoKit.AUTO_LENGTH_S) {
                                endLoose[0] = looseOnOurHalf(f);
                            }
                        };
                        AutoSim.Result r = sim.write(new File(scratch, c[3] + "-" + seed + ".wpilog"));
                        points += r.autoPoints();
                        bySeed[i] = new double[] {r.autoPoints(), seed};
                        if (r.autoTips() >= 3) tip3++;
                        if (r.robots.get(0).leave && r.robots.get(0).park) parked++;
                        if (r.g409 > 0) g409Runs++;
                        loose += endLoose[0];
                        if (r.tipsAt.size() > 0) { tip1 += r.tipsAt.get(0); tip1n++; }
                        if (r.tipsAt.size() > 1) { tip2 += r.tipsAt.get(1); tip2n++; }
                        Staging s = w.s;
                        if (s.staged > 0) {
                            stagedRuns++;
                            staged += s.staged;
                            pocket += s.inPocket;
                            gap += s.gapSum;
                            left += s.leftAt;
                            moved += s.maxMoveAfterLeave;
                            retaken += s.retaken.size();
                            if (s.looseAtTip2 > 0) {
                                looseAtTip2 += s.looseAtTip2;
                                spillMoved += s.maxMoveBySpill;
                            }
                        }
                    }
                    double[][] sorted = bySeed.clone();
                    Arrays.sort(sorted, (a, b) -> a[0] != b[0] ? Double.compare(b[0], a[0]) : Double.compare(a[1], b[1]));
                    long best = (long) sorted[0][1], typical = (long) sorted[RUNS / 2][1];
                    if (friction == 1) {
                        for (Object[] keep : new Object[][] {{best, "best"}, {typical, "typical"}}) {
                            Files.copy(new File(scratch, c[3] + "-" + keep[0] + ".wpilog").toPath(),
                                    new File(dir, "staged-" + c[3] + "-" + keep[1] + ".wpilog").toPath(),
                                    StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                    int sr = Math.max(1, stagedRuns);
                    String row = String.format(Locale.ROOT,
                            "\"%s\",%s,\"%s\",%s,%.0f,%.1f,%d,%d,%d,%.1f,%.1f,%.1f,%.2f,%.1f,%.1f,%.1f,%.1f,%.2f,%.1f,%.2f,%d,%d",
                            c[0], c[1], c[2], c[4], friction, points / RUNS, tip3, parked, g409Runs, loose / RUNS,
                            tip1n == 0 ? Double.NaN : tip1 / tip1n, tip2n == 0 ? Double.NaN : tip2 / tip2n,
                            stagedRuns == 0 ? Double.NaN : left / sr, (double) staged / sr, (double) pocket / sr,
                            staged == 0 ? Double.NaN : gap / staged, stagedRuns == 0 ? Double.NaN : moved / sr,
                            (double) looseAtTip2 / sr, looseAtTip2 == 0 ? Double.NaN : spillMoved / sr,
                            (double) retaken / sr, best, typical);
                    csv.append(row).append('\n');
                    System.out.printf(Locale.ROOT, "STAGED %-38s tiles x%.0f: %.1f pts, TIP 3 %d/%d, PARK %d/%d, G409 in %d runs,"
                                    + " loose at 30 s %.1f, TIP 1 %.1f s, TIP 2 %.1f s (%d runs); staged %d runs: left %.2f s,"
                                    + " in pocket %.1f of %.1f, gap %.1f in, moved after %.1f in, loose at TIP 2 %.2f, retaken %.2f;"
                                    + " best seed %d, typical %d%n",
                            c[0], friction, points / RUNS, tip3, RUNS, parked, RUNS, g409Runs, loose / RUNS,
                            tip1n == 0 ? Double.NaN : tip1 / tip1n, tip2n == 0 ? Double.NaN : tip2 / tip2n, tip2n,
                            stagedRuns, stagedRuns == 0 ? Double.NaN : left / sr, (double) pocket / sr, (double) staged / sr,
                            staged == 0 ? Double.NaN : gap / staged, stagedRuns == 0 ? Double.NaN : moved / sr,
                            (double) looseAtTip2 / sr, (double) retaken / sr, best, typical);
                    if (c[1].startsWith("QualStage")) assertTrue(c[0] + ": never staged", stagedRuns == RUNS);
                }
            } finally {
                FieldSim.frictionScale = 1;
                AutoSim.placeholderOuttakeInPerS = outtake;
            }
        }
        Files.write(new File(dir, "staged-preloads.csv").toPath(), csv.toString().getBytes(StandardCharsets.UTF_8));
        for (File f : scratch.listFiles()) f.delete();
        scratch.delete();
    }

    /** Every case names an Auto that exists and a design the simulator knows. */
    @Test
    public void everyCaseExists() throws Exception {
        List<String> missing = new ArrayList<>();
        for (String[] c : CASES) {
            Class.forName(AutoStudyTest.PKG + c[1]);
            if (!AutoStudyTest.designs().containsKey(c[2])) missing.add(c[2]);
        }
        assertTrue("unknown designs: " + missing, missing.isEmpty());
    }

    /**
     * An Outtake into the lowered large hook on normal tiles: all four pieces stop inside the pocket, clear of
     * the face. The simulator's answer to "do they stay in the hook", so a change that breaks it shows.
     */
    @Test
    public void outtakeLandsInsideTheHook() throws Exception {
        Watcher w = new Watcher();
        AutoSim sim = new AutoSim(Class.forName(AutoStudyTest.PKG + "QualStageBackLargeHookAuto"), Alliance.RED, 1)
                .speed(50, 45).design(AutoStudyTest.designs().get("spring hood, large right hook"));
        sim.alsoRun(Class.forName(AutoStudyTest.PKG + PARTNER)).speed(40, 36).design(AutoStudyTest.designs().get("spring hood"));
        sim.observer = w::accept;
        sim.write(new File(TeamCodeDir.simLogs(), "staged-check.wpilog"));
        assertTrue("staged " + w.s.staged, w.s.staged == 4);
        assertTrue("in the pocket: " + w.s.inPocket, w.s.inPocket == 4);
        assertTrue("taken back: " + w.s.retaken.size(), w.s.retaken.size() >= 3);
    }
}
