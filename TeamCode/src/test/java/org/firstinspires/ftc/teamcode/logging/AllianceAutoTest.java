package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.DuoNorthAuto;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.DuoSouthAuto;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

import java.io.File;
import java.util.Locale;

/**
 * Both of an alliance's robots, each running its own Auto on one field: duo-south loads the HIVE's
 * south CELL and duo-north the north one, each firing when the other's TIP brings its CELL up, and
 * both end parked in the LOADING ZONE. Writes
 * {@code TeamCode/build/sim-logs/alliance-duo-<alliance>.wpilog}; TeamCode/README.md, "Two robots",
 * says how to watch both.
 */
public class AllianceAutoTest {

    @Test
    public void duoTipsThreeTimesAndBothRobotsLeaveAndPark() throws Exception {
        for (Alliance alliance : new Alliance[] {Alliance.RED, Alliance.BLUE}) {
            AutoSim.Result r = new AutoSim(DuoSouthAuto.class, alliance, 3572L).speed(60, 55)
                    .alsoRun(DuoNorthAuto.class).speed(60, 55)
                    .write(new File(TeamCodeDir.simLogs(), "alliance-duo-" + alliance.name().toLowerCase() + ".wpilog"));
            System.out.println(r);
            assertTrue(r.toString(), r.autoTips() >= 3);
            for (AutoSim.RobotResult robot : r.robots) {
                assertTrue(r.toString(), robot.leave && robot.park);
                assertTrue(r.toString(), Double.isNaN(robot.crossedAt));
            }
            assertTrue(r.toString(), Double.isNaN(r.robotsCollidedAt));
            for (AutoSim.RobotResult robot : r.robots) assertTrue(r.toString(), Double.isNaN(robot.hitHiveAt));
        }
    }

    /**
     * Any two exported Autos as an alliance, 10 runs, for trying a plan. Opt in, naming the two
     * generated classes:
     *
     * <pre>
     * BIOBUZZ_ALLIANCE_STUDY=DuoSouthAuto,DuoNorthAuto ./gradlew :TeamCode:testDebugUnitTest \
     *     --tests '*AllianceAutoTest*' -i
     * </pre>
     * Optional: {@code BIOBUZZ_ALLIANCE_SPEED} (in/s, default 60), {@code BIOBUZZ_ALLIANCE_ZONES}
     * ({@code "48,80;0,48"}: each robot's CollectSeen x range, drawn for RED), and
     * {@code BIOBUZZ_ALLIANCE_TWIN=1} for two launchers on each robot.
     */
    @Test
    public void study() throws Exception {
        String pair = System.getenv("BIOBUZZ_ALLIANCE_STUDY");
        if (pair == null) return;
        String[] names = pair.split(",");
        String pkg = "org.firstinspires.ftc.teamcode.opmodes.auto.generated.";
        String speedEnv = System.getenv("BIOBUZZ_ALLIANCE_SPEED");
        double speed = speedEnv == null ? 60 : Double.parseDouble(speedEnv);
        String zones = System.getenv("BIOBUZZ_ALLIANCE_ZONES");
        RobotDesign design = RobotDesign.standard();
        if (System.getenv("BIOBUZZ_ALLIANCE_TWIN") != null) {
            design = RobotDesign.fixedLauncher().copy("two fixed launchers");
            design.launchers = 2;
        }
        int runs = 10;
        double[] sum = new double[10];
        int[] count = new int[10];
        int points = 0, parked = 0, problems = 0;
        for (long seed = 1; seed <= runs; seed++) {
            AutoSim sim = new AutoSim(Class.forName(pkg + names[0]), Alliance.RED, seed).speed(speed, speed * 0.9).design(design);
            if (zones != null) zone(sim, zones, 0);
            sim.alsoRun(Class.forName(pkg + names[1])).speed(speed, speed * 0.9).design(design);
            if (zones != null) zone(sim, zones, 1);
            AutoSim.Result r = sim.write(new File(TeamCodeDir.simLogs(), "alliance-study-" + seed + ".wpilog"));
            System.out.println(r);
            for (int i = 0; i < r.autoTips(); i++) {
                sum[i] += r.tipsAt.get(i);
                count[i]++;
            }
            points += r.autoPoints();
            if (!Double.isNaN(r.robotsCollidedAt)) problems++;
            for (AutoSim.RobotResult robot : r.robots) {
                if (robot.leave && robot.park) parked++;
                if (!Double.isNaN(robot.crossedAt) || !Double.isNaN(robot.hitHiveAt)) problems++;
            }
        }
        StringBuilder tips = new StringBuilder();
        for (int i = 0; i < 10 && count[i] > 0; i++) {
            tips.append(String.format(Locale.ROOT, "  TIP %d: %d/%d at %.1f s", i + 1, count[i], runs, sum[i] / count[i]));
        }
        System.out.printf(Locale.ROOT, "%s at %.0f in/s:%s; %.1f AUTO points; LEAVE+PARK %d/%d; runs with problems %d%n",
                pair, speed, tips, points / (double) runs, parked, 2 * runs, problems);
    }

    private static void zone(AutoSim sim, String zones, int robot) {
        String[] z = zones.split(";")[robot].split(",");
        sim.collectZone(Double.parseDouble(z[0]), Double.parseDouble(z[1]));
    }
}
