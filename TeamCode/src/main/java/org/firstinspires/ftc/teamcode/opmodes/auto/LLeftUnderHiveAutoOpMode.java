package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.LLeftUnderHiveNectarAuto;

/** Runs {@link LLeftUnderHiveNectarAuto}; see its {@code SOURCE} for the drawing. One half of the LeftUnderHive pair (L goes under the HIVE): run it only beside R-LeftUnderHive. */
@Autonomous(name = "L-LeftUnderHive NECTAR TRIAL " + LLeftUnderHiveNectarAuto.SAVED, group = "Auto")
public class LLeftUnderHiveAutoOpMode extends BuiltAuto {
    public LLeftUnderHiveAutoOpMode() {
        super(LLeftUnderHiveNectarAuto.SOURCE, LLeftUnderHiveNectarAuto.COMMANDS, LLeftUnderHiveNectarAuto.TRIGGERS, LLeftUnderHiveNectarAuto.DRAWN_FOR,
                LLeftUnderHiveNectarAuto::startPose, LLeftUnderHiveNectarAuto::build, LLeftUnderHiveNectarAuto.OPTIONS);
    }
}
