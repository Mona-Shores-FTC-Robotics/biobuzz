package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.SisterLeftFixedAuto;

/** Runs {@link SisterLeftFixedAuto}; see its {@code SOURCE} for the drawing. */
@Autonomous(name = "Auto: Sister left", group = "Auto")
public class SisterLeftFixedAutoOpMode extends BuiltAuto {
    public SisterLeftFixedAutoOpMode() {
        super(SisterLeftFixedAuto.COMMANDS, SisterLeftFixedAuto.TRIGGERS, SisterLeftFixedAuto.DRAWN_FOR,
                SisterLeftFixedAuto::startPose, SisterLeftFixedAuto::build);
    }
}
