package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.LLeftUnderHiveAuto;

/** Runs {@link LLeftUnderHiveAuto}; see its {@code SOURCE} for the drawing. One half of the LeftUnderHive pair (L goes under the HIVE): run it only beside R-LeftUnderHive. */
@Autonomous(name = "L-LeftUnderHive " + LLeftUnderHiveAuto.SAVED, group = "Auto")
public class LLeftUnderHiveAutoOpMode extends BuiltAuto {
    public LLeftUnderHiveAutoOpMode() {
        super(LLeftUnderHiveAuto.SOURCE, LLeftUnderHiveAuto.COMMANDS, LLeftUnderHiveAuto.TRIGGERS, LLeftUnderHiveAuto.DRAWN_FOR,
                LLeftUnderHiveAuto::startPose, LLeftUnderHiveAuto::build, LLeftUnderHiveAuto.OPTIONS);
    }
}
