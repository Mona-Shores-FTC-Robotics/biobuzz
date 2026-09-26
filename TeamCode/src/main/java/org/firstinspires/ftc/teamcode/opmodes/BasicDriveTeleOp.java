package org.firstinspires.ftc.teamcode.opmodes;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.pedro.Constants;
import org.firstinspires.ftc.teamcode.util.AccelLimiter;

import java.util.List;

/**
 * Drive the robot with a gamepad. The smallest thing that is actually useful.
 *
 * <p>This deliberately does <b>not</b> build a Pedro {@code Follower}. {@code Constants.create()}
 * throws until the Foresight tuner has run, because twelve of its variables are {@code required}
 * with no defaults — so a Follower-based TeleOp cannot run on an untuned robot, which is every
 * robot we currently have. {@code createDrivetrain()} and {@code createLocalizer()} are separately
 * available and do not throw, so this uses those directly. When tuning lands, moving to a Follower
 * is a small change.
 *
 * <p>It also does not port DECODE's TeleOp. That one is 333 lines resting on a 1,713-line
 * DriveSubsystem, a Robot container, five subsystems and an Ivy scheduler. None of that exists
 * here yet, and none of it is needed to drive.
 *
 * <h2>Controls</h2>
 * <ul>
 *   <li>Left stick — translate</li>
 *   <li>Right stick X — turn</li>
 *   <li>Left bumper (hold) — slow mode. Wins over turbo if both are held.</li>
 *   <li>Right bumper (hold) — turbo: full power, no acceleration limit</li>
 *   <li>Y — reset field-centric forward to the way the robot is facing now</li>
 *   <li>B — toggle field-centric / robot-centric</li>
 * </ul>
 *
 * <p>These bindings are a starting point, not a decision. Which button does what is a driver
 * question, and the drivers should own it.
 *
 * <h2>Speed and acceleration</h2>
 *
 * <p>The test robot is light, and full stick straight to full power made it lurch — hard to coach
 * a driver on. So normal driving is capped at {@link #normalSpeed}, power can only <em>rise</em>
 * at {@link #accelPerSec} (slowing and stopping stay instant, see {@link AccelLimiter}), and turbo
 * gives back everything. All of these are {@code @Configurable}: tune them in Panels while someone
 * drives, then write the values the drivers like back here. Panels edits are lost on restart.
 *
 * <p>Turbo skips the acceleration limit on purpose — a driver pressing it wants speed now. If the
 * robot tips or slips on turbo starts, apply the limiter to turbo too.
 */
@TeleOp(name = "Basic Drive", group = "Drive")
@Configurable
public class BasicDriveTeleOp extends OpMode {

    /**
     * Stick-to-robot sign conventions.
     *
     * <p><b>Expect at least one of these to be wrong the first time you run this.</b> Nobody can
     * tell from a desk which way a robot's motors are wired or which way its odometry pods count.
     * If the robot strafes the wrong way, flip {@code STRAFE_SIGN}; if it turns the wrong way, flip
     * {@code TURN_SIGN}. That is a thirty-second fix on the robot, and it is the expected first
     * result — not a bug report.
     */
    private static final double FORWARD_SIGN = -1.0;  // pushing the stick away from you is +forward
    private static final double STRAFE_SIGN  = -1.0;  // Pedro's +strafe is left; stick +x is right
    private static final double TURN_SIGN    = -1.0;  // Pedro's +turn is counter-clockwise

    /** Stick multiplier for everyday driving. 1.0 would be full power. */
    public static double normalSpeed = 0.6;

    /** Stick multiplier while the right bumper (turbo) is held. */
    public static double turboSpeed = 1.0;

    /** Stick multiplier while the left bumper (slow) is held. */
    public static double slowSpeed = 0.35;

    /**
     * How fast drive power may rise, in power per second. 2.0 takes a standing robot to the 0.6
     * normal cap in 0.3s. Lower is gentler; very high is the same as no limit.
     */
    public static double accelPerSec = 2.0;

    /** The same, for turning. Separate because a laggy turn makes aiming feel mushy. */
    public static double turnAccelPerSec = 4.0;

    /** A loop slower than this (a hiccup, a GC pause) is treated as this long, so power cannot jump. */
    private static final double MAX_LOOP_DT_SEC = 0.1;

    /** Below this, a stick is treated as centred. Guards against drift on a worn gamepad. */
    private static final double STICK_DEADBAND = 0.05;

    private Mecanum drivetrain;
    private PinpointLocalizer localizer;

    /** Non-null only when the Pinpoint was missing at init; holds the reason, to show the driver. */
    private String localizerFault;

    private boolean fieldCentric = true;
    private double headingOffset;

    private boolean prevY;
    private boolean prevB;

    private List<LynxModule> hubs;

    private final AccelLimiter forwardLimiter = new AccelLimiter();
    private final AccelLimiter strafeLimiter = new AccelLimiter();
    private final AccelLimiter turnLimiter = new AccelLimiter();
    private long lastLoopNs;

    @Override
    public void init() {
        // One hub read per loop instead of one per call. Costs nothing, and the loop time it saves
        // is the difference between a robot that feels responsive and one that does not.
        hubs = hardwareMap.getAll(LynxModule.class);
        for (LynxModule hub : hubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
        }

        drivetrain = Constants.createDrivetrain(hardwareMap);

        // A missing Pinpoint must not stop the robot — see #21. Field-centric needs a heading, so
        // without it we fall back to robot-centric and say so, rather than failing to start. This
        // is what lets the OpMode run on a partial machine.
        try {
            localizer = Constants.createLocalizer(hardwareMap);
        } catch (RuntimeException e) {
            localizer = null;
            localizerFault = e.getMessage() == null ? e.toString() : e.getMessage();
            fieldCentric = false;
        }

        telemetry.addLine("Basic Drive ready.");
        telemetry.addLine("Left stick drives, right stick turns.");
        telemetry.addLine("Hold left bumper for slow, right bumper for turbo.");
        if (localizer == null) {
            telemetry.addLine();
            telemetry.addData("NO PINPOINT", "robot-centric only — %s", localizerFault);
        }
    }

    @Override
    public void start() {
        lastLoopNs = System.nanoTime();
    }

    @Override
    public void loop() {
        for (LynxModule hub : hubs) {
            hub.clearBulkCache();
        }
        if (localizer != null) {
            localizer.update();
        }

        handleButtons();

        long now = System.nanoTime();
        double dt = Math.min((now - lastLoopNs) / 1e9, MAX_LOOP_DT_SEC);
        lastLoopNs = now;

        boolean turbo = gamepad1.right_bumper && !gamepad1.left_bumper;
        double scale = unitRange(gamepad1.left_bumper ? slowSpeed : turbo ? turboSpeed : normalSpeed);
        double forward = deadband(gamepad1.left_stick_y) * FORWARD_SIGN * scale;
        double strafe = deadband(gamepad1.left_stick_x) * STRAFE_SIGN * scale;
        double turn = deadband(gamepad1.right_stick_x) * TURN_SIGN * scale;

        if (turbo) {
            // Bypass the limit, and keep the limiters in step with what the wheels really get:
            // releasing turbo then drops straight to the normal cap, rather than the limiter
            // starting from wherever it was before turbo was pressed.
            forwardLimiter.reset(forward);
            strafeLimiter.reset(strafe);
            turnLimiter.reset(turn);
        } else {
            forward = forwardLimiter.step(forward, accelPerSec, dt);
            strafe = strafeLimiter.step(strafe, accelPerSec, dt);
            turn = turnLimiter.step(turn, turnAccelPerSec, dt);
        }

        DrivePowers powers = usingFieldCentric()
                ? ManualDrive.fieldCentric(forward, strafe, turn, heading() - headingOffset)
                : new DrivePowers(forward, strafe, turn);

        // manual = true selects BRAKE over FLOAT when the sticks are centred, if the drivetrain
        // config asks for it. It is what you want under a driver: the robot stops where it is
        // put instead of coasting.
        drivetrain.drive(powers, true);

        publishTelemetry(forward, strafe, turn);
    }

    @Override
    public void stop() {
        if (drivetrain != null) {
            drivetrain.stop();
        }
    }

    /** Edge-detected, so holding a button does not retrigger it every 20ms. */
    private void handleButtons() {
        if (gamepad1.y && !prevY && localizer != null) {
            headingOffset = heading();
        }
        prevY = gamepad1.y;

        if (gamepad1.b && !prevB && localizer != null) {
            fieldCentric = !fieldCentric;
        }
        prevB = gamepad1.b;
    }

    private boolean usingFieldCentric() {
        return fieldCentric && localizer != null;
    }

    private double heading() {
        return localizer == null ? 0.0 : localizer.pose().heading();
    }

    /**
     * Clamps a Panels-editable speed to [0, 1], and treats NaN as 0. A typo in Panels should give
     * a robot that is too slow, never one commanded past full power or sent NaN.
     */
    private static double unitRange(double value) {
        return value > 0.0 ? Math.min(value, 1.0) : 0.0;
    }

    private static double deadband(double value) {
        return Math.abs(value) < STICK_DEADBAND ? 0.0 : value;
    }

    private void publishTelemetry(double forward, double strafe, double turn) {
        telemetry.addData("Mode", usingFieldCentric() ? "FIELD-CENTRIC (B to switch)"
                                                      : "ROBOT-CENTRIC (B to switch)");
        telemetry.addData("Speed", gamepad1.left_bumper ? "SLOW"
                                   : gamepad1.right_bumper ? "TURBO" : "normal");
        telemetry.addData("Stick", "fwd %.2f  strafe %.2f  turn %.2f", forward, strafe, turn);
        if (localizer != null) {
            telemetry.addData("Heading", "%.1f deg  (Y zeroes it)",
                    Math.toDegrees(heading() - headingOffset));
        } else {
            telemetry.addData("NO PINPOINT", localizerFault);
        }
    }
}
