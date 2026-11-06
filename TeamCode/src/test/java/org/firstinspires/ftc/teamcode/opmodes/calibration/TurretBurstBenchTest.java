package org.firstinspires.ftc.teamcode.opmodes.calibration;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class TurretBurstBenchTest {
    @Test
    public void noMovementBeforeZeroOrWithoutDeadmanOrWhileStopped() {
        assertEquals(0, TurretBurstBench.allowedPower(.12, 0, false, true, false, 30), 0);
        assertEquals(0, TurretBurstBench.allowedPower(.12, 0, true, false, false, 30), 0);
        assertEquals(0, TurretBurstBench.allowedPower(.12, 0, true, true, true, 30), 0);
        assertEquals(.12, TurretBurstBench.allowedPower(.12, 0, true, true, false, 30), 0);
    }

    @Test
    public void limitsBlockOutwardMovementAndAllowReturning() {
        assertEquals(0, TurretBurstBench.allowedPower(.12, 30, true, true, false, 30), 0);
        assertEquals(0, TurretBurstBench.allowedPower(-.12, -31, true, true, false, 30), 0);
        assertEquals(-.12, TurretBurstBench.allowedPower(-.12, 31, true, true, false, 30), 0);
        assertEquals(.12, TurretBurstBench.allowedPower(.12, -30, true, true, false, 30), 0);
    }

    @Test
    public void badFeedbackOrLimitNeverDrives() {
        for (double bad : new double[] {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            assertEquals(0, TurretBurstBench.allowedPower(.12, bad, true, true, false, 30), 0);
            assertEquals(0, TurretBurstBench.allowedPower(bad, 0, true, true, false, 30), 0);
            assertEquals(0, TurretBurstBench.allowedPower(.12, 0, true, true, false, bad), 0);
        }
        for (double bad : new double[] {-30, 0, 181}) {
            assertEquals(0, TurretBurstBench.allowedPower(.12, 0, true, true, false, bad), 0);
        }
    }
}
