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
     * The offsets are Measure: Pod Offsets on 19429, 6 Nov 2026, after the back-left wheel's
     * over-tight pulley was fixed: six legs gave x -5.79, -6.23, -6.14, -6.31, -6.25, -6.34 and
     * y 2.28, 2.07, 2.00, 2.14, 2.04, ~2.13. The first leg is the outlier; legs 2-6 spread 0.20 in
     * (x) and 0.14 in (y), and these are their mean. Earlier runs, before the pulley fix, split by
     * turn direction (spread up to 2.5 in) and were discarded, as was the Pinpoint Tuner's
     * hand-turn x -7.00, y 2.97.
     */
    public static PinpointConfig localizerConfig = new PinpointConfig(c -> {
        c.name.set("pinpoint");
        c.podType.set(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        c.xPodOffset.set(-6.25);
        c.yPodOffset.set(2.08);
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
