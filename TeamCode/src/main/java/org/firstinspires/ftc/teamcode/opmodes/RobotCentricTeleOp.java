package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

/**
 * {@link BasicDriveTeleOp}, driving robot-centric: pushing the stick away drives the way the robot
 * faces, whatever the Pinpoint says. For a match where the Pinpoint or its heading cannot be
 * trusted. Choosing it is the drive team's call before INIT; there is no button that switches a
 * running TeleOp between the two, so nobody can switch it by accident mid-match.
 */
@TeleOp(name = "Basic Drive (robot-centric)", group = "Drive")
public class RobotCentricTeleOp extends BasicDriveTeleOp {

    @Override
    protected boolean robotCentric() {
        return true;
    }
}
