package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.subsystems.Subsystem;
import org.firstinspires.ftc.teamcode.vision.LimelightVisionSubsystem;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Every mechanism on the robot, in one place. Think of it as the parts list: the subsystems get
 * built here, and the rest of the code asks {@code Robot} for the one it needs.
 *
 * <p>An OpMode that extends {@link org.firstinspires.ftc.teamcode.opmodes.RobotOpMode} gets one of
 * these built for it, as the {@code robot} field.
 *
 * <h2>To add a new mechanism</h2>
 *
 * <ol>
 *   <li>Copy {@link org.firstinspires.ftc.teamcode.subsystems.ExampleSubsystem} into the
 *       {@code subsystems/} package and fill it in.</li>
 *   <li>Add a {@code public final} field for it here.</li>
 *   <li>Build it in the constructor below.</li>
 *   <li>Add it to {@link #subsystems} so it gets stepped every loop.</li>
 *   <li>If it has start-up or shut-down work, call it from {@link #initialize()} and
 *       {@link #stop()} below. Those name each subsystem explicitly and do <i>not</i> read the list
 *       — a subsystem that is only in the list gets stepped but never started or stopped.</li>
 * </ol>
 *
 * <p>That is the whole pattern, and no file outside {@code subsystems/} has to know anything about
 * how your mechanism works.
 *
 * <h2>What is deliberately missing</h2>
 *
 * <p><b>There is no drivetrain here yet.</b> That is not an oversight — it is the first task on the
 * student ladder, and it is a move rather than an invention: the drive code already exists and works
 * inside {@code BasicDriveTeleOp}, and rung 1 is relocating it into a {@code DriveSubsystem} without
 * changing what it does. Leaving the slot empty is the point.
 *
 * <p>There is likewise no intake, launcher or lighting, because no such mechanism exists on a
 * BIOBUZZ robot yet. A subsystem for hardware nobody has designed would be a guess wearing a
 * class name.
 */
public class Robot {

    /** Tracks the HIVE CELLs. Always present; reports itself unavailable if the camera is missing. */
    public final LimelightVisionSubsystem vision;

    // TODO (rung 1): public final DriveSubsystem drive;

    /**
     * Every subsystem above, for the code that has to walk all of them. Built once rather than per
     * call, because it is read inside the control loop.
     */
    private final List<Subsystem> subsystems;

    public Robot(HardwareMap hardwareMap) {
        vision = new LimelightVisionSubsystem(hardwareMap);

        subsystems = Collections.unmodifiableList(Arrays.<Subsystem>asList(vision));
    }

    /** Every subsystem, in the order they were built. */
    public List<Subsystem> subsystems() {
        return subsystems;
    }

    /**
     * Bring every subsystem up. Called once, from the OpMode's init.
     *
     * <p>Not part of {@link Subsystem} on purpose: subsystems disagree about what starting up means
     * — this one binds its hardware in its constructor and uses {@code initialize()} only to start
     * the camera streaming — so {@code Robot} names each one explicitly rather than pretending they
     * are interchangeable.
     */
    public void initialize() {
        vision.initialize();
    }

    /**
     * Shut every subsystem down. Called once, when the OpMode ends.
     *
     * <p>Same reasoning as {@link #initialize()}: {@code stop()} means teardown for the camera but
     * means "cut power and keep running" for the flywheel rig, so it is spelled out here instead of
     * being assumed.
     */
    public void stop() {
        vision.stop();
    }
}
