package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.firstinspires.ftc.teamcode.hardware.DeviceNames;
import org.junit.After;
import org.junit.Test;

/**
 * Where the turret's encoder is read ({@link TurretSubsystem#encoderOnDriveMotor}): the intake's
 * socket, or a drive wheel's, and the drive's wheel log leaving that wheel out.
 */
public class TurretEncoderHostTest {

    private final boolean old = TurretSubsystem.encoderOnDriveMotor;

    @After
    public void restore() {
        TurretSubsystem.encoderOnDriveMotor = old;
    }

    @Test
    public void offItIsTheIntakesSocket() {
        TurretSubsystem.encoderOnDriveMotor = false;
        assertEquals(DeviceNames.INTAKE, TurretSubsystem.encoderHostName());
        assertEquals("no drive wheel gives up its log", -1,
                DriveSubsystem.wheelIndex(TurretSubsystem.encoderHostName()));
    }

    @Test
    public void onItIsTheBackRightWheelsSocket() {
        TurretSubsystem.encoderOnDriveMotor = true;
        assertEquals(DeviceNames.BACK_RIGHT, TurretSubsystem.encoderHostName());
        assertEquals("back-right's log goes NaN", 3, DriveSubsystem.wheelIndex(TurretSubsystem.encoderHostName()));
    }

    /**
     * Never back-left: 20245's back-left socket read 0 at Belding and Fruitport. And never a
     * motor whose encoder something controls on: the launcher wheels and the intake's own.
     */
    @Test
    public void theDriveHostIsAWorkingDriveSocket() {
        assertNotEquals(DeviceNames.BACK_LEFT, DeviceNames.TURRET_ENCODER_DRIVE_HOST);
        assertNotEquals(-1, DriveSubsystem.wheelIndex(DeviceNames.TURRET_ENCODER_DRIVE_HOST));
    }
}
