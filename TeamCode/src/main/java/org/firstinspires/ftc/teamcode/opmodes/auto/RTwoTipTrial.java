package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Utility;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RTwoTipAuto;

/** Runs the {@link RTwoTipAuto} draft from the Utility menu, with the same thirty-second stop as an AUTO period. */
@Utility(name = "Drive: R-Two-Tip " + RTwoTipAuto.SAVED,
        description = "Draft: R-Solo's steps, LEFT volleys from LEFT_LAUNCH_CATCH; 30 s trial")
public final class RTwoTipTrial extends BuiltAuto {
    private long startedNs;

    public RTwoTipTrial() {
        super(RTwoTipAuto.SOURCE, RTwoTipAuto.COMMANDS, RTwoTipAuto.TRIGGERS, RTwoTipAuto.DRAWN_FOR,
                RTwoTipAuto::startPose, RTwoTipAuto::build, RTwoTipAuto.OPTIONS);
    }

    @Override protected void onInitLoop() {
        super.onInitLoop();
        display.line("DRAFT: normal RIGHT start, four preloads; nothing on LEFT_START; automatic STOP after 30 s.");
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
