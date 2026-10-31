package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RQualsAuto;

/** Runs {@link RQualsAuto}; see its {@code SOURCE} for the drawing. */
@Autonomous(name = "R-Quals " + RQualsAuto.SAVED, group = "Auto")
public class RQualsAutoOpMode extends BuiltAuto {
    public RQualsAutoOpMode() {
        super(RQualsAuto.SOURCE, RQualsAuto.COMMANDS, RQualsAuto.TRIGGERS, RQualsAuto.DRAWN_FOR,
                RQualsAuto::startPose, RQualsAuto::build);
    }
}
