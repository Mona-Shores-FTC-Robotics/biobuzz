package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Utility;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.LLeftUnderHiveWallGatherAuto;

/**
 * Runs the {@link LLeftUnderHiveWallGatherAuto} draft from the Utility menu: L-LeftUnderHive with its GARDEN
 * gathered the other way in (mentor, 20 Nov 2026). Down under the HIVE it turns to face the red-side wall,
 * strafes onto the audience wall and runs along it into the corner without stopping, the intake's left edge
 * down the GARDEN's row ({@code GardenGatherAlongWall}), then backs out along the wall to RIGHT_AUTO_HIVE and
 * does what L-LeftUnderHive does. A trial: run it only beside R-LeftUnderHive, as that half's pair.
 */
@Utility(name = "Drive: L-LeftUnderHive-WallGather " + LLeftUnderHiveWallGatherAuto.SAVED,
        description = "Draft: L-LeftUnderHive, GARDEN gathered along the audience wall without stopping; beside R-LeftUnderHive")
public final class LLeftUnderHiveWallGatherTrial extends BuiltAuto {
    public LLeftUnderHiveWallGatherTrial() {
        super(LLeftUnderHiveWallGatherAuto.SOURCE, LLeftUnderHiveWallGatherAuto.COMMANDS, LLeftUnderHiveWallGatherAuto.TRIGGERS,
                LLeftUnderHiveWallGatherAuto.DRAWN_FOR, LLeftUnderHiveWallGatherAuto::startPose, LLeftUnderHiveWallGatherAuto::build,
                LLeftUnderHiveWallGatherAuto.OPTIONS);
    }
}
