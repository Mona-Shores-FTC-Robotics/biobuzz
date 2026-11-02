package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Utility;

/**
 * {@link BasicDriveTeleOp}, driving robot-centric: pushing the stick away drives the way the robot
 * faces, whatever the Pinpoint says. For a match where the Pinpoint or its heading cannot be
 * trusted. Choosing it is the drive team's call before INIT; there is no button that switches a
 * running TeleOp between the two, so nobody can switch it by accident mid-match.
 *
 * <p>It is in the Utility menu, not the TeleOp list, so the TeleOp list holds only Basic Drive and
 * nobody picks this one by mistake: a drive team has to go looking for it. A missing Pinpoint does
 * not need it, since {@code DriveSubsystem} already drives robot-centric without one; it is for a
 * Pinpoint that is there but whose heading is wrong.
 */
@Utility(name = "Basic Drive (robot-centric)",
        description = "Basic Drive, robot-centric: for a match where the Pinpoint's heading cannot be trusted")
public class RobotCentricTeleOp extends BasicDriveTeleOp {

    @Override
    protected boolean robotCentric() {
        return true;
    }
}
