package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RQualsAuto;

/** Runs {@link RQualsAuto}; see its {@code SOURCE} for the drawing. */
@Autonomous(name = "Auto: R-Quals", group = "Auto")
public class RQualsAutoOpMode extends BuiltAuto {
    public RQualsAutoOpMode() {
        super(RQualsAuto.COMMANDS, RQualsAuto.TRIGGERS, RQualsAuto.DRAWN_FOR,
                RQualsAuto::startPose, RQualsAuto::build);
    }
}
