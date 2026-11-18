package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.LUnderHiveAuto;

/** Runs {@link LUnderHiveAuto}; see its {@code SOURCE} for the drawing. One half of the OutsideLane / UnderHive pair: run it only beside R-OutsideLane. */
@Autonomous(name = "L-UnderHive " + LUnderHiveAuto.SAVED, group = "Auto")
public class LUnderHiveAutoOpMode extends BuiltAuto {
    public LUnderHiveAutoOpMode() {
        super(LUnderHiveAuto.SOURCE, LUnderHiveAuto.COMMANDS, LUnderHiveAuto.TRIGGERS, LUnderHiveAuto.DRAWN_FOR,
                LUnderHiveAuto::startPose, LUnderHiveAuto::build, LUnderHiveAuto.OPTIONS);
    }
}
