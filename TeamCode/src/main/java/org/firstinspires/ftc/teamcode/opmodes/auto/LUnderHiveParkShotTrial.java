package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Utility;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.LUnderHiveParkShotAuto;

/**
 * Runs the {@link LUnderHiveParkShotAuto} draft from the Utility menu: L-UnderHive, its park facing the LEFT
 * CELL at LEFT_AUTO_PARK, and when the RIGHT-up wait gives up, a turret-aimed volley there if LEFT is up
 * ({@code LaunchAllAimed}). One half of the OutsideLane / UnderHive pair: run it only beside R-OutsideLane.
 */
@Utility(name = "Drive: L-UnderHive-ParkShot " + LUnderHiveParkShotAuto.SAVED,
        description = "Draft: L-UnderHive, parks facing LEFT and fires there if RIGHT never came up")
public final class LUnderHiveParkShotTrial extends BuiltAuto {
    public LUnderHiveParkShotTrial() {
        super(LUnderHiveParkShotAuto.SOURCE, LUnderHiveParkShotAuto.COMMANDS, LUnderHiveParkShotAuto.TRIGGERS,
                LUnderHiveParkShotAuto.DRAWN_FOR, LUnderHiveParkShotAuto::startPose, LUnderHiveParkShotAuto::build,
                LUnderHiveParkShotAuto.OPTIONS);
    }
}
