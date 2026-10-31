package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.SisterLeftFixedAuto;

/** Runs {@link SisterLeftFixedAuto}; see its {@code SOURCE} for the drawing. */
@Autonomous(name = "Sister left " + SisterLeftFixedAuto.SAVED, group = "Auto")
public class SisterLeftFixedAutoOpMode extends BuiltAuto {
    public SisterLeftFixedAutoOpMode() {
        super(SisterLeftFixedAuto.SOURCE, SisterLeftFixedAuto.COMMANDS, SisterLeftFixedAuto.TRIGGERS,
                SisterLeftFixedAuto.DRAWN_FOR, SisterLeftFixedAuto::startPose, SisterLeftFixedAuto::build);
    }
}
