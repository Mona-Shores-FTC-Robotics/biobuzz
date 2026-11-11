package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Utility;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RTwoTipPairAuto;

/** Runs the {@link RTwoTipPairAuto} draft from the Utility menu, with the same thirty-second stop as an AUTO period. */
@Utility(name = "Drive: R-Two-Tip-Pair " + RTwoTipPairAuto.SAVED,
        description = "Draft: the right half beside L-NoCross; preloads, WALL FLOWER, one LEFT volley, early park; 30 s trial")
public final class RTwoTipPairTrial extends BuiltAuto {
    private long startedNs;

    public RTwoTipPairTrial() {
        super(RTwoTipPairAuto.SOURCE, RTwoTipPairAuto.COMMANDS, RTwoTipPairAuto.TRIGGERS, RTwoTipPairAuto.DRAWN_FOR,
                RTwoTipPairAuto::startPose, RTwoTipPairAuto::build, RTwoTipPairAuto.OPTIONS);
    }

    @Override protected void onInitLoop() {
        super.onInitLoop();
        display.line("DRAFT PAIR: the other robot runs L-NoCross; normal RIGHT start; automatic STOP after 30 s.");
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
