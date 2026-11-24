package org.firstinspires.ftc.teamcode.opmodes.calibration;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.Utility;
import com.qualcomm.robotcore.hardware.CRServo;

import org.firstinspires.ftc.teamcode.RobotDifferences;
import org.firstinspires.ftc.teamcode.hardware.ActiveConfig;
import org.firstinspires.ftc.teamcode.hardware.DeviceNames;
import org.firstinspires.ftc.teamcode.subsystems.CornerWheelSubsystem;

import java.util.Locale;

/**
 * Bench test for the corner wheels ({@code doc/corner-wheels.md}): spin each one from either gamepad for the
 * build team's zip-tie test, and find which way each servo turns its wheel inward. No Panels, no {@code Robot},
 * none of the match's stop rules: the wheels turn only while this says so. It needs the servos in the active
 * config, not {@code CornerWheelSubsystem.fitted}, so it runs before a robot's {@code RobotDifferences} line.
 *
 * <ol>
 *   <li>INIT shows which hub and port each servo is on. Nothing moves.</li>
 *   <li>PLAY: still nothing moves. Cross starts and stops both wheels; D-pad left and right start and stop one
 *       each.</li>
 *   <li>Watch each wheel from above. Its front must move inward: the left wheel clockwise, the right
 *       counter-clockwise. If one turns the wrong way, Square flips the left and Circle the right
 *       ({@code leftReversed}, {@code rightReversed}). Write the result down and put it in a
 *       {@code RobotDifferences} line (it lasts only until the app restarts).</li>
 *   <li>D-pad up and down: power +/- 0.05, starting at {@code CornerWheelSubsystem.power}.</li>
 * </ol>
 *
 * <p>Standalone diagnostics, like {@code ValidateHardware}: the exception CLAUDE.md allows.
 */
@Utility(name = "Bench: Corner wheels", description = "Spin each corner wheel for the hand test; set which way is inward")
public class CornerWheelBench extends OpMode {

    private static final double STEP = 0.05;

    private CRServo left;
    private CRServo right;
    private String leftWhere;
    private String rightWhere;
    private String problem;

    private boolean started;
    private boolean leftOn;
    private boolean rightOn;
    private double power;

    private boolean crossWas, leftWas, rightWas, upWas, downWas, squareWas, circleWas;

    @Override
    public void init() {
        try {
            RobotDifferences.applyForActiveRobot();
        } catch (RuntimeException e) {
            problem = "robot differences not applied: " + e.getMessage();
        }
        left = hardwareMap.tryGet(CRServo.class, DeviceNames.CORNER_WHEEL_LEFT);
        leftWhere = left == null ? notFound(DeviceNames.CORNER_WHEEL_LEFT)
                : where(left.getConnectionInfo(), left.getPortNumber());
        right = hardwareMap.tryGet(CRServo.class, DeviceNames.CORNER_WHEEL_RIGHT);
        rightWhere = right == null ? notFound(DeviceNames.CORNER_WHEEL_RIGHT)
                : where(right.getConnectionInfo(), right.getPortNumber());
        power = clamp(CornerWheelSubsystem.power);
        report();
    }

    @Override
    public void init_loop() {
        report();
    }

    @Override
    public void start() {
        started = true;
    }

    @Override
    public void loop() {
        if (pressed(cross(), crossWas)) {
            boolean on = !(leftOn || rightOn);
            leftOn = on;
            rightOn = on;
        }
        if (pressed(dpadLeft(), leftWas)) leftOn = !leftOn;
        if (pressed(dpadRight(), rightWas)) rightOn = !rightOn;
        if (pressed(dpadUp(), upWas)) power = clamp(power + STEP);
        if (pressed(dpadDown(), downWas)) power = clamp(power - STEP);
        if (pressed(square(), squareWas)) CornerWheelSubsystem.leftReversed = !CornerWheelSubsystem.leftReversed;
        if (pressed(circle(), circleWas)) CornerWheelSubsystem.rightReversed = !CornerWheelSubsystem.rightReversed;
        rememberButtons();

        if (started) {
            if (left != null) left.setPower(CornerWheelSubsystem.powerFor(leftOn, power, CornerWheelSubsystem.leftReversed));
            if (right != null) right.setPower(CornerWheelSubsystem.powerFor(rightOn, power, CornerWheelSubsystem.rightReversed));
        }
        report();
    }

    @Override
    public void stop() {
        if (left != null) left.setPower(0.0);
        if (right != null) right.setPower(0.0);
    }

    private void report() {
        telemetry.addLine("Bench: Corner wheels" + (started ? "" : "  (nothing moves until PLAY and a press)"));
        telemetry.addData("Config", activeConfig());
        if (problem != null) telemetry.addData("Warning", problem);
        telemetry.addData("Left servo", leftWhere);
        telemetry.addData("Right servo", rightWhere);
        telemetry.addLine();
        telemetry.addData("Power", String.format(Locale.US, "%.2f", power));
        telemetry.addData("Left", (leftOn ? "ON" : "off") + ", leftReversed " + CornerWheelSubsystem.leftReversed);
        telemetry.addData("Right", (rightOn ? "ON" : "off") + ", rightReversed " + CornerWheelSubsystem.rightReversed);
        telemetry.addLine();
        telemetry.addLine("Either gamepad. Each wheel's front must move inward: left clockwise, right"
                + " counter-clockwise, seen from above.");
        telemetry.addLine("Cross both on/off · D-pad left/right one wheel · D-pad up/down power ±0.05");
        telemetry.addLine("Square flip left · Circle flip right (write it down: RobotDifferences)");
        telemetry.update();
    }

    private static String notFound(String name) {
        return "NOT FOUND: \"" + name + "\" is not a <ContinuousRotationServo> in the active config";
    }

    private static String where(String connection, int port) {
        return "port " + port + " · " + connection;
    }

    private static String activeConfig() {
        try {
            return String.valueOf(ActiveConfig.name());
        } catch (RuntimeException e) {
            return "unknown (" + e.getMessage() + ")";
        }
    }

    private static double clamp(double p) {
        return Double.isNaN(p) ? 0.0 : Math.max(0.0, Math.min(1.0, Math.round(p * 100.0) / 100.0));
    }

    // Either gamepad works, so whoever holds one can run the bench.
    private boolean cross() { return gamepad1.cross || gamepad2.cross; }
    private boolean square() { return gamepad1.square || gamepad2.square; }
    private boolean circle() { return gamepad1.circle || gamepad2.circle; }
    private boolean dpadLeft() { return gamepad1.dpad_left || gamepad2.dpad_left; }
    private boolean dpadRight() { return gamepad1.dpad_right || gamepad2.dpad_right; }
    private boolean dpadUp() { return gamepad1.dpad_up || gamepad2.dpad_up; }
    private boolean dpadDown() { return gamepad1.dpad_down || gamepad2.dpad_down; }

    private static boolean pressed(boolean now, boolean was) {
        return now && !was;
    }

    private void rememberButtons() {
        crossWas = cross();
        leftWas = dpadLeft();
        rightWas = dpadRight();
        upWas = dpadUp();
        downWas = dpadDown();
        squareWas = square();
        circleWas = circle();
    }
}
