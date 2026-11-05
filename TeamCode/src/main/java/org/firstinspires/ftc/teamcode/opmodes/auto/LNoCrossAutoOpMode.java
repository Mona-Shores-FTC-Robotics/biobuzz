package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.LNoCrossAuto;

/** Runs {@link LNoCrossAuto}; see its {@code SOURCE} for the drawing. */
@Autonomous(name = "L-NoCross " + LNoCrossAuto.SAVED, group = "Auto")
public class LNoCrossAutoOpMode extends BuiltAuto {
    public LNoCrossAutoOpMode() {
        super(LNoCrossAuto.SOURCE, LNoCrossAuto.COMMANDS, LNoCrossAuto.TRIGGERS, LNoCrossAuto.DRAWN_FOR,
                LNoCrossAuto::startPose, LNoCrossAuto::build, LNoCrossAuto.OPTIONS);
    }
}
