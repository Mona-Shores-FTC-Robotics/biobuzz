package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.autokit.AutoKit;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.PartnerThreeTipAuto;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.SoloThreeTipAuto;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.SoloTwoTipAuto;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.SpillThreeTipAuto;
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
            for (double friction : new double[] {1, 2}) {
                FieldSim.frictionScale = friction;
                for (Alliance alliance : new Alliance[] {Alliance.RED, Alliance.BLUE}) {
                    AutoSim.Result r = new AutoSim(SoloThreeTipAuto.class, alliance, 3572L)
                            .speed(50, 45).write(log("three", alliance, friction));
                    assertTrue("friction x" + friction + ": " + r, r.tipsAt.size() >= 3 && r.tipsAt.get(2) < AUTO_S);
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
            assertTrue(r.toString(), r.tipsAt.size() >= 3 && r.tipsAt.get(2) < AUTO_S);
        }
    }

    /** A partner that does not move, but stages its 4 POLLEN where this Auto picks them up. */
    @Test
    public void partnerThreeTipTipsThreeTimesAt50InPerS() throws Exception {
        for (Alliance alliance : new Alliance[] {Alliance.RED, Alliance.BLUE}) {
            AutoSim.Result r = new AutoSim(PartnerThreeTipAuto.class, alliance, 3572L).speed(50, 45)
                    .partner(DesignComparisonTest.NORTH_PARTNER, DesignComparisonTest.NORTH_PARTNER_POLLEN)
                    .write(log("partner", alliance, 1));
            assertTrue(r.toString(), r.tipsAt.size() >= 3 && r.tipsAt.get(2) < AUTO_S);
        }
    }
}
