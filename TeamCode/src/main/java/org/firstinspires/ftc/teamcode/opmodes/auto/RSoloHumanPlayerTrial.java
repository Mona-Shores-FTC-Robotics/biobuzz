package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Utility;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RSoloHumanPlayerAuto;

/** Runs the {@link RSoloHumanPlayerAuto} draft from the Utility menu, with the same thirty-second stop as an AUTO period. */
@Utility(name = "Drive: R-Solo-HumanPlayer " + RSoloHumanPlayerAuto.SAVED,
        description = "Draft: R-Two-Tip with a stop by the LOADING ZONE for the human player's NECTAR; 30 s trial")
public final class RSoloHumanPlayerTrial extends BuiltAuto {
    private long startedNs;

    public RSoloHumanPlayerTrial() {
        super(RSoloHumanPlayerAuto.SOURCE, RSoloHumanPlayerAuto.COMMANDS, RSoloHumanPlayerAuto.TRIGGERS, RSoloHumanPlayerAuto.DRAWN_FOR,
                RSoloHumanPlayerAuto::startPose, RSoloHumanPlayerAuto::build, RSoloHumanPlayerAuto.OPTIONS);
    }

    @Override protected void onInitLoop() {
        super.onInitLoop();
        display.line("DRAFT: RIGHT start, four preloads; nothing on LEFT_START or in the LOADING ZONE; STOP after 30 s.");
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
