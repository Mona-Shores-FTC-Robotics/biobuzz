package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.LQualsAuto;

/** Runs {@link LQualsAuto}; see its {@code SOURCE} for the drawing. */
@Autonomous(name = "Auto: L-Quals", group = "Auto")
public class LQualsAutoOpMode extends BuiltAuto {
    public LQualsAutoOpMode() {
        super(LQualsAuto.SOURCE, LQualsAuto.COMMANDS, LQualsAuto.TRIGGERS, LQualsAuto.DRAWN_FOR,
                LQualsAuto::startPose, LQualsAuto::build);
    }
}
