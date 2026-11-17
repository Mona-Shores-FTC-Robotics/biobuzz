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
 * Bench test for the FLOWER stick: drive the pivot servo straight to a position from either gamepad,
 * read the number off the Driver Station, and spin the roller if this stick has one. No Panels,
 * no {@code Robot}, no limp timer: the servo holds whatever it was last sent until Circle lets it
 * go. It is how {@code FlowerStickSubsystem.RETRACTED} and {@code EXTENDED} are measured.
 *
 * <ol>
 *   <li>INIT shows which hub and port each servo is on. Nothing moves.</li>
 *   <li>PLAY: still nothing moves. The first D-pad or bumper press powers the servo at the
 *       position shown, starting from {@code EXTENDED} (down), so the first press never asks a
 *       stick lying on the tile to lift.</li>
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
        // From down: a stick resting on the tile is already there, so the first press never asks
        // for a lift. Step up from here to see how far it rises.
        position = FlowerStickSubsystem.unitRange(FlowerStickSubsystem.EXTENDED);
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
        if (pressed(dpad_up(), upWas)) { position += FINE_STEP; step = true; }
        if (pressed(dpad_down(), downWas)) { position -= FINE_STEP; step = true; }
        if (pressed(right_bumper(), rbWas)) { position += COARSE_STEP; step = true; }
        if (pressed(left_bumper(), lbWas)) { position -= COARSE_STEP; step = true; }
        position = FlowerStickSubsystem.unitRange(Math.round(position * 1000.0) / 1000.0);
        if (step) powered = true;
        if (pressed(circle(), circleWas)) powered = false;
        if (pressed(cross(), crossWas)) {
            recordedRetracted = position;
            FlowerStickSubsystem.RETRACTED = position;
        }
        if (pressed(triangle(), triangleWas)) {
            recordedExtended = position;
            FlowerStickSubsystem.EXTENDED = position;
        }
        if (pressed(square(), squareWas)) rollerOn = !rollerOn;
        if (pressed(dpad_left(), leftWas)) {
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
        telemetry.addLine("Either gamepad. Lower numbers = stick down (EXTENDED 0.0), higher = up.");
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

    // Either gamepad works, so whoever holds one can run the bench.
    private boolean dpad_up() { return gamepad1.dpad_up || gamepad2.dpad_up; }
    private boolean dpad_down() { return gamepad1.dpad_down || gamepad2.dpad_down; }
    private boolean dpad_left() { return gamepad1.dpad_left || gamepad2.dpad_left; }
    private boolean left_bumper() { return gamepad1.left_bumper || gamepad2.left_bumper; }
    private boolean right_bumper() { return gamepad1.right_bumper || gamepad2.right_bumper; }
    private boolean cross() { return gamepad1.cross || gamepad2.cross; }
    private boolean triangle() { return gamepad1.triangle || gamepad2.triangle; }
    private boolean circle() { return gamepad1.circle || gamepad2.circle; }
    private boolean square() { return gamepad1.square || gamepad2.square; }

    private static boolean pressed(boolean now, boolean was) {
        return now && !was;
    }

    private void rememberButtons() {
        upWas = dpad_up();
        downWas = dpad_down();
        leftWas = dpad_left();
        lbWas = left_bumper();
        rbWas = right_bumper();
        crossWas = cross();
        triangleWas = triangle();
        circleWas = circle();
        squareWas = square();
    }
}
