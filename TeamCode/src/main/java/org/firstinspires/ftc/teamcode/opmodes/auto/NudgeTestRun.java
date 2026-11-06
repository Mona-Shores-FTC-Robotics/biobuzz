package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.ivy.Command;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;

import org.firstinspires.ftc.teamcode.autokit.AutoDrive;
import org.firstinspires.ftc.teamcode.autokit.AutoKit;
import org.firstinspires.ftc.teamcode.autokit.AutoRegistry;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.PartnerNudgeTestAuto;

import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

/** Runs only the drawn push and retreat, with real Pedro constraints and a bounded test duration. */
public final class NudgeTestRun {
    private final AutoDrive drive;
    private final ForesightConfig config;
    private final DoubleSupplier speed;
    private final DoubleSupplier clock;
    private final Runnable releaseDrive;
    private final Consumer<String> event;
    private final double velocity;
    private final double acceleration;
    private final double timeout;
    private Command command;
    private double began;
    private boolean running;
    private String result = "Ready";

    public NudgeTestRun(AutoDrive drive, ForesightConfig config, DoubleSupplier speed,
                        DoubleSupplier clock, Runnable releaseDrive, Consumer<String> event,
                        double velocity, double acceleration, double timeout) {
        // Invalid Panels input refuses the test instead of silently removing a limit.
        requireRange(velocity, 0.5, 8, "speed (in/s)");
        requireRange(acceleration, 1, 16, "acceleration (in/s^2)");
        requireRange(timeout, 1, 15, "timeout (s)");
        this.drive = drive;
        this.config = config;
        this.speed = speed;
        this.clock = clock;
        this.releaseDrive = releaseDrive;
        this.event = event;
        this.velocity = velocity;
        this.acceleration = acceleration;
        this.timeout = timeout;
    }

    private static void requireRange(double value, double min, double max, String name) {
        if (!Double.isFinite(value) || value < min || value > max) {
            throw new IllegalArgumentException("Nudge " + name + " must be " + min + " to " + max);
        }
    }

    public void start(boolean rotated) {
        if (command != null) throw new IllegalStateException("Re-INIT to repeat the nudge test");
        if (!drive.poseReferenced()) throw new IllegalStateException("Nudge needs a referenced pose");
        began = clock.getAsDouble();
        AutoDrive limited = new AutoDrive() {
            private Pose target;

            @Override public void follow(Path path) {
                target = path.endPose();
                drive.follow(path.with(config.maxVelocityConstraint.at(velocity),
                        config.maxAccelerationConstraint.at(acceleration)));
            }

            @Override public boolean pathDone() {
                // A short contact stroke must actually reach its endpoint before retreating.
                // This extra settle belongs only to this experiment, not the normal AutoKit.
                Pose actual = drive.pose();
                if (target == null || actual == null || !drive.pathDone()) return false;
                double headingError = Math.atan2(Math.sin(actual.heading() - target.heading()),
                        Math.cos(actual.heading() - target.heading()));
                return Math.hypot(actual.x() - target.x(), actual.y() - target.y()) <= 0.3
                        && Math.abs(headingError) <= Math.toRadians(3)
                        && Math.abs(speed.getAsDouble()) <= 1;
            }

            @Override public Pose pose() { return drive.pose(); }
            @Override public boolean poseReferenced() { return drive.poseReferenced(); }
            @Override public void hold(Pose pose) { drive.hold(pose); }
        };
        AutoKit kit = new AutoKit(limited, new AutoRegistry(), () -> clock.getAsDouble() - began)
                .trace(line -> event.accept("nudge: " + line));
        command = PartnerNudgeTestAuto.build(kit, rotated);
        running = true;
        result = "Running push and retreat";
        event.accept("nudge: start, speed limit " + velocity + " in/s, acceleration limit "
                + acceleration + " in/s^2, timeout " + timeout + " s");
        command.schedule();
    }

    public void update() {
        if (!running) return;
        if (!drive.poseReferenced()) stop("Stopped: pose reference lost");
        else if (!command.isScheduled()) stop("Complete: inspect partner PARK and wall clearance");
        else if (clock.getAsDouble() - began >= timeout) stop("Timed out: drive released; inspect contact");
    }

    public void stop(String why) {
        if (command != null && command.isScheduled()) command.cancel();
        releaseDrive.run(); // Leave Pedro's end hold: do not keep pressing on a blocked chassis.
        if (running) event.accept("nudge: " + why + " after " + (clock.getAsDouble() - began) + " s");
        running = false;
        result = why;
    }

    public String result() { return result; }
    public boolean running() { return running; }
}
