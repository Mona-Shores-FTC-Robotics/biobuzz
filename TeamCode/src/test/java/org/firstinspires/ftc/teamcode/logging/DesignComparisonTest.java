package org.firstinspires.ftc.teamcode.logging;

import org.firstinspires.ftc.teamcode.autokit.AutoKit;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.PartnerThreeTipAuto;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.SoloThreeTipAuto;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.SoloTwoTipAuto;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.SpillThreeTipAuto;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Runs the solo Autos on a set of robot designs and prints how many TIPs each gets: a desk tool for
 * "turret or fixed launcher?", "does it need to launch NECTAR?" and the like. Slow (a few minutes),
 * so it runs only when asked:
 *
 * <pre>
 * BIOBUZZ_DESIGN_STUDY=1 ./gradlew :TeamCode:testDebugUnitTest --tests '*DesignComparisonTest*' -i
 * </pre>
 * Set {@code BIOBUZZ_DESIGN_STUDY_SPREAD} or {@code _FRICTION} to scale the placeholder shot spread
 * or friction. TeamCode/README.md, "Robot design questions", has the results and what they rest on.
 */
public class DesignComparisonTest {

    /**
     * A partner that does not move, against the left wall west of the far FLOWER, its 4 POLLEN on
     * the tiles touching its front (Competition Manual §10.3.4, G304), drawn for RED.
     */
    static final double[] LEFT_PARTNER = {12, 132.5, Math.toRadians(270)};
    static final double[][] LEFT_PARTNER_POLLEN = {{7.8, 121.6}, {10.6, 121.6}, {13.4, 121.6}, {16.2, 121.6}};

    static final int RUNS = 10;

    static Map<String, RobotDesign> designs() {
        Map<String, RobotDesign> m = new LinkedHashMap<>();
        m.put("turret", RobotDesign.standard());
        m.put("fixed launcher", RobotDesign.fixedLauncher());
        m.put("catapult", RobotDesign.catapult());
        RobotDesign twin = RobotDesign.fixedLauncher().copy("two fixed launchers");
        twin.launchers = 2;
        m.put(twin.name, twin);
        RobotDesign fast = RobotDesign.standard().copy("turret, 0.25 s/shot");
        fast.shotIntervalS = 0.25;
        m.put(fast.name, fast);
        RobotDesign pollenOnly = RobotDesign.standard().copy("POLLEN only");
        pollenOnly.launchesNectar = false;
        m.put(pollenOnly.name, pollenOnly);
        RobotDesign shortNectar = RobotDesign.standard().copy("NECTAR 7% short");
        shortNectar.nectarSpeedFactor = 0.93;
        m.put(shortNectar.name, shortNectar);
        // The same launcher with its one setting between the two pieces instead of tuned for POLLEN.
        RobotDesign between = RobotDesign.standard().copy("NECTAR 7% slower, set between");
        between.pollenSpeedFactor = 1.035;
        between.nectarSpeedFactor = 0.965;
        m.put(between.name, between);
        RobotDesign close = RobotDesign.standard().copy("NECTAR 4% slower, set between");
        close.pollenSpeedFactor = 1.02;
        close.nectarSpeedFactor = 0.98;
        m.put(close.name, close);
        RobotDesign slowFlower = RobotDesign.standard().copy("1 s per FLOWER POLLEN");
        slowFlower.flowerPullS = 1.0;
        m.put(slowFlower.name, slowFlower);
        RobotDesign narrow = RobotDesign.standard().copy("8 in intake");
        narrow.intakeWidthIn = 8;
        m.put(narrow.name, narrow);
        return m;
    }

    static AutoSim.Result run(Class<?> auto, RobotDesign design, long seed, String file) throws Exception {
        AutoSim sim = new AutoSim(auto, Alliance.RED, seed).speed(50, 45).design(design);
        if (auto == PartnerThreeTipAuto.class) sim.partner(LEFT_PARTNER, LEFT_PARTNER_POLLEN);
        return sim.write(new File(TeamCodeDir.simLogs(), file));
    }

    /** TIPs that count for AUTO: complete before TELEOP starts (Competition Manual §10.5 B). */
    static int autoTips(AutoSim.Result r) {
        int n = 0;
        for (double t : r.tipsAt) if (t < AutoKit.AUTO_LENGTH_S + 8) n++;
        return n;
    }

    @Test
    public void compareDesigns() throws Exception {
        if (System.getenv("BIOBUZZ_DESIGN_STUDY") == null) return;
        String only = System.getenv("BIOBUZZ_DESIGN_STUDY_ONLY");
        String spread = System.getenv("BIOBUZZ_DESIGN_STUDY_SPREAD");
        String friction = System.getenv("BIOBUZZ_DESIGN_STUDY_FRICTION");
        FieldSim.spreadScale = spread == null ? 1 : Double.parseDouble(spread);
        FieldSim.frictionScale = friction == null ? 1 : Double.parseDouble(friction);
        try {
            List<Class<?>> autos = new ArrayList<>();
            autos.add(SoloTwoTipAuto.class);
            autos.add(SoloThreeTipAuto.class);
            autos.add(SpillThreeTipAuto.class);
            autos.add(PartnerThreeTipAuto.class);
            System.out.printf(Locale.ROOT, "Design study at 50 in/s, spread x%.1f, friction x%.1f, %d runs each%n",
                    FieldSim.spreadScale, FieldSim.frictionScale, RUNS);
            for (Class<?> auto : autos) {
                for (Map.Entry<String, RobotDesign> e : designs().entrySet()) {
                    if (only != null && !e.getKey().contains(only)) continue;
                    int[] count = new int[8];
                    double[] sum = new double[8];
                    for (long seed = 1; seed <= RUNS; seed++) {
                        AutoSim.Result r = run(auto, e.getValue(), seed, "design-study.wpilog");
                        int tips = autoTips(r);
                        for (int i = 0; i < tips; i++) {
                            count[i]++;
                            sum[i] += r.tipsAt.get(i);
                        }
                    }
                    StringBuilder line = new StringBuilder(String.format(Locale.ROOT, "%-18s %-22s",
                            AutoSim.name(auto), e.getKey()));
                    for (int i = 0; i < 3; i++) {
                        line.append(count[i] == 0 ? "   tip " + (i + 1) + ":  0/" + RUNS
                                : String.format(Locale.ROOT, "   tip %d: %2d/%d at %4.1f s", i + 1, count[i], RUNS,
                                sum[i] / count[i]));
                    }
                    System.out.println(line);
                }
            }
        } finally {
            FieldSim.spreadScale = 1;
            FieldSim.frictionScale = 1;
        }
    }
}
