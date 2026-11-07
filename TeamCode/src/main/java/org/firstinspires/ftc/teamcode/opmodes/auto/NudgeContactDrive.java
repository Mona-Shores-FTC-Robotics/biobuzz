package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import org.firstinspires.ftc.teamcode.autokit.AutoDrive;
import java.util.function.DoubleSupplier;

/** A scoped speed cap and endpoint confirmation for the short contact approach and push only. */
public final class NudgeContactDrive implements AutoDrive {
    /**
     * The range {@code PartnerNudgeTest.pathSpeed} may take, a fraction of the robot's top path
     * speed: a typo refuses the test rather than driving into a partner at full speed, or so slowly
     * the wheels never turn.
     */
    public static final double MIN_PATH_SPEED = 0.1, MAX_PATH_SPEED = 0.5;

    /**
     * {@code path} under Pedro's {@code maxPathSpeed} cap. Not {@code maxVelocityConstraint} /
     * {@code maxAccelerationConstraint}: on 6 Nov a 4 in/s, 8 in/s² cap never moved 20245. Pedro's
     * acceleration cap targets the current speed plus one loop's worth (0.16 in/s from rest), and
     * 20245's coast feedforward has no static term, so the wheels got about 0.2 % power and the robot
     * stayed at rest for good. {@code maxPathSpeed} does not depend on the current speed.
     */
    public static Path capped(Path path, ForesightConfig config, double pathSpeed) {
        return path.with(config.maxPathSpeed.at(requirePathSpeed(pathSpeed)));
    }

    /** {@code pathSpeed}, or an exception naming the allowed range (NaN included). */
    public static double requirePathSpeed(double pathSpeed) {
        if (!(pathSpeed >= MIN_PATH_SPEED && pathSpeed <= MAX_PATH_SPEED)) {
            throw new IllegalArgumentException("Nudge path speed must be " + MIN_PATH_SPEED + " to "
                    + MAX_PATH_SPEED + " of top speed, not " + pathSpeed);
        }
        return pathSpeed;
    }

    private final AutoDrive drive;
    private final ForesightConfig config;
    private final Pose contact, pushed;
    private final DoubleSupplier speed, clock;
    private final double pathSpeed;
    private Pose target;
    private boolean limited;
    private double began;

    public NudgeContactDrive(AutoDrive drive, ForesightConfig config, Pose contact, Pose pushed,
                             DoubleSupplier speed, DoubleSupplier clock, double pathSpeed) {
        this.pathSpeed = requirePathSpeed(pathSpeed); // refused at INIT, not at contact
        this.drive = drive; this.config = config; this.contact = contact; this.pushed = pushed;
        this.speed = speed; this.clock = clock;
    }
    private static boolean near(Pose a, Pose b) {
        return Math.hypot(a.x() - b.x(), a.y() - b.y()) < .01;
    }
    @Override public void follow(Path path) {
        target = path.endPose();
        limited = near(target, contact) || near(target, pushed);
        began = clock.getAsDouble();
        drive.follow(limited ? capped(path, config, pathSpeed) : path);
    }
    @Override public boolean pathDone() {
        if (!drive.pathDone()) return false;
        if (!limited) return true;
        Pose p = drive.pose();
        if (p == null) return false;
        double error = Math.atan2(Math.sin(p.heading() - target.heading()),
                Math.cos(p.heading() - target.heading()));
        boolean arrived = Math.hypot(p.x() - target.x(), p.y() - target.y()) <= .3
                && Math.abs(error) <= Math.toRadians(3) && Math.abs(speed.getAsDouble()) <= 1;
        if (arrived) limited = false;
        return arrived;
    }
    /** A blocked contact stops the full trial; it must not continue into the scoring route. */
    public boolean contactTimedOut() { return limited && clock.getAsDouble() - began >= 6; }
    @Override public Pose pose() { return drive.pose(); }
    @Override public boolean poseReferenced() { return drive.poseReferenced(); }
    @Override public void hold(Pose pose) { drive.hold(pose); }
}
