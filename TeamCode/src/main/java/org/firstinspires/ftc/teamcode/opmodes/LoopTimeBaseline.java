package org.firstinspires.ftc.teamcode.opmodes;

import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.localization.Localizer;
import com.pedropathing.math.Pose;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.pedro.Constants;
import org.firstinspires.ftc.teamcode.util.FieldView;
import org.firstinspires.ftc.teamcode.util.LoopTimer;
import org.firstinspires.ftc.teamcode.util.WelfordVariance;

/**
 * The reference TeleOp: drive the robot, watch the loop time, watch the pose on the field.
 *
 * <p>This is a measuring instrument, not a game OpMode. Its job is to answer "what does a healthy
 * loop cost on this robot today" so that when a subsystem lands next month and the loop drops to
 * 40 Hz, there is a number to compare against instead of an argument. Run it at the start of a
 * build session, note the numbers, and run it again after.
 *
 * <p>It does the four things every loop of every real OpMode will do — read the gamepads, read
 * odometry, write motor powers, publish telemetry — and times each of them separately, so a
 * regression points at a culprit rather than just at "the loop".
 *
 * <h2>Why it does not use the follower</h2>
 *
 * <p>{@link Constants#create} builds a {@code Follower}, which needs the Foresight algorithm, which
 * {@link Constants#createAlgorithm()} deliberately refuses to build until the Foresight Tuner has
 * run — twelve of its variables are {@code required} with no defaults. So a TeleOp built on the
 * follower cannot init on an untuned robot, which is every robot at the start of a season.
 *
 * <p>This one takes the drivetrain and the localizer directly and drives them open-loop. That is
 * not a workaround; it is the right shape for a baseline. It means this OpMode runs on day one,
 * before any tuning at all, and it is the tool you want <i>while</i> tuning — the field view shows
 * whether the Pinpoint offsets you just pasted in actually track when you push the robot around.
 *
 * <h2>Reading the result</h2>
 *
 * <p>The Driver Station and Panels get the same lines. {@code loop_hz} and {@code loop_mean_ms} are
 * the headline; {@code loop_max_ms} and {@code loop_slow_pct} are what catch the stalls a mean
 * hides. The {@code cost_*_ms} lines say where the time goes.
 *
 * <p>Graph the {@code loop_*} keys in Panels rather than reading them as text — a stall is obvious
 * as a spike and nearly invisible as a number that flickers once.
 *
 * <p>{@code field_frames_sent} is the one to check first if the field view looks dead, and the
 * white cross at field centre is the second: between them they say whether the drawing path works,
 * which is otherwise indistinguishable from a pose stuck at the origin.
 *
 * <h2>Controls</h2>
 *
 * <ul>
 *   <li>Left stick — forward and strafe. Right stick X — turn. Robot-centric, deliberately: this
 *       OpMode should still be honest when the heading is wrong, which is exactly when you are
 *       using it.
 *   <li><b>Options</b> — zero the pose at the robot's current position.
 *   <li><b>Back</b> — reset the loop statistics.
 * </ul>
 */
@TeleOp(name = "Loop Time Baseline", group = "Diagnostics")
public class LoopTimeBaseline extends LinearOpMode {

    /**
     * Drive power is scaled by this so the baseline is measured at a speed you can control.
     *
     * <p>0.4 is the number issue #52 sets for first power-on of new drive code on a robot heavy
     * enough to hurt someone, and this OpMode will often be the first thing that moves a new
     * drivetrain. Same rule applies: mentor present, wheels off the ground for the first run.
     */
    private static final double DRIVE_SCALE = 0.4;

    private final LoopTimer loop = new LoopTimer();
    private final WelfordVariance odometryCostMs = new WelfordVariance();
    private final WelfordVariance driveCostMs = new WelfordVariance();
    private final WelfordVariance fieldCostMs = new WelfordVariance();
    private final WelfordVariance telemetryCostMs = new WelfordVariance();

    /**
     * Frames actually pushed to the field view.
     *
     * <p>Published as {@code field_frames_sent}, and it is the first thing to look at when the
     * field view looks dead. Climbing means the drawing path works and the problem is the pose or
     * the browser; stuck at zero means it is the drawing path.
     */
    private int fieldFrames = 0;

    @Override
    public void runOpMode() {
        TelemetryManager panels = PanelsTelemetry.INSTANCE.getTelemetry();
        FieldView fieldView = FieldView.pedroCoordinates();

        Mecanum drivetrain = tryCreateDrivetrain();
        Localizer localizer = tryCreateLocalizer();

        telemetry.addLine("Loop Time Baseline");
        telemetry.addData("Drivetrain", drivetrain == null ? "UNAVAILABLE" : "ready");
        telemetry.addData("Localizer", localizer == null ? "UNAVAILABLE" : "ready");
        if (drivetrain == null || localizer == null) {
            telemetry.addLine();
            telemetry.addLine("Missing hardware is not fatal here - the loop is still measurable.");
            telemetry.addLine("Run Validate Hardware to find out what is not configured.");
        }
        telemetry.update();

        waitForStart();

        // Init did a lot of one-off work. Anything measured before now would sit in the max for
        // the rest of the run and make a healthy loop look like it stalls.
        loop.reset();

        boolean lastOptions = false;
        boolean lastBack = false;

        while (opModeIsActive()) {
            loop.lap();

            long t0 = System.nanoTime();
            Pose pose = Pose.zero();
            if (localizer != null) {
                localizer.update();
                pose = localizer.pose();
            }
            long t1 = System.nanoTime();

            if (drivetrain != null) {
                DrivePowers powers = new DrivePowers(
                        -gamepad1.left_stick_y * DRIVE_SCALE,
                        -gamepad1.left_stick_x * DRIVE_SCALE,
                        -gamepad1.right_stick_x * DRIVE_SCALE);
                drivetrain.drive(powers, true);
            }
            long t2 = System.nanoTime();

            // Skipped on roughly nineteen loops in twenty; see FieldView for why that is
            // deliberate rather than a sampling shortcut.
            if (fieldView.shouldDraw()) {
                fieldView.drawCentreReference();
                fieldView.drawRobot(pose.x(), pose.y(), pose.heading());
                fieldView.send();
                fieldFrames++;
            }
            long t3 = System.nanoTime();

            if (localizer != null && gamepad1.options && !lastOptions) {
                localizer.setPose(Pose.zero());
            }
            lastOptions = gamepad1.options;

            if (gamepad1.back && !lastBack) {
                loop.reset();
                fieldFrames = 0;
                odometryCostMs.reset();
                driveCostMs.reset();
                fieldCostMs.reset();
                telemetryCostMs.reset();
            }
            lastBack = gamepad1.back;

            publish(panels, pose, localizer != null);
            long t4 = System.nanoTime();

            odometryCostMs.update(millisBetween(t0, t1));
            driveCostMs.update(millisBetween(t1, t2));
            fieldCostMs.update(millisBetween(t2, t3));
            telemetryCostMs.update(millisBetween(t3, t4));
        }

        if (drivetrain != null) {
            drivetrain.stop();
        }
    }

    /**
     * Publishes to Panels and the Driver Station in one call.
     *
     * <p>{@code TelemetryManager.update(Telemetry)} mirrors the same lines to both, which is why
     * there is no second set of {@code telemetry.addData} calls here. Panels graphs the numeric
     * keys; the Driver Station is the fallback for when nobody has a laptop on the field.
     */
    private void publish(TelemetryManager panels, Pose pose, boolean localized) {
        panels.addData("loop_hz", loop.hz());
        panels.addData("loop_mean_ms", loop.meanMs());
        panels.addData("loop_last_ms", loop.lastMs());
        panels.addData("loop_sd_ms", loop.stdDevMs());
        panels.addData("loop_max_ms", loop.maxMs());
        panels.addData("loop_slow_pct", loop.slowFraction() * 100.0);
        panels.addData("loop_count", loop.count());

        panels.addData("cost_odometry_ms", odometryCostMs.mean());
        panels.addData("cost_drive_ms", driveCostMs.mean());
        panels.addData("cost_field_ms", fieldCostMs.mean());
        panels.addData("cost_telemetry_ms", telemetryCostMs.mean());
        panels.addData("field_frames_sent", fieldFrames);

        if (localized) {
            panels.addData("pose_x_in", pose.x());
            panels.addData("pose_y_in", pose.y());
            panels.addData("pose_heading_deg", Math.toDegrees(pose.heading()));
        }

        panels.debug(loop.summary());
        panels.debug(localized ? "pose: live" : "pose: NO LOCALIZER");
        panels.update(telemetry);
    }

    private static double millisBetween(long startNanos, long endNanos) {
        return (endNanos - startNanos) / 1_000_000.0;
    }

    /**
     * Neither of these stops the OpMode from running.
     *
     * <p>A diagnostic that refuses to start because one device is unplugged is a diagnostic you
     * cannot use on the day you most need it. The loop is still worth measuring on a robot with no
     * odometry pod fitted, so a missing device degrades this OpMode rather than ending it.
     * {@code ValidateHardware} is the OpMode whose job is to name what is missing.
     */
    private Mecanum tryCreateDrivetrain() {
        HardwareMap map = hardwareMap;
        try {
            return Constants.createDrivetrain(map);
        } catch (RuntimeException e) {
            telemetry.addData("Drivetrain error", String.valueOf(e.getMessage()));
            return null;
        }
    }

    private Localizer tryCreateLocalizer() {
        try {
            return Constants.createLocalizer(hardwareMap);
        } catch (RuntimeException e) {
            telemetry.addData("Localizer error", String.valueOf(e.getMessage()));
            return null;
        }
    }
}
