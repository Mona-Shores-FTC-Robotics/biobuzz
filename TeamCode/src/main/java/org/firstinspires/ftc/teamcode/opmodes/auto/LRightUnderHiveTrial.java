package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Utility;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.LRightUnderHiveAuto;

/**
 * Runs the {@link LRightUnderHiveAuto} draft from the Utility menu, beside {@link RRightUnderHiveTrial}: from
 * LEFT_START, wait at LEFT_AUTO_HIVE for TIP 1, preloads at LEFT, the SCORING FLOWER load from LEFT_AUTO_OFFSET,
 * then the outer lane to the WALL FLOWER (only before 22 s) and RIGHT_PARK. It fixes nothing (mentor, 19 Nov 2026).
 */
@Utility(name = "Drive: L-RightUnderHive " + LRightUnderHiveAuto.SAVED,
        description = "Draft, beside R-RightUnderHive: L takes the outer lane and parks right")
public final class LRightUnderHiveTrial extends BuiltAuto {
    public LRightUnderHiveTrial() {
        super(LRightUnderHiveAuto.SOURCE, LRightUnderHiveAuto.COMMANDS, LRightUnderHiveAuto.TRIGGERS,
                LRightUnderHiveAuto.DRAWN_FOR, LRightUnderHiveAuto::startPose, LRightUnderHiveAuto::build,
                LRightUnderHiveAuto.OPTIONS);
    }
}
