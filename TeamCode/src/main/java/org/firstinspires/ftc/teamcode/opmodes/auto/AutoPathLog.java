package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;

import org.firstinspires.ftc.teamcode.autokit.AutoKit;
import org.firstinspires.ftc.teamcode.logging.FieldRobot;
import org.firstinspires.ftc.teamcode.logging.MatchLog;

import java.util.List;

/**
 * Writes an Auto's paths to the match log, where AdvantageScope's Field tab draws them: the whole
 * plan in grey ({@link FieldRobot#fullPath}, {@code /Path/Full}), the path being driven in yellow
 * ({@link FieldRobot#activePath}, {@code /Path/Active}, empty between paths) and where it ends as a
 * ghost ({@link FieldRobot#target}, {@code /Path/Target}).
 *
 * <p>{@link BuiltAuto} gives one to its {@link AutoKit} ({@link AutoKit#watchPaths}) and calls
 * {@link #plan} once the Auto is built. Each path is sampled once, when the Auto is built and again
 * as it starts, about {@link #SAMPLE_SPACING_IN} apart along the curve; nothing here runs per loop.
 * The samples are Pedro inches and radians; {@link MatchLog} converts them to AdvantageScope's
 * frame the way it converts {@code /Odometry/Robot}.
 *
 * <p>Keys are {@link FieldRobot#ROBOT}'s: the robot logs itself in slot 0. The partner and
 * opponent slots are the simulator's.
 */
public final class AutoPathLog implements AutoKit.PathWatcher {

    /** About how far apart the samples along a path are, in inches. */
    static final double SAMPLE_SPACING_IN = 2.0;
    /** Samples a path can have, however long: {@code /Path/Full} is every path in one value. */
    static final int MAX_SAMPLES_PER_PATH = 200;

    private static final double[] NONE = new double[0];

    private final MatchLog log;
    private final FieldRobot robot;

    public AutoPathLog(MatchLog log) {
        this(log, FieldRobot.ROBOT);
    }

    AutoPathLog(MatchLog log, FieldRobot robot) {
        this.log = log;
        this.robot = robot;
    }

    /**
     * Every path of the Auto as one trajectory, {@link FieldRobot#fullPath}. Call it once, with
     * {@link AutoKit#paths()}, as soon as the Auto is built; it writes with the next loop's frame.
     */
    public void plan(List<Path> paths) {
        double[][] sampled = new double[paths.size()][];
        int total = 0;
        for (int i = 0; i < sampled.length; i++) {
            sampled[i] = sample(paths.get(i));
            total += sampled[i].length;
        }
        double[] all = new double[total];
        int at = 0;
        for (double[] one : sampled) {
            System.arraycopy(one, 0, all, at, one.length);
            at += one.length;
        }
        log.poses(robot.fullPath, all);
    }

    @Override
    public void pathStarted(Path path) {
        log.poses(robot.activePath, sample(path));
        Pose end = path.endPose();
        log.pose(robot.target, end.x(), end.y(), end.heading());
    }

    @Override
    public void pathEnded(Path path) {
        log.poses(robot.activePath, NONE);
    }

    /**
     * {@code path} as packed {@code x, y, heading} samples in Pedro inches and radians, both ends
     * included, about {@link #SAMPLE_SPACING_IN} apart along the curve (by parameter, which Pedro's
     * curves keep close to arc length), and never more than {@link #MAX_SAMPLES_PER_PATH}.
     */
    static double[] sample(Path path) {
        int steps = (int) Math.ceil(path.curve.length() / SAMPLE_SPACING_IN);
        steps = Math.max(1, Math.min(MAX_SAMPLES_PER_PATH - 1, steps));
        double[] out = new double[3 * (steps + 1)];
        for (int i = 0; i <= steps; i++) {
            Pose p = path.get((double) i / steps);
            out[3 * i] = p.x();
            out[3 * i + 1] = p.y();
            out[3 * i + 2] = p.heading();
        }
        return out;
    }
}
