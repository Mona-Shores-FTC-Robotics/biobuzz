package org.firstinspires.ftc.teamcode.pedro.robots;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.follower.Follower;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.pedro.RobotConstants;

// Not used until the Foresight Tuner's block is pasted in, and then needed by it. Kept here so
// the paste compiles as-is instead of sending someone hunting for three imports at a meeting.
import com.pedropathing.controllers.Controller;
import com.pedropathing.math.Matrix;
import com.pedropathing.math.Vector2D;
import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * Pedro's tuned values for <b>20245</b>, used whenever the active Driver Station configuration is
 * {@code robot_20245}.
 *
 * <p><b>Only paste output from tuners run on 20245 into this file.</b> Robot19429.java has the
 * same shape and different numbers; see {@code pedro/Constants.java} for the tuner order.
 *
 * <p>Paste each tuner's generated block <b>exactly as the tuner shows it</b> over the field of the
 * same name — name lines and all, nothing to delete. Device names are shared by every robot:
 * {@link RobotConstants} sets them from {@code DeviceNames} whatever this file says, and the build
 * fails only if a pasted name <em>differs</em> from {@code DeviceNames} (a typo in the tuner's name
 * field).
 */
public final class Robot20245 {

    private Robot20245() {}

    public static MecanumConfig drivetrainConfig = new MecanumConfig(c -> {
        c.frontLeftName.set("frontLeft");
        c.frontRightName.set("frontRight");
        c.backLeftName.set("backLeft");
        c.backRightName.set("backRight");
        c.frontLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.frontRightDirection.set(DcMotorSimple.Direction.FORWARD);
        c.backLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.backRightDirection.set(DcMotorSimple.Direction.FORWARD);
    });

    /**
     * Pod type and directions from the Pinpoint Tuner on 17 Oct 2026 (#175). The offsets were
     * re-measured by ruler on 30 Oct 2026, from the centre of rotation (the middle of the
     * wheelbase): the forward pod (its wheel rolls front to back) 6.5 in right of centre, the strafe
     * pod (its wheel rolls left to right) 1.0 in forward. The tuner's strafe offset, 3.22 in forward,
     * was 2.2 in out, which swings the pose round a 2.2 in circle as the robot turns: about 4.4 in
     * off after a half turn, likely the 3-5 in seen at line-ups while Panels drew the paths spot on.
     * Each offset is measured across its pod's rolling direction: forward pod left (+) / right (-),
     * strafe pod forward (+) / back (-).
     */
    public static PinpointConfig localizerConfig = new PinpointConfig(c -> {
        c.name.set("pinpoint");
        c.podType.set(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        c.xPodOffset.set(-6.5);
        c.yPodOffset.set(1.0);
        c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);
        c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);
        c.globalDistanceUnit.set(DistanceUnit.INCH);
        c.offsetUnits.set(DistanceUnit.INCH);
    });

    /**
     * Foresight Tuner output, measured on 20245 on 17 Oct 2026 (before the turret and launcher
     * were mounted; re-run the tuner once the robot's mass settles). Its twelve required values
     * have no defaults and must not be guessed; {@code Constants.createAlgorithm()} refuses to
     * build a Foresight until a tuner's block is pasted here.
     */
    public static ForesightConfig foresightConfig = new ForesightConfig(
            c -> {
                Controller primaryTranslationalForward = Controller.proportional(0.2606582765728706);
                Controller secondaryTranslationalForward = Controller.proportional(0.09630626827625094);
                Controller primaryTranslationalLateral = Controller.proportional(0.43701809598485064);
                Controller secondaryTranslationalLateral = Controller.proportional(0.16146650912781296);

                c.forwardTranslational.set(Controller.piecewise(secondaryTranslationalForward).put(2.5, primaryTranslationalForward));
                c.strafeTranslational.set(Controller.piecewise(secondaryTranslationalLateral).put(2.5, primaryTranslationalLateral));

                c.coast.set(Controller.proportionalFeedforward(0.013094480171022358));
                c.brake.set(Controller.proportionalFeedforward(0.011130308145369004));

                c.headingFeedback.set(Controller.proportional(3.5260038257983317));
                c.headingBrakeCoefficients.set(Vector2D.cartesian(0.03710800717181879, 0.012276513664978382));

                c.linearBrakeCoefficients.set(Matrix.diag(0.028397645082802254, 0.05464755023071258));
                c.quadraticBrakeCoefficients.set(Matrix.diag(0.002939691354279288, 0.0021088917095499903));

                c.maxAchievableForwardVelocity.set(76.99298921198282);
                c.maxAchievableStrafeVelocity.set(43.29306375924765);
                c.naturalForwardDeceleration.set(39.794350624517094);
                c.naturalStrafeDeceleration.set(54.40283120163522);
            }
    );

    public static Follower create(HardwareMap h) {
        return new Follower(
                new PinpointLocalizer(h, localizerConfig),
                new Mecanum(h, drivetrainConfig),
                new Foresight(foresightConfig)
        );
    }


    /** Registered in {@code Constants.ROBOTS}. Nothing to change here when pasting. */
    public static RobotConstants constants() {
        return new RobotConstants(Robot20245.class, drivetrainConfig, localizerConfig, foresightConfig);
    }
}
