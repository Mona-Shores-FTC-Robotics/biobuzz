package org.firstinspires.ftc.teamcode.opmodes.calibration;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.PwmControl;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    @Test
    public void swyftPausesDisablePwmInsteadOfSendingTheHomeSignal() {
        List<Double> positions = new ArrayList<>();
        List<String> pwmCalls = new ArrayList<>();
        Servo servo = (Servo) Proxy.newProxyInstance(Servo.class.getClassLoader(),
                new Class<?>[] {Servo.class, PwmControl.class}, (proxy, method, args) -> {
                    if (method.getName().equals("setPosition")) positions.add((Double) args[0]);
                    if (method.getName().equals("setPwmDisable") || method.getName().equals("setPwmEnable")) {
                        pwmCalls.add(method.getName());
                    }
                    return null;
                });
        PwmControl pwm = (PwmControl) servo;
        TurretBurstBench.writeBurst(servo, pwm, 0); // PLAY/trigger released/paused
        assertEquals(0, positions.size());
        assertEquals(Arrays.asList("setPwmDisable"), pwmCalls);
        TurretBurstBench.writeBurst(servo, pwm, 1);
        TurretBurstBench.writeBurst(servo, pwm, 0); // between bursts
        TurretBurstBench.writeBurst(servo, pwm, 1); // same endpoint must re-enable
        TurretBurstBench.writeBurst(servo, pwm, -1);
        TurretBurstBench.writeBurst(servo, pwm, Double.NaN);
        assertEquals(Arrays.asList(1.0, 1.0, 0.0), positions); // never 0.5: SWYFT would home
        assertEquals(Arrays.asList("setPwmDisable", "setPwmEnable", "setPwmDisable",
                "setPwmEnable", "setPwmEnable", "setPwmDisable"), pwmCalls);
    }
}
