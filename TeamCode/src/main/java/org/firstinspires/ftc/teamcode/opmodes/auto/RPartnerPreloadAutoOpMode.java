package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RPartnerPreloadAuto;

/** Runs {@link RPartnerPreloadAuto}; see its {@code SOURCE} for the drawing. */
@Autonomous(name = "R-Partner-Preload " + RPartnerPreloadAuto.SAVED, group = "Auto")
public class RPartnerPreloadAutoOpMode extends BuiltAuto {
    public RPartnerPreloadAutoOpMode() {
        super(RPartnerPreloadAuto.SOURCE, RPartnerPreloadAuto.COMMANDS, RPartnerPreloadAuto.TRIGGERS,
                RPartnerPreloadAuto.DRAWN_FOR, RPartnerPreloadAuto::startPose, RPartnerPreloadAuto::build, RPartnerPreloadAuto.OPTIONS);
    }
}
