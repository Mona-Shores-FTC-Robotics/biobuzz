package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Utility;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RRightUnderHiveAuto;

/**
 * Runs the {@link RRightUnderHiveAuto} draft from the Utility menu, beside {@link LRightUnderHiveTrial}: from
 * RIGHT_START, preloads at RIGHT (TIP 1), the GARDEN, then the GARDEN load at RIGHT once RIGHT is up and up the
 * corridor under the HIVE to LEFT_PARK; if RIGHT is not up by 18 s it fixes LEFT instead (mentor, 19 Nov 2026).
 */
@Utility(name = "Drive: R-RightUnderHive " + RRightUnderHiveAuto.SAVED,
        description = "Draft, beside L-RightUnderHive: R owns every fix and parks left, under the HIVE")
public final class RRightUnderHiveTrial extends BuiltAuto {
    public RRightUnderHiveTrial() {
        super(RRightUnderHiveAuto.SOURCE, RRightUnderHiveAuto.COMMANDS, RRightUnderHiveAuto.TRIGGERS,
                RRightUnderHiveAuto.DRAWN_FOR, RRightUnderHiveAuto::startPose, RRightUnderHiveAuto::build,
                RRightUnderHiveAuto.OPTIONS);
    }
}
