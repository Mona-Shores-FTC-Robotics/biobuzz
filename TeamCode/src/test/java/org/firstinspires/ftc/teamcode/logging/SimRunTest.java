package org.firstinspires.ftc.teamcode.logging;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One request from the Auto Builder's "Run in simulator" button: an Auto (or two, run together as
 * an alliance) on one robot design, over several seeds, through {@link AutoStudyTest#run} exactly
 * as a study runs it. The Auto Builder's dev server writes {@code request.json} into a folder and
 * runs this; it reads back {@code result.json} and the seeds' {@code .wpilog}s from the same folder.
 *
 * <pre>
 * BIOBUZZ_SIM_RUN=/path/to/folder ./gradlew :TeamCode:testDebugUnitTest --tests '*SimRunTest*' --rerun
 * </pre>
 * {@code request.json}: {@code {"spec": "Recycle5LeftAuto,PartnerPreloadsParkAuto@50", "design":
 * "two spring hoods, full-width intake", "partnerDesign": null, "partnerSpeed": null, "seeds": [1, 2],
 * "alliance": "RED", "metadata": {"SourceCommit": "…"}}}. Only {@code spec} is required. The
 * {@code metadata} entries go on every log's Metadata tab, with the run's spec, designs and seed, so a
 * simulated log says which Auto and commit it simulated. {@code result.json} lists every run, the
 * design names this simulation knows, and {@code error} when the run could not start.
 *
 * <p>The "Simulate Auto" workflow runs this for each {@code TeamCode/sim-request.json} the
 * Visualizer's "Save to GitHub" commits, and publishes the result on the {@code sim-results} branch.
 */
public class SimRunTest {

    static final String DEFAULT_DESIGN = ReviewPackageTest.TWIN;

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
            Files.write(new File(dir, "result.json").toPath(), MiniJson.write(out).getBytes(StandardCharsets.UTF_8));
        }
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> request(File dir) throws Exception {
        String text = new String(Files.readAllBytes(new File(dir, "request.json").toPath()), StandardCharsets.UTF_8);
        return (Map<String, Object>) MiniJson.parse(text);
    }

    @SuppressWarnings("unchecked")
    private static void runAll(Map<String, Object> request, File dir, Map<String, Object> out) throws Exception {
        String spec = (String) request.get("spec");
        if (spec == null || spec.isEmpty()) throw new IllegalArgumentException("request.json has no spec");
        String designName = request.get("design") == null ? DEFAULT_DESIGN : (String) request.get("design");
        String partnerName = (String) request.get("partnerDesign");
        RobotDesign design = design(designName);
        RobotDesign partner = partnerName == null ? null : design(partnerName);
        double partnerSpeed = request.get("partnerSpeed") == null ? Double.NaN : (Double) request.get("partnerSpeed");
        Alliance alliance = "BLUE".equals(request.get("alliance")) ? Alliance.BLUE : Alliance.RED;
        List<Object> seeds = request.get("seeds") == null ? defaultSeeds() : (List<Object>) request.get("seeds");
        out.put("spec", spec);
        out.put("design", designName);
        out.put("partnerDesign", partnerName);
        out.put("alliance", alliance.name());

        List<Object> runs = new ArrayList<>();
        out.put("runs", runs);
        // What this run simulated, on each log's Metadata tab, so a log can be matched to the Auto
        // (and the commit) a real match ran: the request's own entries, then the run's settings.
        Map<String, String> metadata = new LinkedHashMap<>();
        Object given = request.get("metadata");
        if (given instanceof Map) {
            for (Map.Entry<String, Object> e : ((Map<String, Object>) given).entrySet()) {
                if (e.getValue() != null) metadata.put(e.getKey(), String.valueOf(e.getValue()));
            }
        }
        metadata.put("SimSpec", spec);
        metadata.put("SimDesign", designName);
        metadata.put("SimPartnerDesign", partnerName == null ? "same as ours" : partnerName);
        for (Object s : seeds) {
            long seed = ((Double) s).longValue();
            String log = "seed-" + seed + ".wpilog";
            metadata.put("SimSeed", String.valueOf(seed));
            AutoSim.Result r = AutoStudyTest.run(spec, design, partner, partnerSpeed, seed, alliance, metadata,
                    new File(dir, log));
            System.out.println("SIMRUN seed " + seed + ": " + r);
            runs.add(describe(r, seed, log));
        }
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
