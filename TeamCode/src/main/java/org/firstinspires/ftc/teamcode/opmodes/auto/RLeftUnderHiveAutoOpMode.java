package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RLeftUnderHiveAuto;

/** Runs {@link RLeftUnderHiveAuto}; see its {@code SOURCE} for the drawing. One half of the LeftUnderHive pair (L goes under the HIVE): run it only beside L-LeftUnderHive. */
@Autonomous(name = "R-LeftUnderHive " + RLeftUnderHiveAuto.SAVED, group = "Auto")
public class RLeftUnderHiveAutoOpMode extends BuiltAuto {
    public RLeftUnderHiveAutoOpMode() {
        super(RLeftUnderHiveAuto.SOURCE, RLeftUnderHiveAuto.COMMANDS, RLeftUnderHiveAuto.TRIGGERS, RLeftUnderHiveAuto.DRAWN_FOR,
                RLeftUnderHiveAuto::startPose, RLeftUnderHiveAuto::build, RLeftUnderHiveAuto.OPTIONS);
    }
}
