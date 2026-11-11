package org.firstinspires.ftc.teamcode.opmodes;

import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.ivy.commands.Commands;
import com.pedropathing.math.Pose;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.VoltageSensor;

import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.RobotDifferences;
import org.firstinspires.ftc.teamcode.controls.Bindings;
import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.controls.Handoff;
import org.firstinspires.ftc.teamcode.controls.LongPress;
import org.firstinspires.ftc.teamcode.controls.MatchSetup;
import org.firstinspires.ftc.teamcode.hardware.ActiveConfig;
import org.firstinspires.ftc.teamcode.logging.AdvantageScopeKeys;
import org.firstinspires.ftc.teamcode.logging.GamepadLog;
import org.firstinspires.ftc.teamcode.logging.MatchLog;
import org.firstinspires.ftc.teamcode.logging.RobotComponents;
import org.firstinspires.ftc.teamcode.logging.HiveComponents;
import org.firstinspires.ftc.teamcode.logging.MatchLogFiles;
import org.firstinspires.ftc.teamcode.logging.BuildStamp;
import org.firstinspires.ftc.teamcode.logging.MotionWatch;
import org.firstinspires.ftc.teamcode.logging.Tunables;
import org.firstinspires.ftc.teamcode.subsystems.IndicatorSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.ColorSensorSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.FeedGateSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.IntakeSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.LaneFull;
import org.firstinspires.ftc.teamcode.subsystems.LaneTracker;
import org.firstinspires.ftc.teamcode.subsystems.NectarReject;
import org.firstinspires.ftc.teamcode.subsystems.LauncherResponse;
import org.firstinspires.ftc.teamcode.subsystems.LauncherSubsystem;
import org.firstinspires.ftc.teamcode.localization.PinpointTrust;
import org.firstinspires.ftc.teamcode.localization.ImpactWatch;
import org.firstinspires.ftc.teamcode.localization.ResetPositions;
import org.firstinspires.ftc.teamcode.localization.CalibrationSpots;
import org.firstinspires.ftc.teamcode.localization.CameraStart;
import org.firstinspires.ftc.teamcode.localization.LocalizationTuning;
import org.firstinspires.ftc.teamcode.localization.StartCheck;
import org.firstinspires.ftc.teamcode.localization.SettledStats;
import org.firstinspires.ftc.teamcode.localization.StartPosition;
import org.firstinspires.ftc.teamcode.localization.StartPositions;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.FieldFrame;
import org.firstinspires.ftc.teamcode.util.FieldLayers;
import org.firstinspires.ftc.teamcode.util.Throttle;
import org.firstinspires.ftc.teamcode.util.FieldView;
import org.firstinspires.ftc.teamcode.util.PanelsSwitch;
import org.firstinspires.ftc.teamcode.subsystems.Subsystem;
import org.firstinspires.ftc.teamcode.subsystems.TurretZeroPolicy;
import org.firstinspires.ftc.teamcode.util.LoopTimer;
import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.HiveTracker;
import org.firstinspires.ftc.teamcode.util.WelfordVariance;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The base class for every OpMode that runs the robot. It owns the loop, so your OpMode is only
 * the part that is actually about your OpMode.
 *
 * <pre>{@code
 * @TeleOp(name = "My OpMode", group = "Drive")
 * public class MyOpMode extends RobotOpMode {
 *     @Override
 *     protected void onLoop() {
 *         robot.drive.drive(-gamepad1.left_stick_y, -gamepad1.left_stick_x, -gamepad1.right_stick_x);
 *     }
 * }
 * }</pre>
 *
 * <h2>The loop contract</h2>
 *
 * <p>Every loop, in this order:
 *
 * <ol>
 *   <li><b>Clear the bulk cache.</b> Every hub is in {@code MANUAL} bulk caching, so each sensor
 *       read after this is served from one hub read per loop instead of one per call.</li>
 *   <li><b>Gamepad bindings.</b> {@link #driver} and {@link #operator} fire whatever you bound in
 *       {@code onInit()}.</li>
 *   <li><b>Your {@link #onLoop()}.</b> Read the sticks, tell subsystems what you want, write the
 *       Match page.</li>
 *   <li><b>{@code Scheduler.execute()}.</b> Every subsystem's {@code update()} runs as an Ivy
 *       command, along with any commands you scheduled — so what you asked for above happens in
 *       this same loop, not the next one.</li>
 *   <li><b>The Driver Station page.</b> See {@link Display}: Back/Share on gamepad 1 cycles Match,
 *       Controls and Robot.</li>
 *   <li><b>The Panels Field view.</b> The robot's pose from {@code robot.drive}, drawn through
 *       {@link FieldView} at the throttle's rate (about 10 Hz) and skipped without a Pinpoint. Every
 *       OpMode on this class gets it for free; an OpMode that draws more (a camera's opinion of the
 *       pose, a target) draws into the same view in {@code onLoop()} and sends it, and this send then
 *       finds nothing to do that loop.</li>
 *   <li><b>The match log.</b> See {@link #log}: this loop's state goes to the background writer.</li>
 * </ol>
 *
 * <p>{@code init_loop()} does the same with {@link #onInitLoop()}, except that bindings are not
 * polled: the robot may not move before PLAY. Every subsystem's {@code update()} runs, but none moves
 * a mechanism until {@code robot.start()} at PLAY (see {@link Subsystem}, "Nothing moves before
 * PLAY"). {@link #loopTimer} is lapped at the top of every
 * {@code loop()}.
 *
 * <p>Never set a bulk caching mode, clear the cache, or call {@code Scheduler.reset()} /
 * {@code execute()} / {@code robot.start()} / {@code robot.stop()} in a subclass. That is this
 * class's job, and doing it twice is how an OpMode ends up reading stale sensors or double-stepping a mechanism. {@code MANUAL}
 * without a clear returns the same stale values forever and does not complain; having exactly one
 * place that does both is what makes that mistake impossible.
 *
 * <h2>Before PLAY, and the Auto → TeleOp handoff</h2>
 *
 * <p>{@link #setup} settles the alliance during INIT — vision proposes, X/B on either gamepad
 * overrides — and locks it at PLAY; see {@link MatchSetup}. When an {@code @Autonomous} OpMode
 * stops, this records the alliance, the robot's final pose and the turret's zero in {@link Handoff}.
 * When any other OpMode initializes after that, however long after, it restores the pose
 * and the turret's zero and inherits the alliance, and the Match page says so and how old it is — or
 * says there was no handoff (nothing has run since the app started). Triangle (Y) in INIT discards
 * it. With no Auto before it, the turret is assumed at home and zeroes where it stands at PLAY
 * ({@link TurretZeroPolicy}); a person's zero button always wins. Subclasses never touch either.
 *
 * <p>An Autonomous overrides {@link #startPosition()}. The pose starts there, and during INIT the
 * camera checks the placement ({@link StartCheck}): a confirmed placement is also what confirms the
 * alliance from vision.
 *
 * <h2>The match log</h2>
 *
 * <p>Every run writes a {@code .wpilog} to {@code /sdcard/FIRST/logs/}, which AdvantageScope
 * opens. <b>One file per match</b>: an Autonomous pauses its log when it stops, and the TeleOp
 * that follows (the one that receives its handoff) continues the same file, so Auto, the break and
 * TeleOp share one timeline. A TeleOp on its own starts its own file; see {@link MatchLogFiles}. It
 * records match state, alliance, both gamepads, the pose, loop time, battery voltage and events
 * (init, PLAY, alliance changes, slow loops, stop), with no code in the OpMode. Add anything else
 * with {@code log.put("/Shooter/LeftRPM", rpm)} or {@code log.event("...")}. The loop only copies
 * numbers; a background thread writes the file, and a logging failure never stops the robot. See
 * {@link MatchLog}.
 *
 * <h2>Why the scheduler is always on</h2>
 *
 * <p>There is one way to run: subsystems update through {@code periodic()} commands, and behaviour
 * is commands. TeleOp gets interruptible macros for free, and a command written for Autonomous can
 * be bound to a button unchanged. The rejected alternative was an opt-out for TeleOp, because
 * {@code Scheduler.execute()} allocates a few objects per call. That cost is microseconds, and the
 * opt-out was a second execution model with a trap in it: an OpMode that switched it off had to
 * remember to update every subsystem itself, or look alive while updating nothing.
 */
public abstract class RobotOpMode extends OpMode {

    /** Every mechanism on the robot. Built before {@link #onInit()} runs. */
    protected Robot robot;

    /** Lapped once per {@code loop()}, reset at start. Publish it; see {@code LoopTimeBaseline}. */
    protected final LoopTimer loopTimer = new LoopTimer();

    /** Gamepad 1 bindings. Bind in {@link #onInit()}; they fire after PLAY. */
    protected final Bindings driver = new Bindings("DRIVER — gamepad 1");

    /** Gamepad 2 bindings. Bind in {@link #onInit()}; they fire after PLAY. */
    protected final Bindings operator = new Bindings("OPERATOR — gamepad 2");

    /** The Driver Station screen. Use it to write the Match page in {@link #onLoop()}. */
    protected Display display;

    /**
     * The Panels Field view, in Pedro coordinates, drawn by this class every loop (throttled; see
     * {@link FieldView#shouldDraw()}). Null when Panels could not provide one at init, in which case
     * nothing is drawn and the robot runs on regardless.
     */
    private FieldView fieldView;

    /**
     * Whether Panels is switched on ({@link PanelsSwitch}), read once at INIT. Off, there is no
     * field view and {@link #panelsTelemetry()} is null, so the loop pays nothing for a dashboard
     * nobody can open.
     */
    private boolean panelsEnabled = true;

    /** The alliance, settled during INIT and locked at PLAY. Read {@code setup.alliance()}. */
    protected final MatchSetup setup = new MatchSetup();

    /**
     * This run's match log. Already records the match state, gamepads, pose and loop time; add
     * more with {@code log.put(key, value)} and {@code log.event(text)}. Opened before the robot is
     * built, so a robot that fails to build still leaves a file saying why.
     */
    protected MatchLog log = MatchLog.disabled("not started");

    /** A loop longer than this is an event in the log, with its time. */
    private static final double SLOW_LOOP_EVENT_MS = 80.0;
    /** Battery voltage is a hub command, not bulk-read data, so it is read this often, not every loop. */
    private static final long BATTERY_EVERY_MS = 1000;
    /** How long stop() waits for the log's last writes before leaving the writer to finish alone. */
    private static final long LOG_CLOSE_WAIT_MS = 300;

    /** This run's match ID: when the match's first OpMode started, inherited through the handoff. */
    private String matchId;
    private VoltageSensor battery;
    private long lastBatteryNs;
    /** The battery's last reading, volts: at INIT and then once a second. NaN with no sensor. */
    private double lastBatteryVolts = Double.NaN;
    private Alliance loggedAlliance;
    private MatchSetup.Source loggedSource;

    private List<LynxModule> hubs;
    private boolean prevPageButton;

    /** True when the drive pose was restored from Autonomous, so headings are field-absolute. */
    private Display.Level handoffLevel;
    private String handoffNote;
    /** Whether the pose came from an Auto, or a TeleOp just restarted, rather than starting unknown. */
    private boolean handoffPose;
    /** A handoff was taken at init, so "start fresh" has something to discard. */
    private boolean handoffTaken;
    /** The drive team chose "start fresh" in this INIT. */
    private boolean startedFresh;
    /** Triangle (Y) on either gamepad, held 1 s in INIT: discard the handoff and start fresh. */
    private final LongPress startFresh = new LongPress(1000);

    private StartPosition declaredStart;

    /** How many stack frames of a crash go into the match log: enough to find the line, short enough for one event. */
    private static final int CRASH_FRAMES = 6;

    /** Below this at INIT the battery is reported, on the INIT page and in the log: 12.0 V resting is nearly empty. */
    static final double LOW_BATTERY_VOLTS = 12.0;
    /** The INIT page's battery warning, null when the battery read fine or there is no sensor. */
    private String batteryNote;
    /** The low-battery rumble has gone, once per INIT: 29 Oct's low runs all had the page warning. */
    private boolean batteryRumbled;
    /** The battery at INIT, volts; NaN with no sensor. */
    private double batteryAtInitVolts = Double.NaN;

    /**
     * What the drive team must fix before PLAY: an Autonomous's every warning, a TeleOp's unassigned
     * gamepad only. The light flashes white while there is any ({@link #initWarningFlash}).
     */
    private final InitWarnings initWarnings = new InitWarnings();
    /** When INIT was pressed, System.nanoTime: the camera's first frame has a grace period from it. */
    private long initStartNs;
    /** What went wrong with the Pinpoint this INIT: a hard fault is kept once seen, a bad read only while it holds. */
    private final PinpointInitFault pinpointFault = new PinpointInitFault();
    /** The Pinpoint's trust as last logged: an event on each change ({@link PinpointTrust}). */
    private PinpointTrust.State loggedPinpointTrust = PinpointTrust.State.TRUSTED;
    private String loggedWarnings;
    private boolean loggedLaneFull;
    private long loggedDepartures;
    /** Between INIT and PLAY. */
    private boolean initializing = true;

    // A mechanism asked to move whose encoder never does: one event each, from the log alone.
    private final MotionWatch intakeWatch = new MotionWatch("intake");
    private final MotionWatch feederWatch = new MotionWatch("feeder");
    private String loggedLauncherFault;
    private int loggedPidfWrites;
    private int loggedSpinUps;
    private int loggedDips;
    // Drift: pushes and jolts, the camera frame last logged, the Pinpoint's last status.
    private final ImpactWatch impacts = new ImpactWatch();
    /** TeleOp with no handoff: the camera against the declared start in INIT ({@link CameraStart}). */
    private final CameraStart cameraStart = new CameraStart();
    private long cameraStartNs;
    private String cameraStartNote;
    private int loggedAutoHeadingFixes;
    private int loggedShadowFixes;
    private long loggedCameraNs;
    private Object loggedPinpointStatus;
    /** The lane tracker's slots, front first, built once. */
    private static final String[] LANE_SLOT_KEYS = {"/Lane/Slot1", "/Lane/Slot2", "/Lane/Slot3", "/Lane/Slot4"};
    private static final String[] WHEEL_KEYS = {"/Drive/WheelTicksPerSec/FrontLeft",
            "/Drive/WheelTicksPerSec/FrontRight", "/Drive/WheelTicksPerSec/BackLeft",
            "/Drive/WheelTicksPerSec/BackRight"};
    private final Throttle frameRate = new Throttle(1000, 0);
    private long frameRateCount;
    private long frameRateNs;
    private boolean cameraWasConnected;
    private boolean cameraStatusLogged;
    private static final Alliance[] HIVE_ALLIANCES = {Alliance.RED, Alliance.BLUE};
    private static final String[] HIVE_STATE_KEYS = {"/Hive/Red/State", "/Hive/Blue/State"};
    private static final String[] HIVE_ASSUMED_KEYS = {"/Hive/Red/Assumed", "/Hive/Blue/Assumed"};
    private final int[] loggedHiveState = {-1, -1};

    // ------------------------------------------------------------------ hooks

    /**
     * Where this OpMode expects the robot to start, from {@code StartPositions}. Autonomous
     * overrides it; null (the default) means no declared start and no start check. It is read at
     * init and again every INIT loop, so an Auto whose start depends on the alliance returns the
     * matching one; return the same instance while nothing has changed, since a different instance
     * moves the pose and restarts the start check.
     */
    protected StartPosition startPosition() {
        return null;
    }

    /**
     * Whether the pose was handed over (an Auto just ran, or this TeleOp restarted), so a TeleOp
     * keeps it instead of declaring a start. Known from {@code onInit} on.
     */
    protected final boolean hasHandoffPose() {
        return handoffPose;
    }

    /**
     * The battery's last reading, volts: taken at INIT and then once a second for the match log, so
     * reading it here costs no hub round trip. NaN when the configuration has no voltage sensor.
     */
    protected final double batteryVolts() {
        return lastBatteryVolts;
    }

    /** Called once, after the robot is built and initialized. Put your init telemetry here. */
    /**
     * Panels telemetry for this OpMode, or null when Panels is switched off or cannot provide it.
     * Call once in {@link #onInit()} and skip publishing on null: when Panels is off, an update
     * would still serialise every key for no one (see {@link PanelsSwitch}).
     */
    protected final TelemetryManager panelsTelemetry() {
        if (!panelsEnabled) return null;
        try {
            return PanelsTelemetry.INSTANCE.getTelemetry();
        } catch (RuntimeException | LinkageError e) {
            return null; // Panels is a window onto the robot, not part of it.
        }
    }

    protected void onInit() {
    }

    /** Called repeatedly between INIT and PLAY, before subsystems update. */
    protected void onInitLoop() {
    }

    /** Called once when PLAY is pressed. */
    protected void onStart() {
    }

    /** Called every loop after PLAY, before subsystems update. */
    protected abstract void onLoop();

    /** Called once at the end, before every subsystem is stopped. */
    protected void onStop() {
    }

    // -------------------------------------------------------------- lifecycle

    @Override
    public final void init() {
        initStartNs = System.nanoTime();
        display = new Display(telemetry);
        openLog();
        // Once, here: the view is a handle on Panels' shared field, not something to look up per
        // loop. A dashboard failure is not a robot failure, so it is allowed to be absent.
        panelsEnabled = PanelsSwitch.enabled();
        if (!panelsEnabled) {
            fieldView = null;
            log.event("Panels disabled: no field view and no Panels telemetry this run");
        } else {
            try {
                fieldView = FieldView.pedroCoordinates();
            } catch (RuntimeException e) {
                fieldView = null;
                log.event("field view unavailable: " + e);
            }
        }

        // Before the robot is built, so no read anywhere — constructors included — bypasses it.
        hubs = hardwareMap.getAll(LynxModule.class);
        for (int i = 0; i < hubs.size(); i++) {
            hubs.get(i).setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
        }

        try {
            // Before the robot is built, so every constructor reads this robot's values.
            List<String> differences = RobotDifferences.applyForActiveRobot();
            log.metadata("RobotDifferences", differences.isEmpty()
                    ? "none: the shared values" : String.join(" | ", differences));
            robot = new Robot(hardwareMap);
            robot.initialize();
        } catch (RuntimeException e) {
            // Still thrown: the Driver Station shows it. The log keeps it for after the match.
            log.event("init failed: " + e);
            throw e;
        }
        // Optional by design: a configuration with no voltage sensor logs no battery voltage.
        Iterator<VoltageSensor> sensors = hardwareMap.voltageSensor.iterator();
        battery = sensors.hasNext() ? sensors.next() : null;
        receiveHandoff();
        // A TeleOp or test OpMode is red unless told otherwise; an Auto must be told or see.
        if (!isAutonomous()) {
            setup.defaultTo(MatchSetup.DEFAULT_ALLIANCE);
        }
        declaredStart = startPosition();
        if (declaredStart != null) {
            robot.drive.setPose(declaredStart.pose);
        }
        // An Auto always starts with the lane full of POLLEN; anything else starts it empty.
        if (isAutonomous()) {
            robot.colorSensor.lane().fill(LaneTracker.Piece.POLLEN, LaneTracker.AUTO_PRELOAD);
        }

        // The scheduler is static, so commands survive from one OpMode to the next unless this
        // is called. Reset before scheduling, never after.
        Scheduler.reset();
        List<Subsystem> subsystems = robot.subsystems();
        subsystemKeys = new String[subsystems.size()];
        subsystemMs = new double[subsystems.size()];
        for (int i = 0; i < subsystems.size(); i++) {
            Scheduler.schedule(timedPeriodic(subsystems.get(i), i));
        }

        // What each subsystem found, on the log's Metadata tab: the one place that says whether
        // the Pinpoint answered, the camera is connected and the turret was zeroed. Motors cannot
        // be known this way; MotionWatch catches one that is commanded and never moves.
        for (int i = 0; i < subsystems.size(); i++) {
            String health = subsystems.get(i).health();
            if (health != null) {
                log.metadata("Hardware/" + subsystems.get(i).getClass().getSimpleName(), health);
            }
        }
        if (battery != null) {
            double volts = battery.getVoltage();
            lastBatteryVolts = volts;
            batteryAtInitVolts = volts;
            robot.batteryVolts(volts);
            log.metadata("BatteryAtInit", String.format(Locale.US, "%.2f V", volts));
            if (volts < LOW_BATTERY_VOLTS) {
                batteryNote = String.format(Locale.US, "%.2f V at INIT, below %.1f V: swap it", volts, LOW_BATTERY_VOLTS);
                log.event("battery low: " + batteryNote);
            }
        }

        onInit();
    }

    @Override
    public final void init_loop() {
        try {
            initLoopBody();
        } catch (RuntimeException | Error e) {
            logCrash("init_loop", e);
            throw e;
        }
    }

    private void initLoopBody() {
        clearBulkCache();
        if (gamepad1.x || gamepad2.x) setup.chooseManually(Alliance.BLUE);
        if (gamepad1.b || gamepad2.b) setup.chooseManually(Alliance.RED);
        if (startFresh.update(gamepad1.y || gamepad2.y, System.nanoTime()) && handoffTaken) {
            startFresh();
        }
        // The declared start may follow the alliance (BuiltAuto: the drawing's start, or the
        // rotated one). When it changes, the pose moves there and the fix statistics restart.
        StartPosition wanted = startPosition();
        if (wanted != declaredStart) {
            declaredStart = wanted;
            if (wanted != null) {
                robot.drive.setPose(wanted.pose);
                log.event("Start: now declaring " + wanted);
            }
        }
        considerCameraStart();
        StartCheck.Result startCheck = StartCheck.evaluate(declaredStart, robot.drive.fixCount(),
                robot.drive.meanFixX(), robot.drive.meanFixY());
        // An Auto may know the alliance from which of its starts the camera puts the robot on;
        // otherwise the start check can only confirm the start already declared.
        Alliance byStart = allianceFromStarts();
        if (byStart != Alliance.UNKNOWN) {
            setup.offerVision(byStart, "the camera puts the robot on the " + byStart + " start");
        } else {
            setup.offerVision(startCheck.confirmedAlliance(), robot.vision.allianceEvidence());
        }
        if (isAutonomous()) {
            updateInitWarnings(startCheck);
        }
        initWarnings.gamepads(assigned(gamepad1), assigned(gamepad2));
        watchGamepads();
        beginPage(true);
        // With no handoff and a declared start, the start line says where the robot is; the
        // "no handoff" note would only contradict it.
        if (handoffNote != null && (handoffPose || declaredStart == null || startedFresh)) {
            display.status("Start", handoffLevel, handoffNote);
        }
        if (batteryNote != null) {
            // An Auto lists it with the warnings at the top instead.
            if (!isAutonomous()) display.status("Battery", Display.Level.FAULT, batteryNote);
            if (!batteryRumbled) {
                batteryRumbled = true;
                gamepad1.rumble(600);
                gamepad2.rumble(600);
            }
        }
        if (isAutonomous()) {
            describeCameraStarting();
        } else {
            describeCamera();
        }
        if (cameraStartNote != null) {
            display.status("Start", Display.Level.WARN, cameraStartNote);
        } else {
            startCheck.describe(display);
        }
        driver.updateDuringInit();
        operator.updateDuringInit();
        onInitLoop();
        // The map last, and only while it helps and fits: the page is one screen (Display).
        if (display.page() == Display.Page.MATCH && startCheck.mapHelps(setup.alliance())
                && display.room(StartCheck.Result.MAP_LINES)) {
            startCheck.describeMap(display, setup.alliance());
        }
        showIndicator(true);
        Scheduler.execute();
        finishPage();
        drawField(true);
        recordLoop(MatchLog.Mode.DISABLED, Double.NaN);
    }

    /**
     * The camera's state before PLAY, when it is not yet fine. On 29 Oct PLAY was pressed five times
     * before the Limelight's first frame, so the start check had nothing to go on, and for half an
     * hour the Limelight did not answer at all without anyone seeing it.
     */
    private void describeCamera() {
        if (!robot.vision.isAvailable()) {
            display.status("Camera", Display.Level.FAULT, robot.vision.unavailableReason());
        } else if (!robot.vision.isConnected()) {
            display.status("Camera", Display.Level.FAULT, "the Limelight is not answering: check its power and Ethernet");
        } else if (robot.vision.freshResultCount() == 0) {
            display.status("Camera", Display.Level.WARN, "no frames yet: wait for it before PLAY");
        }
    }

    /**
     * An Auto's camera in its first seconds, before "no picture" is a warning
     * ({@link InitWarnings#FIRST_FRAME_GRACE_MS}): a quiet line, so nobody presses PLAY before the
     * first frame (29 Oct) and nobody is told to replug a camera that is still starting.
     */
    private void describeCameraStarting() {
        if (robot.vision.isAvailable() && robot.vision.isConnected() && robot.vision.freshResultCount() == 0
                && !initWarnings.has(InitWarnings.Kind.CAMERA_NO_FRAMES)) {
            display.line("<small>camera starting: no picture yet, wait for it before PLAY</small>");
        }
    }

    /**
     * The warnings for this INIT loop ({@link InitWarnings}), and an event in the match log each
     * time the list changes. Autonomous only.
     */
    private void updateInitWarnings(StartCheck.Result startCheck) {
        // A hard fault is kept once seen, in words short enough for one line ("y pod not detected");
        // a bad read only while it holds. The log has every status change with its time.
        Object status = robot.drive.pinpointStatus();
        pinpointFault.observe(robot.drive.hasHeading(), status == null ? null : status.toString(),
                System.nanoTime() / 1_000_000L);
        String pinpointProblem = pinpointFault.problem();
        // Short, for one line: the reason in full is in the log's hardware metadata.
        String cameraProblem = !robot.vision.isAvailable() ? "not found"
                : !robot.vision.isConnected() ? "not answering" : null;
        // "Move to" names this OpMode's start for the alliance PLAY will use: the one chosen, or,
        // with none chosen yet, the one whose start the robot is on.
        StartPosition otherStart = cameraOnOtherStart();
        Alliance chosen = setup.alliance();
        StartPosition moveTo = otherStart == null ? null
                : startFor(chosen == Alliance.UNKNOWN ? otherStart.alliance : chosen);
        initWarnings.evaluate(pinpointProblem, chosen, setup.visionAlliance(), startCheck,
                cameraProblem, robot.vision.freshResultCount(), (System.nanoTime() - initStartNs) / 1_000_000L,
                batteryAtInitVolts, LOW_BATTERY_VOLTS, robot.colorSensor.silent(),
                robot.drive.lastFrame().count() >= LocalizationTuning.minFixTags, otherStart, moveTo);
        String summary = initWarnings.summary();
        if (!summary.equals(loggedWarnings)) {
            log.event("INIT warnings: " + summary);
            loggedWarnings = summary;
        }
    }

    /**
     * What the indicator light shows this loop: see {@code IndicatorSubsystem}. Only what the drive
     * team acts on: in INIT an Auto's warnings or a TeleOp's unassigned gamepad, else the alliance;
     * after PLAY green when full, else off.
     */
    private void showIndicator(boolean init) {
        robot.indicator.show(IndicatorSubsystem.choose(init, init && initWarningFlash(),
                robot.colorSensor.full(), setup.alliance()));
    }

    /**
     * The alliance whose start the camera puts the robot on, for an OpMode with a start per
     * alliance ({@code BuiltAuto}); {@code UNKNOWN} otherwise, and whenever it cannot tell. Read
     * every INIT loop, before the start check's own confirmation. Manual X/B still wins.
     */
    protected Alliance allianceFromStarts() {
        return Alliance.UNKNOWN;
    }

    /**
     * The known start the camera puts the robot on when it is not one of this OpMode's own
     * ({@code BuiltAuto}: the wrong Auto, or the wrong spot), for the INIT warnings; null otherwise.
     */
    protected StartPosition cameraOnOtherStart() {
        return null;
    }

    /** This OpMode's start for {@code alliance}, named in that warning. */
    protected StartPosition startFor(Alliance alliance) {
        return StartPositions.forAlliance(alliance);
    }

    @Override
    public final void start() {
        try {
            startBody();
        } catch (RuntimeException | Error e) {
            logCrash("start", e);
            throw e;
        }
    }

    private void startBody() {
        loopTimer.reset();
        // Before this, nothing moved (G304.H, G403); from here on the mechanisms may.
        robot.start();
        initializing = false;
        logStartCheck();
        setup.lock();
        // Which NECTAR is the other alliance's, for the colour sensor's reject burst (NectarReject).
        robot.colorSensor.alliance(setup.alliance());
        driver.prime();
        operator.prime();
        if (!isAutonomous()) {
            // Field-centric forward: the driver's "away from me" when the pose is on the field
            // (handed over from Autonomous, or seeded by the camera in INIT) and that heading has
            // been measured for the alliance; otherwise the way the robot faces now.
            double forward = FieldFrame.driverForwardHeading(setup.alliance());
            if (robot.drive.poseReferenced() && !Double.isNaN(forward)) {
                robot.drive.setFieldForward(forward);
                log.event("PLAY: field forward = driver forward");
            } else {
                robot.drive.resetHeading();
                log.event("PLAY: field forward = robot's facing");
            }
        }
        log.event("PLAY: " + setup.alliance() + " alliance");
        // The turret is assumed at home at the start of a match; a person's zero or an Auto's wins.
        if (TurretZeroPolicy.atPlay(robot.turret.zeroed()) == TurretZeroPolicy.Action.ZERO_HERE) {
            robot.turret.zeroAtPlay();
        }
        log.event("PLAY: turret " + robot.turret.zeroSource().text);
        if (battery != null) {
            log.event(String.format(Locale.US, "PLAY: battery %.2f V", battery.getVoltage()));
        }
        // Every Panels tunable as it stands now, so the log can be reproduced. Once, at PLAY.
        Tunables.snapshot(log);
        onStart();
    }

    @Override
    public final void loop() {
        try {
            loopBody();
        } catch (RuntimeException | Error e) {
            logCrash("loop", e);
            throw e;
        }
    }

    /**
     * An uncaught exception ends the OpMode: the SDK stops the robot and shows the error on the
     * Driver Station, where it stays until someone clears it. This puts the same error in the match
     * log too, which keeps the stack after the Driver Station has forgotten it, then rethrows so
     * nothing about the crash changes. Never throws itself.
     */
    private void logCrash(String where, Throwable e) {
        try {
            StringBuilder text = new StringBuilder("CRASH in ").append(where).append(": ").append(e);
            StackTraceElement[] frames = e.getStackTrace();
            for (int i = 0; i < Math.min(frames.length, CRASH_FRAMES); i++) {
                text.append(" | at ").append(frames[i]);
            }
            Throwable cause = e.getCause();
            if (cause != null) {
                text.append(" | caused by ").append(cause);
            }
            log.event(text.toString());
            log.commit();
        } catch (RuntimeException ignored) {
            // The crash is the story; a log problem must not replace it.
        }
    }

    private void loopBody() {
        loopTimer.lap();
        clearBulkCache();
        beginPage(false);
        driver.update();
        operator.update();
        watchGamepads();
        long t0 = System.nanoTime();
        onLoop();
        showIndicator(false);
        long t1 = System.nanoTime();
        Scheduler.execute();
        long t2 = System.nanoTime();
        finishPage();
        drawField(false);
        long t3 = System.nanoTime();
        recordLoop(isAutonomous() ? MatchLog.Mode.AUTONOMOUS : MatchLog.Mode.TELEOP, loopTimer.lastMs());
        long t4 = System.nanoTime();
        costs.record(t0, t1, t2, t3, t4);
    }

    /** Per subsystem: its log key and its last update's time, ms. Built once in init. */
    private String[] subsystemKeys = new String[0];
    private double[] subsystemMs = new double[0];

    /**
     * A subsystem's {@code periodic()}, timed: its {@code update()} as a never-ending command that
     * requires it, which is what {@link Subsystem#periodic()} is, with the time each update takes
     * kept for the match log ({@code /Loop/Subsystem/<Name>Ms}). The first subsystem to read a hub
     * after the loop clears the bulk cache pays for that hub's bulk read, so a large number on it
     * may be the read rather than its own work; the list order is fixed, so that is the same
     * subsystem every loop.
     */
    private Command timedPeriodic(final Subsystem subsystem, final int index) {
        subsystemKeys[index] = "/Loop/Subsystem/" + subsystem.getClass().getSimpleName() + "Ms";
        return Commands.infinite(() -> {
            long start = System.nanoTime();
            subsystem.update();
            subsystemMs[index] = (System.nanoTime() - start) / 1e6;
        }).requiring(subsystem);
    }

    /**
     * Where a loop's time goes, so a slow loop can be blamed on the right thing: the OpMode's own
     * {@code onLoop()} (its drive request, Match page and Panels), the subsystems' updates through
     * the Scheduler (sensors, the camera, the localizer, every mechanism), the Controls or Robot
     * page together with the Panels Field view, and the match log. Shown on the Robot page and
     * written to the match log under {@code /Loop/...}. The bulk-cache clear and the bindings run
     * before the first stamp and are microseconds.
     */
    protected final LoopCosts costs = new LoopCosts();

    /** Running means of the loop's phases, ms. Allocated once; nothing per loop. */
    protected final class LoopCosts {
        private final WelfordVariance onLoopMs = new WelfordVariance();
        private final WelfordVariance subsystemsMs = new WelfordVariance();
        private final WelfordVariance pageMs = new WelfordVariance();
        private final WelfordVariance logMs = new WelfordVariance();
        private double lastOnLoopMs, lastSubsystemsMs, lastPageMs, lastLogMs;

        void record(long t0, long t1, long t2, long t3, long t4) {
            lastOnLoopMs = (t1 - t0) / 1e6;
            lastSubsystemsMs = (t2 - t1) / 1e6;
            lastPageMs = (t3 - t2) / 1e6;
            lastLogMs = (t4 - t3) / 1e6;
            onLoopMs.update(lastOnLoopMs);
            subsystemsMs.update(lastSubsystemsMs);
            pageMs.update(lastPageMs);
            logMs.update(lastLogMs);
            log.put("/Loop/OnLoopMs", lastOnLoopMs);
            log.put("/Loop/SubsystemsMs", lastSubsystemsMs);
            log.put("/Loop/PageMs", lastPageMs);
            log.put("/Loop/LogMs", lastLogMs);
            for (int i = 0; i < subsystemKeys.length; i++) {
                log.put(subsystemKeys[i], subsystemMs[i]);
            }
        }

        public double onLoopMs() { return onLoopMs.mean(); }
        public double subsystemsMs() { return subsystemsMs.mean(); }
        public double pageMs() { return pageMs.mean(); }
        public double logMs() { return logMs.mean(); }

        /** One line for the Robot page. */
        public String summary() {
            return String.format(java.util.Locale.US,
                    "mean: onLoop %.1f · subsystems %.1f · page %.1f · log %.1f ms",
                    onLoopMs.mean(), subsystemsMs.mean(), pageMs.mean(), logMs.mean());
        }
    }

    @Override
    public final void stop() {
        onStop();

        boolean handedOff = isAutonomous() && robot != null;
        if (handedOff) {
            // Only a field pose is worth handing on. An Auto with no declared start knows where
            // it is relative to init, not on the field; passing that on would make TeleOp treat
            // it as field-referenced and feed camera fixes computed from a meaningless heading.
            Pose pose = robot.drive.poseReferenced() ? robot.drive.pose() : null;
            // The turret's zero goes too (null if this Auto never got one, say it stopped in INIT).
            Long turretZero = robot.turret.zeroed() ? Long.valueOf(robot.turret.zeroCounts()) : null;
            Handoff.record(setup.alliance(), pose, matchId, log, turretZero, System.currentTimeMillis());
        }

        // Guarded because stop() runs even when init() threw partway through — a missing device, a
        // bad configuration — and an exception in stop() replaces the one that actually explains
        // what went wrong.
        if (robot != null) {
            // The camera's temperature and load after the run: a dropout's likeliest causes.
            try {
                log.event("Limelight at end: " + robot.vision.statusLine());
            } catch (RuntimeException e) {
                log.event("Limelight at end: no status (" + e + ")");
            }
            robot.stop();
        }
        Scheduler.reset();
        if (handedOff) {
            // The file stays open for the TeleOp that follows; unclaimed, it closes by itself.
            log.pause("OpMode stopped: " + opModeName(), Handoff.LOG_MAX_AGE_MS);
        } else {
            log.close("OpMode stopped: " + opModeName(), LOG_CLOSE_WAIT_MS);
        }
    }

    /**
     * At PLAY, what the camera made of the placement over the time the robot sat untouched before it
     * ({@link SettledStats}), against the declared start: into the match log's Metadata tab
     * ({@code StartCheck}) and as numbers ({@code /StartCheck/*}). The drive team placed it by the
     * marks, so each run is a calibration sample. Position and heading: a gap that follows one robot
     * to every field is its camera mount, one on every robot at one field is that field's HIVE, one
     * that changes run to run is placement. Per CELL: one CELL off and the others not is that CELL.
     * {@code RobotConfig} and the date are already in the log. {@code SettledSeconds} says how much
     * to trust the sample: a robot adjusted just before PLAY gives a short one.
     */
    private void logStartCheck() {
        if (declaredStart == null) {
            return;
        }
        SettledStats s = robot.drive.settled();
        if (s.frames() == 0) {
            log.metadata("StartCheck", declaredStart + ": no camera frames while settled");
            return;
        }
        Pose start = declaredStart.pose;
        double dx = s.x() - start.x();
        double dy = s.y() - start.y();
        double c = Math.cos(start.heading());
        double sn = Math.sin(start.heading());
        double fwd = c * dx + sn * dy;
        double left = -sn * dx + c * dy;
        // SettledStats keeps each CELL against the pose; a relocalize in INIT moves the pose off
        // the start, so shift those back onto the start like everything else here.
        Pose now = robot.drive.pose();
        double shiftX = now == null ? 0.0 : now.x() - start.x();
        double shiftY = now == null ? 0.0 : now.y() - start.y();
        StringBuilder text = new StringBuilder(String.format(Locale.US,
                "%s: settled %.0f s, %d frames; camera minus start x %+.2f, y %+.2f in "
                        + "(fwd %+.2f, left %+.2f); heading %+.2f deg (%d frames)",
                declaredStart, s.seconds(), s.frames(), dx, dy, fwd, left,
                s.headingDiffDeg(), s.headingFrames()));
        for (HiveCell cell : HiveCell.values()) {
            if (s.tags(cell) == 0) continue;
            text.append(String.format(Locale.US,
                    "; %s: %d tag sightings, x %+.2f, y %+.2f, height %+.2f, range %.0f in",
                    cell, s.tags(cell), s.cellDx(cell) + shiftX, s.cellDy(cell) + shiftY,
                    s.cellHeightErrorIn(cell), s.cellRangeIn(cell)));
            String key = "/StartCheck/" + cell.name() + "/";
            log.put(key + "DxIn", s.cellDx(cell) + shiftX);
            log.put(key + "DyIn", s.cellDy(cell) + shiftY);
            log.put(key + "RangeIn", s.cellRangeIn(cell));
            log.put(key + "HeightErrorIn", s.cellHeightErrorIn(cell));
        }
        log.metadata("StartCheck", text.toString());
        log.put("/StartCheck/SettledSeconds", s.seconds());
        log.put("/StartCheck/Frames", s.frames());
        log.put("/StartCheck/CameraMinusStartXIn", dx);
        log.put("/StartCheck/CameraMinusStartYIn", dy);
        log.put("/StartCheck/CameraFwdOfStartIn", fwd);
        log.put("/StartCheck/CameraLeftOfStartIn", left);
        if (s.headingFrames() > 0) {
            log.put("/StartCheck/CameraMinusStartHeadingDeg", s.headingDiffDeg());
        }
    }

    /** Where the robot should start (during INIT), and where the camera says it is, on the field. */
    private static final String START_COLOUR = "#00E676";
    private static final String CAMERA_COLOUR = "#FFB300";
    /** The shadow pose on the field view ({@code ShadowPose}): purple. */
    private static final String SHADOW_COLOUR = "#AB47BC";
    /** A shadow fix that moved it less than this, inches, is not an event; the channels have it. */
    private static final double SHADOW_EVENT_MOVE_IN = 0.25;

    /** The measured spots on the field view: grey, under the robots. */
    private static final String MARK_COLOUR = "#9E9E9E";

    /**
     * Every measured spot a robot can be parked on to judge the pose: the calibration spots (which
     * include the measured starts) and the four reset corners. A spot still NaN is skipped.
     */
    private static final Pose[] MARKS = measuredMarks();

    private static Pose[] measuredMarks() {
        Pose[] corners = {ResetPositions.RED_LEFT, ResetPositions.RED_RIGHT,
                ResetPositions.BLUE_LEFT, ResetPositions.BLUE_RIGHT};
        Pose[] marks = new Pose[CalibrationSpots.ALL.length + corners.length];
        for (int i = 0; i < CalibrationSpots.ALL.length; i++) {
            marks[i] = CalibrationSpots.ALL[i].pose;
        }
        System.arraycopy(corners, 0, marks, CalibrationSpots.ALL.length, corners.length);
        return marks;
    }

    /**
     * The robot on the Panels Field view, after the subsystems have updated so it is this loop's
     * pose, in the alliance's colour (white with none). During INIT, an OpMode with a declared
     * start also gets that start (green) and where the camera's latest fixes put the robot
     * (amber): the gap between them is the start check's error, and the way to move the robot.
     * Then {@link #drawOnField} adds the OpMode's own. Gated on the throttle, so on most loops
     * this is one boolean read; skipped when the drive has no heading (no Pinpoint), since then
     * there is no pose to draw. The publish is caught and swallowed: telemetry never takes a
     * mechanism down.
     */
    private void drawField(boolean initializing) {
        if (fieldView == null || !robot.drive.hasHeading()) {
            return;
        }
        try {
            if (fieldView.shouldDraw()) {
                Pose pose = robot.drive.pose();
                // Each layer can be switched off in Panels → FieldLayers. Marks first, so the
                // robots are drawn over them.
                if (FieldLayers.marks) {
                    for (Pose mark : MARKS) {
                        if (!Double.isNaN(mark.x()) && !Double.isNaN(mark.y())) {
                            fieldView.drawMark(mark.x(), mark.y(), mark.heading(), MARK_COLOUR);
                        }
                    }
                }
                if (pose != null && FieldLayers.odometry) {
                    fieldView.drawRobot(pose.x(), pose.y(), pose.heading(),
                            FieldView.allianceColour(setup.alliance()));
                }
                Pose shadowPose = robot.drive.shadowPose();
                if (shadowPose != null && FieldLayers.shadow && robot.drive.shadow().offsetIn() > 0.05) {
                    fieldView.drawRobot(shadowPose.x(), shadowPose.y(), shadowPose.heading(), SHADOW_COLOUR);
                }
                if (initializing && declaredStart != null) {
                    Pose start = declaredStart.pose;
                    if (FieldLayers.declaredStart) {
                        fieldView.drawRobot(start.x(), start.y(), start.heading(), START_COLOUR);
                    }
                    double cameraX = robot.drive.meanFixX();
                    double cameraY = robot.drive.meanFixY();
                    if (!Double.isNaN(cameraX) && FieldLayers.camera) {
                        fieldView.drawRobot(cameraX, cameraY, start.heading(), CAMERA_COLOUR);
                    }
                }
                drawOnField(fieldView);
                fieldView.send();
            }
        } catch (RuntimeException ignored) {
            // Panels is a window onto the robot, not part of it.
        }
    }

    /**
     * An OpMode's own lines at the top of the Robot page: what the person testing wants and the drive
     * team does not, kept off the one-screen INIT page.
     */
    protected void describeRobotPage(Display display) {
    }

    /**
     * An OpMode's own marks on the Panels Field view, drawn over the robot each time the view is
     * sent. Override instead of making a second {@code FieldView}: two views sending in turn make
     * the field flicker between them.
     */
    protected void drawOnField(FieldView field) {
    }

    /**
     * Page button edge, then the header — so the OpMode's Match lines land under it. In INIT the
     * Match page starts with the alliance, big, and in an Auto the verdict and the warnings, each
     * with what to do.
     */
    private void beginPage(boolean initializing) {
        boolean pressed = gamepad1.back;
        if (pressed && !prevPageButton) {
            display.nextPage();
        }
        prevPageButton = pressed;
        display.header();
        if (!initializing && display.page() == Display.Page.MATCH) {
            PinpointTrust.State trust = robot.drive.pinpointTrust();
            if (trust == PinpointTrust.State.FAULTED) {
                display.alert("Pinpoint not reading", "keep driving; Square, FLOWER pick and pose aim are off"
                        + " · field-centric goes wrong as the robot turns · tell the coach the time");
            } else if (trust == PinpointTrust.State.CONFIRMING) {
                display.alert("Pinpoint back", "show the camera a HIVE, or corner reset (D-pad left/right held"
                        + " in the corner): then Square and the FLOWER pick work again");
            }
        }
        if (initializing && display.page() == Display.Page.MATCH) {
            initWarnings.describeTop(display, setup.alliance(), isAutonomous());
        }
        // An Auto's alliance warning already says it all, with its button: one screen (Display).
        if (!(initializing && isAutonomous() && initWarnings.has(InitWarnings.Kind.ALLIANCE))) {
            setup.describe(display);
        }
    }

    /** TeleOp side of the handoff: restore Auto's pose and alliance, and say which happened. */
    private void receiveHandoff() {
        if (isAutonomous()) {
            return;
        }
        long now = System.currentTimeMillis();
        Handoff.Snapshot handoff = Handoff.last();
        if (handoff == null) {
            handoffLevel = Display.Level.WARN;
            handoffNote = "no handoff (no OpMode has run since the app started) — "
                    + "pose starts at 0,0; forward = robot's facing at PLAY";
            return;
        }
        String from = handoff.fromAuto ? "Auto" : "the TeleOp that just stopped";
        handoffTaken = true;
        setup.inheritFromAuto(handoff.alliance);
        if (TurretZeroPolicy.atInit(false, handoff.turretZeroCounts != null)
                == TurretZeroPolicy.Action.RESTORE_FROM_AUTO) {
            robot.turret.restoreZero(handoff.turretZeroCounts);
        }
        String age = handoff.ageText(now);
        if (handoff.pose != null && robot.drive.hasHeading()) {
            robot.drive.setPose(handoff.pose);
            handoffPose = true;
            boolean forwardKnown = !Double.isNaN(FieldFrame.driverForwardHeading(handoff.alliance));
            handoffLevel = forwardKnown ? Display.Level.OK : Display.Level.WARN;
            handoffNote = String.format(java.util.Locale.US,
                    "pose from %s %s (%.0f, %.0f, %.0f°)%s · hold Triangle (Y) to start fresh", from, age,
                    handoff.pose.x(), handoff.pose.y(), Math.toDegrees(handoff.pose.heading()),
                    forwardKnown ? "" : "; forward = robot's facing at PLAY (not measured)");
        } else {
            handoffLevel = Display.Level.WARN;
            handoffNote = "alliance from " + from + " " + age + ", but no field pose (no declared "
                    + "start, or no Pinpoint); forward = robot's facing at PLAY";
        }
    }

    /**
     * Triangle (Y) held in INIT: the drive team says the last run's state is wrong for where the
     * robot is now (someone moved it, or the last run was a test). The handoff's pose, alliance and
     * turret zero are dropped, and this OpMode starts as if nothing ran before it: its declared
     * start (a TeleOp's alliance start) on the next INIT loop, the default alliance unless X/B says
     * otherwise, and the turret zeroed at PLAY. The log file it resumed carries on.
     */
    private void startFresh() {
        handoffTaken = false;
        startedFresh = true;
        handoffPose = false;
        setup.forgetAuto();
        robot.turret.forgetCarriedZero();
        declaredStart = null; // the next INIT loop declares the start afresh and moves the pose there
        cameraStart.reset();
        cameraStartNote = null;
        handoffLevel = Display.Level.OK;
        handoffNote = "starting fresh: the last run's pose, alliance and turret zero are discarded";
        log.event("INIT: start fresh, handoff discarded by the drive team");
        gamepad1.rumble(200);
        gamepad2.rumble(200);
    }

    /**
     * A TeleOp with no handoff assumes its declared start, wherever the robot is. Each INIT loop the
     * camera's whole-pose fit is offered to {@link CameraStart}; once steady fits put the robot off the
     * mark (past {@code startMarginIn} or {@code startOverrideDeg}), the pose moves to the camera's and
     * the Start line says so. An Autonomous never does this
     * (its drive team places by the marks), nor an OpMode that says {@link #cameraMayOverrideStart}
     * false, such as the camera calibration, whose residual is measured against the declared start.
     */
    private void considerCameraStart() {
        if (cameraStartNote != null || declaredStart == null || handoffPose || isAutonomous()
                || !cameraMayOverrideStart()) {
            return;
        }
        robot.drive.setWholePoseFits(true);
        long ns = robot.drive.wholeCameraPoseNs();
        if (ns == 0 || ns == cameraStartNs) return;
        cameraStartNs = ns;
        Pose taken = cameraStart.update(robot.drive.wholeCameraPose(), declaredStart);
        if (taken == null) return;
        robot.drive.setPose(taken);
        robot.drive.setWholePoseFits(false);
        cameraStartNote = String.format(java.util.Locale.US,
                "pose from the camera (%.0f, %.0f, %.0f°): the %s mark is %.0f in, %.0f° away, so the robot is not on it"
                        + " · hold Triangle (Y) to start fresh", taken.x(), taken.y(), Math.toDegrees(taken.heading()),
                declaredStart, cameraStart.offIn(), cameraStart.offDeg());
        log.event("Start: " + cameraStartNote);
    }

    /** Whether a TeleOp's declared start may give way to the camera in INIT ({@link #considerCameraStart}). */
    protected boolean cameraMayOverrideStart() {
        return true;
    }

    /**
     * One Match-page line while the robot is still with tags in view: the camera's heading against the
     * Pinpoint's. Past {@link LocalizationTuning#headingFixDeg} it warns and says what to press, so a
     * robot that "drives funny" is explained before the sticks find out. Nothing while moving.
     */
    protected void showHeadingCheck() {
        double diff = robot.drive.cameraHeadingDiffDeg();
        int frames = robot.drive.cameraHeadingFrames();
        if (Double.isNaN(diff) || frames < LocalizationTuning.headingFixFrames || !robot.drive.isStill()) return;
        boolean off = Math.abs(diff) > LocalizationTuning.headingFixDeg;
        display.status("Heading", off ? Display.Level.WARN : Display.Level.OK, String.format(java.util.Locale.US,
                "camera vs odometry %+.1f° over %d frames%s", diff, frames,
                off ? " — the Pinpoint heading is off: hold still and hold the camera reset" : ""));
    }

    /**
     * The light flashes white in INIT: an Auto for any warning, a TeleOp for an unassigned gamepad
     * only (its one INIT warning).
     */
    private boolean initWarningFlash() {
        return isAutonomous() ? initWarnings.any() : initWarnings.gamepadMissing();
    }

    /** A gamepad assigned to its user on the Driver Station (Options + Cross or Circle). */
    private static boolean assigned(Gamepad pad) {
        return pad.getGamepadId() != Gamepad.ID_UNASSOCIATED;
    }

    /**
     * One match-log event each time a gamepad is assigned or drops, with its type then (the type
     * read at INIT can be UNKNOWN for a pad that works: 6 Nov 2026), and one at its first press, so
     * a log says whether each pad was live. Called every loop, INIT and after PLAY.
     */
    private void watchGamepads() {
        watchGamepad(gamepad1, 0, "gamepad 1 (driver)");
        watchGamepad(gamepad2, 1, "gamepad 2 (operator)");
    }

    private void watchGamepad(Gamepad pad, int i, String name) {
        boolean now = assigned(pad);
        if (now != padAssigned[i]) {
            padAssigned[i] = now;
            log.event(name + (now ? ": assigned (" + pad.type + ")" : ": not assigned"));
        }
        if (!padPressed[i] && now && anyInput(pad)) {
            padPressed[i] = true;
            log.event(name + ": first input (" + pad.type + ")");
        }
    }

    /** Any button down, or a stick or trigger off rest. */
    private static boolean anyInput(Gamepad pad) {
        return pad.a || pad.b || pad.x || pad.y || pad.left_bumper || pad.right_bumper
                || pad.dpad_up || pad.dpad_down || pad.dpad_left || pad.dpad_right
                || pad.left_trigger > 0.1 || pad.right_trigger > 0.1
                || Math.abs(pad.left_stick_x) > 0.1 || Math.abs(pad.left_stick_y) > 0.1
                || Math.abs(pad.right_stick_x) > 0.1 || Math.abs(pad.right_stick_y) > 0.1;
    }

    private final boolean[] padAssigned = new boolean[2];
    private final boolean[] padPressed = new boolean[2];

    private boolean isAutonomous() {
        return getClass().isAnnotationPresent(Autonomous.class);
    }

    /** Controls and Robot pages replace whatever the OpMode wrote this loop. */
    private void finishPage() {
        Display.Page page = display.page();
        if (page == Display.Page.MATCH) {
            return;
        }
        telemetry.clear();
        display.header();
        // The light flashes white on every page, so every page says why, in one line.
        if (initializing && initWarningFlash()) {
            display.line("<b><font color=\"#FFB300\">" + initWarnings.count()
                    + " TO FIX: see the Match page (Share)</font></b>");
        }
        if (page == Display.Page.CONTROLS) {
            display.section("BEFORE PLAY — either gamepad");
            display.line("Square (X) — Blue alliance");
            display.line("Circle (B) — Red alliance");
            display.line("Triangle (Y), hold 1 s — start fresh (drop the last run's pose)");
            display.line("Share (Back) — next page (any time)");
            describeBindings(driver);
            describeBindings(operator);
        } else {
            describeRobotPage(display);
            display.section("Loop");
            display.line(loopTimer.summary());
            display.line(costs.summary());
            display.section("Log");
            if (log.failure() != null) {
                display.status("Log", Display.Level.WARN, log.failure());
            } else {
                display.line(log.name() + " · " + log.framesWritten() + " loops written"
                        + (log.droppedLoops() > 0 ? " · " + log.droppedLoops() + " dropped" : ""));
            }
            List<Subsystem> subsystems = robot.subsystems();
            for (int i = 0; i < subsystems.size(); i++) {
                Subsystem subsystem = subsystems.get(i);
                display.section(subsystem.getClass().getSimpleName());
                subsystem.describe(display);
            }
        }
    }

    private void describeBindings(Bindings bindings) {
        display.section(bindings.title());
        List<String> labels = bindings.labels();
        if (labels.isEmpty()) {
            display.line("(nothing bound)");
        }
        for (int i = 0; i < labels.size(); i++) {
            display.line(labels.get(i));
        }
    }

    /** Opens this run's log file. Never throws: a log that cannot open is disabled and says why. */
    private void openLog() {
        String name = opModeName();
        long nowMs = System.currentTimeMillis();

        // One file per match: a TeleOp that follows an Autonomous carries on writing the file
        // that Autonomous paused (the same handoff receiveHandoff() reads later). Only within the
        // log's own window: a pose handoff has no expiry, but a match id from an hour ago would put
        // today's drive into last hour's file name.
        Handoff.Snapshot handoff = isAutonomous() ? null : Handoff.last();
        if (handoff != null && handoff.ageMs(nowMs) > Handoff.LOG_MAX_AGE_MS) {
            handoff = null;
        }
        if (handoff != null && handoff.log != null && handoff.log.resume()) {
            log = handoff.log;
            matchId = handoff.matchId;
            log.event("OpMode init: " + name + " · continues match " + matchId);
            return;
        }

        // A new file: this OpMode starts the match (an Autonomous, or a TeleOp on its own).
        matchId = handoff != null && handoff.matchId != null ? handoff.matchId : MatchLogFiles.matchId(nowMs);
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("MatchId", matchId);
        metadata.put("OpMode", name);
        metadata.put("OpModeClass", getClass().getName());
        String config = ActiveConfig.name();
        metadata.put("RobotConfig", config == null ? "(none active)" : config);
        // Which code this is, so a log can be matched to a commit (and a wrong-branch install shows).
        metadata.put("Build", BuildStamp.describe());
        metadata.put("GitCommit", BuildStamp.commit());
        metadata.put("GitBranch", BuildStamp.branch());
        File folder = new File(AppUtil.FIRST_FOLDER, MatchLogFiles.LOGS_FOLDER);
        File file = MatchLogFiles.next(folder, matchId, name);
        // A local, not a field: the clock outlives this OpMode when TeleOp continues the file.
        final long startNs = System.nanoTime();
        log = MatchLog.toFile(file, "BIOBUZZ match " + matchId, metadata,
                () -> (System.nanoTime() - startNs) / 1000L);
        log.event("OpMode init: " + name + " · match " + matchId);
    }

    /** This loop's state into the log, then hand it to the writer. Copies numbers only. */
    /**
     * What it takes to tell why the pose drifts: what the drive was told and what the odometry
     * measured, the camera's gap to the odometry on each gated frame, the FLOWER stick, the HIVE,
     * the Pinpoint's status, and an event for each push and jolt ({@link ImpactWatch}). All of it
     * is already in memory: nothing here reads hardware.
     */
    private void recordDrift(MatchLog.Mode mode, Pose pose, boolean referenced) {
        double forward = robot.drive.appliedForward();
        double strafe = robot.drive.appliedStrafe();
        double speed = robot.drive.odometrySpeedInPerSec();
        log.put("/Drive/CommandForward", forward);
        log.put("/Drive/CommandStrafe", strafe);
        log.put("/Drive/CommandTurn", robot.drive.appliedTurn());
        log.put("/Drive/CommandFieldCentric", robot.drive.commandFieldCentric() ? 1 : 0);
        log.put("/Drive/OdometrySpeedInPerSec", speed);
        for (int i = 0; i < WHEEL_KEYS.length; i++) log.put(WHEEL_KEYS[i], robot.drive.wheelTicksPerSec(i));
        log.put("/FlowerStick/Out", robot.flowerStick.extended() ? 1 : 0);
        log.put("/FlowerStick/Powered", robot.flowerStick.limp() ? 0 : 1);
        HiveTracker hive = robot.hive.of(setup.alliance());
        if (hive != null) log.put("/Hive/State", hive.state().ordinal());
        // Both HIVEs as the robot believes them, drawn in the 3D view (HiveComponents).
        long nowMs = System.nanoTime() / 1_000_000L;   // HiveSubsystem's clock
        for (int i = 0; i < HIVE_ALLIANCES.length; i++) {
            Alliance a = HIVE_ALLIANCES[i];
            HiveTracker h = robot.hive.of(a);
            int state = h.state().ordinal();
            log.put(HIVE_STATE_KEYS[i], state);
            log.put(HIVE_ASSUMED_KEYS[i], h.assumed() ? 1 : 0);
            log.hive(HiveComponents.of(a), HiveComponents.believedRad(a, h, nowMs));
            if (state != loggedHiveState[i]) {
                log.event("HIVE (" + a + "): " + h.state() + (h.assumed() ? " (assumed)" : ""));
                loggedHiveState[i] = state;
            }
        }

        // The camera's health: connected, frames a second, latency inside the camera.
        boolean connected = robot.vision.isConnected();
        log.put("/Vision/Connected", connected ? 1 : 0);
        log.put("/Vision/LatencyMs", robot.vision.lastLatencyMs());
        long nowNs = System.nanoTime();
        if (frameRate.due(nowNs)) {
            long frames = robot.vision.freshResultCount();
            if (frameRateNs != 0) {
                log.put("/Vision/FramesPerSec", (frames - frameRateCount) / ((nowNs - frameRateNs) / 1e9));
            }
            frameRateCount = frames;
            frameRateNs = nowNs;
        }
        if (connected != cameraWasConnected) {
            log.event(connected ? "Limelight: answering" : "Limelight: not answering");
            cameraWasConnected = connected;
        }
        // Its own report once, in INIT (asking it waits on the network, so never in the match loop).
        if (mode == MatchLog.Mode.DISABLED && connected && !cameraStatusLogged) {
            log.event("Limelight at INIT: " + robot.vision.statusLine());
            cameraStatusLogged = true;
        }

        Object status = robot.drive.pinpointStatus();
        if (status != null && !status.equals(loggedPinpointStatus)) {
            log.event("Pinpoint: " + status);
            loggedPinpointStatus = status;
        }
        PinpointTrust.State trust = robot.drive.pinpointTrust();
        log.put("/Odometry/PinpointTrust", trust.ordinal());
        if (trust != loggedPinpointTrust) {
            switch (trust) {
                case FAULTED:
                    log.event("Pinpoint lost: " + robot.drive.pinpointLostStatus() + " held "
                            + PinpointTrust.LOST_AFTER_MS + " ms after PLAY; the pose is not believed until it reads"
                            + " again and is confirmed");
                    break;
                case CONFIRMING:
                    log.event("Pinpoint back: reading again; the pose waits for a reset or "
                            + PinpointTrust.AGREE_FRAMES + " camera frames within " + PinpointTrust.AGREE_IN + " in");
                    break;
                default:
                    log.event("Pinpoint trusted again: confirmed by " + robot.drive.pinpointConfirmedBy());
            }
            loggedPinpointTrust = trust;
        }

        // The camera against the odometry, once per gated frame, in field inches. While the Pinpoint
        // is not believed too (the pose is on the field but not trusted): that is how a recovery is
        // checked afterwards, with x and y rather than range alone.
        long cameraNs = robot.drive.cameraPoseNs();
        if (cameraNs != loggedCameraNs && pose != null && robot.drive.poseSetOnField()) {
            Pose camera = robot.drive.lastCameraPose();
            if (camera != null) {
                log.put("/Drift/GapXIn", camera.x() - pose.x());
                log.put("/Drift/GapYIn", camera.y() - pose.y());
                log.put("/Drift/Still", robot.drive.isStill() ? 1 : 0);
                // The shadow's own gap to the same camera frame: circular as a judge (it was fed the
                // camera), but the trend across HIVEs and after marks is readable.
                Pose shadowNow = robot.drive.shadowPose();
                if (shadowNow != null) {
                    log.put("/Shadow/GapXIn", camera.x() - shadowNow.x());
                    log.put("/Shadow/GapYIn", camera.y() - shadowNow.y());
                }
            }
            double headingDiff = robot.drive.cameraHeadingDiffDeg();
            if (!Double.isNaN(headingDiff)) log.put("/Drift/CameraHeadingDiffDeg", headingDiff);
        }
        loggedCameraNs = cameraNs;
        // A heading correction the drive applied by itself (LocalizationTuning.autoHeadingFixes); the
        // button's are logged by the OpMode that owns the button.
        Pose shadowPose = robot.drive.shadowPose();
        if (shadowPose != null && referenced) {
            log.put("/Shadow/Fixes", robot.drive.shadow().fixesApplied());
            log.put("/Shadow/X", shadowPose.x());
            log.put("/Shadow/Y", shadowPose.y());
            log.put("/Shadow/OffsetIn", robot.drive.shadow().offsetIn());
            // As a Pose2d too, so AdvantageScope draws it on the field beside /Odometry/Robot (one
            // three-double array per loop: MatchLog keeps the reference until its writer has it).
            log.pose("/Shadow/Robot", shadowPose.x(), shadowPose.y(), shadowPose.heading());
            // One event per fix flooded the log (2 Nov: 2949 in 15 min, one every five frames while
            // still); the channels carry the rest. An event only when the shadow actually moved.
            if (robot.drive.shadow().fixesApplied() != loggedShadowFixes) {
                loggedShadowFixes = robot.drive.shadow().fixesApplied();
                if (robot.drive.shadow().lastFixMoveIn() >= SHADOW_EVENT_MOVE_IN) log.event(String.format(java.util.Locale.US,
                        "shadow: still fix %d moved it %.1f in; now %.1f in from the real pose (%+.1f, %+.1f)",
                        loggedShadowFixes, robot.drive.shadow().lastFixMoveIn(), robot.drive.shadow().offsetIn(),
                        robot.drive.shadow().offsetX(), robot.drive.shadow().offsetY()));
            }
        }
        if (robot.drive.autoHeadingFixesApplied() != loggedAutoHeadingFixes) {
            loggedAutoHeadingFixes = robot.drive.autoHeadingFixesApplied();
            log.event("pose: automatic " + robot.drive.lastHeadingFix());
        }

        if (mode == MatchLog.Mode.DISABLED || pose == null) return;
        ImpactWatch.Event event = impacts.update(System.currentTimeMillis(),
                Double.isNaN(forward) ? Double.NaN : Math.hypot(forward, strafe), speed);
        if (event == ImpactWatch.Event.PUSH_ENDED) {
            log.event(String.format(Locale.US, "push: %.2f s at command %.2f, at (%.1f, %.1f, %.0f°), %.1f in off a wall, stick %s",
                    impacts.lastPushMs() / 1000.0, impacts.lastPushPeakCommand(), pose.x(), pose.y(),
                    Math.toDegrees(pose.heading()), wallClearanceIn(pose), robot.flowerStick.extended() ? "down" : "up"));
        } else if (event == ImpactWatch.Event.JOLT) {
            log.event(String.format(Locale.US, "jolt: %.0f in/s² at (%.1f, %.1f, %.0f°), %.1f in off a wall, stick %s",
                    impacts.lastJoltInPerSec2(), pose.x(), pose.y(), Math.toDegrees(pose.heading()),
                    wallClearanceIn(pose), robot.flowerStick.extended() ? "down" : "up"));
        }
    }

    /** How far the robot's square 16 in footprint is from the nearest wall, inches (about; any heading). */
    private static double wallClearanceIn(Pose pose) {
        double f = FieldFrame.FIELD_SIZE_INCHES;
        double nearest = Math.min(Math.min(pose.x(), f - pose.x()), Math.min(pose.y(), f - pose.y()));
        return nearest - ResetPositions.WALL_TO_CENTRE_IN;
    }

    private void recordLoop(MatchLog.Mode mode, double loopMs) {
        Alliance alliance = setup.alliance();
        MatchSetup.Source source = setup.source();
        if (alliance != loggedAlliance || source != loggedSource) {
            log.event("Alliance " + alliance + " (" + source + ")");
            loggedAlliance = alliance;
            loggedSource = source;
        }
        long station = alliance == Alliance.UNKNOWN ? 0
                : AdvantageScopeKeys.allianceStation(alliance == Alliance.RED, 1);
        log.match(mode, station);
        copyGamepad(gamepad1, log.gamepad1());
        copyGamepad(gamepad2, log.gamepad2());
        Pose pose = robot.drive.pose();
        boolean referenced = pose != null && robot.drive.poseReferenced();
        if (pose != null) {
            log.pose(pose.x(), pose.y(), pose.heading());
            log.put("/Odometry/Referenced", referenced ? 1 : 0);
        }
        // The robot model's moving parts (AdvantageScope's 3D tabs): written only when one moves.
        log.component(RobotComponents.TURRET, robot.turret.angleDegrees());
        log.component(RobotComponents.FLOWER_STICK, robot.flowerStick.commandedDegrees());
        // What the camera's gate made of the latest frame, and how the stationary fix stands.
        log.put("/Vision/TagsAccepted", robot.drive.lastFrame().acceptedCount());
        log.put("/Vision/TagsRejected", robot.drive.lastFrame().count() - robot.drive.lastFrame().acceptedCount());
        log.put("/Vision/StillFix", robot.drive.stillFix().ordinal());
        log.put("/Vision/StillFixesApplied", robot.drive.stillFixesApplied());
        recordDrift(mode, pose, referenced);
        if (mode != MatchLog.Mode.DISABLED) {
            // Where the robot is, every loop, so an OpMode that crashes still leaves it behind.
            Handoff.update(mode == MatchLog.Mode.AUTONOMOUS, alliance, referenced,
                    referenced ? pose.x() : 0.0, referenced ? pose.y() : 0.0,
                    referenced ? pose.heading() : 0.0, robot.turret.zeroed(), robot.turret.zeroCounts(),
                    matchId, System.currentTimeMillis());
        }
        // The keys #181 asks for, so a real match log compares with the simulator's.
        log.put("/Intake/TicksPerSecond", robot.intake.ticksPerSecond());
        double intakeAmps = robot.intake.currentAmps();
        if (!Double.isNaN(intakeAmps)) {
            log.put("/Intake/CurrentAmps", intakeAmps);
        }
        // The intake running with nothing asking for it: it joined a feed (IntakeSubsystem.rollerFor).
        log.put("/Intake/FeedAssist", robot.intake.feedAssist() ? 1 : 0);
        log.put("/Intake/HoldingForFull", robot.intake.holdingForFull() ? 1 : 0);
        log.put("/Intake/PositionHolding", robot.intake.positionHolding() ? 1 : 0);
        log.put("/Intake/HoldTargetTicks", robot.intake.holdTargetTicks());
        log.put("/Intake/HoldErrorTicks", robot.intake.positionHolding() ? robot.intake.holdErrorTicks() : 0);
        // The launcher's speed loop and the feeder, so a log shows what the wheels did at each shot.
        log.put("/Launcher/TargetTicksPerSecond", robot.launcher.isSpinning() ? robot.launcher.target() : 0.0);
        log.put("/Launcher/LeftTicksPerSecond", robot.launcher.leftTicksPerSecond());
        log.put("/Launcher/RightTicksPerSecond", robot.launcher.rightTicksPerSecond());
        log.put("/Launcher/AtSpeed", robot.launcher.atSpeed() ? 1 : 0);
        log.put("/Launcher/Shots", robot.launcher.shots());
        log.put("/Launcher/InDip", robot.launcher.inDip() ? 1 : 0);
        double lastDip = robot.launcher.lastShotDipTicksPerSecond();
        if (!Double.isNaN(lastDip)) {
            log.put("/Launcher/LastShotDipTicksPerSecond", lastDip);
        }
        double leftAmps = robot.launcher.leftCurrentAmps();
        double rightAmps = robot.launcher.rightCurrentAmps();
        if (!Double.isNaN(leftAmps)) {
            log.put("/Launcher/LeftCurrentAmps", leftAmps);
        }
        if (!Double.isNaN(rightAmps)) {
            log.put("/Launcher/RightCurrentAmps", rightAmps);
        }
        log.put("/Launcher/Reversing", robot.launcher.reversing() ? 1 : 0);
        // Each spin-up's and each shot dip's timing (LauncherResponse), with the battery, once each.
        LauncherResponse response = robot.launcher.response();
        if (response.spinUps() != loggedSpinUps) {
            loggedSpinUps = response.spinUps();
            log.put("/Launcher/RiseMs", response.riseMs());
            log.put("/Launcher/SpinOvershootTicksPerSecond", response.spinOvershootTps());
            log.put("/Launcher/SettleMs", response.settleMs());
            log.event(String.format(Locale.US,
                    "launcher: spin-up from %.0f to %.0f ticks/s: in tolerance %.0f ms, overshoot %.0f, at speed %.0f ms (battery %.2f V)",
                    response.spinFromTps(), robot.launcher.target(), response.riseMs(), response.spinOvershootTps(),
                    response.settleMs(), lastBatteryVolts));
        }
        if (response.dips() != loggedDips) {
            loggedDips = response.dips();
            log.put("/Launcher/DipDepthTicksPerSecond", response.dipDepthTps());
            log.put("/Launcher/RecoverMs", response.recoverMs());
            log.put("/Launcher/DipOvershootTicksPerSecond", response.dipOvershootTps());
            if (!Double.isNaN(response.resettleMs())) log.put("/Launcher/ResettleMs", response.resettleMs());
            // The gap from the shot before, beside the recovery: a feed paced right has gap > recover.
            if (!Double.isNaN(response.shotGapMs())) log.put("/Launcher/ShotGapMs", response.shotGapMs());
            log.event(String.format(Locale.US,
                    "launcher: dip %.0f ticks/s, back in tolerance %.0f ms, overshoot %.0f, at speed %s, gap from last shot %s (feeder %.2f, battery %.2f V)",
                    response.dipDepthTps(), response.recoverMs(), response.dipOvershootTps(),
                    Double.isNaN(response.resettleMs()) ? "not again" : String.format(Locale.US, "%.0f ms", response.resettleMs()),
                    Double.isNaN(response.shotGapMs()) ? "none (first)" : String.format(Locale.US, "%.0f ms", response.shotGapMs()),
                    robot.feeder.power(), lastBatteryVolts));
        }
        // The hub velocity loop's gains as written (nothing until Panels sets one), and each write as an event.
        if (robot.launcher.pidfWrites() != loggedPidfWrites) {
            loggedPidfWrites = robot.launcher.pidfWrites();
            log.put("/Launcher/VelocityP", robot.launcher.appliedP());
            log.put("/Launcher/VelocityI", robot.launcher.appliedI());
            log.put("/Launcher/VelocityD", robot.launcher.appliedD());
            log.put("/Launcher/VelocityF", robot.launcher.appliedF());
            log.put("/Launcher/FScale", robot.launcher.fScale());
            log.event(String.format(Locale.US, "launcher: velocity PIDF %s (F scale %.3f, battery %.2f V)",
                    LauncherSubsystem.gainsText(robot.launcher.appliedP(), robot.launcher.appliedI(),
                            robot.launcher.appliedD(), robot.launcher.appliedF()),
                    robot.launcher.fScale(), lastBatteryVolts));
        }
        log.put("/Feeder/Power", robot.feeder.power());
        // The battery's scale on that power (FeederSubsystem.compensateVoltage); 1 when off.
        log.put("/Feeder/VoltageScale", robot.feeder.voltageScale());
        log.put("/Feeder/TicksPerSecond", robot.feeder.ticksPerSecond());
        // Surface speeds, comparable across the two (ticks per second are not); NaN until measured, so skipped.
        double feederIps = robot.feeder.surfaceInchesPerSecond();
        if (!Double.isNaN(feederIps)) log.put("/Feeder/SurfaceInchesPerSecond", feederIps);
        double launcherIps = robot.launcher.surfaceInchesPerSecond();
        if (!Double.isNaN(launcherIps)) log.put("/Launcher/SurfaceInchesPerSecond", launcherIps);
        double feederAmps = robot.feeder.currentAmps();
        if (!Double.isNaN(feederAmps)) {
            log.put("/Feeder/CurrentAmps", feederAmps);
        }
        // The transfer: the prefeed's power, and whether the gate is asked open (with where it was last written).
        log.put("/Prefeed/Power", robot.prefeed.power());
        log.put("/FeedGate/Feeding", robot.feedGate.state() == FeedGateSubsystem.State.FEEDING ? 1 : 0);
        double gatePosition = robot.feedGate.position();
        if (!Double.isNaN(gatePosition)) {
            log.put("/FeedGate/Position", gatePosition);
        }
        // The colour sensor, data only: written when it read, so the log shows its real rate.
        if (robot.colorSensor.fresh()) {
            log.put("/ColorSensor/Red", robot.colorSensor.red());
            log.put("/ColorSensor/Green", robot.colorSensor.green());
            log.put("/ColorSensor/Blue", robot.colorSensor.blue());
            log.put("/ColorSensor/Alpha", robot.colorSensor.alpha());
            double hue = robot.colorSensor.hueDegrees();
            if (!Double.isNaN(hue)) log.put("/ColorSensor/HueDeg", hue);
            log.put("/ColorSensor/Saturation", robot.colorSensor.saturation());
        }
        // Which read rate ran (1 fast: a piece can move; 0 slow), so a full held between slow reads
        // shows as one in the log (ColorSensorSubsystem, "When it reads").
        log.put("/ColorSensor/Watching", robot.colorSensor.watching() ? 1 : 0);
        // The lane tracker's guess (LaneTracker), data only: count, each slot (0 empty, 1 POLLEN,
        // 2 red NECTAR, 3 blue NECTAR, 4 unknown, front first), and its arrivals, shots and ejects as events.
        LaneTracker lane = robot.colorSensor.lane();
        log.put("/Lane/Count", lane.count());
        log.put("/Lane/Present", lane.present() ? 1 : 0);
        for (int i = 0; i < LaneTracker.SLOTS; i++) {
            LaneTracker.Piece piece = lane.slot(i);
            log.put(LANE_SLOT_KEYS[i], piece == null ? 0 : piece.ordinal() + 1);
        }
        if (lane.event() != null) log.event(lane.event());
        // The other alliance's NECTAR spat out (NectarReject): one event per burst, with the reading.
        log.put("/Lane/Rejecting", robot.intake.rejecting() ? 2 : robot.intake.holdingOff() ? 1 : 0);
        if (robot.colorSensor.reject().justStarted()) {
            log.event(String.format(Locale.US,
                    "Lane: REJECT %s at the entrance (hue %.0f°, sat %.2f, brightness %.2f), burst %.2f s, #%d",
                    robot.colorSensor.lastPiece(), robot.colorSensor.hueDegrees(), robot.colorSensor.saturation(),
                    robot.colorSensor.alpha(), ColorSensorSubsystem.rejectBurstS, robot.colorSensor.reject().bursts()));
        }
        if (robot.colorSensor.reject().justGaveUp()) {
            log.event(String.format(Locale.US,
                    "Lane: REJECT gave up: %d bursts in %.1f s, the wrong NECTAR may still be in",
                    ColorSensorSubsystem.rejectMaxBursts, ColorSensorSubsystem.rejectWindowS));
        }
        // Full (LaneFull): the signal the light, the full hold and IntakeFull use. Every piece's
        // dwell under the sensor, so normal-play logs can tighten ColorSensorSubsystem.fullHoldS.
        LaneFull full = robot.colorSensor.laneFull();
        log.put("/Lane/Full", full.full() ? 1 : 0);
        log.put("/Lane/HeldWhileIntakingMs", full.heldWhileIntakingMs());
        // Full on and off as events, with the tracker's count: full then three or four shots means it was.
        if (full.full() != loggedLaneFull) {
            loggedLaneFull = full.full();
            // Full with a rejected NECTAR under the sensor: a reject did not clear it, and an Auto's
            // IntakeFull would move on holding it.
            if (loggedLaneFull && NectarReject.rejects(robot.colorSensor.lastPiece(), setup.alliance(),
                    ColorSensorSubsystem.rejectAllNectar)) {
                log.event("Lane: FULL with a rejected NECTAR under the sensor (" + robot.colorSensor.lastPiece()
                        + "): a reject did not clear it");
            }
            log.event(loggedLaneFull
                    ? String.format(Locale.US, "Lane: FULL, piece under the sensor %.1f s (%.1f s intaking), tracker counts %d",
                            full.presentForMs() / 1000, full.heldWhileIntakingMs() / 1000, lane.count())
                    : Double.isNaN(full.endedDwellMs())
                            ? "Lane: full cleared (fullHoldS turned off), tracker counts " + lane.count()
                            : String.format(Locale.US, "Lane: full cleared after %.1f s under the sensor, tracker counts %d",
                                    full.endedDwellMs() / 1000, lane.count()));
        }
        // Once per departure: the sensor reads every 50 ms, the loop more often.
        if (full.departures() != loggedDepartures) {
            loggedDepartures = full.departures();
            log.put("/Lane/DwellMs", full.endedDwellMs());
            log.event(String.format(Locale.US, "Lane: piece under the sensor %.0f ms%s",
                    full.endedDwellMs(), full.endedFull() ? " (was full)" : ""));
        }
        // The turret: where it points, where it was asked to, and whether anyone zeroed it.
        log.put("/Turret/AngleDeg", robot.turret.angleDegrees());
        log.put("/Turret/TargetDeg", robot.turret.targetDegrees());
        log.put("/Turret/ErrorDeg", robot.turret.errorDegrees());
        log.put("/Turret/Zeroed", robot.turret.zeroed() ? 1 : 0);
        log.put("/Turret/Holding", robot.turret.holding() ? 1 : 0);
        long now = System.nanoTime();
        // A mechanism asked to move that never does, as one event: the only way a match log can
        // learn a motor is absent, since the hub cannot tell an empty port from a stalled motor.
        String launcherFault = robot.launcher.fault();
        if (launcherFault != null && !launcherFault.equals(loggedLauncherFault)) {
            log.event("launcher: " + launcherFault);
        }
        loggedLauncherFault = launcherFault;
        // Not while holding a full lane: the slow roller pressing on the last piece may barely turn.
        String stalled = intakeWatch.check(robot.intake.state() != IntakeSubsystem.State.IDLE
                        && !robot.intake.holdingForFull(),
                robot.intake.ticksPerSecond(), now);
        if (stalled != null) log.event(stalled);
        stalled = feederWatch.check(robot.feeder.power() != 0.0, robot.feeder.ticksPerSecond(), now);
        if (stalled != null) log.event(stalled);
        if (!Double.isNaN(loopMs)) {
            log.loopMs(loopMs);
            if (loopMs > SLOW_LOOP_EVENT_MS) {
                log.event(String.format(Locale.US, "slow loop: %.0f ms", loopMs));
            }
        }
        if (battery != null && now - lastBatteryNs >= BATTERY_EVERY_MS * 1_000_000L) {
            lastBatteryVolts = battery.getVoltage();
            log.put("/Robot/BatteryVolts", lastBatteryVolts);
            lastBatteryNs = now;
            // For the launcher's F and the feeder's power when they compensate: this read, no other.
            robot.batteryVolts(lastBatteryVolts);
        }
        log.commit();
    }

    /** The SDK's gamepad into the log's plain copy of it. */
    private static void copyGamepad(Gamepad from, GamepadLog.State to) {
        to.a = from.a;
        to.b = from.b;
        to.x = from.x;
        to.y = from.y;
        to.back = from.back;
        to.guide = from.guide;
        to.start = from.start;
        to.leftStickButton = from.left_stick_button;
        to.rightStickButton = from.right_stick_button;
        to.leftBumper = from.left_bumper;
        to.rightBumper = from.right_bumper;
        to.dpadUp = from.dpad_up;
        to.dpadDown = from.dpad_down;
        to.dpadLeft = from.dpad_left;
        to.dpadRight = from.dpad_right;
        to.touchpad = from.touchpad;
        to.leftStickX = from.left_stick_x;
        to.leftStickY = from.left_stick_y;
        to.rightStickX = from.right_stick_x;
        to.rightStickY = from.right_stick_y;
        to.leftTrigger = from.left_trigger;
        to.rightTrigger = from.right_trigger;
    }

    /** The name the Driver Station lists, or the class name if the annotation leaves it blank. */
    private String opModeName() {
        TeleOp teleOp = getClass().getAnnotation(TeleOp.class);
        Autonomous auto = getClass().getAnnotation(Autonomous.class);
        String name = teleOp != null ? teleOp.name() : auto != null ? auto.name() : "";
        return name.isEmpty() ? getClass().getSimpleName() : name;
    }

    /** Index loop, not for-each: this runs every loop and must not allocate an iterator. */
    private void clearBulkCache() {
        for (int i = 0; i < hubs.size(); i++) {
            hubs.get(i).clearBulkCache();
        }
    }
}
