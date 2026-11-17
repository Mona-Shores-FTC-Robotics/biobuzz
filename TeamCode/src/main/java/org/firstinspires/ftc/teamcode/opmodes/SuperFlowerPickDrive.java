package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Utility;

import org.firstinspires.ftc.teamcode.controls.ControlScheme;
import org.firstinspires.ftc.teamcode.subsystems.TurretSubsystem;

/**
 * {@link BasicDriveTeleOp} with one more button, on trial: the super FLOWER pick, on the driver's
 * R1, held ({@link FlowerVolley}, the button's way). From anywhere within reach of the SCORING
 * FLOWER it lines up, fires the load the robot arrived with from against the FLOWER with the
 * turret turned back at the CELL, shuts the gate, stays for the FLOWER's pieces and backs out
 * holding them. {@code TeleOpConfig.superPickFiresFlowerLoad} fires all eight instead. Everything
 * else is Basic Drive, both gamepads.
 *
 * <p>In the Utility menu so the match TeleOp, its Controls page and the cheat sheet stay as they
 * are until the button has earned its place. It needs a turret that aims: on a robot whose turret
 * is locked forward R1 is not bound and this is plain Basic Drive.
 *
 * <p>What to look for is in {@code doc/robot-todo.md}: whether the turret can shoot back over the
 * robot at all, and whether a FLOWER piece is ever aboard before the first ball has left (G407).
 */
@Utility(name = "Drive: Super FLOWER Pick",
        description = "Basic Drive plus R1 (driver, hold): fire the load from the SCORING FLOWER, then pick it; turret robots")
public class SuperFlowerPickDrive extends BasicDriveTeleOp {

    @Override
    protected boolean superFlowerPick() {
        return true;
    }

    @Override
    protected void onInitLoop() {
        super.onInitLoop();
        display.line(TurretSubsystem.lockedForward
                ? "Super FLOWER pick: this robot's turret is locked forward, so R1 does nothing here."
                : "Super FLOWER pick on the driver's " + ControlScheme.SUPER_FLOWER_PICK_INPUT.label
                        + " (hold), near the SCORING FLOWER with a load aboard.");
    }
}
