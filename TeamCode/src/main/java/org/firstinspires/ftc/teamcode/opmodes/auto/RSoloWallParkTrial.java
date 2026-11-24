package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Utility;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RSoloWallParkAuto;

/**
 * Runs the {@link RSoloWallParkAuto} draft from the Utility menu: R-Solo with its park moved to its own
 * RIGHT_PARK_WALL (14.5, 92.5, 270), driven in up the alliance wall from below, so it stays clear of a
 * partner parked high on PARTNER_PARK (9.8, 117.4). Tried beside R-Solo before it replaces R-Solo's park
 * (mentor, 24 Nov 2026).
 */
@Utility(name = "Drive: R-Solo-WallPark " + RSoloWallParkAuto.SAVED,
        description = "Draft: R-Solo, parking along the alliance wall clear of a partner parked high")
public final class RSoloWallParkTrial extends BuiltAuto {
    public RSoloWallParkTrial() {
        super(RSoloWallParkAuto.SOURCE, RSoloWallParkAuto.COMMANDS, RSoloWallParkAuto.TRIGGERS,
                RSoloWallParkAuto.DRAWN_FOR, RSoloWallParkAuto::startPose, RSoloWallParkAuto::build,
                RSoloWallParkAuto.OPTIONS);
    }
}
