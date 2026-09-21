package org.firstinspires.ftc.teamcode.hardware;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.junit.Test;

import java.lang.reflect.Proxy;

/**
 * Runs {@link HardwareCheck} against a real SDK {@code HardwareMap} with nothing plugged in —
 * the worst case, and the easiest one to build on the JVM.
 *
 * <p>{@link JvmHardwareMap} explains why the SDK map needs one small override on the JVM.
 *
 * <p>There is no Robot Controller here, so reading the active configuration fails. That is
 * useful rather than a gap: it proves a failure there becomes a reported problem, not an
 * exception.
 */
public class HardwareCheckTest {

    private static HardwareMap emptyHardwareMap() {
        return new JvmHardwareMap();
    }

    @Test
    public void prepareNeverThrowsEvenWithNothingConnected() {
        HardwareCheck check = HardwareCheck.prepare(emptyHardwareMap());
        assertFalse(check.isOk());
    }

    /**
     * On the unmodified SDK map every lookup fails on the JVM with {@code NoClassDefFoundError}
     * (see {@link JvmHardwareMap}) — an {@code Error}, not an exception. That makes it a good
     * stand-in for "something unexpected went wrong inside the SDK": it must still come back as a
     * report, never as a crash.
     */
    @Test
    public void anSdkErrorBecomesAReportNotACrash() {
        HardwareCheck check = HardwareCheck.prepare(new HardwareMap(null, null));
        assertFalse(check.isOk());
        assertTrue(check.isMissing(DeviceNames.FRONT_LEFT));
    }

    @Test
    public void everyMissingDeviceIsReportedByName() {
        HardwareCheck check = HardwareCheck.prepare(emptyHardwareMap());
        String all = String.join("\n", check.problems());
        for (DeviceNames.Device device : DeviceNames.ALL) {
            assertTrue("no problem names " + device.name + ":\n" + all, all.contains(device.name));
            assertTrue(check.isMissing(device.name));
        }
    }

    @Test
    public void missingMotorsGetStandInsSoLookupsSucceed() {
        HardwareMap hardwareMap = emptyHardwareMap();
        HardwareCheck.prepare(hardwareMap);

        DcMotorEx motor = hardwareMap.get(DcMotorEx.class, DeviceNames.FRONT_LEFT);
        assertTrue(StandIn.is(motor));
        motor.setPower(1.0); // must not throw
    }

    @Test
    public void thePinpointGetsNoStandIn() {
        HardwareMap hardwareMap = emptyHardwareMap();
        HardwareCheck check = HardwareCheck.prepare(hardwareMap);

        assertTrue(check.isMissing(DeviceNames.PINPOINT));
        assertFalse(check.standInsInstalled().contains(DeviceNames.PINPOINT));
        assertEquals(null, hardwareMap.tryGet(Object.class, DeviceNames.PINPOINT));
    }

    /** The SDK keeps one HardwareMap across OpModes, so init runs repeatedly against it. */
    @Test
    public void runningTwiceDoesNotStackStandInsAndStillReports() {
        HardwareMap hardwareMap = emptyHardwareMap();
        HardwareCheck.prepare(hardwareMap);
        HardwareCheck second = HardwareCheck.prepare(hardwareMap);

        assertTrue(second.standInsInstalled().isEmpty());
        assertTrue(second.isMissing(DeviceNames.FRONT_LEFT));
        assertEquals(4, hardwareMap.getAll(DcMotorEx.class).size());
    }

    @Test
    public void inspectChangesNothing() {
        HardwareMap hardwareMap = emptyHardwareMap();
        HardwareCheck check = HardwareCheck.inspect(hardwareMap);

        assertTrue(check.standInsInstalled().isEmpty());
        assertEquals(null, hardwareMap.tryGet(DcMotorEx.class, DeviceNames.FRONT_LEFT));
    }

    /** Someone configured a servo under a drive motor's name. */
    @Test
    public void aRealDeviceOfTheWrongKindIsReportedAndStoodInFor() {
        HardwareMap hardwareMap = emptyHardwareMap();
        hardwareMap.put(DeviceNames.BACK_RIGHT, realServo());

        HardwareCheck check = HardwareCheck.prepare(hardwareMap);

        assertEquals(HardwareCheck.Status.WRONG_TYPE, check.statuses().get(DeviceNames.BACK_RIGHT));
        assertTrue(StandIn.is(hardwareMap.get(DcMotorEx.class, DeviceNames.BACK_RIGHT)));
    }

    /** A servo that is not a {@link StandIn}, so the check treats it as real hardware. */
    private static Servo realServo() {
        return (Servo) Proxy.newProxyInstance(Servo.class.getClassLoader(),
                new Class<?>[] {Servo.class}, (proxy, method, args) -> {
                    if (method.getReturnType() == String.class) return "servo";
                    if (method.getReturnType() == int.class) return 0;
                    if (method.getReturnType() == boolean.class) return proxy == args[0];
                    return null;
                });
    }
}
