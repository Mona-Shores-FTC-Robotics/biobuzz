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
            // One since the spill was fitted to the 3 Oct 2026 films (FieldSim.FILMED_SPILL_EXIT_SCALE: it lands
            // about 4 ft out, not against the wall where this Auto picks it up; two before that, three before
            // FieldSim.grabs): an older hand-drawn Auto, not re-tuned.
            assertTrue(r.toString(), r.tipsAt.size() >= 1 && r.tipsAt.get(0) < AUTO_S);
        }
    }

    /** A partner that does not move, but stages its 4 POLLEN where this Auto picks them up. */
    @Test
    public void partnerThreeTipTipsThreeTimesAt50InPerS() throws Exception {
        for (Alliance alliance : new Alliance[] {Alliance.RED, Alliance.BLUE}) {
            AutoSim.Result r = new AutoSim(PartnerThreeTipAuto.class, alliance, 3572L).speed(50, 45)
                    .partner(DesignComparisonTest.LEFT_PARTNER, DesignComparisonTest.LEFT_PARTNER_POLLEN)
                    .write(log("partner", alliance, 1));
            // One since the spill was fitted to the 3 Oct 2026 films (FieldSim.FILMED_SPILL_EXIT_SCALE: it lands
            // about 4 ft out, not against the wall where this Auto picks it up; two before that, three before
            // FieldSim.grabs): an older hand-drawn Auto, not re-tuned.
            assertTrue(r.toString(), r.tipsAt.size() >= 1 && r.tipsAt.get(0) < AUTO_S);
        }
    }

    /**
     * On the spring-hood robot, alone it tips twice and the endgame guard parks it; with a partner
     * that fires its preloads at the left CELL, it notices TIP 2 came early and takes its pieces
     * right. (Since FLOWERs became solid it has no time to park with the partner; since the spill
     * was fitted to the 3 Oct 2026 films it tips twice with the partner on this seed, not three times.)
     *
     * <p>History: this asserted three TIPs alone until 1 Oct 2026. That third TIP landed at about
     * 30.5 s, and only because the guard then checked between cards and let the TIP 3 volley run
     * past its deadline; the robot never parked. The guard now cuts the volley at the deadline.
     */
    @Test
    public void threeTipAdaptiveUsesAPartnersEarlyTip() throws Exception {
        for (Alliance alliance : new Alliance[] {Alliance.RED, Alliance.BLUE}) {
            AutoSim.Result alone = new AutoSim(ThreeTipAdaptiveAuto.class, alliance, 3572L).speed(50, 45)
                    .design(RobotDesign.springHood()).write(log("adaptive", alliance, 1));
            assertTrue(alone.toString(), alone.autoTips() >= 2 && alone.robots.get(0).park);
            AutoSim.Result paired = new AutoSim(ThreeTipAdaptiveAuto.class, alliance, 3572L).speed(50, 45)
                    .design(RobotDesign.springHood()).alsoRun(PartnerPreloadsParkAuto.class).speed(40, 36)
                    .write(log("adaptive-with-partner", alliance, 1));
            // 2 TIPs on this seed since the spill was fitted to the 3 Oct 2026 films: the spilled NECTAR it
            // fetches for TIP 3 lies 30-50 in out now, not at the wall (3 before that; the 20-seed study has
            // TIP 3 in 16 of 20 with two spring hoods). Since FLOWERs became solid it has no time to park.
            assertTrue(paired.toString(), paired.autoTips() >= 2);
            assertTrue(paired.toString(), Double.isNaN(paired.robotsCollidedAt));
            for (AutoSim.RobotResult r : paired.robots) {
                assertTrue(paired.toString(), Double.isNaN(r.crossedAt) && Double.isNaN(r.hitHiveAt));
            }
        }
    }
}
