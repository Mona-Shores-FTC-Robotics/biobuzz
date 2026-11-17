package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.ROutsideLaneAuto;

/** Runs {@link ROutsideLaneAuto}; see its {@code SOURCE} for the drawing. One half of the OutsideLane / UnderHive pair: run it only beside L-UnderHive. */
@Autonomous(name = "R-OutsideLane " + ROutsideLaneAuto.SAVED, group = "Auto")
public class ROutsideLaneAutoOpMode extends BuiltAuto {
    public ROutsideLaneAutoOpMode() {
        super(ROutsideLaneAuto.SOURCE, ROutsideLaneAuto.COMMANDS, ROutsideLaneAuto.TRIGGERS, ROutsideLaneAuto.DRAWN_FOR,
                ROutsideLaneAuto::startPose, ROutsideLaneAuto::build, ROutsideLaneAuto.OPTIONS);
    }
}
