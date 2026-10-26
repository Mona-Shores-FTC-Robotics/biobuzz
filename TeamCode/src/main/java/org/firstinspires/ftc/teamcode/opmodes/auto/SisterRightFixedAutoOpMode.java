package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.SisterRightFixedAuto;

/** Runs {@link SisterRightFixedAuto}; see its {@code SOURCE} for the drawing. */
@Autonomous(name = "Auto: Sister right", group = "Auto")
public class SisterRightFixedAutoOpMode extends BuiltAuto {
    public SisterRightFixedAutoOpMode() {
        super(SisterRightFixedAuto.COMMANDS, SisterRightFixedAuto.TRIGGERS, SisterRightFixedAuto.DRAWN_FOR,
                SisterRightFixedAuto::startPose, SisterRightFixedAuto::build);
    }
}
