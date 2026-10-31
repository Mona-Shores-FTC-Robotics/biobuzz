package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RQualsPollenOnlyAuto;

/** Runs {@link RQualsPollenOnlyAuto}; see its {@code SOURCE} for the drawing. */
@Autonomous(name = "Auto: R-Quals-Pollen-Only", group = "Auto")
public class RQualsPollenOnlyAutoOpMode extends BuiltAuto {
    public RQualsPollenOnlyAutoOpMode() {
        super(RQualsPollenOnlyAuto.SOURCE, RQualsPollenOnlyAuto.COMMANDS, RQualsPollenOnlyAuto.TRIGGERS, RQualsPollenOnlyAuto.DRAWN_FOR,
                RQualsPollenOnlyAuto::startPose, RQualsPollenOnlyAuto::build);
    }
}
