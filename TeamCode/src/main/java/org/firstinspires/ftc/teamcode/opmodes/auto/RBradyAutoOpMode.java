package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RBradyAuto;

/** Runs {@link RBradyAuto}; see its {@code SOURCE} for the drawing. */
@Autonomous(name = "R-Brady " + RBradyAuto.SAVED, group = "Auto")
public class RBradyAutoOpMode extends BuiltAuto {
    public RBradyAutoOpMode() {
        super(RBradyAuto.SOURCE, RBradyAuto.COMMANDS, RBradyAuto.TRIGGERS, RBradyAuto.DRAWN_FOR,
                RBradyAuto::startPose, RBradyAuto::build);
    }
}
