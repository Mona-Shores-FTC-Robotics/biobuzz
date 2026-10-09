package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Simulates every Auto the Auto Builder has exported, for both alliances, and writes one
 * {@code .wpilog} each to {@code TeamCode/build/sim-logs/auto-<name>-<alliance>.wpilog}. Open one in
 * AdvantageScope as {@code TeamCode/README.md} describes ("Simulating an Auto") to watch the Auto
 * drive, launch and tip the HIVE.
 *
 * <pre>
 * ./gradlew :TeamCode:testDebugUnitTest --tests '*AutoSimTest*'
 * </pre>
 * It prints one line per run: shots launched and scored, when the HIVE tipped, and when the Auto
 * finished.
 */
public class AutoSimTest {

    /**
     * The CAD model's extractor pose in the logs matches its extractor_poses.json: stowed is 146 deg about the shaft
     * (the file's translation and quaternion), down is the identity, and the roller's pose is the identity throughout.
     */
    @Test
    public void cadExtractorPoseMatchesTheModelsFile() {
        double[] stowed = AutoSim.cadComponents(0, 0);
        double[] file = {0.52663, 0, 0.06759, 0.292372, 0, -0.956305, 0};
        for (int i = 0; i < 7; i++) assertEquals("stowed[" + i + "]", file[i], stowed[i], 1e-4);
        double[] down = AutoSim.cadComponents(1, 0);
        double[] identity = {0, 0, 0, 1, 0, 0, 0};
        assertEquals(7 * AutoSim.CAD_COMPONENTS, down.length);
        for (int i = 0; i < 7; i++) assertEquals("down[" + i + "]", identity[i], down[i], 1e-9);
        for (int i = 7; i < down.length; i++) {
            assertEquals("rest stowed[" + i + "]", identity[i % 7], stowed[i], 1e-9);
            assertEquals("rest down[" + i + "]", identity[i % 7], down[i], 1e-9);
        }
        // The turret turned a quarter left about its axis: the axis point stays put.
        double[] left = AutoSim.cadComponents(1, Math.PI / 2);
        double ax = AutoSim.TURRET_AXIS_X_M, ay = AutoSim.TURRET_AXIS_Y_M;
        // Apply the pose to the axis point (ax, ay): R (ax, ay) = (-ay, ax), plus the translation.
        assertEquals(ax, left[14] - ay, 1e-9);
        assertEquals(ay, left[15] + ax, 1e-9);
        assertEquals(Math.cos(Math.PI / 4), left[17], 1e-9);
        assertEquals(Math.sin(Math.PI / 4), left[20], 1e-9);
    }

    private static final String GENERATED = "org.firstinspires.ftc.teamcode.opmodes.auto.generated";

    /** Every exported Auto: each class in the generated package that says where it came from. */
    static List<Class<?>> exportedAutos() throws ClassNotFoundException {
        File dir = new File(TeamCodeDir.get(), "src/test/java/" + GENERATED.replace('.', '/'));
        File[] files = dir.listFiles((d, n) -> n.endsWith(".java") && !n.endsWith("Test.java"));
        List<Class<?>> autos = new ArrayList<>();
        if (files == null) return autos;
        java.util.Arrays.sort(files);
        for (File f : files) {
            Class<?> c = Class.forName(GENERATED + "." + f.getName().replace(".java", ""));
            try {
                c.getField("SOURCE");
                autos.add(c);
            } catch (NoSuchFieldException notAnAuto) {
                // a helper, not an export
            }
        }
        return autos;
    }

    @Test
    public void everyExportedAutoRunsInTheSimulationForBothAlliances() throws Exception {
        List<Class<?>> autos = exportedAutos();
        assertFalse("found no exported Autos", autos.isEmpty());
        double edge = AdvantageScopeFrame.PEDRO_FIELD_CENTER_IN * 2;
        for (Class<?> auto : autos) {
            for (Alliance alliance : new Alliance[] {Alliance.RED, Alliance.BLUE}) {
                File file = new File(TeamCodeDir.simLogs(),
                        "auto-" + AutoSim.name(auto) + "-" + alliance.name().toLowerCase() + ".wpilog");
                AutoSim sim = new AutoSim(auto, alliance, 3572L);
                // The one Auto drawn around a partner gets that partner, so its log shows the plan.
                if (AutoSim.name(auto).equals("partner-three-tip")) {
                    sim.partner(DesignComparisonTest.LEFT_PARTNER, DesignComparisonTest.LEFT_PARTNER_POLLEN);
                }
                AutoSim.Result result = sim.write(file);
                System.out.println(result);
                assertTrue(file.length() > 0);
                for (double[] p : result.poses) {
                    assertTrue(result + ": robot left the field at " + p[0] + ", " + p[1],
                            p[0] > -1 && p[0] < edge + 1 && p[1] > -1 && p[1] < edge + 1);
                }
                assertFalse(result + ": made no decisions", result.decisions.isEmpty());
                checkLog(file);
            }
        }
    }

    /**
     * RightStartTipAuto opens with "LaunchAll, and wait for Tip": its 4 preloads land in the raised
     * CELL, which already holds 3 NECTAR, so the 3rd tips the HIVE (Event Field Setup Guide §12.3) and
     * the Auto takes its "tipped" route.
     */
    @Test
    public void rightStartTipAutoTipsTheHiveWithItsPreloads() throws Exception {
        Class<?> auto = Class.forName(GENERATED + ".RightStartTipAuto");
        for (Alliance alliance : new Alliance[] {Alliance.RED, Alliance.BLUE}) {
            AutoSim.Result result = new AutoSim(auto, alliance, 3572L).write(
                    new File(TeamCodeDir.simLogs(), "auto-check-" + alliance.name().toLowerCase() + ".wpilog"));
            assertFalse(result.toString(), result.tipsAt.isEmpty());
            assertTrue(result.toString(), result.tipsAt.get(0) < 6.0);
            assertTrue(result.toString(), result.decisions.stream().anyMatch(d -> d.startsWith("Did the HIVE tip?: Tip")));
            assertTrue(result.toString(), result.scored >= 3);
        }
    }

    /**
     * With the camera down ({@link AutoSim#cameraDown}) L-Quals cannot see TIP 1, so its wait for it runs to
     * its 7 s limit and the Auto takes its no-partner branch, firing its own preloads; the HIVE still tips when
     * those land, but no decision of the robot's names a CELL it saw.
     */
    @Test
    public void aRobotWithItsCameraDownWaitsOutEveryHiveTrigger() throws Exception {
        Class<?> auto = Class.forName(GENERATED + ".LQualsAuto");
        AutoSim.Result seeing = new AutoSim(auto, Alliance.RED, 3572L).write(
                new File(TeamCodeDir.simLogs(), "auto-camera-on.wpilog"));
        AutoSim.Result blind = new AutoSim(auto, Alliance.RED, 3572L).cameraDown().write(
                new File(TeamCodeDir.simLogs(), "auto-camera-down.wpilog"));
        assertTrue(blind.toString(), blind.robots.get(0).cameraDown);
        assertFalse(seeing.toString(), seeing.robots.get(0).cameraDown);
        for (String d : blind.decisions) {
            assertFalse("a blind robot saw the HIVE: " + d, d.contains(": LeftCellUp") || d.contains(": RightCellUp")
                    || d.contains(": Tip ") || d.endsWith(": Tip"));
        }
        assertTrue(blind.toString(), blind.decisions.stream().anyMatch(d -> d.contains("ms passed")));
        assertTrue("the shots still tip the HIVE: " + blind, !blind.tipsAt.isEmpty());
    }

    /**
     * The turret slews ({@link RobotDesign#turretSlewRadPerS}): Backup-L fires from spots well off the CELL's
     * bearing, so a turret that barely turns never gets within 2 deg and fires nothing, while the servo's 240 deg/s
     * is close to aiming at once. (Seat fire is no test: seated at the FLOWER the turret is already on the CELL.)
     */
    @Test
    public void aSlowTurretHoldsItsShotsUntilItIsAimed() throws Exception {
        Class<?> auto = Class.forName(GENERATED + ".BackupLAuto");
        double was = AutoSim.turretSlewOverrideRadPerS;
        try {
            AutoSim.turretSlewOverrideRadPerS = 0;  // at once
            int atOnce = new AutoSim(auto, Alliance.RED, 3572L).write(new File(TeamCodeDir.simLogs(), "turret-at-once.wpilog")).launched;
            AutoSim.turretSlewOverrideRadPerS = Math.toRadians(RobotDesign.TURRET_SERVO_DEG_PER_S);
            int servo = new AutoSim(auto, Alliance.RED, 3572L).write(new File(TeamCodeDir.simLogs(), "turret-servo.wpilog")).launched;
            AutoSim.turretSlewOverrideRadPerS = Math.toRadians(0.5);  // barely turns
            int stuck = new AutoSim(auto, Alliance.RED, 3572L).write(new File(TeamCodeDir.simLogs(), "turret-stuck.wpilog")).launched;
            assertTrue("at once " + atOnce + ", stuck " + stuck, stuck < atOnce / 2);
            assertTrue("at once " + atOnce + ", servo " + servo, servo >= atOnce - 2);
        } finally {
            AutoSim.turretSlewOverrideRadPerS = was;
        }
    }

    private static void checkLog(File file) throws IOException {
        WpiLogReader r = new WpiLogReader(Files.readAllBytes(file.toPath()));
        assertEquals("struct:Pose3d[]", r.entry(FieldSimLog.KEY_POLLEN).type);
        assertEquals("struct:Pose3d[]", r.entry(FieldSimLog.KEY_HIVE_COMPONENTS).type);
        assertEquals("struct:Pose3d", r.entry("/Odometry/Robot3d").type);
        assertEquals("struct:Pose2d[]", r.entry("/Path/Active").type);
        long last = -1;
        for (WpiLogReader.Record e : r.entry(AdvantageScopeKeys.EVENTS).records) {
            assertTrue("AdvantageScope drops events that share a timestamp", e.timestampUs > last);
            last = e.timestampUs;
        }
    }
}
