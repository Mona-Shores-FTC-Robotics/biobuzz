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
     * measured on 1 Nov 2026, after the builder moved the pods, with the Pod Offset Check: the robot
     * spins itself in place six half turns, alternating direction, so it pivots on the middle of the
     * wheelbase. Three runs gave xPodOffset -6.59, -6.51, -6.44 and yPodOffset 1.68, 1.57, 1.63;
     * these are the means. (The 30 Oct ruler measure, before the pods moved, was -6.5 and 1.0.) Each
     * offset is measured across its pod's rolling direction: forward pod left (+) / right (-),
     * strafe pod forward (+) / back (-).
     */
    public static PinpointConfig localizerConfig = new PinpointConfig(c -> {
        c.name.set("pinpoint");
        c.podType.set(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        c.xPodOffset.set(-6.51);
        c.yPodOffset.set(1.63);
        c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);
        c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);
        c.globalDistanceUnit.set(DistanceUnit.INCH);
        c.offsetUnits.set(DistanceUnit.INCH);
    });

    /**
     * Foresight Tuner output, measured on 20245 on 1 Nov 2026 with the turret and launcher mounted,
     * after the pod offsets were re-measured the same day (12.4 V battery). Replaces the 17 Oct run,
     * made before the robot's mass settled. Its twelve required values have no defaults and must not
     * be guessed; {@code Constants.createAlgorithm()} refuses to build a Foresight until a tuner's
     * block is pasted here.
     */
    public static ForesightConfig foresightConfig = new ForesightConfig(
            c -> {
                Controller primaryTranslationalForward = Controller.proportional(0.3711725482151876);
                Controller secondaryTranslationalForward = Controller.proportional(0.1371383386523627);
                Controller primaryTranslationalLateral = Controller.proportional(0.8797062454962113);
                Controller secondaryTranslationalLateral = Controller.proportional(0.32502795152705083);

                c.forwardTranslational.set(Controller.piecewise(secondaryTranslationalForward).put(2.5, primaryTranslationalForward));
                c.strafeTranslational.set(Controller.piecewise(secondaryTranslationalLateral).put(2.5, primaryTranslationalLateral));

                c.coast.set(Controller.proportionalFeedforward(0.01436619092980278));
                c.brake.set(Controller.proportionalFeedforward(0.012211262290332363));

                c.headingFeedback.set(Controller.proportional(6.81102561066061));
                c.headingBrakeCoefficients.set(Vector2D.cartesian(0.0486307653912525, 0.006041486212840524));

                c.linearBrakeCoefficients.set(Matrix.diag(0.09314850563668818, 0.044191651326303094));
                c.quadraticBrakeCoefficients.set(Matrix.diag(0.0014603186766220487, 0.002220542921721947));

                c.maxAchievableForwardVelocity.set(71.12855272032681);
                c.maxAchievableStrafeVelocity.set(53.31840381477558);
                c.naturalForwardDeceleration.set(32.234283803366466);
                c.naturalStrafeDeceleration.set(82.41560270612311);
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
