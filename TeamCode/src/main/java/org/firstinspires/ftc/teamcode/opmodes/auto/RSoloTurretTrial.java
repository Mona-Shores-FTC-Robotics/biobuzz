package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Utility;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RSoloTurretAuto;
import org.firstinspires.ftc.teamcode.subsystems.TurretSubsystem;

/**
 * Runs the {@link RSoloTurretAuto} draft from the Utility menu, with the same thirty-second stop
 * as an AUTO period. Drawn for a turret that aims ({@code RSoloTurretAuto.TURRET_AIMS}): it tracks
 * our CELL for the whole Auto and its launches wait for the turret, so the robot fires facing the
 * wall. On a robot whose turret is locked it does not move.
 */
@Utility(name = "Drive: R-Solo-Turret " + RSoloTurretAuto.SAVED,
        description = "Draft: R-Solo with the turret aiming, nose-first, no CELL checks; turret robots; 30 s trial")
public final class RSoloTurretTrial extends BuiltAuto {
    private long startedNs;

    public RSoloTurretTrial() {
        super(RSoloTurretAuto.SOURCE, RSoloTurretAuto.COMMANDS, RSoloTurretAuto.TRIGGERS, RSoloTurretAuto.DRAWN_FOR,
                RSoloTurretAuto::startPose, RSoloTurretAuto::build, RSoloTurretAuto.OPTIONS);
    }

    @Override protected boolean needsTurret() {
        return RSoloTurretAuto.TURRET_AIMS;
    }

    @Override protected void onInitLoop() {
        super.onInitLoop();
        display.line("DRAFT: normal RIGHT start, four preloads, both FLOWERs loaded; automatic STOP after 30 s.");
        display.line("No CELL checks: it fires its LEFT volleys whether or not the LEFT CELL came up.");
        if (TurretSubsystem.lockedForward) {
            display.line("This robot's turret is locked forward: this Auto will not move.");
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
