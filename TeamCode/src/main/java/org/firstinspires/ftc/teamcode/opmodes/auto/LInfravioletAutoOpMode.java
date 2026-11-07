package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.LInfravioletAuto;

/** Runs {@link LInfravioletAuto}; see its {@code SOURCE} for the drawing. One half of the Infraviolet pair. */
@Autonomous(name = "L-Infraviolet " + LInfravioletAuto.SAVED, group = "Auto")
public class LInfravioletAutoOpMode extends BuiltAuto {
    public LInfravioletAutoOpMode() {
        super(LInfravioletAuto.SOURCE, LInfravioletAuto.COMMANDS, LInfravioletAuto.TRIGGERS, LInfravioletAuto.DRAWN_FOR,
                LInfravioletAuto::startPose, LInfravioletAuto::build, LInfravioletAuto.OPTIONS);
    }
}
