package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Utility;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RSolo19429Auto;

/**
 * Runs the {@link RSolo19429Auto} draft from the Utility menu: R-Solo with the WALL FLOWER leg to LEFT_AUTO_HIVE
 * driven backward, on a robot with a bottom plate only ({@link BackwardLoadTrial}). Named for 19429, the first
 * robot with one; 20245 runs it too.
 */
@Utility(name = "Drive: R-Solo-19429 " + RSolo19429Auto.SAVED,
        description = "Draft, bottom plate only: R-Solo, WALL FLOWER leg backward; 30 s trial")
public final class RSolo19429Trial extends BackwardLoadTrial {
    public RSolo19429Trial() {
        super(RSolo19429Auto.SOURCE, RSolo19429Auto.COMMANDS, RSolo19429Auto.TRIGGERS, RSolo19429Auto.DRAWN_FOR,
                RSolo19429Auto::startPose, RSolo19429Auto::build, RSolo19429Auto.OPTIONS,
                "the WALL FLOWER leg to LEFT_AUTO_HIVE drives backward; nothing on LEFT_START.");
    }
}
