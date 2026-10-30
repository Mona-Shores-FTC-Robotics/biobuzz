package org.firstinspires.ftc.teamcode.opmodes;

import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.teamcode.controls.Action;
import org.firstinspires.ftc.teamcode.controls.ControlScheme;
import org.firstinspires.ftc.teamcode.controls.Controls;
import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.controls.IntakeArbiter;
import org.firstinspires.ftc.teamcode.controls.LongPress;
import org.firstinspires.ftc.teamcode.localization.HiveFieldPoints;
import org.firstinspires.ftc.teamcode.localization.ResetPositions;
import org.firstinspires.ftc.teamcode.localization.StartPosition;
import org.firstinspires.ftc.teamcode.localization.StartPositions;
import org.firstinspires.ftc.teamcode.subsystems.FeederSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.IntakeSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.LauncherSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.TurretAim;
import org.firstinspires.ftc.teamcode.subsystems.TurretSubsystem;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.vision.CellSighting;
import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.HiveCellState;
import org.firstinspires.ftc.teamcode.vision.Vec3;

import java.util.Locale;

/**
 * The match TeleOp: read the gamepads by {@link Action}, tell the subsystems what you want,
 * report. {@link RobotCentricTeleOp} is the same OpMode driving robot-centric, for a match where
 * the Pinpoint has failed; there is no field / robot-centric button.
 *
 * <h2>Controls</h2>
 *
 * <p>Which input does what is {@link ControlScheme}, one layout for both teams. Both people run
 * the intake (R2) and unjam (L2), the driver winning when both press ({@link IntakeArbiter}); unjam
 * on either pad also reverses the feeder. The Driver Station's Controls page (Share) and the
 * printed cheat sheet are both generated from the layout. Nothing here names a button.
 *
 * <p>Buttons do what is asked at once. Fire feeds the moment it is held: the checks a shot could
 * wait for ({@link FireGates}: launcher at speed, aim on our CELL) run every loop and are logged
 * with every fire press, but block nothing until their flag in {@link TeleOpConfig} is turned on.
 *
 * <p>Moving a drive stick cancels the driver's automatic routines: the FLOWER pick
 * ({@link FlowerPicker}, which stows the stick and idles the intake as it stops) and the GARDEN
 * gather ({@link GardenGather}) on either stick, one of them at a time, and the chassis aim on the
 * right stick, since the left stick keeps driving under it.
 *
 * <h2>Aiming</h2>
 *
 * <p>The turret starts every OpMode LOCKED straight ahead; the operator's lock button switches it
 * to AIM at our CELL ({@link TurretAimer}: left half of the field, left CELL; right half, right
 * CELL). While LOCKED, the driver's chassis aim turns the whole robot onto the same CELL. The
 * operator's trim nudges either one and stays until reset. It zeroes itself, assuming it starts a
 * match at its home mark, or taking the Auto's zero when an Auto just ran
 * ({@link org.firstinspires.ftc.teamcode.subsystems.TurretZeroPolicy}); the zero button always
 * wins.
 *
 * <h2>What it reports</h2>
 *
 * <p>The operator's light bar is green at launch speed, amber spinning up, blue at idle, with a
 * short rumble the moment it reaches speed. The match log has every fire press with
 * its launcher speed, aim error, trim, pose and gates, every trim, who ran the intake each loop,
 * and every routine a stick cancelled. Each shot the launcher counts from its speed dip is one line
 * with the range to the CELL, which is #80's speed-vs-distance table filled in from ordinary
 * driving.
 */
@TeleOp(name = "Basic Drive", group = "Drive")
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

    /** Below this, a stick is treated as centred. Guards against drift on a worn gamepad. */
    private static final double STICK_DEADBAND = 0.05;

    /** A CELL sighting older than this is not shown on the Aim line. */
    private static final long SIGHTING_FRESH_MS = 500;

    /** The range to log with a shot when the camera does not see a CELL: the pose's, to the near-end CELL. */
    private static final double AIM_SWITCH_BAND_IN = 19.75;

    /** What the turret is doing for the operator: held straight ahead, or aiming at our CELL. */
    private enum TurretMode { LOCKED, AIM }

    /** What the operator's light bar shows. */
    private enum Bar { NONE, IDLE, SPINNING, AT_SPEED }

    /** A drive stick past this cancels a routine that is driving the robot. */
    private static final double CANCEL_STICK = 0.2;

    /** How long a reset (gyro, camera, pose, turret zero) must be held. A tap does nothing. */
    private static final long RESET_HOLD_MS = 1000;

    /** The launcher trim step can never be more than this, ticks per second, whatever Panels says. */
    private static final double MAX_LAUNCHER_TRIM_STEP_TPS = 200.0;

    /** The trim step can never be more than this, degrees, whatever Panels says. */
    private static final double MAX_TRIM_STEP_DEG = 5.0;

    /** {@code /Controls/IntakeOwner} while the FLOWER pick or the GARDEN gather runs the intake. */
    private static final int INTAKE_OWNER_PICK = IntakeArbiter.Owner.values().length;

    private Controls controls;
    private final IntakeArbiter intakeArbiter = new IntakeArbiter();
    private final FireGates gates = new FireGates();
    private final LongPress headingReset = new LongPress(RESET_HOLD_MS);
    private final LongPress cameraReset = new LongPress(RESET_HOLD_MS);
    private final LongPress leftCornerReset = new LongPress(RESET_HOLD_MS);
    private final LongPress rightCornerReset = new LongPress(RESET_HOLD_MS);
    private final LongPress turretZero = new LongPress(RESET_HOLD_MS);

    private TurretMode turretMode = TurretMode.LOCKED;
    private TurretAimer aimer;
    private FlowerPicker picker;
    private GardenGather gatherer;
    private boolean launchMode;
    /** The operator's trim on the launch speed, ticks per second; lasts the OpMode. */
    private double launcherTrimTps;
    private boolean chassisAimCancelled;
    private boolean lastFire;
    private Bar bar = Bar.NONE;
    private String poseNote;
    private String autoParkNote;
    private double aimErrorDeg = Double.NaN;

    private TelemetryManager panels;
    private HiveCell nearEndCell;
    private double lastShotRangeIn = Double.NaN;
    private String lastShotRangeSource = "";

    /** True for {@link RobotCentricTeleOp}: every drive request is robot-centric. */
    protected boolean robotCentric() {
        return false;
    }

    @Override
    protected void onInit() {
        try {
            panels = PanelsTelemetry.INSTANCE.getTelemetry();
        } catch (RuntimeException | LinkageError e) {
            panels = null; // Panels is a window onto the robot, not part of it.
        }
        aimer = new TurretAimer(robot);
        picker = new FlowerPicker(robot, text -> log.event(text));
        gatherer = new GardenGather(robot, text -> log.event(text));
        controls = new Controls(TeleOpConfig.flowerPickButton, gamepad1, gamepad2, driver, operator);
        log.metadata("Controls/Drive", robotCentric() ? "robot-centric" : "field-centric");

        // Bound in Action order, so the Controls page reads like the cheat sheet.
        controls.note(Action.DRIVE);
        controls.note(Action.TURN);
        controls.note(Action.INTAKE);
        controls.note(Action.DRIVER_UNJAM);
        controls.note(Action.SLOW);
        controls.note(Action.TURBO);
        controls.note(Action.FLOWER_PICK);
        controls.note(Action.GARDEN_GATHER);
        controls.note(Action.CHASSIS_AIM);
        controls.note(Action.RESET_HEADING);
        controls.note(Action.RESET_LEFT_CORNER);
        controls.note(Action.RESET_RIGHT_CORNER);
        controls.note(Action.CAMERA_RELOCALIZE);
        controls.on(Action.AUTO_PARK).onPress(() -> {
            autoParkNote = "Auto-park: not built yet (the loading zone is not measured)";
            log.event("auto-park pressed: not built yet");
        });

        controls.note(Action.OPERATOR_INTAKE);
        controls.note(Action.UNJAM);
        controls.on(Action.LAUNCHER_TOGGLE).onPress(this::toggleLauncher);
        controls.on(Action.LAUNCHER_TRIM_DOWN).onPress(() -> trimLauncher(-1));
        controls.on(Action.LAUNCHER_TRIM_UP).onPress(() -> trimLauncher(+1));
        controls.note(Action.FIRE);
        controls.on(Action.TURRET_LOCK).onPress(this::toggleTurretMode);
        controls.on(Action.FLOWER_STICK).onPress(robot.flowerStick::toggle);
        controls.on(Action.TRIM_LEFT).onPress(() -> trim(+1));
        controls.on(Action.TRIM_RIGHT).onPress(() -> trim(-1));
        controls.on(Action.TRIM_RESET).onPress(() -> trim(0));
        controls.note(Action.TURRET_ZERO);

        driver.note("Angles on the Match page", "degrees; heading is in the field frame; "
                + "a bearing is from the robot's forward, 0 = ahead, + = left (counter-clockwise), - = right");
    }

    /**
     * With no Auto before it (and no TeleOp just restarted), Basic Drive starts from the Autos'
     * start, so the drive team lines up there for a test too: the pose is known, field-centric
     * forward is the alliance's from PLAY, and the run is a calibration sample. After an Auto it
     * keeps the Auto's pose. Red until an alliance is chosen, as the Autos are drawn.
     */
    @Override
    protected StartPosition startPosition() {
        return hasHandoffPose() ? null : StartPositions.forAlliance(setup.alliance());
    }

    /** PLAY: the turret has been zeroed by now (RobotOpMode does it); hold it straight ahead. */
    @Override
    protected void onStart() {
        turretMode = TurretMode.LOCKED;
        aimer.off();
        robot.turret.home();
        log.event("PLAY: " + (robotCentric() ? "robot-centric" : "field-centric"));
    }

    /** The pose set to our alliance's measured spot in {@code corner}, if it has been measured. */
    private void resetToSpot(ResetPositions.Corner corner) {
        poseNote = robot.drive.resetToSpot(setup.alliance(), corner);
        log.event("pose: " + poseNote);
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
        log.event("turret " + turretMode);
    }

    /** Idle speed to launch speed and back. Idle is {@link TeleOpConfig#launcherIdleTicksPerSecond}; 0 stops. */
    private void toggleLauncher() {
        launchMode = !launchMode;
        if (launchMode) {
            robot.launcher.spinUp(launchSpeed());
            log.event(String.format(Locale.US, "launcher: launch speed %.0f ticks/s", launchSpeed()));
            return;
        }
        double idle = TeleOpConfig.launcherIdleTicksPerSecond;
        if (idle > 0.0) {
            // Never past the launch speed: an idle typed as 16000 is a typo, not a request.
            robot.launcher.spinUp(Math.min(idle, launchSpeed()));
        } else {
            robot.launcher.spinDown();
        }
        log.event(String.format(Locale.US, "launcher: idle (%.0f ticks/s)", Math.max(0.0, idle)));
    }

    /** The launch speed: the configured one plus the operator's trim, never below zero. */
    private double launchSpeed() {
        return Math.max(0.0, LauncherSubsystem.targetTicksPerSecond + launcherTrimTps);
    }

    /**
     * One launcher trim press, +1 up or -1 down. Takes effect at once if the launcher is at launch
     * speed, and otherwise at the next spin-up. The step is guarded: NaN or negative is no step.
     */
    private void trimLauncher(int direction) {
        double step = TeleOpConfig.launcherTrimStepTps > 0.0
                ? Math.min(TeleOpConfig.launcherTrimStepTps, MAX_LAUNCHER_TRIM_STEP_TPS) : 0.0;
        launcherTrimTps = Math.max(-LauncherSubsystem.targetTicksPerSecond, launcherTrimTps + direction * step);
        if (launchMode) {
            robot.launcher.spinUp(launchSpeed());
        }
        log.event(String.format(Locale.US, "launcher trim %+.0f ticks/s: launch speed %.0f",
                launcherTrimTps, launchSpeed()));
    }

    /** +1 a step left, -1 a step right, 0 back to zero. The step is guarded: NaN or negative is no step. */
    private void trim(int direction) {
        double step = TeleOpConfig.trimStepDeg > 0.0 ? Math.min(TeleOpConfig.trimStepDeg, MAX_TRIM_STEP_DEG) : 0.0;
        if (direction > 0) aimer.trimLeft(step);
        else if (direction < 0) aimer.trimRight(step);
        else aimer.resetTrim();
        log.event(String.format(Locale.US, "trim %+.1f°", aimer.trimDeg()));
    }

    /**
     * The resets, each a 1 s hold so a brushed button does nothing, each with a short rumble on the
     * pad that held it. In INIT only the ones that move nothing: the camera reset and the turret
     * zero.
     */
    private void holdResets(long now, boolean init) {
        if (!init && headingReset.update(controls.held(Action.RESET_HEADING), now)) {
            robot.drive.resetHeading();
            gamepad1.rumble(200);
            log.event("gyro reset: field-centric forward = the robot's facing");
        }
        if (cameraReset.update(controls.held(Action.CAMERA_RELOCALIZE), now)) {
            relocalize();
            gamepad1.rumble(200);
        }
        if (!init && leftCornerReset.update(controls.held(Action.RESET_LEFT_CORNER), now)) {
            resetToSpot(ResetPositions.Corner.LEFT);
            gamepad1.rumble(200);
        }
        if (!init && rightCornerReset.update(controls.held(Action.RESET_RIGHT_CORNER), now)) {
            resetToSpot(ResetPositions.Corner.RIGHT);
            gamepad1.rumble(200);
        }
        if (turretZero.update(controls.held(Action.TURRET_ZERO), now)) {
            robot.turret.zeroHere();
            gamepad2.rumble(200);
            log.event("turret zeroed here by the operator");
        }
    }

    /**
     * The camera sets the pose, if it can; the Match page and the match log say what happened. A 1 s
     * hold, live in INIT, so one person with one gamepad can put a pose carried from the last run
     * right before PLAY.
     */
    private void relocalize() {
        poseNote = robot.drive.relocalizeFromCamera();
        log.event("pose: " + poseNote);
    }

    @Override
    protected void onInitLoop() {
        // Camera reset and turret zero move nothing, so they work before PLAY too.
        holdResets(System.nanoTime(), true);
        display.status("Basic Drive", Display.Level.OK, "ready — Share (Back) shows the controls");
        showControlsNotes();
        if (poseNote != null) display.line("Last pose fix: " + poseNote);
        showPinpoint();
    }

    @Override
    protected void onLoop() {
        long now = System.nanoTime();
        boolean slow = controls.held(Action.SLOW);
        boolean turbo = controls.held(Action.TURBO) && !slow;
        double scale = unitRange(slow ? TeleOpConfig.slowSpeed : turbo ? TeleOpConfig.turboSpeed : TeleOpConfig.normalSpeed);

        double forward = deadband(gamepad1.left_stick_y) * FORWARD_SIGN * scale;
        double strafe = deadband(gamepad1.left_stick_x) * STRAFE_SIGN * scale;
        double turn = deadband(gamepad1.right_stick_x) * TURN_SIGN * scale;
        boolean leftStick = Math.abs(forward) > CANCEL_STICK || Math.abs(strafe) > CANCEL_STICK;
        boolean rightStick = Math.abs(turn) > CANCEL_STICK;

        holdResets(now, false);

        // Where a shot would go, every loop: the turret aims there in AIM; LOCKED, the chassis aim
        // and the aim gate use it.
        if (turretMode == TurretMode.AIM) {
            aimer.update(setup.alliance(), AIM_SWITCH_BAND_IN);
        } else {
            aimer.solve(setup.alliance(), AIM_SWITCH_BAND_IN);
        }
        double aimDeg = aimer.aimDeg();
        aimErrorDeg = Double.isNaN(aimDeg) ? Double.NaN : aimDeg - robot.turret.angleDegrees();

        // A FLOWER pick or a GARDEN gather drives the robot while held, one at a time; letting go or
        // a stick hands it back, this loop.
        boolean sticksMoved = leftStick || rightStick;
        picker.update(controls.held(Action.FLOWER_PICK) && !gatherer.active(), sticksMoved);
        gatherer.update(controls.held(Action.GARDEN_GATHER) && !picker.active(), sticksMoved);
        boolean chassisAim = chassisAim(rightStick);
        if (!routineActive()) {
            double turnCommand = chassisAim ? chassisAimTurn() : turn;
            robot.drive.setAccelLimited(!turbo);
            if (robotCentric()) {
                robot.drive.driveRobotCentric(forward, strafe, turnCommand);
            } else {
                robot.drive.drive(forward, strafe, turnCommand);
            }
            intakeArbiter.decide(controls.held(Action.INTAKE), controls.held(Action.DRIVER_UNJAM),
                    controls.held(Action.UNJAM), controls.held(Action.OPERATOR_INTAKE));
            setIntake(intakeArbiter.state());
        }
        // The pick or the gather runs the intake itself while it is active.
        log.put("/Controls/IntakeOwner", routineActive() ? INTAKE_OWNER_PICK : intakeArbiter.owner().ordinal());
        log.put("/Controls/TrimDeg", aimer.trimDeg());
        log.put("/Controls/LauncherTrimTps", launcherTrimTps);
        log.put("/Controls/AimErrorDeg", aimErrorDeg);

        fire();
        lightBar();

        display.status("Mode", robotCentric() ? Display.Level.WARN : Display.Level.OK,
                robotCentric() || !robot.drive.isFieldCentric() ? "robot-centric" : "field-centric");
        display.status("Speed", turbo ? Display.Level.WARN : Display.Level.OK,
                slow ? "slow" : turbo ? "TURBO" : "normal");
        if (headingReset.pending() || cameraReset.pending() || leftCornerReset.pending() || rightCornerReset.pending() || turretZero.pending()) {
            display.line("Keep holding (1 s) to reset…");
        }
        display.status("FLOWER stick", Display.Level.OK, (robot.flowerStick.waitingToClear() ? "out until clear of the FLOWER" : robot.flowerStick.extended() ? "out" : "in") + (robot.flowerStick.limp() ? ", servo limp" : ", servo holding"));
        String pick = picker.line();
        if (!pick.isEmpty()) display.line(pick);
        String gather = gatherer.line();
        if (!gather.isEmpty()) display.line(gather);
        if (controls.held(Action.CHASSIS_AIM)) {
            display.line(chassisAimCancelled ? "Chassis aim: cancelled by the right stick (let go to re-arm)"
                    : turretMode == TurretMode.AIM ? "Chassis aim: off while the turret aims (lock it first)"
                    : "Chassis aim: " + aimer.source());
        }
        robot.intake.describe(display);
        display.line("Intake: " + (picker.active() ? "FLOWER pick" : gatherer.active() ? "GARDEN gather" : intakeArbiter.owner().toString().toLowerCase(Locale.US)));
        robot.launcher.describe(display);
        if (launcherTrimTps != 0.0) {
            display.line(String.format(Locale.US, "Launcher trim %+.0f ticks/s: launch speed %.0f",
                    launcherTrimTps, launchSpeed()));
        }
        robot.feeder.describe(display);
        robot.turret.describe(display);
        if (turretMode == TurretMode.AIM) {
            display.line(aimer.line());
        } else {
            display.status("Turret", Display.Level.WARN, "LOCKED forward: the driver points the robot (chassis aim)"
                    + (aimer.trimDeg() == 0.0 ? "" : String.format(Locale.US, " · trim %+.1f°", aimer.trimDeg())));
        }
        display.line("Gates (shadow" + (TeleOpConfig.enforceAtSpeedGate || TeleOpConfig.enforceAimGate ? ", some enforced" : "")
                + "): " + gates.summary());
        if (poseNote != null) display.line("Last pose fix: " + poseNote);
        if (autoParkNote != null) display.line(autoParkNote);
        recordShot();
        showAim();
        showPinpoint();
        display.line(loopTimer.summary());
        publish();
    }

    /**
     * Whether the chassis aim drives the turn this loop: held, the turret LOCKED (an aiming turret
     * does the pointing), and not cancelled by the right stick since the button went down. A
     * cancel lasts until the button is let go.
     */
    private boolean chassisAim(boolean rightStick) {
        if (!controls.held(Action.CHASSIS_AIM)) {
            chassisAimCancelled = false;
            return false;
        }
        if (turretMode != TurretMode.LOCKED || chassisAimCancelled || routineActive()) return false;
        if (rightStick) {
            chassisAimCancelled = true;
            log.event("chassis aim cancelled: right stick");
            return false;
        }
        return true;
    }

    /**
     * The turn that brings the shot onto our CELL: the aim's error, which with the turret locked
     * is how far the robot must turn, times {@link TeleOpConfig#chassisAimKp}, clamped. No aim, no
     * turn: the robot holds its heading rather than guess.
     */
    private double chassisAimTurn() {
        if (Double.isNaN(aimErrorDeg)) return 0.0;
        double kp = TeleOpConfig.chassisAimKp > 0.0 ? TeleOpConfig.chassisAimKp : 0.0;
        double max = unitRange(TeleOpConfig.chassisAimMaxTurn);
        double turn = kp * Math.toRadians(aimErrorDeg);
        return Math.max(-max, Math.min(max, turn));
    }

    /** A FLOWER pick or a GARDEN gather is driving the robot and running the intake. */
    private boolean routineActive() {
        return picker.active() || gatherer.active();
    }

    private void setIntake(IntakeSubsystem.State state) {
        switch (state) {
            case IN:
                robot.intake.intake();
                break;
            case OUT:
                robot.intake.eject();
                break;
            default:
                robot.intake.idle();
        }
    }

    /**
     * The feeder, decided every loop: unjam wins (clearing a jam is always allowed), then fire.
     * Fire feeds at once unless an enforced gate is closed, and an enforced gate only holds back
     * the start of a feed, never cuts one ({@code FeederSubsystem.requestFor}). Every press is one
     * log line with what a gate would have said.
     */
    private void fire() {
        gates.evaluate(launchMode, robot.launcher.atSpeed(), aimErrorDeg, TeleOpConfig.aimToleranceDeg);
        boolean fireHeld = controls.held(Action.FIRE);
        boolean open = gates.enforcedOpen(TeleOpConfig.enforceAtSpeedGate, TeleOpConfig.enforceAimGate);
        if (fireHeld && !lastFire) {
            log.event(fireLine(open));
        }
        lastFire = fireHeld;
        // requestFor's "at speed" is the enforced gates' verdict here, and it is always required:
        // with nothing enforced the gates are always open.
        FeederSubsystem.State feed = FeederSubsystem.requestFor(fireHeld, unjamHeld(),
                open, true, robot.feeder.state() == FeederSubsystem.State.FEED);
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
    }

    /** Unjam on either pad: it reverses the feeder as well as the intake. */
    private boolean unjamHeld() {
        return controls.held(Action.UNJAM) || controls.held(Action.DRIVER_UNJAM);
    }

    /** One fire press, for the log: launcher, aim, trim, pose, gates, and what happened. */
    private String fireLine(boolean open) {
        Pose pose = robot.drive.pose();
        String where = pose == null ? "no pose"
                : String.format(Locale.US, "pose (%.1f, %.1f, %.0f°)%s", pose.x(), pose.y(),
                Math.toDegrees(pose.heading()), robot.drive.poseReferenced() ? "" : " not on the field");
        String outcome = unjamHeld() ? "not fed: unjam is held"
                : !open ? "WAITING: an enforced gate is closed"
                : gates.anyWouldBlock() ? "fired; a gate would have blocked it (shadow)" : "fired; gates clear";
        return String.format(Locale.US, "fire: launcher %.0f/%.0f ticks/s%s, aim %s (%s), trim %+.1f°, turret %s, %s, gates %s → %s",
                (robot.launcher.leftTicksPerSecond() + robot.launcher.rightTicksPerSecond()) / 2.0,
                robot.launcher.target(), launchMode ? "" : " (idle)",
                Double.isNaN(aimErrorDeg) ? "unknown" : String.format(Locale.US, "%+.1f°", aimErrorDeg),
                aimer.source(), aimer.trimDeg(), turretMode, where, gates.summary(), outcome);
    }

    /** The operator's light bar, sent only when it changes; a short rumble on reaching launch speed. */
    private void lightBar() {
        Bar now = !launchMode ? Bar.IDLE : robot.launcher.atSpeed() ? Bar.AT_SPEED : Bar.SPINNING;
        if (now == bar) return;
        switch (now) {
            case AT_SPEED:
                gamepad2.setLedColor(0.0, 1.0, 0.0, Gamepad.LED_DURATION_CONTINUOUS);
                gamepad2.rumble(250);
                break;
            case SPINNING:
                gamepad2.setLedColor(1.0, 0.6, 0.0, Gamepad.LED_DURATION_CONTINUOUS);
                break;
            default:
                gamepad2.setLedColor(0.0, 0.0, 1.0, Gamepad.LED_DURATION_CONTINUOUS);
        }
        bar = now;
    }

    /** On the INIT page: robot-centric if it is, and a FLOWER pick button Panels cannot use. */
    private void showControlsNotes() {
        if (robotCentric()) {
            display.status("Drive", Display.Level.WARN, "ROBOT-CENTRIC");
        }
        if (TeleOpConfig.flowerPickButton != null && !TeleOpConfig.flowerPickButton.isFaceOrDpad()) {
            display.status("FLOWER pick", Display.Level.WARN, TeleOpConfig.flowerPickButton.label
                    + " is not a face button or D-pad: the pick stays on " + ControlScheme.DEFAULT_FLOWER_PICK.label);
        }
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

    /** The launcher, feeder, aim and controls to Panels, flat keys, one update per loop. Never takes the robot down. */
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
            panels.addData("launch_mode", launchMode ? 1.0 : 0.0);
            panels.addData("launcher_trim_tps", launcherTrimTps);
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
            panels.addData("aim_error_deg", aimErrorDeg);
            panels.addData("trim_deg", aimer.trimDeg());
            panels.addData("gate_speed", gates.verdict(FireGates.Gate.LAUNCHER_AT_SPEED).ordinal());
            panels.addData("gate_aim", gates.verdict(FireGates.Gate.AIM_ON_TARGET).ordinal());
            panels.addData("intake_owner", routineActive() ? INTAKE_OWNER_PICK : intakeArbiter.owner().ordinal());
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
