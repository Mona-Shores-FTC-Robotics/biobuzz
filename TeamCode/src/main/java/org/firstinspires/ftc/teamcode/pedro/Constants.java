package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.follower.Follower;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * Robot configuration for Pedro Pathing.
 *
 * <p>This and {@link Tuning} are the two files in the {@code pedro} package that are ours rather
 * than upstream's — the rest is the Quickstart copied verbatim and should be re-copied, not
 * patched, on the next Pedro release.
 *
 * <p>Run the tuners in this order; each one produces the values the next one needs:
 *
 * <ol>
 *   <li><b>Mecanum Tuner</b> → the four {@code *Direction} values in {@link #drivetrainConfig}.
 *   <li><b>Pinpoint Tuner</b> → the pod directions and offsets in {@link #localizerConfig}.
 *   <li><b>Foresight Tuner</b> → the whole of {@link #foresightConfig}, which is empty until then.
 *   <li><b>Tests</b> → verifies the result.
 * </ol>
 *
 * <p>Each tuner ends on a page of generated Java. Paste it over the matching block below.
 */
public class Constants {
    /**
     * Drivetrain: four-motor mecanum.
     *
     * <p>The names must match the Robot Controller configuration. The directions are the
     * conventional result for mirror-mounted motors (left side reversed) and are a
     * <b>placeholder</b> — the Mecanum Tuner spins each motor and tells you the real answer.
     */
    public static MecanumConfig drivetrainConfig = new MecanumConfig(c -> {
        c.frontLeftName.set("frontLeft");
        c.frontRightName.set("frontRight");
        c.backLeftName.set("backLeft");
        c.backRightName.set("backRight");

        c.frontLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.backLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.frontRightDirection.set(DcMotorSimple.Direction.FORWARD);
        c.backRightDirection.set(DcMotorSimple.Direction.FORWARD);
    });

    /**
     * Localizer: goBILDA Pinpoint with 4-bar odometry pods.
     *
     * <p>The offsets and pod directions are <b>placeholders</b>. The Pinpoint Tuner derives all
     * four by having you push the robot forward, push it left, and spin it 180°; it then emits a
     * finished {@code PinpointConfig} block to paste over this one. Zero offsets mean the pods are
     * treated as if they sat on the tracking centre, which they don't — heading changes will
     * corrupt the position estimate until the real values are in.
     */
    public static PinpointConfig localizerConfig = new PinpointConfig(c -> {
        c.name.set("pinpoint");
        c.podType.set(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);

        c.xPodOffset.set(0.0);
        c.yPodOffset.set(0.0);
        c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
        c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);

        c.globalDistanceUnit.set(DistanceUnit.INCH);
        c.offsetUnits.set(DistanceUnit.INCH);
    });

    /**
     * Following algorithm: Foresight. <b>Not tuned yet — this block is intentionally empty.</b>
     *
     * <p>Twelve of {@code ForesightConfig}'s variables are declared {@code ConfigVar.required(…)}
     * with no default: {@code headingFeedback}, {@code forwardTranslational},
     * {@code strafeTranslational}, {@code brake}, {@code coast}, the linear, quadratic and heading
     * brake coefficients, both max achievable velocities, and both natural decelerations. Reading
     * an unset one throws {@code IllegalStateException("Config variable has not been set")}. Those
     * twelve are exactly what the Foresight Tuner measures, so there is no sensible default to put
     * here and nothing should be guessed — the numbers are properties of this robot's mass, wheels
     * and battery.
     *
     * <p>Run the Foresight Tuner and paste its generated block in place of this lambda body.
     * {@link #createAlgorithm()} fails with a readable message until you do.
     */
    public static ForesightConfig foresightConfig = new ForesightConfig(c -> {
        // Paste the Foresight Tuner's output here.
    });

    public static Mecanum createDrivetrain(HardwareMap hardwareMap) {
        return new Mecanum(hardwareMap, drivetrainConfig);
    }

    public static PinpointLocalizer createLocalizer(HardwareMap hardwareMap) {
        return new PinpointLocalizer(hardwareMap, localizerConfig);
    }

    /**
     * Builds the Foresight algorithm, checking up front that it has been tuned.
     *
     * <p>Without this check the first unset variable surfaces as a bare
     * {@code IllegalStateException("Config variable has not been set")} partway through following a
     * path — no field name, no hint. The tuner writes all twelve required values in one block, so
     * probing one of them is enough to tell "never tuned" from "tuned".
     */
    public static Foresight createAlgorithm() {
        try {
            foresightConfig.maxAchievableForwardVelocity.get();
        } catch (IllegalStateException e) {
            throw new IllegalStateException(
                    "Foresight has not been tuned. Run the Foresight Tuner in AutoTune and paste "
                            + "its generated block into Constants.foresightConfig.", e);
        }
        return new Foresight(foresightConfig);
    }

    public static Follower create(HardwareMap hardwareMap) {
        return new Follower(createLocalizer(hardwareMap), createDrivetrain(hardwareMap), createAlgorithm());
    }
}
