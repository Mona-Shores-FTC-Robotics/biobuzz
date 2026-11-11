package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RClockwiseAuto;

/** Runs {@link RClockwiseAuto}; see its {@code SOURCE} for the drawing. One half of the Clockwise pair: run it only beside L-Clockwise. */
@Autonomous(name = "R-Clockwise " + RClockwiseAuto.SAVED, group = "Auto")
public class RClockwiseAutoOpMode extends BuiltAuto {
    public RClockwiseAutoOpMode() {
        super(RClockwiseAuto.SOURCE, RClockwiseAuto.COMMANDS, RClockwiseAuto.TRIGGERS, RClockwiseAuto.DRAWN_FOR,
                RClockwiseAuto::startPose, RClockwiseAuto::build, RClockwiseAuto.OPTIONS);
    }
}
