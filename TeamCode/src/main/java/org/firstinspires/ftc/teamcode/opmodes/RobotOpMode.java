package org.firstinspires.ftc.teamcode.opmodes;

import com.pedropathing.ivy.Scheduler;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.subsystems.Subsystem;

/**
 * The base class for OpModes that drive the whole robot. It builds the {@link Robot}, runs the Ivy
 * {@code Scheduler} around your code, and shuts everything down at the end — so your OpMode is only
 * the part that is actually about your OpMode.
 *
 * <p>Write one like this:
 *
 * <pre>{@code
 * @TeleOp(name = "My OpMode", group = "Drive")
 * public class MyOpMode extends RobotOpMode {
 *     @Override
 *     protected void onInit() {
 *         telemetry.addLine("Ready.");
 *     }
 *
 *     @Override
 *     protected void onLoop() {
 *         telemetry.addData("Camera", robot.vision.state());
 *     }
 * }
 * }</pre>
 *
 * <p>{@code robot} is already built by the time {@link #onInit()} runs, every subsystem has been
 * initialized, and every subsystem is being stepped. You do not call {@code Scheduler.reset()},
 * {@code Scheduler.execute()} or {@code robot.stop()} anywhere — that is what this class is for.
 *
 * <h2>Why this exists</h2>
 *
 * <p>Before it, this exact block was copied into three OpModes, byte for byte:
 *
 * <pre>{@code
 * vision = new LimelightVisionSubsystem(hardwareMap);
 * vision.initialize();
 * Scheduler.reset();
 * Scheduler.schedule(vision.periodic());
 * ...
 * public void init_loop() { Scheduler.execute(); }
 * public void stop() { if (vision != null) vision.stop(); Scheduler.reset(); }
 * }</pre>
 *
 * <p>Three copies means three places to fix when the lifecycle changes, and it had already drifted
 * — one of the three guarded {@code vision} for null and the others did not.
 *
 * <p>Ivy does not ship a class like this; its own docs show {@code Scheduler.reset()} and
 * {@code execute()} written inline in a {@code LinearOpMode}. This follows the pattern Ivy's
 * <a href="https://pedropathing.com/docs/ivy/example-repos">example repos</a> converged on instead,
 * where #22131's {@code RobotOpMode.java} does the same job.
 *
 * <h2>The scheduler is optional</h2>
 *
 * <p>Ivy's {@code Scheduler} exists to arbitrate — to stop two commands driving the same motor at
 * once, and to sequence one after another. Autonomous needs that. A TeleOp that just reads the
 * sticks and drives usually does not, and {@code Scheduler.execute()} allocates about three objects
 * on every call even when nothing is scheduled.
 *
 * <p>So an OpMode with nothing to arbitrate can turn it off by overriding {@link #useScheduler()}
 * to return {@code false}, and step its subsystems itself:
 *
 * <pre>{@code
 * @Override protected boolean useScheduler() { return false; }
 *
 * @Override protected void onLoop() {
 *     robot.drive.update();
 * }
 * }</pre>
 *
 * <p>Both are correct. The difference is microseconds, not milliseconds — pick whichever makes your
 * OpMode easier to read, and leave it alone if you are not sure.
 */
public abstract class RobotOpMode extends OpMode {

    /** Every mechanism on the robot. Built before {@link #onInit()} runs. */
    protected Robot robot;

    /**
     * {@link #useScheduler()}, read once at init.
     *
     * <p>Cached so the answer cannot change halfway through a match — an OpMode that scheduled its
     * subsystems at init and then stopped calling {@code Scheduler.execute()} would look alive while
     * quietly updating nothing.
     */
    private boolean schedulerInUse;

    // ------------------------------------------------------------------ hooks

    /** Called once, after the robot is built and initialized. Put your init telemetry here. */
    protected void onInit() {
    }

    /** Called repeatedly between init and start. Override if you need it; most OpModes do not. */
    protected void onInitLoop() {
    }

    /** Called once, when the driver presses play. */
    protected void onStart() {
    }

    /** Called every loop after start. This is where your OpMode's actual work goes. */
    protected abstract void onLoop();

    /** Called once when the OpMode ends, before the robot is shut down. */
    protected void onStop() {
    }

    /**
     * Whether to run the Ivy {@code Scheduler} and schedule every subsystem's
     * {@link Subsystem#periodic()}.
     *
     * <p>Default {@code true}. Return {@code false} to skip it entirely and call
     * {@link Subsystem#update()} yourself — see the class javadoc.
     */
    protected boolean useScheduler() {
        return true;
    }

    // -------------------------------------------------------------- lifecycle

    @Override
    public final void init() {
        robot = new Robot(hardwareMap);
        robot.initialize();

        schedulerInUse = useScheduler();
        if (schedulerInUse) {
            // The scheduler is static, so commands survive from one OpMode to the next unless this
            // is called. Reset before scheduling, never after.
            Scheduler.reset();
            for (Subsystem subsystem : robot.subsystems()) {
                Scheduler.schedule(subsystem.periodic());
            }
        }

        onInit();
    }

    @Override
    public final void init_loop() {
        if (schedulerInUse) {
            Scheduler.execute();
        }
        onInitLoop();
    }

    @Override
    public final void start() {
        onStart();
    }

    @Override
    public final void loop() {
        if (schedulerInUse) {
            Scheduler.execute();
        }
        onLoop();
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
}
