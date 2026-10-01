package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.DuoLzLeftAuto;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.DuoLzRightAuto;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.LeanLeftAuto;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.LeanRightAuto;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.PartnerPreloadsParkAuto;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.SoloTwoTipAuto;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.ThreeTipAdaptiveAuto;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

/**
 * The best Autos, one log each, written to build/sim-logs/best/ for watching in AdvantageScope
 * (TeamCode/README.md, "The best Autos so far", lists them and what each assumes). Each two-robot
 * log is checked to show both robots: /Odometry/Robot3d, /Odometry/Partner3d, /Odometry/AllRobots3d
 * and "2 on the field" in the metadata.
 */
public class BestAutosTest {

    static final long SEED = 1;

    static RobotDesign twinCatcher() {
        return AutoStudyTest.designs().get("two spring hoods, 24 in catcher");
    }

    static File file(String name) {
        File dir = new File(TeamCodeDir.simLogs(), "best");
        dir.mkdirs();
        return new File(dir, name + ".wpilog");
    }

    static void assertTwoRobots(File f) throws Exception {
        WpiLogReader r = new WpiLogReader(Files.readAllBytes(f.toPath()));
        assertEquals("struct:Pose3d", r.entry("/Odometry/Robot3d").type);
        assertEquals("struct:Pose3d", r.entry("/Odometry/Partner3d").type);
        assertEquals("struct:Pose3d[]", r.entry(FieldRobot.ALL_3D).type);
        assertTrue(r.entry("/RealMetadata/Robots").records.get(0).asString().startsWith("2 on the field"));
    }

    @Test
    public void duoLz() throws Exception {
        for (boolean better : new boolean[] {false, true}) {
            RobotDesign d = better ? twinCatcher() : RobotDesign.springHood();
            File f = file("1-duo-lz_" + (better ? "two-spring-hoods-24in-catcher" : "spring-hood"));
            AutoSim.Result r = new AutoSim(DuoLzRightAuto.class, Alliance.RED, SEED).speed(50, 45).design(d)
                    .alsoRun(DuoLzLeftAuto.class).speed(50, 45).design(d).write(f);
            System.out.println("BEST " + f.getName() + ": " + r);
            assertTwoRobots(f);
            // FLOWERs became solid on 1 Oct 2026 (FieldSim.hitsFlower): picking one up honestly costs ~3 s, which
            // cost both robots a TIP on this seed (2, from 3-4); the better one still parks both.
            assertTrue(r.toString(), r.autoTips() >= 2);
            if (better) for (AutoSim.RobotResult robot : r.robots) assertTrue(r.toString(), robot.park);
        }
    }

    /** The stay-home pair on the robot with two launchers and a 24 in catcher. */
    @Test
    public void lean() throws Exception {
        File f = file("2-lean_two-spring-hoods-24in-catcher");
        AutoSim.Result r = new AutoSim(LeanRightAuto.class, Alliance.RED, SEED).speed(50, 45).design(twinCatcher())
                .alsoRun(LeanLeftAuto.class).speed(50, 45).design(twinCatcher()).write(f);
        System.out.println("BEST " + f.getName() + ": " + r);
        assertTwoRobots(f);
        assertTrue(r.toString(), r.autoTips() >= 3); // was 4 before FLOWERs were solid
    }

    @Test
    public void threeTipAdaptive() throws Exception {
        File alone = file("3-three-tip-adaptive_alone");
        AutoSim.Result a = new AutoSim(ThreeTipAdaptiveAuto.class, Alliance.RED, SEED).speed(50, 45)
                .design(RobotDesign.springHood()).write(alone);
        System.out.println("BEST " + alone.getName() + ": " + a);
        assertTrue(a.toString(), a.autoTips() >= 2); // was 3 before FLOWERs were solid
        File paired = file("4-three-tip-adaptive_with-preloads-partner");
        AutoSim.Result p = new AutoSim(ThreeTipAdaptiveAuto.class, Alliance.RED, SEED).speed(50, 45)
                .design(RobotDesign.springHood()).alsoRun(PartnerPreloadsParkAuto.class).speed(40, 36).write(paired);
        System.out.println("BEST " + paired.getName() + ": " + p);
        assertTwoRobots(paired);
        assertTrue(p.toString(), p.autoTips() >= 3);
    }

    @Test
    public void soloTwoTip() throws Exception {
        File f = file("5-solo-two-tip_40ips");
        AutoSim.Result r = new AutoSim(SoloTwoTipAuto.class, Alliance.RED, SEED).speed(40, 36)
                .design(RobotDesign.springHood()).write(f);
        System.out.println("BEST " + f.getName() + ": " + r);
        assertTrue(r.toString(), r.autoTips() >= 2 && r.robots.get(0).park);
    }
}
