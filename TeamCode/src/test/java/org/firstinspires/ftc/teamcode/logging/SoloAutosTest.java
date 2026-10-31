package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.autokit.AutoKit;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.PartnerThreeTipAuto;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.SpillThreeTipAuto;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

import java.io.File;

/**
 * Two of the simulator's own Autos still make a TIP on the real robot design. They are plans to try
 * on a robot, not proof.
 *
 * <p>Until 31 Oct 2026 this also held solo-two-tip, solo-three-tip and three-tip-adaptive to two or
 * three TIPs. Those were drawn for the 18 in what-if designs and make one on the real robot; the
 * checks went with those designs (git history has them).
 */
public class SoloAutosTest {

    /** A TIP counts for AUTO if it completes before TELEOP starts (Competition Manual §10.5 B). */
    private static final double AUTO_S = AutoKit.AUTO_LENGTH_S + 8;

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
            // At least one since the spill was fitted to the 3 Oct 2026 films (FieldSim.FILMED_SPILL_EXIT_SCALE:
            // it lands about 42 in out, not where this Auto picks it up; two before that, three before
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
            // At least one since the spill was fitted to the 3 Oct 2026 films (FieldSim.FILMED_SPILL_EXIT_SCALE:
            // it lands about 42 in out, not where this Auto picks it up; two before that, three before
            // FieldSim.grabs): an older hand-drawn Auto, not re-tuned.
            assertTrue(r.toString(), r.tipsAt.size() >= 1 && r.tipsAt.get(0) < AUTO_S);
        }
    }

}
