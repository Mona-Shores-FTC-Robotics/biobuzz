package org.firstinspires.ftc.teamcode.opmodes;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.eventloop.opmode.Utility;

import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.opmodes.auto.NudgeTestRun;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.PartnerNudgeTestAuto;
import org.firstinspires.ftc.teamcode.pedro.Constants;
import org.firstinspires.ftc.teamcode.util.Alliance;

/** A repeatable push-only experiment; the generated .pp owns both paths and the placement. */
@Configurable
@Utility(name = "Drive: Partner Nudge " + PartnerNudgeTestAuto.SAVED,
        description = "Angled 16x16 chassis: short push, retreat, release drive; no shooting")
public final class PartnerNudgeTest extends RobotOpMode {
    /** Initial test settings, not measured tune values. Read once at PLAY. */
    public static double speedInPerSec = 4;
    public static double accelerationInPerSec2 = 8;
    public static double timeoutSeconds = 8;

    private NudgeTestRun run;
    private String problem;

    @Override protected boolean cameraMayOverrideStart() { return false; }

    @Override protected void onInit() {
        robot.drive.preparePathFollowing();
        log.metadata("AutoSource", "drafts/partner-nudge-test.pp");
    }

    @Override protected void onInitLoop() {
        display.line("16x16 partner, 20 degrees toward field interior; see nudge setup in robot-todo.");
        display.line("Place us at the drawn PARTNER_CONTACT with a 0.5 in bumper gap.");
        display.line("PLAY: push 6 in, retreat; re-INIT to repeat. Choose red or blue.");
        display.line("Nudge limit: " + speedInPerSec + " in/s; " + accelerationInPerSec2
                + " in/s^2; timeout " + timeoutSeconds + " s");
    }

    @Override protected void onStart() {
        if (setup.alliance() == Alliance.UNKNOWN) {
            problem = "Choose alliance in INIT before running the nudge test";
            robot.drive.drive(0, 0, 0);
            return;
        }
        try {
            run = new NudgeTestRun(robot.drive, Constants.forActiveRobot().foresightConfig,
                    robot.drive::odometrySpeedInPerSec, () -> System.nanoTime() / 1e9,
                    () -> robot.drive.drive(0, 0, 0), log::event,
                    speedInPerSec, accelerationInPerSec2, timeoutSeconds);
        } catch (IllegalArgumentException e) {
            problem = e.getMessage();
            robot.drive.drive(0, 0, 0);
            return;
        }
        boolean rotated = setup.alliance() == Alliance.BLUE;
        robot.drive.setPose(PartnerNudgeTestAuto.startPose(rotated));
        robot.drive.setAccelLimited(false); // Zero command must release the drive immediately.
        run.start(rotated);
    }

    @Override protected void onLoop() {
        if (problem != null) {
            display.status("Nudge test", Display.Level.FAULT, problem);
            return;
        }
        if (run != null) {
            run.update();
            display.line(run.result());
            display.line("Measured speed: " + robot.drive.odometrySpeedInPerSec() + " in/s");
        }
    }

    @Override protected void onStop() {
        if (run != null) run.stop("Driver Station STOP");
    }
}
