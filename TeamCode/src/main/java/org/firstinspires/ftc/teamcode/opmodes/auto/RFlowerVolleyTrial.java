package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Utility;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RFlowerVolleyAuto;
import org.firstinspires.ftc.teamcode.subsystems.TurretSubsystem;

/**
 * Runs the {@link RFlowerVolleyAuto} draft from the Utility menu, with the same thirty-second stop
 * as an AUTO period. For a robot whose turret aims: on one whose turret is locked the
 * {@code PickFlowerLaunch} card refuses, and the robot parks with its WALL FLOWER load.
 */
@Utility(name = "Drive: R-Flower-Volley " + RFlowerVolleyAuto.SAVED,
        description = "Draft: preloads, WALL FLOWER, then eight in a row from the SCORING FLOWER; turret robots; 30 s trial")
public final class RFlowerVolleyTrial extends BuiltAuto {
    private long startedNs;

    public RFlowerVolleyTrial() {
        super(RFlowerVolleyAuto.SOURCE, RFlowerVolleyAuto.COMMANDS, RFlowerVolleyAuto.TRIGGERS, RFlowerVolleyAuto.DRAWN_FOR,
                RFlowerVolleyAuto::startPose, RFlowerVolleyAuto::build, RFlowerVolleyAuto.OPTIONS);
    }

    @Override protected void onInitLoop() {
        super.onInitLoop();
        display.line("DRAFT: normal RIGHT start, four preloads, both FLOWERs loaded; automatic STOP after 30 s.");
        if (TurretSubsystem.lockedForward) {
            display.line("This robot's turret is locked forward: it will not fire from the FLOWER, and parks with its load.");
        }
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
