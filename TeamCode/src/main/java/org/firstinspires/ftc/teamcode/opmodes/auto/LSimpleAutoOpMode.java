package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.LSimpleAuto;

/** Runs {@link LSimpleAuto}; see its {@code SOURCE} for the drawing. */
@Autonomous(name = "Auto: L-Simple", group = "Auto")
public class LSimpleAutoOpMode extends BuiltAuto {
    public LSimpleAutoOpMode() {
        super(LSimpleAuto.SOURCE, LSimpleAuto.COMMANDS, LSimpleAuto.TRIGGERS, LSimpleAuto.DRAWN_FOR,
                LSimpleAuto::startPose, LSimpleAuto::build);
    }
}
