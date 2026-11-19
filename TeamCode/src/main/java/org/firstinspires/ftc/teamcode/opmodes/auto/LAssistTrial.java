package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Utility;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.LAssistAuto;

/**
 * Runs the {@link LAssistAuto} draft from the Utility menu: from LEFT_START, wait at LEFT_AUTO_HIVE for TIP 1
 * (the near-end partner's preloads at RIGHT), fire our preloads at LEFT and park at LEFT_PARK at once, leaving
 * the left end to the partner. Made for 10255 RoboDux's Kentwood route (mentor, 19 Nov 2026).
 */
@Utility(name = "Drive: L-Assist " + LAssistAuto.SAVED,
        description = "Draft: wait for TIP 1, preloads at LEFT, park; leaves the left end to a near-end partner")
public final class LAssistTrial extends BuiltAuto {
    public LAssistTrial() {
        super(LAssistAuto.SOURCE, LAssistAuto.COMMANDS, LAssistAuto.TRIGGERS,
                LAssistAuto.DRAWN_FOR, LAssistAuto::startPose, LAssistAuto::build,
                LAssistAuto.OPTIONS);
    }
}
