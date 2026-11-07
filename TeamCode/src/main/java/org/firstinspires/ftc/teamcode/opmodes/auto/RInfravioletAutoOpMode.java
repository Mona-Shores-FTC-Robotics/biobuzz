package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RInfravioletAuto;

/** Runs {@link RInfravioletAuto}; see its {@code SOURCE} for the drawing. One half of the Infraviolet pair. */
@Autonomous(name = "R-Infraviolet " + RInfravioletAuto.SAVED, group = "Auto")
public class RInfravioletAutoOpMode extends BuiltAuto {
    public RInfravioletAutoOpMode() {
        super(RInfravioletAuto.SOURCE, RInfravioletAuto.COMMANDS, RInfravioletAuto.TRIGGERS, RInfravioletAuto.DRAWN_FOR,
                RInfravioletAuto::startPose, RInfravioletAuto::build, RInfravioletAuto.OPTIONS);
    }
}
