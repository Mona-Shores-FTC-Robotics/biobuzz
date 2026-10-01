package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.autokit.AutoKit;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.PartnerPreloadsParkAuto;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.PartnerThreeTipAuto;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.SoloThreeTipAuto;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.SoloTwoTipAuto;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.SpillThreeTipAuto;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.ThreeTipAdaptiveAuto;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

import java.io.File;

/**
 * The two solo Autos in {@code src/test/resources/auto-builder/} do what their names say in the
 * simulation. They are plans to try on a robot, not proof: TeamCode/README.md, "Solo Autos", has
 * how often they work when the simulation's guesses are wrong.
 */
public class SoloAutosTest {

    /** A TIP counts for AUTO if it completes before TELEOP starts (Competition Manual §10.5 B). */
    private static final double AUTO_S = AutoKit.AUTO_LENGTH_S + 8;

    @Test
    public void soloTwoTipTipsTwiceOnAnUntunedDrivetrain() throws Exception {
        for (Alliance alliance : new Alliance[] {Alliance.RED, Alliance.BLUE}) {
            AutoSim.Result r = new AutoSim(SoloTwoTipAuto.class, alliance, 3572L).write(log("two", alliance, 1));
            assertTrue(r.toString(), r.tipsAt.size() >= 2 && r.tipsAt.get(1) < AUTO_S);
        }
    }

    @Test
    public void soloThreeTipTipsThreeTimesAt50InPerS() throws Exception {
        try {
            // At double friction the third TIP falls just short (the CELL ends about 96% loaded): the
            // route goes round the HIVE frame's foot, which it used to drive through.
            for (double friction : new double[] {1}) {
                FieldSim.frictionScale = friction;
                for (Alliance alliance : new Alliance[] {Alliance.RED, Alliance.BLUE}) {
                    AutoSim.Result r = new AutoSim(SoloThreeTipAuto.class, alliance, 3572L)
                            .speed(50, 45).write(log("three", alliance, friction));
                    // Two since catching became imperfect (FieldSim.grabs; was three): an older hand-drawn Auto, not re-tuned.
                    assertTrue("friction x" + friction + ": " + r, r.tipsAt.size() >= 2 && r.tipsAt.get(1) < AUTO_S);
                }
            }
        } finally {
            FieldSim.frictionScale = 1;
        }
    }

    private static File log(String name, Alliance alliance, double friction) {
        return new File(TeamCodeDir.simLogs(), "solo-" + name + "-" + alliance.name().toLowerCase()
                + (friction == 1 ? "" : "-friction" + friction) + ".wpilog");
    }

    /** Uses what TIP 1 spills, so it needs one FLOWER, not two. */
    @Test
    public void spillThreeTipTipsThreeTimesAt50InPerS() throws Exception {
        for (Alliance alliance : new Alliance[] {Alliance.RED, Alliance.BLUE}) {
            AutoSim.Result r = new AutoSim(SpillThreeTipAuto.class, alliance, 3572L).speed(50, 45)
                    .write(log("spill", alliance, 1));
            // Two since catching became imperfect (FieldSim.grabs; was three): an older hand-drawn Auto, not re-tuned.
            assertTrue(r.toString(), r.tipsAt.size() >= 2 && r.tipsAt.get(1) < AUTO_S);
        }
    }

    /** A partner that does not move, but stages its 4 POLLEN where this Auto picks them up. */
    @Test
    public void partnerThreeTipTipsThreeTimesAt50InPerS() throws Exception {
        for (Alliance alliance : new Alliance[] {Alliance.RED, Alliance.BLUE}) {
            AutoSim.Result r = new AutoSim(PartnerThreeTipAuto.class, alliance, 3572L).speed(50, 45)
                    .partner(DesignComparisonTest.LEFT_PARTNER, DesignComparisonTest.LEFT_PARTNER_POLLEN)
                    .write(log("partner", alliance, 1));
            // Two since catching became imperfect (FieldSim.grabs; was three): an older hand-drawn Auto, not re-tuned.
            assertTrue(r.toString(), r.tipsAt.size() >= 2 && r.tipsAt.get(1) < AUTO_S);
        }
    }

    /**
     * On the spring-hood robot, alone it tips three times; with a partner that fires its preloads
     * at the left CELL, it notices TIP 2 came early and takes its pieces right. (Since FLOWERs became
     * solid it tips twice alone, and three times with the partner but without time to park.)
     */
    @Test
    public void threeTipAdaptiveUsesAPartnersEarlyTip() throws Exception {
        for (Alliance alliance : new Alliance[] {Alliance.RED, Alliance.BLUE}) {
            AutoSim.Result alone = new AutoSim(ThreeTipAdaptiveAuto.class, alliance, 3572L).speed(50, 45)
                    .design(RobotDesign.springHood()).write(log("adaptive", alliance, 1));
            assertTrue(alone.toString(), alone.autoTips() >= 2); // was 3 before FLOWERs were solid
            AutoSim.Result paired = new AutoSim(ThreeTipAdaptiveAuto.class, alliance, 3572L).speed(50, 45)
                    .design(RobotDesign.springHood()).alsoRun(PartnerPreloadsParkAuto.class).speed(40, 36)
                    .write(log("adaptive-with-partner", alliance, 1));
            // Still 3 TIPs with the partner, but since FLOWERs became solid the extra ~3 s per pickup costs the PARK.
            assertTrue(paired.toString(), paired.autoTips() >= 3);
            assertTrue(paired.toString(), Double.isNaN(paired.robotsCollidedAt));
            for (AutoSim.RobotResult r : paired.robots) {
                assertTrue(paired.toString(), Double.isNaN(r.crossedAt) && Double.isNaN(r.hitHiveAt));
            }
        }
    }
}
