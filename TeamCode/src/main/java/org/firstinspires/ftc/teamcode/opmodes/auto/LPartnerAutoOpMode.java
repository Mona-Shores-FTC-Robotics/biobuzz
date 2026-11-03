package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.LPartnerAuto;

/** Runs {@link LPartnerAuto}; see its {@code SOURCE} for the drawing. */
@Autonomous(name = "L-Partner " + LPartnerAuto.SAVED, group = "Auto")
public class LPartnerAutoOpMode extends BuiltAuto {
    public LPartnerAutoOpMode() {
        super(LPartnerAuto.SOURCE, LPartnerAuto.COMMANDS, LPartnerAuto.TRIGGERS, LPartnerAuto.DRAWN_FOR,
                LPartnerAuto::startPose, LPartnerAuto::build, LPartnerAuto.OPTIONS);
    }
}
