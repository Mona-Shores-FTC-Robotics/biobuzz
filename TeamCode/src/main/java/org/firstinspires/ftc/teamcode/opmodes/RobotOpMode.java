package org.firstinspires.ftc.teamcode.opmodes;

import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.VoltageSensor;

import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.controls.Bindings;
import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.controls.Handoff;
import org.firstinspires.ftc.teamcode.controls.MatchSetup;
import org.firstinspires.ftc.teamcode.hardware.ActiveConfig;
import org.firstinspires.ftc.teamcode.logging.AdvantageScopeKeys;
import org.firstinspires.ftc.teamcode.logging.GamepadLog;
import org.firstinspires.ftc.teamcode.logging.MatchLog;
import org.firstinspires.ftc.teamcode.logging.MatchLogFiles;
import org.firstinspires.ftc.teamcode.localization.StartCheck;
import org.firstinspires.ftc.teamcode.localization.StartPosition;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.FieldFrame;
import org.firstinspires.ftc.teamcode.subsystems.Subsystem;
import org.firstinspires.ftc.teamcode.util.LoopTimer;

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
 *   <li><b>The match log.</b> See {@link #log}: this loop's state goes to the background writer.</li>
 * </ol>
 *
 * <p>{@code init_loop()} does the same with {@link #onInitLoop()}, except that bindings are not
 * polled: the robot may not move before PLAY. {@link #loopTimer} is lapped at the top of every
 * {@code loop()}.
 *
 * <p>Never set a bulk caching mode, clear the cache, or call {@code Scheduler.reset()} /
 * {@code execute()} / {@code robot.stop()} in a subclass. That is this class's job, and doing it
 * twice is how an OpMode ends up reading stale sensors or double-stepping a mechanism. {@code MANUAL}
 * without a clear returns the same stale values forever and does not complain; having exactly one
 * place that does both is what makes that mistake impossible.
 *
 * <h2>Before PLAY, and the Auto → TeleOp handoff</h2>
 *
 * <p>{@link #setup} settles the alliance during INIT — vision proposes, X/B on either gamepad
 * overrides — and locks it at PLAY; see {@link MatchSetup}. When an {@code @Autonomous} OpMode
 * stops, this records the alliance and the robot's final pose in {@link Handoff}. When any other
 * OpMode initializes within {@link Handoff#MAX_AGE_MS} of that, it restores the pose and inherits
 * the alliance, and the Match page says so — or says there was no handoff. Subclasses never touch
 * either.
 *
 * <p>An Autonomous overrides {@link #startPosition()}. The pose starts there, and during INIT the
 * camera checks the placement ({@link StartCheck}): a confirmed placement is also what confirms the
 * alliance from vision.
 *
 * <h2>The match log</h2>
 *
 * <p>Every run writes {@code /sdcard/FIRST/logs/<OpMode>_<date>_<time>.wpilog}, which AdvantageScope
 * opens: match state, alliance, both gamepads, the pose, loop time, battery voltage and events
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

    private long logStartNs;
    private VoltageSensor battery;
    private long lastBatteryNs;
    private Alliance loggedAlliance;
    private MatchSetup.Source loggedSource;

    private List<LynxModule> hubs;
    private boolean prevPageButton;

    /** True when the drive pose was restored from Autonomous, so headings are field-absolute. */
    private boolean poseFromAuto;
    private Display.Level handoffLevel;
    private String handoffNote;

    private StartPosition declaredStart;

    // ------------------------------------------------------------------ hooks

    /**
     * Where this OpMode expects the robot to start, from {@code StartPositions}. Autonomous
     * overrides it; null (the default) means no declared start and no start check.
     */
    protected StartPosition startPosition() {
        return null;
    }

    /** Called once, after the robot is built and initialized. Put your init telemetry here. */
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
        display = new Display(telemetry);
        openLog();

        // Before the robot is built, so no read anywhere — constructors included — bypasses it.
        hubs = hardwareMap.getAll(LynxModule.class);
        for (int i = 0; i < hubs.size(); i++) {
            hubs.get(i).setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
        }

        try {
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
        declaredStart = startPosition();
        if (declaredStart != null) {
            robot.drive.setPose(declaredStart.pose);
        }

        // The scheduler is static, so commands survive from one OpMode to the next unless this
        // is called. Reset before scheduling, never after.
        Scheduler.reset();
        List<Subsystem> subsystems = robot.subsystems();
        for (int i = 0; i < subsystems.size(); i++) {
            Scheduler.schedule(subsystems.get(i).periodic());
        }

        onInit();
    }

    @Override
    public final void init_loop() {
        clearBulkCache();
        StartCheck.Result startCheck = StartCheck.evaluate(declaredStart, robot.drive.fixCount(),
                robot.drive.meanFixX(), robot.drive.meanFixY());
        setup.offerVision(startCheck.confirmedAlliance(), robot.vision.allianceEvidence());
        if (gamepad1.x || gamepad2.x) setup.chooseManually(Alliance.BLUE);
        if (gamepad1.b || gamepad2.b) setup.chooseManually(Alliance.RED);
        beginPage();
        if (handoffNote != null) {
            display.status("Start", handoffLevel, handoffNote);
        }
        startCheck.describe(display);
        onInitLoop();
        Scheduler.execute();
        finishPage();
        recordLoop(MatchLog.Mode.DISABLED, Double.NaN);
    }

    @Override
    public final void start() {
        loopTimer.reset();
        setup.lock();
        driver.prime();
        operator.prime();
        if (!isAutonomous()) {
            double forward = FieldFrame.driverForwardHeading(setup.alliance());
            if (poseFromAuto && !Double.isNaN(forward)) {
                robot.drive.setFieldForward(forward);
            } else {
                robot.drive.resetHeading();
            }
        }
        log.event("PLAY: " + setup.alliance() + " alliance");
        onStart();
    }

    @Override
    public final void loop() {
        loopTimer.lap();
        clearBulkCache();
        beginPage();
        driver.update();
        operator.update();
        onLoop();
        Scheduler.execute();
        finishPage();
        recordLoop(isAutonomous() ? MatchLog.Mode.AUTONOMOUS : MatchLog.Mode.TELEOP, loopTimer.lastMs());
    }

    @Override
    public final void stop() {
        onStop();

        if (isAutonomous() && robot != null) {
            // Only a field pose is worth handing on. An Auto with no declared start knows where
            // it is relative to init, not on the field; passing that on would make TeleOp treat
            // it as field-referenced and feed camera fixes computed from a meaningless heading.
            Pose pose = robot.drive.poseReferenced() ? robot.drive.pose() : null;
            Handoff.record(setup.alliance(), pose, System.currentTimeMillis());
        }

        // Guarded because stop() runs even when init() threw partway through — a missing device, a
        // bad configuration — and an exception in stop() replaces the one that actually explains
        // what went wrong.
        if (robot != null) {
            robot.stop();
        }
        Scheduler.reset();
        log.close("OpMode stopped", LOG_CLOSE_WAIT_MS);
    }

    /** Page button edge, then the header — so the OpMode's Match lines land under it. */
    private void beginPage() {
        boolean pressed = gamepad1.back;
        if (pressed && !prevPageButton) {
            display.nextPage();
        }
        prevPageButton = pressed;
        display.header();
        setup.describe(display);
    }

    /** TeleOp side of the handoff: restore Auto's pose and alliance, and say which happened. */
    private void receiveHandoff() {
        if (isAutonomous()) {
            return;
        }
        long now = System.currentTimeMillis();
        Handoff.Snapshot handoff = Handoff.fresh(now);
        if (handoff == null) {
            handoffLevel = Display.Level.WARN;
            handoffNote = "no Auto handoff — pose starts at 0,0; forward = robot's facing at PLAY";
            return;
        }
        setup.inheritFromAuto(handoff.alliance);
        String age = (handoff.ageMs(now) / 1000) + "s ago";
        if (handoff.pose != null && robot.drive.hasHeading()) {
            robot.drive.setPose(handoff.pose);
            poseFromAuto = true;
            boolean forwardKnown = !Double.isNaN(FieldFrame.driverForwardHeading(handoff.alliance));
            handoffLevel = forwardKnown ? Display.Level.OK : Display.Level.WARN;
            handoffNote = String.format(java.util.Locale.US,
                    "pose from Auto %s (%.0f, %.0f, %.0f°)%s", age,
                    handoff.pose.x(), handoff.pose.y(), Math.toDegrees(handoff.pose.heading()),
                    forwardKnown ? "" : "; forward = robot's facing at PLAY (not measured)");
        } else {
            handoffLevel = Display.Level.WARN;
            handoffNote = "alliance from Auto " + age + ", but no field pose (Auto declared no "
                    + "start, or no Pinpoint); forward = robot's facing at PLAY";
        }
    }

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
        if (page == Display.Page.CONTROLS) {
            display.section("BEFORE PLAY — either gamepad");
            display.line("X — Blue alliance");
            display.line("B — Red alliance");
            display.line("Back/Share — next page (any time)");
            describeBindings(driver);
            describeBindings(operator);
        } else {
            display.section("Loop");
            display.line(loopTimer.summary());
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
        logStartNs = System.nanoTime();
        String name = opModeName();
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("OpMode", name);
        metadata.put("OpModeClass", getClass().getName());
        String config = ActiveConfig.name();
        metadata.put("RobotConfig", config == null ? "(none active)" : config);
        File folder = new File(AppUtil.FIRST_FOLDER, MatchLogFiles.LOGS_FOLDER);
        File file = MatchLogFiles.next(folder, name, System.currentTimeMillis());
        log = MatchLog.toFile(file, "BIOBUZZ " + name, metadata,
                () -> (System.nanoTime() - logStartNs) / 1000L);
        log.event("OpMode init: " + name);
    }

    /** This loop's state into the log, then hand it to the writer. Copies numbers only. */
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
        if (pose != null) {
            log.pose(pose.x(), pose.y(), pose.heading());
            log.put("/Odometry/Referenced", robot.drive.poseReferenced() ? 1 : 0);
        }
        if (!Double.isNaN(loopMs)) {
            log.loopMs(loopMs);
            if (loopMs > SLOW_LOOP_EVENT_MS) {
                log.event(String.format(Locale.US, "slow loop: %.0f ms", loopMs));
            }
        }
        long now = System.nanoTime();
        if (battery != null && now - lastBatteryNs >= BATTERY_EVERY_MS * 1_000_000L) {
            log.put("/Robot/BatteryVolts", battery.getVoltage());
            lastBatteryNs = now;
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
