package org.firstinspires.ftc.teamcode.opmodes;

import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.localization.HiveFieldPoints;
import org.firstinspires.ftc.teamcode.subsystems.FeederSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.TurretAim;
import org.firstinspires.ftc.teamcode.subsystems.TurretSubsystem;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.vision.CellSighting;
import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.HiveCellState;
import org.firstinspires.ftc.teamcode.vision.Vec3;

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
 * <p>The turret starts every OpMode LOCKED straight ahead, and the operator's D-pad right switches
 * it between LOCKED and AIM ({@link TurretAimer}, the same auto-aim the HIVE &amp; Pose test bed
 * runs). It zeroes itself, assuming it starts a match at its home mark, or taking the Auto's zero
 * when an Auto just ran ({@link org.firstinspires.ftc.teamcode.subsystems.TurretZeroPolicy});
 * D-pad up zeroes it by hand and always wins.
 *
 * <p>D-pad left, on either gamepad, relocalizes the pose from the camera, for after a restart or a
 * shove, or when a pose carried from the last run is a little off; the Match page says what it
 * did. It works in INIT too, so the pose can be put right before PLAY with one person and one
 * gamepad.
 *
 * <p>The driver's D-pad up, held, picks the nearest FLOWER ({@link FlowerPicker}): line up, stick
 * down and intake on during the approach, push in, dwell, back out, stick up, every phase timed in
 * the match log. Letting go or touching a drive stick stops it. It needs a field-referenced pose,
 * so after an Auto or a camera relocalize. It is a test unit for making the pick faster, not a
 * claim about how a match is driven.
 *
 * <p>The operator (gamepad 2) runs the launcher and the feeder, with nothing in between them:
 * spin up, hold feed, watch what happens. While the launcher is being brought up on the robot
 * that is the whole point, so the feeder is not gated on the launcher being at speed;
 * {@link #feedOnlyAtSpeed} puts the gate back in Panels once a match loop wants it.
 *
 * <h2>What the launcher reports</h2>
 *
 * <p>Every loop the launcher's and feeder's numbers go to Panels as flat keys (targets, speeds,
 * currents, at-speed, shots), so the speed dip of each shot is a graph, and the target and trims
 * are live in Panels' configurables. Each shot the launcher counts from that dip
 * ({@code LauncherSubsystem.shots()}) is one line in the match log with its target, dip and the
 * range to the CELL: the camera's ground range to a CELL it sees, else the pose's range to the
 * near-end CELL, else none. Hit or miss is written next to it by hand afterwards. That is #80's
 * speed-vs-distance table being filled in from ordinary driving, with no procedure to follow.
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
     * Off: R2 feeds whenever held, launcher spinning or not, so the launcher can be tested the way
     * a bench tests it. On: a feed starts only once the launcher is at speed, the match policy. A
     * feed already running is never cut by the gate (see {@code FeederSubsystem.requestFor}).
     */
    public static boolean feedOnlyAtSpeed = false;

    /** The range to log with a shot when the camera does not see a CELL: the pose's, to the near-end CELL. */
    private static final double AIM_SWITCH_BAND_IN = 19.75;

    /** What the turret is doing for the operator: held straight ahead, or aiming at our CELL. */
    private enum TurretMode { LOCKED, AIM }

    /** A drive stick past this during a FLOWER pick hands the robot back to the driver. */
    private static final double PICK_ABORT_STICK = 0.2;

    private TurretMode turretMode = TurretMode.LOCKED;
    private TurretAimer aimer;
    private FlowerPicker picker;
    private String relocalizeNote;

    private TelemetryManager panels;
    private HiveCell nearEndCell;
    private double lastShotRangeIn = Double.NaN;
    private String lastShotRangeSource = "";

    @Override
    protected void onInit() {
        try {
            panels = PanelsTelemetry.INSTANCE.getTelemetry();
        } catch (RuntimeException | LinkageError e) {
            panels = null; // Panels is a window onto the robot, not part of it.
        }
        aimer = new TurretAimer(robot);
        picker = new FlowerPicker(robot, text -> log.event(text));
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
        driver.note("D-pad up (hold)", "Pick the nearest FLOWER (let go or move a stick to stop)");
        driver.when("D-pad left", "Pose: relocalize from the camera (hold still, 2+ tags in view)", () -> gamepad1.dpad_left)
                .duringInit()
                .onPress(this::relocalize);

        operator.when("Cross (A)", "Launcher spin up", () -> gamepad2.a)
                .onPress(robot.launcher::spinUp);
        operator.when("Circle (B)", "Launcher spin down", () -> gamepad2.b)
                .onPress(robot.launcher::spinDown);
        operator.when("D-pad right", "Turret: switch LOCKED (straight ahead) / AIM at our CELL", () -> gamepad2.dpad_right)
                .onPress(this::toggleTurretMode);
        operator.when("D-pad left", "Pose: relocalize from the camera (hold still, 2+ tags in view)", () -> gamepad2.dpad_left)
                .duringInit()
                .onPress(this::relocalize);
        operator.when("D-pad up", "Turret: zero here, at its home mark (it zeroes itself at PLAY)", () -> gamepad2.dpad_up)
                .duringInit()
                .onPress(robot.turret::zeroHere);
        operator.note("R2 (hold)", "Feed into the launcher (feedOnlyAtSpeed in Panels makes it wait for at speed)");
        operator.note("L2 (hold)", "Reverse the feeder, to clear a jam");

        driver.note("Angles on the Match page", "degrees; heading is in the field frame; "
                + "a bearing is from the robot's forward, 0 = ahead, + = left (counter-clockwise), - = right");
    }

    /** PLAY: the turret has been zeroed by now (RobotOpMode does it); hold it straight ahead. */
    @Override
    protected void onStart() {
        turretMode = TurretMode.LOCKED;
        aimer.off();
        robot.turret.home();
    }

    /** The camera sets the pose, if it can; the Match page and the match log say what happened. */
    private void relocalize() {
        relocalizeNote = robot.drive.relocalizeFromCamera();
        log.event("pose: " + relocalizeNote);
    }

    /** LOCKED to AIM and back. Either way a fresh request, which also lets a dropped hold try again. */
    private void toggleTurretMode() {
        if (turretMode == TurretMode.LOCKED) {
            turretMode = TurretMode.AIM;
            aimer.start();
        } else {
            turretMode = TurretMode.LOCKED;
            aimer.off();
            robot.turret.home();
        }
    }

    @Override
    protected void onInitLoop() {
        display.status("Basic Drive", Display.Level.OK, "ready — Share (Back) shows the controls");
        if (relocalizeNote != null) {
            display.line("Last relocalize: " + relocalizeNote);
        }
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

        // A FLOWER pick drives the robot while held; letting go or a stick hands it back, this loop.
        picker.update(gamepad1.dpad_up, Math.abs(forward) > PICK_ABORT_STICK
                || Math.abs(strafe) > PICK_ABORT_STICK || Math.abs(turn) > PICK_ABORT_STICK);
        if (!picker.active()) {
            robot.drive.setAccelLimited(!turbo);
            robot.drive.drive(forward, strafe, turn);
        }

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
        display.status("FLOWER stick", Display.Level.OK, (robot.flowerStick.extended() ? "out" : "in") + (robot.flowerStick.limp() ? ", servo limp" : ", servo holding"));
        String pick = picker.line();
        if (!pick.isEmpty()) display.line(pick);
        robot.intake.describe(display);
        robot.launcher.describe(display);
        robot.feeder.describe(display);
        if (turretMode == TurretMode.AIM) {
            aimer.update(setup.alliance(), AIM_SWITCH_BAND_IN);
        }
        robot.turret.describe(display);
        display.line(turretMode == TurretMode.AIM ? aimer.line()
                : "Turret: LOCKED straight ahead (operator D-pad right to aim)");
        if (relocalizeNote != null) {
            display.line("Last relocalize: " + relocalizeNote);
        }
        recordShot();
        showAim();
        showPinpoint();
        display.line(loopTimer.summary());
        publish();
    }

    /**
     * One match-log line per shot the launcher counted in its last update, with the range it was
     * taken from, so the log is #80's table: target, dip, range, and a hit or miss added by hand.
     */
    private void recordShot() {
        if (robot.launcher.shotJustDetected()) {
            lastShotRangeIn = shotRangeIn();
            log.event(String.format(Locale.US, "shot %d: target %.0f ticks/s, dip %.0f, range %s",
                    robot.launcher.shots(), robot.launcher.lastShotTargetTicksPerSecond(),
                    robot.launcher.lastShotDipTicksPerSecond(),
                    Double.isNaN(lastShotRangeIn) ? "unknown"
                            : String.format(Locale.US, "%.0f in (%s)", lastShotRangeIn, lastShotRangeSource)));
        }
        if (!Double.isNaN(lastShotRangeIn)) {
            display.line(String.format(Locale.US, "Last shot: %.0f in (%s)", lastShotRangeIn, lastShotRangeSource));
        }
    }

    /**
     * The horizontal range to the CELL a shot was aimed at, inches: the camera's ground range to
     * the nearest fresh CELL of our alliance (any CELL if the alliance is not chosen yet), else the
     * pose's range from the turret axis to the near-end CELL's raised row, else NaN. Sets
     * {@link #lastShotRangeSource} to say which.
     */
    private double shotRangeIn() {
        Alliance alliance = setup.alliance();
        double best = Double.NaN;
        for (CellSighting sighting : robot.vision.sightings()) {
            if (sighting == null || sighting.ageMs() > SIGHTING_FRESH_MS) continue;
            if (alliance != Alliance.UNKNOWN && !sighting.cell().belongsTo(alliance)) continue;
            double range = sighting.groundRangeIn();
            if (Double.isNaN(best) || range < best) best = range;
        }
        if (!Double.isNaN(best)) {
            lastShotRangeSource = "camera";
            return best;
        }
        Pose pose = robot.drive.poseReferenced() ? robot.drive.pose() : null;
        if (pose == null || alliance == Alliance.UNKNOWN) {
            lastShotRangeSource = "";
            return Double.NaN;
        }
        nearEndCell = TurretAim.nearEndCell(alliance, pose.y(), nearEndCell, HiveFieldPoints.AXLE_Y_IN, AIM_SWITCH_BAND_IN);
        Vec3 row = nearEndCell == null ? null : HiveFieldPoints.rowCentre(nearEndCell, HiveCellState.UP);
        if (row == null) {
            lastShotRangeSource = "";
            return Double.NaN;
        }
        lastShotRangeSource = "odometry, " + nearEndCell;
        return TurretAim.rangeFromFieldIn(pose.x(), pose.y(), pose.heading(),
                TurretSubsystem.turretForwardIn, TurretSubsystem.turretLeftIn, row.x(), row.y());
    }

    /** The launcher and feeder to Panels, flat keys, one update per loop. Never takes the robot down. */
    private void publish() {
        if (panels == null) return;
        try {
            panels.addData("left_target_tps", robot.launcher.isSpinning() ? robot.launcher.leftTarget() : 0.0);
            panels.addData("right_target_tps", robot.launcher.isSpinning() ? robot.launcher.rightTarget() : 0.0);
            panels.addData("left_tps", robot.launcher.leftTicksPerSecond());
            panels.addData("right_tps", robot.launcher.rightTicksPerSecond());
            panels.addData("left_amps", robot.launcher.leftCurrentAmps());
            panels.addData("right_amps", robot.launcher.rightCurrentAmps());
            panels.addData("at_speed", robot.launcher.atSpeed() ? 1.0 : 0.0);
            panels.addData("shots", robot.launcher.shots());
            panels.addData("last_shot_dip_tps", robot.launcher.lastShotDipTicksPerSecond());
            panels.addData("last_shot_range_in", lastShotRangeIn);
            // The feeder's own speed is not published: on the competition robots its encoder socket
            // carries the turret's encoder (DeviceNames.TURRET_ENCODER_HOST).
            panels.addData("feeder_power", robot.feeder.power());
            panels.addData("feeder_amps", robot.feeder.currentAmps());
            panels.addData("turret_mode", turretMode == TurretMode.AIM ? 1 : 0);
            panels.addData("turret_angle_deg", robot.turret.angleDegrees());
            panels.addData("turret_target_deg", robot.turret.targetDegrees());
            panels.addData("turret_error_deg", robot.turret.errorDegrees());
            panels.addData("loop_ms", loopTimer.lastMs());
            panels.update();
        } catch (RuntimeException ignored) {
            // Telemetry never takes a mechanism down.
        }
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
