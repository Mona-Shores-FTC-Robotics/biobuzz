package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Utility;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.ROutsideLaneShortAuto;

/** Runs the {@link ROutsideLaneShortAuto} draft from the Utility menu, with the same thirty-second stop as an AUTO period. */
@Utility(name = "Drive: R-OutsideLane-Short " + ROutsideLaneShortAuto.SAVED,
        description = "Draft: wall lane to the LEFT volley, then park; 30 s trial")
public final class ROutsideLaneShortTrial extends BuiltAuto {
    private long startedNs;

    public ROutsideLaneShortTrial() {
        super(ROutsideLaneShortAuto.SOURCE, ROutsideLaneShortAuto.COMMANDS, ROutsideLaneShortAuto.TRIGGERS, ROutsideLaneShortAuto.DRAWN_FOR,
                ROutsideLaneShortAuto::startPose, ROutsideLaneShortAuto::build, ROutsideLaneShortAuto.OPTIONS);
    }

    @Override protected void onInitLoop() {
        super.onInitLoop();
        display.line("DRAFT: normal RIGHT start, four preloads; parks after its LEFT volley. Automatic STOP after 30 s.");
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
