package org.firstinspires.ftc.teamcode.pedro.robots;

import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.pedro.RobotConstants;

// Not used until the Foresight Tuner's block is pasted in, and then needed by it. Kept here so
// the paste compiles as-is instead of sending someone hunting for three imports at a meeting.
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
     * Mecanum Tuner output. <b>Placeholder:</b> the conventional left-reversed guess for
     * mirror-mounted motors, not yet measured on this robot.
     */
    public static MecanumConfig drivetrainConfig = new MecanumConfig(c -> {
        c.frontLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.frontRightDirection.set(DcMotorSimple.Direction.FORWARD);
        c.backLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.backRightDirection.set(DcMotorSimple.Direction.FORWARD);
    });

    /**
     * Pinpoint Tuner output. <b>Stale: measured on the prototype chassis, which is being
     * replaced.</b> Pod offsets say where the Pinpoint sat on one frame, so on the new chassis these
     * are wrong by construction. They are kept only so the code compiles and the field view has
     * something to show. Run the Pinpoint Tuner on this robot and paste over them before trusting
     * any pose (#175, the bring-up checklist under #78).
     */
    public static PinpointConfig localizerConfig = new PinpointConfig(c -> {
        c.podType.set(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        c.xPodOffset.set(-4.412711736724133);
        c.yPodOffset.set(0.01945832934905225);
        c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);
        c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);
        c.globalDistanceUnit.set(DistanceUnit.INCH);
        c.offsetUnits.set(DistanceUnit.INCH);
    });

    /**
     * Foresight Tuner output. <b>Not tuned yet — intentionally empty.</b> Its twelve required
     * values have no defaults and must not be guessed; {@code Constants.createAlgorithm()} refuses
     * to build a Foresight until they are pasted in.
     */
    public static ForesightConfig foresightConfig = new ForesightConfig(c -> {
        // Paste the Foresight Tuner's output (run on 19429) over this whole field.
    });

    /** Registered in {@code Constants.ROBOTS}. Nothing to change here when pasting. */
    public static RobotConstants constants() {
        return new RobotConstants(Robot19429.class, drivetrainConfig, localizerConfig, foresightConfig);
    }
}
