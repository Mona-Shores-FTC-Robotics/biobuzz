package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RQualsLockedAuto;

/** Runs {@link RQualsLockedAuto}; see its {@code SOURCE} for the drawing. */
@Autonomous(name = "R-Quals-Locked " + RQualsLockedAuto.SAVED, group = "Auto")
public class RQualsLockedAutoOpMode extends BuiltAuto {
    public RQualsLockedAutoOpMode() {
        super(RQualsLockedAuto.SOURCE, RQualsLockedAuto.COMMANDS, RQualsLockedAuto.TRIGGERS, RQualsLockedAuto.DRAWN_FOR,
                RQualsLockedAuto::startPose, RQualsLockedAuto::build);
    }
}
