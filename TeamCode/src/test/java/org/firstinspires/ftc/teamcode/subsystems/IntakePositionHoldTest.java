package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.*;
import org.firstinspires.ftc.teamcode.hardware.DeviceNames;
import org.junit.*;
import java.lang.reflect.Proxy;
import java.util.*;
import static org.junit.Assert.*;

/** Tests actual hardware writes and transitions, without a robot. */
public class IntakePositionHoldTest {
    private IntakeSubsystem intake;
    private FeederSubsystem feeder;
    private int position = 123, target, targets;
    private double power, starsPower;
    private DcMotor.RunMode mode;
    private boolean full = true, reject, holdOff;
    private final List<String> writes = new ArrayList<>();
    private final boolean oldReversed = IntakeSubsystem.reversed;
    private final boolean oldEnabled = IntakeSubsystem.holdWhenFull;
    private final boolean oldPosition = IntakeSubsystem.positionHoldWhenFull;
    private final double oldMax = IntakeSubsystem.positionHoldMaxPower;
    private final double oldStars = IntakeSubsystem.starsPowerWhenFull;

    @Before public void setup() {
        IntakeSubsystem.holdWhenFull = true;
        IntakeSubsystem.positionHoldWhenFull = true;
        IntakeSubsystem.positionHoldMaxPower = 0.3;
        IntakeSubsystem.starsPowerWhenFull = 0;
        HardwareMap map = new HardwareMap(null, null) {
            @Override public <T> T get(Class<? extends T> type, String name) {
                return type.cast(Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{type},
                        (proxy, method, args) -> {
                            String m = method.getName();
                            if (name.equals(DeviceNames.INTAKE)) {
                                if (m.startsWith("set")) writes.add(m + ":" + args[0]);
                                if (m.equals("setMode")) mode = (DcMotor.RunMode) args[0];
                                if (m.equals("setPower")) power = (double) args[0];
                                if (m.equals("setTargetPosition")) { target = (int) args[0]; targets++; }
                                if (m.equals("getCurrentPosition")) return position;
                            }
                            if (name.equals(DeviceNames.INTAKE_STARS) && m.equals("setPower")) starsPower = (double) args[0];
                            if (method.getReturnType() == double.class) return 0.0;
                            if (method.getReturnType() == int.class) return 0;
                            if (method.getReturnType() == boolean.class) return false;
                            return null;
                        }));
            }
        };
        feeder = new FeederSubsystem(map);
        feeder.initialize(); feeder.start();
        intake = new IntakeSubsystem(map, feeder);
        intake.laneFull(() -> full);
        intake.rejectWhile(() -> reject);
        intake.holdOffWhile(() -> holdOff);
        intake.initialize(); writes.clear();
    }
    @After public void restore() {
        IntakeSubsystem.reversed = oldReversed;
        IntakeSubsystem.holdWhenFull = oldEnabled;
        IntakeSubsystem.positionHoldWhenFull = oldPosition;
        IntakeSubsystem.positionHoldMaxPower = oldMax;
        IntakeSubsystem.starsPowerWhenFull = oldStars;
    }
    private void hold() { intake.start(); intake.update(); assertTrue(intake.positionHolding()); }
    private void released() {
        assertFalse(intake.positionHolding());
        assertEquals(DcMotor.RunMode.RUN_WITHOUT_ENCODER, mode);
    }
    @Test public void initNeverHoldsAndStopDisablesCorrection() {
        intake.update(); assertEquals(0, targets); assertEquals(0, power, 0);
        hold(); intake.stop(); released(); assertEquals(0, power, 0);
        intake.update(); assertFalse(intake.positionHolding()); assertEquals(0, power, 0);
    }
    @Test public void targetStaysFixedAndNextHoldRecaptures() {
        hold(); assertEquals(123, target); assertEquals(0, starsPower, 0);
        assertTrue(writes.indexOf("setTargetPosition:123") < writes.indexOf("setMode:RUN_TO_POSITION"));
        position = 110; intake.update();
        assertEquals(123, target); assertEquals(1, targets); assertEquals(13, intake.holdErrorTicks());
        assertEquals(0.3, power, 0);
        full = false; intake.update(); released();
        full = true; intake.update(); assertEquals(110, target); assertEquals(2, targets);
    }
    @Test public void overridesReleasePositionMode() {
        hold(); feeder.feed(); feeder.update(); intake.update(); released();
        feeder.idle(); feeder.update(); intake.update(); assertTrue(intake.positionHolding());
        feeder.reverse(); feeder.update(); intake.update(); released(); assertTrue(power < 0);
        feeder.idle(); feeder.update(); intake.update();
        intake.eject(); intake.update(); released(); assertTrue(power < 0);
        intake.idle(); intake.update();
        reject = true; intake.update(); released(); assertTrue(power < 0);
        reject = false; intake.update();
        holdOff = true; intake.update(); released(); assertEquals(0, power, 0);
    }
    @Test public void starsContinueInwardWhileRollerHoldsPosition() {
        IntakeSubsystem.starsPowerWhenFull = 1.0;
        hold();
        assertEquals(IntakeSubsystem.State.IN, intake.starsState());
        assertEquals(IntakeSubsystem.starsReversed ? -1.0 : 1.0, starsPower, 0);
        position = 110; intake.update();
        assertEquals(123, target); assertEquals(1, targets);
    }
    @Test public void unpluggedEncoderCanUseLegacyHoldAndEnableLater() {
        IntakeSubsystem.positionHoldWhenFull = false;
        IntakeSubsystem.starsPowerWhenFull = 1.0;
        intake.start(); intake.update();
        released(); assertEquals(0, targets);
        assertEquals(IntakeSubsystem.unitRange(IntakeSubsystem.holdPowerWhenFull), power, 0);
        assertEquals(IntakeSubsystem.starsReversed ? -1.0 : 1.0, starsPower, 0);
        IntakeSubsystem.positionHoldWhenFull = true;
        intake.update(); assertTrue(intake.positionHolding()); assertEquals(123, target);
        IntakeSubsystem.holdWhenFull = false;
        intake.update(); released();
    }
    @Test public void directionChangeReleasesBeforeRecapturing() {
        hold(); writes.clear(); position = -123;
        IntakeSubsystem.reversed = !IntakeSubsystem.reversed;
        intake.update();
        assertEquals(2, targets); assertEquals(-123, target);
        assertTrue(writes.indexOf("setMode:RUN_WITHOUT_ENCODER") < writes.indexOf("setTargetPosition:-123"));
        assertTrue(intake.positionHolding());
    }
    @Test public void liveSettingsReleaseAndClampPower() {
        hold(); IntakeSubsystem.positionHoldMaxPower = Double.NaN; intake.update(); assertEquals(0, power, 0);
        IntakeSubsystem.positionHoldMaxPower = 2; intake.update(); assertEquals(1, power, 0);
        IntakeSubsystem.positionHoldWhenFull = false; intake.update(); released();
        assertEquals(IntakeSubsystem.unitRange(IntakeSubsystem.holdPowerWhenFull), power, 0);
    }
}
