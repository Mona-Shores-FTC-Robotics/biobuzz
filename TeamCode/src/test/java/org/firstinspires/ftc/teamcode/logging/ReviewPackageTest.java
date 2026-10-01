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

    /**
     * The set. Folder names say left/right as the drivers see the field (red: the old "north" is
     * left, "south" is right). Seed 1 for all: a typical run, not the best one.
     */
    static final Run[] RUNS = {
            new Run("1-playoff-sisters-stay-home-catapult",
                    "Both sister robots stay at their own end, catch their spill and fire it back (lean-opp).",
                    "LeanOppRightAuto,LeanOppLeftAuto@50", CATAPULT, null, Double.NaN, 1),
            new Run("2-playoff-sisters-stay-home-two-spring-hoods",
                    "The same plan with two spring-hood launchers each.",
                    "LeanOppRightAuto,LeanOppLeftAuto@50", TWIN, null, Double.NaN, 1),
            new Run("3-playoff-sisters-stay-home-loose-catapult",
                    "The catapult again, with a volley that comes apart in the air.",
                    "LeanOppRightAuto,LeanOppLeftAuto@50", "clump catapult 72 deg, loose clump", null, Double.NaN, 1),
            new Run("4-playoff-sisters-both-park",
                    "duo-lz: each robot owns one end of the HIVE, and both park.",
                    "DuoLzRightAuto,DuoLzLeftAuto@50", TWIN, null, Double.NaN, 1),
            new Run("5-qual-solo-partner-fires-preloads-left",
                    "Our solo Auto (three-tip-adaptive); the partner fires its 4 preloads when the left CELL rises, then parks.",
                    "ThreeTipAdaptiveAuto,PartnerPreloadsParkAuto@50", TWIN, "spring hood", 40, 1),
            new Run("6-qual-solo-partner-only-leaves",
                    "Our solo Auto; the partner only leaves and parks.",
                    "ThreeTipAdaptiveAuto,PartnerLeaveParkAuto@50", TWIN, "spring hood", 40, 1),
            new Run("7-qual-solo-partner-fires-left-after-5s",
                    "Our solo Auto; the partner starts left, waits 5 s, fires its preloads and parks.",
                    "ThreeTipAdaptiveAuto,PartnerPreloadsLeftTimerAuto@50", TWIN, "spring hood", 40, 1),
            new Run("8-qual-partner-tips-right-we-go-left",
                    "The partner starts right and makes TIP 1; we start left and take the left CELL.",
                    "LeftFirstAuto,PartnerPreloadsRightAuto@50", CATAPULT, "spring hood", 40, 1),
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
            File log = new File(dir, run.folder + ".wpilog");
            AutoSim.Result r = AutoStudyTest.run(run.spec, AutoStudyTest.designs().get(run.design),
                    run.partnerDesign == null ? null : AutoStudyTest.designs().get(run.partnerDesign),
                    run.partnerSpeed, run.seed, log);
            checkLayout(log);
            for (String auto : run.spec.split("@")[0].split(",")) copySource(auto, dir);
            System.out.println("REVIEW " + run.folder + ": " + r);
            String partner = run.partnerDesign == null ? "same as ours"
                    : String.format(Locale.ROOT, "%s at %.0f in/s", run.partnerDesign, run.partnerSpeed);
            StringBuilder tips = new StringBuilder();
            for (double t : r.tipsAt) tips.append(tips.length() == 0 ? "" : ", ").append(String.format(Locale.ROOT, "%.1f", t));
            StringBuilder parks = new StringBuilder();
            for (AutoSim.RobotResult robot : r.robots) parks.append(parks.length() == 0 ? "" : " / ").append(robot.park ? "yes" : "no");
            rows.add(String.format(Locale.ROOT, "| `%s` | %s | %s | %s | **%d** | %s | %s |",
                    run.folder, run.watch, run.design, partner, r.autoPoints(),
                    tips.length() == 0 ? "none" : tips + " s", parks));
        }
        try (PrintWriter out = new PrintWriter(new File(root, "README.md"), "UTF-8")) {
            out.println("# Simulated Autos for review: " + name);
            out.println();
            out.println("One folder per run: the `.wpilog` and the Auto Builder `.pp` files that made it. Red alliance,");
            out.println("seed 1 (a typical run, not the best one). Points are AUTO only.");
            out.println();
            out.println("In AdvantageScope: open a log, then **File → Import Layout** with `sim-review/advantagescope-layout.json`.");
            out.println("AUTO starts 10 s into each log and the robots stand still before and after it. `/Match/Clock`");
            out.println("reads like \"AUTO 21.5 s, 8.5 left\"; drag it onto a line graph's discrete fields to see it on the timeline.");
            out.println();
            out.println("TIP times are match time; on AdvantageScope's timeline add 10 s.");
            out.println();
            out.println("| Folder | What to watch | Our robot | Partner | AUTO points | TIPs at | Parked (us / partner) |");
            out.println("|---|---|---|---|---|---|---|");
            for (String row : rows) out.println(row);
            out.println();
            out.println("Rebuild this set: `BIOBUZZ_REVIEW=" + name
                    + " ./gradlew :TeamCode:testDebugUnitTest --tests '*ReviewPackageTest*' -i`. The runs are listed in");
            out.println("`ReviewPackageTest.RUNS`.");
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
        Set<String> missing = new LinkedHashSet<>(layoutKeys());
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
