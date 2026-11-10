package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;

import com.pedropathing.algorithm.Algorithm;
import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.api.Paths;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;

import org.junit.Test;

import java.lang.reflect.Proxy;

/**
 * Why {@link DriveSubsystem} stops Pedro's Follower on every way out of path control (the sticks,
 * {@code stop()}). A path's speed cap is written into the robot's static {@code foresightConfig}
 * while the path runs, and only a finished path or a cleared Follower puts the old value back. On
 * 6 Nov a nudge test stopped mid-path left its cap in place, and the next OpMode's new Follower
 * ran every path under it until the app restarted.
 */
public class PathCapReleaseTest {
    private final ForesightConfig config = new ForesightConfig(c -> { });

    private static Follower follower() {
        Algorithm algorithm = (Algorithm) Proxy.newProxyInstance(Algorithm.class.getClassLoader(),
                new Class<?>[] {Algorithm.class}, (proxy, method, args) -> null);
        return new Follower(null, null, algorithm);
    }

    private Path capped() {
        return Paths.line(new Pose(0, 0, 0), new Pose(6, 0, 0)).constant(0)
                .with(config.maxPathSpeed.at(0.2));
    }

    @Test
    public void aPathCutShortKeepsItsCapForTheNextFollowerUnlessStopped() {
        Follower first = follower();
        first.follow(capped());
        assertEquals("the cap is live while the path runs", 0.2, config.maxPathSpeed.get(), 0);

        // The 6 Nov failure: the OpMode ends without clearing the Follower, and the next one's
        // first path, uncapped, still runs under the old cap.
        follower().follow(Paths.line(new Pose(0, 0, 0), new Pose(48, 0, 0)).constant(0));
        assertEquals("a new Follower does not undo the old one's cap", 0.2, config.maxPathSpeed.get(), 0);

        first.stop();
        assertEquals("stopping the Follower that applied it puts it back",
                Double.POSITIVE_INFINITY, config.maxPathSpeed.get(), 0);
    }
}
