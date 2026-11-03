package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RPartnerTipAuto;

/** Runs {@link RPartnerTipAuto}; see its {@code SOURCE} for the drawing. */
@Autonomous(name = "R-Partner-Tip " + RPartnerTipAuto.SAVED, group = "Auto")
public class RPartnerTipAutoOpMode extends BuiltAuto {
    public RPartnerTipAutoOpMode() {
        super(RPartnerTipAuto.SOURCE, RPartnerTipAuto.COMMANDS, RPartnerTipAuto.TRIGGERS, RPartnerTipAuto.DRAWN_FOR,
                RPartnerTipAuto::startPose, RPartnerTipAuto::build, RPartnerTipAuto.OPTIONS);
    }
}
