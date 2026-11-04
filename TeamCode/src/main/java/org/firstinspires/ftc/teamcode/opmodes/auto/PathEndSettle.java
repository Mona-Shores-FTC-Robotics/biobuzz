package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.autokit.AutoKit;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

/**
 * Whether the robot has settled after the last path: turned to within {@link #aimToleranceDeg} of
 * that path's end heading, hardly turning, and slowing below {@link #slowSpeedInPerSec}. Aimed and
 * slowing, not still: after Pedro's path end the Follower is braking into the end pose, and a
 * robot that is aimed and nearly there fires as well as one that has stopped. {@link LaunchCards}
 * feeds only once it has, unless the gate already opened on the approach
 * ({@link #readyOnApproach}), so this is the backstop: a fire step after a path waits for the robot
 * rather than for Pedro's "done".
 *
 * <h2>Why</h2>
 *
 * <p>The 25 Oct 2026 logs (20245, light chassis; #155, {@code doc/real-vs-sim-25-oct.md} on
 * {@code claude/simulator}): Pedro reported a long path done 5 to 6 in past its point, and a path
 * ending in a 180° turn done with the heading still 15 to 20° short. Both settled during the next
 * wait. A shot fired on Pedro's done would leave aimed that far off (#193, 26 Oct).
 *
 * <p>Until 4 Nov 2026 it waited for "still" (under 2 in/s): in the 3 and 4 Nov R-Solo logs that
 * cost 0.38 to 0.57 s after a path end where the robot was already within 1.2° and 0.35 in of its
 * end pose (mentor, 4 Nov 2026: aimed and slowing is enough).
 *
 * <h2>What it logs</h2>
 *
 * <p>The logs could not say which of Pedro's end conditions ended a path, or how long the robot
 * then took to settle. So at each path end it writes one event with Pedro's
 * {@code atParametricEnd()}, {@code distanceToEndpoint()}, the follower's mode, how far the robot
 * is from the path's end pose, its heading error and its speed; and a second event when the robot
 * first counts as settled after that end, with how long it took. It measures nothing itself.
 *
 * <p>Fed by {@link AutoKit}'s path watcher and by {@link #update} once per loop. Reads only what
 * the drive already keeps; allocates only when it writes an event.
 */
@Configurable
public final class PathEndSettle implements AutoKit.PathWatcher {

    /**
     * Heading within this of the last path's end heading counts as aimed, degrees. A starting point,
     * <b>not measured</b>: the robot-session-3 log shows what the heading error is at the feed.
     */
    public static double aimToleranceDeg = 3.0;

    /**
     * Slower than this, inches/s, after a path end counts as slowing enough to fire. A starting
     * point, <b>not measured</b>: the 29 Oct to 2 Nov 2026 logs had the robot under 8 in/s 0.1 to
     * 0.2 s after Pedro's path end, under 2 in/s (the old "still") 0.1 to 0.5 s after.
     */
    public static double slowSpeedInPerSec = 6.0;

    /** Turning slower than this, degrees/s, counts as no longer turning. A starting point, <b>not measured</b>. */
    public static double stillTurnDegPerSec = 5.0;

    /**
     * A {@code LaunchAll} right after a path opens the gate on the approach once the robot is within
     * this of the path's end, inches, straight line ({@link #readyOnApproach}). A starting point,
     * <b>not measured</b>: about 0.5 s out at the 10 to 12 in/s the robot carried near the end on
     * 3 and 4 Nov 2026, roughly the 0.8 to 0.9 s from gate open to first ball, so the first ball
     * leaves about as the robot stops. Zero, negative or NaN never opens it on the approach.
     */
    public static double launchWithinIn = 6.0;

    /**
     * Turning faster than this, degrees/s, on the approach keeps the gate shut even inside
     * {@link #aimToleranceDeg}: a late turn swinging through the end heading is not aimed. A starting
     * point, <b>not measured</b> (the late turn from SCORING_FLOWER_STAGING, 4 Nov 2026, swung about
     * 200°/s to the stop). NaN, zero or negative turns this check off.
     */
    public static double launchTurnDegPerSec = 30.0;

    /**
     * The most a launch card waits for the robot to settle, seconds, before it fires anyway (and the
     * log says so). Without it a robot that never quite settles would fire nothing. A starting
     * point, <b>not measured</b>: on 25 Oct the overshoots settled within the next wait.
     */
    public static double maxSettleWaitS = 1.0;

    /** What this needs from the drivetrain; {@code DriveSubsystem} has all of it. */
    public interface Motion {
        Pose pose();
        /** Speed over the ground, inches/s. */
        double speedInPerSec();
        /** Turn rate, radians/s, either sign. */
        double turnRadPerSec();
        /** Pedro's follower mode, for the log. */
        String followerMode();
        /** Pedro's {@code atParametricEnd()}, for the log. */
        boolean atParametricEnd();
        /** Pedro's {@code distanceToEndpoint()}, inches, for the log. */
        double distanceToEndpoint();
    }

    private final Motion motion;
    private final DoubleSupplier secondsNow;
    private final Consumer<String> event;

    /** The end of the last path driven; null before the first. */
    private Pose end;
    private String endLabel = "";
    /** When that path ended, seconds; NaN while a path is driving or before the first. */
    private double endedAt = Double.NaN;
    private boolean settleLogged = true;

    public PathEndSettle(Motion motion, DoubleSupplier secondsNow, Consumer<String> event) {
        this.motion = motion;
        this.secondsNow = secondsNow;
        this.event = event == null ? line -> { } : event;
    }

    /** One for the robot, on the system clock, writing to {@code event}. */
    public static PathEndSettle forRobot(Robot robot, Consumer<String> event) {
        Motion motion = new Motion() {
            @Override public Pose pose() { return robot.drive.pose(); }
            @Override public double speedInPerSec() { return robot.drive.speedInPerSec(); }
            @Override public double turnRadPerSec() { return robot.drive.turnRadPerSec(); }
            @Override public String followerMode() { return robot.drive.followerMode(); }
            @Override public boolean atParametricEnd() { return robot.drive.atParametricEnd(); }
            @Override public double distanceToEndpoint() { return robot.drive.distanceToEndpoint(); }
        };
        return new PathEndSettle(motion, () -> System.nanoTime() / 1e9, event);
    }

    @Override
    public void pathStarted(Path path) {
        end = path.endPose();
        endLabel = String.format(Locale.US, "(%.1f, %.1f, %.0f°)", end.x(), end.y(), Math.toDegrees(end.heading()));
        endedAt = Double.NaN;
        settleLogged = true;
    }

    @Override
    public void pathEnded(Path path) {
        endedAt = secondsNow.getAsDouble();
        settleLogged = false;
        Pose pose = motion.pose();
        // Telemetry never takes the Auto down: Pedro's accessors are read only for the log.
        String pedro;
        try {
            pedro = String.format(Locale.US, "mode %s, atParametricEnd %b, distanceToEndpoint %.2f in",
                    motion.followerMode(), motion.atParametricEnd(), motion.distanceToEndpoint());
        } catch (RuntimeException e) {
            pedro = "Pedro's end state unreadable: " + e.getClass().getSimpleName();
        }
        event.accept(String.format(Locale.US,
                "path end %s: Pedro done; %s; off end %.2f in, heading %+.1f°, %.1f in/s, %.0f°/s",
                endLabel, pedro, distanceOff(pose), headingErrorDeg(pose),
                motion.speedInPerSec(), Math.toDegrees(motion.turnRadPerSec())));
    }

    /** Once per loop: logs the moment the robot first settles after a path end. */
    public void update() {
        if (settleLogged || Double.isNaN(endedAt) || !settled()) return;
        settleLogged = true;
        Pose pose = motion.pose();
        event.accept(String.format(Locale.US, "settled %.2f s after path end %s: off end %.2f in, heading %+.1f°",
                secondsNow.getAsDouble() - endedAt, endLabel, distanceOff(pose), headingErrorDeg(pose)));
    }

    /**
     * Aimed at the last path's end heading, hardly turning, and slowing below
     * {@link #slowSpeedInPerSec}. Before the first path only the speed and turn count. A tolerance
     * that is NaN, zero or negative turns that check off: a typo in Panels fires sooner, never a
     * launch card that cannot fire.
     */
    public boolean settled() {
        boolean slowing = within(motion.speedInPerSec(), slowSpeedInPerSec)
                && within(Math.toDegrees(Math.abs(motion.turnRadPerSec())), stillTurnDegPerSec);
        if (!slowing) return false;
        return end == null || within(Math.abs(headingErrorDeg(motion.pose())), aimToleranceDeg);
    }

    /**
     * Whether a {@code LaunchAll} may open the gate now, on the approach to a path ending at
     * {@code pathEnd}, with the robot at {@code pose} and {@code remainingIn} from the end: within
     * {@link #launchWithinIn}, within {@link #aimToleranceDeg} of the end heading, and turning slower
     * than {@link #launchTurnDegPerSec}. {@link LaunchCards} also needs the launcher at speed. No
     * allocation: called every loop of the drive.
     */
    public boolean readyOnApproach(Pose pathEnd, Pose pose, double remainingIn) {
        if (pathEnd == null || pose == null) return false;
        if (!(launchWithinIn > 0) || !(remainingIn <= launchWithinIn)) return false;
        double error = Math.toDegrees(Math.IEEEremainder(pose.heading() - pathEnd.heading(), 2 * Math.PI));
        return within(Math.abs(error), aimToleranceDeg)
                && within(Math.toDegrees(Math.abs(motion.turnRadPerSec())), launchTurnDegPerSec);
    }

    /** The most to wait for {@link #settled}, seconds; NaN or negative as 0, so a typo never stalls. */
    static double maxWaitS() {
        return LaunchCards.nonNegative(maxSettleWaitS);
    }

    /** Signed heading error to the last path's end heading, degrees; 0 before the first path. */
    double headingErrorDeg(Pose pose) {
        if (end == null || pose == null) return 0.0;
        return Math.toDegrees(Math.IEEEremainder(pose.heading() - end.heading(), 2 * Math.PI));
    }

    private double distanceOff(Pose pose) {
        if (end == null || pose == null) return Double.NaN;
        return Math.hypot(pose.x() - end.x(), pose.y() - end.y());
    }

    /** {@code value <= tolerance}, with a tolerance that is not a positive number as "anything goes". */
    static boolean within(double value, double tolerance) {
        if (!(tolerance > 0)) return true;
        return value <= tolerance;
    }
}
