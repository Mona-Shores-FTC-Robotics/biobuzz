package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** A launch speed changed in Panels reaches a spinning launcher at once (18 Nov 2026). */
public class LaunchSpeedResendTest {

    @Test
    public void aChangedSpeedIsResentWhileSpinning() {
        assertTrue(BasicDriveTeleOp.resendLaunchSpeed(true, false, 1575.0, 1600.0));
        assertTrue(BasicDriveTeleOp.resendLaunchSpeed(true, false, 1575.0, 1574.5));
    }

    @Test
    public void theSameSpeedIsNotResent() {
        // Once sent, the next loop finds it equal: one send, one log line per change.
        assertFalse(BasicDriveTeleOp.resendLaunchSpeed(true, false, 1600.0, 1600.0));
    }

    @Test
    public void idleOrStoppedIsLeftAlone() {
        // The idle speed is not the launch speed: the change waits for the next spin-up.
        assertFalse(BasicDriveTeleOp.resendLaunchSpeed(false, false, Double.NaN, 1600.0));
        assertFalse(BasicDriveTeleOp.resendLaunchSpeed(false, false, 1575.0, 1600.0));
        assertFalse(BasicDriveTeleOp.resendLaunchSpeed(true, false, Double.NaN, 1600.0));
    }

    @Test
    public void notDuringAFeed() {
        // As the speed table: a setpoint that moves mid-volley would re-arm at speed and the shot count.
        assertFalse(BasicDriveTeleOp.resendLaunchSpeed(true, true, 1575.0, 1600.0));
    }

    @Test
    public void aNaNSpeedIsNeverSent() {
        assertFalse(BasicDriveTeleOp.resendLaunchSpeed(true, false, 1575.0, Double.NaN));
    }
}
