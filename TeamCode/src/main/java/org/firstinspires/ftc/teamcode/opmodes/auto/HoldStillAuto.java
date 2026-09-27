package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.opmodes.RobotOpMode;

/**
 * The smallest Autonomous: it does not move. It exists to exercise everything around an
 * Autonomous — alliance selection, the start check once a start is declared, and the handoff to
 * TeleOp — before there is a real routine to test them with.
 *
 * <p>Run it, stop it, then init any TeleOp: the Match page should say "pose from Auto Ns ago" and
 * the alliance should arrive already chosen.
 *
 * <p>To check a start position, override {@code startPosition()} to return one from
 * {@code StartPositions} once they are measured.
 */
@Autonomous(name = "Auto: Hold Still", group = "Auto")
public class HoldStillAuto extends RobotOpMode {

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
