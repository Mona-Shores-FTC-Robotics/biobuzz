package org.firstinspires.ftc.teamcode.pedro.robots;

import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.pedro.RobotConstants;

// Needed by the Foresight block.
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
     * <b>Copied from Robot20245.java on 5 Nov 2026, not measured on 19429.</b> 19429 is built as a
     * replica of 20245, so its values stand in until 19429's own tuners run. Replace each block
     * with that tuner's output on 19429 (Mecanum, Pinpoint, Measure: Pod Offsets, Foresight); a
     * block still marked "copied" has not been measured on this robot.
     */
    public static MecanumConfig drivetrainConfig = new MecanumConfig(c -> {
        c.frontLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.frontRightDirection.set(DcMotorSimple.Direction.FORWARD);
        c.backLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.backRightDirection.set(DcMotorSimple.Direction.FORWARD);
    });

    /**
     * <b>Copied from Robot20245.java on 5 Nov 2026</b> (its pods: Pinpoint Tuner 17 Oct, offsets by
     * Measure: Pod Offsets 1 Nov). Valid only while 19429's pods sit where 20245's do; re-run
     * Measure: Pod Offsets on 19429 and replace the two offsets.
     */
    public static PinpointConfig localizerConfig = new PinpointConfig(c -> {
        c.podType.set(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        c.xPodOffset.set(-6.51);
        c.yPodOffset.set(1.63);
        c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);
        c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);
        c.globalDistanceUnit.set(DistanceUnit.INCH);
        c.offsetUnits.set(DistanceUnit.INCH);
    });

    /**
     * <b>Measured on 19429, 6 Nov 2026</b> (Foresight Tuner, at bring-up). The tuner drove with the
     * {@code drivetrainConfig} and {@code localizerConfig} above as they stood that day; if either is
     * re-tuned, run the Foresight Tuner again and paste over this block.
     */
    public static ForesightConfig foresightConfig = new ForesightConfig(
            c -> {
                Controller primaryTranslationalForward = Controller.proportional(0.46085881081152114);
                Controller secondaryTranslationalForward = Controller.proportional(0.17027501622063512);
                Controller primaryTranslationalLateral = Controller.proportional(1.0367085620325478);
                Controller secondaryTranslationalLateral = Controller.proportional(0.3830361123080656);

                c.forwardTranslational.set(Controller.piecewise(secondaryTranslationalForward).put(2.5, primaryTranslationalForward));
                c.strafeTranslational.set(Controller.piecewise(secondaryTranslationalLateral).put(2.5, primaryTranslationalLateral));

                c.coast.set(Controller.proportionalFeedforward(0.0167362689199765));
                c.brake.set(Controller.proportionalFeedforward(0.014225828581980024));

                c.headingFeedback.set(Controller.proportional(8.54738558487578));
                c.headingBrakeCoefficients.set(Vector2D.cartesian(0.04590798884960745, 0.007077275493233542));

                c.linearBrakeCoefficients.set(Matrix.diag(0.09215363056605187, 0.01810258976052119));
                c.quadraticBrakeCoefficients.set(Matrix.diag(0.0015818740069037233, 0.0028595937907661267));

                c.maxAchievableForwardVelocity.set(66.76302320581648);
                c.maxAchievableStrafeVelocity.set(44.45771301815713);
                c.naturalForwardDeceleration.set(37.129381325600164);
                c.naturalStrafeDeceleration.set(85.8446127901741);
            }
    );

    /** Registered in {@code Constants.ROBOTS}. Nothing to change here when pasting. */
    public static RobotConstants constants() {
        return new RobotConstants(Robot19429.class, drivetrainConfig, localizerConfig, foresightConfig);
    }
}
