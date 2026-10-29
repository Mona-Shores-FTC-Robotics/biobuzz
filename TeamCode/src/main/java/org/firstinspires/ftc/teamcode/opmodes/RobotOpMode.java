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
import org.firstinspires.ftc.teamcode.logging.BuildStamp;
import org.firstinspires.ftc.teamcode.logging.MotionWatch;
import org.firstinspires.ftc.teamcode.logging.Tunables;
import org.firstinspires.ftc.teamcode.subsystems.IntakeSubsystem;
import org.firstinspires.ftc.teamcode.localization.StartCheck;
import org.firstinspires.ftc.teamcode.localization.StartPosition;
import org.firstinspires.ftc.teamcode.localization.TagGate;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.FieldFrame;
import org.firstinspires.ftc.teamcode.util.FieldView;
import org.firstinspires.ftc.teamcode.subsystems.Subsystem;
import org.firstinspires.ftc.teamcode.subsystems.TurretZeroPolicy;
import org.firstinspires.ftc.teamcode.util.LoopTimer;
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
 * stops, this records the alliance, the robot's final pose and the turret's zero in {@link Handoff}.
 * When any other OpMode initializes within {@link Handoff#MAX_AGE_MS} of that, it restores the pose
 * and the turret's zero and inherits the alliance, and the Match page says so — or says there was
 * no handoff. With no Auto before it, the turret is assumed at home and zeroes where it stands at
 * PLAY ({@link TurretZeroPolicy}); a person's zero button always wins. Subclasses never touch
 * either.
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
    private Alliance loggedAlliance;
    private MatchSetup.Source loggedSource;

    private List<LynxModule> hubs;
    private boolean prevPageButton;

    /** True when the drive pose was restored from Autonomous, so headings are field-absolute. */
    private Display.Level handoffLevel;
    private String handoffNote;

    private StartPosition declaredStart;

    /** How many stack frames of a crash go into the match log: enough to find the line, short enough for one event. */
    private static final int CRASH_FRAMES = 6;

    /** Below this at INIT the battery is reported, on the INIT page and in the log: 12.0 V resting is nearly empty. */
    static final double LOW_BATTERY_VOLTS = 12.0;
    /** The INIT page's battery warning, null when the battery read fine or there is no sensor. */
    private String batteryNote;

    // A mechanism asked to move whose encoder never does: one event each, from the log alone.
    private final MotionWatch intakeWatch = new MotionWatch("intake");
    private final MotionWatch feederWatch = new MotionWatch("feeder");
    private String loggedLauncherFault;

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
        // Once, here: the view is a handle on Panels' shared field, not something to look up per
        // loop. A dashboard failure is not a robot failure, so it is allowed to be absent.
        try {
            fieldView = FieldView.pedroCoordinates();
        } catch (RuntimeException e) {
            fieldView = null;
            log.event("field view unavailable: " + e);
        }

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
        StartCheck.Result startCheck = StartCheck.evaluate(declaredStart, robot.drive.fixCount(),
                robot.drive.meanFixX(), robot.drive.meanFixY());
        setup.offerVision(startCheck.confirmedAlliance(), robot.vision.allianceEvidence());
        beginPage();
        if (handoffNote != null) {
            display.status("Start", handoffLevel, handoffNote);
        }
        if (batteryNote != null) {
            display.status("Battery", Display.Level.WARN, batteryNote);
        }
        startCheck.describe(display);
        driver.updateDuringInit();
        operator.updateDuringInit();
        onInitLoop();
        Scheduler.execute();
        finishPage();
        drawField(true);
        recordLoop(MatchLog.Mode.DISABLED, Double.NaN);
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
        logStartCheck();
        setup.lock();
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
        beginPage();
        driver.update();
        operator.update();
        long t0 = System.nanoTime();
        onLoop();
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
            robot.stop();
        }
        Scheduler.reset();
        if (handedOff) {
            // The file stays open for the TeleOp that follows; unclaimed, it closes by itself.
            log.pause("OpMode stopped: " + opModeName(), Handoff.MAX_AGE_MS);
        } else {
            log.close("OpMode stopped: " + opModeName(), LOG_CLOSE_WAIT_MS);
        }
    }

    /**
     * At PLAY, where the camera put the robot against where it was declared to start, into the
     * match log's Metadata tab ({@code StartCheck}) and as numbers ({@code /StartCheck/*}). The
     * drive team placed it by the marks, so each run is a calibration sample: a gap that follows
     * one robot to every field is its camera mount; one that shows on every robot at one field is
     * that field's HIVE. {@code RobotConfig} and the date are already in the log to tell them apart.
     */
    private void logStartCheck() {
        if (declaredStart == null) {
            return;
        }
        StartCheck.Result r = StartCheck.evaluate(declaredStart, robot.drive.fixCount(),
                robot.drive.meanFixX(), robot.drive.meanFixY());
        if (r.status == StartCheck.Status.UNCONFIRMED) {
            log.metadata("StartCheck", declaredStart + ": camera never confirmed ("
                    + robot.drive.fixCount() + " fixes)");
            return;
        }
        double dx = robot.drive.meanFixX() - declaredStart.pose.x();
        double dy = robot.drive.meanFixY() - declaredStart.pose.y();
        TagGate frame = robot.drive.lastFrame();
        StringBuilder tags = new StringBuilder();
        for (int i = 0; i < frame.count(); i++) {
            if (frame.reason(i) != TagGate.Reason.ACCEPTED) continue;
            if (tags.length() > 0) tags.append(' ');
            tags.append(frame.tagId(i));
        }
        log.metadata("StartCheck", String.format(Locale.US,
                "%s: camera minus start x %+.2f, y %+.2f in (%.2f in; robot fwd %+.2f, left %+.2f); "
                        + "%d fixes; tags %s",
                declaredStart, dx, dy, r.errorIn, -r.moveForwardIn, -r.moveLeftIn,
                robot.drive.fixCount(), tags.length() == 0 ? "none" : tags));
        log.put("/StartCheck/CameraMinusStartXIn", dx);
        log.put("/StartCheck/CameraMinusStartYIn", dy);
        log.put("/StartCheck/CameraFwdOfStartIn", -r.moveForwardIn);
        log.put("/StartCheck/CameraLeftOfStartIn", -r.moveLeftIn);
    }

    /** Where the robot should start (during INIT), and where the camera says it is, on the field. */
    private static final String START_COLOUR = "#00E676";
    private static final String CAMERA_COLOUR = "#FFB300";

    /**
     * The robot on the Panels Field view, after the subsystems have updated so it is this loop's
     * pose. During INIT, an OpMode with a declared start also gets that start (green) and where the
     * camera's latest fixes put the robot (amber): the gap between them is the start check's
     * error, and the way to move the robot. Then {@link #drawOnField} adds the OpMode's own.
     * Gated on the throttle, so on most loops this is one boolean read; skipped when the drive has
     * no heading (no Pinpoint), since then there is no pose to draw. The publish is caught and
     * swallowed: telemetry never takes a mechanism down.
     */
    private void drawField(boolean initializing) {
        if (fieldView == null || !robot.drive.hasHeading()) {
            return;
        }
        try {
            if (fieldView.shouldDraw()) {
                Pose pose = robot.drive.pose();
                if (pose != null) {
                    fieldView.drawRobot(pose.x(), pose.y(), pose.heading());
                }
                if (initializing && declaredStart != null) {
                    Pose start = declaredStart.pose;
                    fieldView.drawRobot(start.x(), start.y(), start.heading(), START_COLOUR);
                    double cameraX = robot.drive.meanFixX();
                    double cameraY = robot.drive.meanFixY();
                    if (!Double.isNaN(cameraX)) {
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
     * An OpMode's own marks on the Panels Field view, drawn over the robot each time the view is
     * sent. Override instead of making a second {@code FieldView}: two views sending in turn make
     * the field flicker between them.
     */
    protected void drawOnField(FieldView field) {
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
            handoffNote = "no handoff (no Auto in the last 3 minutes, no TeleOp in the last 30 seconds) — "
                    + "pose starts at 0,0; forward = robot's facing at PLAY";
            return;
        }
        String from = handoff.fromAuto ? "Auto" : "the TeleOp that just stopped";
        setup.inheritFromAuto(handoff.alliance);
        if (TurretZeroPolicy.atInit(false, handoff.turretZeroCounts != null)
                == TurretZeroPolicy.Action.RESTORE_FROM_AUTO) {
            robot.turret.restoreZero(handoff.turretZeroCounts);
        }
        String age = (handoff.ageMs(now) / 1000) + "s ago";
        if (handoff.pose != null && robot.drive.hasHeading()) {
            robot.drive.setPose(handoff.pose);
            boolean forwardKnown = !Double.isNaN(FieldFrame.driverForwardHeading(handoff.alliance));
            handoffLevel = forwardKnown ? Display.Level.OK : Display.Level.WARN;
            handoffNote = String.format(java.util.Locale.US,
                    "pose from %s %s (%.0f, %.0f, %.0f°)%s", from, age,
                    handoff.pose.x(), handoff.pose.y(), Math.toDegrees(handoff.pose.heading()),
                    forwardKnown ? "" : "; forward = robot's facing at PLAY (not measured)");
        } else {
            handoffLevel = Display.Level.WARN;
            handoffNote = "alliance from " + from + " " + age + ", but no field pose (no declared "
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
            display.line("Square (X) — Blue alliance");
            display.line("Circle (B) — Red alliance");
            display.line("Share (Back) — next page (any time)");
            describeBindings(driver);
            describeBindings(operator);
        } else {
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
        // that Autonomous paused (the same handoff receiveHandoff() reads later).
        Handoff.Snapshot handoff = isAutonomous() ? null : Handoff.fresh(nowMs);
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
        // What the camera's gate made of the latest frame, and how the stationary fix stands.
        log.put("/Vision/TagsAccepted", robot.drive.lastFrame().acceptedCount());
        log.put("/Vision/TagsRejected", robot.drive.lastFrame().count() - robot.drive.lastFrame().acceptedCount());
        log.put("/Vision/StillFix", robot.drive.stillFix().ordinal());
        log.put("/Vision/StillFixesApplied", robot.drive.stillFixesApplied());
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
        log.put("/Feeder/Power", robot.feeder.power());
        log.put("/Feeder/TicksPerSecond", robot.feeder.ticksPerSecond());
        double feederAmps = robot.feeder.currentAmps();
        if (!Double.isNaN(feederAmps)) {
            log.put("/Feeder/CurrentAmps", feederAmps);
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
        String stalled = intakeWatch.check(robot.intake.state() != IntakeSubsystem.State.IDLE,
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
