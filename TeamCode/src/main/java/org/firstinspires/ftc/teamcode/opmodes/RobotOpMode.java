package org.firstinspires.ftc.teamcode.opmodes;

import com.pedropathing.ivy.Scheduler;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.controls.Bindings;
import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.subsystems.Subsystem;
import org.firstinspires.ftc.teamcode.util.LoopTimer;

import java.util.List;

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
 * </ol>
 *
 * <p>{@code init_loop()} does the same with {@link #onInitLoop()}, except that bindings are not
 * polled: the robot may not move before PLAY. {@link #loopTimer} is lapped at the top of every
 * {@code loop()}.
 *
 * <p>Never set a bulk caching mode, clear the cache, or call {@code Scheduler.reset()} /
 * {@code execute()} / {@code robot.stop()} in a subclass. That is this class's job, and doing it
 * twice is how an OpMode ends up reading stale sensors or double-stepping a mechanism.
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

    private List<LynxModule> hubs;
    private boolean prevPageButton;

    // ------------------------------------------------------------------ hooks

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

        // Before the robot is built, so no read anywhere — constructors included — bypasses it.
        hubs = hardwareMap.getAll(LynxModule.class);
        for (int i = 0; i < hubs.size(); i++) {
            hubs.get(i).setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
        }

        robot = new Robot(hardwareMap);
        robot.initialize();

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
        beginPage();
        onInitLoop();
        Scheduler.execute();
        finishPage();
    }

    @Override
    public final void start() {
        loopTimer.reset();
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
    }

    @Override
    public final void stop() {
        onStop();

        // Guarded because stop() runs even when init() threw partway through — a missing device, a
        // bad configuration — and an exception in stop() replaces the one that actually explains
        // what went wrong.
        if (robot != null) {
            robot.stop();
        }
        Scheduler.reset();
    }

    /** Page button edge, then the header — so the OpMode's Match lines land under it. */
    private void beginPage() {
        boolean pressed = gamepad1.back;
        if (pressed && !prevPageButton) {
            display.nextPage();
        }
        prevPageButton = pressed;
        display.header();
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
            describeBindings(driver);
            describeBindings(operator);
        } else {
            display.section("Loop");
            display.line(loopTimer.summary());
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

    /** Index loop, not for-each: this runs every loop and must not allocate an iterator. */
    private void clearBulkCache() {
        for (int i = 0; i < hubs.size(); i++) {
            hubs.get(i).clearBulkCache();
        }
    }
}
