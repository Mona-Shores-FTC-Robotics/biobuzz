package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.math.Pose;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.pedro.Constants;
import org.firstinspires.ftc.teamcode.util.AccelLimiter;

/**
 * The mecanum drivetrain. Always drives: field-centric when the Pinpoint is there, robot-centric
 * when it is not.
 *
 * <p>An OpMode says what it wants with {@link #drive(double, double, double)} in {@code onLoop()};
 * {@link #update()} then does it, in the same loop. Stick mapping, deadband and speed modes belong
 * to the OpMode — they are driver preferences. How fast power may rise belongs here, because it is
 * about keeping this robot from lurching whoever is driving it.
 *
 * <h2>Fallback</h2>
 *
 * <p>The motors are required: without them this throws at construction, and the OpMode fails to
 * init with the SDK's "could not find" message. The Pinpoint is not: if it is missing, this builds
 * anyway, drives robot-centric, and {@link #localizerFault()} says why. That is the one optional
 * device here, and it is optional on purpose (#21).
 *
 * <p>This does not build a Pedro {@code Follower}. {@code Constants.createAlgorithm()} throws until
 * the Foresight Tuner has run, and this subsystem has to work on an untuned robot. When path
 * following is needed, the Follower joins here.
 */
@Configurable
public class DriveSubsystem implements Subsystem {

    /** How fast drive power may rise, in power per second. Slowing and stopping are instant. */
    public static double accelPerSec = 2.0;

    /** The same, for turning. Separate because a laggy turn makes aiming feel mushy. */
    public static double turnAccelPerSec = 4.0;

    /** A loop slower than this (a hiccup, a GC pause) is treated as this long, so power cannot jump. */
    private static final double MAX_LOOP_DT_SEC = 0.1;

    private final Mecanum drivetrain;
    private final PinpointLocalizer localizer;
    private final String localizerFault;

    private final AccelLimiter forwardLimiter = new AccelLimiter();
    private final AccelLimiter strafeLimiter = new AccelLimiter();
    private final AccelLimiter turnLimiter = new AccelLimiter();

    private double forward;
    private double strafe;
    private double turn;
    private boolean accelLimited = true;
    private boolean fieldCentric;
    private double headingOffset;
    private long lastUpdateNs;

    public DriveSubsystem(HardwareMap hardwareMap) {
        drivetrain = Constants.createDrivetrain(hardwareMap);

        PinpointLocalizer found = null;
        String fault = null;
        try {
            found = Constants.createLocalizer(hardwareMap);
        } catch (RuntimeException e) {
            fault = e.getMessage() == null ? e.toString() : e.getMessage();
        }
        localizer = found;
        localizerFault = fault;
        fieldCentric = found != null;
    }

    // ------------------------------------------------------------- commands

    /**
     * What to do this loop, in Pedro's robot frame: +forward, +strafe is left, +turn is
     * counter-clockwise, each in [-1, 1]. Holds until the next call.
     */
    public void drive(double forward, double strafe, double turn) {
        this.forward = forward;
        this.strafe = strafe;
        this.turn = turn;
    }

    /** False skips the acceleration limit, for a driver who wants speed now (turbo). */
    public void setAccelLimited(boolean limited) {
        accelLimited = limited;
    }

    /** Field-centric "forward" becomes the way the robot faces now. No-op without a Pinpoint. */
    public void resetHeading() {
        if (localizer != null) {
            headingOffset = localizer.pose().heading();
        }
    }

    /**
     * Field-centric "forward" becomes this field heading, radians. Used at PLAY when the pose is
     * field-absolute (handed over from Autonomous) and the driver's forward for the alliance is known.
     */
    public void setFieldForward(double fieldHeading) {
        headingOffset = fieldHeading;
    }

    /**
     * Put the robot at {@code pose} in Pedro field coordinates — how TeleOp continues from where
     * Autonomous left it. No-op without a Pinpoint.
     */
    public void setPose(Pose pose) {
        if (localizer != null && pose != null) {
            localizer.setPose(pose);
        }
    }

    /** Switch between field- and robot-centric. Stays robot-centric without a Pinpoint. */
    public void toggleFieldCentric() {
        fieldCentric = !fieldCentric && localizer != null;
    }

    // ---------------------------------------------------------------- state

    public boolean isFieldCentric() {
        return fieldCentric;
    }

    /** Whether a heading is available — false means the Pinpoint was missing at init. */
    public boolean hasHeading() {
        return localizer != null;
    }

    /** Heading relative to the last {@link #resetHeading()}, radians. 0 without a Pinpoint. */
    public double heading() {
        return localizer == null ? 0.0 : localizer.pose().heading() - headingOffset;
    }

    /** Field pose in Pedro coordinates, or null without a Pinpoint. */
    public Pose pose() {
        return localizer == null ? null : localizer.pose();
    }

    /** Why the Pinpoint is missing, or null when it is there. */
    public String localizerFault() {
        return localizerFault;
    }

    // ------------------------------------------------------------ lifecycle

    @Override
    public void initialize() {
        lastUpdateNs = System.nanoTime();
    }

    @Override
    public void update() {
        if (localizer != null) {
            localizer.update();
        }

        long now = System.nanoTime();
        double dt = Math.min((now - lastUpdateNs) / 1e9, MAX_LOOP_DT_SEC);
        lastUpdateNs = now;

        double f = forward;
        double s = strafe;
        double t = turn;
        if (accelLimited) {
            f = forwardLimiter.step(f, accelPerSec, dt);
            s = strafeLimiter.step(s, accelPerSec, dt);
            t = turnLimiter.step(t, turnAccelPerSec, dt);
        } else {
            // Keep the limiters in step with what the wheels really get, so leaving turbo drops
            // straight to the new command rather than ramping from wherever they were before.
            forwardLimiter.reset(f);
            strafeLimiter.reset(s);
            turnLimiter.reset(t);
        }

        DrivePowers powers = fieldCentric
                ? ManualDrive.fieldCentric(f, s, t, heading())
                : new DrivePowers(f, s, t);

        // manual = true selects BRAKE over FLOAT when the command is zero, if the drivetrain
        // config asks for it: under a driver, the robot stops where it is put.
        drivetrain.drive(powers, true);
    }

    @Override
    public void stop() {
        drivetrain.stop();
    }

    @Override
    public void describe(Display display) {
        if (localizer != null) {
            display.status("Pinpoint", Display.Level.OK, "present");
        } else {
            display.status("Pinpoint", Display.Level.WARN, "MISSING — " + localizerFault);
        }
        display.status("Mode", Display.Level.OK, fieldCentric ? "field-centric" : "robot-centric");
        if (localizer != null) {
            display.line(String.format(java.util.Locale.US, "Heading %.1f°", Math.toDegrees(heading())));
        }
    }
}
