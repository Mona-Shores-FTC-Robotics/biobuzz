package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RLaunchParkAuto;

/** Runs {@link RLaunchParkAuto}; see its {@code SOURCE} for the drawing. */
@Autonomous(name = "R-Launch-Park " + RLaunchParkAuto.SAVED, group = "Auto")
public class RLaunchParkAutoOpMode extends BuiltAuto {
    public RLaunchParkAutoOpMode() {
        super(RLaunchParkAuto.SOURCE, RLaunchParkAuto.COMMANDS, RLaunchParkAuto.TRIGGERS, RLaunchParkAuto.DRAWN_FOR,
                RLaunchParkAuto::startPose, RLaunchParkAuto::build);
    }
}
