package org.firstinspires.ftc.teamcode.pedro;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.pedropathing.revhub.drivetrains.Mecanum;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.hardware.DeviceNames;
import org.firstinspires.ftc.teamcode.hardware.HardwareCheck;
import org.firstinspires.ftc.teamcode.hardware.JvmHardwareMap;
import org.junit.Test;

/**
 * Guards for updating Pedro Pathing mid-season. None of these stop an update; they tell you, by
 * name, what the update changed that our hardware layer relied on.
 *
 * <p>See the README section "Updating Pedro Pathing" for the whole procedure.
 */
public class PedroCompatibilityTest {

    /**
     * The names Pedro is configured with must be the names the robot has.
     *
     * <p>Two routine steps can silently undo this. Each AutoTune tuner ends on a page of generated
     * Java to paste over a block in {@code Constants.java}, and that block spells the names as
     * string literals. And re-copying the Quickstart on an upgrade replaces {@code Constants.java}
     * with upstream's stub if you are not careful. Either way this fails with the exact name.
     */
    @Test
    public void pedroUsesTheDeviceNamesTheRobotHas() {
        assertEquals(DeviceNames.FRONT_LEFT, Constants.drivetrainConfig.frontLeftName.get());
        assertEquals(DeviceNames.FRONT_RIGHT, Constants.drivetrainConfig.frontRightName.get());
        assertEquals(DeviceNames.BACK_LEFT, Constants.drivetrainConfig.backLeftName.get());
        assertEquals(DeviceNames.BACK_RIGHT, Constants.drivetrainConfig.backRightName.get());
        assertEquals(DeviceNames.PINPOINT, Constants.localizerConfig.name.get());
    }

    /**
     * Canary: Pedro's drivetrain must still run on stand-in motors, because that is what keeps a
     * robot missing a drive motor driving.
     *
     * <p>It works today because {@code Mecanum} fetches motors with
     * {@code hardwareMap.get(DcMotorEx.class, name)} and only calls {@code DcMotorEx} interface
     * methods on them. If a Pedro update changes that — for example by asking for the concrete
     * {@code DcMotorImplEx} — this fails. <b>That is not a reason to skip the update.</b> The
     * worst case is that a missing drive motor goes back to stopping the OpMode with "could not
     * find device", which is what the SDK does anyway; update, and decide separately whether to
     * adapt {@code HardwareCheck}.
     */
    @Test
    public void pedroDrivetrainRunsOnStandInMotors() {
        HardwareMap hardwareMap = new JvmHardwareMap();
        HardwareCheck.prepare(hardwareMap);

        Mecanum drivetrain;
        try {
            drivetrain = Constants.createDrivetrain(hardwareMap);
        } catch (RuntimeException e) {
            throw new AssertionError("Pedro's Mecanum no longer accepts stand-in motors. See this "
                    + "test's javadoc before deciding anything.", e);
        }
        drivetrain.stop();
        drivetrain.currentAmps();
        drivetrain.debug();
    }

    /** Tuning against a do-nothing motor would produce numbers someone then pastes and trusts. */
    @Test
    public void tunersRefuseStandInMotors() {
        HardwareMap hardwareMap = new JvmHardwareMap();
        HardwareCheck.prepare(hardwareMap);

        try {
            Tuning.realDrivetrain(hardwareMap);
            fail("Tuning.realDrivetrain accepted a stand-in motor.");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage(), e.getMessage().contains(DeviceNames.FRONT_LEFT));
        }
    }
}
