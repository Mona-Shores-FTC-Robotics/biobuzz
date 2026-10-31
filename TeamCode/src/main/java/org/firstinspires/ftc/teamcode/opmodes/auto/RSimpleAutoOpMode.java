package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RSimpleAuto;

/** Runs {@link RSimpleAuto}; see its {@code SOURCE} for the drawing. */
@Autonomous(name = "R-Simple " + RSimpleAuto.SAVED, group = "Auto")
public class RSimpleAutoOpMode extends BuiltAuto {
    public RSimpleAutoOpMode() {
        super(RSimpleAuto.SOURCE, RSimpleAuto.COMMANDS, RSimpleAuto.TRIGGERS, RSimpleAuto.DRAWN_FOR,
                RSimpleAuto::startPose, RSimpleAuto::build);
    }
}
