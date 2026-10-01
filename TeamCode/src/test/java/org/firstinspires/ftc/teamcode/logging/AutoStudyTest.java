package org.firstinspires.ftc.teamcode.logging;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.PartnerThreeTipAuto;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Runs exported Autos on robot designs, several seeds each, and prints what each earns in AUTO and
 * the head start it leaves TELEOP. Opt in, listing Autos (two joined by a comma run together as an
 * alliance) with their drivetrain speed:
 *
 * <pre>
 * BIOBUZZ_AUTO_STUDY="SoloTwoTipAuto@40;DuoSouthAuto,DuoNorthAuto@60" \
 *   BIOBUZZ_AUTO_DESIGNS="turret|spring hood" ./gradlew :TeamCode:testDebugUnitTest --tests '*AutoStudyTest*' -i
 * </pre>
 * Optional: {@code BIOBUZZ_AUTO_RUNS} (default 10). PartnerThreeTipAuto gets its standing partner.
 */
public class AutoStudyTest {

    static Map<String, RobotDesign> designs() {
        Map<String, RobotDesign> m = new LinkedHashMap<>();
        m.put("turret", RobotDesign.standard());
        m.put("spring hood", RobotDesign.springHood());
        RobotDesign slow = RobotDesign.springHood().copy("spring hood, 3 s spin-up");
        slow.spinUpS = 3.0;
        m.put(slow.name, slow);
        RobotDesign ratio = RobotDesign.springHood().copy("spring hood, NECTAR 5% slow");
        ratio.pollenSpeedFactor = 1.025;
        ratio.nectarSpeedFactor = 0.975;
        m.put(ratio.name, ratio);
        RobotDesign twin = RobotDesign.springHood().copy("two spring hoods");
        twin.launchers = 2;
        m.put(twin.name, twin);
        return m;
    }

    static final String PKG = "org.firstinspires.ftc.teamcode.opmodes.auto.generated.";

    static AutoSim.Result run(String spec, RobotDesign design, long seed, File file) throws Exception {
        String[] at = spec.split("@");
        double speed = at.length > 1 ? Double.parseDouble(at[1]) : 50;
        String[] autos = at[0].split(",");
        Class<?> first = Class.forName(PKG + autos[0]);
        AutoSim sim = new AutoSim(first, Alliance.RED, seed).speed(speed, speed * 0.9).design(design);
        if (first == PartnerThreeTipAuto.class) {
            sim.partner(DesignComparisonTest.NORTH_PARTNER, DesignComparisonTest.NORTH_PARTNER_POLLEN);
        }
        if (autos.length > 1) sim.alsoRun(Class.forName(PKG + autos[1])).speed(speed, speed * 0.9).design(design);
        return sim.write(file);
    }

    @Test
    public void study() throws Exception {
        String specs = System.getenv("BIOBUZZ_AUTO_STUDY");
        if (specs == null) return;
        String only = System.getenv("BIOBUZZ_AUTO_DESIGNS");
        String runsEnv = System.getenv("BIOBUZZ_AUTO_RUNS");
        int runs = runsEnv == null ? 10 : Integer.parseInt(runsEnv);
        for (String spec : specs.split(";")) {
            for (Map.Entry<String, RobotDesign> e : designs().entrySet()) {
                if (only != null && !java.util.Arrays.asList(only.split("\\|")).contains(e.getKey())) continue;
                int[] count = new int[8];
                double[] sum = new double[8];
                double points = 0, load = 0, held = 0;
                int parked = 0, robots = 0, problems = 0;
                for (long seed = 1; seed <= runs; seed++) {
                    File file = new File(TeamCodeDir.simLogs(), "study-" + spec.replaceAll("[^A-Za-z0-9]+", "-")
                            + "-" + e.getKey().replaceAll("[^A-Za-z0-9]+", "-") + "-" + seed + ".wpilog");
                    AutoSim.Result r = run(spec, e.getValue(), seed, file);
                    String tl = System.getenv("BIOBUZZ_AUTO_TIMELINE");
                    if (tl != null && (tl.equals("1") ? seed == 1 : tl.equals("fail") ? (r.robots.stream().anyMatch(x -> !x.park)) : Long.parseLong(tl) == seed)) {
                        System.out.println("STUDY   seed " + seed + ": " + r);
                        for (AutoSim.RobotResult robot : r.robots) {
                            for (String t : robot.timeline) System.out.println("STUDY     " + robot.auto + " " + t);
                        }
                    }
                    for (int i = 0; i < r.autoTips(); i++) {
                        count[i]++;
                        sum[i] += r.tipsAt.get(i);
                    }
                    points += r.autoPoints();
                    load += r.cellLoad;
                    held += r.held;
                    for (AutoSim.RobotResult robot : r.robots) {
                        robots++;
                        if (robot.leave && robot.park) parked++;
                        if (!Double.isNaN(robot.crossedAt) || !Double.isNaN(robot.hitHiveAt)) {
                            if (problems++ == 0) System.out.println("STUDY   first problem: " + robot);
                        }
                    }
                    if (!Double.isNaN(r.robotsCollidedAt) && problems++ == 0) {
                        System.out.printf(Locale.ROOT, "STUDY   first problem: robots collide at %.1f s%n", r.robotsCollidedAt);
                    }
                }
                StringBuilder line = new StringBuilder(String.format(Locale.ROOT, "%-36s %-28s", spec, e.getKey()));
                for (int i = 0; i < 6 && count[i] > 0; i++) {
                    line.append(String.format(Locale.ROOT, " TIP%d %2d/%d@%4.1f", i + 1, count[i], runs, sum[i] / count[i]));
                }
                line.append(String.format(Locale.ROOT, " | %.1f pts, parked %d/%d, CELL %.0f%%, held %.1f%s",
                        points / runs, parked, robots, 100 * load / runs, held / runs,
                        problems == 0 ? "" : ", PROBLEMS " + problems));
                System.out.println("STUDY " + line);
            }
        }
    }
}
