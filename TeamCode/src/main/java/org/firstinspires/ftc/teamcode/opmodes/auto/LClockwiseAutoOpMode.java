package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.LClockwiseAuto;

/** Runs {@link LClockwiseAuto}; see its {@code SOURCE} for the drawing. One half of the Clockwise pair: run it only beside R-Clockwise. */
@Autonomous(name = "L-Clockwise " + LClockwiseAuto.SAVED, group = "Auto")
public class LClockwiseAutoOpMode extends BuiltAuto {
    public LClockwiseAutoOpMode() {
        super(LClockwiseAuto.SOURCE, LClockwiseAuto.COMMANDS, LClockwiseAuto.TRIGGERS, LClockwiseAuto.DRAWN_FOR,
                LClockwiseAuto::startPose, LClockwiseAuto::build, LClockwiseAuto.OPTIONS);
    }
}
