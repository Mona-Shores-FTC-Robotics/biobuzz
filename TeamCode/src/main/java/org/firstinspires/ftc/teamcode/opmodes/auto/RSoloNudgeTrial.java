package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Utility;
import org.firstinspires.ftc.teamcode.autokit.AutoDrive;
import org.firstinspires.ftc.teamcode.autokit.AutoKit;
import org.firstinspires.ftc.teamcode.autokit.AutoRegistry;
import org.firstinspires.ftc.teamcode.opmodes.PartnerNudgeTest;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.PartnerNudgeTestAuto;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RSoloNudgeAuto;
import org.firstinspires.ftc.teamcode.pedro.Constants;

@Utility(name = "Drive: R-Solo-Nudge " + RSoloNudgeAuto.SAVED,
        description = "Full Solo with gentle angled nudge; shooting, pickups, park; 30-second stop")
public final class RSoloNudgeTrial extends BuiltAuto {
    private long began;
    private NudgeContactDrive contactDrive;

    public RSoloNudgeTrial() {
        super(RSoloNudgeAuto.SOURCE, RSoloNudgeAuto.COMMANDS, RSoloNudgeAuto.TRIGGERS,
                RSoloNudgeAuto.DRAWN_FOR, RSoloNudgeAuto::startPose, RSoloNudgeAuto::build,
                RSoloNudgeAuto.OPTIONS);
    }
    @Override protected AutoDrive autoDrive(boolean rotated) {
        // Read the contact stroke from its generated drawing rather than duplicating coordinates.
        AutoKit drawn = new AutoKit(robot.drive, new AutoRegistry(), () -> 0);
        PartnerNudgeTestAuto.build(drawn, rotated);
        contactDrive = new NudgeContactDrive(robot.drive, Constants.forActiveRobot().foresightConfig,
                PartnerNudgeTestAuto.startPose(rotated), drawn.paths().get(0).endPose(),
                robot.drive::odometrySpeedInPerSec, () -> System.nanoTime() / 1e9,
                PartnerNudgeTest.pathSpeed);
        return contactDrive;
    }
    @Override protected void onInitLoop() {
        super.onInitLoop();
        display.line("FULL NUDGE TEST: normal RIGHT start, four preloads; 16x16 angled partner.");
        display.line("Contact approach/push: " + PartnerNudgeTest.pathSpeed
                + " of top speed (Panels PartnerNudgeTest.pathSpeed). Automatic STOP at 30 s.");
    }
    @Override protected void onStart() { began = System.nanoTime(); super.onStart(); }
    @Override protected void onLoop() {
        if (contactDrive != null && (!robot.drive.poseReferenced() || contactDrive.contactTimedOut())) {
            log.event("Full nudge trial stopped: contact timeout or lost pose reference");
            robot.drive.setAccelLimited(false);
            robot.drive.drive(0, 0, 0);
            requestOpModeStop();
            return;
        }
        if (System.nanoTime() - began >= 30_000_000_000L) {
            requestOpModeStop();
            return;
        }
        super.onLoop();
    }
}
