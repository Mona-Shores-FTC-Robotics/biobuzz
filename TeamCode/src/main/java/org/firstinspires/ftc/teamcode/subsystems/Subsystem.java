package org.firstinspires.ftc.teamcode.subsystems;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.commands.Commands;

/**
 * One mechanism on the robot: a motor, a set of motors, a camera, an arm.
 *
 * <p><b>If you are writing your first subsystem, copy {@link ExampleSubsystem} and rename it.</b>
 * This interface asks you for exactly one method — {@link #update()} — and the compiler will tell
 * you if you forget it.
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
 * <h2>What this interface deliberately leaves out</h2>
 *
 * <p>There is no {@code initialize()} and no {@code stop()} here, though most subsystems have both.
 * They are left off because they genuinely mean different things across the subsystems we already
 * have, and a shared interface that forced one meaning would make working code bend to fit:
 *
 * <ul>
 *   <li>{@code FlywheelBank.stop()} cuts power but leaves the object live and still being stepped.
 *       {@code LimelightVisionSubsystem.stop()} is teardown — it stops the device, clears its
 *       sightings and resets its trackers.</li>
 *   <li>{@code LimelightVisionSubsystem} binds its hardware in its <em>constructor</em>;
 *       {@code FlywheelBank} binds in {@code initialize()}.</li>
 * </ul>
 *
 * <p>So write whichever of those your mechanism needs, with whatever meaning it needs, and let
 * {@link org.firstinspires.ftc.teamcode.Robot} call it. The interface stays honest about the one
 * thing that is actually true of every subsystem.
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
}
