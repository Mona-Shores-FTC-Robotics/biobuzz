package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Utility;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RSoloBackwardsAuto;

/**
 * Runs the {@link RSoloBackwardsAuto} draft from the Utility menu: R-Solo with the WALL FLOWER leg to LEFT_AUTO_HIVE
 * driven backward, on a robot with a bottom plate only ({@link BackwardLoadTrial}). Called R-Solo-19429 until
 * 20 Nov 2026, for 19429, the first robot with a plate; a draft until it is promoted.
 */
@Utility(name = "Drive: R-Solo-Backwards " + RSoloBackwardsAuto.SAVED,
        description = "Draft, bottom plate only: R-Solo, WALL FLOWER leg backward; 30 s trial")
public final class RSoloBackwardsTrial extends BackwardLoadTrial {
    public RSoloBackwardsTrial() {
        super(RSoloBackwardsAuto.SOURCE, RSoloBackwardsAuto.COMMANDS, RSoloBackwardsAuto.TRIGGERS, RSoloBackwardsAuto.DRAWN_FOR,
                RSoloBackwardsAuto::startPose, RSoloBackwardsAuto::build, RSoloBackwardsAuto.OPTIONS,
                "the WALL FLOWER leg to LEFT_AUTO_HIVE drives backward; nothing on LEFT_START.");
    }
}
