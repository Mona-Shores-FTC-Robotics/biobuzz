package org.firstinspires.ftc.teamcode.subsystems;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.commands.Commands;

/**
 * One mechanism on the robot: a motor, a set of motors, a camera, an arm.
 *
 * <p><b>If you are writing your first subsystem, copy {@link ExampleSubsystem} and rename it.</b>
 * This interface asks you for three methods — {@link #initialize()}, {@link #update()} and
 * {@link #stop()} — and the compiler will tell you if you forget one. Leaving a body empty is fine;
 * the point is that you had to decide it should be.
 *
 * <h2>The lifecycle</h2>
 *
 * <table>
 *   <caption>When each part runs</caption>
 *   <tr><th>Method</th><th>Called</th><th>Put here</th></tr>
 *   <tr><td>constructor</td><td>once, when {@code Robot} is built</td>
 *       <td>{@code hardwareMap} lookups. Nothing else.</td></tr>
 *   <tr><td>{@link #initialize()}</td><td>once, in OpMode init, after every subsystem is built</td>
 *       <td>Getting ready: motor modes, starting a camera, a servo's start position.</td></tr>
 *   <tr><td>{@link #update()}</td><td>every loop</td><td>One step of the work.</td></tr>
 *   <tr><td>{@link #stop()}</td><td>once, when the OpMode ends</td>
 *       <td>Teardown: cut power, stop devices, clear state.</td></tr>
 * </table>
 *
 * <p>{@code Robot} calls {@code initialize()} and {@code stop()} on every subsystem in its list, so
 * adding yours to the list is the whole of wiring it in.
 *
 * <h2>The two ways a subsystem runs</h2>
 *
 * <p>{@link #update()} is the contract. It is one step of whatever this mechanism does, and it is
 * meant to be called once per OpMode loop:
 *
 * <pre>{@code
 * public void loop() {
 *     robot.drive.update();   // straight from loop(), no scheduler involved
 * }
 * }</pre>
 *
 * <p>{@link #periodic()} is an optional adapter that wraps the same method as an Ivy
 * {@link Command}, for OpModes that run the {@code Scheduler} — Autonomous, mostly, where commands
 * have to be sequenced and arbitrated against each other. You get it for free; you never write it.
 *
 * <p><b>Prefer calling {@code update()} directly in TeleOp.</b> Ivy's {@code Scheduler.execute()}
 * allocates roughly three objects on every call even when nothing is queued — it copies its running
 * command deque and iterates the copy — so routing a drivetrain through it costs something and buys
 * nothing, because there is no second command competing for the drivetrain. That cost is
 * microseconds rather than milliseconds and will not be your loop-time problem, but there is no
 * reason to pay it where it buys nothing. In Autonomous, arbitration is the whole point and the
 * scheduler earns it.
 *
 * <h2>{@code stop()} means teardown, and only teardown</h2>
 *
 * <p>This is the one rule the interface cannot enforce for you. {@code stop()} runs once, when the
 * OpMode is ending, and the subsystem is never used again afterwards. It is <em>not</em> "stop the
 * mechanism for a moment" — a flywheel that spins down between shots and a camera that stops
 * streaming at the end of a match are different things, and the first one needs its own name
 * ({@code idle()}, {@code coast()}, {@code spinDown()}).
 *
 * <p>That clash is real, not hypothetical: {@code FlywheelBank.stop()} in the shooter rig means
 * "cut power and keep running". The rig is not a {@code Subsystem} and does not need to be, but if
 * it ever becomes one, that method gets renamed rather than this contract bent to fit it.
 *
 * <p><b>History:</b> the first version of this interface asked for {@code update()} alone and left
 * {@code initialize()}/{@code stop()} to {@code Robot}, which named each subsystem by hand. Review
 * of PR #63 found the cost of that immediately — the add-a-subsystem steps said "add it to the
 * list", and a subsystem that was only in the list got stepped but never started or stopped. Putting
 * the lifecycle on the interface makes the list the one place to register, and settles the
 * {@code stop()} disagreement above by naming it instead of hiding it.
 *
 * <h2>If this interface gets in your way, change it</h2>
 *
 * <p>It was designed against one existing subsystem. If yours is the one that does not fit, that is
 * information about the interface, not about your subsystem — say so in your PR and change this
 * file. Do not work around it.
 *
 * @see ExampleSubsystem
 */
public interface Subsystem {

    /**
     * Get ready to run. Called once, during OpMode init, after every subsystem has been built.
     *
     * <p>Hardware lookups belong in the constructor, not here — by the time this runs, a missing
     * device should already have been noticed and recorded. This is for putting the mechanism into
     * its starting state. Empty is a fine answer.
     */
    void initialize();

    /**
     * One step of this mechanism's work. Called once per OpMode loop.
     *
     * <p>Keep it fast and keep it non-blocking — never {@code sleep()} here, and never wait for
     * anything. It runs inside the robot's control loop, so whatever time this takes is time the
     * drivetrain is not being updated. If something must happen slowly, track how far along it is
     * in a field and advance it a little on each call.
     */
    void update();

    /**
     * {@link #update()} wrapped as an Ivy {@link Command} that never finishes, for OpModes running
     * the {@code Scheduler}.
     *
     * <p>You get this for free — there is normally no reason to override it. It <em>returns</em> a
     * command rather than doing the work, so nothing happens until it is scheduled:
     *
     * <pre>{@code
     * Scheduler.schedule(vision.periodic());
     * }</pre>
     *
     * <p>{@code requiring(this)} tells the scheduler this command owns this subsystem, so it will
     * never run two commands that both need it at the same time.
     */
    default Command periodic() {
        return Commands.infinite(this::update).requiring(this);
    }

    /**
     * Shut down for good. Called once, when the OpMode ends; nothing calls this subsystem again.
     *
     * <p>Cut power, stop devices, clear anything that should not leak into the next OpMode. Must be
     * safe to call even if {@link #initialize()} never ran or the hardware is missing, and must not
     * throw — an exception here hides the one that explains why the OpMode ended.
     */
    void stop();
}
