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

    /** True once the path started by {@link #follow} has been driven to its end. */
    boolean pathDone();

    /** How far along the current path the robot is, 0 to 1 by distance. 1 when not following. */
    double pathProgress();

    /** The robot's pose in Pedro's field frame (inches, radians). */
    Pose pose();

    /** True when the pose was set from a known place, so field coordinates mean something. */
    boolean poseReferenced();

    /** Holds {@code pose} until the next {@link #follow} or {@link #hold}. */
    void hold(Pose pose);
}
