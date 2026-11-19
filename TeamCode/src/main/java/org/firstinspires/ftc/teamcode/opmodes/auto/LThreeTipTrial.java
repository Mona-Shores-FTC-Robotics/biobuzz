package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Utility;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.LThreeTipAuto;

/**
 * Runs the {@link LThreeTipAuto} draft from the Utility menu, as a trial: the left-start half of the
 * ThreeTip pair, which runs only beside {@link RThreeTipTrial}. Our two robots make three TIPs between
 * them: L's preloads TIP LEFT, R's TIP RIGHT, and a second volley tips again (mentor, 19 Nov 2026).
 */
@Utility(name = "Drive: L-ThreeTip " + LThreeTipAuto.SAVED,
        description = "Draft trial: left half of the ThreeTip pair; run only beside Drive: R-ThreeTip")
public final class LThreeTipTrial extends BuiltAuto {
    public LThreeTipTrial() {
        super(LThreeTipAuto.SOURCE, LThreeTipAuto.COMMANDS, LThreeTipAuto.TRIGGERS,
                LThreeTipAuto.DRAWN_FOR, LThreeTipAuto::startPose, LThreeTipAuto::build,
                LThreeTipAuto.OPTIONS);
    }
}
