package org.firstinspires.ftc.teamcode.pedro.robots;

import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.pedro.RobotConstants;

// Needed by the Foresight Tuner's block below.
import com.pedropathing.controllers.Controller;
import com.pedropathing.math.Matrix;
import com.pedropathing.math.Vector2D;

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

    /**
     * Mecanum Tuner output, run on <b>20245</b>'s new chassis on 17 Oct 2026, pasted verbatim from
     * the tuner's screen (issue #175). The same directions the file already held as a placeholder.
     */
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
     * Pinpoint Tuner output, measured on <b>20245</b>'s new chassis on 17 Oct 2026 (battery 12.83 V),
     * pasted verbatim from issue #175.
     */
    public static PinpointConfig localizerConfig = new PinpointConfig(c -> {
        c.name.set("pinpoint");
        c.podType.set(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        c.xPodOffset.set(-6.051974559393455);
        c.yPodOffset.set(3.2221840685746805);
        c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);
        c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);
        c.globalDistanceUnit.set(DistanceUnit.INCH);
        c.offsetUnits.set(DistanceUnit.INCH);
    });

    /**
     * Foresight Tuner output, run on <b>20245</b>'s new chassis on 17 Oct 2026, pasted verbatim from
     * issue #175.
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

    /** Registered in {@code Constants.ROBOTS}. Nothing to change here when pasting. */
    public static RobotConstants constants() {
        return new RobotConstants(Robot20245.class, drivetrainConfig, localizerConfig, foresightConfig);
    }
}
