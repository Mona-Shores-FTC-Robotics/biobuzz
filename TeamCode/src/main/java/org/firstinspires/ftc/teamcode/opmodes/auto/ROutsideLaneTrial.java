package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Utility;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.ROutsideLaneAuto;

/** Runs the {@link ROutsideLaneAuto} draft from the Utility menu, with the same thirty-second stop as an AUTO period. */
@Utility(name = "Drive: R-OutsideLane " + ROutsideLaneAuto.SAVED,
        description = "Draft: wall lane to the LEFT volley; if done before 15.5 s, SCORING FLOWER and back to R5; 30 s trial")
public final class ROutsideLaneTrial extends BuiltAuto {
    private long startedNs;

    public ROutsideLaneTrial() {
        super(ROutsideLaneAuto.SOURCE, ROutsideLaneAuto.COMMANDS, ROutsideLaneAuto.TRIGGERS, ROutsideLaneAuto.DRAWN_FOR,
                ROutsideLaneAuto::startPose, ROutsideLaneAuto::build, ROutsideLaneAuto.OPTIONS);
    }

    @Override protected void onInitLoop() {
        super.onInitLoop();
        display.line("DRAFT: normal RIGHT start, four preloads. Parks after its LEFT volley unless that ends before 15.5 s; then R5, which overlaps L-UnderHive on R4. Pickup must be the other one from L-UnderHive's; leave it on WALL_FLOWER. STOP after 30 s.");
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
