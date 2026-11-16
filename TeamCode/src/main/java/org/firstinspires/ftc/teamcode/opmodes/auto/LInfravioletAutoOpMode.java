package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Utility;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.LInfravioletAuto;

/**
 * Runs {@link LInfravioletAuto} from the Utility menu, with the same thirty-second stop as an AUTO period.
 * One half of the Infraviolet pair, which the Clockwise pair replaces on the Autonomous menu (mentor, 14 Nov 2026).
 */
@Utility(name = "Drive: L-Infraviolet " + LInfravioletAuto.SAVED,
        description = "The left half of the Infraviolet pair, replaced by L-Clockwise; run only beside its other half; 30 s trial")
public class LInfravioletAutoOpMode extends BuiltAuto {
    private long startedNs;

    public LInfravioletAutoOpMode() {
        super(LInfravioletAuto.SOURCE, LInfravioletAuto.COMMANDS, LInfravioletAuto.TRIGGERS, LInfravioletAuto.DRAWN_FOR,
                LInfravioletAuto::startPose, LInfravioletAuto::build, LInfravioletAuto.OPTIONS);
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
