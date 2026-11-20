package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Utility;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RClockwise19429Auto;

/**
 * Runs the {@link RClockwise19429Auto} draft from the Utility menu: R-Clockwise with the outside lane from the GARDEN
 * driven backward, on a robot with a bottom plate only ({@link BackwardLoadTrial}). Named for 19429, the first robot
 * with one. The other robot runs L-Clockwise beside it, unchanged.
 */
@Utility(name = "Drive: R-Clockwise-19429 " + RClockwise19429Auto.SAVED,
        description = "Draft, bottom plate only: R-Clockwise, GARDEN lane backward; 30 s trial")
public final class RClockwise19429Trial extends BackwardLoadTrial {
    public RClockwise19429Trial() {
        super(RClockwise19429Auto.SOURCE, RClockwise19429Auto.COMMANDS, RClockwise19429Auto.TRIGGERS,
                RClockwise19429Auto.DRAWN_FOR, RClockwise19429Auto::startPose, RClockwise19429Auto::build,
                RClockwise19429Auto.OPTIONS,
                "Pickup GARDEN drives the GARDEN lane backward (WALL_FLOWER is today's R-Clockwise);"
                        + " L-Clockwise beside it on the other robot, Pickup the other one.");
    }
}
