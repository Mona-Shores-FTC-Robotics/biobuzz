package org.firstinspires.ftc.teamcode.autokit;

import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;

/**
 * What an Auto needs from the drivetrain. {@code DriveSubsystem} implements it on the robot; tests
 * implement it with a fake, so the whole card tree can be checked on a laptop.
 */
public interface AutoDrive {

    /** Starts following {@code path}, replacing whatever the drivetrain was doing. */
    void follow(Path path);

    /**
     * True once the path started by {@link #follow} has been driven to its end. On the robot that is
     * Pedro's parametric end, where the Follower switches to holding the end pose and brakes: the
     * robot is still rolling (10 to 24 in/s in the 29 Oct to 2 Nov 2026 match logs, under 8 in/s
     * 0.1 to 0.2 s later, under 2 in/s 0.1 to 0.5 s later). A card that needs the robot still waits
     * for that itself ({@code PathEndSettle} for a launch, {@code GrabRoutine}'s hand-over speed
     * for a grab); the kit never does.
     */
    boolean pathDone();

    /** The robot's pose in Pedro's field frame (inches, radians). */
    Pose pose();

    /** True when the pose was set from a known place, so field coordinates mean something. */
    boolean poseReferenced();

    /** Holds {@code pose} until the next {@link #follow} or {@link #hold}. */
    void hold(Pose pose);
}
