package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Utility;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RInfravioletAuto;

/**
 * Runs {@link RInfravioletAuto} from the Utility menu, with the same thirty-second stop as an AUTO period.
 * One half of the Infraviolet pair, which the Clockwise pair replaces on the Autonomous menu (mentor, 14 Nov 2026).
 */
@Utility(name = "Drive: R-Infraviolet " + RInfravioletAuto.SAVED,
        description = "The right half of the Infraviolet pair, replaced by R-Clockwise; run only beside its other half; 30 s trial")
public class RInfravioletAutoOpMode extends BuiltAuto {
    private long startedNs;

    public RInfravioletAutoOpMode() {
        super(RInfravioletAuto.SOURCE, RInfravioletAuto.COMMANDS, RInfravioletAuto.TRIGGERS, RInfravioletAuto.DRAWN_FOR,
                RInfravioletAuto::startPose, RInfravioletAuto::build, RInfravioletAuto.OPTIONS);
    }

    @Override protected void onStart() {
        startedNs = System.nanoTime();
        super.onStart();
    }

    @Override protected void onLoop() {
        if (System.nanoTime() - startedNs >= 30_000_000_000L) {
            requestOpModeStop();
            return;
        }
        super.onLoop();
    }
}
