package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.LRightUnderHiveAuto;

/** Runs {@link LRightUnderHiveAuto}; see its {@code SOURCE} for the drawing. One half of the RightUnderHive pair (R goes under the HIVE): run it only beside R-RightUnderHive. */
@Autonomous(name = "L-RightUnderHive " + LRightUnderHiveAuto.SAVED, group = "Auto")
public class LRightUnderHiveAutoOpMode extends BuiltAuto {
    public LRightUnderHiveAutoOpMode() {
        super(LRightUnderHiveAuto.SOURCE, LRightUnderHiveAuto.COMMANDS, LRightUnderHiveAuto.TRIGGERS, LRightUnderHiveAuto.DRAWN_FOR,
                LRightUnderHiveAuto::startPose, LRightUnderHiveAuto::build, LRightUnderHiveAuto.OPTIONS);
    }
}
