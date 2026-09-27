package org.firstinspires.ftc.teamcode.opmodes;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

/**
 * Drive the robot with a gamepad. The reference {@link RobotOpMode}: read the sticks, tell
 * {@code robot.drive} what you want, report.
 *
 * <h2>Controls</h2>
 * <ul>
 *   <li>Left stick — translate</li>
 *   <li>Right stick X — turn</li>
 *   <li>Left bumper (hold) — slow mode. Wins over turbo if both are held.</li>
 *   <li>Right bumper (hold) — turbo: full power, no acceleration limit</li>
 *   <li>Y — reset field-centric forward to the way the robot is facing now</li>
 *   <li>B — toggle field-centric / robot-centric</li>
 * </ul>
 *
 * <p>These bindings are a starting point, not a decision. Which button does what is a driver
 * question, and the drivers should own it.
 *
 * <p>Speeds are {@code @Configurable}: tune them in Panels while someone drives, then write the
 * values the drivers like back here — Panels edits are lost on restart. The acceleration limit
 * lives on {@code DriveSubsystem}, because it is about the robot, not the driver.
 */
@TeleOp(name = "Basic Drive", group = "Drive")
@Configurable
public class BasicDriveTeleOp extends RobotOpMode {

    /**
     * Stick-to-robot sign conventions.
     *
     * <p><b>Expect at least one of these to be wrong the first time you run this.</b> Nobody can
     * tell from a desk which way a robot's motors are wired or which way its odometry pods count.
     * If the robot strafes the wrong way, flip {@code STRAFE_SIGN}; if it turns the wrong way, flip
     * {@code TURN_SIGN}. That is a thirty-second fix on the robot, and it is the expected first
     * result — not a bug report.
     */
    private static final double FORWARD_SIGN = -1.0;  // pushing the stick away from you is +forward
    private static final double STRAFE_SIGN  = -1.0;  // Pedro's +strafe is left; stick +x is right
    private static final double TURN_SIGN    = -1.0;  // Pedro's +turn is counter-clockwise

    /** Stick multiplier for everyday driving. 1.0 would be full power. */
    public static double normalSpeed = 0.6;

    /** Stick multiplier while the right bumper (turbo) is held. */
    public static double turboSpeed = 1.0;

    /** Stick multiplier while the left bumper (slow) is held. */
    public static double slowSpeed = 0.35;

    /** Below this, a stick is treated as centred. Guards against drift on a worn gamepad. */
    private static final double STICK_DEADBAND = 0.05;

    private boolean prevY;
    private boolean prevB;

    @Override
    protected void onInit() {
        telemetry.addLine("Basic Drive ready.");
        telemetry.addLine("Left stick drives, right stick turns.");
        telemetry.addLine("Hold left bumper for slow, right bumper for turbo.");
        if (!robot.drive.hasHeading()) {
            telemetry.addLine();
            telemetry.addData("NO PINPOINT", "robot-centric only — %s", robot.drive.localizerFault());
        }
    }

    @Override
    protected void onLoop() {
        handleButtons();

        boolean slow = gamepad1.left_bumper;
        boolean turbo = gamepad1.right_bumper && !slow;
        double scale = unitRange(slow ? slowSpeed : turbo ? turboSpeed : normalSpeed);

        double forward = deadband(gamepad1.left_stick_y) * FORWARD_SIGN * scale;
        double strafe = deadband(gamepad1.left_stick_x) * STRAFE_SIGN * scale;
        double turn = deadband(gamepad1.right_stick_x) * TURN_SIGN * scale;

        robot.drive.setAccelLimited(!turbo);
        robot.drive.drive(forward, strafe, turn);

        publishTelemetry(slow, turbo, forward, strafe, turn);
    }

    /** Edge-detected, so holding a button does not retrigger it every loop. */
    private void handleButtons() {
        if (gamepad1.y && !prevY) {
            robot.drive.resetHeading();
        }
        prevY = gamepad1.y;

        if (gamepad1.b && !prevB) {
            robot.drive.toggleFieldCentric();
        }
        prevB = gamepad1.b;
    }

    /**
     * Clamps a Panels-editable speed to [0, 1], and treats NaN as 0. A typo in Panels should give
     * a robot that is too slow, never one commanded past full power or sent NaN.
     */
    private static double unitRange(double value) {
        return value > 0.0 ? Math.min(value, 1.0) : 0.0;
    }

    private static double deadband(double value) {
        return Math.abs(value) < STICK_DEADBAND ? 0.0 : value;
    }

    private void publishTelemetry(boolean slow, boolean turbo,
                                  double forward, double strafe, double turn) {
        telemetry.addData("Mode", robot.drive.isFieldCentric() ? "FIELD-CENTRIC (B to switch)"
                                                               : "ROBOT-CENTRIC (B to switch)");
        telemetry.addData("Speed", slow ? "SLOW" : turbo ? "TURBO" : "normal");
        telemetry.addData("Stick", "fwd %.2f  strafe %.2f  turn %.2f", forward, strafe, turn);
        if (robot.drive.hasHeading()) {
            telemetry.addData("Heading", "%.1f deg  (Y zeroes it)",
                    Math.toDegrees(robot.drive.heading()));
        } else {
            telemetry.addData("NO PINPOINT", robot.drive.localizerFault());
        }
        telemetry.addData("Loop", loopTimer.summary());
    }
}
