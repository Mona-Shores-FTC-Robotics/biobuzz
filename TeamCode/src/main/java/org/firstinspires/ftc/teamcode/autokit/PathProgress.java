package org.firstinspires.ftc.teamcode.autokit;

import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;

/** For {@link AutoDrive} implementations: how far along a path the robot is. */
public final class PathProgress {

    private PathProgress() {}

    /**
     * 0 to 1 by distance along {@code path}, while following segment {@code segmentIndex} (Pedro's
     * {@code Follower.pathIndex()}). Pedro's own {@code completion()} covers only the current
     * segment, so events on a compound path need the segments already driven counted in.
     */
    public static double along(Path path, int segmentIndex, Pose robot) {
        return Geometry.progress(path, segmentIndex, robot);
    }
}
