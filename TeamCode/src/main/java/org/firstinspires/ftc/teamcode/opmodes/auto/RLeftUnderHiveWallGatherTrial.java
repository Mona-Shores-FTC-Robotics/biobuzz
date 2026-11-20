package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Utility;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RLeftUnderHiveWallGatherAuto;

/**
 * Runs the {@link RLeftUnderHiveWallGatherAuto} draft from the Utility menu: R-LeftUnderHive with its hold at the
 * SCORING FLOWER ({@code L has left the GARDEN}) at 17.6 s instead of 18.6, since L-LeftUnderHive-WallGather leaves the
 * GARDEN along the audience wall about a second sooner. Run it only beside Drive: L-LeftUnderHive-WallGather.
 */
@Utility(name = "Drive: R-LeftUnderHive-WallGather " + RLeftUnderHiveWallGatherAuto.SAVED,
        description = "Draft: R-LeftUnderHive, leaves the SCORING FLOWER at 17.6 s; beside L-LeftUnderHive-WallGather")
public final class RLeftUnderHiveWallGatherTrial extends BuiltAuto {
    public RLeftUnderHiveWallGatherTrial() {
        super(RLeftUnderHiveWallGatherAuto.SOURCE, RLeftUnderHiveWallGatherAuto.COMMANDS, RLeftUnderHiveWallGatherAuto.TRIGGERS,
                RLeftUnderHiveWallGatherAuto.DRAWN_FOR, RLeftUnderHiveWallGatherAuto::startPose, RLeftUnderHiveWallGatherAuto::build,
                RLeftUnderHiveWallGatherAuto.OPTIONS);
    }
}
