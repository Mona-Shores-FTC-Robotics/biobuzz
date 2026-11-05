package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;

import org.firstinspires.ftc.teamcode.hardware.DeviceNames;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The launcher's velocity PIDF knobs ({@link LauncherSubsystem#velocityP} and the rest): nothing is
 * written while they are NaN, a change is written once and not every loop, NaN goes back to what
 * the hub had at init, a typo keeps the hub's gain, and F follows the battery only when switched on
 * and only by more than a small step. Runs on fake motors that record each write.
 */
public class LauncherGainsTest {

    private static final double EPS = 1e-9;
    /** What the fake hub answers for its velocity PIDF. */
    private static final PIDFCoefficients HUB = new PIDFCoefficients(10.0, 3.0, 0.0, 0.0);

    /** The gains as shipped, put back after each test so no other test sees this one's NaNs. */
    private static final double[] SHIPPED = {LauncherSubsystem.velocityP, LauncherSubsystem.velocityI,
            LauncherSubsystem.velocityD, LauncherSubsystem.velocityF};

    private final Map<String, List<double[]>> writes = new HashMap<>();
    private LauncherSubsystem launcher;

    @Before
    public void build() {
        // These tests start from the hub's own gains, so every knob begins at NaN.
        LauncherSubsystem.velocityP = Double.NaN;
        LauncherSubsystem.velocityI = Double.NaN;
        LauncherSubsystem.velocityD = Double.NaN;
        LauncherSubsystem.velocityF = Double.NaN;
        LauncherSubsystem.compensateF = false;
        LauncherSubsystem.nominalVolts = 12.0;
        Map<String, Object> fakes = new HashMap<>();
        for (String name : new String[] {DeviceNames.LAUNCHER_LEFT, DeviceNames.LAUNCHER_RIGHT, DeviceNames.FEEDER}) {
            List<double[]> record = new ArrayList<>();
            writes.put(name, record);
            fakes.put(name, Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {DcMotorEx.class},
                    (proxy, method, args) -> {
                        String m = method.getName();
                        if (m.equals("setVelocityPIDFCoefficients")) {
                            record.add(new double[] {(double) args[0], (double) args[1], (double) args[2], (double) args[3]});
                        }
                        if (m.equals("getPIDFCoefficients")) return new PIDFCoefficients(HUB);
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
    }

    @After
    public void resetKnobs() {
        LauncherSubsystem.velocityP = SHIPPED[0];
        LauncherSubsystem.velocityI = SHIPPED[1];
        LauncherSubsystem.velocityD = SHIPPED[2];
        LauncherSubsystem.velocityF = SHIPPED[3];
        LauncherSubsystem.compensateF = false;
        LauncherSubsystem.nominalVolts = 12.0;
    }

    private List<double[]> left() {
        return writes.get(DeviceNames.LAUNCHER_LEFT);
    }

    private void loops(int n) {
        for (int i = 0; i < n; i++) launcher.update();
    }

    @Test
    public void theHubsGainsAreReadAtInitAndNothingIsWrittenWhileTheKnobsAreNaN() {
        loops(50);
        assertEquals(10.0, launcher.hubDefaults().p, EPS);
        assertEquals(3.0, launcher.hubDefaults().i, EPS);
        assertEquals(0, left().size());
        assertEquals(0, writes.get(DeviceNames.LAUNCHER_RIGHT).size());
        assertTrue(Double.isNaN(launcher.appliedF()));
        assertTrue(launcher.health(), launcher.health().contains("P 10 I 3 D 0 F 0"));
        assertEquals("13.5", LauncherSubsystem.gainText(13.5));
        assertEquals("0.0005", LauncherSubsystem.gainText(0.0005));
    }

    @Test
    public void aChangeIsWrittenOnceToBothWheelsWithTheOthersAtTheHubsValues() {
        launcher.update();
        LauncherSubsystem.velocityF = 13.5;
        loops(50);
        assertEquals(1, left().size());
        assertEquals(1, writes.get(DeviceNames.LAUNCHER_RIGHT).size());
        assertArrayEquals(new double[] {10.0, 3.0, 0.0, 13.5}, left().get(0));
        assertEquals(13.5, launcher.appliedF(), EPS);
        assertEquals(1, launcher.pidfWrites());
    }

    @Test
    public void backToNaNWritesWhatTheHubHadAtInit() {
        LauncherSubsystem.velocityP = 25.0;
        launcher.update();
        LauncherSubsystem.velocityP = Double.NaN;
        loops(5);
        assertEquals(2, left().size());
        assertArrayEquals(new double[] {10.0, 3.0, 0.0, 0.0}, left().get(1));
    }

    @Test
    public void aTypoKeepsTheHubsGain() {
        LauncherSubsystem.velocityF = 12.0;
        LauncherSubsystem.velocityP = -4.0;
        LauncherSubsystem.velocityI = Double.POSITIVE_INFINITY;
        LauncherSubsystem.velocityD = 1e9;
        launcher.update();
        assertArrayEquals(new double[] {10.0, 3.0, 0.0, 12.0}, left().get(0));
        assertEquals(10.0, LauncherSubsystem.gain(-4.0, 10.0), EPS);
        assertEquals(0.0, LauncherSubsystem.gain(0.0, 10.0), EPS);
        assertEquals(10.0, LauncherSubsystem.gain(Double.NaN, 10.0), EPS);
    }

    @Test
    public void fFollowsTheBatteryOnlyWhenSwitchedOn() {
        LauncherSubsystem.velocityF = 12.0;
        launcher.batteryVolts(10.0);
        launcher.update();
        assertEquals(12.0, left().get(0)[3], EPS);

        LauncherSubsystem.compensateF = true;
        launcher.update();
        assertEquals(2, left().size());
        assertEquals(12.0 * 12.0 / 10.0, left().get(1)[3], EPS);
        assertEquals(1.2, launcher.fScale(), EPS);

        LauncherSubsystem.compensateF = false;
        launcher.update();
        assertEquals(12.0, left().get(2)[3], EPS);
    }

    @Test
    public void aSmallBatteryChangeIsNotWorthAWriteAndABigOneWaitsItsSecond() {
        LauncherSubsystem.velocityF = 12.0;
        LauncherSubsystem.compensateF = true;
        launcher.batteryVolts(12.0);
        launcher.update();
        assertEquals(1, left().size());

        launcher.batteryVolts(11.95); // under 1%
        loops(5);
        assertEquals(1, left().size());

        launcher.batteryVolts(11.0); // over 1%: the first look is due at once
        launcher.update();
        assertEquals(2, left().size());
        assertEquals(12.0 * 12.0 / 11.0, left().get(1)[3], EPS);

        launcher.batteryVolts(10.0); // within the same second: waits
        loops(5);
        assertEquals(2, left().size());
    }

    @Test
    public void compensationWithNoBatteryReadingChangesNothing() {
        LauncherSubsystem.compensateF = true;
        loops(5);
        assertEquals("all NaN and no reading: the hub's own gains stand", 0, left().size());
    }

    private static void assertArrayEquals(double[] expected, double[] actual) {
        org.junit.Assert.assertArrayEquals(expected, actual, EPS);
    }
}
