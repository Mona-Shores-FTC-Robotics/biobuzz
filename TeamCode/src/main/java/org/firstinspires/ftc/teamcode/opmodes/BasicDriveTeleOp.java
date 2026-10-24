package org.firstinspires.ftc.teamcode.opmodes;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.subsystems.FeederSubsystem;
import org.firstinspires.ftc.teamcode.vision.CellSighting;

import java.util.Locale;

/**
 * Drive the robot with a gamepad. The reference {@link RobotOpMode}: read the sticks, tell
 * {@code robot.drive} what you want, report.
 *
 * <h2>Controls</h2>
 *
 * <p>Bound in {@link #onInit()}, and listed on the Driver Station's Controls page (Back/Share) —
 * the bindings are the documentation. Slow wins over turbo if both bumpers are held.
 *
 * <p>These bindings are a starting point, not a decision. Which button does what is a driver
 * question, and the drivers should own it.
 *
 * <p>Speeds are {@code @Configurable}: tune them in Panels while someone drives, then write the
 * values the drivers like back here — Panels edits are lost on restart. The acceleration limit
 * lives on {@code DriveSubsystem}, because it is about the robot, not the driver.
 *
 * <p>The operator (gamepad 2) runs the launcher and the feeder. The feeder only feeds while the
 * launcher is at speed, so a piece is never pushed into wheels that are still spinning up; that
 * is a policy, and {@link #feedOnlyAtSpeed} switches it off for a bench that wants to move the
 * feeder on its own.
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

    /** A trigger pulled past this counts as pressed. Well clear of a resting trigger's drift. */
    private static final double TRIGGER_PRESSED = 0.5;

    /** A CELL sighting older than this is not shown on the Aim line. */
    private static final long SIGHTING_FRESH_MS = 500;

    /**
     * The feeder feeds only while {@code robot.launcher.atSpeed()}. Set false in Panels to run the
     * feeder with the launcher idle or spinning up, for a bench test of the feeder alone.
     */
    public static boolean feedOnlyAtSpeed = true;

    @Override
    protected void onInit() {
        driver.note("Left stick", "Drive");
        driver.note("Right stick X", "Turn");
        driver.note("L1 (hold)", "Slow");
        driver.note("R1 (hold)", "Turbo — full power, no acceleration limit");
        driver.when("Triangle (Y)", "Reset field-centric forward", () -> gamepad1.y)
                .onPress(robot.drive::resetHeading);
        driver.when("Circle (B)", "Toggle field / robot-centric", () -> gamepad1.b)
                .onPress(robot.drive::toggleFieldCentric);
        driver.when("Cross (A)", "Extend FLOWER stick", () -> gamepad1.a)
                .onPress(robot.flowerStick::extend);
        driver.when("Square (X)", "Retract FLOWER stick", () -> gamepad1.x)
                .onPress(robot.flowerStick::retract);
        driver.when("R2 (hold)", "Intake", () -> gamepad1.right_trigger > TRIGGER_PRESSED)
                .onPress(robot.intake::intake)
                .onRelease(robot.intake::idle);
        driver.when("L2 (hold)", "Eject", () -> gamepad1.left_trigger > TRIGGER_PRESSED)
                .onPress(robot.intake::eject)
                .onRelease(robot.intake::idle);

        operator.when("Cross (A)", "Launcher spin up", () -> gamepad2.a)
                .onPress(robot.launcher::spinUp);
        operator.when("Circle (B)", "Launcher spin down", () -> gamepad2.b)
                .onPress(robot.launcher::spinDown);
        operator.note("R2 (hold)", "Feed into the launcher — only once it is at speed (feedOnlyAtSpeed in Panels)");
        operator.note("L2 (hold)", "Reverse the feeder, to clear a jam");

        driver.note("Angles on the Match page", "degrees; heading is in the field frame; "
                + "a bearing is from the robot's forward, 0 = ahead, + = left (counter-clockwise), - = right");
    }

    @Override
    protected void onInitLoop() {
        display.status("Basic Drive", Display.Level.OK, "ready — Share (Back) shows the controls");
        showPinpoint();
    }

    @Override
    protected void onLoop() {
        boolean slow = gamepad1.left_bumper;
        boolean turbo = gamepad1.right_bumper && !slow;
        double scale = unitRange(slow ? slowSpeed : turbo ? turboSpeed : normalSpeed);

        double forward = deadband(gamepad1.left_stick_y) * FORWARD_SIGN * scale;
        double strafe = deadband(gamepad1.left_stick_x) * STRAFE_SIGN * scale;
        double turn = deadband(gamepad1.right_stick_x) * TURN_SIGN * scale;

        robot.drive.setAccelLimited(!turbo);
        robot.drive.drive(forward, strafe, turn);

        // The feeder is a held input gated on a changing fact, so it is decided every loop here
        // rather than on a press: a trigger held through the spin-up starts feeding at speed, and
        // once feeding it keeps feeding through the wheels' dip (see FeederSubsystem.requestFor).
        FeederSubsystem.State feed = FeederSubsystem.requestFor(
                gamepad2.right_trigger > TRIGGER_PRESSED,
                gamepad2.left_trigger > TRIGGER_PRESSED,
                robot.launcher.atSpeed(), feedOnlyAtSpeed,
                robot.feeder.state() == FeederSubsystem.State.FEED);
        switch (feed) {
            case FEED:
                robot.feeder.feed();
                break;
            case REVERSE:
                robot.feeder.reverse();
                break;
            default:
                robot.feeder.idle();
        }

        display.status("Mode", Display.Level.OK,
                robot.drive.isFieldCentric() ? "field-centric" : "robot-centric");
        display.status("Speed", turbo ? Display.Level.WARN : Display.Level.OK,
                slow ? "slow" : turbo ? "TURBO" : "normal");
        display.status("FLOWER stick", Display.Level.OK, robot.flowerStick.extended() ? "out" : "in");
        robot.intake.describe(display);
        robot.launcher.describe(display);
        robot.feeder.describe(display);
        showAim();
        showPinpoint();
        display.line(loopTimer.summary());
    }

    /**
     * The numbers a turret will aim with, so they can be checked against a tape measure before a
     * turret exists: the robot's heading in the field frame, and for every CELL the camera sees,
     * its bearing from the robot's forward (0 ahead, + left, - right) and its ground range.
     */
    private void showAim() {
        String heading = robot.drive.hasHeading()
                ? String.format(Locale.US, "heading %.0f°", Math.toDegrees(robot.drive.heading()))
                : "heading n/a";
        StringBuilder cells = new StringBuilder();
        for (CellSighting sighting : robot.vision.sightings()) {
            if (sighting == null || sighting.ageMs() > SIGHTING_FRESH_MS) continue;
            double bearingDeg = Math.toDegrees(sighting.bearingRad());
            cells.append(String.format(Locale.US, " · %s %s: bearing %+.0f° (%s), %.0f in",
                    sighting.cell(), robot.vision.state(sighting.cell()), bearingDeg,
                    bearingDeg > 0.5 ? "left" : bearingDeg < -0.5 ? "right" : "ahead",
                    sighting.groundRangeIn()));
        }
        display.line("Aim: " + heading + (cells.length() == 0 ? " · no CELL in view" : cells));
    }

    private void showPinpoint() {
        if (!robot.drive.hasHeading()) {
            display.status("Pinpoint", Display.Level.WARN,
                    "MISSING — robot-centric only (" + robot.drive.localizerFault() + ")");
        }
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
}
