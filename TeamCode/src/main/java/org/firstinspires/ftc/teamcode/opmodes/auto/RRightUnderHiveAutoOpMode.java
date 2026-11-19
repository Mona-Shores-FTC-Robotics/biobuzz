package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RRightUnderHiveAuto;

/** Runs {@link RRightUnderHiveAuto}; see its {@code SOURCE} for the drawing. One half of the RightUnderHive pair: run it only beside L-RightUnderHive. */
@Autonomous(name = "R-RightUnderHive " + RRightUnderHiveAuto.SAVED, group = "Auto")
public class RRightUnderHiveAutoOpMode extends BuiltAuto {
    public RRightUnderHiveAutoOpMode() {
        super(RRightUnderHiveAuto.SOURCE, RRightUnderHiveAuto.COMMANDS, RRightUnderHiveAuto.TRIGGERS, RRightUnderHiveAuto.DRAWN_FOR,
                RRightUnderHiveAuto::startPose, RRightUnderHiveAuto::build, RRightUnderHiveAuto.OPTIONS);
    }
}
