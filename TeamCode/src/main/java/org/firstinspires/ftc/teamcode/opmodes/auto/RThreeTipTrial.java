package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Utility;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RThreeTipAuto;

/**
 * Runs the {@link RThreeTipAuto} draft from the Utility menu, as a trial: the right-start half of the
 * ThreeTip pair, which runs only beside {@link LThreeTipTrial}. Our two robots make three TIPs between
 * them: L's preloads TIP LEFT, R's TIP RIGHT, and a second volley tips again (mentor, 19 Nov 2026).
 */
@Utility(name = "Drive: R-ThreeTip " + RThreeTipAuto.SAVED,
        description = "Draft trial: right half of the ThreeTip pair; run only beside Drive: L-ThreeTip")
public final class RThreeTipTrial extends BuiltAuto {
    public RThreeTipTrial() {
        super(RThreeTipAuto.SOURCE, RThreeTipAuto.COMMANDS, RThreeTipAuto.TRIGGERS,
                RThreeTipAuto.DRAWN_FOR, RThreeTipAuto::startPose, RThreeTipAuto::build,
                RThreeTipAuto.OPTIONS);
    }
}
