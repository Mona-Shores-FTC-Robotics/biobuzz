package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.subsystems.DriveSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.Subsystem;
import org.firstinspires.ftc.teamcode.vision.HiveSubsystem;
import org.firstinspires.ftc.teamcode.vision.LimelightVisionSubsystem;
import org.firstinspires.ftc.teamcode.vision.PieceVisionSubsystem;

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
 * <h2>Fallback</h2>
 *
 * <p>If a mechanism misbehaves, comment out its line in the list and its construction, and Sloth
 * Load. The robot runs without it. {@link #drive} is the exception: it is never removed.
 *
 * <p>There is no intake, launcher or turret here, because none is designed yet. A subsystem for
 * hardware nobody has designed would be a guess wearing a class name.
 */
public class Robot {

    /** The drivetrain. Always present; drives robot-centric if the Pinpoint is missing. */
    public final DriveSubsystem drive;

    /** Tracks the HIVE CELLs. Always present; reports itself unavailable if the camera is missing. */
    public final LimelightVisionSubsystem vision;

    /** Both HIVEs over the match: which way each is, and whether a TIP has started. */
    public final HiveSubsystem hive;

    /** Game pieces by colour, from the webcam. Optional; off unless enabled while intaking. */
    public final PieceVisionSubsystem pieces;

    /**
     * Every subsystem above, for the code that has to walk all of them. Built once rather than per
     * call, because it is read inside the control loop.
     */
    private final List<Subsystem> subsystems;

    public Robot(HardwareMap hardwareMap) {
        // Vision first: the drivetrain reads its sightings to correct the pose, and list order is
        // update order, so this loop's sightings reach this loop's pose.
        vision = new LimelightVisionSubsystem(hardwareMap);
        drive = new DriveSubsystem(hardwareMap, vision);
        // After the drive, so it compares this loop's camera with this loop's pose.
        hive = new HiveSubsystem(vision, drive);
        pieces = new PieceVisionSubsystem(hardwareMap);

        subsystems = Collections.unmodifiableList(Arrays.<Subsystem>asList(vision, drive, hive, pieces));
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
