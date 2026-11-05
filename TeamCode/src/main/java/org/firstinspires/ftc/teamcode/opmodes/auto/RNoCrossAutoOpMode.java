package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RNoCrossAuto;

/** Runs {@link RNoCrossAuto}; see its {@code SOURCE} for the drawing. */
@Autonomous(name = "R-NoCross " + RNoCrossAuto.SAVED, group = "Auto")
public class RNoCrossAutoOpMode extends BuiltAuto {
    public RNoCrossAutoOpMode() {
        super(RNoCrossAuto.SOURCE, RNoCrossAuto.COMMANDS, RNoCrossAuto.TRIGGERS, RNoCrossAuto.DRAWN_FOR,
                RNoCrossAuto::startPose, RNoCrossAuto::build, RNoCrossAuto.OPTIONS);
    }
}
