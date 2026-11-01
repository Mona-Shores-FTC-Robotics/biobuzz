package org.firstinspires.ftc.teamcode.opmodes;

import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.math.Pose;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.Utility;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.pedro.Constants;
import org.firstinspires.ftc.teamcode.pedro.RobotConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Measures the Pinpoint pod offsets by spinning the robot in place on its own wheels: no ruler,
 * and no hand spin.
 *
 * <p>With both offsets set to zero the Pinpoint tracks the point where the two pods' lines cross.
 * When the robot turns about its centre, that point swings round a circle whose size and place
 * give the offsets. Pedro's Pinpoint Tuner uses the same idea, but asks for a 180° turn <b>by
 * hand</b>; a hand turn does not stay on the centre, which is how its strafe offset came out 2.2 in
 * wrong on 20245 (30 Oct 2026). Here the wheels turn the robot, so it pivots on the middle of the
 * wheelbase, and the measured angle is used as it is, so stopping a little past 180° costs nothing.
 *
 * <p>It does six half turns, alternating direction, and shows each result, their mean and their
 * spread. A spread under about 0.25 in means the mean can be trusted; paste it into the active
 * robot's file in {@code pedro/robots/}. Pod type and directions come from that file; only the
 * offsets are zeroed for the measurement.
 *
 * <p>Put the robot on the floor (the tiles, not a cart) with a foot of clear space round it.
 * Nothing moves before PLAY. A standalone diagnostic, like {@code DriveMotorCheck}: it owns its
 * own loop and may wait.
 */
@Utility(name = "Pod Offset Check", description = "Spins in place on the floor and measures the Pinpoint pod offsets; no ruler")
public class PodOffsetCheck extends LinearOpMode {

    /** Turn power for the spins. Slow enough not to slip, fast enough to finish. */
    static final double TURN_POWER = 0.3;
    /** Stop commanding the turn this far short of a half turn; the robot coasts the rest. */
    static final double STOP_SHORT_RAD = Math.toRadians(8);
    /** A half turn counts only if the measured angle lands within this of 180°. */
    static final double ACCEPT_RAD = Math.toRadians(25);
    static final int LEGS = 6;
    static final double SETTLE_SEC = 0.6;
    static final double LEG_TIMEOUT_SEC = 6.0;

    @Override
    public void runOpMode() throws InterruptedException {
        RobotConstants robot = Constants.forActiveRobot();
        PinpointConfig file = robot.localizerConfig;
        PinpointConfig zeroed = new PinpointConfig(c -> {
            c.name.set(file.name.get());
            c.podType.set(file.podType.get());
            c.xPodDirection.set(file.xPodDirection.get());
            c.yPodDirection.set(file.yPodDirection.get());
            c.xPodOffset.set(0.0);
            c.yPodOffset.set(0.0);
            c.globalDistanceUnit.set(DistanceUnit.INCH);
            c.offsetUnits.set(DistanceUnit.INCH);
        });
        PinpointLocalizer localizer = new PinpointLocalizer(hardwareMap, zeroed);
        Mecanum drivetrain = Constants.createDrivetrain(hardwareMap);

        String now = String.format(Locale.US, "now in %s: xPodOffset %.2f, yPodOffset %.2f",
                robot.fileName(), file.xPodOffset.get(), file.yPodOffset.get());
        telemetry.addLine("Robot on the floor, a foot clear all round. Press PLAY: six half turns, about 30 s.");
        telemetry.addLine(now);
        telemetry.update();
        waitForStart();

        List<double[]> results = new ArrayList<>();
        for (int leg = 0; leg < LEGS && opModeIsActive(); leg++) {
            double direction = leg % 2 == 0 ? 1.0 : -1.0;
            localizer.setPose(Pose.zero());
            localizer.update();
            double turned = 0.0;
            double last = localizer.pose().heading();
            long start = System.nanoTime();
            while (opModeIsActive() && Math.abs(turned) < Math.PI - STOP_SHORT_RAD
                    && (System.nanoTime() - start) / 1e9 < LEG_TIMEOUT_SEC) {
                drivetrain.drive(new DrivePowers(0.0, 0.0, direction * TURN_POWER), true);
                localizer.update();
                double h = localizer.pose().heading();
                turned += wrap(h - last);
                last = h;
            }
            drivetrain.drive(new DrivePowers(0.0, 0.0, 0.0), true);
            long stopAt = System.nanoTime();
            while (opModeIsActive() && (System.nanoTime() - stopAt) / 1e9 < SETTLE_SEC) {
                localizer.update();
                double h = localizer.pose().heading();
                turned += wrap(h - last);
                last = h;
                sleep(10);
            }
            Pose p = localizer.pose();
            String line;
            if (Math.abs(Math.abs(turned) - Math.PI) > ACCEPT_RAD) {
                line = String.format(Locale.US, "leg %d: turned %.0f°, not a half turn: skipped", leg + 1, Math.toDegrees(turned));
            } else {
                double[] o = offsetsFrom(p.x(), p.y(), turned);
                results.add(o);
                line = String.format(Locale.US, "leg %d: turned %.0f° → xPodOffset %.2f, yPodOffset %.2f",
                        leg + 1, Math.toDegrees(turned), o[0], o[1]);
            }
            telemetry.addLine(line);
            telemetry.update();
        }
        drivetrain.drive(new DrivePowers(0.0, 0.0, 0.0), true);

        double[] mean = mean(results);
        double[] spread = spread(results);
        while (opModeIsActive()) {
            telemetry.addLine(now);
            if (results.isEmpty()) {
                telemetry.addLine("No half turn counted. Is the robot on the floor, and does Basic Drive turn it?");
            } else {
                telemetry.addLine(String.format(Locale.US, "MEAN of %d: xPodOffset %.2f, yPodOffset %.2f",
                        results.size(), mean[0], mean[1]));
                telemetry.addLine(String.format(Locale.US, "spread (max − min): x %.2f in, y %.2f in — %s",
                        spread[0], spread[1], spread[0] < 0.25 && spread[1] < 0.25
                                ? "good, use the mean" : "too wide: run it again, check for wheel slip"));
                telemetry.addLine(String.format(Locale.US, "Paste: c.xPodOffset.set(%.2f); c.yPodOffset.set(%.2f);",
                        mean[0], mean[1]));
            }
            telemetry.update();
            sleep(200);
        }
    }

    /**
     * The pod offsets from where the zero-offset tracking point ended up after the robot turned
     * {@code theta} about its centre, starting at pose zero. That point sits at {@code o} from the
     * centre (robot frame, x forward, y left), so it moves by {@code (R(theta) − I) o}; this
     * inverts that. Returns {@code {xPodOffset, yPodOffset}} as Pedro's {@code PinpointConfig}
     * means them: the forward pod's distance left of centre, and the strafe pod's distance forward.
     * At exactly 180° it is the Pinpoint Tuner's {@code (−dy/2, −dx/2)}. Pure, so a test can check it.
     */
    static double[] offsetsFrom(double dx, double dy, double theta) {
        double a = Math.cos(theta) - 1.0;
        double b = Math.sin(theta);
        double det = a * a + b * b;
        double forward = (a * dx + b * dy) / det;
        double left = (-b * dx + a * dy) / det;
        return new double[] {left, forward};
    }

    static double wrap(double angle) {
        while (angle > Math.PI) angle -= 2 * Math.PI;
        while (angle < -Math.PI) angle += 2 * Math.PI;
        return angle;
    }

    static double[] mean(List<double[]> rows) {
        double[] m = new double[2];
        for (double[] r : rows) {
            m[0] += r[0] / rows.size();
            m[1] += r[1] / rows.size();
        }
        return m;
    }

    static double[] spread(List<double[]> rows) {
        double[] lo = {Double.MAX_VALUE, Double.MAX_VALUE};
        double[] hi = {-Double.MAX_VALUE, -Double.MAX_VALUE};
        for (double[] r : rows) {
            for (int i = 0; i < 2; i++) {
                lo[i] = Math.min(lo[i], r[i]);
                hi[i] = Math.max(hi[i], r[i]);
            }
        }
        return rows.isEmpty() ? new double[] {0, 0} : new double[] {hi[0] - lo[0], hi[1] - lo[1]};
    }
}
