package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.LLaunchParkAuto;

/** Runs {@link LLaunchParkAuto}; see its {@code SOURCE} for the drawing. */
@Autonomous(name = "L-Launch-Park " + LLaunchParkAuto.SAVED, group = "Auto")
public class LLaunchParkAutoOpMode extends BuiltAuto {
    public LLaunchParkAutoOpMode() {
        super(LLaunchParkAuto.SOURCE, LLaunchParkAuto.COMMANDS, LLaunchParkAuto.TRIGGERS, LLaunchParkAuto.DRAWN_FOR,
                LLaunchParkAuto::startPose, LLaunchParkAuto::build);
    }
}
