package org.firstinspires.ftc.teamcode.logging;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * One request from the Visualizer's "Save & Simulate": an Auto on one robot design, over several seeds, through
 * {@link AutoStudyTest#run} exactly as a study runs it, alone and then beside each partner stand-in the request
 * lists, each at its timing. The "Simulate Auto" workflow writes {@code request.json} into a folder and runs this;
 * it reads back {@code result.json} and the seeds' {@code .wpilog}s from the same folder.
 *
 * <pre>
 * BIOBUZZ_SIM_RUN=/path/to/folder ./gradlew :TeamCode:testDebugUnitTest --tests '*SimRunTest*' --rerun
 * </pre>
 * {@code request.json}:
 * <pre>
 * {"spec": "RSoloAuto@50", "design": "real robot", "seeds": [1, 2, …, 10], "alliance": "RED",
 *  "partnerSeeds": [1, 2, 3, 4, 5],
 *  "partners": [
 *    {"partner": "PartnerLStillAuto"},
 *    {"name": "fires at 5 s", "partner": "PartnerLLaunchParkAuto",
 *     "timing": {"startDelayS": 1.5, "speed": 40, "launchS": 0.8, "pickS": 4}}
 *  ],
 *  "metadata": {"SourceCommit": "…"}}
 * </pre>
 * Only {@code spec} is required: our Auto and its drive profile ({@code @50}: top speed in in/s; see
 * {@link AutoStudyTest#profile}). Each {@code partners} entry is one more run of the whole seed list with that
 * stand-in (a generated class in {@code opmodes/auto/generated}, its {@code .pp} in {@code TeamCode/autos/partners/})
 * beside us, timed by its {@code timing} ({@link AutoStudyTest.PartnerTiming}: a start delay, a drive speed, seconds
 * per launch volley, seconds at a FLOWER; each optional). An entry may name its own {@code seeds}; otherwise it runs
 * the request's {@code partnerSeeds}, else its {@code seeds}. {@code design} is our robot; an entry's {@code design}
 * is the partner's (ours by default). The older {@code spec} with a comma ({@code "Ours,PartnerAuto@50"}) and
 * {@code partnerDesign}/{@code partnerSpeed} still run as before, as the first entry.
 *
 * <p>{@code result.json} is the first entry's result as it always was ({@code runs}, one per seed), plus
 * {@code entries}: one row per run of every entry ({@link #row}: points, our launches, the TIPs and when, how our
 * first wait ended and when, the first robot collision, whether we parked), with each partner entry's folder.
 * Each partner entry's full result and logs are in that folder, {@code <partner .pp name>/<timing name>/}
 * ({@code partner-l-launch-park/fires-at-5-s/result.json}). The {@code metadata} entries go on every log's
 * Metadata tab, with the run's spec, designs, partner, timing and seed.
 */
public class SimRunTest {

    static final String DEFAULT_DESIGN = RobotDesign.REAL;

    @Test
    public void run() throws Exception {
        String folder = System.getenv("BIOBUZZ_SIM_RUN");
        if (folder == null) return;
        File dir = new File(folder);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("designs", new ArrayList<>(AutoStudyTest.designs().keySet()));
        try {
            runAll(request(dir), dir, out);
        } catch (Throwable t) {
            out.put("error", describe(t));
            throw t;
        } finally {
            write(dir, out);
        }
    }

    private static void write(File dir, Map<String, Object> result) throws Exception {
        dir.mkdirs();
        Files.write(new File(dir, "result.json").toPath(), MiniJson.write(result).getBytes(StandardCharsets.UTF_8));
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> request(File dir) throws Exception {
        String text = new String(Files.readAllBytes(new File(dir, "request.json").toPath()), StandardCharsets.UTF_8);
        return (Map<String, Object>) MiniJson.parse(text);
    }

    /** One run of the seed list: our Auto alone, or beside a partner stand-in at a timing. */
    static final class Entry {
        /** The row's name: "alone", else the request's {@code name}, else the timing's own words. */
        final String name;
        /** The spec as {@link AutoStudyTest#run} reads it: ours, or {@code Ours,Partner@speed}. */
        final String spec;
        /** The partner's generated class ({@code PartnerLLaunchParkAuto}), or null alone. */
        final String partner;
        final RobotDesign partnerDesign;
        final String partnerDesignName;
        final double partnerSpeed;
        final AutoStudyTest.PartnerTiming timing;
        final Map<String, Object> timingJson;
        final List<Object> seeds;
        /** Where its result and logs go, under the request's folder: "" for the first entry. */
        final String folder;

        Entry(String name, String spec, String partner, RobotDesign partnerDesign, String partnerDesignName, double partnerSpeed,
              AutoStudyTest.PartnerTiming timing, Map<String, Object> timingJson, List<Object> seeds, String folder) {
            this.name = name;
            this.spec = spec;
            this.partner = partner;
            this.partnerDesign = partnerDesign;
            this.partnerDesignName = partnerDesignName;
            this.partnerSpeed = partnerSpeed;
            this.timing = timing;
            this.timingJson = timingJson;
            this.seeds = seeds;
            this.folder = folder;
        }
    }

    @SuppressWarnings("unchecked")
    static List<Entry> entries(Map<String, Object> request) throws Exception {
        String spec = (String) request.get("spec");
        if (spec == null || spec.isEmpty()) throw new IllegalArgumentException("request.json has no spec");
        String partnerName = (String) request.get("partnerDesign");
        RobotDesign partner = partnerName == null ? null : design(partnerName);
        double partnerSpeed = request.get("partnerSpeed") == null ? Double.NaN : ((Number) request.get("partnerSpeed")).doubleValue();
        List<Object> seeds = request.get("seeds") == null ? defaultSeeds() : (List<Object>) request.get("seeds");
        List<Object> partnerSeeds = request.get("partnerSeeds") == null ? seeds : (List<Object>) request.get("partnerSeeds");
        List<Entry> out = new ArrayList<>();
        // The first entry: the spec as given (alone, or with the older comma partner).
        String[] at = spec.split("@");
        String ours = at[0].split("\\|")[0];
        boolean alone = !ours.contains(",");
        out.add(new Entry(alone ? "alone" : ours.substring(ours.indexOf(',') + 1), spec, null, partner, partnerName, partnerSpeed,
                null, null, seeds, ""));
        Object list = request.get("partners");
        if (list == null) return out;
        if (!(list instanceof List)) throw new IllegalArgumentException("\"partners\" must be a list of entries");
        String profile = at.length > 1 ? "@" + at[1] : "";
        String base = ours.split(",")[0];
        for (Object o : (List<Object>) list) {
            if (!(o instanceof Map)) throw new IllegalArgumentException("a partners entry is not an object: " + o);
            Map<String, Object> e = (Map<String, Object>) o;
            String cls = (String) e.get("partner");
            if (cls == null || cls.isEmpty()) throw new IllegalArgumentException("a partners entry has no \"partner\" class");
            Class<?> type;
            try {
                type = Class.forName(AutoStudyTest.PKG + cls);
            } catch (ClassNotFoundException x) {
                throw new IllegalArgumentException("no partner stand-in named \"" + cls + "\" in opmodes/auto/generated", x);
            }
            Map<String, Object> timingJson = e.get("timing") instanceof Map ? (Map<String, Object>) e.get("timing") : null;
            AutoStudyTest.PartnerTiming timing = AutoStudyTest.PartnerTiming.of(timingJson);
            String name = e.get("name") instanceof String && !((String) e.get("name")).isEmpty() ? (String) e.get("name") : timing.slug();
            String designName = e.get("design") instanceof String ? (String) e.get("design") : partnerName;
            List<Object> own = e.get("seeds") instanceof List ? (List<Object>) e.get("seeds") : partnerSeeds;
            String folder = source(type) + "/" + slug(name);
            for (Entry seen : out) {
                if (seen.folder.equals(folder)) {
                    throw new IllegalArgumentException("two partners entries share the folder \"" + folder + "\": give one a different name");
                }
            }
            out.add(new Entry(name, base + "," + cls + profile, cls, designName == null ? null : design(designName), designName,
                    partnerSpeed, timing, timingJson, own, folder));
        }
        return out;
    }

    /** The partner's {@code .pp} name without the extension ({@code partner-l-launch-park}), else its class name. */
    static String source(Class<?> type) {
        try {
            String s = (String) type.getField("SOURCE").get(null);
            return s.endsWith(".pp") ? s.substring(0, s.length() - 3) : s;
        } catch (ReflectiveOperationException e) {
            return type.getSimpleName();
        }
    }

    /** A folder name from a timing's name: lower case letters and digits, runs of anything else as one dash. */
    static String slug(String name) {
        String s = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
        return s.isEmpty() ? "default" : s;
    }

    @SuppressWarnings("unchecked")
    private static void runAll(Map<String, Object> request, File dir, Map<String, Object> out) throws Exception {
        String designName = request.get("design") == null ? DEFAULT_DESIGN : (String) request.get("design");
        RobotDesign design = design(designName);
        Alliance alliance = "BLUE".equals(request.get("alliance")) ? Alliance.BLUE : Alliance.RED;
        List<Entry> entries = entries(request);
        List<Object> table = new ArrayList<>();
        Map<String, String> given = new LinkedHashMap<>();
        if (request.get("metadata") instanceof Map) {
            for (Map.Entry<String, Object> e : ((Map<String, Object>) request.get("metadata")).entrySet()) {
                if (e.getValue() != null) given.put(e.getKey(), String.valueOf(e.getValue()));
            }
        }
        for (Entry entry : entries) {
            boolean first = entry.folder.isEmpty();
            File folder = first ? dir : new File(dir, entry.folder);
            Map<String, Object> result = first ? out : new LinkedHashMap<>();
            if (!first) result.put("designs", out.get("designs"));
            result.put("spec", entry.spec);
            result.put("design", designName);
            result.put("partnerDesign", entry.partnerDesignName);
            result.put("alliance", alliance.name());
            Map<String, Object> summary = summary(entry);
            result.put("entry", summary);
            List<Object> runs = new ArrayList<>();
            result.put("runs", runs);
            List<Object> rows = new ArrayList<>();
            summary.put("rows", rows);
            if (first) out.put("entries", table);
            table.add(summary);
            // What this run simulated, on each log's Metadata tab, so a log can be matched to the Auto (and the
            // commit) a real match ran: the request's own entries, then the run's settings.
            Map<String, String> metadata = new LinkedHashMap<>(given);
            metadata.put("SimSpec", entry.spec);
            metadata.put("SimDesign", designName);
            metadata.put("SimPartnerDesign", entry.partnerDesignName == null ? "same as ours" : entry.partnerDesignName);
            metadata.put("SimPartner", entry.partner == null ? "none" : entry.partner);
            metadata.put("SimPartnerTiming", entry.timing == null ? "none" : entry.name + " (" + entry.timing.slug() + ")");
            double points = 0;
            boolean done = false;
            try {
                for (Object s : entry.seeds) {
                    double value = ((Number) s).doubleValue();
                    if (value != Math.rint(value)) throw new IllegalArgumentException("seed " + value + " is not a whole number");
                    long seed = (long) value;
                    String log = "seed-" + seed + ".wpilog";
                    metadata.put("SimSeed", String.valueOf(seed));
                    AutoSim.Result r = AutoStudyTest.run(entry.spec, design, entry.partnerDesign, entry.partnerSpeed, entry.timing,
                            seed, alliance, metadata, new File(folder, log));
                    System.out.println("SIMRUN " + entry.name + " seed " + seed + ": " + r);
                    runs.add(describe(r, seed, log));
                    rows.add(row(r, seed, log));
                    points += r.autoPoints();
                }
                done = true;
            } finally {
                summary.put("runs", (double) runs.size());
                summary.put("meanPoints", runs.isEmpty() ? null : points / runs.size());
                if (!done) {
                    // The run stopped early: the publish step must not take what is there for a finished run.
                    summary.put("error", "stopped after " + runs.size() + " of " + entry.seeds.size() + " seeds");
                    result.put("error", summary.get("error"));
                }
                if (!first) write(folder, result);
            }
        }
    }

    private static Map<String, Object> summary(Entry entry) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", entry.name);
        m.put("folder", entry.folder);
        m.put("spec", entry.spec);
        m.put("partner", entry.partner);
        m.put("partnerSource", entry.partner == null ? null : entry.folder.substring(0, entry.folder.indexOf('/')) + ".pp");
        m.put("timing", entry.timingJson);
        m.put("timingSlug", entry.timing == null ? null : entry.timing.slug());
        return m;
    }

    private static List<Object> defaultSeeds() {
        List<Object> seeds = new ArrayList<>();
        for (int s = 1; s <= 10; s++) seeds.add((double) s);
        return seeds;
    }

    private static RobotDesign design(String name) {
        RobotDesign d = AutoStudyTest.designs().get(name);
        if (d == null) throw new IllegalArgumentException("no robot design named \"" + name + "\"");
        return d;
    }

    /**
     * One line of the results table for a run: AUTO points, our launches (and the partner's), the TIPs and when, how
     * our Auto's first wait ended ({@link #waits}), the first robot collision and when, and whether we (and the
     * partner) parked.
     */
    static Map<String, Object> row(AutoSim.Result r, long seed, String log) {
        Map<String, Object> m = new LinkedHashMap<>();
        AutoSim.RobotResult ours = r.robots.get(0);
        // The partner is our alliance's other robot; with "Ours|Opponent" robot 2 is an opponent, not a partner.
        AutoSim.RobotResult partner = r.robots.size() > 1 && r.robots.get(1).alliance == ours.alliance ? r.robots.get(1) : null;
        m.put("seed", (double) seed);
        m.put("log", log);
        m.put("points", (double) r.autoPoints());
        m.put("launched", (double) ours.launched);
        m.put("partnerLaunched", partner == null ? null : (double) partner.launched);
        List<Object> tips = new ArrayList<>();
        for (double t : r.tipsAt) tips.add(t);
        m.put("tipsAt", tips);
        m.put("autoTips", (double) r.autoTips());
        List<Object> waits = waits(ours.timeline);
        m.put("wait", waits.isEmpty() ? null : waits.get(0));
        m.put("waits", waits);
        m.put("robotsCollidedAt", time(r.robotsCollidedAt));
        m.put("parked", ours.park);
        m.put("partnerParked", partner == null ? null : partner.park);
        m.put("finishedAt", ours.finished ? ours.finishedAt : null);
        m.put("partnerFinishedAt", partner == null || !partner.finished ? null : partner.finishedAt);
        m.put("g409", (double) r.g409);
        return m;
    }

    private static final Pattern WAIT_STARTS = Pattern.compile("^\\s*(\\d+\\.\\d+) wait (.+?)(?: \\(holds \\d+\\))?$");
    private static final Pattern WAIT_ENDS = Pattern.compile("^\\s*(\\d+\\.\\d+) (.+?): (.+?) after (\\d+\\.\\d+) s(?: \\(holds \\d+\\))?$");
    private static final Pattern TIME_ROW = Pattern.compile("^\\d+ ms passed$");

    /**
     * Every "Wait for …" card a robot ran, from its timeline (AutoKit's trace: {@code wait <label>}, then
     * {@code <label>: <row> after <s> s}): {@code label}, {@code startedAt}, {@code outcome} (the trigger that fired,
     * or "timed out"), {@code fired} (true when a trigger did), {@code after} (seconds waited) and {@code at}
     * (the match second it ended); a wait still open when AUTO ended has no end.
     */
    static List<Object> waits(List<String> timeline) {
        List<Object> out = new ArrayList<>();
        List<Map<String, Object>> open = new ArrayList<>();
        for (String line : timeline) {
            Matcher start = WAIT_STARTS.matcher(line);
            if (start.matches()) {
                Map<String, Object> w = new LinkedHashMap<>();
                w.put("label", start.group(2));
                w.put("startedAt", Double.parseDouble(start.group(1)));
                w.put("outcome", null);
                w.put("fired", null);
                w.put("after", null);
                w.put("at", null);
                open.add(w);
                out.add(w);
                continue;
            }
            Matcher end = WAIT_ENDS.matcher(line);
            if (!end.matches()) continue;
            for (int i = open.size() - 1; i >= 0; i--) {
                Map<String, Object> w = open.get(i);
                if (!w.get("label").equals(end.group(2))) continue;
                boolean timedOut = TIME_ROW.matcher(end.group(3)).matches();
                w.put("outcome", timedOut ? "timed out" : end.group(3));
                w.put("fired", !timedOut);
                w.put("after", Double.parseDouble(end.group(4)));
                w.put("at", Double.parseDouble(end.group(1)));
                open.remove(i);
                break;
            }
        }
        return out;
    }

    private static Map<String, Object> describe(AutoSim.Result r, long seed, String log) {
        Map<String, Object> run = new LinkedHashMap<>();
        run.put("seed", (double) seed);
        run.put("log", log);
        run.put("points", (double) r.autoPoints());
        run.put("autoTips", (double) r.autoTips());
        List<Object> tips = new ArrayList<>();
        for (double t : r.tipsAt) tips.add(t);
        run.put("tipsAt", tips);
        run.put("launched", (double) r.launched);
        run.put("scored", (double) r.scored);
        run.put("cellLoad", r.cellLoad);
        run.put("held", (double) r.held);
        run.put("robotsCollidedAt", time(r.robotsCollidedAt));
        run.put("g409", (double) r.g409);
        List<Object> robots = new ArrayList<>();
        for (AutoSim.RobotResult robot : r.robots) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("auto", robot.auto);
            m.put("launched", (double) robot.launched);
            m.put("finishedAt", robot.finished ? robot.finishedAt : null);
            m.put("leave", robot.leave);
            m.put("park", robot.park);
            m.put("illegalStart", robot.illegalStart);
            m.put("crossedAt", time(robot.crossedAt));
            m.put("hitHiveAt", time(robot.hitHiveAt));
            m.put("hitFlowerAt", time(robot.hitFlowerAt));
            m.put("waits", waits(robot.timeline));
            m.put("timeline", new ArrayList<Object>(robot.timeline));
            robots.add(m);
        }
        run.put("robots", robots);
        run.put("summary", r.toString());
        return run;
    }

    /** NaN ("never") as null, which JSON can carry. */
    private static Object time(double t) {
        return Double.isNaN(t) ? null : t;
    }

    private static String describe(Throwable t) {
        StringBuilder b = new StringBuilder(String.valueOf(t.getMessage() == null ? t : t.getMessage()));
        for (Throwable c = t.getCause(); c != null && c != c.getCause(); c = c.getCause()) {
            if (c.getMessage() != null) b.append(": ").append(c.getMessage());
        }
        return b.toString();
    }
}
