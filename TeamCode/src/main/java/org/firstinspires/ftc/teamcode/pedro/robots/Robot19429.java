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
     * replace each with that tool's output on 19429 (Measure: Pod Offsets).
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
     * <b>Foresight Tuner, measured on 19429, 6 Nov 2026</b> (second run; the first ended against a
     * wall and was discarded). It ran with the pod offsets the robot had loaded that day, before
     * Measure: Pod Offsets had passed on 19429; if those offsets change by much, run it again.
     * Max strafe velocity is about 10% below 20245's (48.3 against 53.3 in/s).
     */
    public static ForesightConfig foresightConfig = new ForesightConfig(
            c -> {
                Controller primaryTranslationalForward = Controller.proportional(0.41196140301573647);
                Controller secondaryTranslationalForward = Controller.proportional(0.15220873060289228);
                Controller primaryTranslationalLateral = Controller.proportional(1.4107292622281007);
                Controller secondaryTranslationalLateral = Controller.proportional(0.5212267670131507);

                c.forwardTranslational.set(Controller.piecewise(secondaryTranslationalForward).put(2.5, primaryTranslationalForward));
                c.strafeTranslational.set(Controller.piecewise(secondaryTranslationalLateral).put(2.5, primaryTranslationalLateral));

                c.coast.set(Controller.proportionalFeedforward(0.01565104954873756));
                c.brake.set(Controller.proportionalFeedforward(0.013303392116426924));

                c.headingFeedback.set(Controller.proportional(7.637328626529433));
                c.headingBrakeCoefficients.set(Vector2D.cartesian(0.040932823966850294, 0.007926584272974768));

                c.linearBrakeCoefficients.set(Matrix.diag(0.08336980978243928, 0.048338401842859854));
                c.quadraticBrakeCoefficients.set(Matrix.diag(0.0020180228162586743, 0.001995585523220165));

                c.maxAchievableForwardVelocity.set(69.09043561746174);
                c.maxAchievableStrafeVelocity.set(48.280546705798756);
                c.naturalForwardDeceleration.set(40.93215923957978);
                c.naturalStrafeDeceleration.set(85.94607752679752);
            }
    );

    /** Registered in {@code Constants.ROBOTS}. Nothing to change here when pasting. */
    public static RobotConstants constants() {
        return new RobotConstants(Robot19429.class, drivetrainConfig, localizerConfig, foresightConfig);
    }
}
