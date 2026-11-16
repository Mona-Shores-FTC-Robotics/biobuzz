package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.firstinspires.ftc.teamcode.subsystems.TurretDriveWatch.Fault;

/** {@link TurretDriveWatch}: a turret driven the wrong way, or driven and not turning, is caught. */
public class TurretDriveWatchTest {

    private static final long MS = 1_000_000L;

    private static Fault run(TurretDriveWatch watch, double power, double rate, int ms) {
        Fault fault = Fault.NONE;
        for (int t = 0; t <= ms; t += 15) {
            fault = watch.step(power, rate, 0.15, 0.4, 10.0, 400.0, 700.0, t * MS);
        }
        return fault;
    }

    @Test
    public void drivenAndFollowingIsFine() {
        assertEquals(Fault.NONE, run(new TurretDriveWatch(), 0.8, 150.0, 3000));
        assertEquals(Fault.NONE, run(new TurretDriveWatch(), -0.8, -150.0, 3000));
        assertEquals(Fault.NONE, run(new TurretDriveWatch(), 0.0, 0.0, 3000));
    }

    @Test
    public void turningAgainstThePowerIsAWrongSign() {
        assertEquals(Fault.WRONG_WAY, run(new TurretDriveWatch(), 0.8, -150.0, 450));
        assertEquals("a reversal takes a moment: not yet", Fault.NONE, run(new TurretDriveWatch(), 0.8, -150.0, 300));
    }

    @Test
    public void drivenHardAndStillIsNotMoving() {
        assertEquals(Fault.NOT_MOVING, run(new TurretDriveWatch(), 0.9, 0.0, 750));
        assertEquals(Fault.NOT_MOVING, run(new TurretDriveWatch(), 0.9, Double.NaN, 750));
        assertEquals("a gentle power on a sticky turret is not a fault", Fault.NONE,
                run(new TurretDriveWatch(), 0.2, 0.0, 3000));
    }

    @Test
    public void aBriefDisagreementStartsTheClockAgain() {
        TurretDriveWatch watch = new TurretDriveWatch();
        assertEquals(Fault.NONE, run(watch, 0.8, -150.0, 300));
        assertEquals(Fault.NONE, watch.step(0.8, 150.0, 0.15, 0.4, 10.0, 400.0, 700.0, 315 * MS));
        Fault fault = Fault.NONE;
        for (int t = 330; t <= 630; t += 15) {
            fault = watch.step(0.8, -150.0, 0.15, 0.4, 10.0, 400.0, 700.0, t * MS);
        }
        assertEquals(Fault.NONE, fault);
    }
}
