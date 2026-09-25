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
 *   <li>Add it to {@link #subsystems}. That one list is how it gets initialized, stepped every
 *       loop, and stopped at the end — there is nowhere else to register it.</li>
 * </ol>
 *
 * <p>That is the whole pattern. Four steps, and no file outside {@code subsystems/} has to know
 * anything about how your mechanism works.
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
     * Bring every subsystem up, in the order they were built. Called once, from the OpMode's init.
     */
    public void initialize() {
        for (Subsystem subsystem : subsystems) {
            subsystem.initialize();
        }
    }

    /**
     * Shut every subsystem down, in the reverse of the order they were built. Called once, when the
     * OpMode ends.
     *
     * <p>Reverse order for the usual reason: something built later may depend on something built
     * earlier, so it should let go first.
     */
    public void stop() {
        for (int i = subsystems.size() - 1; i >= 0; i--) {
            subsystems.get(i).stop();
        }
    }
}
