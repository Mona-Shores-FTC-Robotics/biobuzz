package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RSoloAuto;

/** Runs {@link RSoloAuto}; see its {@code SOURCE} for the drawing. */
@Autonomous(name = "R-Solo " + RSoloAuto.SAVED, group = "Auto")
public class RSoloAutoOpMode extends BuiltAuto {
    public RSoloAutoOpMode() {
        super(RSoloAuto.SOURCE, RSoloAuto.COMMANDS, RSoloAuto.TRIGGERS, RSoloAuto.DRAWN_FOR,
                RSoloAuto::startPose, RSoloAuto::build);
    }
}
