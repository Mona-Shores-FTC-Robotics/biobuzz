package org.firstinspires.ftc.teamcode.pedro.robots;

import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.pedro.RobotConstants;

// Needed by the Foresight block; kept for the paste of 19429's own tuner output.
import com.pedropathing.controllers.Controller;
import com.pedropathing.math.Matrix;
import com.pedropathing.math.Vector2D;

/**
 * Pedro's tuned values for <b>19429</b>, used whenever the active Driver Station configuration is
 * {@code robot_19429}.
 *
 * <p><b>Only paste output from tuners run on 19429 into this file.</b> Robot20245.java has the
 * same shape and different numbers; see {@code pedro/Constants.java} for the tuner order.
 *
 * <p>Paste each tuner's generated block <b>exactly as the tuner shows it</b> over the field of the
 * same name — name lines and all, nothing to delete. Device names are shared by every robot:
 * {@link RobotConstants} sets them from {@code DeviceNames} whatever this file says, and the build
 * fails only if a pasted name <em>differs</em> from {@code DeviceNames} (a typo in the tuner's name
 * field).
 */
public final class Robot19429 {

    private Robot19429() {}

    /**
     * <b>Mecanum Tuner, measured on 19429, 6 Nov 2026.</b> The same directions as 20245.
     *
     * <p>Values below that still say "copied from Robot20245.java" have not been measured on 19429:
     * replace each with that tool's output on 19429 (Measure: Pod Offsets, Foresight).
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
     * Pod type and directions from the Pinpoint Tuner on 19429, 6 Nov 2026: the same as 20245's.
     *
     * <p><b>The offsets are still copied from Robot20245.java</b> (its Measure: Pod Offsets, 1 Nov),
     * not measured on 19429. On 6 Nov the Pinpoint Tuner's hand turn gave x -7.00, y 2.97 (its y is
     * the number a hand turn gets wrong: 2.2 in on 20245), and a first Measure: Pod Offsets run gave
     * a mean of x -4.67, y 1.72 with a spread of 1.37 / 2.52 in, too wide to use (low battery). Run
     * Measure: Pod Offsets on 19429 until its spread is under 0.25 in and paste its two offsets here.
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
     * <b>Copied from Robot20245.java on 5 Nov 2026</b> (20245's Foresight Tuner run of 1 Nov), so
     * path Autos and Square's drive to a launch spot run on 19429 before it is tuned. Run the
     * Foresight Tuner on 19429 and paste its block over this one.
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

    /** Registered in {@code Constants.ROBOTS}. Nothing to change here when pasting. */
    public static RobotConstants constants() {
        return new RobotConstants(Robot19429.class, drivetrainConfig, localizerConfig, foresightConfig);
    }
}
