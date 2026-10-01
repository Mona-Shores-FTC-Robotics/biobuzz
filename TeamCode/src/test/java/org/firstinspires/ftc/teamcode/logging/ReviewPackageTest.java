package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Builds a review set: a handful of simulated Autos, each in its own folder with its {@code .wpilog}
 * and the Auto Builder {@code .pp} files that made it, plus a README listing what to watch. This is
 * what goes back to a mentor or a student, however many runs it took to choose these. Opt in:
 *
 * <pre>
 * BIOBUZZ_REVIEW=2026-10-01b ./gradlew :TeamCode:testDebugUnitTest --tests '*ReviewPackageTest*' -i
 * </pre>
 * Writes {@code sim-review/<name>/} at the repository root. Every log is checked against
 * {@code sim-review/advantagescope-layout.json}, so Import Layout always finds its keys.
 * To change what is in the set, edit {@link #RUNS}.
 */
public class ReviewPackageTest {

    /** One run in the set: the folder name, what to watch, and how to run it. */
    static final class Run {
        final String folder, watch, spec, design, partnerDesign;
        final double partnerSpeed;
        final long seed;

        Run(String folder, String watch, String spec, String design, String partnerDesign, double partnerSpeed,
            long seed) {
            this.folder = folder;
            this.watch = watch;
            this.spec = spec;
            this.design = design;
            this.partnerDesign = partnerDesign;
            this.partnerSpeed = partnerSpeed;
            this.seed = seed;
        }
    }

    static final String CATAPULT = "clump catapult 72 deg, 24 in catcher";
    static final String TWIN = "two spring hoods, 24 in catcher";

    static final String TRIANGLE = "clump catapult 72 deg, triangle cup";

    static final String TWIN_BACK = TWIN + ", intake at back", TWIN_TURRET = TWIN + ", turret";

    /**
     * The sections of the set: the top folder of each run, and its heading in the README. What the
     * partner does decides the section (mentor review: keep the three kinds of match apart).
     */
    static final String[][] SECTIONS = {
            {"1-partner-stages-preloads", "The partner doesn't shoot: it stages its preloads for us",
                    "A partner that can't fire sets its 4 preloads on the tiles touching it (G304) for us to collect, then drives to park. The orange outline in front of our robot is where its intake catches pieces (24 in wide with the catcher)."},
            {"2-partner-fires-preloads", "The partner fires one volley of preloads, then parks",
                    "The most common partner in qualifications. `partner-starts-left` fires into the left CELL once our TIP 1 raises it; `partner-starts-right` fires at the start (TIP 1 is theirs), or holds its volley for later."},
            {"3-choreography", "Choreography: two robots that both run our Autos",
                    "Playoffs with a capable partner, or our two sister robots."},
    };

    /**
     * The set, by section. Folder names say left/right as the drivers see the field. Each seed is a
     * typical run for its Auto and design (the median of 10, from {@code BIOBUZZ_AUTO_PER_SEED}), unless
     * the line says otherwise.
     */
    static final Run[] RUNS = {
            // 1. The partner doesn't shoot: it stages its preloads for us and parks.
            new Run("1-partner-stages-preloads/staged-three-tip",
                    "The partner sets its 4 preloads in a row at its side and drives straight to park. We fire ours (TIP 1), go through the tunnel, pick up the row with the webcam, fire, then the far FLOWER for TIP 2; the GARDEN for TIP 3; park. 3 TIPs in 17 of 20 seeds.",
                    "StagedThreeTipAuto,PartnerLeaveParkAuto@50", TWIN, "spring hood", 40, 3),
            new Run("1-partner-stages-preloads/solo-tunnel",
                    "The same partner with solo-tunnel: catches the TIP 1 spill, drives up the row intake first, fires, TIP 2 at about 17 s, and parks from the left through a tight gap. No time for TIP 3.",
                    "SoloTunnelAuto,PartnerLeaveParkAuto@50", CATAPULT, "spring hood", 40, 2),
            // 2. The partner fires one volley.
            new Run("2-partner-fires-preloads/partner-starts-left/solo-tunnel",
                    "solo-tunnel: fires all 4, catches each spill, drives under the HIVE both ways, the GARDEN for TIP 3, parks.",
                    "SoloTunnelAuto,PartnerPreloadsParkAuto@50", TWIN, "spring hood", 40, 2),
            new Run("2-partner-fires-preloads/partner-starts-left/solo-tunnel-catapult",
                    "solo-tunnel with the triangle-cup catapult: the fastest 3 TIPs (2, 9, 22 s) and both park.",
                    "SoloTunnelAuto,PartnerPreloadsParkAuto@50", TRIANGLE, "spring hood", 40, 1),
            new Run("2-partner-fires-preloads/partner-starts-left/three-tip-adaptive",
                    "three-tip-adaptive (the legacy reference: FLOWERs, round the outside, angled shots): fires all 4 and leaves at once; 3 TIPs and park is 76, its ceiling whatever the partner does.",
                    "ThreeTipAdaptiveAuto,PartnerPreloadsParkAuto@50", TWIN, "spring hood", 40, 2),
            new Run("2-partner-fires-preloads/partner-starts-right/flower-feed",
                    "Fire and feed: the partner makes TIP 1; we wait at the far FLOWER, lined up angled with the intake at the back, and fire our 4 while the FLOWER's 4 feed in behind them. TIP 2 at 8 s, never more than 4 held.",
                    "FlowerFeedBackAuto,PartnerPreloadsRightAuto@50", TWIN_BACK, "spring hood", 40, 2),
            new Run("2-partner-fires-preloads/partner-starts-right/left-tunnel-catapult",
                    "The partner makes TIP 1; we start left, add our preloads and the far FLOWER for TIP 2, tunnel right, the GARDEN for TIP 3, park. 76 in 10 of 10 seeds.",
                    "LeftTunnelAuto,PartnerPreloadsRightAuto@50", TRIANGLE, "spring hood", 40, 1),
            new Run("2-partner-fires-preloads/partner-holds-its-volley/four-tip-attempt-best-case",
                    "Chasing 4 TIPs: the partner holds its preloads for TIP 3. Its best run of 10: TIPs at 4, 13 and 23 s, back with pieces for TIP 4 at 28 s, 2-3 s short. In the other 9 the TIP 1 catch comes up short and TIP 2 fails.",
                    "FourTipXAuto,PartnerPreloadsRightLateAuto@50", TWIN_TURRET, "spring hood", 40, 1),
            // 3. Choreography.
            new Run("3-choreography/stay-home/catapult-triangle",
                    "lean-opp, catapult volleys only (triangle cup): each robot catches its own spill and fires it back; when a volley falls short the left robot gathers loose pieces with the webcam; both park.",
                    "LeanOppRightAuto,LeanOppLeftAuto@50", TRIANGLE, null, Double.NaN, 1),
            new Run("3-choreography/stay-home/two-spring-hoods-a-miss",
                    "lean-opp with two spring hoods, in a run that goes wrong: TIP 2 comes late and there is no TIP 3 (about half the seeds).",
                    "LeanOppRightAuto,LeanOppLeftAuto@50", TWIN, null, Double.NaN, 3),
            new Run("3-choreography/each-owns-an-end/duo-lz-4-tips",
                    "duo-lz: each robot owns one end of the HIVE; 4 TIPs and both park (4 of 10 seeds; the rest stop at 2 TIPs).",
                    "DuoLzRightAuto,DuoLzLeftAuto@50", TWIN, null, Double.NaN, 2),
    };

    @Test
    public void build() throws Exception {
        String name = System.getenv("BIOBUZZ_REVIEW");
        if (name == null) return;
        File root = new File(repoRoot(), "sim-review/" + name);
        root.mkdirs();
        List<String> rows = new ArrayList<>();
        for (Run run : RUNS) {
            File dir = new File(root, run.folder);
            if (dir.isDirectory()) try (Stream<java.nio.file.Path> old = Files.list(dir.toPath())) {
                for (java.nio.file.Path p : (Iterable<java.nio.file.Path>) old::iterator) Files.delete(p);
            }
            dir.mkdirs();
            File log = new File(dir, dir.getName() + ".wpilog");
            AutoSim.Result r = AutoStudyTest.run(run.spec, AutoStudyTest.designs().get(run.design),
                    run.partnerDesign == null ? null : AutoStudyTest.designs().get(run.partnerDesign),
                    run.partnerSpeed, run.seed, log);
            checkLayout(log, run.spec.contains(","));
            for (String auto : run.spec.split("@")[0].split(",")) copySource(auto, dir);
            System.out.println("REVIEW " + run.folder + ": " + r);
            String partner = run.partnerDesign == null ? "same as ours"
                    : String.format(Locale.ROOT, "%s at %.0f in/s", run.partnerDesign, run.partnerSpeed);
            StringBuilder tips = new StringBuilder();
            for (double t : r.tipsAt) tips.append(tips.length() == 0 ? "" : ", ").append(String.format(Locale.ROOT, "%.1f", t));
            StringBuilder parks = new StringBuilder();
            for (AutoSim.RobotResult robot : r.robots) parks.append(parks.length() == 0 ? "" : " / ").append(robot.park ? "yes" : "no");
            rows.add(run.folder.split("/")[0] + "\t" + String.format(Locale.ROOT, "| `%s` | %s | %s | %s | %d | **%d** | %s | %s |",
                    run.folder.substring(run.folder.indexOf('/') + 1), run.watch, run.design, partner, run.seed, r.autoPoints(),
                    tips.length() == 0 ? "none" : tips + " s", parks));
        }
        try (PrintWriter out = new PrintWriter(new File(root, "README.md"), "UTF-8")) {
            out.println("# Simulated Autos for review: " + name);
            out.println();
            out.println("One folder per run: the `.wpilog` and the Auto Builder `.pp` files that made it. Red alliance,");
            out.println("each seed a typical run, not the best one, unless the row says otherwise. Points are AUTO only.");
            out.println();
            out.println("In AdvantageScope: open a log, then **File → Import Layout** with `sim-review/advantagescope-layout.json`.");
            out.println("AUTO starts 10 s into each log and the robots stand still before and after it. `/Match/Clock`");
            out.println("reads like \"AUTO 21.5 s, 8.5 left\"; drag it onto a line graph's discrete fields to see it on the timeline.");
            out.println();
            out.println("TIP times are match time; on AdvantageScope's timeline add 10 s.");
            out.println();
            for (String[] section : SECTIONS) {
                out.println("## " + section[1]);
                out.println();
                out.println("`" + section[0] + "/`. " + section[2]);
                out.println();
                out.println("| Folder | What to watch | Our robot | Partner | Seed | AUTO points | TIPs at | Parked (us / partner) |");
                out.println("|---|---|---|---|---|---|---|---|");
                for (String row : rows) {
                    String[] parts = row.split("\t", 2);
                    if (parts[0].equals(section[0])) out.println(parts[1]);
                }
                out.println();
            }
            out.println();
            out.println("Rebuild this set (the runs are listed in `ReviewPackageTest.RUNS`). Windows PowerShell:");
            out.println();
            out.println("```powershell");
            out.println("$env:BIOBUZZ_REVIEW = \"" + name + "\"");
            out.println(".\\gradlew.bat :TeamCode:testDebugUnitTest --tests \"*ReviewPackageTest*\" -i");
            out.println("Remove-Item Env:BIOBUZZ_REVIEW");
            out.println("```");
            out.println();
            out.println("macOS/Linux: `BIOBUZZ_REVIEW=" + name
                    + " ./gradlew :TeamCode:testDebugUnitTest --tests '*ReviewPackageTest*' -i`");
        }
    }

    /** Copies the {@code .pp} an exported Auto was made from into {@code dir}. */
    static void copySource(String autoClass, File dir) throws Exception {
        String source = (String) Class.forName(AutoStudyTest.PKG + autoClass).getField("SOURCE").get(null);
        File builder = new File(TeamCodeDir.get(), "src/test/resources/auto-builder");
        try (Stream<java.nio.file.Path> all = Files.walk(builder.toPath())) {
            java.nio.file.Path pp = all.filter(p -> p.getFileName().toString().equals(source)).findFirst()
                    .orElseThrow(() -> new AssertionError(autoClass + ": no " + source + " under " + builder));
            Files.copy(pp, new File(dir, source).toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /** Fails if the log lacks any key that the committed AdvantageScope layout draws. */
    static void checkLayout(File log) throws IOException {
        checkLayout(log, true);
    }

    /** As above; a run with one robot has no partner, so the layout's partner keys are not expected. */
    static void checkLayout(File log, boolean twoRobots) throws IOException {
        Set<String> missing = new LinkedHashSet<>(layoutKeys());
        if (!twoRobots) missing.removeIf(k -> k.contains("Partner"));
        missing.removeAll(new WpiLogReader(Files.readAllBytes(log.toPath())).entries.keySet());
        if (!missing.isEmpty()) {
            fail(log.getName() + " lacks keys the AdvantageScope layout uses: " + missing);
        }
    }

    static Set<String> layoutKeys() throws IOException {
        String json = new String(Files.readAllBytes(new File(repoRoot(), "sim-review/advantagescope-layout.json")
                .toPath()), StandardCharsets.UTF_8);
        Set<String> keys = new LinkedHashSet<>();
        Matcher m = Pattern.compile("\"logKey\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
        while (m.find()) keys.add(m.group(1));
        assertTrue("the layout names no log keys", !keys.isEmpty());
        return keys;
    }

    static File repoRoot() {
        return TeamCodeDir.get().getParentFile();
    }

    /** Every two-robot Auto log has what the layout draws (it runs without {@code BIOBUZZ_REVIEW}). */
    @Test
    public void layoutKeysAreInTwoRobotLogs() throws Exception {
        File log = new File(TeamCodeDir.simLogs(), "layout-check.wpilog");
        AutoStudyTest.run("DuoLzRightAuto,DuoLzLeftAuto@50", AutoStudyTest.designs().get(TWIN), null, Double.NaN, 1, log);
        checkLayout(log);
    }
}
