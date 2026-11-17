package org.firstinspires.ftc.teamcode.opmodes.calibration;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.Utility;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.PwmControl;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.RobotDifferences;
import org.firstinspires.ftc.teamcode.hardware.ActiveConfig;
import org.firstinspires.ftc.teamcode.hardware.DeviceNames;
import org.firstinspires.ftc.teamcode.subsystems.FlowerStickSubsystem;

import java.util.Locale;

/**
 * Bench test for the FLOWER stick: drive the pivot servo straight to a position from gamepad 1,
 * read the number off the Driver Station, and spin the roller if this stick has one. No Panels,
 * no {@code Robot}, no limp timer: the servo holds whatever it was last sent until Circle lets it
 * go. It is how {@code FlowerStickSubsystem.RETRACTED} and {@code EXTENDED} are measured.
 *
 * <ol>
 *   <li>INIT shows which hub and port each servo is on. Nothing moves.</li>
 *   <li>PLAY: still nothing moves. The first D-pad or bumper press powers the servo at the
 *       position shown, starting from {@code RETRACTED}, so a new stick is not flung anywhere.</li>
 *   <li>D-pad up/down: position +/- 0.01. Bumpers: +/- 0.05. Step until the stick sits on its
 *       stowed stop: Cross records that as RETRACTED. Step until it just touches the tile:
 *       Triangle records it as EXTENDED. Both are shown; write them down (they last only until
 *       the app restarts) and put them in a PR.</li>
 *   <li>Circle: pulse off (limp), to move the stick by hand. Any step press takes it back.</li>
 *   <li>Square: roller on/off, at {@code rollerPower}. D-pad left flips its direction
 *       ({@code rollerReversed}): note the way that suits the pick.</li>
 * </ol>
 *
 * <p>Standalone diagnostics, like {@code ValidateHardware}: the exception CLAUDE.md allows.
 */
@Utility(name = "Bench: FLOWER Stick", description = "Step the FLOWER stick's servo to find RETRACTED and EXTENDED; spin the roller")
public class FlowerStickBench extends OpMode {

    private static final double FINE_STEP = 0.01;
    private static final double COARSE_STEP = 0.05;

    private Servo pivot;
    private PwmControl pwm;
    private CRServo roller;
    private String pivotWhere;
    private String rollerWhere;
    private String problem;

    private double position;
    private boolean powered;
    private boolean started;
    private boolean rollerOn;
    private double recordedRetracted = Double.NaN;
    private double recordedExtended = Double.NaN;

    private boolean upWas, downWas, leftWas, lbWas, rbWas, crossWas, triangleWas, circleWas, squareWas;

    @Override
    public void init() {
        try {
            RobotDifferences.applyForActiveRobot();
        } catch (RuntimeException e) {
            problem = "robot differences not applied: " + e.getMessage();
        }
        try {
            pivot = hardwareMap.get(Servo.class, DeviceNames.FLOWER_STICK);
            pwm = pivot instanceof PwmControl ? (PwmControl) pivot : null;
            pivotWhere = where(pivot.getConnectionInfo(), pivot.getPortNumber());
        } catch (RuntimeException e) {
            pivot = null;
            pivotWhere = "NOT FOUND: \"" + DeviceNames.FLOWER_STICK + "\" is not a <Servo> in the active config";
        }
        try {
            roller = hardwareMap.get(CRServo.class, DeviceNames.FLOWER_ROLLER);
            rollerWhere = where(roller.getConnectionInfo(), roller.getPortNumber());
        } catch (RuntimeException e) {
            roller = null;
            rollerWhere = "none in this config";
        }
        position = FlowerStickSubsystem.unitRange(FlowerStickSubsystem.RETRACTED);
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
        boolean step = false;
        if (pressed(gamepad1.dpad_up, upWas)) { position += FINE_STEP; step = true; }
        if (pressed(gamepad1.dpad_down, downWas)) { position -= FINE_STEP; step = true; }
        if (pressed(gamepad1.right_bumper, rbWas)) { position += COARSE_STEP; step = true; }
        if (pressed(gamepad1.left_bumper, lbWas)) { position -= COARSE_STEP; step = true; }
        position = FlowerStickSubsystem.unitRange(Math.round(position * 1000.0) / 1000.0);
        if (step) powered = true;
        if (pressed(gamepad1.circle, circleWas)) powered = false;
        if (pressed(gamepad1.cross, crossWas)) {
            recordedRetracted = position;
            FlowerStickSubsystem.RETRACTED = position;
        }
        if (pressed(gamepad1.triangle, triangleWas)) {
            recordedExtended = position;
            FlowerStickSubsystem.EXTENDED = position;
        }
        if (pressed(gamepad1.square, squareWas)) rollerOn = !rollerOn;
        if (pressed(gamepad1.dpad_left, leftWas)) {
            FlowerStickSubsystem.rollerReversed = !FlowerStickSubsystem.rollerReversed;
        }
        rememberButtons();

        if (pivot != null && started) {
            if (powered) {
                pivot.setPosition(position);
                if (pwm != null) pwm.setPwmEnable();
            } else if (pwm != null) {
                pwm.setPwmDisable();
            }
        }
        if (roller != null && started) {
            roller.setPower(FlowerStickSubsystem.rollerPowerFor(rollerOn,
                    FlowerStickSubsystem.rollerPower, FlowerStickSubsystem.rollerReversed));
        }
        report();
    }

    @Override
    public void stop() {
        if (roller != null) roller.setPower(0.0);
        if (pwm != null) pwm.setPwmDisable();
    }

    private void report() {
        telemetry.addLine("Bench: FLOWER Stick" + (started ? "" : "  (nothing moves until PLAY and a step press)"));
        telemetry.addData("Config", activeConfig());
        if (problem != null) telemetry.addData("Warning", problem);
        telemetry.addData("Pivot servo", pivotWhere);
        telemetry.addData("Roller servo", rollerWhere);
        telemetry.addLine();
        telemetry.addData("Position", String.format(Locale.US, "%.3f  (%s)", position,
                pivot == null ? "no servo" : !started ? "not sent yet" : powered ? "servo holding" : "limp"));
        telemetry.addData("Degrees from 0", String.format(Locale.US, "%.0f°",
                position * FlowerStickSubsystem.SERVO_RANGE_DEG));
        telemetry.addData("RETRACTED", fmt(FlowerStickSubsystem.RETRACTED) + recorded(recordedRetracted));
        telemetry.addData("EXTENDED", fmt(FlowerStickSubsystem.EXTENDED) + recorded(recordedExtended));
        if (roller != null) {
            telemetry.addData("Roller", (rollerOn ? "ON" : "off") + ", reversed " + FlowerStickSubsystem.rollerReversed
                    + ", power " + fmt(FlowerStickSubsystem.rollerPower));
        }
        telemetry.addLine();
        telemetry.addLine("D-pad up/down ±0.01 · bumpers ±0.05 · Circle limp");
        telemetry.addLine("Cross = save as RETRACTED · Triangle = save as EXTENDED");
        if (roller != null) telemetry.addLine("Square roller on/off · D-pad left flip roller direction");
        telemetry.update();
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

    private static String fmt(double v) {
        return String.format(Locale.US, "%.3f", v);
    }

    private static String recorded(double v) {
        return Double.isNaN(v) ? "" : "  (recorded now - write it down)";
    }

    private static boolean pressed(boolean now, boolean was) {
        return now && !was;
    }

    private void rememberButtons() {
        upWas = gamepad1.dpad_up;
        downWas = gamepad1.dpad_down;
        leftWas = gamepad1.dpad_left;
        lbWas = gamepad1.left_bumper;
        rbWas = gamepad1.right_bumper;
        crossWas = gamepad1.cross;
        triangleWas = gamepad1.triangle;
        circleWas = gamepad1.circle;
        squareWas = gamepad1.square;
    }
}
