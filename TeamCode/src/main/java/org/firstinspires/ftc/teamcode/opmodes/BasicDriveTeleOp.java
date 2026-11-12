package org.firstinspires.ftc.teamcode.opmodes;

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
import org.firstinspires.ftc.teamcode.localization.LaunchSpots;
import org.firstinspires.ftc.teamcode.localization.PinpointTrust;
import org.firstinspires.ftc.teamcode.localization.ResetPositions;
import org.firstinspires.ftc.teamcode.localization.StartPosition;
import org.firstinspires.ftc.teamcode.localization.StartPositions;
import org.firstinspires.ftc.teamcode.subsystems.FeederSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.IntakeSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.LaunchSpeedTable;
import org.firstinspires.ftc.teamcode.subsystems.LauncherSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.TurretAim;
import org.firstinspires.ftc.teamcode.subsystems.TurretEncoder;
import org.firstinspires.ftc.teamcode.subsystems.TurretSubsystem;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.vision.CellSighting;
import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.HiveCellState;
import org.firstinspires.ftc.teamcode.vision.HiveTracker;
import org.firstinspires.ftc.teamcode.vision.Vec3;

import java.util.Locale;

/**
 * The match TeleOp: read the gamepads by {@link Action}, tell the subsystems what you want,
 * report. {@link RobotCentricTeleOp} is the same OpMode driving robot-centric, for a match where
 * the Pinpoint's heading cannot be trusted; it is in the Utility menu so nobody picks it by
 * mistake. There is no field / robot-centric button.
 *
 * <h2>Controls</h2>
 *
 * <p>Which input does what is {@link ControlScheme}, one layout for both teams. Both people run
 * the intake (R2, hold) and unjam (L2, hold), the driver winning when both press
 * ({@link IntakeArbiter}); unjam on either pad also reverses the feeder. The intake runs all match
 * from PLAY ({@code TeleOpConfig.intakeAlwaysOn}), so R2 changes nothing unless that is switched
 * off to save battery; there is no toggle to turn it off by accident. It stops taking in while the
 * lane is full and starts again when a shot makes room; nobody's R2 gets past full, only unjam
 * does. The Driver Station's Controls page (Share) and the
 * printed cheat sheet are both generated from the layout. Nothing here names a button.
 *
 * <p>Buttons do what is asked at once. Fire feeds the moment it is held: the checks a shot could
 * wait for ({@link FireGates}: launcher at speed, aim on our CELL) run every loop and are logged
 * with every fire press, but block nothing until their flag in {@link TeleOpConfig} is turned on.
 *
 * <p>Moving a drive stick cancels the driver's automatic routines: the FLOWER pick
 * ({@link FlowerPicker}, which stows the stick and idles the intake as it stops) and the GARDEN
 * gather ({@link GardenGather}) and the drive to a launch spot ({@link LaunchSpotDrive}) on either
 * stick, one of them at a time, and the chassis aim on the right stick, since the left stick keeps
 * driving under it.
 *
 * <p>The launcher ({@link LauncherOwner}): letting go of Square means "done shooting", so the
 * launcher idles when a Square press ends, whoever spun it up. The operator's toggle spins it up
 * early, so Square finds it at speed; while a Square press is firing the toggle cannot idle it and
 * their Fire waits for the volley. Without Square, the operator's toggle idles it.
 *
 * <h2>Aiming</h2>
 *
 * <p>The turret starts every OpMode LOCKED straight ahead; the operator's lock button switches it
 * to AIM at our CELL ({@link TurretAimer}: left half of the field, left CELL; right half, right
 * CELL). While LOCKED, the driver has two ways to shoot. The launch-spot buttons trust the
 * odometry: experimentally, Square drives to the launch spot on the side wall in line with the
 * robot, Triangle (behind {@code TeleOpConfig.launchSpotClose}) to the close one in front of it,
 * and the robot arrives aimed at that half's CELL ({@link LaunchSpotDrive}), and with {@code TeleOpConfig.goToSpotAlsoFires} spins up and
 * fires there while it is held. The chassis aim trusts the camera: it turns the robot onto the UP
 * CELL of ours the camera sees, aims and ranges to where the camera last saw it when the camera is
 * not on it, and uses the odometry only with neither ({@link ChassisAimTarget}); the operator's
 * FIRE then chooses the moment, waiting until the robot is on the CELL
 * ({@code TeleOpConfig.chassisAimGate}). Pose good, the spot; pose off, a gyro reset and the
 * chassis aim. The
 * operator's trim nudges either one and stays until reset. It zeroes itself, assuming it starts a
 * match at its home mark, or taking the Auto's zero when an Auto just ran
 * ({@link org.firstinspires.ftc.teamcode.subsystems.TurretZeroPolicy}); the zero button always
 * wins.
 *
 * <h2>What it reports</h2>
 *
 * <p>The Match page is a glance for the operator and the coach (mentor, 7 Nov 2026), three huge
 * lines and at most one alert: the launch trim (amber while trimmed), the launcher (READY, SPIN,
 * IDLE), the lane tracker's piece count (green while the sensor sees the lane full), and the most urgent of
 * no Pinpoint, intake off, robot-centric, Square refused, FLOWER stick out. Everything else is on
 * the Robot page. The trim is on every shot, Square's included, and is felt as well as seen: one
 * rumble blip up, two down, a long buzz when D-pad down resets it to 0.
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

    /**
     * How long a reset (gyro, camera, pose, turret zero) must be held. A tap does nothing. 0.5 s since
     * 4 Nov 2026: 1 s felt long to the drivers.
     */
    private static final long RESET_HOLD_MS = 500;

    /** The hint while a reset is held, built once. */
    private static final String KEEP_HOLDING = String.format(Locale.US,
            "Keep holding (%.1f s) to reset…", RESET_HOLD_MS / 1000.0);

    /**
     * The buzz when a reset fires: both motors (some pads answer only one), long enough to feel
     * while driving.
     */
    private static final int RESET_RUMBLE_MS = 300;
    /** The launch trim reset's buzz: longer than a trim press's blips, so the two never feel alike. */
    private static final int LAUNCHER_TRIM_RESET_RUMBLE_MS = 400;

    /** The launcher trim step can never be more than this, ticks per second, whatever Panels says. */
    /** Package-private: {@link LaunchTest} caps the same Panels step the same way. */
    static final double MAX_LAUNCHER_TRIM_STEP_TPS = 200.0;

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
    private LaunchSpotDrive launchSpot;
    private final FreshSticks pickSticks = new FreshSticks();
    private final FreshSticks gatherSticks = new FreshSticks();
    private final FreshSticks spotSticks = new FreshSticks();
    /** Who has the launcher, and so when it idles: Square's press or the operator ({@link LauncherOwner}). */
    private final LauncherOwner launcherOwner = new LauncherOwner();
    /** The operator's trim on the launch speed, ticks per second; lasts the OpMode. */
    private double launcherTrimTps;
    private boolean chassisAimCancelled;
    /** The chassis aim's camera-first target ({@link ChassisAimTarget}): where Circle points the robot. */
    private ChassisAimTarget chassisTarget;
    /** The chassis aim drove the turn this loop: the aim gate is enforced for FIRE ({@code TeleOpConfig.chassisAimGate}). */
    private boolean chassisAimDriving;
    /** The press's last chassis aim, for its let-go log line: error, where it came from, whether the camera locked a CELL. */
    private double lastChassisAimErrorDeg = Double.NaN;
    private ChassisAimTarget.Source lastChassisSource = ChassisAimTarget.Source.NONE;
    private boolean chassisSawCell;
    /** Circle is aiming the chassis this loop (held, turret LOCKED): its range is the shot's. */
    private boolean chassisAiming;
    /** When the chassis aim button went down, ns; 0 while it is up. For its log line. */
    private long chassisAimSinceNs;
    private boolean lastFire;
    /** The driver's slow button this loop, for the Robot page. */
    private boolean slowHeld;
    /** FIRE as pressed, before a Square volley holds it back: for that press's one log line. */
    private boolean lastRawFire;
    /** This fire press spun the launcher up from idle, so it feeds only once at launch speed. */
    private boolean spunUpByFire;
    /** The current launch-spot press has had its spin-up decided. */
    private boolean spotPressSeen;
    private Bar bar = Bar.NONE;
    private String poseNote;
    private String autoParkNote;
    private double aimErrorDeg = Double.NaN;

    private TelemetryManager panels;
    private HiveCell nearEndCell;
    private double lastShotRangeIn = Double.NaN;
    private String lastShotRangeSource = "";

    /** Where {@link #shotRangeIn()} took its last range from. */
    private enum RangeFrom { NONE, CAMERA, ANCHOR, ODOMETRY }

    private RangeFrom rangeFrom = RangeFrom.NONE;
    /**
     * The CELL measured to: with {@link RangeFrom#ODOMETRY} ours at the robot's end of the field,
     * with {@link RangeFrom#ANCHOR} the one Circle is on.
     */
    private HiveCell rangeCell;
    /**
     * The camera's and the pose's ranges from the turret axis, inches, as {@link #shotRangeIn()}
     * last found them (NaN when it had none), logged side by side: the same range two ways, so a
     * gap of more than an inch or two is a pose or camera problem.
     */
    private double cameraRangeIn = Double.NaN;
    private double poseRangeIn = Double.NaN;
    /** The launch speed {@code LaunchSpeedTable} gave, before trim, ticks/s; NaN: the configured speed. */
    private double tableTps = Double.NaN;
    /** The range {@link #tableTps} was read at, inches, and whether it was a launch-spot press's spot. */
    private double tableRangeIn = Double.NaN;
    private boolean tableFromSpot;

    /** True for {@link RobotCentricTeleOp}: every drive request is robot-centric. */
    protected boolean robotCentric() {
        return false;
    }

    @Override
    protected void onInit() {
        panels = panelsTelemetry();
        aimer = new TurretAimer(robot);
        // Made once: the camera's bearing, range and frame age for one of our CELLs, NaN unless freshly in view.
        chassisTarget = new ChassisAimTarget(cell -> {
            CellSighting s = freshSighting(cell);
            return s == null ? Double.NaN : TurretAim.fromRobotFrameDeg(s.rowCentreRobot(),
                    TurretSubsystem.turretForwardIn, TurretSubsystem.turretLeftIn);
        }, cell -> {
            CellSighting s = freshSighting(cell);
            return s == null ? Double.NaN : s.groundRangeFromIn(TurretSubsystem.turretForwardIn, TurretSubsystem.turretLeftIn);
        }, cell -> {
            CellSighting s = freshSighting(cell);
            return s == null ? Double.NaN : s.ageMs();
        }, () -> TurretSubsystem.turretForwardIn, () -> TurretSubsystem.turretLeftIn);
        picker = new FlowerPicker(robot, text -> log.event(text));
        gatherer = new GardenGather(robot, text -> log.event(text));
        launchSpot = new LaunchSpotDrive(robot, text -> log.event(text));
        // A locked turret's controls are left out, so the pages show only buttons that work.
        controls = new Controls(TeleOpConfig.flowerPickButton, TurretSubsystem.lockedForward,
                TeleOpConfig.launchSpot, TeleOpConfig.launchSpotClose, gamepad1, gamepad2, driver, operator);
        log.metadata("Controls/LaunchSpot", TeleOpConfig.launchSpot ? "bound" : "off");
        log.metadata("Controls/LaunchSpotClose",
                TeleOpConfig.launchSpot && TeleOpConfig.launchSpotClose ? "bound" : "off");
        log.metadata("Controls/Drive", robotCentric() ? "robot-centric" : "field-centric");
        // Which controller each pad is, as the SDK sees it at INIT: a pad that cannot rumble says so.
        log.metadata("Controls/Gamepad1", String.valueOf(gamepad1.type));
        log.metadata("Controls/Gamepad2", String.valueOf(gamepad2.type));

        // Bound in Action order, so the Controls page reads like the cheat sheet.
        controls.note(Action.DRIVE);
        controls.note(Action.TURN);
        controls.note(Action.INTAKE);
        controls.note(Action.DRIVER_UNJAM);
        controls.note(Action.SLOW);
        controls.note(Action.FLOWER_PICK);
        controls.note(Action.GARDEN_GATHER);
        controls.note(Action.CHASSIS_AIM);
        controls.note(Action.LAUNCH_SPOT);
        controls.note(Action.LAUNCH_SPOT_CLOSE);
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
        controls.on(Action.LAUNCHER_TOGGLE).onPress(() -> {
            boolean spotFiring = launchSpot.wantsLauncher();
            LauncherOwner.Effect effect = launcherOwner.operatorToggle(spotFiring);
            if (effect == LauncherOwner.Effect.KEEP) {
                log.event("launcher: toggle during Square's volley: kept spinning (idles after Square is let go)");
            }
            applyLauncher(effect);
        });
        controls.on(Action.LAUNCHER_TRIM_DOWN).onPress(() -> trimLauncher(-1));
        controls.on(Action.LAUNCHER_TRIM_UP).onPress(() -> trimLauncher(+1));
        controls.on(Action.LAUNCHER_TRIM_RESET).onPress(this::resetLauncherTrim);
        controls.note(Action.FIRE);
        controls.on(Action.TURRET_LOCK).onPress(this::toggleTurretMode);
        controls.on(Action.FLOWER_STICK).onPress(robot.flowerStick::toggle);
        controls.on(Action.TRIM_LEFT).onPress(() -> trim(+1));
        controls.on(Action.TRIM_RIGHT).onPress(() -> trim(-1));
        // An aiming turret has the D-pad, so its trim reset clears the launch trim too.
        controls.on(Action.TRIM_RESET).onPress(() -> {
            trim(0);
            resetLauncherTrim();
        });
        controls.note(Action.TURRET_ZERO);

        operator.note("Launcher and Square", "spin up early when a shot is coming; while the driver "
                + "holds Square, your buttons cannot stop it (no idle, Fire waits for the volley); "
                + "it idles a few seconds after Square is let go, unless you Fire first");
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

    /**
     * Does what {@link LauncherOwner} decided: launch speed, or idle. Idle is
     * {@link TeleOpConfig#launcherIdleTicksPerSecond}; 0 stops.
     */
    private void applyLauncher(LauncherOwner.Effect effect) {
        if (effect == LauncherOwner.Effect.KEEP) return;
        if (effect == LauncherOwner.Effect.SPIN_UP) {
            robot.launcher.spinUp(launchSpeed());
            log.event(String.format(Locale.US, "launcher: launch speed %.0f ticks/s (%s)",
                    launchSpeed(), launcherOwner.owner() == LauncherOwner.Owner.SPOT ? "Square" : "operator"));
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

    /**
     * The launch speed: the speed table's for the range ({@link #updateTableSpeed()}), else the
     * configured one, plus the operator's trim, never below zero.
     */
    private double launchSpeed() {
        double base = Double.isNaN(tableTps) ? LauncherSubsystem.targetTicksPerSecond : tableTps;
        return Math.max(0.0, base + launcherTrimTps);
    }

    /**
     * One launcher trim press, +1 up or -1 down. Takes effect at once if the launcher is at launch
     * speed, and otherwise at the next spin-up. The step is guarded: NaN or negative is no step.
     */
    private void trimLauncher(int direction) {
        double step = TeleOpConfig.launcherTrimStepTps > 0.0
                ? Math.min(TeleOpConfig.launcherTrimStepTps, MAX_LAUNCHER_TRIM_STEP_TPS) : 0.0;
        launcherTrimTps = Math.max(-LauncherSubsystem.targetTicksPerSecond, launcherTrimTps + direction * step);
        if (launcherOwner.spinning()) {
            robot.launcher.spinUp(launchSpeed());
        }
        // Felt, not looked at: one blip up, two down (7 Nov 2026).
        gamepad2.rumbleBlips(direction > 0 ? 1 : 2);
        log.event(String.format(Locale.US, "launcher trim %+.0f ticks/s: launch speed %.0f",
                launcherTrimTps, launchSpeed()));
    }

    /** The launch trim back to 0, at once if the launcher is at launch speed; one long buzz. */
    private void resetLauncherTrim() {
        launcherTrimTps = 0.0;
        if (launcherOwner.spinning()) {
            robot.launcher.spinUp(launchSpeed());
        }
        gamepad2.rumble(LAUNCHER_TRIM_RESET_RUMBLE_MS);
        log.event(String.format(Locale.US, "launcher trim reset to 0: launch speed %.0f", launchSpeed()));
    }

    /**
     * The Match page's first line: the operator's launch-speed trim, huge ({@link Display#huge},
     * about twice ordinary text) so the operator reads it in a glance down at the Driver Station;
     * amber while trimmed, so a trim left on is never missed. "TRIM 0" when there is none (mentor,
     * 7 Nov 2026). The trim is on every shot, Square's included.
     */
    static String launchTrimLine(double trimTps) {
        boolean trimmed = trimTps != 0.0;
        return Display.huge(trimmed ? String.format(Locale.US, "TRIM %+.0f", trimTps) : "TRIM 0", trimmed);
    }

    /**
     * The line under the trim: the launch speed it gives, the speed table's for the range (or the
     * configured one) plus the trim; the next volley's, Square's included.
     */
    static String launchSpeedLine(double launchSpeedTps) {
        return String.format(Locale.US, "launch speed %.0f t/s", launchSpeedTps);
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
     * The resets, each a {@link #RESET_HOLD_MS} hold so a brushed button does nothing, each with a
     * short rumble on the pad that held it ({@link #buzz}, which logs it). In INIT only the ones that move nothing: the camera reset and the turret
     * zero.
     */
    private void holdResets(long now, boolean init) {
        if (!init && headingReset.update(controls.held(Action.RESET_HEADING), now)) {
            robot.drive.resetHeading();
            log.event("gyro reset: field-centric forward = the robot's facing");
            buzz(gamepad1, "gamepad1", "gyro reset");
        }
        if (cameraReset.update(controls.held(Action.CAMERA_RELOCALIZE), now)) {
            relocalize();
            buzz(gamepad1, "gamepad1", "camera reset");
        }
        if (!init && leftCornerReset.update(controls.held(Action.RESET_LEFT_CORNER), now)) {
            resetToSpot(ResetPositions.Corner.LEFT);
            buzz(gamepad1, "gamepad1", "left corner reset");
        }
        if (!init && rightCornerReset.update(controls.held(Action.RESET_RIGHT_CORNER), now)) {
            resetToSpot(ResetPositions.Corner.RIGHT);
            buzz(gamepad1, "gamepad1", "right corner reset");
        }
        if (turretZero.update(controls.held(Action.TURRET_ZERO), now)) {
            robot.turret.zeroHere();
            log.event("turret zeroed here by the operator");
            buzz(gamepad2, "gamepad2", "turret zero");
        }
    }

    /**
     * A reset's rumble, and a {@code rumble:} line in the match log with the pad's type, so a buzz
     * nobody felt (4 Nov 2026, corner and gyro resets) shows whether the code sent it or the pad
     * cannot rumble (a Logitech F310 has no motors).
     */
    private void buzz(Gamepad pad, String name, String why) {
        pad.rumble(1.0, 1.0, RESET_RUMBLE_MS);
        log.event("rumble: " + why + " on " + name + " (" + pad.type + ")");
    }

    /**
     * The camera sets the pose, if it can; the Match page and the match log say what happened. A
     * {@link #RESET_HOLD_MS} hold, live in INIT, so one person with one gamepad can put a pose carried from the last run
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
        showHeadingCheck();
        if (poseNote != null) display.line("Last pose fix: " + poseNote);
        showPinpoint();
    }

    @Override
    protected void onLoop() {
        long now = System.nanoTime();
        boolean slow = controls.held(Action.SLOW);
        slowHeld = slow;
        double scale = unitRange(slow ? TeleOpConfig.slowSpeed : TeleOpConfig.normalSpeed);

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
        // The chassis aim (Circle) is camera first: the CELL the camera sees, then where the camera
        // last saw it (the anchor), and only then the odometry's aim above (ChassisAimTarget). It
        // runs every loop, held or not, so the anchor is ready when the press starts.
        boolean circleHeld = turretMode == TurretMode.LOCKED && controls.held(Action.CHASSIS_AIM);
        chassisAiming = circleHeld;
        Pose here = robot.drive.pose();
        HiveTracker ourHive = robot.hive.of(setup.alliance());
        chassisTarget.update(circleHeld, setup.alliance(),
                TurretAim.upCellOr(setup.alliance(), ourHive == null ? null : ourHive.state(), null),
                here == null ? Double.NaN : here.x(), here == null ? Double.NaN : here.y(),
                here == null ? Double.NaN : here.heading(),
                here != null && robot.drive.pinpointTrust() != PinpointTrust.State.FAULTED,
                robot.drive.poseSets(), aimDeg, aimer.trimDeg());
        if (circleHeld) {
            aimDeg = chassisTarget.aimDeg();
            lastChassisSource = chassisTarget.source();
            chassisSawCell = chassisTarget.cameraThisPress();
        }
        if (chassisTarget.lockedThisLoop()) log.event(chassisLockLine());
        aimErrorDeg = Double.isNaN(aimDeg) ? Double.NaN : aimDeg - robot.turret.angleDegrees();
        if (circleHeld) lastChassisAimErrorDeg = aimErrorDeg;

        // A FLOWER pick, a GARDEN gather or the drive to a launch spot drives the robot while held,
        // one at a time; a stick, or letting go (for a grab, before the dwell), hands it back this
        // loop. Let go in a grab's dwell, it backs out first.
        // Only a stick moved after the press: one still moving from the drive up to it is ignored
        // until it centres (FreshSticks, #495).
        boolean sticksMoved = leftStick || rightStick;
        double nowS = now / 1e9;
        double staleS = staleStickCancelsAfterS();
        boolean pickHeld = controls.held(Action.FLOWER_PICK) && !gatherer.active() && !launchSpot.active();
        picker.update(pickHeld, pickSticks.moved(pickHeld, sticksMoved, nowS, staleS));
        boolean gatherHeld = controls.held(Action.GARDEN_GATHER) && !picker.active() && !launchSpot.active();
        gatherer.update(gatherHeld, gatherSticks.moved(gatherHeld, sticksMoved, nowS, staleS));
        // A volley already under way does not yield to the operator's FIRE (fire() holds it back).
        // Square: the wall row; Triangle: the close row. The row is read when the press starts, and
        // the wall wins if both go down in one loop.
        boolean wallHeld = controls.held(Action.LAUNCH_SPOT);
        boolean closeHeld = controls.held(Action.LAUNCH_SPOT_CLOSE);
        boolean spotHeld = (wallHeld || closeHeld) && !grabActive();
        LaunchSpots.Row spotRow = wallHeld ? LaunchSpots.Row.WALL : LaunchSpots.Row.CLOSE;
        launchSpot.update(spotHeld, spotRow, setup.alliance(), spotSticks.moved(spotHeld, sticksMoved, nowS, staleS),
                controls.held(Action.FIRE) && !launchSpot.wantsLauncher());
        updateTableSpeed();
        spotLauncher();
        boolean chassisAim = chassisAim(rightStick);
        chassisAimDriving = chassisAim;
        if (!routineActive()) {
            double turnCommand = chassisAim ? chassisAimTurn() : turn;
            robot.drive.setAccelLimited(true);
            // The scale is also the wheel cap, so slow is that fraction of full power in every
            // direction: without it, a diagonal or a turn saturated to full power
            // (TeleOpConfig.slowSpeed). The cap holds the chassis aim's turn too: slow turns
            // slowly whoever asks.
            if (robotCentric()) {
                robot.drive.driveRobotCentric(forward, strafe, turnCommand, scale);
            } else {
                robot.drive.drive(forward, strafe, turnCommand, scale);
            }
        }
        // The pick and the gather run the intake themselves; the drive to a launch spot leaves it
        // to the people.
        if (!grabActive()) {
            boolean driverIn = controls.held(Action.INTAKE);
            // TeleOpConfig.intakeAlwaysOn: the intake runs unless something else asks, counted as the
            // operator's request (IntakeArbiter); R2 on either pad runs it while held. The driver
            // wins while held, as does either unjam. A full lane stops the roller and the prefeed
            // whoever asks: only unjam and a feed get past it.
            boolean operatorIn = TeleOpConfig.intakeAlwaysOn || controls.held(Action.OPERATOR_INTAKE);
            intakeArbiter.decide(driverIn, controls.held(Action.DRIVER_UNJAM),
                    controls.held(Action.UNJAM), operatorIn);
            setIntake(intakeArbiter.state());
        }
        // The pick or the gather runs the intake itself while it is active.
        log.put("/Controls/IntakeOwner", grabActive() ? INTAKE_OWNER_PICK : intakeArbiter.owner().ordinal());
        log.put("/Controls/TrimDeg", aimer.trimDeg());
        log.put("/Controls/LauncherTrimTps", launcherTrimTps);
        log.put("/Controls/TableTicksPerSecond", tableTps);
        log.put("/Launch/CameraRangeIn", cameraRangeIn);
        log.put("/Launch/PoseRangeIn", poseRangeIn);
        log.put("/Controls/AimErrorDeg", aimErrorDeg);

        fire();
        lightBar();

        // The Match page is a glance (mentor, 7 Nov 2026): the operator and the coach read it from
        // the Driver Station mid-match, so it is three huge lines and one alert, nothing to parse.
        // Everything a person testing wants is on the Robot page (describeRobotPage).
        display.line(launchTrimLine(launcherTrimTps));
        display.line(launcherLine());
        display.line(piecesLine(robot.colorSensor.lane().count(), robot.colorSensor.full()));
        String alert = matchAlert();
        if (alert != null) display.line(Display.huge(alert, Display.Level.WARN));
        if (headingReset.pending() || cameraReset.pending() || leftCornerReset.pending() || rightCornerReset.pending() || turretZero.pending()) {
            display.line(KEEP_HOLDING);
        }
        recordShot();
        publish();
    }

    /**
     * Whether the chassis aim drives the turn this loop: held, the turret LOCKED (an aiming turret
     * does the pointing), and not cancelled by the right stick since the button went down. A
     * cancel lasts until the button is let go.
     */
    private boolean chassisAim(boolean rightStick) {
        if (!controls.held(Action.CHASSIS_AIM)) {
            if (chassisAimSinceNs != 0) {
                // One line per press, to compare with the launch-spot button's (LaunchSpotDrive).
                // aimErrorDeg is this loop's, the turret's own aim now the button is up; the press's
                // last chassis aim is the one to report.
                log.event(String.format(Locale.US, "chassis aim: let go after %.2f s, aim %s from %s, camera %s, %s",
                        (System.nanoTime() - chassisAimSinceNs) / 1e9,
                        Double.isNaN(lastChassisAimErrorDeg) ? "unknown" : String.format(Locale.US, "%+.1f° off", lastChassisAimErrorDeg),
                        lastChassisSource, chassisSawCell ? "locked a CELL" : "saw none of ours",
                        chassisAimCancelled ? "cancelled by the right stick" : "not cancelled"));
                chassisAimSinceNs = 0;
            }
            chassisAimCancelled = false;
            return false;
        }
        if (chassisAimSinceNs == 0) {
            chassisAimSinceNs = System.nanoTime();
            Pose pose = robot.drive.pose();
            log.event("chassis aim: pressed at " + (pose == null ? "no pose" : String.format(Locale.US,
                    "(%.1f, %.1f, %.0f°)", pose.x(), pose.y(), Math.toDegrees(pose.heading()))));
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

    /** {@code cell}'s camera sighting if it is fresh enough to aim by ({@link TurretAimer#SIGHTING_FRESH_MS}), else null. */
    private CellSighting freshSighting(HiveCell cell) {
        CellSighting s = robot.vision.sighting(cell);
        return s != null && s.ageMs() <= TurretAimer.SIGHTING_FRESH_MS ? s : null;
    }

    /**
     * The chassis aim's first camera lock in a press, one log line: the camera's bearing and range to
     * the CELL beside what the odometry would have said for the same CELL. Their gap is how far off
     * the pose is where it matters for a shot, the evidence for when Circle beats Square.
     */
    private String chassisLockLine() {
        HiveCell cell = chassisTarget.locked();
        CellSighting s = freshSighting(cell);
        double fwd = TurretSubsystem.turretForwardIn;
        double left = TurretSubsystem.turretLeftIn;
        double camDeg = s == null ? Double.NaN : TurretAim.fromRobotFrameDeg(s.rowCentreRobot(), fwd, left);
        double camRange = s == null ? Double.NaN : s.groundRangeFromIn(fwd, left);
        String line = String.format(Locale.US, "chassis aim: camera locked %s CELL, %+.1f° at %.0f in",
                ChassisAimTarget.driverName(cell), camDeg, camRange);
        Pose pose = robot.drive.poseReferenced() ? robot.drive.pose() : null;
        Vec3 row = HiveFieldPoints.rowCentre(cell, HiveCellState.UP);
        if (pose == null || row == null) return line + "; odometry: " + (pose == null ? "pose not on the field" : "no field point");
        double odoDeg = TurretAim.fromFieldDeg(pose.x(), pose.y(), pose.heading(), fwd, left, row.x(), row.y());
        double odoRange = TurretAim.rangeFromFieldIn(pose.x(), pose.y(), pose.heading(), fwd, left, row.x(), row.y());
        return line + String.format(Locale.US, "; odometry %+.1f° at %.0f in (gap %+.1f°, %+.1f in) from (%.1f, %.1f, %.0f°)",
                odoDeg, odoRange, TurretEncoder.wrap180(odoDeg - camDeg), odoRange - camRange,
                pose.x(), pose.y(), Math.toDegrees(pose.heading()));
    }

    /**
     * The launch-spot button's launcher (TeleOpConfig.goToSpotAlsoFires): spun up to the launch
     * speed and trim as the press starts if it is idle, and idled
     * {@link TeleOpConfig#launcherKeepSpinningAfterSquareS} after the press ends, whoever spun it
     * up: letting go is "done shooting" ({@link LauncherOwner}).
     */
    private void spotLauncher() {
        boolean wants = launchSpot.wantsLauncher();
        if (wants && !spotPressSeen) {
            spotPressSeen = true;
            applyLauncher(launcherOwner.spotPressStarted());
        } else if (!wants && spotPressSeen) {
            spotPressSeen = false;
            applyLauncher(launcherOwner.spotPressEnded(System.nanoTime() / 1e9, keepSpinningAfterSquareS()));
        }
        applyLauncher(launcherOwner.tick(System.nanoTime() / 1e9));
    }

    /**
     * The Robot page's top: what the Match page used to carry for the people testing (7 Nov 2026),
     * above every subsystem's own block.
     */
    @Override
    protected void describeRobotPage(Display display) {
        display.section("Basic Drive");
        display.line(launchSpeedLine(launchSpeed()));
        display.status("Intake", intakeRunning() ? Display.Level.OK : Display.Level.WARN, intakeState()
                + (TeleOpConfig.intakeAlwaysOn ? "" : " (battery saving: runs while R2 is held)"));
        String spot = launchSpot.line();
        if (!spot.isEmpty()) display.line(spot);
        String pick = picker.line();
        if (!pick.isEmpty()) display.line(pick);
        String gather = gatherer.line();
        if (!gather.isEmpty()) display.line(gather);
        if (controls.held(Action.CHASSIS_AIM)) {
            display.line(chassisAimCancelled ? "Chassis aim: cancelled by the right stick (let go to re-arm)"
                    : turretMode == TurretMode.AIM ? "Chassis aim: off while the turret aims (lock it first)"
                    : "Chassis aim: " + chassisTarget.describe());
        }
        showPinpoint();
        display.status("Mode", robotCentric() ? Display.Level.WARN : Display.Level.OK,
                robotCentric() || !robot.drive.isFieldCentric() ? "robot-centric" : "field-centric");
        display.status("Speed", Display.Level.OK, slowHeld ? "slow" : "normal");
        display.status("FLOWER stick", Display.Level.OK, (robot.flowerStick.waitingToClear() ? "out until clear of the FLOWER" : robot.flowerStick.extended() ? "out" : "in") + (robot.flowerStick.limp() ? ", servo limp" : ", servo holding"));
        display.line("Intake: " + (picker.active() ? "FLOWER pick" : gatherer.active() ? "GARDEN gather" : intakeArbiter.owner().toString().toLowerCase(Locale.US)));
        if (!LaunchSpeedTable.enabled) {
            display.line(String.format(Locale.US, "Speed table: OFF, launch speed %.0f + trim",
                    LauncherSubsystem.targetTicksPerSecond));
        } else if (!Double.isNaN(tableTps)) {
            display.line(String.format(Locale.US, "Speed table: ON, %.0f ticks/s at %.0f in (%s)", tableTps,
                    tableRangeIn, tableFromSpot ? "launch spot" : "range"));
        } else {
            display.line(LaunchSpeedTable.usableRows() > 0 ? "Speed table: ON, no range yet: configured speed"
                    : "Speed table: ON but no rows: configured speed");
        }
        if (!Double.isNaN(lastShotRangeIn)) {
            display.line(String.format(Locale.US, "Last shot: %.0f in (%s) at %.0f ticks/s", lastShotRangeIn,
                    lastShotRangeSource, robot.launcher.lastShotTargetTicksPerSecond()));
        }
        if (turretMode == TurretMode.AIM) {
            display.line(aimer.line());
        } else {
            display.status("Turret", Display.Level.WARN, "LOCKED forward: the driver points the robot (chassis aim)"
                    + (aimer.trimDeg() == 0.0 ? "" : String.format(Locale.US, " · trim %+.1f°", aimer.trimDeg())));
        }
        display.line("Gates (shadow" + (TeleOpConfig.enforceAtSpeedGate || TeleOpConfig.enforceAimGate ? ", some enforced" : "")
                + "): " + gates.summary());
        showHeadingCheck();
        if (poseNote != null) display.line("Last pose fix: " + poseNote);
        if (autoParkNote != null) display.line(autoParkNote);
        showAim();
    }

    /** The Match page's launcher line, huge: READY in green, SPIN in amber, IDLE plain. */
    private String launcherLine() {
        if (!launcherOwner.spinning()) return Display.huge("IDLE", (Display.Level) null);
        return robot.launcher.atSpeed() ? Display.huge("READY", Display.Level.OK)
                : Display.huge("SPIN", Display.Level.WARN);
    }

    /**
     * The Match page's lane line, huge: the lane tracker's count, green while the sensor sees the
     * lane full (the robot's light means the same). Just the number, no "FULL" beside it, which read
     * as a contradiction when the two disagreed (mentor, 7 Nov 2026). The count is the tracker's guess
     * (data only, never decided from); a green count under 4 is the tracker missing a piece, plain to
     * the drive team and the coach, which is how it gets better.
     */
    static String piecesLine(int count, boolean sensorFull) {
        return Display.huge(count + (count == 1 ? " PIECE" : " PIECES"), sensorFull ? Display.Level.OK : null);
    }

    /**
     * The one thing that needs doing, most urgent first, or null: the Match page's fourth line, huge
     * and amber. A lost Pinpoint has its own alert above the page (RobotOpMode).
     */
    private String matchAlert() {
        if (!robot.drive.hasHeading() && !robot.drive.pinpointLost()) return "NO PINPOINT";
        if (TeleOpConfig.intakeAlwaysOn && !intakeRunning()) return "INTAKE OFF";
        if (robotCentric() || !robot.drive.isFieldCentric()) return "ROBOT-CENTRIC";
        if (launchSpot.refused()) return launchSpot.row() == LaunchSpots.Row.CLOSE ? "TRIANGLE REFUSED" : "SQUARE REFUSED";
        if (robot.flowerStick.extended() || robot.flowerStick.waitingToClear()) return "FLOWER STICK OUT";
        return null;
    }

    /** The intake is running, or waiting on a full lane, rather than off. */
    private boolean intakeRunning() {
        return grabActive() || intakeArbiter.owner() != IntakeArbiter.Owner.NONE;
    }

    /** The Match page's intake line: who runs it, or OFF. */
    private String intakeState() {
        if (picker.active()) return "FLOWER pick";
        if (gatherer.active()) return "GARDEN gather";
        if (!intakeRunning()) return "OFF";
        return robot.colorSensor.full() ? "on, holding (lane full)" : "on";
    }

    /** A FLOWER pick or a GARDEN gather is driving the robot and running the intake. */
    private boolean grabActive() {
        return picker.active() || gatherer.active();
    }

    /** A grab or the drive to a launch spot is driving the robot: the sticks wait. */
    private boolean routineActive() {
        return grabActive() || launchSpot.active();
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
     * Pressed with the launcher idle, fire spins it up first and feeds once it reaches launch
     * speed, whatever the gates are set to: 17 presses on 1 Nov 2026 fired an idle launcher and did
     * nothing. Otherwise it feeds at once unless an enforced gate is closed, and an enforced gate
     * only holds back the start of a feed, never cuts one ({@code FeederSubsystem.requestFor}).
     * Every press is one log line with what a gate would have said.
     */
    private void fire() {
        boolean rawFire = controls.held(Action.FIRE);
        // A Square press that fires owns the shot: the operator's FIRE feeds nothing until the press
        // ends, so the volley still waits for the spot, aim and speed. Without goToSpotAlsoFires,
        // Square only drives and FIRE is theirs.
        boolean spotFiring = launchSpot.wantsLauncher();
        if (rawFire && !lastRawFire && spotFiring) {
            log.event("fire: held back, Square's volley is firing");
        }
        lastRawFire = rawFire;
        boolean fireHeld = rawFire && !spotFiring;
        boolean pressed = fireHeld && !lastFire;
        if (pressed) {
            boolean wasIdle = !launcherOwner.spinning();
            applyLauncher(launcherOwner.operatorFire(unjamHeld()));
            if (wasIdle && launcherOwner.spinning()) spunUpByFire = true;
        }
        if (!fireHeld) spunUpByFire = false;
        gates.evaluate(launcherOwner.spinning(), robot.launcher.atSpeed(), aimErrorDeg, TeleOpConfig.aimToleranceDeg);
        // The aim gate also holds while the driver's chassis aim is turning the robot: the driver
        // points, the operator chooses the moment, and FIRE waits until the robot is on the CELL.
        boolean enforceAim = TeleOpConfig.enforceAimGate || (TeleOpConfig.chassisAimGate && chassisAimDriving);
        boolean open = gates.enforcedOpen(TeleOpConfig.enforceAtSpeedGate, enforceAim)
                && (!spunUpByFire || robot.launcher.atSpeed());
        if (pressed) {
            log.event(fireLine(open));
        }
        lastFire = fireHeld;
        // requestFor's "at speed" is the enforced gates' verdict here, and it is always required:
        // with nothing enforced the gates are always open. The launch-spot button's volley feeds
        // through the same request: it has already waited for speed and settle as an Auto's
        // LaunchAll does (SpotVolley), and the operator's FIRE, which it yields to, comes first.
        boolean spotFeed = !fireHeld && launchSpot.feedRequested();
        FeederSubsystem.State feed = FeederSubsystem.requestFor(fireHeld || spotFeed, unjamHeld(),
                fireHeld ? open : spotFeed, true, robot.feeder.state() == FeederSubsystem.State.FEED);
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
                : !open && spunUpByFire ? "SPINNING UP: feeds at launch speed"
                : !open ? "WAITING: an enforced gate is closed"
                : gates.anyWouldBlock() ? "fired; a gate would have blocked it (shadow)" : "fired; gates clear";
        return String.format(Locale.US, "fire: launcher %.0f/%.0f ticks/s%s, aim %s (%s), trim %+.1f°, turret %s, %s, gates %s → %s",
                (robot.launcher.leftTicksPerSecond() + robot.launcher.rightTicksPerSecond()) / 2.0,
                robot.launcher.target(), launcherOwner.spinning() ? "" : " (idle)",
                Double.isNaN(aimErrorDeg) ? "unknown" : String.format(Locale.US, "%+.1f°", aimErrorDeg),
                aimer.source(), aimer.trimDeg(), turretMode, where, gates.summary(), outcome);
    }

    /** The operator's light bar, sent only when it changes; a short rumble on reaching launch speed. */
    private void lightBar() {
        Bar now = !launcherOwner.spinning() ? Bar.IDLE : robot.launcher.atSpeed() ? Bar.AT_SPEED : Bar.SPINNING;
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
            lastShotRangeSource = rangeSource();
            log.event(String.format(Locale.US, "shot %d: target %.0f ticks/s, dip %.0f, range %s",
                    robot.launcher.shots(), robot.launcher.lastShotTargetTicksPerSecond(),
                    robot.launcher.lastShotDipTicksPerSecond(),
                    Double.isNaN(lastShotRangeIn) ? "unknown"
                            : String.format(Locale.US, "%.0f in (%s)", lastShotRangeIn, lastShotRangeSource)));
        }
    }

    /**
     * The horizontal range from the turret axis to the CELL a shot was aimed at, inches (the speed
     * table's range). While Circle aims the chassis it is the range to the CELL Circle is on: the
     * camera's, else to where the camera last saw it ({@link ChassisAimTarget}'s anchor), so the
     * aim and the launch speed are for the same CELL and neither needs the pose to be right.
     * Otherwise: the camera's ground range to the nearest fresh CELL of our alliance (any CELL if the alliance is not chosen yet), else the
     * pose's range from the turret axis to the raised row of our CELL at the robot's end of the
     * field, the one the turret aims at ({@link TurretAim#nearEndCell}), else NaN. Sets
     * {@link #lastShotRangeSource} to say which.
     */
    private double shotRangeIn() {
        Alliance alliance = setup.alliance();
        double best = Double.NaN;
        for (CellSighting sighting : robot.vision.sightings()) {
            if (sighting == null || sighting.ageMs() > SIGHTING_FRESH_MS) continue;
            if (alliance != Alliance.UNKNOWN && !sighting.cell().belongsTo(alliance)) continue;
            double range = sighting.groundRangeFromIn(TurretSubsystem.turretForwardIn, TurretSubsystem.turretLeftIn);
            if (Double.isNaN(best) || range < best) best = range;
        }
        cameraRangeIn = best;
        poseRangeIn = poseRangeIn(alliance);
        double circleRange = chassisAiming ? chassisTarget.rangeIn() : Double.NaN;
        if (!Double.isNaN(circleRange)) {
            boolean camera = chassisTarget.source() == ChassisAimTarget.Source.CAMERA;
            if (camera) cameraRangeIn = circleRange;
            rangeFrom = camera ? RangeFrom.CAMERA : RangeFrom.ANCHOR;
            rangeCell = chassisTarget.locked();
            return circleRange;
        }
        if (!Double.isNaN(best)) {
            rangeFrom = RangeFrom.CAMERA;
            return best;
        }
        rangeFrom = Double.isNaN(poseRangeIn) ? RangeFrom.NONE : RangeFrom.ODOMETRY;
        return poseRangeIn;
    }

    /**
     * The pose's range from the turret axis to the CELL a shot would aim at (see
     * {@link #shotRangeIn()}), inches; NaN without a referenced pose, an alliance or a field point.
     * It is the near-end CELL whatever the HIVE's state: the range must be to the CELL the shot is
     * aimed at, and the operator decides whether that one is up. Sets {@link #rangeCell}.
     */
    private double poseRangeIn(Alliance alliance) {
        Pose pose = robot.drive.poseReferenced() ? robot.drive.pose() : null;
        if (pose == null || alliance == Alliance.UNKNOWN) return Double.NaN;
        nearEndCell = TurretAim.nearEndCell(alliance, pose.y(), nearEndCell, HiveFieldPoints.AXLE_Y_IN, AIM_SWITCH_BAND_IN);
        Vec3 row = nearEndCell == null ? null : HiveFieldPoints.rowCentre(nearEndCell, HiveCellState.UP);
        if (row == null) return Double.NaN;
        rangeCell = nearEndCell;
        return TurretAim.rangeFromFieldIn(pose.x(), pose.y(), pose.heading(),
                TurretSubsystem.turretForwardIn, TurretSubsystem.turretLeftIn, row.x(), row.y());
    }

    /** Where {@link #shotRangeIn()}'s last answer came from, for a log line: "" when it had none. */
    private String rangeSource() {
        switch (rangeFrom) {
            case CAMERA:
                return "camera";
            case ANCHOR:
                return "where the camera last saw " + rangeCell;
            case ODOMETRY:
                return "odometry, " + rangeCell;
            default:
                return "";
        }
    }

    /**
     * The launch speed from {@code LaunchSpeedTable}, once per loop, before the launch-spot press
     * and the operator's buttons spin anything up. The range is the camera's, as for FIRE, whenever
     * it sees one of our CELLs: the table's rows were measured with it (4 Nov 2026), and a press's
     * planned spot range ran up to 4 in long at RIGHT_LAUNCH_CATCH, so Square fired faster than the
     * operator's FIRE from the same place. Without a camera range, a launch-spot press uses its
     * spot's range (the wheels spin up for it on the way), else {@link #shotRangeIn()}'s odometry.
     * Held within half the launcher's tolerance
     * ({@link LaunchSpeedTable#held}) and never changed while the feeder feeds, so a range that
     * wobbles never re-arms at-speed or the shot detector mid-volley. With the table switched off
     * ({@link LaunchSpeedTable#enabled}) or no usable row, the configured speed stands, exactly as
     * before the table.
     */
    private void updateTableSpeed() {
        if (robot.feeder.state() == FeederSubsystem.State.FEED) return;
        double next;
        double range = Double.NaN;
        boolean fromSpot = false;
        if (!LaunchSpeedTable.enabled || LaunchSpeedTable.usableRows() == 0) {
            next = Double.NaN;
        } else {
            double shotRange = shotRangeIn();
            double spotRange = launchSpot.spotRangeIn();
            fromSpot = rangeFrom != RangeFrom.CAMERA && !Double.isNaN(spotRange);
            range = fromSpot ? spotRange : shotRange;
            next = LaunchSpeedTable.held(tableTps, LaunchSpeedTable.ticksPerSecondFor(range),
                    LauncherSubsystem.toleranceTicksPerSecond / 2.0);
        }
        if (Double.compare(next, tableTps) == 0) return;
        tableTps = next;
        tableRangeIn = range;
        tableFromSpot = fromSpot;
        if (launcherOwner.spinning()) robot.launcher.spinUp(launchSpeed());
        log.event(Double.isNaN(next)
                ? String.format(Locale.US, "launcher: speed table %s, configured speed %.0f ticks/s",
                        LaunchSpeedTable.enabled ? "has no rows" : "off", launchSpeed())
                : String.format(Locale.US, "launcher: speed table %.0f ticks/s at %.0f in (%s), launch speed %.0f",
                        next, range, fromSpot ? "launch spot" : rangeSource(), launchSpeed()));
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
            panels.addData("launch_mode", launcherOwner.spinning() ? 1.0 : 0.0);
            panels.addData("launcher_trim_tps", launcherTrimTps);
            panels.addData("table_tps", tableTps);
            panels.addData("shots", robot.launcher.shots());
            panels.addData("last_shot_dip_tps", robot.launcher.lastShotDipTicksPerSecond());
            panels.addData("last_shot_range_in", lastShotRangeIn);
            panels.addData("feeder_power", robot.feeder.power());
            // The feeder's own encoder: the turret is locked and has none (TurretSubsystem.lockedForward).
            panels.addData("feeder_tps", robot.feeder.ticksPerSecond());
            panels.addData("feeder_amps", robot.feeder.currentAmps());
            LanePanels.publish(panels, robot.colorSensor.lane());
            panels.addData("turret_mode", turretMode == TurretMode.AIM ? 1 : 0);
            panels.addData("turret_angle_deg", robot.turret.angleDegrees());
            panels.addData("turret_target_deg", robot.turret.targetDegrees());
            panels.addData("turret_error_deg", robot.turret.errorDegrees());
            panels.addData("aim_error_deg", aimErrorDeg);
            panels.addData("trim_deg", aimer.trimDeg());
            panels.addData("gate_speed", gates.verdict(FireGates.Gate.LAUNCHER_AT_SPEED).ordinal());
            panels.addData("gate_aim", gates.verdict(FireGates.Gate.AIM_ON_TARGET).ordinal());
            panels.addData("intake_owner", grabActive() ? INTAKE_OWNER_PICK : intakeArbiter.owner().ordinal());
            // How far the shadow pose (what still fixes would do) sits from the real one; graph it.
            panels.addData("shadow_offset_in", robot.drive.shadow().offsetIn());
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
                    sighting.groundRangeFromIn(TurretSubsystem.turretForwardIn, TurretSubsystem.turretLeftIn)));
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

    /** {@link TeleOpConfig#launcherKeepSpinningAfterSquareS}, guarded: [0, 10] s, NaN is 0 (idle at once). */
    private static double keepSpinningAfterSquareS() {
        double v = TeleOpConfig.launcherKeepSpinningAfterSquareS;
        return v > 0.0 ? Math.min(v, 10.0) : 0.0;
    }

    /** {@link TeleOpConfig#staleStickCancelsAfterS}, guarded: [0, 2] s, NaN is 0 (the old rule). */
    private static double staleStickCancelsAfterS() {
        double v = TeleOpConfig.staleStickCancelsAfterS;
        return v > 0.0 ? Math.min(v, 2.0) : 0.0;
    }

    private static double deadband(double value) {
        return Math.abs(value) < STICK_DEADBAND ? 0.0 : value;
    }
}
