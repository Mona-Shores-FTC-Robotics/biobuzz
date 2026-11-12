package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Utility;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.LUnderHiveAuto;

/** Runs the {@link LUnderHiveAuto} draft from the Utility menu, with the same thirty-second stop as an AUTO period. */
@Utility(name = "Drive: L-UnderHive " + LUnderHiveAuto.SAVED,
        description = "Draft: preloads from L4, under the HIVE to the GARDEN, volley from R4, back under; 30 s trial")
public final class LUnderHiveTrial extends BuiltAuto {
    private long startedNs;

    public LUnderHiveTrial() {
        super(LUnderHiveAuto.SOURCE, LUnderHiveAuto.COMMANDS, LUnderHiveAuto.TRIGGERS, LUnderHiveAuto.DRAWN_FOR,
                LUnderHiveAuto::startPose, LUnderHiveAuto::build, LUnderHiveAuto.OPTIONS);
    }

    @Override protected void onInitLoop() {
        super.onInitLoop();
        display.line("DRAFT: normal LEFT start, four preloads; needs a partner that tips the RIGHT CELL by 9 s. Its right spot (R4) overlaps R5. Automatic STOP after 30 s.");
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
