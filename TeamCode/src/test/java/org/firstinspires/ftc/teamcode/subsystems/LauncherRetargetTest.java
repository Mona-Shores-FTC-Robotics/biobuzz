package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;

import org.firstinspires.ftc.teamcode.hardware.DeviceNames;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * A launcher already spinning that is asked for a new target: a big move (TeleOp's idle speed up
 * to the launch speed) is not at speed from that call on, so a FIRE in the same loop cannot feed
 * at the idle speed; a small one (a trim) keeps at speed. Runs on fake motors whose speed the test
 * sets.
 */
public class LauncherRetargetTest {

    private static final double SHIPPED_HOLD_MS = LauncherSubsystem.atSpeedHoldMs;

    private double wheelTps;
    private LauncherSubsystem launcher;

    @Before
    public void build() {
        LauncherSubsystem.atSpeedHoldMs = 0.0; // at speed on the first reading within tolerance
        Map<String, Object> fakes = new HashMap<>();
        for (String name : new String[] {DeviceNames.LAUNCHER_LEFT, DeviceNames.LAUNCHER_RIGHT, DeviceNames.FEEDER}) {
            boolean wheel = !name.equals(DeviceNames.FEEDER);
            fakes.put(name, Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {DcMotorEx.class},
                    (proxy, method, args) -> {
                        String m = method.getName();
                        if (m.equals("getVelocity")) return wheel ? wheelTps : 0.0;
                        if (m.equals("getPIDFCoefficients")) return new PIDFCoefficients(10.0, 3.0, 0.0, 0.0);
                        Class<?> type = method.getReturnType();
                        if (type == double.class) return 0.0;
                        if (type == boolean.class) return false;
                        if (type == int.class) return 0;
                        return null;
                    }));
        }
        HardwareMap map = new HardwareMap(null, null) {
            @Override
            public <T> T get(Class<? extends T> type, String name) {
                return type.cast(fakes.get(name));
            }
        };
        FeederSubsystem feeder = new FeederSubsystem(map);
        feeder.initialize();
        launcher = new LauncherSubsystem(map, feeder);
        launcher.initialize();
        launcher.start();
    }

    @After
    public void restore() {
        LauncherSubsystem.atSpeedHoldMs = SHIPPED_HOLD_MS;
    }

    private void idlingAtSpeed(double idleTps) {
        launcher.spinUp(idleTps);
        wheelTps = idleTps;
        launcher.update();
        launcher.update();
        assertTrue("idling, at its own speed", launcher.atSpeed());
    }

    @Test
    public void fromTheIdleSpeedUpToTheLaunchSpeedIsNotAtSpeedInTheSameLoop() {
        idlingAtSpeed(1000.0);
        launcher.spinUp(1595.0); // FIRE from idle, before this loop's update()
        assertFalse("must not feed at the idle speed", launcher.atSpeed());
        launcher.update(); // wheels still at 1000
        assertFalse(launcher.atSpeed());
        wheelTps = 1595.0;
        launcher.update();
        assertTrue(launcher.atSpeed());
    }

    @Test
    public void aTrimKeepsAtSpeed() {
        idlingAtSpeed(1595.0);
        launcher.spinUp(1620.0); // one trim press, inside the at-speed tolerance
        assertTrue(launcher.atSpeed());
    }
}
