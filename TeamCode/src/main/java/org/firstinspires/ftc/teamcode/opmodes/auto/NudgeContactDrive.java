package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import org.firstinspires.ftc.teamcode.autokit.AutoDrive;
import java.util.function.DoubleSupplier;

/** Scoped limits and endpoint confirmation for the short contact approach and push only. */
public final class NudgeContactDrive implements AutoDrive {
    private final AutoDrive drive;
    private final ForesightConfig config;
    private final Pose contact, pushed;
    private final DoubleSupplier speed, clock;
    private Pose target;
    private boolean limited;
    private double began;

    public NudgeContactDrive(AutoDrive drive, ForesightConfig config, Pose contact, Pose pushed,
                             DoubleSupplier speed, DoubleSupplier clock) {
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
        drive.follow(limited ? path.with(config.maxVelocityConstraint.at(4),
                config.maxAccelerationConstraint.at(8)) : path);
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
