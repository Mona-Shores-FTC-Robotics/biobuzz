package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Utility;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RClockwiseBackwardsAuto;

/**
 * Runs the {@link RClockwiseBackwardsAuto} draft from the Utility menu: R-Clockwise with the outside lane from the GARDEN
 * driven backward, on a robot with a bottom plate only ({@link BackwardLoadTrial}). Called R-Clockwise-19429 until
 * 20 Nov 2026; a draft until it is promoted. The other robot runs L-Clockwise beside it, unchanged.
 */
@Utility(name = "Drive: R-Clockwise-Backwards " + RClockwiseBackwardsAuto.SAVED,
        description = "Draft, bottom plate only: R-Clockwise, GARDEN lane backward; 30 s trial")
public final class RClockwiseBackwardsTrial extends BackwardLoadTrial {
    public RClockwiseBackwardsTrial() {
        super(RClockwiseBackwardsAuto.SOURCE, RClockwiseBackwardsAuto.COMMANDS, RClockwiseBackwardsAuto.TRIGGERS,
                RClockwiseBackwardsAuto.DRAWN_FOR, RClockwiseBackwardsAuto::startPose, RClockwiseBackwardsAuto::build,
                RClockwiseBackwardsAuto.OPTIONS,
                "Pickup GARDEN drives the GARDEN lane backward (WALL_FLOWER is today's R-Clockwise);"
                        + " L-Clockwise beside it on the other robot, Pickup the other one.");
    }
}
