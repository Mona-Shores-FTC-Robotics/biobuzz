package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.localization.StartPosition;
import org.firstinspires.ftc.teamcode.localization.StartPositions;
import org.firstinspires.ftc.teamcode.opmodes.RobotOpMode;

/**
 * The smallest Autonomous: it does not move. It exists to exercise everything around an
 * Autonomous — alliance selection, the start check once a start is declared, and the handoff to
 * TeleOp — before there is a real routine to test them with.
 *
 * <p>Run it, stop it, then init any TeleOp: the Match page should say "pose from Auto Ns ago" and
 * the alliance should arrive already chosen.
 *
 * <p>It declares {@link StartPositions#RED_AUDIENCE}, the one start measured so far, so INIT runs
 * the start check: place the robot there, and the Match page says whether the camera agrees. To
 * check another start once it is measured, point {@link #startPosition()} at it.
 */
@Autonomous(name = "Auto: Hold Still", group = "Auto")
public class HoldStillAuto extends RobotOpMode {

    @Override
    protected StartPosition startPosition() {
        return StartPositions.RED_AUDIENCE;
    }

    @Override
    protected void onLoop() {
        Pose pose = robot.drive.pose();
        if (pose != null) {
            display.status("Pose", robot.drive.poseReferenced() ? Display.Level.OK : Display.Level.WARN,
                    String.format(java.util.Locale.US, "(%.1f, %.1f, %.0f°)",
                            pose.x(), pose.y(), Math.toDegrees(pose.heading())));
        }
        display.line("Holding still. Stop, then init a TeleOp to test the handoff.");
    }
}
