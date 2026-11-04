package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RSoloStagingAuto;

/** Runs {@link RSoloStagingAuto}; see its {@code SOURCE} for the drawing. */
@Autonomous(name = "R-Solo-Staging " + RSoloStagingAuto.SAVED, group = "Auto")
public class RSoloStagingAutoOpMode extends BuiltAuto {
    public RSoloStagingAutoOpMode() {
        super(RSoloStagingAuto.SOURCE, RSoloStagingAuto.COMMANDS, RSoloStagingAuto.TRIGGERS,
                RSoloStagingAuto.DRAWN_FOR, RSoloStagingAuto::startPose, RSoloStagingAuto::build, RSoloStagingAuto.OPTIONS);
    }
}
