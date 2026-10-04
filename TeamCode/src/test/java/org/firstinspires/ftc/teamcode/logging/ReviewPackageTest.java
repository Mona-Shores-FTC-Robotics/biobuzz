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
 * BIOBUZZ_REVIEW=2026-10-03 ./gradlew :TeamCode:testDebugUnitTest --tests '*ReviewPackageTest*' -i
 * </pre>
 * Writes {@code sim-review/<name>/} at the repository root (git ignores it: build it on your own
 * laptop, or share it as a zip, rather than committing logs). Every log is checked against
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

    // An intake the frame's width (mentor decision, 1 Oct 2026): a 24 in catcher can't use the tunnel.
    static final String TWIN = "two spring hoods, full-width intake";

    static final String TRIANGLE = "clump catapult 72 deg, triangle cup, full-width intake";


    /**
     * The sections of the set: the top folder of each run, and its heading in the README. What the
     * partner does decides the section (mentor review: keep the three kinds of match apart).
     */
    static final String[][] SECTIONS = {
            {"1-partner-stages-preloads", "The partner doesn't shoot: it stages its preloads for us",
                    "A partner that can't fire sets its 4 preloads on the tiles touching it (G304) for us to collect, then drives to park. The orange outline in front of our robot is where its intake catches pieces (18 in wide, the robot's own width)."},
            {"2-partner-fires-preloads", "The partner fires one volley of preloads, then parks",
                    "The most common partner in qualifications. `partner-starts-left` fires into the left CELL once our TIP 1 raises it; `partner-starts-right` fires at the start (TIP 1 is theirs), or holds its volley for later."},
            {"3-choreography", "Choreography: two robots that both run our Autos",
                    "Our two sister robots (or a playoff partner running our Autos), each at its own end of the HIVE."},
    };

    /**
     * The set, by section. Folder names say left/right as the drivers see the field. Each seed is a
     * typical run for its Auto and design (the median of 10, from {@code BIOBUZZ_AUTO_PER_SEED}), unless
     * the line says otherwise.
     */
    static final Run[] RUNS = {
            // 1. The partner doesn't shoot: it stages its preloads for us and parks.
            new Run("1-partner-stages-preloads/staged-three-tip",
                    "The partner sets its 4 preloads in a row at its side and parks. We fire ours (TIP 1), go through the tunnel, pick up the row with the webcam, fire, the far FLOWER for TIP 2, the GARDEN for TIP 3, park. 71 points on average, 3 TIPs in 15 of 20 runs (3 of 20 on slow tiles).",
                    "StagedThreeTipAuto,PartnerLeaveParkAuto@50", TWIN, "spring hood", 40, 3),
            // 2. The partner fires one volley.
            new Run("2-partner-fires-preloads/partner-starts-left/three-tip-adaptive",
                    "three-tip-adaptive: fires all 4, both FLOWERs and the GARDEN with angled shots, parks. The most robust Auto: 75 points, 3 TIPs in 19 of 20 runs on normal and slow tiles alike.",
                    "ThreeTipAdaptiveAuto,PartnerPreloadsParkAuto@50", TWIN, "spring hood", 40, 2),
            new Run("2-partner-fires-preloads/partner-starts-right/left-tunnel",
                    "The partner makes TIP 1 from the right start; we start left, add our preloads and the far FLOWER for TIP 2, tunnel right, the GARDEN for TIP 3, park. 73 points (76 on slow tiles), 3 TIPs in 18 of 20.",
                    "LeftTunnelAuto,PartnerPreloadsRightAuto@50", TRIANGLE, "spring hood", 40, 1),
            // 3. Our two sister robots, each at its own end, recycling its CELL's spills (Family A).
            new Run("3-choreography/sisters/recycle3",
                    "recycle3: each robot catches its own CELL's spill and holds it for the CELL's next rise; the right uses the GARDEN, the left both FLOWERs. No park: a TIP (20) beats PARK (5). A typical run, 4 TIPs. 84 points on average (78 on slow tiles).",
                    "Recycle3RightAuto,Recycle3LeftAuto@50", TRIANGLE, null, Double.NaN, 3),
            new Run("3-choreography/sisters/recycle3-five-tips",
                    "recycle3 in one of its 5-TIP runs (4 of 20): the fifth at 29.8 s.",
                    "Recycle3RightAuto,Recycle3LeftAuto@50", TRIANGLE, null, Double.NaN, 4),
            new Run("3-choreography/sisters/recycle4-staging",
                    "recycle4: the right robot sets its catch down (reversed intake) and fetches the GARDEN while it waits, then fires twice when its CELL rises. 87 points on normal tiles, the best there, but 67 on slow ones.",
                    "Recycle4RightAuto,Recycle4LeftAuto@50", TRIANGLE, null, Double.NaN, 3),
            new Run("3-choreography/sisters/recycle5-shoots-both-ways",
                    "recycle5: recycle4 with a launcher whose slats also throw straight back; the left robot throws the far FLOWER back from beside it, TIP 2 at 8.1 s. 82 points (75 on slow tiles).",
                    "Recycle5RightAuto,Recycle5LeftAuto@50", TRIANGLE + ", shoots both ways", null, Double.NaN, 2),
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
            out.println("AUTO starts 1 s into each log and the robots stand still before and after it. `/Match/Clock`");
            out.println("reads like \"AUTO 21.5 s, 8.5 left\"; drag it onto a line graph's discrete fields to see it on the timeline.");
            out.println();
            out.println("TIP times are match time; on AdvantageScope's timeline add 1 s.");
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
        // The team's Autos are in TeamCode/autos; test-only ones under the test resources.
        File autos = new File(TeamCodeDir.get(), "autos");
        File builder = new File(TeamCodeDir.get(), "src/test/resources/auto-builder");
        File pp = new File(autos, source);
        if (!pp.isFile()) {
            try (Stream<java.nio.file.Path> all = Files.walk(builder.toPath())) {
                pp = all.filter(p -> p.getFileName().toString().equals(source)).findFirst()
                        .orElseThrow(() -> new AssertionError(autoClass + ": no " + source + " in " + autos + " or under " + builder))
                        .toFile();
            }
        }
        Files.copy(pp.toPath(), new File(dir, source).toPath(), StandardCopyOption.REPLACE_EXISTING);
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
        AutoStudyTest.run("Recycle3RightAuto,Recycle3LeftAuto@50", AutoStudyTest.designs().get(TRIANGLE), null, Double.NaN, 1, log);
        checkLayout(log);
    }
}
